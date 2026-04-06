package com.fitnessdb;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class App {
    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        // Global dark theme defaults
        UIManager.put("OptionPane.background",         Theme.SURFACE);
        UIManager.put("Panel.background",              Theme.BG);
        UIManager.put("OptionPane.messageForeground",  Theme.TEXT);
        UIManager.put("Button.background",             Theme.SURFACE2);
        UIManager.put("Button.foreground",             Theme.TEXT);
        UIManager.put("TextField.background",          Theme.SURFACE2);
        UIManager.put("TextField.foreground",          Theme.TEXT);
        UIManager.put("TextArea.background",           Theme.SURFACE2);
        UIManager.put("TextArea.foreground",           Theme.TEXT);
        UIManager.put("ComboBox.background",           new Color(0x060810));
        UIManager.put("ComboBox.foreground",           new Color(0xF4F7FF));
        UIManager.put("ComboBox.selectionBackground",  Theme.alpha(Theme.ACCENT, 60));
        UIManager.put("ComboBox.selectionForeground",  new Color(0xF4F7FF));
        UIManager.put("ComboBox.buttonBackground",     new Color(0x060810));
        UIManager.put("ComboBox.disabledBackground",   Theme.SURFACE2);
        UIManager.put("ComboBox.disabledForeground",   Theme.TEXT3);
        UIManager.put("List.background",               new Color(0x0D1017));
        UIManager.put("List.foreground",               new Color(0xF4F7FF));
        UIManager.put("List.selectionBackground",      Theme.alpha(Theme.ACCENT, 60));
        UIManager.put("List.selectionForeground",      new Color(0xF4F7FF));
        UIManager.put("ScrollBar.background",          Theme.BG);
        UIManager.put("ScrollBar.thumb",               Theme.BORDER2);
        UIManager.put("ScrollBar.track",               Theme.BG);
        UIManager.put("TabbedPane.background",         Theme.SURFACE);
        UIManager.put("TabbedPane.foreground",         Theme.TEXT2);
        UIManager.put("TabbedPane.selected",           Theme.SURFACE2);
        UIManager.put("TabbedPane.contentAreaColor",   Theme.SURFACE);
        UIManager.put("TabbedPane.tabAreaBackground",  Theme.SURFACE);
        UIManager.put("Label.foreground",              Theme.TEXT);
        UIManager.put("Table.background",              Theme.SURFACE);
        UIManager.put("Table.foreground",              Theme.TEXT2);
        UIManager.put("TableHeader.background",        Theme.SURFACE2);
        UIManager.put("TableHeader.foreground",        Theme.TEXT3);
        UIManager.put("ScrollPane.background",         Theme.BG);
        UIManager.put("Viewport.background",           Theme.SURFACE);

        SwingUtilities.invokeLater(() -> {
            JWindow splash = showSplash();
            boolean connected = DatabaseManager.getInstance().testConnection();
            splash.dispose();

            if (!connected) {
                int choice = JOptionPane.showOptionDialog(
                    null,
                    "⚠  Could not connect to SQL Server.\n\n" +
                    "Database: FitnessDB\n" +
                    "User:     fitnessdb_user\n\n" +
                    "Ensure:\n" +
                    "  • SQL Server Express is running\n" +
                    "  • TCP/IP is enabled (SQL Server Configuration Manager)\n" +
                    "  • setup_login.sql has been run\n\n" +
                    "Continue anyway?",
                    "Connection Failed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"Continue Anyway", "Exit"},
                    "Exit");
                if (choice != 0) System.exit(0);
            }
            new com.fitnessdb.ui.LoginFrame();
        });
    }

    private static JWindow showSplash() {
        JWindow w = new JWindow();
        w.setSize(420, 220);
        w.setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout(0, 0)) {
            float[] progAnim = {0f};
            javax.swing.Timer t = new javax.swing.Timer(16, e -> {
                progAnim[0] = Math.min(1f, progAnim[0] + 0.012f);
                repaint();
            });
            { t.start(); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Background
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Top accent gradient bar
                GradientPaint bar = new GradientPaint(0, 0, Theme.ACCENT, getWidth()*0.6f, 0, Theme.alpha(Theme.ACCENT, 0));
                g2.setPaint(bar);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                // Border
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                // Progress bar at bottom
                int barY = getHeight()-14;
                g2.setColor(Theme.BORDER);
                g2.fillRoundRect(30, barY, getWidth()-60, 4, 4, 4);
                GradientPaint prog = new GradientPaint(30, 0, Theme.ACCENT, 30 + (int)((getWidth()-60)*progAnim[0]), 0, Theme.ACCENT_DIM);
                g2.setPaint(prog);
                g2.fillRoundRect(30, barY, (int)((getWidth()-60)*progAnim[0]), 4, 4, 4);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(34, 40, 30, 40));

        // Logo row
        JPanel logoBox = new JPanel() {
            float pulse = 0f;
            javax.swing.Timer t = new javax.swing.Timer(50, e -> { pulse=(pulse+0.08f)%(float)(Math.PI*2); repaint(); });
            { t.start(); setOpaque(false); setPreferredSize(new Dimension(48, 48)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float glow = 0.10f + 0.06f*(float)Math.sin(pulse);
                g2.setColor(Theme.alpha(Theme.ACCENT, (int)(glow*255)));
                g2.fillRoundRect(-4, -4, 56, 56, 16, 16);
                GradientPaint gp = new GradientPaint(0, 0, Theme.lighten(Theme.ACCENT, 0.2f), 48, 48, Theme.ACCENT_DIM);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, 48, 48, 12, 12);
                g2.setColor(new Color(0x080A0F));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                g2.drawString("F", 15, 34);
                g2.dispose();
            }
        };

        JLabel title = new JLabel("FitnessDB");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Theme.TEXT);

        JLabel sub = new JLabel("Admin Panel  ·  Connecting to SQL Server…");
        sub.setFont(Theme.FONT_LOGO_S);
        sub.setForeground(Theme.TEXT3);

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 5));
        textCol.setOpaque(false);
        textCol.add(title);
        textCol.add(sub);

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(logoBox,  BorderLayout.WEST);
        row.add(textCol,  BorderLayout.CENTER);

        p.add(row, BorderLayout.CENTER);
        w.setContentPane(p);
        w.setVisible(true);
        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
        return w;
    }
}
