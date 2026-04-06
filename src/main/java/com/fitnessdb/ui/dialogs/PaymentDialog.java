package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class PaymentDialog extends BaseDialog {

    private final Integer paymentId;
    private JComboBox<String> cbMember, cbMembership, cbMethod;
    private JTextField tfDate, tfAmount;
    private int[] memberIds, membershipIds;

    public PaymentDialog(Frame owner, Integer paymentId) {
        super(owner, paymentId == null ? "➕  Add Payment" : "✎  Edit Payment");
        this.paymentId = paymentId;

        cbMember     = new JComboBox<>(); cbMembership = new JComboBox<>();
        cbMethod     = new JComboBox<>(new String[]{"cash","card","online","bank_transfer"});
        for (JComboBox<?> cb : new JComboBox[]{cbMember, cbMembership, cbMethod}) {
            cb.setBackground(new Color(0x060810));
            cb.setForeground(new Color(0x000000));
            cb.setFont(Theme.FONT_BODY);
            cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER3, 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
            cb.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? Theme.alpha(Theme.ACCENT, 40) : new Color(0x0D1017));
                    setForeground(new Color(0xF4F7FF));
                    setFont(Theme.FONT_BODY);
                    setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                    return this;
                }
            });
        }

        List<Integer> mids = new ArrayList<>(), msids = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { mids.add(rs.getInt(1)); cbMember.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
            rs = DatabaseManager.getInstance().executeQuery(
                "SELECT mm.membership_id, m.first_name+' '+m.last_name+' — '+p.plan_name " +
                "FROM MemberMembership mm JOIN Member m ON mm.member_id=m.member_id " +
                "JOIN MembershipPlan p ON mm.plan_id=p.plan_id ORDER BY mm.membership_id DESC");
            while (rs.next()) { msids.add(rs.getInt(1)); cbMembership.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
        memberIds      = mids.stream().mapToInt(i->i).toArray();
        membershipIds  = msids.stream().mapToInt(i->i).toArray();

        tfDate   = UIUtils.formField("YYYY-MM-DD");
        tfAmount = UIUtils.formField("e.g. 49.99");

        addFormRow("Member *",     cbMember);
        addFormRow("Membership *", cbMembership);
        addFormRow2("Payment Date *", tfDate, "Amount *", tfAmount);
        addFormRow("Method *", cbMethod);

        if (paymentId != null) loadData();
        pack();
        setMinimumSize(new Dimension(520, 400));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return paymentId == null ? "Add Payment" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM Payment WHERE payment_id=?");
            ps.setInt(1, paymentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int mid = rs.getInt("member_id"), msid = rs.getInt("membership_id");
                for (int i = 0; i < memberIds.length; i++)     if (memberIds[i]==mid)     { cbMember.setSelectedIndex(i); break; }
                for (int i = 0; i < membershipIds.length; i++) if (membershipIds[i]==msid) { cbMembership.setSelectedIndex(i); break; }
                tfDate.setText(nullStr(rs.getString("payment_date")));
                tfAmount.setText(rs.getBigDecimal("amount").toPlainString());
                cbMethod.setSelectedItem(rs.getString("method"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfDate.getText().trim().isEmpty() || tfAmount.getText().trim().isEmpty()) {
            showError("Date and amount are required."); return;
        }
        int mid  = memberIds[cbMember.getSelectedIndex()];
        int msid = membershipIds[cbMembership.getSelectedIndex()];
        try {
            double amount = Double.parseDouble(tfAmount.getText().trim());
            if (paymentId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO Payment (member_id,membership_id,payment_date,amount,method) VALUES(?,?,?,?,?)");
                ps.setInt(1,mid); ps.setInt(2,msid); ps.setString(3,tfDate.getText().trim());
                ps.setDouble(4,amount); ps.setString(5,(String)cbMethod.getSelectedItem());
                ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE Payment SET member_id=?,membership_id=?,payment_date=?,amount=?,method=? WHERE payment_id=?");
                ps.setInt(1,mid); ps.setInt(2,msid); ps.setString(3,tfDate.getText().trim());
                ps.setDouble(4,amount); ps.setString(5,(String)cbMethod.getSelectedItem());
                ps.setInt(6,paymentId); ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (NumberFormatException ex) { showError("Amount must be a valid number.");
        } catch (SQLException ex)          { showError("Save error:\n" + ex.getMessage()); }
    }
}
