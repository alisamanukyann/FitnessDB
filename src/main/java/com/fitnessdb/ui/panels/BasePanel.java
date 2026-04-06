package com.fitnessdb.ui.panels;

import com.fitnessdb.db.DatabaseManager;
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

public abstract class BasePanel extends JPanel {

    protected DefaultTableModel tableModel;
    protected JTable table;
    protected JTextField searchField;
    protected JLabel statusLabel;
    protected JLabel rowCountLabel;
    protected TableRowSorter<DefaultTableModel> sorter;

    // ── Pagination state
    private static final int PAGE_SIZE = 20;
    private int currentPage = 0;
    private final List<Object[]> allRows = new ArrayList<>();
    private JLabel pageLabel;
    private JButton prevBtn, nextBtn;

    protected abstract String getPanelTitle();
    protected abstract String getPanelSubtitle();
    protected abstract String[] getColumnNames();
    protected abstract void loadData(String filter);
    protected abstract void openAddDialog();
    protected abstract void openEditDialog(int selectedModelRow);
    protected abstract void deleteSelected(int selectedModelRow);

    public BasePanel() {
        super(new BorderLayout(0, 22));
        setOpaque(false);
        build();
        loadData("");
    }

    private void build() {
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(UIUtils.sectionHeader(getPanelTitle(), getPanelSubtitle()), BorderLayout.WEST);
        add(topRow, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.setColor(Theme.BORDER2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,14,14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.add(buildToolbar(), BorderLayout.NORTH);

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
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0) openEditDialog(table.convertRowIndexToModel(viewRow));
                }
            }
        });

        JScrollPane sp = UIUtils.scroll(table);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        card.add(sp, BorderLayout.CENTER);
        card.add(buildFooter(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Theme.SURFACE2);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER2),
            BorderFactory.createEmptyBorder(13, 22, 13, 22)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        searchField = UIUtils.searchField("  Search " + getPanelTitle().toLowerCase() + "...");
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        left.add(searchField);
        addToolbarExtras(left);

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

        if (showAddButton()) right.add(addBtn);
        right.add(editBtn); right.add(delBtn);
        toolbar.add(left,  BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    protected void addToolbarExtras(JPanel toolbar) {}
    protected boolean showAddButton() { return true; }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.SURFACE2);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 22, 10, 22)));

        // Left — status
        statusLabel = UIUtils.label("Ready", Theme.TEXT2, Theme.FONT_SMALL);
        footer.add(statusLabel, BorderLayout.WEST);

        // Center — pagination controls
        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pager.setOpaque(false);

        prevBtn = buildPageBtn("← Prev");
        nextBtn = buildPageBtn("Next →");
        pageLabel = new JLabel("Page 1 of 1");
        pageLabel.setFont(Theme.FONT_SMALL);
        pageLabel.setForeground(Theme.TEXT2);
        rowCountLabel = new JLabel("");
        rowCountLabel.setFont(Theme.FONT_SMALL);
        rowCountLabel.setForeground(Theme.TEXT3);

        prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderCurrentPage(); } });
        nextBtn.addActionListener(e -> {
            int totalPages = Math.max(1, (int)Math.ceil(allRows.size() / (double)PAGE_SIZE));
            if (currentPage < totalPages-1) { currentPage++; renderCurrentPage(); }
        });

        pager.add(prevBtn);
        pager.add(pageLabel);
        pager.add(nextBtn);

        footer.add(pager, BorderLayout.CENTER);

        // Right — record count + refresh
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
                public void mouseEntered(MouseEvent e) { hover=true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean en = isEnabled();
                g2.setColor(en && hover ? Theme.SURFACE3 : Theme.SURFACE2);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),7,7);
                g2.setColor(en ? Theme.BORDER3 : Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,7,7);
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
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
        // Re-sync allRows with filtered view for pagination on search
        renderCurrentPage();
        updatePager();
    }

    public void refresh() {
        loadData(searchField != null ? searchField.getText().trim() : "");
    }

    /** Called by subclasses — stores all rows, then renders page 0 */
    protected void populateTable(String sql) {
        allRows.clear();
        currentPage = 0;
        if (statusLabel != null) statusLabel.setText("Loading…");
        // Run DB fetch on background thread to prevent UI hang
        new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> rows = new ArrayList<>();
                ResultSet rs = DatabaseManager.getInstance().executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) row[i] = rs.getObject(i+1);
                    rows.add(row);
                }
                rs.getStatement().close();
                return rows;
            }
            @Override protected void done() {
                try {
                    allRows.addAll(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BasePanel.this,
                        "DB error:\n" + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                renderCurrentPage();
                updatePager();
            }
        }.execute();
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
        if (pageLabel != null)
            pageLabel.setText("Page " + (currentPage+1) + " of " + totalPages);
        if (rowCountLabel != null)
            rowCountLabel.setText(total + " record" + (total==1?"":"s"));
        if (prevBtn != null) prevBtn.setEnabled(currentPage > 0);
        if (nextBtn != null) nextBtn.setEnabled(currentPage < totalPages-1);
        if (prevBtn != null) prevBtn.repaint();
        if (nextBtn != null) nextBtn.repaint();
    }

    protected void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }
    protected Object cell(int modelRow, int col) { return tableModel.getValueAt(modelRow, col); }
    protected void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Info",  JOptionPane.INFORMATION_MESSAGE); }
    protected void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
