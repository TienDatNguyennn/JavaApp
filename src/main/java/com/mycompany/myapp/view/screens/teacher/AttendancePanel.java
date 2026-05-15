package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.StudentAttendanceDTO;
import com.mycompany.myapp.service.AttendanceService;
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
    
    private List<StudentAttendanceDTO> currentStudentList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public AttendancePanel() {
        service = new AttendanceService();
        initUI();
        loadClasses(); 
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(245, 245, 249)); // Màu nền nhạt giúp bảng nổi bật
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ================= HEADER & BỘ LỌC =================
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Điểm Danh Lớp Học");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIKit.TEXT_DARK);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // Filter Bar với style hiện đại hơn
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1));

        // Hàm helper để tạo label nhỏ phía trên input
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

        // FIX LỖI NÚT TÀNG HÌNH: Tải danh sách
        JButton btnLoad = new JButton("Tải danh sách");
        btnLoad.setPreferredSize(new Dimension(140, 38));
        btnLoad.setBackground(new Color(99, 102, 241)); // Tím Indigo
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setOpaque(true);
        btnLoad.setBorderPainted(false);
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLoad.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        // ================= CENTER: BẢNG DỮ LIỆU =================
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
        table.setRowHeight(40); // Tăng chiều cao dòng cho dễ bấm ComboBox

        // Nhúng ComboBox vào cột "Trạng Thái"
        TableColumn statusColumn = table.getColumnModel().getColumn(2);
        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Có mặt", "Vắng mặt"});
        statusColumn.setCellEditor(new DefaultCellEditor(comboStatus));

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // ================= BOTTOM: NÚT LƯU =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        
        // FIX LỖI NÚT TÀNG HÌNH: Lưu điểm danh
        JButton btnSave = new JButton("Lưu Điểm Danh");
        btnSave.setPreferredSize(new Dimension(180, 45));
        btnSave.setBackground(new Color(34, 197, 94)); // Xanh lá đậm
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener((ActionEvent e) -> saveAttendance());
        
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(70, 70, 80));
        return l;
    }

    // --- Các hàm Logic giữ nguyên để đảm bảo Backend chạy đúng ---
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