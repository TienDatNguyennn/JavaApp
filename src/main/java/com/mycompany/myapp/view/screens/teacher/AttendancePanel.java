package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.StudentAttendanceDTO;
import com.mycompany.myapp.service.AttendanceService;
import com.mycompany.myapp.service.DynamicQRService;
import com.mycompany.myapp.utils.EventBus;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.ModernScrollPane;
import com.mycompany.myapp.view.components.UIKit.ModernTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class AttendancePanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private JComboBox<ComboItem> cbxSchedules;
    private JFormattedTextField txtDate;

    private ModernTable table;
    private DefaultTableModel model;
    private AttendanceService service;

    private JLabel lblQRCode;
    private JLabel lblStatus;
    private JLabel lblTotalStudents;
    private JLabel lblPresentStudents;
    private JLabel lblAbsentStudents;
    private JLabel lblQrState;
    private JLabel lblHint;

    private JButton btnStartQR;
    private JButton btnStopQR;
    private JButton btnLoad;
    private JButton btnSave;
    private JButton btnOpenMock;

    private DynamicQRService qrService;

    private List<StudentAttendanceDTO> currentStudentList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

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

    private static final Color RED = new Color(220, 38, 38);
    private static final Color RED_SOFT = new Color(254, 226, 226);

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    public AttendancePanel() {
        service = new AttendanceService();
        initUI();
        loadClasses();
        setupRealtimeReceiver();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 22));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildBottomActions(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(18, 0));
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Điểm danh lớp học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Quản lý điểm danh thủ công, phát mã QR động và cập nhật trạng thái theo thời gian thực");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTED);

        titleRow.add(titleBox, BorderLayout.WEST);
        titleRow.add(lblStatus, BorderLayout.EAST);

        JPanel metricRow = new JPanel(new GridLayout(1, 3, 18, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 104));

        lblTotalStudents = new JLabel("0");
        lblPresentStudents = new JLabel("0");
        lblAbsentStudents = new JLabel("0");

        metricRow.add(createMetricCard("Tổng học viên", lblTotalStudents, "HV", new StudentIconPanel(BLUE, BLUE_SOFT), BLUE));
        metricRow.add(createMetricCard("Có mặt", lblPresentStudents, "HV", new CheckIconPanel(GREEN, GREEN_SOFT), GREEN));
        metricRow.add(createMetricCard("Vắng mặt", lblAbsentStudents, "HV", new AbsenceIconPanel(RED, RED_SOFT), RED));

        wrapper.add(titleRow, BorderLayout.NORTH);
        wrapper.add(metricRow, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildMainContent() {
        JPanel mainContentPanel = new JPanel(new BorderLayout(22, 0));
        mainContentPanel.setOpaque(false);

        mainContentPanel.add(buildQrCard(), BorderLayout.WEST);
        mainContentPanel.add(buildAttendanceTableCard(), BorderLayout.CENTER);

        return mainContentPanel;
    }

    private JPanel buildQrCard() {
        RoundedPanel qrContainer = new RoundedPanel(18);
        qrContainer.setLayout(new BorderLayout(0, 16));
        qrContainer.setBackground(BG_CARD);
        qrContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        qrContainer.setPreferredSize(new Dimension(355, 0));

        JPanel titleBox = new JPanel(new BorderLayout(12, 0));
        titleBox.setOpaque(false);

        JPanel icon = new QrIconPanel(PRIMARY, PRIMARY_SOFT);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));

        JLabel lblQrTitle = new JLabel("Mã QR điểm danh");
        lblQrTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQrTitle.setForeground(TEXT_MAIN);

        lblQrState = new JLabel("Chưa phát mã");
        lblQrState.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQrState.setForeground(TEXT_MUTED);

        textBox.add(lblQrTitle);
        textBox.add(Box.createVerticalStrut(4));
        textBox.add(lblQrState);

        titleBox.add(icon, BorderLayout.WEST);
        titleBox.add(textBox, BorderLayout.CENTER);

        lblQRCode = new JLabel("Chọn lớp, ca học và bấm tải danh sách", SwingConstants.CENTER);
        lblQRCode.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblQRCode.setForeground(TEXT_MUTED);
        lblQRCode.setOpaque(true);
        lblQRCode.setBackground(new Color(248, 250, 252));
        lblQRCode.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel qrActionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        qrActionPanel.setOpaque(false);

        btnStartQR = createFilledButton("Phát QR", GREEN);
        btnStopQR = createFilledButton("Dừng", RED);
        btnStopQR.setEnabled(false);

        btnStartQR.addActionListener(e -> startQRSession());
        btnStopQR.addActionListener(e -> stopQRSession());

        qrActionPanel.add(btnStartQR);
        qrActionPanel.add(btnStopQR);

        qrContainer.add(titleBox, BorderLayout.NORTH);
        qrContainer.add(lblQRCode, BorderLayout.CENTER);
        qrContainer.add(qrActionPanel, BorderLayout.SOUTH);

        return qrContainer;
    }

    private JPanel buildAttendanceTableCard() {
        RoundedPanel tableContainer = new RoundedPanel(18);
        tableContainer.setLayout(new BorderLayout(0, 16));
        tableContainer.setBackground(BG_CARD);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        tableContainer.add(buildFilterBar(), BorderLayout.NORTH);

        String[] cols = {"Mã HV", "Họ và tên", "Trạng thái", "Ghi chú"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 2 || col == 3; }
        };

        table = new ModernTable();
        table.setModel(model);
        configureTable(table);

        TableColumn statusColumn = table.getColumnModel().getColumn(2);
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Có mặt", "Vắng mặt"});
        comboStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboStatus.setBackground(Color.WHITE);
        statusColumn.setCellEditor(new DefaultCellEditor(comboStatus));
        statusColumn.setCellRenderer(new AttendanceStatusRenderer());

        ModernScrollPane scrollPane = new ModernScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        return tableContainer;
    }

    private JPanel buildFilterBar() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel filterBar = new JPanel(new GridBagLayout());
        filterBar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(190, 38));
        configureComboBox(cbxClasses);
        cbxClasses.addActionListener(e -> loadSchedulesForClass());

        cbxSchedules = new JComboBox<>();
        cbxSchedules.setPreferredSize(new Dimension(220, 38));
        configureComboBox(cbxSchedules);

        txtDate = new JFormattedTextField(sdf);
        txtDate.setValue(new java.util.Date());
        txtDate.setPreferredSize(new Dimension(130, 38));
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(0, 10, 0, 8)
        ));

        btnLoad = createFilledButton("Tải danh sách", PRIMARY);
        btnLoad.setPreferredSize(new Dimension(135, 38));
        btnLoad.addActionListener(e -> loadAttendanceData());

        gbc.gridx = 0;
        filterBar.add(createLabel("Lớp"), gbc);

        gbc.gridx = 1;
        filterBar.add(cbxClasses, gbc);

        gbc.gridx = 2;
        filterBar.add(createLabel("Ca học"), gbc);

        gbc.gridx = 3;
        filterBar.add(cbxSchedules, gbc);

        gbc.gridx = 4;
        filterBar.add(createLabel("Ngày"), gbc);

        gbc.gridx = 5;
        filterBar.add(txtDate, gbc);

        gbc.gridx = 6;
        filterBar.add(btnLoad, gbc);

        lblHint = new JLabel("Chọn lớp, ca học và ngày để tải danh sách điểm danh.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(TEXT_MUTED);

        wrapper.add(filterBar, BorderLayout.CENTER);
        wrapper.add(lblHint, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel buildBottomActions() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomPanel.setOpaque(false);

        btnOpenMock = createOutlineButton("Mở trạm giả lập test");
        btnOpenMock.setPreferredSize(new Dimension(190, 42));
        btnOpenMock.addActionListener(e -> {
            JFrame frame = new JFrame("Trạm Giả Lập Quét Mã Đa Luồng");
            frame.setSize(650, 450);
            frame.setLocationRelativeTo(this);
            frame.add(new com.mycompany.myapp.view.screens.teacher.MockConcurrencyTestUI());
            frame.setVisible(true);
        });

        btnSave = createFilledButton("Lưu điểm danh", BLUE);
        btnSave.setPreferredSize(new Dimension(165, 42));
        btnSave.addActionListener((ActionEvent e) -> saveAttendance());

        bottomPanel.add(btnOpenMock);
        bottomPanel.add(btnSave);

        return bottomPanel;
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

    private void configureComboBox(JComboBox<ComboItem> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
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

        DefaultTableCellRenderer renderer = new AttendanceTableRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(125);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
    }

    private JButton createFilledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MAIN);
        return label;
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }

    private void updateAttendanceSummary() {
        int total = model == null ? 0 : model.getRowCount();
        int present = 0;

        if (model != null) {
            for (int i = 0; i < model.getRowCount(); i++) {
                Object value = model.getValueAt(i, 2);
                if ("Có mặt".equals(value)) {
                    present++;
                }
            }
        }

        int absent = Math.max(0, total - present);

        lblTotalStudents.setText(String.valueOf(total));
        lblPresentStudents.setText(String.valueOf(present));
        lblAbsentStudents.setText(String.valueOf(absent));
    }

    private void setupRealtimeReceiver() {
        EventBus.register((studentId, status) -> {
            SwingUtilities.invokeLater(() -> {
                String targetMaHV = "HV" + String.format("%04d", studentId);
                boolean found = false;

                for (int r = 0; r < model.getRowCount(); r++) {
                    String rowId = model.getValueAt(r, 0).toString();

                    if (rowId.equals(targetMaHV) || rowId.contains(String.valueOf(studentId))) {
                        model.setValueAt("Có mặt", r, 2);
                        model.setValueAt("Quét QR Auto", r, 3);

                        if (currentStudentList != null && r < currentStudentList.size()) {
                            currentStudentList.get(r).setStatus("PRESENT");
                            currentStudentList.get(r).setNote("Quét QR Auto");
                        }

                        found = true;
                        break;
                    }
                }

                if (found) {
                    updateAttendanceSummary();
                    lblHint.setText("Đã nhận tín hiệu điểm danh QR cho học viên " + targetMaHV + ".");
                    setLoadingState(false, "Đã cập nhật QR");
                } else {
                    lblHint.setText("Không tìm thấy học viên " + targetMaHV + " trong danh sách hiện tại.");
                }
            });
        });
    }

    private void startQRSession() {
        ComboItem sch = (ComboItem) cbxSchedules.getSelectedItem();

        if (sch == null || sch.getId() == -1 || currentStudentList == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tải danh sách học viên trước khi phát mã QR!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        qrService = new DynamicQRService(lblQRCode, sch.getId());
        qrService.startSession();

        btnStartQR.setEnabled(false);
        btnStopQR.setEnabled(true);
        cbxClasses.setEnabled(false);
        cbxSchedules.setEnabled(false);
        btnLoad.setEnabled(false);
        lblQrState.setText("Đang phát mã QR động");
        lblQrState.setForeground(GREEN);
        setLoadingState(false, "QR đang hoạt động");
    }

    private void stopQRSession() {
        if (qrService != null) {
            qrService.stopSession();
        }

        btnStartQR.setEnabled(true);
        btnStopQR.setEnabled(false);
        cbxClasses.setEnabled(true);
        cbxSchedules.setEnabled(true);
        btnLoad.setEnabled(true);
        lblQrState.setText("Đã dừng phát mã");
        lblQrState.setForeground(TEXT_MUTED);
        setLoadingState(false, "Sẵn sàng");
    }

    private void loadClasses() {
        cbxClasses.removeAllItems();

        Result<List<Map<String, Object>>> res = service.getTeacherClasses();

        if (res.isSuccess() && !res.getData().isEmpty()) {
            for (Map<String, Object> map : res.getData()) {
                cbxClasses.addItem(new ComboItem(toInt(map.get("class_id")), safe(map.get("class_name"))));
            }
        } else {
            cbxClasses.addItem(new ComboItem(-1, "Không có lớp"));
        }
    }

    private void loadSchedulesForClass() {
        cbxSchedules.removeAllItems();

        ComboItem selectedClass = (ComboItem) cbxClasses.getSelectedItem();

        if (selectedClass != null && selectedClass.getId() != -1) {
            Result<List<Map<String, Object>>> res = service.getClassSchedules(selectedClass.getId());

            if (res.isSuccess() && !res.getData().isEmpty()) {
                for (Map<String, Object> map : res.getData()) {
                    cbxSchedules.addItem(new ComboItem(toInt(map.get("schedule_id")), safe(map.get("schedule_name"))));
                }
            } else {
                cbxSchedules.addItem(new ComboItem(-1, "Chưa có lịch học"));
            }
        } else {
            cbxSchedules.addItem(new ComboItem(-1, "Chưa có lịch học"));
        }
    }

    private void loadAttendanceData() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();
        ComboItem sch = (ComboItem) cbxSchedules.getSelectedItem();

        if (cls == null || cls.getId() == -1 || sch == null || sch.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Lớp và Ca học hợp lệ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.Date targetDate;

        try {
            targetDate = sdf.parse(txtDate.getText());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày không đúng định dạng dd/MM/yyyy!", "Lỗi ngày", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setLoadingState(true, "Đang tải danh sách...");

        SwingWorker<Result<List<StudentAttendanceDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<StudentAttendanceDTO>> doInBackground() {
                return service.getAttendanceList(cls.getId(), sch.getId(), targetDate);
            }

            @Override
            protected void done() {
                try {
                    Result<List<StudentAttendanceDTO>> res = get();

                    if (res.isSuccess()) {
                        currentStudentList = res.getData();
                        model.setRowCount(0);

                        if (currentStudentList.isEmpty()) {
                            lblHint.setText("Lớp học này hiện chưa có học viên nào.");
                        }

                        for (StudentAttendanceDTO dto : currentStudentList) {
                            String statusUI = "PRESENT".equals(dto.getStatus()) ? "Có mặt" : "Vắng mặt";

                            model.addRow(new Object[]{
                                    "HV" + String.format("%04d", dto.getStudentId()),
                                    dto.getFullName(),
                                    statusUI,
                                    dto.getNote() != null ? dto.getNote() : ""
                            });
                        }

                        stopQRSession();
                        lblQRCode.setText("Bấm 'Phát QR' để bắt đầu");
                        lblQRCode.setIcon(null);
                        lblQrState.setText("Sẵn sàng phát mã");
                        lblQrState.setForeground(TEXT_MUTED);
                        lblHint.setText("Đã tải " + currentStudentList.size() + " học viên. Có thể phát QR hoặc chỉnh điểm danh thủ công.");
                        updateAttendanceSummary();
                    } else {
                        JOptionPane.showMessageDialog(AttendancePanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AttendancePanel.this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setLoadingState(false, "Dữ liệu đã cập nhật");
                }
            }
        };

        worker.execute();
    }

    private void saveAttendance() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        if (currentStudentList == null || currentStudentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có danh sách để lưu!");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            String statusUI = safe(model.getValueAt(i, 2));
            String noteUI = safe(model.getValueAt(i, 3));
            StudentAttendanceDTO dto = currentStudentList.get(i);
            dto.setStatus("Có mặt".equals(statusUI) ? "PRESENT" : "ABSENT");
            dto.setNote(noteUI);
        }

        ComboItem sch = (ComboItem) cbxSchedules.getSelectedItem();

        try {
            java.util.Date targetDate = sdf.parse(txtDate.getText());
            Result<Void> res = service.saveAttendanceList(sch.getId(), targetDate, currentStudentList);

            if (res.isSuccess()) {
                updateAttendanceSummary();
                JOptionPane.showMessageDialog(this, res.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày không đúng định dạng dd/MM/yyyy!", "Lỗi ngày", JOptionPane.WARNING_MESSAGE);
        }
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

    private static class AttendanceTableRenderer extends DefaultTableCellRenderer {
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

            if (isSelected) {
                setBackground(PRIMARY);
                setForeground(Color.WHITE);
                return comp;
            }

            setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
            setForeground(TEXT_MAIN);

            if (column == 0) {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(PRIMARY);
            }

            if (value != null) {
                setToolTipText(value.toString());
            } else {
                setToolTipText("");
            }

            return comp;
        }
    }

    private static class AttendanceStatusRenderer extends DefaultTableCellRenderer {
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
            label.setBorder(new EmptyBorder(5, 8, 5, 8));

            if (isSelected) {
                label.setBackground(PRIMARY);
                label.setForeground(Color.WHITE);
                return label;
            }

            if ("Có mặt".equals(label.getText())) {
                label.setBackground(GREEN_SOFT);
                label.setForeground(GREEN);
            } else {
                label.setBackground(RED_SOFT);
                label.setForeground(RED);
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

    private static class CheckIconPanel extends MetricIconPanel {
        CheckIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 22, 22);
            g2.drawLine(17, 24, 22, 29);
            g2.drawLine(22, 29, 31, 18);
        }
    }

    private static class AbsenceIconPanel extends MetricIconPanel {
        AbsenceIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawOval(12, 12, 22, 22);
            g2.drawLine(18, 18, 28, 28);
            g2.drawLine(28, 18, 18, 28);
        }
    }

    private static class QrIconPanel extends MetricIconPanel {
        QrIconPanel(Color accent, Color soft) {
            super(accent, soft);
        }

        @Override
        protected void drawIcon(Graphics2D g2) {
            g2.drawRect(13, 13, 7, 7);
            g2.drawRect(26, 13, 7, 7);
            g2.drawRect(13, 26, 7, 7);
            g2.fillRect(25, 25, 3, 3);
            g2.fillRect(30, 25, 3, 3);
            g2.fillRect(25, 30, 3, 3);
            g2.fillRect(31, 31, 2, 2);
        }
    }
}
