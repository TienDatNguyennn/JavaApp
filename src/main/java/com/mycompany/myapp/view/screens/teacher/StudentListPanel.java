package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.ClassStudentDTO;
import com.mycompany.myapp.service.LearningService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StudentListPanel extends JPanel {

    private JComboBox<ClassItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private LearningService learningService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private JTextField txtSearch;
    private JLabel lblStatus;
    private JLabel lblClassCount;
    private JLabel lblStudentCount;
    private JLabel lblActiveCount;
    private JLabel lblHint;

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

    public StudentListPanel() {
        learningService = new LearningService();
        initUI();
        loadTeacherClasses();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(18, 24, 22, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Danh sách học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Theo dõi học viên theo lớp, liên hệ phụ huynh và trạng thái học tập");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Đang tải dữ liệu...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        JButton btnReload = createOutlineButton("Làm mới");
        btnReload.setPreferredSize(new Dimension(105, 36));
        btnReload.addActionListener(e -> loadTeacherClasses());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(lblStatus);
        actions.add(btnReload);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(actions, BorderLayout.EAST);

        RoundedPanel metricBar = new RoundedPanel(16);
        metricBar.setBackground(Color.WHITE);
        metricBar.setLayout(new GridLayout(1, 3, 12, 0));
        metricBar.setPreferredSize(new Dimension(0, 92));
        metricBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        lblClassCount = new JLabel("0");
        lblStudentCount = new JLabel("0");
        lblActiveCount = new JLabel("0");

        metricBar.add(createCompactMetric("Lớp phân công", lblClassCount, "Lớp", PRIMARY));
        metricBar.add(createCompactMetric("Học viên", lblStudentCount, "HV", BLUE));
        metricBar.add(createCompactMetric("Đang học", lblActiveCount, "HV", GREEN));

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(metricBar, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildTableCard() {
        RoundedPanel tableContainer = new RoundedPanel(18);
        tableContainer.setBackground(BG_CARD);
        tableContainer.setLayout(new BorderLayout(0, 10));
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(12, 14, 14, 14)
        ));

        tableContainer.add(buildToolbar(), BorderLayout.NORTH);

        String[] cols = {"Mã HV", "Họ và tên", "Ngày sinh", "Giới tính", "SĐT HV", "Phụ huynh", "SĐT phụ huynh", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable();
        table.setModel(model);
        configureTable(table);

        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        ModernScrollPane scrollPane = new ModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        return tableContainer;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 8));
        toolbar.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(10, 0));
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Danh sách theo lớp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT_MAIN);

        lblHint = new JLabel("Chọn lớp để xem học viên");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(TEXT_MUTED);
        lblHint.setHorizontalAlignment(SwingConstants.RIGHT);

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(lblHint, BorderLayout.EAST);

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(true);
        filterRow.setBackground(new Color(248, 250, 252));
        filterRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblClass = new JLabel("Lớp");
        lblClass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClass.setForeground(TEXT_MAIN);

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(260, 34));
        cbxClasses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxClasses.setBackground(Color.WHITE);
        cbxClasses.setToolTipText("Chọn lớp để xem danh sách học viên");
        cbxClasses.addActionListener((ActionEvent e) -> handleClassSelection());

        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_MAIN);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 34));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 10, 0, 10)
        ));
        txtSearch.setToolTipText("Tìm theo mã học viên, tên, số điện thoại hoặc phụ huynh");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearch(); }
            public void removeUpdate(DocumentEvent e) { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }
        });

        gbc.gridx = 0;
        gbc.weightx = 0;
        filterRow.add(lblClass, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        filterRow.add(cbxClasses, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        filterRow.add(lblSearch, gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterRow.add(txtSearch, gbc);

        toolbar.add(titleRow, BorderLayout.NORTH);
        toolbar.add(filterRow, BorderLayout.CENTER);

        return toolbar;
    }

    private JPanel createCompactMetric(String title, JLabel valueLabel, String unit, Color accent) {
        JPanel item = new JPanel(new BorderLayout(14, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel iconBox = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color soft;
                if (accent.equals(PRIMARY)) {
                    soft = new Color(245, 235, 255);
                } else if (accent.equals(BLUE)) {
                    soft = new Color(219, 234, 254);
                } else if (accent.equals(GREEN)) {
                    soft = new Color(220, 252, 231);
                } else {
                    soft = new Color(241, 245, 249);
                }

                g2.setColor(soft);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                if (title.toLowerCase().contains("lớp")) {
                    g2.drawRoundRect(cx - 9, cy - 11, 18, 22, 4, 4);
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy - 5);
                    g2.drawLine(cx - 5, cy, cx + 5, cy);
                    g2.drawLine(cx - 5, cy + 5, cx + 2, cy + 5);
                } else if (title.toLowerCase().contains("học viên")) {
                    g2.fillOval(cx - 9, cy - 9, 7, 7);
                    g2.fillOval(cx + 2, cy - 9, 7, 7);
                    g2.drawArc(cx - 13, cy + 1, 14, 12, 0, 180);
                    g2.drawArc(cx, cy + 1, 14, 12, 0, 180);
                } else {
                    g2.drawOval(cx - 11, cy - 11, 22, 22);
                    g2.drawLine(cx - 6, cy, cx - 1, cy + 5);
                    g2.drawLine(cx - 1, cy + 5, cx + 8, cy - 6);
                }

                g2.dispose();
            }
        };
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(54, 54));
        iconBox.setMinimumSize(new Dimension(54, 54));
        iconBox.setMaximumSize(new Dimension(54, 54));

        JPanel textBox = new JPanel(new GridBagLayout());
        textBox.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(TEXT_MUTED);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        valueRow.setOpaque(false);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);
        valueLabel.setVerticalAlignment(SwingConstants.CENTER);

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUnit.setForeground(accent);
        lblUnit.setVerticalAlignment(SwingConstants.CENTER);

        valueRow.add(valueLabel);
        valueRow.add(lblUnit);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        textBox.add(lblTitle, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        textBox.add(valueRow, gbc);

        item.add(iconBox, BorderLayout.WEST);
        item.add(textBox, BorderLayout.CENTER);

        return item;
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
        table.setRowHeight(38);
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
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new StudentTableRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(175);
        table.getColumnModel().getColumn(6).setPreferredWidth(135);
        table.getColumnModel().getColumn(7).setPreferredWidth(115);
    }

    private void setLoadingState(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(loading ? "Đang tải dữ liệu..." : "Dữ liệu đã cập nhật");
        }
    }

    private void loadTeacherClasses() {
        setLoadingState(true);

        SwingWorker<Result<List<Map<String, Object>>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<Map<String, Object>>> doInBackground() {
                return learningService.getMyTeachingClasses();
            }

            @Override
            protected void done() {
                try {
                    Result<List<Map<String, Object>>> result = get();

                    cbxClasses.removeAllItems();

                    if (result.isSuccess()) {
                        List<Map<String, Object>> classes = result.getData();

                        lblClassCount.setText(String.valueOf(classes.size()));

                        if (classes.isEmpty()) {
                            cbxClasses.addItem(new ClassItem(-1, "-- Bạn chưa được phân công dạy lớp nào --"));
                            model.setRowCount(0);
                            resetMetricsForNoClass();
                            return;
                        }

                        for (Map<String, Object> map : classes) {
                            int id = toInt(map.get("class_id"));
                            String name = safe(map.get("class_name"));
                            cbxClasses.addItem(new ClassItem(id, name));
                        }
                    } else {
                        JOptionPane.showMessageDialog(StudentListPanel.this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StudentListPanel.this, "Lỗi tải danh sách lớp: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoadingState(false);
                }
            }
        };

        worker.execute();
    }

    private void handleClassSelection() {
        ClassItem selectedClass = (ClassItem) cbxClasses.getSelectedItem();

        if (selectedClass == null || selectedClass.getId() == -1) {
            model.setRowCount(0);
            resetMetricsForNoClass();
            return;
        }

        setLoadingState(true);

        SwingWorker<Result<List<ClassStudentDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<ClassStudentDTO>> doInBackground() {
                return learningService.getStudentsByClass(selectedClass.getId());
            }

            @Override
            protected void done() {
                try {
                    Result<List<ClassStudentDTO>> result = get();
                    model.setRowCount(0);

                    if (result.isSuccess()) {
                        List<ClassStudentDTO> students = result.getData();
                        int activeCount = 0;

                        for (ClassStudentDTO s : students) {
                            String genderStr = mapGender(s.getGender());
                            String statusStr = mapStatus(s.getStatus());

                            if ("Đang học".equals(statusStr)) {
                                activeCount++;
                            }

                            model.addRow(new Object[]{
                                    "HV" + String.format("%04d", s.getStudentId()),
                                    safe(s.getFullName()),
                                    s.getDob() != null ? sdf.format(s.getDob()) : "",
                                    genderStr,
                                    safeOrDash(s.getPhone()),
                                    safeOrDash(s.getParentName()),
                                    safeOrDash(s.getParentPhone()),
                                    statusStr
                            });
                        }

                        lblStudentCount.setText(String.valueOf(students.size()));
                        lblActiveCount.setText(String.valueOf(activeCount));

                        if (students.isEmpty()) {
                            lblHint.setText("Lớp " + selectedClass + " chưa có học viên.");
                        } else {
                            lblHint.setText(selectedClass + " · " + students.size() + " học viên");
                        }

                        applySearch();
                    } else {
                        JOptionPane.showMessageDialog(StudentListPanel.this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(StudentListPanel.this, "Lỗi tải danh sách học viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    setLoadingState(false);
                }
            }
        };

        worker.execute();
    }

    private void applySearch() {
        if (rowSorter == null) return;

        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
    }

    private void resetMetricsForNoClass() {
        lblStudentCount.setText("0");
        lblActiveCount.setText("0");
        lblHint.setText("Chưa có lớp được chọn hoặc giáo viên chưa được phân công lớp.");
    }

    private String mapGender(String gender) {
        if ("M".equals(gender)) return "Nam";
        if ("F".equals(gender)) return "Nữ";
        return "Khác";
    }

    private String mapStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "ACTIVE": return "Đang học";
            case "RESERVED": return "Bảo lưu";
            case "DROPPED": return "Đã nghỉ";
            default: return status;
        }
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private String safeOrDash(Object value) {
        String text = safe(value).trim();
        return text.isEmpty() ? "-" : text;
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

    private class ClassItem {
        private int id;
        private String name;

        public ClassItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class StudentTableRenderer extends DefaultTableCellRenderer {
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
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (isSelected) {
                setBackground(new Color(108, 92, 231));
                setForeground(Color.WHITE);
                return comp;
            }

            setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            setForeground(new Color(15, 23, 42));

            if (column == 0) {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(new Color(108, 92, 231));
            }

            if (value != null) {
                setToolTipText(value.toString());
            } else {
                setToolTipText("");
            }

            return comp;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));

            if (isSelected) {
                label.setBackground(new Color(108, 92, 231));
                label.setForeground(Color.WHITE);
                return label;
            }

            String status = label.getText();

            if ("Đang học".equals(status)) {
                label.setBackground(GREEN_SOFT);
                label.setForeground(GREEN);
            } else if ("Bảo lưu".equals(status)) {
                label.setBackground(ORANGE_SOFT);
                label.setForeground(ORANGE);
            } else if ("Đã nghỉ".equals(status)) {
                label.setBackground(RED_SOFT);
                label.setForeground(RED);
            } else {
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(TEXT_MUTED);
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

            drawIcon(g2);

            g2.dispose();
        }

        protected abstract void drawIcon(Graphics2D g2);
    }

    private static class ClassIconPanel extends MetricIconPanel {
        ClassIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRoundRect(13, 12, 20, 22, 4, 4);
            g2.drawLine(17, 18, 29, 18);
            g2.drawLine(17, 23, 29, 23);
            g2.drawLine(17, 28, 25, 28);
        }
    }

    private static class StudentIconPanel extends MetricIconPanel {
        StudentIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.fillOval(14, 12, 7, 7);
            g2.fillOval(24, 12, 7, 7);
            g2.drawArc(11, 22, 13, 12, 0, 180);
            g2.drawArc(22, 22, 13, 12, 0, 180);
        }
    }

    private static class ActiveIconPanel extends MetricIconPanel {
        ActiveIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 22, 22);
            g2.drawLine(17, 24, 22, 29);
            g2.drawLine(22, 29, 31, 18);
        }
    }
}
