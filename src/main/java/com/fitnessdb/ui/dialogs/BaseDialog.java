package com.fitnessdb.ui.dialogs;

import com.fitnessdb.util.Theme;
import com.fitnessdb.util.UIUtils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

public abstract class BaseDialog extends JDialog {

    protected JPanel formPanel;
    private int formRow = 0;

    public BaseDialog(Frame owner, String title) {
        super(owner, title, true);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(buildHeader(title), BorderLayout.NORTH);
        formPanel = buildFormPanel();
        JScrollPane sp = new JScrollPane(formPanel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(Theme.BG);
        add(sp, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    protected abstract void onSave();

    // ── Dialog Header — with accent left bar + gradient ──────────
    private JPanel buildHeader(String title) {
        JPanel h = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.SURFACE2, getWidth(), 0, Theme.SURFACE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER2);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        h.setOpaque(false);
        h.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Left accent strip
        JPanel strip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT, 0, getHeight(), Theme.ACCENT_DIM);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 2, 4, getHeight()-4, 4, 4);
                g2.dispose();
            }
            { setOpaque(false); setPreferredSize(new Dimension(14, 0)); }
        };

        JPanel textCol = new JPanel(new BorderLayout(0, 3));
        textCol.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_TITLE);
        lbl.setForeground(Theme.TEXT);
        textCol.add(lbl, BorderLayout.CENTER);

        h.add(strip,   BorderLayout.WEST);
        h.add(textCol, BorderLayout.CENTER);
        return h;
    }

    // ── Form Panel ────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(22, 26, 10, 26));
        return p;
    }

    // ── Single-column full-width field row ────────────────────────
    protected void addFormRow(String label, JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.weightx = 1; gbc.gridwidth = 2;

        gbc.gridy = formRow * 3;
        formPanel.add(makeLabel(label), gbc);

        gbc.gridy = formRow * 3 + 1;
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        formPanel.add(field, gbc);

        gbc.gridy = formRow * 3 + 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(Box.createVerticalStrut(2), gbc);
        formRow++;
    }

    // ── Two-column row ────────────────────────────────────────────
    protected void addFormRow2(String lbl1, JComponent f1, String lbl2, JComponent f2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = formRow * 3; gbc.insets = new Insets(0, 0, 4, 12); gbc.weightx = 0.5; gbc.gridwidth = 1;
        gbc.gridx = 0; formPanel.add(makeLabel(lbl1), gbc);
        gbc.gridx = 1; gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(makeLabel(lbl2), gbc);

        gbc.gridy = formRow * 3 + 1;
        gbc.gridx = 0; gbc.insets = new Insets(0, 0, 0, 12);
        f1.setPreferredSize(new Dimension(200, 36));
        formPanel.add(f1, gbc);
        gbc.gridx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        f2.setPreferredSize(new Dimension(200, 36));
        formPanel.add(f2, gbc);

        gbc.gridy = formRow * 3 + 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(Box.createVerticalStrut(2), gbc);
        formRow++;
    }

    // ── Footer ────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.SURFACE2);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER2);
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton cancel = UIUtils.btnGhost("Cancel");
        cancel.addActionListener(e -> dispose());

        JButton save = UIUtils.btnPrimary(getSaveLabel());
        save.setPreferredSize(new Dimension(140, 34));
        save.addActionListener(e -> onSave());

        right.add(cancel);
        right.add(save);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    protected String getSaveLabel() { return "Save Changes"; }

    // ── Helpers ───────────────────────────────────────────────────
    protected JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.TEXT2);
        return l;
    }

    protected JComboBox<String> makeCombo(String... items) {
        JComboBox<String> cb = UIUtils.styledCombo(items);
        return cb;
    }

    protected JTextArea makeTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setBackground(Theme.SURFACE2);
        ta.setForeground(Theme.TEXT);
        ta.setFont(Theme.FONT_MONO);
        ta.setCaretColor(Theme.ACCENT);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER2),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return ta;
    }

    protected void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected String nullStr(String s)   { return s == null ? "" : s; }
    protected String emptyNull(String s) { return (s==null||s.trim().isEmpty()) ? null : s.trim(); }
    protected void finish() { dispose(); }
}
