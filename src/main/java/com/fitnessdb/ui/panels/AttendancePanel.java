package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.AttendanceDialog;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.BorderFactory;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends BasePanel {

    @Override protected String getPanelTitle()    { return "Attendance"; }
    @Override protected String getPanelSubtitle() { return "Member class attendance records"; }

    @Override protected String[] getColumnNames() {
        return new String[]{"ID", "Member", "Session ID", "Class", "Date", "Status"};
    }

    @Override protected void loadData(String f) {
        populateTable(
            "SELECT a.attendance_id,m.first_name+' '+m.last_name,a.session_id,c.class_name," +
            "a.attendance_date,a.status FROM Attendance a " +
            "JOIN Member m ON a.member_id=m.member_id " +
            "JOIN ClassSession cs ON a.session_id=cs.session_id " +
            "JOIN Class c ON cs.class_id=c.class_id ORDER BY a.attendance_date DESC");
    }

    // No Add button — attendance is added via sp_AddAttendance
    @Override protected boolean showAddButton() { return false; }

    @Override protected void addToolbarExtras(JPanel toolbar) {
        JButton spBtn = new JButton("⚙  sp_AddAttendance") {
            boolean hover;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { hover=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? Theme.alpha(Theme.ACCENT2, 30) : Theme.alpha(Theme.ACCENT2, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Theme.alpha(Theme.ACCENT2, hover ? 100 : 60));
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(Theme.ACCENT2);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        spBtn.setPreferredSize(new Dimension(175, 34));
        spBtn.addActionListener(e -> openSpDialog());
        toolbar.add(spBtn);
    }

    private void openSpDialog() {
        JDialog dlg = new JDialog((Frame) null, "sp_AddAttendance", true);
        dlg.getContentPane().setBackground(Theme.BG);
        dlg.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.SURFACE2, getWidth(), 0, Theme.SURFACE);
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER2); g2.fillRect(0, getHeight()-1, getWidth(), 1);
                GradientPaint bar = new GradientPaint(0, 0, Theme.ACCENT2, getWidth()*0.5f, 0, Theme.alpha(Theme.ACCENT2, 0));
                g2.setPaint(bar); g2.fillRect(0, 0, getWidth(), 3);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel strip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT2, 0, getHeight(), Theme.alpha(Theme.ACCENT2, 120));
                g2.setPaint(gp); g2.fillRoundRect(0, 2, 4, getHeight()-4, 4, 4);
                g2.dispose();
            }
            { setOpaque(false); setPreferredSize(new Dimension(14, 0)); }
        };

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 4));
        textCol.setOpaque(false);
        JLabel title = new JLabel("sp_AddAttendance");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.ACCENT2);
        JLabel sub = new JLabel("Register a member for a class session (date auto-set from session)");
        sub.setFont(Theme.FONT_SUBTITLE); sub.setForeground(Theme.TEXT3);
        textCol.add(title); textCol.add(sub);
        header.add(strip, BorderLayout.WEST);
        header.add(textCol, BorderLayout.CENTER);
        dlg.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(Theme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 22, 0, 22));

        // Member dropdown — sorted by member_id ASC
        JComboBox<String> cbMember  = makeStyledDropdown();
        // Session dropdown — sorted by session_id ASC
        JComboBox<String> cbSession = makeStyledDropdown();

        List<Integer> memberIds  = new ArrayList<>();
        List<Integer> sessionIds = new ArrayList<>();

        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(
                "SELECT member_id, member_id, first_name+' '+last_name FROM Member ORDER BY member_id ASC");
            while (rs.next()) {
                memberIds.add(rs.getInt(1));
                cbMember.addItem(rs.getInt(2)+": "+rs.getString(3));
            }
            rs.getStatement().close();

            rs = DatabaseManager.getInstance().adminQuery(
                "SELECT cs.session_id, cs.session_id, c.class_name+' ('+CAST(cs.session_date AS VARCHAR)+' @ '+CONVERT(VARCHAR(5),cs.start_time,108)+')' " +
                "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id ORDER BY cs.session_id ASC");
            while (rs.next()) {
                sessionIds.add(rs.getInt(1));
                cbSession.addItem(rs.getInt(2)+": "+rs.getString(3));
            }
            rs.getStatement().close();
        } catch (Exception ignored) {}

        JPanel paramCard = new JPanel(new GridLayout(2, 2, 12, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        paramCard.setOpaque(false);
        paramCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        paramCard.add(makeMono("@member_id"));  paramCard.add(cbMember);
        paramCard.add(makeMono("@session_id")); paramCard.add(cbSession);

        JTextArea out = new JTextArea(5, 40);
        out.setEditable(false);
        out.setBackground(new Color(0x08090E));
        out.setForeground(Theme.TEXT2);
        out.setFont(Theme.FONT_MONO);
        out.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER2),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        out.setLineWrap(true); out.setWrapStyleWord(true);
        out.setText("-- Ready to execute\n-- attendance_date is auto-set from the session's date\n-- sp_AddAttendance @member_id=?, @session_id=?");

        body.add(paramCard, BorderLayout.NORTH);
        body.add(out, BorderLayout.CENTER);
        dlg.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.SURFACE2); g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Theme.BORDER2);  g.fillRect(0, 0, getWidth(), 1);
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton cancel = UIUtils.btnGhost("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton run = UIUtils.btnPrimary("▶  Execute");
        run.setPreferredSize(new Dimension(130, 34));

        final List<Integer> finalMemberIds  = memberIds;
        final List<Integer> finalSessionIds = sessionIds;

        run.addActionListener(e -> {
            if (finalMemberIds.isEmpty() || finalSessionIds.isEmpty()) {
                out.setForeground(Theme.WARNING);
                out.setText("⚠  No members or sessions found.");
                return;
            }
            int midIdx = cbMember.getSelectedIndex();
            int sidIdx = cbSession.getSelectedIndex();
            if (midIdx < 0 || sidIdx < 0) return;
            int mid = finalMemberIds.get(midIdx);
            int sid = finalSessionIds.get(sidIdx);
            try {
                // sp_AddAttendance inserts attendance_date = session_date automatically
                CallableStatement cs = DatabaseManager.getInstance().getAdminConnection_public()
                    .prepareCall("{call sp_AddAttendance(?,?)}");
                cs.setInt(1, mid); cs.setInt(2, sid);
                cs.execute(); cs.close();
                out.setForeground(Theme.SUCCESS);
                out.setText("✓  EXEC sp_AddAttendance\n\n" +
                    "  @member_id  = " + mid + "\n" +
                    "  @session_id = " + sid + "\n\n" +
                    "Attendance inserted. attendance_date set from session_date automatically.\n" +
                    "trg_CheckCapacity evaluated — session was not full.");
                refresh();
            } catch (SQLException ex) {
                out.setForeground(Theme.DANGER);
                out.setText("✗  Error\n\n" + ex.getMessage());
            }
        });

        btnRow.add(cancel); btnRow.add(run);
        footer.add(btnRow, BorderLayout.EAST);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setMinimumSize(new Dimension(560, 480));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JComboBox<String> makeStyledDropdown() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(0x060810));
        cb.setForeground(new Color(0xF4F7FF));
        cb.setFont(Theme.FONT_BODY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER3, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSel, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSel, hasFocus);
                setBackground(isSel ? Theme.alpha(Theme.ACCENT, 40) : new Color(0x0D1017));
                setForeground(new Color(0xF4F7FF)); setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        return cb;
    }

    private JLabel makeMono(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_MONO); l.setForeground(Theme.ACCENT2); return l;
    }

    @Override protected void openAddDialog()         { new AttendanceDialog(null, null).setVisible(true); }
    @Override protected void openEditDialog(int row) { new AttendanceDialog(null, (int) cell(row, 0)).setVisible(true); }
    @Override protected void deleteSelected(int row) {
        try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Attendance WHERE attendance_id=" + (int) cell(row, 0)); }
        catch (SQLException e) { error("Delete failed:\n" + e.getMessage()); }
    }
}
