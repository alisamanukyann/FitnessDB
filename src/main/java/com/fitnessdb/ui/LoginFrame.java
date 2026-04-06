package com.fitnessdb.ui;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;

/**
 * Login screen — shown before MainFrame or UserFrame.
 * Two modes: Admin (existing fitnessdb_user) and Member (email + member_id lookup).
 */
public class LoginFrame extends JFrame {

    private static final int W = 480, H = 580;

    // "ADMIN" or "MEMBER"
    private String selectedRole = "ADMIN";

    // Admin fields
    private JTextField   tfAdminUser;
    private JPasswordField pfAdminPass;

    // Member fields
    private JTextField   tfMemberEmail;
    private JPasswordField pfMemberPass;   // used as "member ID" pin — we verify by email match

    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;

    public LoginFrame() {
        super("FitnessDB — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(W, H);
        setResizable(false);
        setLocationRelativeTo(null);
        setUndecorated(true);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Top accent bar
                GradientPaint bar = new GradientPaint(0, 0, Theme.ACCENT,
                        getWidth() * 0.55f, 0, Theme.alpha(Theme.ACCENT, 0));
                g2.setPaint(bar);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                // Border
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.add(buildTitleBar(),  BorderLayout.NORTH);
        root.add(buildCenter(),    BorderLayout.CENTER);
        return root;
    }

    // ── Draggable title bar ───────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 20, 0, 16));
        bar.setPreferredSize(new Dimension(W, 44));

        JLabel logo = new JLabel("FitnessDB");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logo.setForeground(Theme.TEXT2);
        bar.add(logo, BorderLayout.WEST);

        JButton close = new JButton("✕") {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                if (hover) { g2.setColor(Theme.DANGER); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6); }
                g2.setFont(new Font("Segoe UI",Font.PLAIN,12));
                g2.setColor(hover ? Color.WHITE : Theme.TEXT3);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        close.setPreferredSize(new Dimension(28,28));
        close.addActionListener(e -> System.exit(0));
        bar.add(close, BorderLayout.EAST);

        // Make window draggable
        Point[] drag = {null};
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { drag[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { drag[0] = null; }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (drag[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - drag[0].x, loc.y + e.getY() - drag[0].y);
                }
            }
        });
        return bar;
    }

    // ── Center area ───────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 40, 32, 40));

        // Logo
        center.add(buildLogo(), BorderLayout.NORTH);

        // Role selector tabs
        JPanel inner = new JPanel(new BorderLayout(0, 22));
        inner.setOpaque(false);
        inner.add(buildRoleSelector(), BorderLayout.NORTH);

        // Card panels
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        cardPanel.add(buildAdminForm(),  "ADMIN");
        cardPanel.add(buildMemberForm(), "MEMBER");
        inner.add(cardPanel, BorderLayout.CENTER);

        // Status + login button
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(statusLabel, BorderLayout.NORTH);

        JButton loginBtn = buildLoginButton();
        bottom.add(loginBtn, BorderLayout.CENTER);

        JLabel hint = new JLabel("Member login uses your registered email + member ID as password", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Theme.TEXT3);
        bottom.add(hint, BorderLayout.SOUTH);

        inner.add(bottom, BorderLayout.SOUTH);
        center.add(inner, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 18));
        p.setOpaque(false);

        JPanel icon = new JPanel() {
            float pulse=0f;
            javax.swing.Timer t=new javax.swing.Timer(60,e->{pulse=(pulse+0.07f)%(float)(Math.PI*2);repaint();});
            {t.start();setOpaque(false);setPreferredSize(new Dimension(46,46));}
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                float glow=0.12f+0.06f*(float)Math.sin(pulse);
                g2.setColor(Theme.alpha(Theme.ACCENT,(int)(glow*255)));
                g2.fillRoundRect(-4,-4,54,54,14,14);
                GradientPaint gp=new GradientPaint(0,0,Theme.lighten(Theme.ACCENT,0.2f),46,46,Theme.ACCENT_DIM);
                g2.setPaint(gp); g2.fillRoundRect(0,0,46,46,10,10);
                g2.setColor(new Color(0x080A0F));
                g2.setFont(new Font("Segoe UI",Font.BOLD,24));
                g2.drawString("F",14,33);
                g2.dispose();
            }
        };

        JPanel txt = new JPanel(new GridLayout(2,1,0,3));
        txt.setOpaque(false);
        JLabel title = new JLabel("FitnessDB");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.TEXT);
        JLabel sub = new JLabel("Member & Admin Portal");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT3);
        txt.add(title); txt.add(sub);

        p.add(icon); p.add(txt);
        return p;
    }

    // ── Role selector ─────────────────────────────────────────────
    private JPanel buildRoleSelector() {
        JPanel wrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        wrap.setOpaque(false);
        wrap.setLayout(new GridLayout(1,2,4,4));
        wrap.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        wrap.setPreferredSize(new Dimension(400, 48));

        JToggleButton btnAdmin  = buildRoleBtn("Admin",  "ADMIN");
        JToggleButton btnMember = buildRoleBtn("Member", "MEMBER");
        ButtonGroup bg = new ButtonGroup();
        bg.add(btnAdmin); bg.add(btnMember);
        btnAdmin.setSelected(true);

        wrap.add(btnAdmin); wrap.add(btnMember);
        return wrap;
    }

    private JToggleButton buildRoleBtn(String label, String role) {
        JToggleButton btn = new JToggleButton(label) {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel=isSelected();
                if (sel) {
                    GradientPaint gp=new GradientPaint(0,0,Theme.alpha(Theme.ACCENT,30),getWidth(),0,Theme.alpha(Theme.ACCENT,10));
                    g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(Theme.alpha(Theme.ACCENT,80));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                } else if (hover) {
                    g2.setColor(Theme.SURFACE3); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(sel ? Theme.ACCENT : (hover ? Theme.TEXT : Theme.TEXT2));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            selectedRole = role;
            cardLayout.show(cardPanel, role);
            statusLabel.setText(" ");
        });
        return btn;
    }

    // ── Admin form ────────────────────────────────────────────────
    private JPanel buildAdminForm() {
        JPanel p = new JPanel(new GridLayout(4,1,0,8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));

        p.add(fieldLabel("Username"));
        tfAdminUser = UIUtils.formField("fitnessdb_user");
        tfAdminUser.setText("fitnessdb_user");
        p.add(tfAdminUser);
        p.add(fieldLabel("Password"));
        pfAdminPass = new JPasswordField();
        stylePassField(pfAdminPass, "Enter admin password");
        p.add(pfAdminPass);
        return p;
    }

    // ── Member form ───────────────────────────────────────────────
    private JPanel buildMemberForm() {
        JPanel p = new JPanel(new GridLayout(4,1,0,8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));

        p.add(fieldLabel("Email address"));
        tfMemberEmail = UIUtils.formField("your@email.com");
        p.add(tfMemberEmail);
        p.add(fieldLabel("Password  (your Member ID number)"));
        pfMemberPass = new JPasswordField();
        stylePassField(pfMemberPass, "Enter your Member ID");
        p.add(pfMemberPass);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Theme.TEXT2);
        return l;
    }

    private void stylePassField(JPasswordField pf, String hint) {
        pf.setBackground(new Color(0x060810));
        pf.setForeground(new Color(0xF4F7FF));
        pf.setCaretColor(Theme.ACCENT);
        pf.setFont(Theme.FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER3, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        pf.setPreferredSize(new Dimension(400, 38));
        pf.addActionListener(e -> attemptLogin());
    }

    // ── Login button ──────────────────────────────────────────────
    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In") {
            boolean hover, pressed;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;pressed=false;repaint();}
                public void mousePressed(MouseEvent e){pressed=true;repaint();}
                public void mouseReleased(MouseEvent e){pressed=false;repaint();}
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
              setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = pressed ? new Color(0xA0CC00) : (hover ? new Color(0xD8FF20) : Theme.ACCENT);
                if (hover && !pressed) { g2.setColor(Theme.alpha(Theme.ACCENT,35)); g2.fillRoundRect(-3,-3,getWidth()+6,getHeight()+6,12,12); }
                GradientPaint gp=new GradientPaint(0,0,Theme.lighten(base,0.15f),0,getHeight(),base);
                g2.setPaint(gp); g2.fillRoundRect(0,pressed?1:0,getWidth(),getHeight()-(pressed?1:0),9,9);
                g2.setFont(new Font("Segoe UI",Font.BOLD,14));
                g2.setColor(new Color(0x080A0F));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2+(pressed?1:0));
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(400, 44));
        btn.addActionListener(e -> attemptLogin());
        return btn;
    }

    // ── Login logic ───────────────────────────────────────────────
    private void attemptLogin() {
        statusLabel.setText("Connecting…");
        statusLabel.setForeground(Theme.TEXT3);

        if ("ADMIN".equals(selectedRole)) {
            loginAsAdmin();
        } else {
            loginAsMember();
        }
    }

    private void loginAsAdmin() {
        // Just verify the connection works (credentials are hardcoded in DatabaseManager)
        DatabaseManager.getInstance().setAdminMode();
        if (DatabaseManager.getInstance().testConnection()) {
            dispose();
            new MainFrame();
        } else {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("⚠  Cannot connect to SQL Server. Check setup_login.sql.");
        }
    }

    private void loginAsMember() {
        String email = tfMemberEmail.getText().trim();
        String pass  = new String(pfMemberPass.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("⚠  Email and Member ID are required.");
            return;
        }

        // Verify: email must exist in Member table AND pass == member_id (as string)
        try {
            // Use admin connection for the lookup (member login may not have SELECT on Member)
            DatabaseManager dm = DatabaseManager.getInstance();
            dm.setAdminMode();
            PreparedStatement ps = dm.getAdminConnection_public().prepareStatement(
                "SELECT member_id, first_name, last_name FROM Member WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                statusLabel.setForeground(Theme.DANGER);
                statusLabel.setText("⚠  No account found for that email.");
                rs.close(); ps.close();
                return;
            }

            int memberId  = rs.getInt("member_id");
            String firstName = rs.getString("first_name");
            rs.close(); ps.close();

            // Password = member ID as string
            if (!pass.equals(String.valueOf(memberId))) {
                statusLabel.setForeground(Theme.DANGER);
                statusLabel.setText("⚠  Incorrect Member ID / password.");
                return;
            }

            // Switch to member mode and open UserFrame
            dm.setMemberMode(memberId);
            dispose();
            new UserFrame(memberId, firstName);

        } catch (SQLException ex) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("⚠  DB error: " + ex.getMessage());
        }
    }
}
