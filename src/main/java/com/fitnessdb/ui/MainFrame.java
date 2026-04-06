package com.fitnessdb.ui;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.panels.*;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentArea;
    private JLabel breadcrumbPage;

    private static final Object[][] NAV = {
        { null,  "CORE",                    null,              "group" },
        { "⊞",   "Dashboard",               "DASHBOARD",       "item"  },
        { "◉",   "Members & Goals",         "MEMBERS_GOALS",   "item"  },
        { "★",   "Trainers & Certs",        "TRAINERS_CERTS",  "item"  },
        { null,  "CLASSES",                 null,              "group" },
        { "▣",   "Classes & Sessions",      "CLASSES_SESSIONS","item"  },
        { "☑",   "Attendance",              "ATTENDANCE",      "item"  },
        { null,  "FINANCE",                 null,              "group" },
        { "$",   "Payments",                "PAYMENTS",        "item"  },
        { null,  "DB OBJECTS",              null,              "group" },
        { "▤",   "Views",                   "VIEWS",           "item"  },
        { "⚡",  "Triggers",               "TRIGGERS",        "item"  },
        { "🔎",  "DQL Queries",             "DQL",             "item"  },
        { "⚙",   "Procedures",              "PROCS",           "item"  },
        { "✏",   "Execute SQL",             "SQL",             "item"  },
    };

    private JToggleButton[] navButtons;
    private ButtonGroup navGroup;

    // Sidebar width — enlarged
    private static final int SIDEBAR_W = 280;

    public MainFrame() {
        super("FitnessDB — Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500, 920);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        initUI();
        setVisible(true);
        selectNav("DASHBOARD");
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── SIDEBAR ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                GradientPaint gp = new GradientPaint(0, 0, Theme.alpha(Theme.ACCENT, 5), 0, 320, Theme.alpha(Theme.ACCENT, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 320);
                g2.setColor(Theme.BORDER);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));
        sidebar.setLayout(new BorderLayout());

        sidebar.add(buildLogo(), BorderLayout.NORTH);

        // ── Scrollable nav area
        JPanel navArea = new JPanel();
        navArea.setOpaque(false);
        navArea.setLayout(new BoxLayout(navArea, BoxLayout.Y_AXIS));

        navGroup   = new ButtonGroup();
        navButtons = new JToggleButton[NAV.length];
        int btnCount = 0;
        for (Object[] row : NAV) {
            if ("group".equals(row[3])) {
                navArea.add(buildSectionLabel((String) row[1]));
            } else {
                String icon  = (String) row[0];
                String label = (String) row[1];
                String key   = (String) row[2];
                JToggleButton btn = buildNavItem(icon, label, key);
                navGroup.add(btn);
                navButtons[btnCount++] = btn;
                navArea.add(btn);
            }
        }
        navArea.add(Box.createVerticalGlue());

        JScrollPane navScroll = new JScrollPane(navArea);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.getViewport().setBackground(new Color(0, 0, 0, 0));
        navScroll.setBorder(BorderFactory.createEmptyBorder());
        navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        navScroll.getVerticalScrollBar().setBackground(Theme.SURFACE2);

        sidebar.add(navScroll,          BorderLayout.CENTER);
        sidebar.add(buildSidebarFooter(), BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 76));
        p.setMinimumSize(new Dimension(SIDEBAR_W, 76));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(17, 20, 17, 20)));

        JPanel iconBox = new JPanel() {
            float pulse = 0f;
            javax.swing.Timer anim = new javax.swing.Timer(60, e -> { pulse=(pulse+0.07f)%(float)(Math.PI*2); repaint(); });
            { anim.start(); setOpaque(false); setPreferredSize(new Dimension(42, 42)); setMinimumSize(new Dimension(42, 42)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float glowAlpha = 0.13f + 0.07f*(float)Math.sin(pulse);
                g2.setColor(Theme.alpha(Theme.ACCENT, (int)(glowAlpha*255)));
                g2.fillRoundRect(-4, -4, 50, 50, 14, 14);
                GradientPaint gp = new GradientPaint(0, 0, Theme.lighten(Theme.ACCENT, 0.2f), 42, 42, Theme.ACCENT_DIM);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 42, 42, 10, 10);
                g2.setColor(new Color(0x080A0F));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                g2.drawString("F", 13, 30);
                g2.dispose();
            }
        };

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 3));
        textCol.setOpaque(false);
        textCol.add(UIUtils.label("FitnessDB", Theme.TEXT, Theme.FONT_LOGO));
        textCol.add(UIUtils.label("Admin Panel", Theme.TEXT2, Theme.FONT_LOGO_S));

        p.add(iconBox, BorderLayout.WEST);
        p.add(textCol, BorderLayout.CENTER);
        return p;
    }

    private JLabel buildSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_NAV_LBL);
        l.setForeground(Theme.TEXT3);
        l.setBorder(BorderFactory.createEmptyBorder(20, 14, 7, 20));
        l.setMaximumSize(new Dimension(SIDEBAR_W, 46));
        return l;
    }

    private JToggleButton buildNavItem(String icon, String label, String panelKey) {
        JToggleButton btn = new JToggleButton() {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelected();
                if (sel) {
                    GradientPaint gp = new GradientPaint(0,0, Theme.alpha(Theme.ACCENT,25), getWidth(),0, Theme.alpha(Theme.ACCENT,8));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else if (hover) {
                    g2.setColor(Theme.SURFACE2);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                // Left accent bar
                if (sel) {
                    GradientPaint bar = new GradientPaint(0,0, Theme.ACCENT, 0,getHeight(), Theme.ACCENT_DIM);
                    g2.setPaint(bar);
                    g2.fillRoundRect(0, 5, 4, getHeight()-10, 4, 4);
                }
                // Icon
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 17));
                g2.setColor(sel ? Theme.ACCENT : (hover ? Theme.TEXT : Theme.TEXT2));
                g2.drawString(icon, 14, getHeight()/2 + 6);
                // Label
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(sel ? Theme.TEXT : (hover ? Theme.TEXT : Theme.TEXT2));
                g2.drawString(label, 40, getHeight()/2 + 6);
                // Right dot
                if (sel) {
                    g2.setColor(Theme.alpha(Theme.ACCENT, 90));
                    g2.fillOval(getWidth()-18, getHeight()/2-4, 7, 7);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(SIDEBAR_W, 50));
        btn.setMaximumSize(new Dimension(SIDEBAR_W, 50));
        btn.setMinimumSize(new Dimension(SIDEBAR_W, 50));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showPanel(panelKey, label));
        return btn;
    }

    private JPanel buildSidebarFooter() {
        boolean connected = DatabaseManager.getInstance().testConnection();
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 58));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel dot = new JPanel() {
            float pulse = 0;
            javax.swing.Timer t = new javax.swing.Timer(40, e -> { pulse=(pulse+0.1f)%(float)(Math.PI*2); repaint(); });
            { t.start(); setOpaque(false); setPreferredSize(new Dimension(18, 18)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = connected ? Theme.SUCCESS : Theme.DANGER;
                float a = 0.25f + 0.2f*(float)Math.sin(pulse);
                g2.setColor(Theme.alpha(c, (int)(a*255)));
                g2.fillOval(0, 0, 18, 18);
                g2.setColor(c);
                g2.fillOval(4, 4, 10, 10);
                g2.dispose();
            }
        };

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 2));
        textCol.setOpaque(false);
        textCol.add(UIUtils.label(connected ? "Connected" : "Disconnected",
            connected ? Theme.SUCCESS : Theme.DANGER, Theme.FONT_SMALL));
        textCol.add(UIUtils.label("SQL Server", Theme.TEXT3, Theme.FONT_LOGO_S));

        p.add(dot,     BorderLayout.WEST);
        p.add(textCol, BorderLayout.CENTER);
        return p;
    }

    // ── MAIN AREA ────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildHeader(), BorderLayout.NORTH);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG);
        contentArea.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        contentArea.add(new DashboardPanel(),        "DASHBOARD");
        contentArea.add(new MembersGoalsPanel(),     "MEMBERS_GOALS");
        contentArea.add(new TrainersCertsPanel(),    "TRAINERS_CERTS");
        contentArea.add(new ClassesSessionsPanel(),  "CLASSES_SESSIONS");
        contentArea.add(new AttendancePanel(),       "ATTENDANCE");
        contentArea.add(new PaymentsPanel(),         "PAYMENTS");
        // Separate DB-object panels
        contentArea.add(new ViewsPanel(),            "VIEWS");
        contentArea.add(new TriggersPanel(),         "TRIGGERS");
        contentArea.add(new DqlPanel(),              "DQL");
        contentArea.add(new ProcsPanel(),            "PROCS");
        contentArea.add(new SqlPanel(),              "SQL");

        main.add(contentArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.SURFACE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER2);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        header.setPreferredSize(new Dimension(0, 62));

        JPanel bread = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        bread.setOpaque(false);
        bread.add(UIUtils.label("FitnessDB", Theme.TEXT2, Theme.FONT_BREAD));
        bread.add(UIUtils.label(" / ", Theme.TEXT3, Theme.FONT_BREAD));
        breadcrumbPage = UIUtils.label("Dashboard", Theme.TEXT, Theme.FONT_BREAD);
        breadcrumbPage.setBorder(BorderFactory.createMatteBorder(0,0,2,0, Theme.alpha(Theme.ACCENT,130)));
        bread.add(breadcrumbPage);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        right.setOpaque(false);

        header.add(bread, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void showPanel(String key, String pageTitle) {
        cardLayout.show(contentArea, key);
        breadcrumbPage.setText(pageTitle);
        breadcrumbPage.repaint();
    }

    private void selectNav(String panelKey) {
        int idx = 0;
        for (Object[] row : NAV) {
            if ("item".equals(row[3])) {
                if (panelKey.equals(row[2]) && navButtons[idx] != null) {
                    navButtons[idx].setSelected(true);
                    showPanel(panelKey, (String) row[1]);
                    break;
                }
                idx++;
            }
        }
    }
}
