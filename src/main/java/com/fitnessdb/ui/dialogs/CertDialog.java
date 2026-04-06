package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class CertDialog extends BaseDialog {

    private final Integer certId;
    private JComboBox<String> cbTrainer;
    private JTextField tfName, tfBody, tfIssue, tfExpiry;
    private int[] trainerIds;

    public CertDialog(Frame owner, Integer certId) {
        super(owner, certId == null ? "➕  Add Certification" : "✎  Edit Certification");
        this.certId = certId;

        cbTrainer = new JComboBox<>();
        cbTrainer.setBackground(new java.awt.Color(0x060810));
        cbTrainer.setForeground(new java.awt.Color(0x000000));
        cbTrainer.setFont(com.fitnessdb.util.Theme.FONT_BODY);
        cbTrainer.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(com.fitnessdb.util.Theme.BORDER3, 1),
            javax.swing.BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        cbTrainer.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? com.fitnessdb.util.Theme.alpha(com.fitnessdb.util.Theme.ACCENT, 40) : new java.awt.Color(0x0D1017));
                setForeground(new java.awt.Color(0xF4F7FF));
                setFont(com.fitnessdb.util.Theme.FONT_BODY);
                setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });

        List<Integer> ids = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(
                "SELECT trainer_id, first_name+' '+last_name FROM Trainer ORDER BY trainer_id ASC");
            while (rs.next()) { ids.add(rs.getInt(1)); cbTrainer.addItem(rs.getInt(1) + ": " + rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
        trainerIds = ids.stream().mapToInt(i -> i).toArray();

        tfName   = UIUtils.formField("Certification name");
        tfBody   = UIUtils.formField("Issuing body");
        tfIssue  = UIUtils.formField("YYYY-MM-DD");
        tfExpiry = UIUtils.formField("YYYY-MM-DD (optional)");

        addFormRow("Trainer *", cbTrainer);
        addFormRow("Certification Name *", tfName);
        addFormRow2("Issuing Body", tfBody, "Issue Date *", tfIssue);
        addFormRow("Expiry Date", tfExpiry);

        if (certId != null) loadData();
        pack();
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return certId == null ? "Add Certification" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM TrainerCertification WHERE cert_id=?");
            ps.setInt(1, certId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tid = rs.getInt("trainer_id");
                for (int i = 0; i < trainerIds.length; i++)
                    if (trainerIds[i] == tid) { cbTrainer.setSelectedIndex(i); break; }
                tfName.setText(rs.getString("cert_name"));
                tfBody.setText(nullStr(rs.getString("issuing_body")));
                tfIssue.setText(nullStr(rs.getString("issue_date")));
                tfExpiry.setText(nullStr(rs.getString("expiry_date")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfName.getText().trim().isEmpty() || tfIssue.getText().trim().isEmpty()) {
            showError("Certification name and issue date are required."); return;
        }
        int tid = trainerIds[cbTrainer.getSelectedIndex()];
        try {
            if (certId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO TrainerCertification (trainer_id,cert_name,issuing_body,issue_date,expiry_date) VALUES(?,?,?,?,?)");
                ps.setInt(1, tid); ps.setString(2, tfName.getText().trim());
                ps.setString(3, emptyNull(tfBody.getText())); ps.setString(4, tfIssue.getText().trim());
                ps.setString(5, emptyNull(tfExpiry.getText())); ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE TrainerCertification SET trainer_id=?,cert_name=?,issuing_body=?,issue_date=?,expiry_date=? WHERE cert_id=?");
                ps.setInt(1, tid); ps.setString(2, tfName.getText().trim());
                ps.setString(3, emptyNull(tfBody.getText())); ps.setString(4, tfIssue.getText().trim());
                ps.setString(5, emptyNull(tfExpiry.getText())); ps.setInt(6, certId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
