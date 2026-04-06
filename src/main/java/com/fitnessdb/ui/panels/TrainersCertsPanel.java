package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.CertDialog;
import com.fitnessdb.ui.dialogs.TrainerDialog;
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

/**
 * Combined panel showing Trainers and Trainer Certifications as two tabs.
 */
public class TrainersCertsPanel extends JPanel {

    private JPanel contentHolder;
    private CardLayout tabCards;

    public TrainersCertsPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        build();
    }

    private void build() {
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIUtils.sectionHeader("Trainers & Certifications", "Staff trainers and their professional credentials"), BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.add(buildTabBar(), BorderLayout.NORTH);

        tabCards = new CardLayout();
        contentHolder = new JPanel(tabCards);
        contentHolder.setOpaque(false);
        contentHolder.add(new TrainersSubPanel(),  "TRAINERS");
        contentHolder.add(new CertsSubPanel(),     "CERTS");

        card.add(contentHolder, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        tabCards.show(contentHolder, "TRAINERS");
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(Theme.SURFACE2);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        bar.setPreferredSize(new Dimension(0, 50));

        JToggleButton btnTrainers = buildTabButton("★  Trainers",           "TRAINERS");
        JToggleButton btnCerts    = buildTabButton("✦  Certifications",      "CERTS");

        ButtonGroup bg = new ButtonGroup();
        bg.add(btnTrainers); bg.add(btnCerts);
        btnTrainers.setSelected(true);

        bar.add(Box.createHorizontalStrut(16));
        bar.add(btnTrainers);
        bar.add(Box.createHorizontalStrut(4));
        bar.add(btnCerts);
        return bar;
    }

    private JToggleButton buildTabButton(String label, String key) {
        JToggleButton btn = new JToggleButton(label) {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelected();
                g2.setColor(sel ? Theme.SURFACE : (hover ? Theme.SURFACE3 : Theme.SURFACE2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (sel) {
                    GradientPaint gp = new GradientPaint(0, getHeight()-3, Theme.ACCENT, getWidth(), getHeight()-3, Theme.ACCENT_DIM);
                    g2.setPaint(gp);
                    g2.fillRect(0, getHeight()-3, getWidth(), 3);
                }
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(sel ? Theme.TEXT : (hover ? Theme.TEXT : Theme.TEXT2));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> tabCards.show(contentHolder, key));
        return btn;
    }

    // ── Shared base for sub-panels (self-contained pagination)
    abstract static class TabbedSubPanel extends ClassesSessionsPanel.TabbedSubPanel {}

    // ── Trainers sub-panel
    static class TrainersSubPanel extends ClassesSessionsPanel.TabbedSubPanel {
        @Override String getEntityName() { return "Trainers"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","First Name","Last Name","Email","Phone","Specialization","Hire Date"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT trainer_id,first_name,last_name,email,phone,specialization,hire_date FROM Trainer ORDER BY trainer_id DESC");
        }
        @Override void openAddDialog()           { new TrainerDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new TrainerDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Trainer WHERE trainer_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }

    // ── Certifications sub-panel
    static class CertsSubPanel extends ClassesSessionsPanel.TabbedSubPanel {
        @Override String getEntityName() { return "Certifications"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","Trainer","Certification","Issuing Body","Issue Date","Expiry Date"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT tc.cert_id,t.first_name+' '+t.last_name,tc.cert_name,tc.issuing_body,tc.issue_date,tc.expiry_date FROM TrainerCertification tc JOIN Trainer t ON tc.trainer_id=t.trainer_id ORDER BY tc.cert_id DESC");
        }
        @Override void openAddDialog()           { new CertDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new CertDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM TrainerCertification WHERE cert_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }
}
