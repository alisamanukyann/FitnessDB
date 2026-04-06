package com.fitnessdb.ui.panels;

import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/** Triggers panel — two tabs, each showing trigger description + full-height data table. */
public class TriggersPanel extends DbObjectBasePanel {

    public TriggersPanel() {
        add(UIUtils.sectionHeader("Triggers", "Database triggers — automatic enforcement rules"), BorderLayout.NORTH);
        add(buildTabbedTriggers(), BorderLayout.CENTER);
    }

    private JPanel buildTabbedTriggers() {
        JPanel card = buildCard(Theme.WARNING);
        card.setLayout(new BorderLayout());

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0, 46));

        CardLayout cl = new CardLayout();
        JPanel content = new JPanel(cl);
        content.setOpaque(false);

        // Trigger 1 — CheckCapacity
        String desc1 = "Blocks enrollment when session is at max_capacity.\nIf full → RAISERROR + ROLLBACK\nIf available → INSERT + increment current_enrollment";
        String sql1  = "SELECT cs.session_id,c.class_name,cs.current_enrollment,c.max_capacity," +
            "CASE WHEN cs.current_enrollment>=c.max_capacity THEN 'FULL' ELSE 'Available' END AS status " +
            "FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id ORDER BY status DESC";
        DefaultTableModel m1 = new DefaultTableModel(new String[]{"Session","Class","Enrolled","Capacity","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t1 = buildStyledTable(m1);
        loadIntoModel(m1, sql1);
        content.add(buildTriggerTab("trg_CheckCapacity", "INSTEAD OF INSERT on Attendance", desc1, Theme.WARNING, m1, sql1, t1), "T1");

        // Trigger 2 — AutoExpireMembership
        String desc2 = "Automatically marks memberships expired\nwhen end_date < today AND status = 'active'.\nFires after every INSERT or UPDATE.";
        String sql2  = "SELECT mm.membership_id,m.first_name+' '+m.last_name,p.plan_name,mm.start_date,mm.end_date,mm.status " +
            "FROM MemberMembership mm JOIN Member m ON mm.member_id=m.member_id " +
            "JOIN MembershipPlan p ON mm.plan_id=p.plan_id ORDER BY mm.end_date DESC";
        DefaultTableModel m2 = new DefaultTableModel(new String[]{"ID","Member","Plan","Start","End","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t2 = buildStyledTable(m2);
        loadIntoModel(m2, sql2);
        content.add(buildTriggerTab("trg_AutoExpireMembership", "AFTER INSERT, UPDATE on MemberMembership", desc2, Theme.ACCENT3, m2, sql2, t2), "T2");

        ButtonGroup bg = new ButtonGroup();
        JToggleButton b1 = buildTabBtn("trg_CheckCapacity",        () -> cl.show(content,"T1"), bg, true,  Theme.WARNING);
        JToggleButton b2 = buildTabBtn("trg_AutoExpireMembership", () -> cl.show(content,"T2"), bg, false, Theme.ACCENT3);
        tabBar.add(Box.createHorizontalStrut(12)); tabBar.add(b1);
        tabBar.add(Box.createHorizontalStrut(4));  tabBar.add(b2);

        card.add(tabBar,  BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTriggerTab(String name, String target, String desc, Color accent,
                                    DefaultTableModel model, String sql, JTable tbl) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setOpaque(false);

        // Header bar
        JPanel hdr = buildCardToolbar(name, "TRIGGER", target, accent);
        JButton ref = UIUtils.btnGhost("↻  Refresh");
        ref.setPreferredSize(new Dimension(110, 30));
        ref.addActionListener(e -> loadIntoModel(model, sql));
        hdr.add(ref, BorderLayout.EAST);

        // Description
        JTextArea descArea = new JTextArea(desc);
        descArea.setEditable(false);
        descArea.setBackground(new Color(0x08090E));
        descArea.setForeground(Theme.TEXT2);
        descArea.setFont(Theme.FONT_MONO);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,Theme.BORDER2),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setRows(3);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(hdr,      BorderLayout.NORTH);
        top.add(descArea, BorderLayout.SOUTH);

        p.add(top,               BorderLayout.NORTH);
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
