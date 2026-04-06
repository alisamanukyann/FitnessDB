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

public class SqlPanel extends DbObjectBasePanel {

    public SqlPanel() {
        add(UIUtils.sectionHeader("Execute SQL", "Run any SELECT query against FitnessDB"), BorderLayout.NORTH);
        add(buildSqlCard(), BorderLayout.CENTER);
    }

    private JPanel buildSqlCard() {
        JPanel card = buildCard(Theme.ACCENT);
        card.setLayout(new BorderLayout());
        card.add(buildCardToolbar("Execute SQL", "EDITOR", "Run any SELECT against FitnessDB", Theme.ACCENT), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(Theme.SURFACE);

        // Editor
        JPanel editorWrap = new JPanel(new BorderLayout());
        editorWrap.setBackground(Theme.SURFACE);
        editorWrap.setBorder(BorderFactory.createEmptyBorder(18, 20, 0, 20));

        JTextArea editor = new JTextArea(
            "SELECT TOP 20\n    member_id,\n    first_name,\n    last_name,\n    email\nFROM Member\nORDER BY join_date DESC;");
        editor.setBackground(new Color(0x08090E));
        editor.setForeground(new Color(0xC9D1D9));
        editor.setFont(Theme.FONT_MONO);
        editor.setCaretColor(Theme.ACCENT);
        editor.setSelectionColor(Theme.alpha(Theme.ACCENT, 45));
        editor.setLineWrap(false);
        editor.setRows(8);
        JScrollPane edScroll = new JScrollPane(editor);
        edScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER2));
        edScroll.setOpaque(false);
        edScroll.getViewport().setBackground(new Color(0x08090E));
        editorWrap.add(edScroll, BorderLayout.CENTER);

        // Button row
        DefaultTableModel model = new DefaultTableModel();
        JTable resultTbl = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        UIUtils.styleTable(resultTbl);

        JLabel statusLbl = new JLabel("Ready to execute");
        statusLbl.setFont(Theme.FONT_SMALL);
        statusLbl.setForeground(Theme.TEXT2);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        btnRow.setBackground(Theme.SURFACE);
        btnRow.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton runBtn   = UIUtils.btnPrimary("▶  Run Query");
        JButton clearBtn = UIUtils.btnGhost("Clear");
        runBtn.setPreferredSize(new Dimension(140, 36));
        clearBtn.setPreferredSize(new Dimension(80, 36));

        runBtn.addActionListener(e -> {
            String sql = editor.getText().trim();
            if (sql.isEmpty()) return;
            model.setRowCount(0); model.setColumnCount(0);
            try {
                ResultSet rs = DatabaseManager.getInstance().executeQuery(sql);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int i = 1; i <= cols; i++) model.addColumn(meta.getColumnLabel(i));
                int count = 0;
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) row[i] = rs.getObject(i+1);
                    model.addRow(row); count++;
                }
                rs.getStatement().close();
                UIUtils.styleTable(resultTbl);
                statusLbl.setForeground(Theme.SUCCESS);
                statusLbl.setText("✓  " + count + " row" + (count==1?"":"s") + " returned");
            } catch (SQLException ex) {
                statusLbl.setForeground(Theme.DANGER);
                statusLbl.setText("✗  " + ex.getMessage());
            }
        });
        clearBtn.addActionListener(e -> {
            editor.setText(""); model.setRowCount(0); model.setColumnCount(0);
            statusLbl.setText("Ready to execute"); statusLbl.setForeground(Theme.TEXT2);
        });

        btnRow.add(runBtn); btnRow.add(clearBtn);
        btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(statusLbl);

        // Result pane
        JPanel resultHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        resultHeader.setBackground(Theme.SURFACE2);
        resultHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        JLabel resultLbl = new JLabel("  ↓  RESULT SET");
        resultLbl.setFont(Theme.FONT_HEADER);
        resultLbl.setForeground(Theme.TEXT2);
        resultHeader.add(resultLbl);

        JPanel resultWrapper = new JPanel(new BorderLayout());
        resultWrapper.setBackground(Theme.SURFACE2);
        resultWrapper.setBorder(BorderFactory.createMatteBorder(2,0,0,0, Theme.alpha(Theme.ACCENT,80)));
        resultWrapper.add(resultHeader, BorderLayout.NORTH);
        resultWrapper.add(UIUtils.scroll(resultTbl), BorderLayout.CENTER);
        resultWrapper.setPreferredSize(new Dimension(0, 320));

        body.add(editorWrap,    BorderLayout.NORTH);
        body.add(btnRow,        BorderLayout.CENTER);
        body.add(resultWrapper, BorderLayout.SOUTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
