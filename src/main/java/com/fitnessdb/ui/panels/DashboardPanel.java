package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        build();
    }

    private void build() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UIUtils.sectionHeader("Dashboard", "FitnessDB overview — all tables at a glance"), BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildKpiGrid());
        content.add(Box.createVerticalStrut(20));
        content.add(buildSecondRow());
        content.add(Box.createVerticalStrut(20));
        content.add(buildThirdRow());

        JScrollPane sp = UIUtils.scroll(content);
        sp.getVerticalScrollBar().setUnitIncrement(22);
        add(sp, BorderLayout.CENTER);
    }

    // ── ROW 1 — 4 KPI cards ──────────────────────────────────────
    private JPanel buildKpiGrid() {
        int[] s = loadStats();
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        grid.add(makeKpi("Total Members",   String.valueOf(s[0]), "All registered members",    Theme.ACCENT));
        grid.add(makeKpi("Active Members",  String.valueOf(s[1]), "Active memberships",         Theme.ACCENT2));
        grid.add(makeKpi("Trainers on Staff",String.valueOf(s[2]),"Certified trainers",         Theme.ACCENT4));
        grid.add(makeKpi("Class Sessions",  String.valueOf(s[3]), "Scheduled sessions",         Theme.ACCENT3));
        return grid;
    }

    // ── ROW 2 — 4 more KPI cards ─────────────────────────────────
    private JPanel buildSecondRow() {
        int[] s = loadStats2();
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        grid.add(makeKpi("Total Revenue",    s[0] == -1 ? "—" : "$"+s[0], "Lifetime payments",       Theme.SUCCESS));
        grid.add(makeKpi("Payments / Month", String.valueOf(s[1]),          "This calendar month",     Theme.WARNING));
        grid.add(makeKpi("Goals In Progress",String.valueOf(s[2]),          "Active member goals",     Theme.INFO));
        grid.add(makeKpi("Certifications",   String.valueOf(s[3]),          "Trainer certifications",  Theme.ACCENT3));
        return grid;
    }

    // ── ROW 3 — Recent Members table + Stats sidebar ─────────────
    private JPanel buildThirdRow() {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 460));
        row.add(buildRecentMembersCard(), BorderLayout.CENTER);
        row.add(buildStatsCard(),         BorderLayout.EAST);
        return row;
    }

    private JPanel makeKpi(String label, String value, String sub, Color accent) {
        JPanel p = new JPanel(null) {
            boolean hover;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg1 = hover ? Theme.SURFACE3 : Theme.SURFACE2;
                Color bg2 = hover ? Theme.SURFACE2 : Theme.SURFACE;
                g2.setPaint(new GradientPaint(0,0, bg1, 0,getHeight(), bg2));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(hover ? Theme.alpha(accent, 90) : Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                g2.setPaint(new GradientPaint(0,0, accent, getWidth()*0.7f,0, Theme.alpha(accent,0)));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),3,3,3));
                g2.setColor(Theme.alpha(accent, hover ? 30 : 18));
                g2.fillRoundRect(getWidth()-54, 16, 38, 38, 10, 10);
                g2.setColor(Theme.alpha(accent, hover ? 70 : 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(getWidth()-54, 16, 38, 38, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(220, 122));

        JLabel lblLbl = new JLabel(label.toUpperCase());
        lblLbl.setFont(Theme.FONT_TH);
        lblLbl.setForeground(Theme.TEXT3);
        lblLbl.setBounds(18, 20, 170, 15);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(Theme.FONT_KPI);
        valLbl.setForeground(Theme.TEXT);
        valLbl.setBounds(18, 38, 170, 46);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT2);
        subLbl.setBounds(18, 88, 180, 16);

        p.add(lblLbl); p.add(valLbl); p.add(subLbl);
        return p;
    }

    // ── Recent Members card ───────────────────────────────────────
    private JPanel buildRecentMembersCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.SURFACE2);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER2),
            BorderFactory.createEmptyBorder(13,20,13,20)));
        JLabel title = UIUtils.label("Recent Members", Theme.TEXT, Theme.FONT_HEADER);
        JLabel sub   = UIUtils.label("Latest 10 registrations", Theme.TEXT2, Theme.FONT_SMALL);
        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(sub,   BorderLayout.EAST);

        String[] cols = {"Name","Email","Plan","Join Date","Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        JTable tbl = new JTable(model);
        UIUtils.styleTable(tbl);
        tbl.setRowHeight(42);

        // Badge renderer for status column
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
                p.setBackground(sel ? Theme.alpha(Theme.ACCENT,20)
                    : (row%2==1 ? Theme.alpha(Theme.SURFACE2,180) : Theme.SURFACE));
                if (val != null) p.add(UIUtils.badge(val.toString()));
                return p;
            }
        });

        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(
                "SELECT TOP 10 m.first_name+' '+m.last_name,m.email," +
                "p.plan_name,m.join_date,mm.status " +
                "FROM Member m " +
                "LEFT JOIN MemberMembership mm ON m.member_id=mm.member_id " +
                "LEFT JOIN MembershipPlan p ON mm.plan_id=p.plan_id " +
                "ORDER BY m.join_date DESC");
            while (rs.next())
                model.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)});
            rs.getStatement().close();
        } catch (Exception ignored) {}

        card.add(toolbar,          BorderLayout.NORTH);
        card.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        return card;
    }

    // ── Stats sidebar card ────────────────────────────────────────
    private JPanel buildStatsCard() {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 0));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.SURFACE2);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER2),
            BorderFactory.createEmptyBorder(13,20,13,20)));
        toolbar.add(UIUtils.label("Revenue & Activity", Theme.TEXT, Theme.FONT_HEADER), BorderLayout.WEST);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.SURFACE);
        list.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        Object[][] items = {
            {"Total Revenue",       "SELECT '$'+CAST(ROUND(ISNULL(SUM(amount),0),0) AS VARCHAR) FROM Payment",             Theme.ACCENT},
            {"This Month Revenue",  "SELECT '$'+CAST(ROUND(ISNULL(SUM(amount),0),0) AS VARCHAR) FROM Payment WHERE MONTH(payment_date)=MONTH(GETDATE()) AND YEAR(payment_date)=YEAR(GETDATE())", Theme.SUCCESS},
            {"Payments / Month",    "SELECT COUNT(*) FROM Payment WHERE MONTH(payment_date)=MONTH(GETDATE())",              Theme.ACCENT2},
            {"Active Plans",        "SELECT COUNT(*) FROM MemberMembership WHERE status='active'",                          Theme.SUCCESS},
            {"Expired Plans",       "SELECT COUNT(*) FROM MemberMembership WHERE status='expired'",                         Theme.DANGER},
            {"Attendance Records",  "SELECT COUNT(*) FROM Attendance",                                                      Theme.ACCENT4},
            {"No-Shows",            "SELECT COUNT(*) FROM Attendance WHERE status='no-show'",                               Theme.WARNING},
            {"Goals In Progress",   "SELECT COUNT(*) FROM MemberGoal WHERE status='in_progress'",                          Theme.INFO},
            {"Goals Achieved",      "SELECT COUNT(*) FROM MemberGoal WHERE status='achieved'",                             Theme.SUCCESS},
            {"Certifications",      "SELECT COUNT(*) FROM TrainerCertification",                                            Theme.ACCENT3},
            {"Classes",             "SELECT COUNT(*) FROM Class",                                                           Theme.ACCENT4},
            {"Member-Trainer Links","SELECT COUNT(*) FROM MemberTrainer",                                                   Theme.ACCENT2},
        };

        DatabaseManager db = DatabaseManager.getInstance();
        for (Object[] item : items) {
            String label = (String) item[0];
            String sql   = (String) item[1];
            Color  color = (Color)  item[2];
            String val = "—";
            try {
                ResultSet rs = db.executeQuery(sql);
                if (rs.next()) val = rs.getString(1) == null ? "0" : rs.getString(1);
                rs.getStatement().close();
            } catch (Exception ignored) {}
            list.add(buildStatRow(label, val, color));
            list.add(new JSeparator() {{
                setForeground(Theme.BORDER); setBackground(Theme.BORDER);
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            }});
        }

        card.add(toolbar,          BorderLayout.NORTH);
        card.add(UIUtils.scroll(list), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatRow(String label, String val, Color accent) {
        JPanel row = new JPanel(new BorderLayout()) {
            boolean hover;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                g.setColor(hover ? Theme.alpha(accent,9) : Theme.SURFACE);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setMinimumSize(new Dimension(0, 42));
        row.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,8, accent, 0,getHeight()-8, Theme.alpha(accent,80)));
                g2.fillRoundRect(0,8,3,getHeight()-16,3,3);
                g2.dispose();
            }
            { setOpaque(false); setPreferredSize(new Dimension(10,0)); }
        };

        JLabel lbl = UIUtils.label(label, Theme.TEXT2, Theme.FONT_BODY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));

        JLabel valLbl = UIUtils.label(val, Theme.TEXT, Theme.FONT_HEADER);

        row.add(bar,    BorderLayout.WEST);
        row.add(lbl,    BorderLayout.CENTER);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    private int[] loadStats() {
        int[] s = {0,0,0,0};
        try {
            DatabaseManager db = DatabaseManager.getInstance(); ResultSet rs;
            rs=db.executeQuery("SELECT COUNT(*) FROM Member");                              rs.next(); s[0]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM MemberMembership WHERE status='active'"); rs.next(); s[1]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM Trainer");                             rs.next(); s[2]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM ClassSession");                        rs.next(); s[3]=rs.getInt(1); rs.getStatement().close();
        } catch (Exception ignored) {}
        return s;
    }

    private int[] loadStats2() {
        int[] s = {-1,0,0,0};
        try {
            DatabaseManager db = DatabaseManager.getInstance(); ResultSet rs;
            rs=db.executeQuery("SELECT ISNULL(ROUND(SUM(amount),0),0) FROM Payment"); rs.next(); s[0]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM Payment WHERE MONTH(payment_date)=MONTH(GETDATE())"); rs.next(); s[1]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM MemberGoal WHERE status='in_progress'"); rs.next(); s[2]=rs.getInt(1); rs.getStatement().close();
            rs=db.executeQuery("SELECT COUNT(*) FROM TrainerCertification"); rs.next(); s[3]=rs.getInt(1); rs.getStatement().close();
        } catch (Exception ignored) {}
        return s;
    }
}
