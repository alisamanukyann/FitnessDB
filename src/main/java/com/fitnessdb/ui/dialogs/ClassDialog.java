package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ClassDialog extends BaseDialog {

    private final Integer classId;
    private JTextField tfName, tfCap;
    private JTextArea  taDesc;

    public ClassDialog(Frame owner, Integer classId) {
        super(owner, classId == null ? "➕  Add Class" : "✎  Edit Class");
        this.classId = classId;

        tfName = UIUtils.formField("Class name");
        tfCap  = UIUtils.formField("e.g. 20");
        taDesc = makeTextArea(5);

        addFormRow2("Class Name *", tfName, "Max Capacity *", tfCap);
        addFormRow("Description", new JScrollPane(taDesc) {{ setBorder(taDesc.getBorder()); setOpaque(false); }});

        if (classId != null) loadData();
        pack();
        setMinimumSize(new Dimension(480, 330));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return classId == null ? "Add Class" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM Class WHERE class_id=?");
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tfName.setText(rs.getString("class_name"));
                tfCap.setText(String.valueOf(rs.getInt("max_capacity")));
                taDesc.setText(nullStr(rs.getString("description")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfName.getText().trim().isEmpty() || tfCap.getText().trim().isEmpty()) {
            showError("Class name and max capacity are required."); return;
        }
        try {
            int cap = Integer.parseInt(tfCap.getText().trim());
            if (classId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO Class (class_name,max_capacity,description) VALUES(?,?,?)");
                ps.setString(1, tfName.getText().trim()); ps.setInt(2, cap);
                ps.setString(3, emptyNull(taDesc.getText())); ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE Class SET class_name=?,max_capacity=?,description=? WHERE class_id=?");
                ps.setString(1, tfName.getText().trim()); ps.setInt(2, cap);
                ps.setString(3, emptyNull(taDesc.getText())); ps.setInt(4, classId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (NumberFormatException ex) { showError("Max capacity must be a whole number.");
        } catch (SQLException ex)          { showError("Save error:\n" + ex.getMessage()); }
    }
}
