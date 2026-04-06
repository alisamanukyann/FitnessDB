package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.SessionDialog;
import java.sql.*;
public class SessionsPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Class Sessions"; }
    @Override protected String getPanelSubtitle() { return "Scheduled class sessions"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","Class","Trainer","Date","Start","End","Room","Enrolled","Capacity"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT cs.session_id,c.class_name,t.first_name+' '+t.last_name,cs.session_date,cs.start_time,cs.end_time,cs.room,cs.current_enrollment,c.max_capacity FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id JOIN Trainer t ON cs.trainer_id=t.trainer_id ORDER BY cs.session_date DESC");
    }
    @Override protected void openAddDialog()           { new SessionDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new SessionDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM ClassSession WHERE session_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
