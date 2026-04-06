package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.ClassDialog;
import java.sql.*;
public class ClassesPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Classes"; }
    @Override protected String getPanelSubtitle() { return "Fitness class catalog"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","Class Name","Max Capacity","Description"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT class_id,class_name,max_capacity,CAST(description AS VARCHAR(300)) FROM Class ORDER BY class_id");
    }
    @Override protected void openAddDialog()           { new ClassDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new ClassDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Class WHERE class_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
