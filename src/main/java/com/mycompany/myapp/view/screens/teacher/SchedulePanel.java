package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.TeacherScheduleDTO;
import com.mycompany.myapp.service.TeacherScheduleService;
import com.mycompany.myapp.utils.SessionStore;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SchedulePanel extends JPanel {
    private ModernTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private TeacherScheduleService scheduleService;

    private JLabel lblStatus;
    private JLabel lblTotalSessions;
    private JLabel lblTeachingDays;
    private JLabel lblFreeDays;
    private JTextField txtSearch;
    private JComboBox<String> cmbDayFilter;

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

    public SchedulePanel() {
        scheduleService = new TeacherScheduleService();
        initUI();
        loadDataFromDatabase();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 22));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScheduleCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lịch dạy hàng tuần");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Theo dõi ca dạy, lớp học, môn học và phòng học trong tuần");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        JButton btnReload = createOutlineButton("Làm mới");
        btnReload.addActionListener(e -> loadDataFromDatabase());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnReload);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        JPanel metricRow = new JPanel(new GridLayout(1, 3, 18, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 104));

        lblTotalSessions = new JLabel("0");
        lblTeachingDays = new JLabel("0");
        lblFreeDays = new JLabel("0");

        metricRow.add(createMetricCard("Tổng ca dạy", lblTotalSessions, "Ca", new ClockIconPanel(PRIMARY, PRIMARY_SOFT), PRIMARY));
        metricRow.add(createMetricCard("Ngày có lịch", lblTeachingDays, "Ngày", new CalendarIconPanel(BLUE, BLUE_SOFT), BLUE));
        metricRow.add(createMetricCard("Ngày trống", lblFreeDays, "Ngày", new FreeDayIconPanel(GREEN, GREEN_SOFT), GREEN));

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(metricRow, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildScheduleCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(BG_CARD);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        card.add(buildToolbar(), BorderLayout.NORTH);

        String[] cols = {"Thứ", "Giờ bắt đầu", "Giờ kết thúc", "Môn học", "Lớp học", "Phòng học", "dayKey"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable();
        table.setModel(model);
        configureTable(table);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        // Ẩn cột kỹ thuật dayKey nhưng vẫn dùng để lọc theo thứ.
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(6));

        ModernScrollPane scrollPane = new ModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_MAIN);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.setToolTipText("Tìm theo môn học, lớp học hoặc phòng học");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JLabel lblDay = new JLabel("Thứ");
        lblDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDay.setForeground(TEXT_MAIN);

        cmbDayFilter = new JComboBox<>(new String[]{
                "Tất cả",
                "Thứ 2",
                "Thứ 3",
                "Thứ 4",
                "Thứ 5",
                "Thứ 6",
                "Thứ 7",
                "Chủ Nhật"
        });
        cmbDayFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbDayFilter.setBackground(Color.WHITE);
        cmbDayFilter.setPreferredSize(new Dimension(150, 38));
        cmbDayFilter.addActionListener(e -> applyFilter());

        gbc.gridx = 0;
        gbc.weightx = 0;
        left.add(lblSearch, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        left.add(txtSearch, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        left.add(lblDay, gbc);

        gbc.gridx = 3;
        left.add(cmbDayFilter, gbc);

        JLabel hint = new JLabel("Lọc nhanh lịch dạy theo lớp, môn học hoặc từng ngày trong tuần");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);

        toolbar.add(left, BorderLayout.CENTER);
        toolbar.add(hint, BorderLayout.SOUTH);

        return toolbar;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String unit, JPanel iconBox, Color accent) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_MUTED);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        valueRow.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUnit.setForeground(accent);

        valueRow.add(valueLabel);
        valueRow.add(lblUnit);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(10));
        info.add(valueRow);

        card.add(iconBox, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                new EmptyBorder(9, 18, 9, 18)
        ));
        return button;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(PRIMARY);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 234, 240));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        ScheduleCellRenderer renderer = new ScheduleCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(95);
        table.getColumnModel().getColumn(1).setPreferredWidth(115);
        table.getColumnModel().getColumn(2).setPreferredWidth(115);
        table.getColumnModel().getColumn(3).setPreferredWidth(210);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
    }

    private void applyFilter() {
        if (rowSorter == null) return;

        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();
        String day = cmbDayFilter == null || cmbDayFilter.getSelectedItem() == null
                ? "Tất cả"
                : cmbDayFilter.getSelectedItem().toString();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 3, 4, 5));
        }

        if (!"Tất cả".equals(day)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(day) + "$", 6));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void setLoadingState(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(loading ? "Đang tải dữ liệu..." : "Dữ liệu đã cập nhật");
        }
    }

    private void loadDataFromDatabase() {
        Integer teacherId = SessionStore.getUserId();

        if (teacherId == null || teacherId == -1) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin phiên đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setLoadingState(true);

        SwingWorker<Map<Integer, List<TeacherScheduleDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<Integer, List<TeacherScheduleDTO>> doInBackground() {
                return scheduleService.getGroupedSchedule(teacherId.longValue());
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, List<TeacherScheduleDTO>> data = get();
                    renderScheduleData(data);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            SchedulePanel.this,
                            "Lỗi tải dữ liệu: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoadingState(false);
                }
            }
        };

        worker.execute();
    }

    private void renderScheduleData(Map<Integer, List<TeacherScheduleDTO>> data) {
        model.setRowCount(0);

        int totalSessions = 0;
        int teachingDays = 0;
        int freeDays = 0;

        for (int i = 2; i <= 8; i++) {
            List<TeacherScheduleDTO> daySchedules = data == null ? null : data.get(i);
            String dayText = (i == 8) ? "Chủ Nhật" : "Thứ " + i;

            if (daySchedules == null || daySchedules.isEmpty()) {
                freeDays++;
                model.addRow(new Object[]{
                        dayText,
                        "-",
                        "-",
                        "Nghỉ / Không có lịch dạy",
                        "-",
                        "-",
                        dayText
                });
            } else {
                teachingDays++;
                totalSessions += daySchedules.size();

                for (int j = 0; j < daySchedules.size(); j++) {
                    TeacherScheduleDTO dto = daySchedules.get(j);
                    String displayDayText = (j == 0) ? dayText : "";

                    model.addRow(new Object[]{
                            displayDayText,
                            dto.getStartTime(),
                            dto.getEndTime(),
                            dto.getSubjectName(),
                            dto.getClassName(),
                            dto.getRoomName(),
                            dayText
                    });
                }
            }
        }

        lblTotalSessions.setText(String.valueOf(totalSessions));
        lblTeachingDays.setText(String.valueOf(teachingDays));
        lblFreeDays.setText(String.valueOf(freeDays));

        applyFilter();
    }

    private static class ScheduleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(0, 10, 0, 10));

            String subject = "";
            try {
                int modelRow = table.convertRowIndexToModel(row);
                subject = table.getModel().getValueAt(modelRow, 3).toString();
            } catch (Exception ignored) {
            }

            boolean isFreeDay = subject.contains("Không có lịch");

            if (isSelected) {
                setBackground(new Color(108, 92, 231));
                setForeground(Color.WHITE);
                return comp;
            }

            if (isFreeDay) {
                setBackground(new Color(248, 250, 252));
                setForeground(new Color(148, 163, 184));
                setFont(new Font("Segoe UI", Font.ITALIC, 13));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                setForeground(new Color(15, 23, 42));
                if (column == 0) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setForeground(new Color(108, 92, 231));
                }
            }

            if (value != null) {
                setToolTipText(value.toString());
            } else {
                setToolTipText("");
            }

            return comp;
        }
    }

    private static abstract class MetricIconPanel extends JPanel {
        private final Color accent;
        private final Color soft;

        MetricIconPanel(Color accent, Color soft) {
            this.accent = accent;
            this.soft = soft;
            setOpaque(false);
            setPreferredSize(new Dimension(46, 46));
            setMinimumSize(new Dimension(46, 46));
            setMaximumSize(new Dimension(46, 46));
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

            drawIcon(g2, accent);

            g2.dispose();
        }

        protected abstract void drawIcon(Graphics2D g2, Color accent);
    }

    private static class ClockIconPanel extends MetricIconPanel {
        ClockIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2, Color accent) {
            g2.drawOval(14, 10, 18, 18);
            g2.drawLine(23, 19, 23, 13);
            g2.drawLine(23, 19, 28, 22);
            g2.drawLine(14, 34, 32, 34);
        }
    }

    private static class CalendarIconPanel extends MetricIconPanel {
        CalendarIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2, Color accent) {
            g2.drawRoundRect(12, 13, 22, 20, 5, 5);
            g2.drawLine(12, 19, 34, 19);
            g2.drawLine(18, 10, 18, 15);
            g2.drawLine(28, 10, 28, 15);
            g2.fillOval(17, 24, 3, 3);
            g2.fillOval(24, 24, 3, 3);
            g2.fillOval(17, 29, 3, 3);
        }
    }

    private static class FreeDayIconPanel extends MetricIconPanel {
        FreeDayIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2, Color accent) {
            g2.drawRoundRect(13, 13, 20, 20, 6, 6);
            g2.drawLine(17, 23, 21, 27);
            g2.drawLine(21, 27, 30, 18);
        }
    }
}
