package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MemberDialog extends BaseDialog {

    private final Integer memberId;
    private JTextField tfFirst, tfLast, tfEmail, tfPhone, tfDob, tfAddress;

    public MemberDialog(Frame owner, Integer memberId) {
        super(owner, memberId == null ? "➕  Add Member" : "✎  Edit Member");
        this.memberId = memberId;

        tfFirst   = UIUtils.formField("John");
        tfLast    = UIUtils.formField("Doe");
        tfEmail   = UIUtils.formField("john@example.com");
        tfPhone   = UIUtils.formField("+1 555 0000");
        tfDob     = UIUtils.formField("YYYY-MM-DD");
        tfAddress = UIUtils.formField("123 Main St");

        addFormRow2("First Name *", tfFirst,   "Last Name *",   tfLast);
        addFormRow2("Email *",      tfEmail,   "Phone",         tfPhone);
        addFormRow2("Date of Birth",tfDob,     "Address",       tfAddress);

        if (memberId != null) loadData();
        pack();
        setMinimumSize(new Dimension(540, 360));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return memberId == null ? "Add Member" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM Member WHERE member_id=?");
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfFirst.setText(rs.getString("first_name"));
                tfLast.setText(rs.getString("last_name"));
                tfEmail.setText(rs.getString("email"));
                tfPhone.setText(nullStr(rs.getString("phone")));
                tfDob.setText(nullStr(rs.getString("date_of_birth")));
                tfAddress.setText(nullStr(rs.getString("address")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        String first = tfFirst.getText().trim();
        String last  = tfLast.getText().trim();
        String email = tfEmail.getText().trim();
        if (first.isEmpty() || last.isEmpty() || email.isEmpty()) {
            showError("First name, last name, and email are required."); return;
        }
        try {
            if (memberId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO Member (first_name,last_name,email,phone,date_of_birth,join_date,address) VALUES(?,?,?,?,?,CAST(GETDATE() AS DATE),?)");
                ps.setString(1, first); ps.setString(2, last); ps.setString(3, email);
                ps.setString(4, emptyNull(tfPhone.getText()));
                ps.setString(5, emptyNull(tfDob.getText()));
                ps.setString(6, emptyNull(tfAddress.getText()));
                ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE Member SET first_name=?,last_name=?,email=?,phone=?,date_of_birth=?,address=? WHERE member_id=?");
                ps.setString(1, first); ps.setString(2, last); ps.setString(3, email);
                ps.setString(4, emptyNull(tfPhone.getText()));
                ps.setString(5, emptyNull(tfDob.getText()));
                ps.setString(6, emptyNull(tfAddress.getText()));
                ps.setInt(7, memberId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
