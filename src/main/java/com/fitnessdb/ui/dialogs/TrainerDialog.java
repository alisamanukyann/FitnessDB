package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TrainerDialog extends BaseDialog {

    private final Integer trainerId;
    private JTextField tfFirst, tfLast, tfEmail, tfPhone, tfSpec, tfHire;

    public TrainerDialog(Frame owner, Integer trainerId) {
        super(owner, trainerId == null ? "➕  Add Trainer" : "✎  Edit Trainer");
        this.trainerId = trainerId;

        tfFirst = UIUtils.formField("First name");
        tfLast  = UIUtils.formField("Last name");
        tfEmail = UIUtils.formField("email@example.com");
        tfPhone = UIUtils.formField("+1 555 0000");
        tfSpec  = UIUtils.formField("e.g. Yoga, Strength Training");
        tfHire  = UIUtils.formField("YYYY-MM-DD");

        addFormRow2("First Name *", tfFirst,  "Last Name *",    tfLast);
        addFormRow2("Email *",      tfEmail,  "Phone",          tfPhone);
        addFormRow2("Specialization", tfSpec, "Hire Date *",    tfHire);

        if (trainerId != null) loadData();
        pack();
        setMinimumSize(new Dimension(540, 340));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return trainerId == null ? "Add Trainer" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM Trainer WHERE trainer_id=?");
            ps.setInt(1, trainerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfFirst.setText(rs.getString("first_name"));
                tfLast.setText(rs.getString("last_name"));
                tfEmail.setText(rs.getString("email"));
                tfPhone.setText(nullStr(rs.getString("phone")));
                tfSpec.setText(nullStr(rs.getString("specialization")));
                tfHire.setText(nullStr(rs.getString("hire_date")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfFirst.getText().trim().isEmpty() || tfLast.getText().trim().isEmpty() ||
            tfEmail.getText().trim().isEmpty()  || tfHire.getText().trim().isEmpty()) {
            showError("First name, last name, email, and hire date are required."); return;
        }
        try {
            if (trainerId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO Trainer (first_name,last_name,email,phone,specialization,hire_date) VALUES(?,?,?,?,?,?)");
                ps.setString(1, tfFirst.getText().trim()); ps.setString(2, tfLast.getText().trim());
                ps.setString(3, tfEmail.getText().trim()); ps.setString(4, emptyNull(tfPhone.getText()));
                ps.setString(5, emptyNull(tfSpec.getText())); ps.setString(6, tfHire.getText().trim());
                ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE Trainer SET first_name=?,last_name=?,email=?,phone=?,specialization=?,hire_date=? WHERE trainer_id=?");
                ps.setString(1, tfFirst.getText().trim()); ps.setString(2, tfLast.getText().trim());
                ps.setString(3, tfEmail.getText().trim()); ps.setString(4, emptyNull(tfPhone.getText()));
                ps.setString(5, emptyNull(tfSpec.getText())); ps.setString(6, tfHire.getText().trim());
                ps.setInt(7, trainerId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
