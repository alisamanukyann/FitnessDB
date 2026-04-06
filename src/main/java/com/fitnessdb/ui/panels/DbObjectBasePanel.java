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

/** Shared helpers for Views, Triggers, DQL, Procs, SQL panels. */
public class DbObjectBasePanel extends JPanel {

    public DbObjectBasePanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
    }

    protected JPanel buildCard(Color accentHint) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                GradientPaint top = new GradientPaint(0,0, accentHint, getWidth()*0.5f,0, Theme.alpha(accentHint,0));
                g2.setPaint(top);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),3,3,3));
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    protected JPanel buildCardToolbar(String name, String typeLabel, String desc, Color accent) {
        JPanel toolbar = new JPanel(new BorderLayout(10,0));
        toolbar.setBackground(Theme.SURFACE2);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER2),
            BorderFactory.createEmptyBorder(13,20,13,20)));

        JPanel strip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, accent, 0,getHeight(), Theme.alpha(accent,100));
                g2.setPaint(gp);
                g2.fillRoundRect(0,3, 3,getHeight()-6, 3,3);
                g2.dispose();
            }
            { setOpaque(false); setPreferredSize(new Dimension(12,0)); }
        };

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        left.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(Theme.FONT_HEADER);
        nameLbl.setForeground(Theme.TEXT);
        JLabel tag = UIUtils.typeTag(typeLabel, accent);
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(Theme.FONT_SMALL);
        descLbl.setForeground(Theme.TEXT2);
        left.add(nameLbl); left.add(tag); left.add(descLbl);

        JPanel inner = new JPanel(new BorderLayout(10,0));
        inner.setOpaque(false);
        inner.add(strip, BorderLayout.WEST);
        inner.add(left,  BorderLayout.CENTER);
        toolbar.add(inner, BorderLayout.WEST);
        return toolbar;
    }

    protected JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        UIUtils.styleTable(t);
        t.setRowHeight(38);
        return t;
    }

    protected JTextArea buildOutputArea(int rows) {
        JTextArea ta = new JTextArea(rows, 30);
        ta.setEditable(false);
        ta.setBackground(new Color(0x08090E));
        ta.setForeground(Theme.TEXT2);
        ta.setFont(Theme.FONT_MONO);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER2),
            BorderFactory.createEmptyBorder(11,14,11,14)));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    protected JLabel monoLbl(String t, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_MONO);
        l.setForeground(c);
        return l;
    }

    protected void loadIntoModel(DefaultTableModel model, String sql) {
        model.setRowCount(0);
        try {
            ResultSet rs = DatabaseManager.getInstance().executeQuery(sql);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[cols];
                for (int i = 0; i < cols; i++) row[i] = rs.getObject(i+1);
                model.addRow(row);
            }
            rs.getStatement().close();
        } catch (Exception e) {
            model.addRow(new Object[]{"Error: " + e.getMessage()});
        }
    }

    protected JPanel buildViewBlock(String name, String typeLabel, String desc,
                                     String sql, String[] cols, Color accent) {
        JPanel card = buildCard(accent);
        card.setLayout(new BorderLayout());

        JPanel toolbar = buildCardToolbar(name, typeLabel, desc, accent);
        JButton refresh = UIUtils.btnGhost("↻");
        refresh.setPreferredSize(new Dimension(40, 32));
        toolbar.add(refresh, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = buildStyledTable(model);
        refresh.addActionListener(e -> loadIntoModel(model, sql));
        loadIntoModel(model, sql);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(UIUtils.scroll(tbl), BorderLayout.CENTER);
        return card;
    }
}
