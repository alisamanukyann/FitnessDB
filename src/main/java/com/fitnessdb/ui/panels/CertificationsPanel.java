package com.fitnessdb.ui.panels;
import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.CertDialog;
import java.sql.*;
public class CertificationsPanel extends BasePanel {
    @Override protected String getPanelTitle()    { return "Certifications"; }
    @Override protected String getPanelSubtitle() { return "Trainer certifications and credentials"; }
    @Override protected String[] getColumnNames() {
        return new String[]{"ID","Trainer","Certification","Issuing Body","Issue Date","Expiry Date"};
    }
    @Override protected void loadData(String f) {
        populateTable("SELECT tc.cert_id,t.first_name+' '+t.last_name,tc.cert_name,tc.issuing_body,tc.issue_date,tc.expiry_date FROM TrainerCertification tc JOIN Trainer t ON tc.trainer_id=t.trainer_id ORDER BY tc.cert_id DESC");
    }
    @Override protected void openAddDialog()           { new CertDialog(null,null).setVisible(true); }
    @Override protected void openEditDialog(int row)   { new CertDialog(null,(int)cell(row,0)).setVisible(true); }
    @Override protected void deleteSelected(int row)   {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM TrainerCertification WHERE cert_id="+(int)cell(row,0)); }
        catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
    }
}
