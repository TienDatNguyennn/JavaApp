package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import com.mycompany.myapp.service.AttendanceAnalyticsService;
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
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsPanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private AttendanceAnalyticsService service;

    private JLabel lblStatus;
    private JLabel lblClassCount;
    private JLabel lblStudentCount;
    private JLabel lblRiskCount;
    private JLabel lblAverageRate;
    private JLabel lblSelectedClass;
    private JLabel lblHint;

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

    public AttendanceAnalyticsPanel() {
        service = new AttendanceAnalyticsService();
        initUI();
        loadClasses();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Theo dõi chuyên cần");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subtitle = new JLabel("Phân tích tỷ lệ đi học theo lớp để phát hiện học viên cần theo dõi");
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
        lblRiskCount = new JLabel("0");
        lblAverageRate = new JLabel("0.0%");

        metricRow.add(createMetricCard("Lớp phụ trách", lblClassCount, "Lớp", new ClassIconPanel(PRIMARY, PRIMARY_SOFT), PRIMARY));
        metricRow.add(createMetricCard("Học viên", lblStudentCount, "HV", new StudentIconPanel(BLUE, BLUE_SOFT), BLUE));
        metricRow.add(createMetricCard("Cần theo dõi", lblRiskCount, "HV", new RiskIconPanel(RED, RED_SOFT), RED));
        metricRow.add(createMetricCard("Tỷ lệ TB", lblAverageRate, "", new RateIconPanel(GREEN, GREEN_SOFT), GREEN));

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

        String[] cols = {"Mã HV", "Họ và tên", "Tổng buổi", "Có mặt", "Vắng", "Tỷ lệ chuyên cần"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable();
        table.setModel(model);
        configureTable(table);

        table.getColumnModel().getColumn(5).setCellRenderer(new AttendanceRateRenderer());

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

        JLabel title = new JLabel("Bảng phân tích chuyên cần");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        lblSelectedClass = new JLabel("Chọn lớp để xem thống kê");
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
        cbxClasses.setToolTipText("Chọn lớp cần xem chuyên cần");

        JButton btnLoad = createPrimaryButton("Xem thống kê");
        btnLoad.setPreferredSize(new Dimension(140, 38));
        btnLoad.addActionListener(e -> loadAnalyticsData());

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

        lblHint = new JLabel("Gợi ý nghiệp vụ: học viên có tỷ lệ dưới 80% cần được nhắc nhở hoặc trao đổi với phụ huynh.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(TEXT_MUTED);

        toolbar.add(top, BorderLayout.NORTH);
        toolbar.add(lblHint, BorderLayout.SOUTH);

        return toolbar;
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
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
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

        table.setDefaultRenderer(Object.class, new AnalyticsCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(170);
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
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

    private void loadAnalyticsData() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();

        if (cls == null || cls.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        lblSelectedClass.setText("Đang xem lớp: " + cls);
        setLoadingState(true, "Đang tải thống kê...");

        SwingWorker<Result<List<AttendanceAnalyticsDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<AttendanceAnalyticsDTO>> doInBackground() {
                return service.getClassAnalytics(cls.getId());
            }

            @Override
            protected void done() {
                try {
                    Result<List<AttendanceAnalyticsDTO>> res = get();
                    model.setRowCount(0);

                    if (res.isSuccess()) {
                        List<AttendanceAnalyticsDTO> data = res.getData();

                        if (data.isEmpty()) {
                            resetSummary();
                            lblHint.setText("Lớp này chưa có dữ liệu chuyên cần.");
                            JOptionPane.showMessageDialog(AttendanceAnalyticsPanel.this, "Lớp này chưa có dữ liệu học viên.");
                            return;
                        }

                        int riskCount = 0;
                        double totalRate = 0;

                        for (AttendanceAnalyticsDTO dto : data) {
                            double rate = dto.getAttendanceRate();
                            totalRate += rate;
                            if (rate < 80.0) riskCount++;

                            model.addRow(new Object[]{
                                    "HV" + String.format("%04d", dto.getStudentId()),
                                    dto.getFullName(),
                                    dto.getTotalSessions(),
                                    dto.getPresentCount(),
                                    dto.getAbsentCount(),
                                    String.format("%.1f %%", rate)
                            });
                        }

                        lblStudentCount.setText(String.valueOf(data.size()));
                        lblRiskCount.setText(String.valueOf(riskCount));
                        lblAverageRate.setText(String.format("%.1f%%", totalRate / data.size()));

                        if (riskCount > 0) {
                            lblHint.setText("Có " + riskCount + " học viên dưới 80%. Nên ưu tiên nhắc nhở hoặc liên hệ phụ huynh.");
                        } else {
                            lblHint.setText("Lớp đang có chuyên cần ổn định. Tiếp tục duy trì theo dõi định kỳ.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(AttendanceAnalyticsPanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            AttendanceAnalyticsPanel.this,
                            "Không thể tải thống kê chuyên cần: " + e.getMessage(),
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

    private void resetSummary() {
        lblStudentCount.setText("0");
        lblRiskCount.setText("0");
        lblAverageRate.setText("0.0%");
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

    private static class AnalyticsCellRenderer extends DefaultTableCellRenderer {
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
                label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 13));
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }

            return label;
        }
    }

    private static class AttendanceRateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            return new RatePillPanel(value == null ? "0.0 %" : value.toString(), isSelected);
        }
    }

    private static class RatePillPanel extends JPanel {
        private final String text;
        private final boolean selected;
        private double rate = 0;

        RatePillPanel(String text, boolean selected) {
            this.text = text;
            this.selected = selected;
            setOpaque(false);
            setBorder(new EmptyBorder(6, 10, 6, 10));

            try {
                rate = Double.parseDouble(text.replace("%", "").trim());
            } catch (Exception ignored) {
                rate = 0;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            Color fg;
            Color fill;

            if (rate < 80) {
                bg = RED_SOFT;
                fg = RED;
                fill = RED;
            } else if (rate < 90) {
                bg = ORANGE_SOFT;
                fg = ORANGE;
                fill = ORANGE;
            } else {
                bg = GREEN_SOFT;
                fg = GREEN;
                fill = GREEN;
            }

            if (selected) {
                bg = PRIMARY_SOFT;
                fg = PRIMARY_DARK;
                fill = PRIMARY;
            }

            int x = 10;
            int y = 9;
            int w = getWidth() - 20;
            int h = getHeight() - 18;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, 18, 18);

            int fillW = Math.max(8, (int) Math.round(w * Math.max(0, Math.min(rate, 100)) / 100.0));
            g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 46));
            g2.fillRoundRect(x, y, fillW, h, 18, 18);

            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();

            int tx = x + (w - fm.stringWidth(text)) / 2;
            int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, tx, ty);

            g2.dispose();
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

    private static class RiskIconPanel extends MetricIconPanel {
        RiskIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 20, 20);
            g2.drawLine(22, 16, 22, 24);
            g2.fillOval(21, 27, 2, 2);
        }
    }

    private static class RateIconPanel extends MetricIconPanel {
        RateIconPanel(Color accent, Color soft) { super(accent, soft); }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawLine(12, 31, 32, 31);
            g2.fillRoundRect(14, 22, 4, 9, 2, 2);
            g2.fillRoundRect(21, 16, 4, 15, 2, 2);
            g2.fillRoundRect(28, 11, 4, 20, 2, 2);
        }
    }
}
