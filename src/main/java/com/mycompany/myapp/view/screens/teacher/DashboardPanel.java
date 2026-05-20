package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private JLabel lblTotalClasses;
    private JLabel lblTotalStudents;
    private JLabel lblTodaySchedules;
    private JLabel lblPendingGrades;
    private JLabel lblStatus;

    private DefaultTableModel scheduleModel;
    private DefaultTableModel attendanceModel;

    private ModernTable scheduleTable;
    private ModernTable attendanceTable;

    private final DashboardService service;

    private static final Color BG_PAGE = new Color(248, 250, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);

    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_SOFT = new Color(219, 234, 254);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_SOFT = new Color(220, 252, 231);

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    private static final Color RED = new Color(220, 38, 38);
    private static final Color RED_SOFT = new Color(254, 226, 226);

    public DashboardPanel() {
        service = new DashboardService();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 22));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenterContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel headerWrapper = new JPanel(new BorderLayout(0, 18));
        headerWrapper.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Tổng quan giảng dạy");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Theo dõi lớp học, lịch dạy, học viên và chuyên cần trong ngày");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);
        lblStatus.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setForeground(PRIMARY);
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                new EmptyBorder(9, 18, 9, 18)
        ));
        btnRefresh.addActionListener(e -> loadData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnRefresh);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 18, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 118));

        lblTotalClasses = new JLabel("0", SwingConstants.LEFT);
        lblTotalStudents = new JLabel("0", SwingConstants.LEFT);
        lblTodaySchedules = new JLabel("0", SwingConstants.LEFT);
        lblPendingGrades = new JLabel("0", SwingConstants.LEFT);

        cardsPanel.add(createMetricCard("Lớp đang dạy", lblTotalClasses, "Lớp", "📚", PRIMARY, PRIMARY_SOFT));
        cardsPanel.add(createMetricCard("Tổng học viên", lblTotalStudents, "HV", "👥", BLUE, BLUE_SOFT));
        cardsPanel.add(createMetricCard("Lịch hôm nay", lblTodaySchedules, "Ca", "🕒", GREEN, GREEN_SOFT));
        cardsPanel.add(createMetricCard("Chờ chấm điểm", lblPendingGrades, "Bài", "✎", ORANGE, ORANGE_SOFT));

        headerWrapper.add(header, BorderLayout.NORTH);
        headerWrapper.add(cardsPanel, BorderLayout.CENTER);

        return headerWrapper;
    }

    private JPanel buildCenterContent() {
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 22, 0));
        mainContent.setOpaque(false);

        mainContent.add(buildSchedulePanel());
        mainContent.add(buildAttendancePanel());

        return mainContent;
    }

    private JPanel buildSchedulePanel() {
        RoundedPanel container = createContentCard();

        JPanel titleBar = createSectionTitle(
                "Lịch dạy sắp tới",
                "Danh sách ca dạy cần chuẩn bị",
                "🗓",
                PRIMARY,
                PRIMARY_SOFT
        );
        container.add(titleBar, BorderLayout.NORTH);

        String[] schCols = {"Thứ", "Thời gian", "Lớp", "Môn học"};
        scheduleModel = new DefaultTableModel(schCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        scheduleTable = new ModernTable();
        scheduleTable.setModel(scheduleModel);
        configureTable(scheduleTable);
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        scheduleTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        scheduleTable.getColumnModel().getColumn(3).setPreferredWidth(210);

        ModernScrollPane scrollPane = new ModernScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel buildAttendancePanel() {
        RoundedPanel container = createContentCard();

        JPanel titleBar = createSectionTitle(
                "Thống kê chuyên cần",
                "Tỷ lệ đi học trung bình theo lớp",
                "▥",
                GREEN,
                GREEN_SOFT
        );
        container.add(titleBar, BorderLayout.NORTH);

        String[] attCols = {"Tên lớp", "Tỷ lệ đi học trung bình"};
        attendanceModel = new DefaultTableModel(attCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        attendanceTable = new ModernTable();
        attendanceTable.setModel(attendanceModel);
        configureTable(attendanceTable);
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        attendanceTable.getColumnModel().getColumn(1).setCellRenderer(new AttendanceRateRenderer());

        ModernScrollPane scrollPane = new ModernScrollPane(attendanceTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        container.add(scrollPane, BorderLayout.CENTER);

        return container;
    }

    private RoundedPanel createContentCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return card;
    }

    private JPanel createSectionTitle(String title, String subtitle, String iconText, Color accent, Color soft) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JPanel iconBox = new RoundedIconBox(iconText, accent, soft, 42, 42);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBox.add(lblTitle);
        textBox.add(Box.createVerticalStrut(4));
        textBox.add(lblSub);

        panel.add(iconBox, BorderLayout.WEST);
        panel.add(textBox, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String unit, String iconText, Color accentColor, Color softColor) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel iconBox = new RoundedIconBox(iconText, accentColor, softColor, 46, 46);

        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_MUTED);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valuePanel.setOpaque(false);
        valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUnit.setForeground(accentColor);

        valuePanel.add(valueLabel);
        valuePanel.add(lblUnit);

        infoBox.add(lblTitle);
        infoBox.add(Box.createVerticalStrut(10));
        infoBox.add(valuePanel);

        card.add(iconBox, BorderLayout.WEST);
        card.add(infoBox, BorderLayout.CENTER);

        return card;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(46);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setSelectionBackground(new Color(245, 243, 255));
        table.setSelectionForeground(PRIMARY);
        table.setGridColor(new Color(226, 232, 240));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setRowMargin(1);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new BusinessTableCellRenderer());
    }

    private void setLoadingState(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(loading ? "Đang tải dữ liệu..." : "Dữ liệu đã cập nhật");
        }
    }

    private void loadData() {
        setLoadingState(true);

        SwingWorker<Result<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<Map<String, Object>> doInBackground() {
                return service.getFullDashboardData();
            }

            @Override
            protected void done() {
                try {
                    Result<Map<String, Object>> res = get();

                    if (res.isSuccess()) {
                        Map<String, Object> data = res.getData();

                        Map<String, Integer> metrics = (Map<String, Integer>) data.get("metrics");
                        lblTotalClasses.setText(String.valueOf(metrics.get("totalClasses")));
                        lblTotalStudents.setText(String.valueOf(metrics.get("totalStudents")));
                        lblTodaySchedules.setText(String.valueOf(metrics.get("todaySchedules")));
                        lblPendingGrades.setText(String.valueOf(metrics.get("pendingGrades")));

                        scheduleModel.setRowCount(0);
                        List<Map<String, Object>> schedules = (List<Map<String, Object>>) data.get("schedules");
                        for (Map<String, Object> s : schedules) {
                            scheduleModel.addRow(new Object[]{
                                    s.get("day"),
                                    s.get("time"),
                                    s.get("class_name"),
                                    s.get("subject")
                            });
                        }

                        attendanceModel.setRowCount(0);
                        List<Map<String, Object>> attendance = (List<Map<String, Object>>) data.get("attendance");
                        for (Map<String, Object> a : attendance) {
                            attendanceModel.addRow(new Object[]{
                                    a.get("class_name"),
                                    String.format("%.1f %%", (Double) a.get("rate"))
                            });
                        }

                        if (schedules.isEmpty()) {
                            scheduleModel.addRow(new Object[]{"-", "Không có lịch", "-", "-"});
                        }

                        if (attendance.isEmpty()) {
                            attendanceModel.addRow(new Object[]{"Chưa có dữ liệu", "0.0 %"});
                        }
                    } else {
                        JOptionPane.showMessageDialog(DashboardPanel.this, res.getMessage(), "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Không thể tải dữ liệu dashboard: " + e.getMessage(),
                            "Lỗi dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                } finally {
                    setLoadingState(false);
                }
            }
        };

        worker.execute();
    }

    private static class RoundedIconBox extends JPanel {
        private final String text;
        private final Color accent;
        private final Color soft;

        RoundedIconBox(String text, Color accent, Color soft, int width, int height) {
            this.text = text;
            this.accent = accent;
            this.soft = soft;

            setOpaque(false);
            setPreferredSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2.setColor(soft);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(accent);
            g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(text, x, y);
            g2.dispose();
        }
    }

    private static class BusinessTableCellRenderer extends DefaultTableCellRenderer {
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

            label.setFont(new Font("Segoe UI", column == 0 ? Font.BOLD : Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(0, 10, 0, 10));
            label.setOpaque(true);
            label.setToolTipText(value == null ? "" : value.toString());

            if (isSelected) {
                label.setBackground(new Color(238, 234, 255));
                label.setForeground(PRIMARY);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                label.setForeground(column == 0 ? PRIMARY : TEXT_MAIN);
            }

            if (column == 0 || column == 1) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
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
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color bg;
            Color fg;
            Color fill;

            if (rate < 75) {
                bg = RED_SOFT;
                fg = RED;
                fill = RED;
            } else if (rate < 85) {
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
                fg = PRIMARY;
                fill = PRIMARY;
            }

            int x = 10;
            int y = 9;
            int w = getWidth() - 20;
            int h = getHeight() - 18;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, 18, 18);

            int fillW = Math.max(8, (int) Math.round(w * Math.max(0, Math.min(rate, 100)) / 100.0));
            g2.setColor(new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 48));
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

}
