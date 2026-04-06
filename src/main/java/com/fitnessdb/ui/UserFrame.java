package com.fitnessdb.ui;

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
import java.util.*;

public class UserFrame extends JFrame {

    private final int memberId;
    private final String firstName;
    private JLabel breadcrumb;

    public UserFrame(int memberId, String firstName) {
        super("FitnessDB — Member Portal");
        this.memberId  = memberId;
        this.firstName = firstName;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1350, 880);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);
    }

    // ── SIDEBAR ───────────────────────────────────────────────────
    private static final int SW = 240;
    private static final String[][] NAV = {
        {"⊞", "Dashboard",      "DASH"},
        {"◉", "My Profile",     "PROFILE"},
        {"☑", "My Attendance",  "ATTEND"},
        {"▣", "Classes",        "CLASSES"},
        {"★", "Trainers",       "TRAINERS"},
        {"$", "My Payments",    "PAYMENTS"},
        {"◆", "My Goals",       "GOALS"},
    };
    private JToggleButton[] navBtns;
    private ButtonGroup navGroup;
    private CardLayout cardLayout;
    private JPanel contentArea;

    private JPanel buildSidebar() {
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(Theme.SURFACE); g2.fillRect(0,0,getWidth(),getHeight());
                GradientPaint gp=new GradientPaint(0,0,Theme.alpha(Theme.ACCENT2,6),0,280,Theme.alpha(Theme.ACCENT2,0));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),280);
                g2.setColor(Theme.BORDER); g2.fillRect(getWidth()-1,0,1,getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(SW, 0));
        bar.setLayout(new BorderLayout());

        JPanel logo = new JPanel(new BorderLayout(12, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(16,16,16,16)));
        logo.setPreferredSize(new Dimension(SW, 70));

        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,Theme.ACCENT2,38,38,Theme.alpha(Theme.ACCENT2,150));
                g2.setPaint(gp); g2.fillRoundRect(0,0,38,38,8,8);
                g2.setColor(new Color(0x080A0F));
                g2.setFont(new Font("Segoe UI",Font.BOLD,18));
                g2.drawString("M",11,26);
                g2.dispose();
            }
            {setOpaque(false);setPreferredSize(new Dimension(38,38));}
        };
        JPanel txt = new JPanel(new GridLayout(2,1,0,2));
        txt.setOpaque(false);
        txt.add(UIUtils.label("Member Portal", Theme.TEXT, new Font("Segoe UI",Font.BOLD,14)));
        txt.add(UIUtils.label("Hi, "+firstName+"!", Theme.ACCENT2, Theme.FONT_SMALL));
        logo.add(iconBox, BorderLayout.WEST);
        logo.add(txt,     BorderLayout.CENTER);

        JPanel navArea = new JPanel();
        navArea.setOpaque(false);
        navArea.setLayout(new BoxLayout(navArea, BoxLayout.Y_AXIS));
        navArea.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        navGroup = new ButtonGroup();
        navBtns  = new JToggleButton[NAV.length];
        for (int i = 0; i < NAV.length; i++) {
            JToggleButton btn = buildNavBtn(NAV[i][0], NAV[i][1], NAV[i][2]);
            navGroup.add(btn); navBtns[i] = btn; navArea.add(btn);
        }
        navArea.add(Box.createVerticalGlue());

        JScrollPane navScroll = new JScrollPane(navArea);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.getViewport().setBackground(new Color(0,0,0,0));
        navScroll.setBorder(BorderFactory.createEmptyBorder());
        navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(4,0));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,Theme.BORDER),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        JButton logout = UIUtils.btnGhost("← Log Out");
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        footer.add(logout, BorderLayout.CENTER);

        bar.add(logo, BorderLayout.NORTH);
        bar.add(navScroll, BorderLayout.CENTER);
        bar.add(footer, BorderLayout.SOUTH);
        return bar;
    }

    private JToggleButton buildNavBtn(String icon, String label, String key) {
        JToggleButton btn = new JToggleButton() {
            boolean hover;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel=isSelected();
                if (sel) {
                    GradientPaint gp=new GradientPaint(0,0,Theme.alpha(Theme.ACCENT2,22),getWidth(),0,Theme.alpha(Theme.ACCENT2,6));
                    g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                    GradientPaint bar=new GradientPaint(0,0,Theme.ACCENT2,0,getHeight(),Theme.alpha(Theme.ACCENT2,150));
                    g2.setPaint(bar); g2.fillRoundRect(0,5,4,getHeight()-10,4,4);
                } else if (hover) { g2.setColor(Theme.SURFACE2); g2.fillRect(0,0,getWidth(),getHeight()); }
                g2.setFont(new Font("Segoe UI",Font.PLAIN,15));
                g2.setColor(sel?Theme.ACCENT2:(hover?Theme.TEXT:Theme.TEXT2));
                g2.drawString(icon,14,getHeight()/2+6);
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(sel?Theme.TEXT:(hover?Theme.TEXT:Theme.TEXT2));
                g2.drawString(label,40,getHeight()/2+6);
                if (sel) { g2.setColor(Theme.alpha(Theme.ACCENT2,80)); g2.fillOval(getWidth()-16,getHeight()/2-3,6,6); }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(SW,48));
        btn.setMaximumSize(new Dimension(SW,48));
        btn.setMinimumSize(new Dimension(SW,48));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { cardLayout.show(contentArea, key); breadcrumb.setText(label); });
        return btn;
    }

    // ── MAIN AREA ─────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(Theme.SURFACE); g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(Theme.BORDER2); g2.fillRect(0,getHeight()-1,getWidth(),1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0,26,0,26));
        header.setPreferredSize(new Dimension(0,60));

        JPanel bread = new JPanel(new FlowLayout(FlowLayout.LEFT,7,0));
        bread.setOpaque(false);
        bread.add(UIUtils.label("Member Portal",Theme.TEXT2,Theme.FONT_BREAD));
        bread.add(UIUtils.label(" / ",Theme.TEXT3,Theme.FONT_BREAD));
        breadcrumb = UIUtils.label("Dashboard",Theme.TEXT,Theme.FONT_BREAD);
        breadcrumb.setBorder(BorderFactory.createMatteBorder(0,0,2,0,Theme.alpha(Theme.ACCENT2,130)));
        bread.add(breadcrumb);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,15));
        right.setOpaque(false);
        JLabel memberTag = new JLabel("  Member #"+memberId+"  ");
        memberTag.setFont(Theme.FONT_MONO); memberTag.setForeground(Theme.ACCENT2);
        memberTag.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.alpha(Theme.ACCENT2,60),1),
            BorderFactory.createEmptyBorder(3,8,3,8)));
        right.add(memberTag);
        header.add(bread, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG);
        contentArea.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        contentArea.add(buildDashTab(),    "DASH");
        contentArea.add(buildProfileTab(), "PROFILE");
        contentArea.add(buildAttendTab(),  "ATTEND");
        contentArea.add(buildClassesTab(), "CLASSES");
        contentArea.add(buildTrainersTab(),"TRAINERS");
        contentArea.add(buildPaymentsTab(),"PAYMENTS");
        contentArea.add(buildGoalsTab(),   "GOALS");

        main.add(contentArea, BorderLayout.CENTER);
        navBtns[0].setSelected(true);
        return main;
    }

    // ── TAB 1: DASHBOARD with charts ─────────────────────────────
    private JPanel buildDashTab() {
        JPanel p = new JPanel(new BorderLayout(0,18));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("My Dashboard","Your fitness overview at a glance"), BorderLayout.NORTH);

        JPanel kpiRow = new JPanel(new GridLayout(1,4,14,0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(0, 118));

        JPanel chartsRow = new JPanel(new GridLayout(1,3,14,0));
        chartsRow.setOpaque(false);

        try {
            DatabaseManager dm = DatabaseManager.getInstance();

            // ── Attendance rate (attended / total registered)
            ResultSet rs = dm.adminQuery("SELECT COUNT(*) FROM Attendance WHERE member_id="+memberId);
            int totalReg = rs.next() ? rs.getInt(1) : 0; rs.getStatement().close();

            rs = dm.adminQuery("SELECT COUNT(*) FROM Attendance WHERE member_id="+memberId+" AND status='attended'");
            int attended = rs.next() ? rs.getInt(1) : 0; rs.getStatement().close();

            int attendRate = totalReg > 0 ? (int)Math.round(attended * 100.0 / totalReg) : 0;

            // ── Days as member
            rs = dm.adminQuery("SELECT DATEDIFF(DAY, join_date, GETDATE()) FROM Member WHERE member_id="+memberId);
            int daysAsMember = rs.next() ? rs.getInt(1) : 0; rs.getStatement().close();

            // ── Favourite class (most attended)
            rs = dm.adminQuery(
                "SELECT TOP 1 c.class_name, COUNT(*) AS cnt " +
                "FROM Attendance a JOIN ClassSession cs ON a.session_id=cs.session_id " +
                "JOIN Class c ON cs.class_id=c.class_id " +
                "WHERE a.member_id="+memberId+" AND a.status='attended' " +
                "GROUP BY c.class_name ORDER BY cnt DESC");
            String favClass = rs.next() ? rs.getString(1) : "—"; rs.getStatement().close();

            // ── Goals achieved
            rs = dm.adminQuery("SELECT COUNT(*) FROM MemberGoal WHERE member_id="+memberId+" AND status='achieved'");
            int goalsAchieved = rs.next() ? rs.getInt(1) : 0; rs.getStatement().close();

            // ── KPI cards
            kpiRow.add(UIUtils.kpiCard("Attendance Rate", attendRate+"%",
                attended+" attended of "+totalReg+" registered", Theme.ACCENT2));
            kpiRow.add(UIUtils.kpiCard("Member Since", daysAsMember+" days",
                "Days as a member", Theme.ACCENT));
            kpiRow.add(UIUtils.kpiCard("Fav Class", favClass,
                "Most attended class", Theme.SUCCESS));
            kpiRow.add(UIUtils.kpiCard("Goals Achieved", String.valueOf(goalsAchieved),
                "Completed fitness goals", Theme.WARNING));

            // ── Chart 1: Sessions per class (top 5)
            rs = dm.adminQuery(
                "SELECT TOP 5 c.class_name, COUNT(*) AS cnt " +
                "FROM Attendance a JOIN ClassSession cs ON a.session_id=cs.session_id " +
                "JOIN Class c ON cs.class_id=c.class_id " +
                "WHERE a.member_id="+memberId+" " +
                "GROUP BY c.class_name ORDER BY cnt DESC");
            java.util.List<String> classNames = new ArrayList<>();
            java.util.List<Integer> classCounts = new ArrayList<>();
            while (rs.next()) { classNames.add(rs.getString(1)); classCounts.add(rs.getInt(2)); }
            rs.getStatement().close();
            chartsRow.add(buildBarChart("Sessions per Class (Top 5)",
                classNames.toArray(new String[0]),
                classCounts.stream().mapToInt(i->i).toArray(),
                new Color[]{Theme.ACCENT2, Theme.ACCENT4, Theme.SUCCESS, Theme.WARNING, Theme.ACCENT}));

            // ── Chart 2: Activity by day of week
            rs = dm.adminQuery(
                "SELECT DATENAME(WEEKDAY, cs.session_date) AS dow, COUNT(*) AS cnt " +
                "FROM Attendance a JOIN ClassSession cs ON a.session_id=cs.session_id " +
                "WHERE a.member_id="+memberId+" AND a.status='attended' " +
                "GROUP BY DATENAME(WEEKDAY, cs.session_date), DATEPART(WEEKDAY, cs.session_date) " +
                "ORDER BY DATEPART(WEEKDAY, cs.session_date)");
            // Map to short names in order
            Map<String,Integer> dowMap = new LinkedHashMap<>();
            String[] dayOrder = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
            for (String d : dayOrder) dowMap.put(d, 0);
            while (rs.next()) { String d = rs.getString(1); if (dowMap.containsKey(d)) dowMap.put(d, rs.getInt(2)); }
            rs.getStatement().close();
            // Keep only days with data, shorten names
            java.util.List<String> dowLabels = new ArrayList<>();
            java.util.List<Integer> dowVals  = new ArrayList<>();
            for (Map.Entry<String,Integer> e : dowMap.entrySet()) {
                if (e.getValue() > 0) { dowLabels.add(e.getKey().substring(0,3)); dowVals.add(e.getValue()); }
            }
            chartsRow.add(buildBarChart("Activity by Day of Week",
                dowLabels.toArray(new String[0]),
                dowVals.stream().mapToInt(i->i).toArray(),
                new Color[]{Theme.ACCENT4}));

            // ── Chart 3: Goal progress breakdown
            rs = dm.adminQuery(
                "SELECT status, COUNT(*) FROM MemberGoal WHERE member_id="+memberId+" GROUP BY status");
            Map<String,Integer> goalMap = new LinkedHashMap<>();
            goalMap.put("in_progress", 0); goalMap.put("achieved", 0); goalMap.put("abandoned", 0);
            while (rs.next()) { String s = rs.getString(1); if (goalMap.containsKey(s)) goalMap.put(s, rs.getInt(2)); }
            rs.getStatement().close();
            String[] gLabels = {"In Progress","Achieved","Abandoned"};
            int[] gVals = { goalMap.get("in_progress"), goalMap.get("achieved"), goalMap.get("abandoned") };
            chartsRow.add(buildBarChart("Goal Progress",
                gLabels, gVals,
                new Color[]{Theme.ACCENT2, Theme.SUCCESS, Theme.DANGER}));

        } catch (SQLException e) {
            kpiRow.add(new JLabel("DB error: "+e.getMessage()));
        }

        JPanel center = new JPanel(new BorderLayout(0,14));
        center.setOpaque(false);
        center.add(kpiRow,    BorderLayout.NORTH);
        center.add(chartsRow, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    /** Simple bar chart painted in a card */
    private JPanel buildBarChart(String title, String[] labels, int[] values, Color[] colors) {
        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  "+title);
        hdr.setFont(Theme.FONT_HEADER); hdr.setForeground(Theme.TEXT);
        hdr.setPreferredSize(new Dimension(0,40));
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2),
            BorderFactory.createEmptyBorder(0,14,0,0)));
        card.add(hdr, BorderLayout.NORTH);

        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE); g2.fillRect(0,0,getWidth(),getHeight());
                if (labels==null||labels.length==0||values==null||values.length==0){g2.dispose();return;}
                int n=labels.length;
                int maxV=0; for (int v:values) if(v>maxV) maxV=v;
                if (maxV==0) maxV=1;
                int padL=14, padR=14, padT=16, padB=38;
                int w=getWidth()-padL-padR;
                int h=getHeight()-padT-padB;
                int barW=Math.max(8,(w/n)-8);
                int gap=(w-(barW*n))/(n+1);
                for (int i=0;i<n;i++){
                    int bh=(int)((double)values[i]/maxV*h);
                    int x=padL+gap+(barW+gap)*i;
                    int y=padT+h-bh;
                    Color c=colors[i%colors.length];
                    GradientPaint gp=new GradientPaint(x,y,Theme.lighten(c,0.3f),x,padT+h,c);
                    g2.setPaint(gp);
                    g2.fillRoundRect(x,y,barW,bh,5,5);
                    // Value label
                    g2.setFont(new Font("Segoe UI",Font.BOLD,11));
                    g2.setColor(Theme.TEXT);
                    String vStr=String.valueOf(values[i]);
                    FontMetrics fm=g2.getFontMetrics();
                    g2.drawString(vStr,x+(barW-fm.stringWidth(vStr))/2,y-4);
                    // Category label
                    g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                    g2.setColor(Theme.TEXT2);
                    String lbl=labels[i].length()>10?labels[i].substring(0,9)+"…":labels[i];
                    g2.drawString(lbl,x+(barW-fm.stringWidth(lbl))/2+2,padT+h+16);
                }
                // Grid lines
                g2.setColor(Theme.alpha(Theme.BORDER2,80));
                g2.setStroke(new BasicStroke(1f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0,new float[]{4},0));
                for (int i=1;i<=4;i++){
                    int y=padT+h-(int)(h*i/4.0);
                    g2.drawLine(padL,y,padL+w,y);
                }
                g2.dispose();
            }
        };
        chart.setBackground(Theme.SURFACE);
        chart.setOpaque(true);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ── TAB 2: MY PROFILE ────────────────────────────────────────
    private JPanel buildProfileTab() {
        JPanel p = new JPanel(new BorderLayout(0,20));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("My Profile","View and update your personal information"), BorderLayout.NORTH);

        JPanel card = buildCard();
        card.setLayout(new BorderLayout(0,18));
        card.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));

        JTextField tfFirst   = UIUtils.formField("First name");
        JTextField tfLast    = UIUtils.formField("Last name");
        JTextField tfEmail   = UIUtils.formField("Email");
        JTextField tfPhone   = UIUtils.formField("Phone");
        JTextField tfDob     = UIUtils.formField("YYYY-MM-DD");
        JTextField tfAddress = UIUtils.formField("Address");

        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery("SELECT * FROM Member WHERE member_id="+memberId);
            if (rs.next()) {
                tfFirst.setText(nullStr(rs.getString("first_name")));
                tfLast.setText(nullStr(rs.getString("last_name")));
                tfEmail.setText(nullStr(rs.getString("email")));
                tfPhone.setText(nullStr(rs.getString("phone")));
                tfDob.setText(nullStr(rs.getString("date_of_birth")));
                tfAddress.setText(nullStr(rs.getString("address")));
            }
            rs.getStatement().close();
        } catch (SQLException ignored) {}

        JPanel form = new JPanel(new GridLayout(6,2,14,10));
        form.setOpaque(false);
        form.add(frmLabel("First Name *")); form.add(tfFirst);
        form.add(frmLabel("Last Name *"));  form.add(tfLast);
        form.add(frmLabel("Email *"));      form.add(tfEmail);
        form.add(frmLabel("Phone"));        form.add(tfPhone);
        form.add(frmLabel("Date of Birth"));form.add(tfDob);
        form.add(frmLabel("Address"));      form.add(tfAddress);

        JLabel status = new JLabel(" ");
        status.setFont(Theme.FONT_SMALL); status.setForeground(Theme.SUCCESS);
        status.setHorizontalAlignment(SwingConstants.CENTER);

        JButton save = UIUtils.btnPrimary("Save Changes");
        save.setPreferredSize(new Dimension(160,36));
        save.addActionListener(e -> {
            try {
                PreparedStatement ps = DatabaseManager.getInstance().getAdminConnection_public()
                    .prepareStatement("UPDATE Member SET first_name=?,last_name=?,email=?,phone=?,date_of_birth=?,address=? WHERE member_id=?");
                ps.setString(1,tfFirst.getText().trim()); ps.setString(2,tfLast.getText().trim());
                ps.setString(3,tfEmail.getText().trim()); ps.setString(4,emptyNull(tfPhone.getText()));
                ps.setString(5,emptyNull(tfDob.getText())); ps.setString(6,emptyNull(tfAddress.getText()));
                ps.setInt(7,memberId); ps.executeUpdate(); ps.close();
                status.setForeground(Theme.SUCCESS); status.setText("✓  Profile updated successfully.");
            } catch (SQLException ex) { status.setForeground(Theme.DANGER); status.setText("✗  "+ex.getMessage()); }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnRow.setOpaque(false); btnRow.add(save);

        card.add(form, BorderLayout.CENTER);
        card.add(status, BorderLayout.SOUTH);
        card.add(btnRow, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    // ── TAB 3: MY ATTENDANCE ─────────────────────────────────────
    private JPanel buildAttendTab() {
        JPanel p = new JPanel(new BorderLayout(0,16));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("My Attendance","History and class registration"), BorderLayout.NORTH);

        // Registration card
        JPanel regCard = buildCard();
        regCard.setLayout(new BorderLayout(0,10));
        regCard.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));
        regCard.setPreferredSize(new Dimension(0, 130));

        JLabel regTitle = new JLabel("Register for an Upcoming Session");
        regTitle.setFont(Theme.FONT_HEADER); regTitle.setForeground(Theme.ACCENT2);

        // Load upcoming sessions
        JComboBox<String> cbSession = makeDropdown();
        java.util.List<Integer> sidList = new ArrayList<>();
        try {
            ResultSet rs = DatabaseManager.getInstance().adminQuery(
                "SELECT cs.session_id, " +
                "c.class_name+' — '+CAST(cs.session_date AS VARCHAR)+' @ '+CONVERT(VARCHAR(5),cs.start_time,108)+' | Room: '+ISNULL(cs.room,'?')+' | Spots: '+CAST(c.max_capacity-cs.current_enrollment AS VARCHAR) " +
                "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id " +
                "WHERE cs.session_date>=CAST(GETDATE() AS DATE) " +
                "AND cs.current_enrollment < c.max_capacity " +
                "AND cs.session_id NOT IN (SELECT session_id FROM Attendance WHERE member_id="+memberId+") " +
                "ORDER BY cs.session_date ASC, cs.start_time ASC");
            while (rs.next()) { sidList.add(rs.getInt(1)); cbSession.addItem(rs.getString(2)); }
            rs.getStatement().close();
        } catch (Exception ignored) {}

        JLabel regStatus = new JLabel(sidList.isEmpty() ? "No upcoming sessions available to register." : " ");
        regStatus.setFont(Theme.FONT_SMALL); regStatus.setForeground(sidList.isEmpty() ? Theme.TEXT3 : Theme.SUCCESS);

        JButton regBtn = UIUtils.btnPrimary("Register");
        regBtn.setPreferredSize(new Dimension(120,36));
        regBtn.setEnabled(!sidList.isEmpty());

        final java.util.List<Integer> finalSidList = sidList;
        regBtn.addActionListener(e -> {
            int idx = cbSession.getSelectedIndex();
            if (idx < 0 || idx >= finalSidList.size()) return;
            int sid = finalSidList.get(idx);
            try {
                CallableStatement cs = DatabaseManager.getInstance().getAdminConnection_public()
                    .prepareCall("{call sp_AddAttendance(?,?)}");
                cs.setInt(1,memberId); cs.setInt(2,sid);
                cs.execute(); cs.close();
                regStatus.setForeground(Theme.SUCCESS);
                regStatus.setText("✓  Registered successfully for session #"+sid+"!");
            } catch (SQLException ex) {
                regStatus.setForeground(Theme.DANGER);
                regStatus.setText("✗  "+ex.getMessage());
            }
        });

        JPanel dropRow = new JPanel(new BorderLayout(10,0));
        dropRow.setOpaque(false);
        dropRow.add(cbSession, BorderLayout.CENTER);
        dropRow.add(regBtn,    BorderLayout.EAST);

        regCard.add(regTitle, BorderLayout.NORTH);
        regCard.add(dropRow,  BorderLayout.CENTER);
        regCard.add(regStatus,BorderLayout.SOUTH);

        // History table
        JPanel histCard = buildCard();
        histCard.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"#","Class","Date","Start","End","Room","Status"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tbl = new JTable(model); UIUtils.styleTable(tbl);
        String attendSql = "SELECT a.attendance_id,c.class_name,cs.session_date,cs.start_time,cs.end_time,cs.room,a.status "+
            "FROM Attendance a JOIN ClassSession cs ON a.session_id=cs.session_id "+
            "JOIN Class c ON cs.class_id=c.class_id "+
            "WHERE a.member_id="+memberId+" ORDER BY cs.session_date DESC";
        loadIntoModel(model, attendSql);

        histCard.add(buildTableToolbar("My Attendance History",model,attendSql), BorderLayout.NORTH);
        histCard.add(UIUtils.scroll(tbl), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, regCard, histCard);
        split.setDividerLocation(140);
        split.setResizeWeight(0.0);
        split.setOpaque(false); split.setBorder(null); split.setDividerSize(8);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    // ── TAB 4: CLASSES ───────────────────────────────────────────
    private JPanel buildClassesTab() {
        JPanel p = new JPanel(new BorderLayout(0,20));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("Classes & Sessions","Available fitness classes and upcoming sessions"), BorderLayout.NORTH);

        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0,46));

        CardLayout cl = new CardLayout();
        JPanel cl_content = new JPanel(cl);
        cl_content.setOpaque(false);

        // Classes tab
        DefaultTableModel mClasses = new DefaultTableModel(new String[]{"ID","Class Name","Max Capacity","Description"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tClasses = new JTable(mClasses); UIUtils.styleTable(tClasses);
        // Hide ID column from user
        tClasses.getColumnModel().getColumn(0).setMinWidth(0);
        tClasses.getColumnModel().getColumn(0).setMaxWidth(0);
        tClasses.getColumnModel().getColumn(0).setWidth(0);
        String classSql = "SELECT class_id,class_name,max_capacity,CAST(description AS VARCHAR(300)) FROM Class ORDER BY class_name";
        loadIntoModel(mClasses, classSql);

        // Upcoming sessions tab — rich display
        DefaultTableModel mSessions = new DefaultTableModel(new String[]{"Class","Trainer","Date","Start","End","Room","Enrolled","Capacity","Spots Left"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tSessions = new JTable(mSessions); UIUtils.styleTable(tSessions);
        String sessionSql =
            "SELECT c.class_name,t.first_name+' '+t.last_name,cs.session_date,cs.start_time,cs.end_time,cs.room,"+
            "cs.current_enrollment,c.max_capacity,(c.max_capacity-cs.current_enrollment) "+
            "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id JOIN Trainer t ON cs.trainer_id=t.trainer_id "+
            "WHERE cs.session_date>=CAST(GETDATE() AS DATE) ORDER BY cs.session_date ASC, cs.start_time ASC";
        loadIntoModel(mSessions, sessionSql);

        JPanel p1 = new JPanel(new BorderLayout()); p1.setOpaque(false);
        p1.add(buildTableToolbar("Classes",mClasses,classSql), BorderLayout.NORTH);
        p1.add(UIUtils.scroll(tClasses), BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout()); p2.setOpaque(false);
        p2.add(buildTableToolbar("Upcoming Sessions — Spots Available",mSessions,sessionSql), BorderLayout.NORTH);
        p2.add(UIUtils.scroll(tSessions), BorderLayout.CENTER);

        cl_content.add(p1,"CLASSES"); cl_content.add(p2,"SESSIONS");

        ButtonGroup bg = new ButtonGroup();
        JToggleButton t1 = buildInnerTab("▣  Classes",  ()->cl.show(cl_content,"CLASSES"),  bg, true);
        JToggleButton t2 = buildInnerTab("◈  Upcoming Sessions", ()->cl.show(cl_content,"SESSIONS"), bg, false);
        tabBar.add(Box.createHorizontalStrut(12));
        tabBar.add(t1); tabBar.add(Box.createHorizontalStrut(4)); tabBar.add(t2);

        card.add(tabBar, BorderLayout.NORTH);
        card.add(cl_content, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ── TAB 5: TRAINERS ──────────────────────────────────────────
    private JPanel buildTrainersTab() {
        JPanel p = new JPanel(new BorderLayout(0,20));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("Trainers & Certifications","Meet our staff and their credentials"), BorderLayout.NORTH);

        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0,46));

        CardLayout cl = new CardLayout();
        JPanel cl_content = new JPanel(cl);
        cl_content.setOpaque(false);

        DefaultTableModel mT = new DefaultTableModel(new String[]{"First Name","Last Name","Email","Phone","Specialization","Hire Date"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tT = new JTable(mT); UIUtils.styleTable(tT);
        String trainSql = "SELECT first_name,last_name,email,phone,specialization,hire_date FROM Trainer ORDER BY last_name";
        loadIntoModel(mT, trainSql);

        DefaultTableModel mC = new DefaultTableModel(new String[]{"Trainer","Certification","Issuing Body","Issue Date","Expiry"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tC = new JTable(mC); UIUtils.styleTable(tC);
        String certSql = "SELECT t.first_name+' '+t.last_name,tc.cert_name,tc.issuing_body,tc.issue_date,tc.expiry_date "+
            "FROM TrainerCertification tc JOIN Trainer t ON tc.trainer_id=t.trainer_id ORDER BY t.last_name";
        loadIntoModel(mC, certSql);

        JPanel p1 = new JPanel(new BorderLayout()); p1.setOpaque(false);
        p1.add(buildTableToolbar("Trainers",mT,trainSql), BorderLayout.NORTH);
        p1.add(UIUtils.scroll(tT), BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout()); p2.setOpaque(false);
        p2.add(buildTableToolbar("Certifications",mC,certSql), BorderLayout.NORTH);
        p2.add(UIUtils.scroll(tC), BorderLayout.CENTER);

        cl_content.add(p1,"TRAINERS"); cl_content.add(p2,"CERTS");

        ButtonGroup bg = new ButtonGroup();
        tabBar.add(Box.createHorizontalStrut(12));
        tabBar.add(buildInnerTab("★  Trainers",       ()->cl.show(cl_content,"TRAINERS"), bg, true));
        tabBar.add(Box.createHorizontalStrut(4));
        tabBar.add(buildInnerTab("✦  Certifications", ()->cl.show(cl_content,"CERTS"),    bg, false));

        card.add(tabBar, BorderLayout.NORTH);
        card.add(cl_content, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ── TAB 6: MY PAYMENTS ───────────────────────────────────────
    private JPanel buildPaymentsTab() {
        JPanel p = new JPanel(new BorderLayout(0,20));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("My Payments","Payment history and active memberships"), BorderLayout.NORTH);

        JPanel card = buildCard();
        card.setLayout(new BorderLayout());

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0,46));

        CardLayout cl = new CardLayout();
        JPanel cl_content = new JPanel(cl);
        cl_content.setOpaque(false);

        String paySql = "SELECT p.payment_date,pl.plan_name,p.amount,p.method FROM Payment p "+
            "JOIN MemberMembership mm ON p.membership_id=mm.membership_id "+
            "JOIN MembershipPlan pl ON mm.plan_id=pl.plan_id "+
            "WHERE p.member_id="+memberId+" ORDER BY p.payment_date DESC";
        DefaultTableModel mPay = new DefaultTableModel(new String[]{"Date","Plan","Amount","Method"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tPay = new JTable(mPay); UIUtils.styleTable(tPay);
        loadIntoModel(mPay, paySql);

        String memSql = "SELECT p.plan_name,mm.start_date,mm.end_date,mm.status,mm.payment_amount "+
            "FROM MemberMembership mm JOIN MembershipPlan p ON mm.plan_id=p.plan_id "+
            "WHERE mm.member_id="+memberId+" ORDER BY mm.start_date DESC";
        DefaultTableModel mMem = new DefaultTableModel(new String[]{"Plan","Start","End","Status","Amount"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tMem = new JTable(mMem); UIUtils.styleTable(tMem);
        loadIntoModel(mMem, memSql);

        JPanel pp = new JPanel(new BorderLayout()); pp.setOpaque(false);
        pp.add(buildTableToolbar("Payment History",mPay,paySql), BorderLayout.NORTH);
        pp.add(UIUtils.scroll(tPay), BorderLayout.CENTER);

        JPanel pm = new JPanel(new BorderLayout()); pm.setOpaque(false);
        pm.add(buildTableToolbar("Memberships",mMem,memSql), BorderLayout.NORTH);
        pm.add(UIUtils.scroll(tMem), BorderLayout.CENTER);

        cl_content.add(pp,"PAYMENTS"); cl_content.add(pm,"MEMBERSHIPS");

        ButtonGroup bg = new ButtonGroup();
        tabBar.add(Box.createHorizontalStrut(12));
        tabBar.add(buildInnerTab("$  Payments",    ()->cl.show(cl_content,"PAYMENTS"),    bg, true));
        tabBar.add(Box.createHorizontalStrut(4));
        tabBar.add(buildInnerTab("◉  Memberships", ()->cl.show(cl_content,"MEMBERSHIPS"), bg, false));

        card.add(tabBar, BorderLayout.NORTH);
        card.add(cl_content, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ── TAB 7: MY GOALS ──────────────────────────────────────────
    private JPanel buildGoalsTab() {
        JPanel p = new JPanel(new BorderLayout(0,20));
        p.setOpaque(false);
        p.add(UIUtils.sectionHeader("My Goals","Your tracked fitness goals"), BorderLayout.NORTH);

        JPanel card = buildCard();
        card.setLayout(new BorderLayout());
        String sql = "SELECT g.goal_type,g.description,g.target_value,g.target_date,g.status,"+
            "ISNULL(t.first_name+' '+t.last_name,'—'),g.created_date "+
            "FROM MemberGoal g LEFT JOIN Trainer t ON g.trainer_id=t.trainer_id "+
            "WHERE g.member_id="+memberId+" ORDER BY g.created_date DESC";
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Goal Type","Description","Target","Target Date","Status","Trainer","Created"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl = new JTable(model); UIUtils.styleTable(tbl);
        loadIntoModel(model, sql);
        card.add(buildTableToolbar("My Goals",model,sql), BorderLayout.NORTH);
        card.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ── HELPERS ───────────────────────────────────────────────────
    private JPanel buildCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                g2.dispose(); super.paintComponent(g);
            }
            {setOpaque(false);}
        };
    }

    private JPanel buildTableToolbar(String title, DefaultTableModel model, String sql) {
        JPanel tb = new JPanel(new BorderLayout());
        tb.setBackground(Theme.SURFACE2);
        tb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2),
            BorderFactory.createEmptyBorder(10,18,10,18)));
        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(Theme.FONT_TH); lbl.setForeground(Theme.TEXT3);
        JButton ref = UIUtils.btnGhost("↻  Refresh");
        ref.setPreferredSize(new Dimension(100,28));
        ref.addActionListener(e -> loadIntoModel(model,sql));
        tb.add(lbl, BorderLayout.WEST);
        tb.add(ref, BorderLayout.EAST);
        return tb;
    }

    private JToggleButton buildInnerTab(String label, Runnable action, ButtonGroup bg, boolean sel) {
        JToggleButton btn = new JToggleButton(label) {
            boolean hover;
            {addMouseListener(new MouseAdapter(){
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            });}
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                boolean s=isSelected();
                g2.setColor(s?Theme.SURFACE:(hover?Theme.SURFACE3:Theme.SURFACE2));
                g2.fillRect(0,0,getWidth(),getHeight());
                if (s) {
                    GradientPaint gp=new GradientPaint(0,getHeight()-3,Theme.ACCENT2,getWidth(),getHeight()-3,Theme.alpha(Theme.ACCENT2,100));
                    g2.setPaint(gp); g2.fillRect(0,getHeight()-3,getWidth(),3);
                }
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(s?Theme.TEXT:(hover?Theme.TEXT:Theme.TEXT2));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2-1);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220,46));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setSelected(sel);
        btn.addActionListener(e -> action.run());
        bg.add(btn);
        return btn;
    }

    private JComboBox<String> makeDropdown() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(new Color(0x060810));
        cb.setForeground(new Color(0xF4F7FF));
        cb.setFont(Theme.FONT_BODY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER3,1),
            BorderFactory.createEmptyBorder(2,4,2,4)));
        cb.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean isSel,boolean hasFocus){
                super.getListCellRendererComponent(list,value,index,isSel,hasFocus);
                setBackground(isSel?Theme.alpha(Theme.ACCENT,40):new Color(0x0D1017));
                setForeground(new Color(0xF4F7FF)); setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
                return this;
            }
        });
        return cb;
    }

    private JLabel frmLabel(String t) {
        JLabel l=new JLabel(t);
        l.setFont(new Font("Segoe UI",Font.BOLD,12)); l.setForeground(Theme.TEXT2); return l;
    }

    private void loadIntoModel(DefaultTableModel model, String sql) {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            try {
                ResultSet rs = DatabaseManager.getInstance().adminQuery(sql);
                int cols=rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row=new Object[cols];
                    for (int i=0;i<cols;i++) row[i]=rs.getObject(i+1);
                    model.addRow(row);
                }
                rs.getStatement().close();
            } catch (Exception e) {
                model.addRow(new Object[]{"Error: "+e.getMessage()});
            }
        });
    }

    private String nullStr(String s) { return s==null?"":s; }
    private String emptyNull(String s){ return (s==null||s.trim().isEmpty())?null:s.trim(); }
}
