package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.CourseResultDTO;
import com.mycompany.myapp.service.GradeService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit.ModernScrollPane;
import com.mycompany.myapp.view.components.UIKit.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class GradeEntryPanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private GradeService service;

    private List<CourseResultDTO> currentGrades;

    private JLabel lblStatus;
    private JLabel lblClassCount;
    private JLabel lblStudentCount;
    private JLabel lblEnteredCount;
    private JLabel lblAverageScore;
    private JLabel lblSelectedClass;
    private JLabel lblHint;

    private JButton btnLoad;
    private JButton btnSave;

    private static final Color BG_PAGE = new Color(248, 250, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_SOFT = new Color(219, 234, 254);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_SOFT = new Color(220, 252, 231);

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    private static final Color RED = new Color(220, 38, 38);
    private static final Color RED_SOFT = new Color(254, 226, 226);

    public GradeEntryPanel() {
        service = new GradeService();
        initUI();
        loadClasses();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildBottomActions(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Nhập điểm tổng kết");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Nhập điểm cuối khóa, tự động xếp loại và lưu bảng điểm theo từng lớp");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(lblStatus, BorderLayout.EAST);

        JPanel metricRow = new JPanel(new GridLayout(1, 4, 14, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 96));

        lblClassCount = new JLabel("0");
        lblStudentCount = new JLabel("0");
        lblEnteredCount = new JLabel("0");
        lblAverageScore = new JLabel("0.0");

        metricRow.add(createMetricCard("Lớp phụ trách", lblClassCount, "Lớp", new ClassIconPanel(PRIMARY, PRIMARY_SOFT), PRIMARY));
        metricRow.add(createMetricCard("Học viên", lblStudentCount, "HV", new StudentIconPanel(BLUE, BLUE_SOFT), BLUE));
        metricRow.add(createMetricCard("Đã nhập điểm", lblEnteredCount, "HV", new CheckIconPanel(GREEN, GREEN_SOFT), GREEN));
        metricRow.add(createMetricCard("Điểm TB", lblAverageScore, "/10", new ScoreIconPanel(ORANGE, ORANGE_SOFT), ORANGE));

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(metricRow, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildTableCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        card.add(buildToolbar(), BorderLayout.NORTH);

        String[] cols = {"Mã HV", "Họ và tên", "Điểm tổng kết", "Xếp loại"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 2) {
                    try {
                        String valStr = aValue == null ? "" : aValue.toString().trim().replace(",", ".");

                        if (valStr.isEmpty()) {
                            super.setValueAt("", row, 2);
                            super.setValueAt("", row, 3);
                            updateGradeSummary();
                            return;
                        }

                        double score = Double.parseDouble(valStr);

                        if (score < 0.0 || score > 10.0) {
                            JOptionPane.showMessageDialog(
                                    GradeEntryPanel.this,
                                    "Điểm số phải nằm trong khoảng từ 0 đến 10.",
                                    "Lỗi nhập liệu",
                                    JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }

                        super.setValueAt(score, row, 2);
                        super.setValueAt(GradeService.calculateRank(score), row, 3);
                        updateGradeSummary();

                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(
                                GradeEntryPanel.this,
                                "Vui lòng chỉ nhập số. Ví dụ: 8.5 hoặc 8,5",
                                "Lỗi nhập liệu",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        table = new ModernTable();
        table.setModel(model);
        configureTable(table);

        table.getColumnModel().getColumn(2).setCellRenderer(new ScoreInputRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new RankBadgeRenderer());

        ModernScrollPane scrollPane = new ModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 12));
        toolbar.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Bảng điểm học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        lblSelectedClass = new JLabel("Chọn lớp để tải danh sách nhập điểm");
        lblSelectedClass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSelectedClass.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSelectedClass);

        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setOpaque(true);
        filterBar.setBackground(new Color(248, 250, 252));
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblFilter = new JLabel("Lớp học");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(TEXT_MAIN);

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(260, 38));
        cbxClasses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxClasses.setBackground(Color.WHITE);
        cbxClasses.setToolTipText("Chọn lớp cần nhập điểm");

        btnLoad = createPrimaryButton("Tải danh sách");
        btnLoad.setPreferredSize(new Dimension(140, 38));
        btnLoad.addActionListener(e -> loadGradeData());

        gbc.gridx = 0;
        gbc.weightx = 0;
        filterBar.add(lblFilter, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        filterBar.add(cbxClasses, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterBar.add(btnLoad, gbc);

        top.add(titleBox, BorderLayout.WEST);
        top.add(filterBar, BorderLayout.EAST);

        lblHint = new JLabel("Gợi ý: chỉ cột “Điểm tổng kết” được nhập. Hệ thống tự xếp loại sau khi nhập điểm hợp lệ.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(TEXT_MUTED);

        toolbar.add(top, BorderLayout.NORTH);
        toolbar.add(lblHint, BorderLayout.SOUTH);

        return toolbar;
    }

    private JPanel buildBottomActions() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JLabel note = new JLabel("Kiểm tra kỹ điểm trước khi lưu. Điểm hợp lệ từ 0 đến 10.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(TEXT_MUTED);

        btnSave = createFilledButton("Lưu bảng điểm", GREEN);
        btnSave.setPreferredSize(new Dimension(170, 42));
        btnSave.addActionListener((ActionEvent e) -> saveGradeData());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnSave);

        bottomPanel.add(note, BorderLayout.WEST);
        bottomPanel.add(right, BorderLayout.EAST);

        return bottomPanel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String unit, JPanel iconBox, Color accent) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_MUTED);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valueRow.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 25));
        valueLabel.setForeground(accent);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUnit.setForeground(accent);

        valueRow.add(valueLabel);
        if (unit != null && !unit.isEmpty()) valueRow.add(lblUnit);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(8));
        info.add(valueRow);

        card.add(iconBox, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private JButton createPrimaryButton(String text) {
        return createFilledButton(text, PRIMARY);
    }

    private JButton createFilledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new GradeCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
        if (btnLoad != null) btnLoad.setEnabled(!loading);
        if (btnSave != null) btnSave.setEnabled(!loading);
    }

    private void loadClasses() {
        setLoadingState(true, "Đang tải lớp học...");

        Result<List<Map<String, Object>>> res = service.getTeacherClasses();
        cbxClasses.removeAllItems();

        if (res.isSuccess() && !res.getData().isEmpty()) {
            List<Map<String, Object>> classes = res.getData();
            lblClassCount.setText(String.valueOf(classes.size()));

            for (Map<String, Object> map : classes) {
                cbxClasses.addItem(new ComboItem(toInt(map.get("class_id")), safe(map.get("class_name"))));
            }

            setLoadingState(false, "Sẵn sàng");
        } else {
            cbxClasses.addItem(new ComboItem(-1, "Không có lớp"));
            lblClassCount.setText("0");
            setLoadingState(false, "Không có lớp");
        }
    }

    private void loadGradeData() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();

        if (cls == null || cls.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblSelectedClass.setText("Đang nhập điểm lớp: " + cls);
        setLoadingState(true, "Đang tải danh sách...");

        SwingWorker<Result<List<CourseResultDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<CourseResultDTO>> doInBackground() {
                return service.getStudentGrades(cls.getId());
            }

            @Override
            protected void done() {
                try {
                    Result<List<CourseResultDTO>> res = get();

                    if (res.isSuccess()) {
                        currentGrades = res.getData();
                        model.setRowCount(0);

                        for (CourseResultDTO dto : currentGrades) {
                            model.addRow(new Object[]{
                                    "HV" + String.format("%04d", dto.getStudentId()),
                                    dto.getFullName(),
                                    dto.getFinalScore() != null ? dto.getFinalScore() : "",
                                    dto.getRank() != null ? dto.getRank() : ""
                            });
                        }

                        updateGradeSummary();

                        if (currentGrades.isEmpty()) {
                            lblHint.setText("Lớp này chưa có học viên để nhập điểm.");
                        } else {
                            lblHint.setText("Có " + currentGrades.size() + " học viên. Nhập điểm vào cột “Điểm tổng kết”, hệ thống tự xếp loại.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(GradeEntryPanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            GradeEntryPanel.this,
                            "Không thể tải danh sách điểm: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                } finally {
                    setLoadingState(false, "Dữ liệu đã cập nhật");
                }
            }
        };

        worker.execute();
    }

    private void updateGradeSummary() {
        int total = model == null ? 0 : model.getRowCount();
        int entered = 0;
        double sum = 0;

        if (model != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                Object value = model.getValueAt(i, 2);
                String scoreText = value == null ? "" : value.toString().trim();

                if (!scoreText.isEmpty()) {
                    try {
                        double score = Double.parseDouble(scoreText.replace(",", "."));
                        entered++;
                        sum += score;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        lblStudentCount.setText(String.valueOf(total));
        lblEnteredCount.setText(String.valueOf(entered));
        lblAverageScore.setText(entered == 0 ? "0.0" : String.format("%.1f", sum / entered));
    }

    private void saveGradeData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        if (currentGrades == null || currentGrades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có danh sách điểm để lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();

        if (cls == null || cls.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < table.getRowCount(); i++) {
            Object scoreVal = table.getValueAt(i, 2);
            String scoreStr = (scoreVal == null) ? "" : scoreVal.toString().trim();
            CourseResultDTO dto = currentGrades.get(i);

            if (scoreStr.isEmpty()) {
                dto.setFinalScore(null);
                dto.setRank(null);
            } else {
                dto.setFinalScore(Double.parseDouble(scoreStr.replace(",", ".")));
            }
        }

        setLoadingState(true, "Đang lưu bảng điểm...");

        SwingWorker<Result<Void>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<Void> doInBackground() {
                return service.saveGrades(cls.getId(), currentGrades);
            }

            @Override
            protected void done() {
                try {
                    Result<Void> res = get();

                    if (res.isSuccess()) {
                        JOptionPane.showMessageDialog(GradeEntryPanel.this, res.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadGradeData();
                    } else {
                        JOptionPane.showMessageDialog(GradeEntryPanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            GradeEntryPanel.this,
                            "Không thể lưu bảng điểm: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoadingState(false, "Sẵn sàng");
                }
            }
        };

        worker.execute();
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private int toInt(Object value) {
        if (value == null) return -1;
        if (value instanceof Number) return ((Number) value).intValue();

        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return -1;
        }
    }

    private class ComboItem {
        private int id;
        private String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return name; }
    }

    private static class GradeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setToolTipText(value == null ? "" : value.toString());
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(column == 0 ? PRIMARY_DARK : TEXT_MAIN);
            }

            if (column == 0 || column >= 2) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 13));
            return label;
        }
    }

    private static class ScoreInputRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setToolTipText("Nhập điểm từ 0 đến 10");

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
            } else {
                label.setBackground(new Color(255, 251, 235));
                label.setForeground(new Color(120, 53, 15));
            }

            return label;
        }
    }

    private static class RankBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            String rank = value == null ? "" : value.toString();

            JLabel label = new JLabel(rank.isEmpty() ? "Chưa có" : rank, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(5, 8, 5, 8));
            label.setToolTipText(rank);

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
                return label;
            }

            String lower = rank.toLowerCase();

            if (rank.isEmpty()) {
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(TEXT_MUTED);
            } else if (lower.contains("yếu") || lower.contains("kém") || lower.contains("trượt")) {
                label.setBackground(RED_SOFT);
                label.setForeground(RED);
            } else if (lower.contains("trung") || lower.contains("đạt")) {
                label.setBackground(ORANGE_SOFT);
                label.setForeground(ORANGE);
            } else {
                label.setBackground(GREEN_SOFT);
                label.setForeground(GREEN);
            }

            return label;
        }
    }

    private static abstract class MetricIconPanel extends JPanel {
        private final Color accent;
        private final Color soft;

        MetricIconPanel(Color accent, Color soft) {
            this.accent = accent;
            this.soft = soft;
            setOpaque(false);
            setPreferredSize(new Dimension(44, 44));
            setMinimumSize(new Dimension(44, 44));
            setMaximumSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(soft);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawIcon(g2);
            g2.dispose();
        }

        protected abstract void drawIcon(Graphics2D g2);
    }

    private static class ClassIconPanel extends MetricIconPanel {
        ClassIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRoundRect(12, 11, 20, 22, 4, 4);
            g2.drawLine(16, 17, 28, 17);
            g2.drawLine(16, 22, 28, 22);
            g2.drawLine(16, 27, 24, 27);
        }
    }

    private static class StudentIconPanel extends MetricIconPanel {
        StudentIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.fillOval(13, 12, 7, 7);
            g2.fillOval(23, 12, 7, 7);
            g2.drawArc(10, 22, 13, 12, 0, 180);
            g2.drawArc(21, 22, 13, 12, 0, 180);
        }
    }

    private static class CheckIconPanel extends MetricIconPanel {
        CheckIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 20, 20);
            g2.drawLine(17, 23, 21, 27);
            g2.drawLine(21, 27, 30, 17);
        }
    }

    private static class ScoreIconPanel extends MetricIconPanel {
        ScoreIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRoundRect(13, 12, 18, 20, 4, 4);
            g2.drawLine(17, 18, 27, 18);
            g2.drawLine(17, 23, 24, 23);
            g2.drawLine(17, 28, 26, 28);
        }
    }
}
