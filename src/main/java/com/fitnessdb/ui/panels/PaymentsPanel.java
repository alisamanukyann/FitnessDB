package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.PaymentDialog;
import java.sql.*;
public class PaymentsPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Payments"; }
    @Override protected String getPanelSubtitle() { return "Member payment records"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","Member","Membership ID","Payment Date","Amount","Method"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT p.payment_id,m.first_name+' '+m.last_name,p.membership_id,p.payment_date,p.amount,p.method FROM Payment p JOIN Member m ON p.member_id=m.member_id ORDER BY p.payment_date DESC");
    }
    @Override protected void openAddDialog()           { new PaymentDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new PaymentDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Payment WHERE payment_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
