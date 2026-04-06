package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProcsPanel extends DbObjectBasePanel {

    private JComboBox<String> cbMember, cbSession;
    private List<Integer> memberIds  = new ArrayList<>();
    private List<Integer> sessionIds = new ArrayList<>();

    public ProcsPanel() {
        // Header row — title only + global refresh
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(UIUtils.sectionHeader("Stored Procedures", ""), BorderLayout.WEST);

        JButton refreshAll = UIUtils.btnGhost("↻  Refresh");
        refreshAll.setPreferredSize(new Dimension(120, 34));
        refreshAll.addActionListener(e -> reloadDropdowns());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnWrap.setOpaque(false); btnWrap.add(refreshAll);
        headerRow.add(btnWrap, BorderLayout.EAST);
        add(headerRow, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.add(buildSpCard_AddAttendance());
        grid.add(buildSpCard_AssignTrainer());
        add(grid, BorderLayout.CENTER);
    }

    private void reloadDropdowns() {
        if (cbMember == null || cbSession == null) return;
        cbMember.removeAllItems();  cbSession.removeAllItems();
        memberIds.clear();          sessionIds.clear();
        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { memberIds.add(rs.getInt(1)); cbMember.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();

            rs = DatabaseManager.getInstance().adminQuery(
                "SELECT cs.session_id, cs.session_id, c.class_name+' ('+CAST(cs.session_date AS VARCHAR)+' @ '+CONVERT(VARCHAR(5),cs.start_time,108)+')' "+
                "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id ORDER BY cs.session_id ASC");
            while (rs.next()) { sessionIds.add(rs.getInt(1)); cbSession.addItem(rs.getInt(2)+": "+rs.getString(3)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}
    }

    // ── sp_AddAttendance card ─────────────────────────────────────
    private JPanel buildSpCard_AddAttendance() {
        JPanel card = buildCard(Theme.ACCENT2);
        card.setLayout(new BorderLayout());
        card.add(buildCardToolbar("sp_AddAttendance", "PROCEDURE",
            "Register a member for a class session", Theme.ACCENT2), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(Theme.SURFACE);
        body.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));

        // Member dropdown sorted by ID ASC
        cbMember  = makeDropdown();
        cbSession = makeDropdown();

        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { memberIds.add(rs.getInt(1)); cbMember.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();

            rs = DatabaseManager.getInstance().adminQuery(
                "SELECT cs.session_id, cs.session_id, c.class_name+' ('+CAST(cs.session_date AS VARCHAR)+' @ '+CONVERT(VARCHAR(5),cs.start_time,108)+')' "+
                "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id ORDER BY cs.session_id ASC");
            while (rs.next()) { sessionIds.add(rs.getInt(1)); cbSession.addItem(rs.getInt(2)+": "+rs.getString(3)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}

        JPanel form = new JPanel(new GridLayout(2, 2, 12, 10));
        form.setOpaque(false);
        form.add(monoLbl("@member_id",  Theme.ACCENT2)); form.add(cbMember);
        form.add(monoLbl("@session_id", Theme.ACCENT2)); form.add(cbSession);

        JTextArea out = buildOutputArea(4);
        out.setText("-- Ready\n-- attendance_date auto-set from session_date\nEXEC sp_AddAttendance @member_id=?, @session_id=?");

        // Latest attendance table
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Member","Session","Class","Date","Status"}, 0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        JTable resultTbl = buildStyledTable(model);
        String attendSql = "SELECT TOP 10 a.attendance_id,m.first_name+' '+m.last_name,a.session_id,"+
            "c.class_name,a.attendance_date,a.status FROM Attendance a "+
            "JOIN Member m ON a.member_id=m.member_id "+
            "JOIN ClassSession cs ON a.session_id=cs.session_id "+
            "JOIN Class c ON cs.class_id=c.class_id ORDER BY a.attendance_id DESC";
        loadIntoModel(model, attendSql);
        JPanel resultPanel = buildResultPanel("Attendance Records (latest 10)", model, attendSql, resultTbl);

        JButton run = UIUtils.btnPrimary("▶  Execute");
        run.setPreferredSize(new Dimension(140, 36));
        run.addActionListener(e -> {
            if (memberIds.isEmpty() || sessionIds.isEmpty()) {
                out.setForeground(Theme.WARNING); out.setText("⚠  No members or sessions. Click Refresh."); return;
            }
            int midIdx = cbMember.getSelectedIndex();
            int sidIdx = cbSession.getSelectedIndex();
            if (midIdx < 0 || sidIdx < 0) return;
            int mid = memberIds.get(midIdx);
            int sid = sessionIds.get(sidIdx);
            try {
                CallableStatement cs = DatabaseManager.getInstance().getAdminConnection_public()
                    .prepareCall("{call sp_AddAttendance(?,?)}");
                cs.setInt(1, mid); cs.setInt(2, sid);
                cs.execute(); cs.close();
                out.setForeground(Theme.SUCCESS);
                out.setText("✓  EXEC sp_AddAttendance\n  @member_id  = "+mid+
                    "\n  @session_id = "+sid+
                    "\n\nAttendance inserted. Date auto-set from session_date.");
                loadIntoModel(model, attendSql);
            } catch (SQLException ex) {
                out.setForeground(Theme.DANGER);
                out.setText("✗  "+ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false); btnRow.add(run);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(form,   BorderLayout.NORTH);
        top.add(btnRow, BorderLayout.CENTER);
        top.add(out,    BorderLayout.SOUTH);

        body.add(top,         BorderLayout.NORTH);
        body.add(resultPanel, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // ── sp_AssignTrainerToMember card — results open in popup window ─
    private JPanel buildSpCard_AssignTrainer() {
        JPanel card = buildCard(Theme.ACCENT3);
        card.setLayout(new BorderLayout());
        card.add(buildCardToolbar("sp_AssignTrainerToMember", "PROCEDURE",
            "Assign a personal trainer to a member", Theme.ACCENT3), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(Theme.SURFACE);
        body.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));

        // Member + trainer dropdowns sorted by ID ASC
        JComboBox<String> cbM    = makeDropdown();
        JComboBox<String> cbT    = makeDropdown();
        JComboBox<String> cbType = UIUtils.styledCombo(
            "personal_training","nutrition_coaching","rehabilitation","goal_coaching");

        List<Integer> mArr = new ArrayList<>(), tArr = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(
                "SELECT member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) { mArr.add(rs.getInt(1)); cbM.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();

            rs = DatabaseManager.getInstance().adminQuery(
                "SELECT trainer_id, first_name+' '+last_name FROM Trainer ORDER BY trainer_id ASC");
            while (rs.next()) { tArr.add(rs.getInt(1)); cbT.addItem(rs.getInt(1)+": "+rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}

        JPanel form = new JPanel(new GridLayout(3, 2, 12, 10));
        form.setOpaque(false);
        form.add(monoLbl("@member_id",    Theme.ACCENT3)); form.add(cbM);
        form.add(monoLbl("@trainer_id",   Theme.ACCENT3)); form.add(cbT);
        form.add(monoLbl("@session_type", Theme.ACCENT3)); form.add(cbType);

        JTextArea out = buildOutputArea(4);
        out.setText("-- Ready\nEXEC sp_AssignTrainerToMember @member_id=?, @trainer_id=?, @session_type=?");

        String assignSql = "SELECT TOP 15 m.first_name+' '+m.last_name,t.first_name+' '+t.last_name,"+
            "t.specialization,mt.session_type,mt.assigned_date "+
            "FROM MemberTrainer mt JOIN Member m ON mt.member_id=m.member_id "+
            "JOIN Trainer t ON mt.trainer_id=t.trainer_id ORDER BY mt.assigned_date DESC";

        JButton run = UIUtils.btnPrimary("▶  Execute");
        run.setPreferredSize(new Dimension(140, 36));

        JButton viewResults = UIUtils.btnGhost("📋  View Results");
        viewResults.setPreferredSize(new Dimension(140, 36));
        viewResults.addActionListener(e -> showResultsPopup(
            "Member-Trainer Assignments (latest 15)", assignSql,
            new String[]{"Member","Trainer","Specialization","Session Type","Assigned"}));

        final List<Integer> finalMArr = mArr, finalTArr = tArr;
        run.addActionListener(e -> {
            if (finalMArr.isEmpty() || finalTArr.isEmpty()) {
                out.setForeground(Theme.WARNING); out.setText("⚠  No data available."); return;
            }
            int midIdx = cbM.getSelectedIndex();
            int tidIdx = cbT.getSelectedIndex();
            if (midIdx < 0 || tidIdx < 0) return;
            int mid = finalMArr.get(midIdx), tid = finalTArr.get(tidIdx);
            try {
                CallableStatement cs = DatabaseManager.getInstance().getAdminConnection_public()
                    .prepareCall("{call sp_AssignTrainerToMember(?,?,?,?)}");
                cs.setInt(1, mid); cs.setInt(2, tid);
                cs.setString(3, (String)cbType.getSelectedItem());
                cs.setNull(4, java.sql.Types.VARCHAR);
                cs.execute(); cs.close();
                out.setForeground(Theme.SUCCESS);
                out.setText("✓  EXEC sp_AssignTrainerToMember\n  @member_id   = "+mid+
                    "\n  @trainer_id  = "+tid+
                    "\n  @session_type = "+cbType.getSelectedItem()+
                    "\n\nINSERT INTO MemberTrainer executed.");
            } catch (SQLException ex) {
                out.setForeground(Theme.DANGER);
                out.setText("✗  "+ex.getMessage());
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false); btnRow.add(run); btnRow.add(viewResults);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(form,   BorderLayout.NORTH);
        top.add(btnRow, BorderLayout.CENTER);
        top.add(out,    BorderLayout.SOUTH);

        body.add(top, BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /** Opens a resizable popup window showing query results with a close button. */
    private void showResultsPopup(String title, String sql, String[] cols) {
        JDialog dlg = new JDialog((Frame) null, title, false);
        dlg.setSize(820, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.getContentPane().setBackground(Theme.BG);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.SURFACE2); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(Theme.BORDER2); ((Graphics2D)g).fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(14,20,14,20));
        JLabel titleLbl = new JLabel("📋  "+title);
        titleLbl.setFont(Theme.FONT_HEADER); titleLbl.setForeground(Theme.TEXT);
        JButton closeBtn = UIUtils.btnGhost("✕  Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        JButton refreshBtn = UIUtils.btnGhost("↻  Refresh");
        JPanel hdrRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        hdrRight.setOpaque(false); hdrRight.add(refreshBtn); hdrRight.add(closeBtn);
        hdr.add(titleLbl,  BorderLayout.WEST);
        hdr.add(hdrRight,  BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        // Table
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model); UIUtils.styleTable(tbl);
        loadIntoModel(model, sql);
        refreshBtn.addActionListener(e -> loadIntoModel(model, sql));

        JLabel countLbl = new JLabel();
        countLbl.setFont(Theme.FONT_SMALL); countLbl.setForeground(Theme.TEXT3);
        countLbl.setBorder(BorderFactory.createEmptyBorder(8,20,8,20));

        dlg.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        dlg.add(countLbl, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── Shared helpers ────────────────────────────────────────────
    private JComboBox<String> makeDropdown() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(0x060810));
        cb.setForeground(new Color(0x000000));
        cb.setFont(Theme.FONT_BODY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER3, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? Theme.alpha(Theme.ACCENT, 40) : new Color(0x0D1017));
                setForeground(new Color(0xF4F7FF)); setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        return cb;
    }

    private JPanel buildResultPanel(String title, DefaultTableModel model, String sql, JTable tbl) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Theme.SURFACE2);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER),
            BorderFactory.createEmptyBorder(8,16,8,16)));
        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(Theme.FONT_TH); lbl.setForeground(Theme.TEXT3);
        JButton refresh = UIUtils.btnGhost("↻");
        refresh.setPreferredSize(new Dimension(40, 28));
        refresh.addActionListener(e -> loadIntoModel(model, sql));
        hdr.add(lbl, BorderLayout.WEST); hdr.add(refresh, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.SURFACE); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createLineBorder(Theme.BORDER2));
        wrap.add(hdr, BorderLayout.NORTH);
        wrap.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    public void loadIntoModel(DefaultTableModel model, String sql) {
        model.setRowCount(0);
        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(sql);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i=0; i<cols; i++) row[i] = rs.getObject(i+1);
                model.addRow(row);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            model.addRow(new Object[]{"Error: "+e.getMessage()});
        }
    }
}
