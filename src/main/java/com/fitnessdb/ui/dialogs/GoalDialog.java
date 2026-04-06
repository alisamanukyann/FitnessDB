package com.fitnessdb.ui.dialogs;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class GoalDialog extends BaseDialog {

    private final Integer goalId;
    private JComboBox<String> cbMember, cbTrainer, cbGoalType, cbStatus;
    private JTextField tfDesc, tfTarget, tfTargetDate;
    private int[] memberIds, trainerIds;

    public GoalDialog(Frame owner, Integer goalId) {
        super(owner, goalId == null ? "➕  Add Member Goal" : "✎  Edit Member Goal");
        this.goalId = goalId;

        cbMember   = new JComboBox<>(); cbTrainer = new JComboBox<>();
        cbGoalType = new JComboBox<>(new String[]{"weight_loss","muscle_gain","endurance","flexibility","general_fitness","rehabilitation","other"});
        cbStatus   = new JComboBox<>(new String[]{"in_progress","achieved","abandoned"});
        for (JComboBox<?> cb : new JComboBox[]{cbMember, cbTrainer, cbGoalType, cbStatus}) {
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

        List<Integer> mids = new ArrayList<>(), tids = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { mids.add(rs.getInt(1)); cbMember.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
            cbTrainer.addItem("— None —"); tids.add(-1);
            rs = DatabaseManager.getInstance().executeQuery(
                "SELECT trainer_id, first_name+' '+last_name FROM Trainer ORDER BY trainer_id ASC");
            while (rs.next()) { tids.add(rs.getInt(1)); cbTrainer.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
        memberIds  = mids.stream().mapToInt(i->i).toArray();
        trainerIds = tids.stream().mapToInt(i->i).toArray();

        tfDesc       = UIUtils.formField("Goal description");
        tfTarget     = UIUtils.formField("e.g. Lose 8 kg");
        tfTargetDate = UIUtils.formField("YYYY-MM-DD");

        addFormRow2("Member *",   cbMember,   "Trainer",     cbTrainer);
        addFormRow2("Goal Type *",cbGoalType, "Status *",    cbStatus);
        addFormRow("Description", tfDesc);
        addFormRow2("Target Value", tfTarget, "Target Date", tfTargetDate);

        if (goalId != null) loadData();
        pack();
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(owner);
    }

    @Override protected String getSaveLabel() { return goalId == null ? "Add Goal" : "Save Changes"; }

    private void loadData() {
        try {
            PreparedStatement ps = DatabaseManager.getInstance()
                .prepareStatement("SELECT * FROM MemberGoal WHERE goal_id=?");
            ps.setInt(1, goalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int mid = rs.getInt("member_id");
                for (int i = 0; i < memberIds.length; i++) if (memberIds[i]==mid) { cbMember.setSelectedIndex(i); break; }
                int tid = rs.getInt("trainer_id");
                if (!rs.wasNull()) for (int i = 0; i < trainerIds.length; i++) if (trainerIds[i]==tid) { cbTrainer.setSelectedIndex(i); break; }
                cbGoalType.setSelectedItem(rs.getString("goal_type"));
                cbStatus.setSelectedItem(rs.getString("status"));
                tfDesc.setText(nullStr(rs.getString("description")));
                tfTarget.setText(nullStr(rs.getString("target_value")));
                tfTargetDate.setText(nullStr(rs.getString("target_date")));
            }
            rs.close(); ps.close();
        } catch (SQLException e) { showError("Load error: " + e.getMessage()); }
    }

    @Override protected void onSave() {
        int mid = memberIds[cbMember.getSelectedIndex()];
        Integer tid = trainerIds[cbTrainer.getSelectedIndex()];
        if (tid == -1) tid = null;
        try {
            if (goalId == null) {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "INSERT INTO MemberGoal (member_id,trainer_id,goal_type,description,target_value,target_date,status,created_date) " +
                    "VALUES(?,?,?,?,?,?,?,CAST(GETDATE() AS DATE))");
                ps.setInt(1,mid);
                if (tid==null) ps.setNull(2,Types.INTEGER); else ps.setInt(2,tid);
                ps.setString(3,(String)cbGoalType.getSelectedItem()); ps.setString(4,emptyNull(tfDesc.getText()));
                ps.setString(5,emptyNull(tfTarget.getText())); ps.setString(6,emptyNull(tfTargetDate.getText()));
                ps.setString(7,(String)cbStatus.getSelectedItem()); ps.executeUpdate(); ps.close();
            } else {
                PreparedStatement ps = DatabaseManager.getInstance().prepareStatement(
                    "UPDATE MemberGoal SET member_id=?,trainer_id=?,goal_type=?,description=?,target_value=?,target_date=?,status=? WHERE goal_id=?");
                ps.setInt(1,mid);
                if (tid==null) ps.setNull(2,Types.INTEGER); else ps.setInt(2,tid);
                ps.setString(3,(String)cbGoalType.getSelectedItem()); ps.setString(4,emptyNull(tfDesc.getText()));
                ps.setString(5,emptyNull(tfTarget.getText())); ps.setString(6,emptyNull(tfTargetDate.getText()));
                ps.setString(7,(String)cbStatus.getSelectedItem()); ps.setInt(8,goalId);
                ps.executeUpdate(); ps.close();
            }
            finish();
        } catch (SQLException e) { showError("Save error:\n" + e.getMessage()); }
    }
}
