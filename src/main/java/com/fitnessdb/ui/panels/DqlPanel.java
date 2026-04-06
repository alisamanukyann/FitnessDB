package com.fitnessdb.ui.panels;

import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/** DQL panel — two tabs, each with full-height result table. */
public class DqlPanel extends DbObjectBasePanel {

    public DqlPanel() {
        add(UIUtils.sectionHeader("DQL Queries", "Pre-built joined data queries"), BorderLayout.NORTH);
        add(buildTabbedDql(), BorderLayout.CENTER);
    }

    private JPanel buildTabbedDql() {
        JPanel card = buildCard(Theme.SUCCESS);
        card.setLayout(new BorderLayout());

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0, 46));

        CardLayout cl = new CardLayout();
        JPanel content = new JPanel(cl);
        content.setOpaque(false);

        String sql1 = "SELECT m.first_name+' '+m.last_name AS full_name,p.plan_name,mm.start_date,mm.end_date " +
            "FROM Member m JOIN MemberMembership mm ON m.member_id=mm.member_id " +
            "JOIN MembershipPlan p ON mm.plan_id=p.plan_id WHERE mm.status='active' ORDER BY mm.end_date";
        DefaultTableModel m1 = new DefaultTableModel(new String[]{"Full Name","Plan","Start Date","End Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t1 = buildStyledTable(m1);
        loadIntoModel(m1, sql1);
        JPanel p1 = wrapWithHeader("Active Members + Plans", "DQL", "Members with active memberships", Theme.SUCCESS, m1, sql1, t1);

        String sql2 = "SELECT m.first_name+' '+m.last_name AS member,t.first_name+' '+t.last_name AS trainer," +
            "t.specialization,mt.session_type,mt.assigned_date " +
            "FROM MemberTrainer mt JOIN Member m ON mt.member_id=m.member_id " +
            "JOIN Trainer t ON mt.trainer_id=t.trainer_id ORDER BY m.last_name";
        DefaultTableModel m2 = new DefaultTableModel(new String[]{"Member","Trainer","Specialization","Session Type","Assigned"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t2 = buildStyledTable(m2);
        loadIntoModel(m2, sql2);
        JPanel p2 = wrapWithHeader("Members + Assigned Trainers", "DQL", "All member-trainer assignments", Theme.ACCENT3, m2, sql2, t2);

        content.add(p1, "Q1");
        content.add(p2, "Q2");

        ButtonGroup bg = new ButtonGroup();
        JToggleButton b1 = buildTabBtn("Active Members + Plans",        () -> cl.show(content,"Q1"), bg, true,  Theme.SUCCESS);
        JToggleButton b2 = buildTabBtn("Members + Assigned Trainers",   () -> cl.show(content,"Q2"), bg, false, Theme.ACCENT3);
        tabBar.add(Box.createHorizontalStrut(12)); tabBar.add(b1);
        tabBar.add(Box.createHorizontalStrut(4));  tabBar.add(b2);

        card.add(tabBar,  BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapWithHeader(String name, String type, String desc, Color accent,
                                   DefaultTableModel model, String sql, JTable tbl) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JPanel hdr = buildCardToolbar(name, type, desc, accent);
        JButton ref = UIUtils.btnGhost("↻  Refresh");
        ref.setPreferredSize(new Dimension(110, 30));
        ref.addActionListener(e -> loadIntoModel(model, sql));
        hdr.add(ref, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);
        p.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private JToggleButton buildTabBtn(String label, Runnable action, ButtonGroup bg, boolean sel, Color accent) {
        JToggleButton btn = new JToggleButton(label) {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean s = isSelected();
                g2.setColor(s ? Theme.SURFACE : (hover ? Theme.SURFACE3 : Theme.SURFACE2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (s) { g2.setColor(accent); g2.fillRect(0, getHeight()-3, getWidth(), 3); }
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(s ? Theme.TEXT : (hover ? Theme.TEXT : Theme.TEXT2));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2-1);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(240, 46));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setSelected(sel);
        btn.addActionListener(e -> action.run());
        bg.add(btn);
        return btn;
    }
}
