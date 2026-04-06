package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.MemberDialog;
import java.sql.*;
public class MembersPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Members"; }
    @Override protected String getPanelSubtitle() { return "Manage all registered gym members"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","First Name","Last Name","Email","Phone","Date of Birth","Join Date","Address"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT member_id,first_name,last_name,email,phone,date_of_birth,join_date,address FROM Member ORDER BY member_id DESC");
    }
    @Override protected void openAddDialog()           { new MemberDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new MemberDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Member WHERE member_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
