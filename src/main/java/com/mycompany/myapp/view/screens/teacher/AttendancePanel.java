package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.StudentAttendanceDTO;
import com.mycompany.myapp.service.AttendanceService;
import com.mycompany.myapp.service.DynamicQRService; // THÊM IMPORT SERVICE QR
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
    
    // --- COMPONENT CHO QR CODE ---
    private JLabel lblQRCode;
    private JButton btnStartQR;
    private JButton btnStopQR;
    private DynamicQRService qrService;
    // -----------------------------

    private List<StudentAttendanceDTO> currentStudentList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public AttendancePanel() {
        service = new AttendanceService();
        initUI();
        loadClasses(); 
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(245, 245, 249)); 
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ================= HEADER & BỘ LỌC =================
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Điểm Danh Lớp Học");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIKit.TEXT_DARK);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1));

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(200, 38));
        cbxClasses.addActionListener(e -> loadSchedulesForClass());

        cbxSchedules = new JComboBox<>();
        cbxSchedules.setPreferredSize(new Dimension(220, 38));

        txtDate = new JFormattedTextField(sdf);
        txtDate.setValue(new java.util.Date());
        txtDate.setPreferredSize(new Dimension(130, 38));
        txtDate.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(0, 8, 0, 5)
        ));

        JButton btnLoad = createStyledButton("Tải danh sách", new Color(99, 102, 241));
        btnLoad.addActionListener(e -> loadAttendanceData());

        filterBar.add(createLabel("Lớp:"));
        filterBar.add(cbxClasses);
        filterBar.add(createLabel("Ca học:"));
        filterBar.add(cbxSchedules);
        filterBar.add(createLabel("Ngày:"));
        filterBar.add(txtDate);
        filterBar.add(btnLoad);

        topPanel.add(filterBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER: CHIA LƯỚI (QR BÊN TRÁI, TABLE BÊN PHẢI) =================
        JPanel mainContentPanel = new JPanel(new BorderLayout(20, 0)); // Gap 20px
        mainContentPanel.setOpaque(false);

        // --- 1. CỘT TRÁI: KHU VỰC QR ĐỘNG ---
        RoundedPanel qrContainer = new RoundedPanel(15);
        qrContainer.setLayout(new BorderLayout(0, 15));
        qrContainer.setBackground(Color.WHITE);
        qrContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        qrContainer.setPreferredSize(new Dimension(350, 0));

        JLabel lblQrTitle = new JLabel("Mã QR Điểm Danh", SwingConstants.CENTER);
        lblQrTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQrTitle.setForeground(UIKit.TEXT_DARK);
        qrContainer.add(lblQrTitle, BorderLayout.NORTH);

        lblQRCode = new JLabel("Chọn ca học để phát mã", SwingConstants.CENTER);
        lblQRCode.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblQRCode.setForeground(Color.GRAY);
        lblQRCode.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 3, 2));
        qrContainer.add(lblQRCode, BorderLayout.CENTER);

        JPanel qrActionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        qrActionPanel.setOpaque(false);
        
        btnStartQR = createStyledButton("Phát Mã QR", new Color(16, 185, 129)); // Xanh lá
        btnStopQR = createStyledButton("Dừng", new Color(239, 68, 68)); // Đỏ
        btnStopQR.setEnabled(false);

        btnStartQR.addActionListener(e -> startQRSession());
        btnStopQR.addActionListener(e -> stopQRSession());

        qrActionPanel.add(btnStartQR);
        qrActionPanel.add(btnStopQR);
        qrContainer.add(qrActionPanel, BorderLayout.SOUTH);

        mainContentPanel.add(qrContainer, BorderLayout.WEST);

        // --- 2. CỘT PHẢI: BẢNG DỮ LIỆU ---
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã HV", "Họ và Tên", "Trạng Thái", "Ghi Chú"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 2 || col == 3; }
        };

        table = new ModernTable();
        table.setModel(model);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 240));
        table.setRowHeight(40);

        TableColumn statusColumn = table.getColumnModel().getColumn(2);
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Có mặt", "Vắng mặt"});
        statusColumn.setCellEditor(new DefaultCellEditor(comboStatus));

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        mainContentPanel.add(tableContainer, BorderLayout.CENTER);

        add(mainContentPanel, BorderLayout.CENTER);

        // ================= BOTTOM: NÚT LƯU THỦ CÔNG =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        
        JButton btnSave = createStyledButton("Lưu Điểm Danh Thủ Công", new Color(59, 130, 246)); // Xanh dương
        btnSave.setPreferredSize(new Dimension(220, 45));
        btnSave.addActionListener((ActionEvent e) -> saveAttendance());
        
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Hàm Helper tạo nút bấm để code gọn hơn
    private JButton createStyledButton(String text, Color bgColor) {
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

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(70, 70, 80));
        return l;
    }

    // ================= LOGIC QR ĐỘNG =================
    private void startQRSession() {
        ComboItem sch = (ComboItem) cbxSchedules.getSelectedItem();
        if (sch == null || sch.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng tải danh sách học viên trước khi phát mã QR!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Khởi tạo và chạy Service sinh QR
        qrService = new DynamicQRService(lblQRCode, sch.getId());
        qrService.startSession();

        btnStartQR.setEnabled(false);
        btnStopQR.setEnabled(true);
        cbxClasses.setEnabled(false); // Khóa bộ lọc trong lúc đang quét
        cbxSchedules.setEnabled(false);
    }

    private void stopQRSession() {
        if (qrService != null) {
            qrService.stopSession();
        }
        btnStartQR.setEnabled(true);
        btnStopQR.setEnabled(false);
        cbxClasses.setEnabled(true);
        cbxSchedules.setEnabled(true);
    }

    // ================= LOGIC DỮ LIỆU CŨ =================
    private void loadClasses() {
        cbxClasses.removeAllItems();
        Result<List<Map<String, Object>>> res = service.getTeacherClasses();
        if (res.isSuccess() && !res.getData().isEmpty()) {
            for (Map<String, Object> map : res.getData()) {
                cbxClasses.addItem(new ComboItem((int) map.get("class_id"), (String) map.get("class_name")));
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
                    cbxSchedules.addItem(new ComboItem((int) map.get("schedule_id"), (String) map.get("schedule_name")));
                }
            } else {
                cbxSchedules.addItem(new ComboItem(-1, "Chưa có lịch học"));
            }
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
        try { targetDate = sdf.parse(txtDate.getText()); } 
        catch (ParseException e) { JOptionPane.showMessageDialog(this, "Ngày không đúng định dạng!"); return; }

        Result<List<StudentAttendanceDTO>> res = service.getAttendanceList(cls.getId(), sch.getId(), targetDate);
        if (res.isSuccess()) {
            currentStudentList = res.getData();
            model.setRowCount(0);
            if(currentStudentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lớp học này hiện chưa có học viên nào.", "Thông tin", JOptionPane.INFORMATION_MESSAGE);
            }
            for (StudentAttendanceDTO dto : currentStudentList) {
                String statusUI = "PRESENT".equals(dto.getStatus()) ? "Có mặt" : "Vắng mặt";
                model.addRow(new Object[]{ "HV" + String.format("%04d", dto.getStudentId()), dto.getFullName(), statusUI, dto.getNote() != null ? dto.getNote() : "" });
            }
            
            // Nếu có kết quả, vô hiệu hóa mã QR cũ nếu đang chạy để làm mới
            stopQRSession();
            lblQRCode.setText("Bấm 'Phát Mã QR' để bắt đầu");
            lblQRCode.setIcon(null);
        }
    }

    private void saveAttendance() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        if (currentStudentList == null || currentStudentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có danh sách để lưu!"); return;
        }

        for (int i = 0; i < table.getRowCount(); i++) {
            String statusUI = (String) table.getValueAt(i, 2);
            String noteUI = (String) table.getValueAt(i, 3);
            StudentAttendanceDTO dto = currentStudentList.get(i);
            dto.setStatus("Có mặt".equals(statusUI) ? "PRESENT" : "ABSENT");
            dto.setNote(noteUI);
        }

        ComboItem sch = (ComboItem) cbxSchedules.getSelectedItem();
        try {
            java.util.Date targetDate = sdf.parse(txtDate.getText());
            Result<Void> res = service.saveAttendanceList(sch.getId(), targetDate, currentStudentList);
            if (res.isSuccess()) JOptionPane.showMessageDialog(this, res.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (ParseException e) { }
    }

    private class ComboItem {
        private int id; private String name;
        public ComboItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }
}