package com.fitnessdb.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.RenderingHints;

public class UIUtils {

    // ── SURFACE PANEL (rounded, glassmorphism-style) ────────────
    public static JPanel surface(int arc) {
        return new JPanel() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                // Inner glow on top edge
                GradientPaint topGlow = new GradientPaint(0, 0, Theme.alpha(Theme.BORDER3, 80), 0, 2, Theme.alpha(Theme.BORDER, 0));
                g2.setPaint(topGlow);
                g2.fill(new RoundRectangle2D.Float(1, 0, getWidth()-2, arc, arc, arc));
                // Border
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, arc, arc));
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }
    public static JPanel surface() { return surface(14); }

    // ── KPI CARD — elevated with gradient top bar and glow ──────
    public static JPanel kpiCard(String label, String value, String sub, Color accentColor) {
        JPanel p = new JPanel(null) {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle background gradient
                GradientPaint bg = new GradientPaint(0, 0, Theme.SURFACE2, 0, getHeight(), Theme.SURFACE);
                g2.setPaint(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                // Border
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
                // Top accent bar (3px gradient)
                GradientPaint bar = new GradientPaint(0, 0, accentColor, getWidth(), 0, Theme.alpha(accentColor, 80));
                g2.setPaint(bar);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), 3, 3, 3));
                // Icon glow box (top-right)
                g2.setColor(Theme.alpha(accentColor, 18));
                g2.fillRoundRect(getWidth()-54, 14, 38, 38, 10, 10);
                g2.setColor(Theme.alpha(accentColor, 35));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(getWidth()-54, 14, 38, 38, 10, 10);
                // Bottom accent glow
                GradientPaint bottomGlow = new GradientPaint(0, getHeight()-20, Theme.alpha(accentColor, 0), 0, getHeight(), Theme.alpha(accentColor, 8));
                g2.setPaint(bottomGlow);
                g2.fill(new RoundRectangle2D.Float(0, getHeight()-20, getWidth(), 20, 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setPreferredSize(new Dimension(200, 118));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(Theme.FONT_TH);
        lbl.setForeground(Theme.TEXT3);
        lbl.setBounds(18, 20, 160, 14);

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_KPI);
        val.setForeground(Theme.TEXT);
        val.setBounds(18, 38, 160, 42);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT3);
        subLbl.setBounds(18, 84, 160, 16);

        p.add(lbl); p.add(val); p.add(subLbl);
        return p;
    }

    // ── PRIMARY BUTTON — electric lime with press animation ─────
    public static JButton btnPrimary(String text) {
        JButton b = new JButton(text) {
            boolean hover; boolean pressed;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; pressed=false; repaint(); }
                public void mousePressed(MouseEvent e) { pressed=true; repaint(); }
                public void mouseReleased(MouseEvent e){ pressed=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = pressed ? new Color(0xA0CC00) : (hover ? new Color(0xD8FF20) : Theme.ACCENT);
                // Glow effect on hover
                if (hover && !pressed) {
                    g2.setColor(Theme.alpha(Theme.ACCENT, 35));
                    g2.fillRoundRect(-3, -3, getWidth()+6, getHeight()+6, 12, 12);
                }
                // Button fill
                GradientPaint gp = new GradientPaint(0, 0, Theme.lighten(base, 0.15f), 0, getHeight(), base);
                g2.setPaint(gp);
                g2.fillRoundRect(0, pressed?1:0, getWidth(), getHeight()-(pressed?1:0), 8, 8);
                // Text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(0x080A0F));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()-fm.stringWidth(getText()))/2;
                int ty = (getHeight()+fm.getAscent()-fm.getDescent())/2 + (pressed?1:0);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(110, 34));
        return b;
    }

    // ── GHOST BUTTON — subtle glassy ────────────────────────────
    public static JButton btnGhost(String text) {
        JButton b = new JButton(text) {
            boolean hover; boolean pressed;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; pressed=false; repaint(); }
                public void mousePressed(MouseEvent e) { pressed=true; repaint(); }
                public void mouseReleased(MouseEvent e){ pressed=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = pressed ? Theme.SURFACE4 : (hover ? Theme.SURFACE3 : Theme.SURFACE2);
                g2.setColor(bg);
                g2.fillRoundRect(0, pressed?1:0, getWidth(), getHeight()-(pressed?1:0), 8, 8);
                // Border glow on hover
                g2.setColor(hover ? Theme.BORDER3 : Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, pressed?1:0, getWidth()-1, getHeight()-1-(pressed?1:0), 8, 8);
                // Text
                g2.setFont(Theme.FONT_BODY);
                g2.setColor(hover ? Theme.TEXT : Theme.TEXT2);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2+(pressed?1:0));
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(100, 34));
        return b;
    }

    // ── DANGER BUTTON ────────────────────────────────────────────
    public static JButton btnDanger(String text) {
        JButton b = new JButton(text) {
            boolean hover; boolean pressed;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; pressed=false; repaint(); }
                public void mousePressed(MouseEvent e) { pressed=true; repaint(); }
                public void mouseReleased(MouseEvent e){ pressed=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int alpha = pressed ? 70 : (hover ? 55 : 30);
                g2.setColor(Theme.alpha(Theme.DANGER, alpha));
                g2.fillRoundRect(0, pressed?1:0, getWidth(), getHeight()-(pressed?1:0), 8, 8);
                g2.setColor(Theme.alpha(Theme.DANGER, hover ? 100 : 70));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, pressed?1:0, getWidth()-1, getHeight()-1-(pressed?1:0), 8, 8);
                g2.setFont(Theme.FONT_BODY);
                g2.setColor(hover ? Theme.lighten(Theme.DANGER, 0.2f) : Theme.DANGER);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2+(pressed?1:0));
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(90, 34));
        return b;
    }

    // ── SEARCH BOX — glassy with focus glow ─────────────────────
    public static JTextField searchField(String placeholder) {
        JTextField f = new JTextField() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                // Focus glow
                if (isFocusOwner()) {
                    g2.setColor(Theme.alpha(Theme.ACCENT, 40));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                } else {
                    g2.setColor(Theme.BORDER2);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(Theme.TEXT3);
                    g3.setFont(getFont());
                    g3.drawString(placeholder, getInsets().left + 2, getHeight()/2 + g3.getFontMetrics().getAscent()/2 - 1);
                    g3.dispose();
                }
            }
        };
        f.setBackground(new Color(0,0,0,0));
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setFont(Theme.FONT_BODY);
        f.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        f.setPreferredSize(new Dimension(220, 34));
        return f;
    }

    // ── COMBO BOX ────────────────────────────────────────────────
    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(new Color(0x060810));
        cb.setForeground(new Color(0xF4F7FF));
        cb.setFont(Theme.FONT_BODY);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER3, 1),
            BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        cb.setPreferredSize(new Dimension(180, 36));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? Theme.alpha(Theme.ACCENT, 40) : new Color(0x0D1017));
                setForeground(new Color(0xF4F7FF));
                setFont(Theme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                return this;
            }
        });
        return cb;
    }

    // ── DATA TABLE — refined striping and hover ──────────────────
    public static void styleTable(JTable t) {
        t.setBackground(Theme.SURFACE);
        t.setForeground(Theme.TEXT2);
        t.setFont(Theme.FONT_BODY);
        t.setRowHeight(42);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(Theme.alpha(Theme.ACCENT, 20));
        t.setSelectionForeground(Theme.TEXT);

        JTableHeader h = t.getTableHeader();
        h.setBackground(Theme.SURFACE2);
        h.setForeground(Theme.TEXT2);
        h.setFont(Theme.FONT_TH);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        h.setReorderingAllowed(false);
        h.setResizingAllowed(true);
        h.setPreferredSize(new Dimension(h.getWidth(), 38));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setOpaque(true);
                if (sel) {
                    setBackground(Theme.alpha(Theme.ACCENT, 20));
                    setForeground(Theme.TEXT);
                } else if (row % 2 == 1) {
                    setBackground(Theme.alpha(Theme.SURFACE2, 180));
                    setForeground(Theme.TEXT2);
                } else {
                    setBackground(Theme.SURFACE);
                    setForeground(Theme.TEXT2);
                }
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                    BorderFactory.createEmptyBorder(0, 18, 0, 18)));
                setFont(Theme.FONT_BODY);
                return this;
            }
        });
    }

    // ── STATUS BADGE — pill with dot ────────────────────────────
    public static JLabel badge(String status) {
        Color bg, fg;
        switch (status == null ? "" : status.toLowerCase()) {
            case "active":        bg=Theme.alpha(Theme.SUCCESS,30); fg=Theme.SUCCESS; break;
            case "expired":       bg=Theme.alpha(Theme.DANGER,30);  fg=Theme.DANGER;  break;
            case "canceled":      bg=Theme.alpha(Theme.TEXT3,30);   fg=Theme.TEXT2;   break;
            case "attended":      bg=Theme.alpha(Theme.SUCCESS,30); fg=Theme.SUCCESS; break;
            case "no-show":       bg=Theme.alpha(Theme.DANGER,30);  fg=Theme.DANGER;  break;
            case "in_progress":   bg=Theme.alpha(Theme.INFO,30);    fg=Theme.INFO;    break;
            case "achieved":      bg=Theme.alpha(Theme.SUCCESS,30); fg=Theme.SUCCESS; break;
            case "abandoned":     bg=Theme.alpha(Theme.DANGER,30);  fg=Theme.DANGER;  break;
            case "cash":          bg=Theme.alpha(Theme.SUCCESS,30); fg=Theme.SUCCESS; break;
            case "card":          bg=Theme.alpha(Theme.ACCENT4,30); fg=Theme.ACCENT4; break;
            case "online":        bg=Theme.alpha(Theme.ACCENT2,30); fg=Theme.ACCENT2; break;
            case "bank_transfer": bg=Theme.alpha(Theme.ACCENT2,30); fg=Theme.ACCENT2; break;
            default:              bg=Theme.alpha(Theme.TEXT3,30);   fg=Theme.TEXT3;   break;
        }
        final Color fbg=bg, ffg=fg;
        JLabel l = new JLabel("  " + (status==null?"":status) + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fbg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // glowing dot
                g2.setColor(Theme.alpha(ffg, 60));
                g2.fillOval(7, getHeight()/2-4, 8, 8);
                g2.setColor(ffg);
                g2.fillOval(9, getHeight()/2-2, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setForeground(fg);
        l.setFont(Theme.FONT_BADGE);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    // ── TAG CHIP ─────────────────────────────────────────────────
    public static JLabel typeTag(String text, Color fg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.alpha(fg, 22));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2.setColor(Theme.alpha(fg, 50));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setForeground(fg);
        l.setFont(Theme.FONT_TH);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    // ── SCROLLPANE ───────────────────────────────────────────────
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(Theme.SURFACE);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setBackground(Theme.BG);
        sp.getHorizontalScrollBar().setBackground(Theme.BG);
        // Style scrollbar thumb
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Theme.BORDER3;
                trackColor = Theme.BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
        return sp;
    }

    // ── LABEL HELPERS ─────────────────────────────────────────────
    public static JLabel label(String t, Color c, Font f) {
        JLabel l = new JLabel(t); l.setForeground(c); l.setFont(f); return l;
    }

    // ── FORM TEXT FIELD ───────────────────────────────────────────
    public static JTextField formField(String placeholder) {
        final Color BG_FIELD   = new Color(0x060810);   // very dark — clearly different from dialog bg
        final Color TEXT_FIELD = new Color(0xF4F7FF);   // bright white
        JTextField f = new JTextField() {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_FIELD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (isFocusOwner()) {
                    g2.setColor(Theme.alpha(Theme.ACCENT, 55));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                } else {
                    g2.setColor(Theme.BORDER3);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g3 = (Graphics2D) g.create();
                    g3.setColor(Theme.TEXT3);
                    g3.setFont(getFont());
                    g3.drawString(placeholder, getInsets().left + 2, getHeight()/2 + g3.getFontMetrics().getAscent()/2 - 1);
                    g3.dispose();
                }
            }
        };
        f.setBackground(new Color(0, 0, 0, 0));
        f.setForeground(TEXT_FIELD);
        f.setCaretColor(Theme.ACCENT);
        f.setFont(Theme.FONT_BODY);
        f.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        f.setPreferredSize(new Dimension(220, 36));
        return f;
    }

    // ── SECTION HEADER ────────────────────────────────────────────
    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_TITLE);
        t.setForeground(Theme.TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.FONT_SUBTITLE);
        s.setForeground(Theme.TEXT3);
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.CENTER);
        return p;
    }

    // ── TABLE TOOLBAR PANEL ───────────────────────────────────────
    public static JPanel tableToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p.setBackground(Theme.SURFACE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        return p;
    }
}
