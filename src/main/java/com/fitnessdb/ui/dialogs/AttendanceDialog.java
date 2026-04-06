package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class AttendanceDialog extends BaseDialog {

    private final Integer attendanceId;
    private JComboBox<String> cbMember, cbSession, cbStatus;
    private JTextField tfDate;
    private int[] memberIds, sessionIds;

    public AttendanceDialog(Frame owner, Integer attendanceId) {
        super(owner, attendanceId == null ? "➕  Add Attendance" : "✎  Edit Attendance");
        this.attendanceId = attendanceId;

        cbMember  = new JComboBox<>(); cbSession = new JComboBox<>();
        cbStatus  = new JComboBox<>(new String[]{"attended", "no-show"});
        for (JComboBox<?> cb : new JComboBox[]{cbMember, cbSession, cbStatus}) {
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

        List<Integer> mids = new ArrayList<>(), sids = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { mids.add(rs.getInt(1)); cbMember.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
            rs = DatabaseManager.getInstance().executeQuery(
                "SELECT cs.session_id, c.class_name+' ('+CAST(cs.session_date AS VARCHAR)+')' " +
                "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id ORDER BY cs.session_date DESC");
            while (rs.next()) { sids.add(rs.getInt(1)); cbSession.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
        memberIds  = mids.stream().mapToInt(i->i).toArray();
        sessionIds = sids.stream().mapToInt(i->i).toArray();

        tfDate = UIUtils.formField("YYYY-MM-DD");

        addFormRow("Member *",  cbMember);
        addFormRow("Session *", cbSession);
        addFormRow2("Date *", tfDate, "Status *", cbStatus);

        if (attendanceId != null) loadData();
        pack();
        setMinimumSize(new Dimension(520, 360));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return attendanceId == null ? "Add Attendance" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM Attendance WHERE attendance_id=?");
            ps.setInt(1, attendanceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int mid = rs.getInt("member_id"), sid = rs.getInt("session_id");
                for (int i = 0; i < memberIds.length; i++)  if (memberIds[i]==mid)  { cbMember.setSelectedIndex(i); break; }
                for (int i = 0; i < sessionIds.length; i++) if (sessionIds[i]==sid) { cbSession.setSelectedIndex(i); break; }
                tfDate.setText(nullStr(rs.getString("attendance_date")));
                cbStatus.setSelectedItem(rs.getString("status"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        if (tfDate.getText().trim().isEmpty()) { showError("Date is required."); return; }
        int mid = memberIds[cbMember.getSelectedIndex()];
        int sid = sessionIds[cbSession.getSelectedIndex()];
        try {
            if (attendanceId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO Attendance (member_id,session_id,attendance_date,status) VALUES(?,?,?,?)");
                ps.setInt(1,mid); ps.setInt(2,sid);
                ps.setString(3,tfDate.getText().trim()); ps.setString(4,(String)cbStatus.getSelectedItem());
                ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE Attendance SET member_id=?,session_id=?,attendance_date=?,status=? WHERE attendance_id=?");
                ps.setInt(1,mid); ps.setInt(2,sid);
                ps.setString(3,tfDate.getText().trim()); ps.setString(4,(String)cbStatus.getSelectedItem());
                ps.setInt(5,attendanceId); ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
