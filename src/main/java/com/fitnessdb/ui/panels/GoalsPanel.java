package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.GoalDialog;
import java.sql.*;
public class GoalsPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Member Goals"; }
    @Override protected String getPanelSubtitle() { return "Fitness goals tracked per member"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","Member","Trainer","Goal Type","Description","Target","Target Date","Status","Created"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT g.goal_id,m.first_name+' '+m.last_name,ISNULL(t.first_name+' '+t.last_name,'—'),g.goal_type,g.description,g.target_value,g.target_date,g.status,g.created_date FROM MemberGoal g JOIN Member m ON g.member_id=m.member_id LEFT JOIN Trainer t ON g.trainer_id=t.trainer_id ORDER BY g.goal_id DESC");
    }
    @Override protected void openAddDialog()           { new GoalDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new GoalDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM MemberGoal WHERE goal_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
