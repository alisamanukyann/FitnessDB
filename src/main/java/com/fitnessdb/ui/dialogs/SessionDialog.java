package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class SessionDialog extends BaseDialog {

    private final Integer sessionId;
    private JComboBox<String> cbClass, cbTrainer;
    private JTextField tfDate, tfStart, tfEnd, tfRoom;
    private int[] classIds, trainerIds;

    public SessionDialog(Frame owner, Integer sessionId) {
        super(owner, sessionId == null ? "➕  Add Session" : "✎  Edit Session");
        this.sessionId = sessionId;

        cbClass   = makeStyledCombo(); cbTrainer = makeStyledCombo();
        List<Integer> cids = new ArrayList<>(), tids = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery("SELECT class_id,class_name FROM Class ORDER BY class_id ASC");
            while (rs.next()) { cids.add(rs.getInt(1)); cbClass.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
            rs = DatabaseManager.getInstance().executeQuery("SELECT trainer_id,first_name+' '+last_name FROM Trainer ORDER BY trainer_id ASC");
            while (rs.next()) { tids.add(rs.getInt(1)); cbTrainer.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
        classIds   = cids.stream().mapToInt(i->i).toArray();
        trainerIds = tids.stream().mapToInt(i->i).toArray();

        tfDate  = UIUtils.formField("YYYY-MM-DD");
        tfStart = UIUtils.formField("HH:MM:SS");
        tfEnd   = UIUtils.formField("HH:MM:SS");
        tfRoom  = UIUtils.formField("Room 1");

        addFormRow2("Class *", cbClass, "Trainer *", cbTrainer);
        addFormRow("Session Date *", tfDate);
        addFormRow2("Start Time *", tfStart, "End Time *", tfEnd);
        addFormRow("Room", tfRoom);

        if (sessionId != null) loadData();
        pack();
        setMinimumSize(new Dimension(540, 400));
        setLocationRelativeTo(owner);
    }

    private JComboBox<String> makeStyledCombo() {
        JComboBox<String> cb = new JComboBox<>();
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
        return cb;
    }

    @Override protected String getSaveLabel() { return sessionId == null ? "Add Session" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM ClassSession WHERE session_id=?");
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cid = rs.getInt("class_id"), tid = rs.getInt("trainer_id");
                for (int i = 0; i < classIds.length; i++)   if (classIds[i]==cid)   { cbClass.setSelectedIndex(i); break; }
                for (int i = 0; i < trainerIds.length; i++) if (trainerIds[i]==tid) { cbTrainer.setSelectedIndex(i); break; }
                tfDate.setText(nullStr(rs.getString("session_date")));
                tfStart.setText(nullStr(rs.getString("start_time")));
                tfEnd.setText(nullStr(rs.getString("end_time")));
                tfRoom.setText(nullStr(rs.getString("room")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfDate.getText().trim().isEmpty() || tfStart.getText().trim().isEmpty() || tfEnd.getText().trim().isEmpty()) {
            showError("Date, start time, and end time are required."); return;
        }
        int cid = classIds[cbClass.getSelectedIndex()];
        int tid = trainerIds[cbTrainer.getSelectedIndex()];
        try {
            if (sessionId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO ClassSession (class_id,trainer_id,session_date,start_time,end_time,room) VALUES(?,?,?,?,?,?)");
                ps.setInt(1,cid); ps.setInt(2,tid); ps.setString(3,tfDate.getText().trim());
                ps.setString(4,tfStart.getText().trim()); ps.setString(5,tfEnd.getText().trim());
                ps.setString(6,emptyNull(tfRoom.getText())); ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE ClassSession SET class_id=?,trainer_id=?,session_date=?,start_time=?,end_time=?,room=? WHERE session_id=?");
                ps.setInt(1,cid); ps.setInt(2,tid); ps.setString(3,tfDate.getText().trim());
                ps.setString(4,tfStart.getText().trim()); ps.setString(5,tfEnd.getText().trim());
                ps.setString(6,emptyNull(tfRoom.getText())); ps.setInt(7,sessionId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
