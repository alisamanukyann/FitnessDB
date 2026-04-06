package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
import com.fitnessdb.ui.dialogs.ClassDialog;
import com.fitnessdb.ui.dialogs.SessionDialog;
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
 * Combined panel showing Classes and Class Sessions as two tabs.
 */
public class ClassesSessionsPanel extends JPanel {

    private TabbedSubPanel classesTab;
    private TabbedSubPanel sessionsTab;
    private JPanel tabBar;
    private JPanel contentHolder;
    private CardLayout tabCards;

    public ClassesSessionsPanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        build();
    }

    private void build() {
        // ── Page header
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIUtils.sectionHeader("Classes & Sessions", "Manage fitness classes and their scheduled sessions"), BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);

        // ── Card to hold tab bar + content
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

        // ── Tab bar
        tabBar = buildTabBar();
        card.add(tabBar, BorderLayout.NORTH);

        // ── Tab content area
        tabCards = new CardLayout();
        contentHolder = new JPanel(tabCards);
        contentHolder.setOpaque(false);

        classesTab  = new ClassesSubPanel();
        sessionsTab = new SessionsSubPanel();
        contentHolder.add(classesTab,  "CLASSES");
        contentHolder.add(sessionsTab, "SESSIONS");

        card.add(contentHolder, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        selectTab("CLASSES");
    }

    // ── Tab bar buttons
    private String selectedTab = "CLASSES";

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(Theme.SURFACE2);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2));
        bar.setPreferredSize(new Dimension(0, 50));

        JToggleButton btnClasses  = buildTabButton("▣  Classes",       "CLASSES");
        JToggleButton btnSessions = buildTabButton("◈  Class Sessions", "SESSIONS");

        ButtonGroup bg = new ButtonGroup();
        bg.add(btnClasses);
        bg.add(btnSessions);
        btnClasses.setSelected(true);

        bar.add(Box.createHorizontalStrut(16));
        bar.add(btnClasses);
        bar.add(Box.createHorizontalStrut(4));
        bar.add(btnSessions);
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
                // Active underline
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
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> selectTab(key));
        return btn;
    }

    private void selectTab(String key) {
        selectedTab = key;
        tabCards.show(contentHolder, key);
    }

    // ══════════════════════════════════════════════════════════════
    // Inner base class for each sub-panel (has its own pagination)
    // ══════════════════════════════════════════════════════════════
    abstract static class TabbedSubPanel extends JPanel {
        protected DefaultTableModel tableModel;
        protected JTable table;
        protected JTextField searchField;
        protected JLabel statusLabel, rowCountLabel, pageLabel;
        protected JButton prevBtn, nextBtn;
        protected TableRowSorter<DefaultTableModel> sorter;

        private static final int PAGE_SIZE = 20;
        private int currentPage = 0;
        private final List<Object[]> allRows = new ArrayList<>();

        TabbedSubPanel() {
            super(new BorderLayout());
            setOpaque(false);
            buildInner();
            loadData("");
        }

        abstract String[] getColumnNames();
        abstract void loadData(String filter);
        abstract void openAddDialog();
        abstract void openEditDialog(int modelRow);
        abstract void deleteSelected(int modelRow);
        abstract String getEntityName();

        private void buildInner() {
            add(buildToolbar(), BorderLayout.NORTH);

            tableModel = new DefaultTableModel(getColumnNames(), 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            UIUtils.styleTable(table);

            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);

            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int vr = table.getSelectedRow();
                        if (vr >= 0) openEditDialog(table.convertRowIndexToModel(vr));
                    }
                }
            });

            JScrollPane sp = UIUtils.scroll(table);
            sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
            add(sp, BorderLayout.CENTER);
            add(buildFooter(), BorderLayout.SOUTH);
        }

        private JPanel buildToolbar() {
            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setBackground(Theme.SURFACE2);
            toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER2),
                BorderFactory.createEmptyBorder(13, 22, 13, 22)));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setOpaque(false);

            searchField = UIUtils.searchField("  Search " + getEntityName().toLowerCase() + "...");
            searchField.setPreferredSize(new Dimension(260, 36));
            searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            });
            left.add(searchField);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton addBtn  = UIUtils.btnPrimary("+ Add New");
            JButton editBtn = UIUtils.btnGhost("✎  Edit");
            JButton delBtn  = UIUtils.btnDanger("✕  Delete");

            addBtn.addActionListener(e  -> { openAddDialog(); refresh(); });
            editBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { info("Select a row to edit."); return; }
                openEditDialog(table.convertRowIndexToModel(r));
                refresh();
            });
            delBtn.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) { info("Select a row to delete."); return; }
                if (JOptionPane.showConfirmDialog(this, "Delete this record?", "Confirm Delete",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    deleteSelected(table.convertRowIndexToModel(r));
                    refresh();
                }
            });

            right.add(addBtn); right.add(editBtn); right.add(delBtn);
            toolbar.add(left,  BorderLayout.WEST);
            toolbar.add(right, BorderLayout.EAST);
            return toolbar;
        }

        private JPanel buildFooter() {
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(Theme.SURFACE2);
            footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 22, 10, 22)));

            statusLabel = UIUtils.label("Ready", Theme.TEXT2, Theme.FONT_SMALL);
            footer.add(statusLabel, BorderLayout.WEST);

            JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            pager.setOpaque(false);

            prevBtn   = buildPageBtn("← Prev");
            nextBtn   = buildPageBtn("Next →");
            pageLabel = new JLabel("Page 1 of 1");
            pageLabel.setFont(Theme.FONT_SMALL);
            pageLabel.setForeground(Theme.TEXT2);
            rowCountLabel = new JLabel("");
            rowCountLabel.setFont(Theme.FONT_SMALL);
            rowCountLabel.setForeground(Theme.TEXT3);

            prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderCurrentPage(); } });
            nextBtn.addActionListener(e -> {
                int totalPages = Math.max(1, (int)Math.ceil(allRows.size() / (double)PAGE_SIZE));
                if (currentPage < totalPages - 1) { currentPage++; renderCurrentPage(); }
            });

            pager.add(prevBtn); pager.add(pageLabel); pager.add(nextBtn);
            footer.add(pager, BorderLayout.CENTER);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            right.add(rowCountLabel);
            JButton refreshBtn = UIUtils.btnGhost("↻  Refresh");
            refreshBtn.setPreferredSize(new Dimension(100, 30));
            refreshBtn.addActionListener(e -> refresh());
            right.add(refreshBtn);
            footer.add(right, BorderLayout.EAST);
            return footer;
        }

        private JButton buildPageBtn(String text) {
            JButton b = new JButton(text) {
                boolean hover;
                { addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
                  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean en = isEnabled();
                    g2.setColor(en && hover ? Theme.SURFACE3 : Theme.SURFACE2);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                    g2.setColor(en ? Theme.BORDER3 : Theme.BORDER);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 7, 7);
                    g2.setFont(Theme.FONT_SMALL);
                    g2.setColor(en ? (hover ? Theme.TEXT : Theme.TEXT2) : Theme.TEXT3);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                    g2.dispose();
                }
            };
            b.setPreferredSize(new Dimension(88, 30));
            return b;
        }

        private void applyFilter() {
            String text = searchField.getText().trim();
            currentPage = 0;
            sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            renderCurrentPage();
            updatePager();
        }

        public void refresh() {
            loadData(searchField != null ? searchField.getText().trim() : "");
        }

        protected void populateTable(String sql) {
            allRows.clear();
            currentPage = 0;
            try {
                ResultSet rs = DatabaseManager.getInstance().executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) row[i] = rs.getObject(i+1);
                    allRows.add(row);
                }
                rs.getStatement().close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            renderCurrentPage();
            updatePager();
        }

        private void renderCurrentPage() {
            tableModel.setRowCount(0);
            int start = currentPage * PAGE_SIZE;
            int end   = Math.min(start + PAGE_SIZE, allRows.size());
            for (int i = start; i < end; i++) tableModel.addRow(allRows.get(i));
            updatePager();
            if (statusLabel != null) statusLabel.setText("Loaded");
        }

        private void updatePager() {
            int total = allRows.size();
            int totalPages = Math.max(1, (int)Math.ceil(total / (double)PAGE_SIZE));
            if (pageLabel    != null) pageLabel.setText("Page " + (currentPage+1) + " of " + totalPages);
            if (rowCountLabel!= null) rowCountLabel.setText(total + " record" + (total==1?"":"s"));
            if (prevBtn != null) { prevBtn.setEnabled(currentPage > 0); prevBtn.repaint(); }
            if (nextBtn != null) { nextBtn.setEnabled(currentPage < totalPages-1); nextBtn.repaint(); }
        }

        protected Object cell(int modelRow, int col) { return tableModel.getValueAt(modelRow, col); }
        protected void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Info",  JOptionPane.INFORMATION_MESSAGE); }
        protected void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    }

    // ── Classes sub-panel ─────────────────────────────────────────
    static class ClassesSubPanel extends TabbedSubPanel {
        @Override String getEntityName() { return "Classes"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","Class Name","Max Capacity","Description"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT class_id,class_name,max_capacity,CAST(description AS VARCHAR(300)) FROM Class ORDER BY class_id");
        }
        @Override void openAddDialog()           { new ClassDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new ClassDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM Class WHERE class_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }

    // ── Sessions sub-panel ────────────────────────────────────────
    static class SessionsSubPanel extends TabbedSubPanel {
        @Override String getEntityName() { return "Sessions"; }
        @Override String[] getColumnNames() {
            return new String[]{"ID","Class","Trainer","Date","Start","End","Room","Enrolled","Capacity"};
        }
        @Override void loadData(String f) {
            populateTable("SELECT cs.session_id,c.class_name,t.first_name+' '+t.last_name,cs.session_date,cs.start_time,cs.end_time,cs.room,cs.current_enrollment,c.max_capacity FROM ClassSession cs JOIN Class c ON cs.class_id=c.class_id JOIN Trainer t ON cs.trainer_id=t.trainer_id ORDER BY cs.session_date DESC");
        }
        @Override void openAddDialog()           { new SessionDialog(null,null).setVisible(true); }
        @Override void openEditDialog(int row)   { new SessionDialog(null,(int)cell(row,0)).setVisible(true); }
        @Override void deleteSelected(int row)   {
            try { DatabaseManager.getInstance().executeUpdate("DELETE FROM ClassSession WHERE session_id="+(int)cell(row,0)); }
            catch(SQLException e) { error("Delete failed:\n"+e.getMessage()); }
        }
    }
}
