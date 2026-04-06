package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.GoalDialog;
import com.fitnessdb.ui.dialogs.MemberDialog;
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
 * Combined panel showing Members and Member Goals as two tabs.
 */
public class MembersGoalsPanel extends JPanel {

    private JPanel contentHolder;
    private CardLayout tabCards;

    public MembersGoalsPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        build();
    }

    private void build() {
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIUtils.sectionHeader("Members & Goals", "Registered members and their tracked fitness goals"), BorderLayout.WEST);
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
        contentHolder.add(new MembersSubPanel(), "MEMBERS");
        contentHolder.add(new GoalsSubPanel(),   "GOALS");

        card.add(contentHolder, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        tabCards.show(contentHolder, "MEMBERS");
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(Theme.SURFACE2);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        bar.setPreferredSize(new Dimension(0, 50));

        JToggleButton btnMembers = buildTabButton("◉  Members",     "MEMBERS");
        JToggleButton btnGoals   = buildTabButton("◆  Member Goals", "GOALS");

        ButtonGroup bg = new ButtonGroup();
        bg.add(btnMembers); bg.add(btnGoals);
        btnMembers.setSelected(true);

        bar.add(Box.createHorizontalStrut(16));
        bar.add(btnMembers);
        bar.add(Box.createHorizontalStrut(4));
        bar.add(btnGoals);
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
        btn.setPreferredSize(new Dimension(210, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> tabCards.show(contentHolder, key));
        return btn;
    }

    // ── Members sub-panel
    static class MembersSubPanel extends ClassesSessionsPanel.TabbedSubPanel {
        @Override String getEntityName() { return "Members"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","First Name","Last Name","Email","Phone","Date of Birth","Join Date","Address"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT member_id,first_name,last_name,email,phone,date_of_birth,join_date,address FROM Member ORDER BY member_id DESC");
        }
        @Override void openAddDialog()           { new MemberDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new MemberDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Member WHERE member_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }

    // ── Goals sub-panel
    static class GoalsSubPanel extends ClassesSessionsPanel.TabbedSubPanel {
        @Override String getEntityName() { return "Goals"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","Member","Trainer","Goal Type","Description","Target","Target Date","Status","Created"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT g.goal_id,m.first_name+' '+m.last_name,ISNULL(t.first_name+' '+t.last_name,'—'),g.goal_type,g.description,g.target_value,g.target_date,g.status,g.created_date FROM MemberGoal g JOIN Member m ON g.member_id=m.member_id LEFT JOIN Trainer t ON g.trainer_id=t.trainer_id ORDER BY g.goal_id DESC");
        }
        @Override void openAddDialog()           { new GoalDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new GoalDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM MemberGoal WHERE goal_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }
}
