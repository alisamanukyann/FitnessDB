package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.TrainerDialog;
import java.sql.*;
public class TrainersPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Trainers"; }
    @Override protected String getPanelSubtitle() { return "All fitness trainers on staff"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","First Name","Last Name","Email","Phone","Specialization","Hire Date"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT trainer_id,first_name,last_name,email,phone,specialization,hire_date FROM Trainer ORDER BY trainer_id DESC");
    }
    @Override protected void openAddDialog()           { new TrainerDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new TrainerDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Trainer WHERE trainer_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
