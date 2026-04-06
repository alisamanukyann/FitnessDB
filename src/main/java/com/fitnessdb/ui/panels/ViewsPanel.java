package com.fitnessdb.ui.panels;

import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/** Views panel — two tabs, one per view, each with full-height table. */
public class ViewsPanel extends DbObjectBasePanel {

    public ViewsPanel() {
        add(UIUtils.sectionHeader("Views", "SQL views — pre-built data perspectives"), BorderLayout.NORTH);
        add(buildTabbedViews(), BorderLayout.CENTER);
    }

    private JPanel buildTabbedViews() {
        JPanel card = buildCard(Theme.ACCENT4);
        card.setLayout(new BorderLayout());

        // Tab bar
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(Theme.SURFACE2);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        tabBar.setPreferredSize(new Dimension(0, 46));

        CardLayout cl = new CardLayout();
        JPanel content = new JPanel(cl);
        content.setOpaque(false);

        // View 1
        DefaultTableModel m1 = new DefaultTableModel(new String[]{"Member ID","Full Name","Sessions Attended"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t1 = buildStyledTable(m1);
        loadIntoModel(m1, "SELECT member_id,full_name,total_sessions_attended FROM vw_MostActiveMembers ORDER BY total_sessions_attended DESC");
        JPanel p1 = wrapTableWithHeader("vw_MostActiveMembers", "VIEW", "Members ranked by total attended sessions", Theme.ACCENT4, m1,
            "SELECT member_id,full_name,total_sessions_attended FROM vw_MostActiveMembers ORDER BY total_sessions_attended DESC", t1);

        // View 2
        DefaultTableModel m2 = new DefaultTableModel(new String[]{"ID","Trainer","Specialization","Sessions","Members","PT Clients"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t2 = buildStyledTable(m2);
        loadIntoModel(m2, "SELECT trainer_id,trainer_name,specialization,total_sessions,unique_members_taught,personal_training_clients FROM vw_TrainerPerformance ORDER BY total_sessions DESC");
        JPanel p2 = wrapTableWithHeader("vw_TrainerPerformance", "VIEW", "Trainer stats: sessions, members, PT clients", Theme.ACCENT2, m2,
            "SELECT trainer_id,trainer_name,specialization,total_sessions,unique_members_taught,personal_training_clients FROM vw_TrainerPerformance ORDER BY total_sessions DESC", t2);

        content.add(p1, "V1");
        content.add(p2, "V2");

        ButtonGroup bg = new ButtonGroup();
        JToggleButton b1 = buildTabBtn("vw_MostActiveMembers",    () -> cl.show(content,"V1"), bg, true);
        JToggleButton b2 = buildTabBtn("vw_TrainerPerformance",   () -> cl.show(content,"V2"), bg, false);
        tabBar.add(Box.createHorizontalStrut(12)); tabBar.add(b1);
        tabBar.add(Box.createHorizontalStrut(4));  tabBar.add(b2);

        card.add(tabBar,   BorderLayout.NORTH);
        card.add(content,  BorderLayout.CENTER);
        return card;
    }

    private JPanel wrapTableWithHeader(String name, String type, String desc, Color accent,
                                        DefaultTableModel model, String sql, JTable tbl) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel hdr = buildCardToolbar(name, type, desc, accent);
        JButton ref = UIUtils.btnGhost("↻  Refresh");
        ref.setPreferredSize(new Dimension(110, 30));
        ref.addActionListener(e -> loadIntoModel(model, sql));
        hdr.add(ref, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);
        p.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        return p;
    }

    private JToggleButton buildTabBtn(String label, Runnable action, ButtonGroup bg, boolean sel) {
        JToggleButton btn = new JToggleButton(label) {
            boolean hover;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                boolean s = isSelected();
                g2.setColor(s ? Theme.SURFACE : (hover ? Theme.SURFACE3 : Theme.SURFACE2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (s) {
                    g2.setColor(Theme.ACCENT4);
                    g2.fillRect(0, getHeight()-3, getWidth(), 3);
                }
                g2.setFont(Theme.FONT_NAV);
                g2.setColor(s ? Theme.TEXT : (hover ? Theme.TEXT : Theme.TEXT2));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2-1);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setSelected(sel);
        btn.addActionListener(e -> action.run());
        bg.add(btn);
        return btn;
    }
}
