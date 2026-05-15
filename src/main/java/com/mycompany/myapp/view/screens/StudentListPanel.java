package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.model.ClassStudentDTO;
import com.mycompany.myapp.service.LearningService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class StudentListPanel extends JPanel {

    private JComboBox<ClassItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private LearningService learningService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public StudentListPanel() {
        learningService = new LearningService();
        initUI();
        loadTeacherClasses(); // Tự động load danh sách lớp khi mở panel
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIKit.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ================= TOP PANEL: BỘ LỌC =================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterPanel.setOpaque(false);

        JLabel lblSelectClass = new JLabel("Chọn lớp học:");
        lblSelectClass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectClass.setForeground(UIKit.TEXT_DARK);

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(300, 40));
        cbxClasses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbxClasses.setBackground(Color.WHITE);
        
        // Bắt sự kiện khi giáo viên chọn một lớp khác
        cbxClasses.addActionListener((ActionEvent e) -> handleClassSelection());

        filterPanel.add(lblSelectClass);
        filterPanel.add(cbxClasses);

        add(filterPanel, BorderLayout.NORTH);

        // ================= CENTER PANEL: BẢNG DỮ LIỆU =================
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã HV", "Họ và Tên", "Ngày Sinh", "Giới Tính", "SĐT HV", "Phụ Huynh", "SĐT Phụ Huynh", "Trạng Thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable();
        table.setModel(model);
        
        // Bật lưới hiển thị chuẩn Excel
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(224, 224, 224)); 
        
        // Set độ rộng tương đối cho các cột
        table.getColumnModel().getColumn(0).setPreferredWidth(60); // Mã HV
        table.getColumnModel().getColumn(1).setPreferredWidth(180); // Họ tên
        table.getColumnModel().getColumn(5).setPreferredWidth(180); // Phụ huynh

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    // Load danh sách lớp của giáo viên đưa vào ComboBox
    private void loadTeacherClasses() {
        Result<List<Map<String, Object>>> result = learningService.getMyTeachingClasses();
        if (result.isSuccess()) {
            cbxClasses.removeAllItems();
            List<Map<String, Object>> classes = result.getData();
            
            if (classes.isEmpty()) {
                cbxClasses.addItem(new ClassItem(-1, "-- Bạn chưa được phân công dạy lớp nào --"));
                return;
            }

            for (Map<String, Object> map : classes) {
                int id = (int) map.get("class_id");
                String name = (String) map.get("class_name");
                cbxClasses.addItem(new ClassItem(id, name));
            }
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Xử lý khi chọn lớp từ ComboBox
    private void handleClassSelection() {
        ClassItem selectedClass = (ClassItem) cbxClasses.getSelectedItem();
        if (selectedClass == null || selectedClass.getId() == -1) {
            model.setRowCount(0);
            return;
        }

        // Dùng SwingWorker để tránh đơ UI khi gọi DB
        SwingWorker<Result<List<ClassStudentDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<List<ClassStudentDTO>> doInBackground() {
                return learningService.getStudentsByClass(selectedClass.getId());
            }

            @Override
            protected void done() {
                try {
                    Result<List<ClassStudentDTO>> result = get();
                    model.setRowCount(0); // Xóa data cũ

                    if (result.isSuccess()) {
                        List<ClassStudentDTO> students = result.getData();
                        for (ClassStudentDTO s : students) {
                            String genderStr = "M".equals(s.getGender()) ? "Nam" : ("F".equals(s.getGender()) ? "Nữ" : "Khác");
                            String statusStr = mapStatus(s.getStatus());
                            
                            model.addRow(new Object[]{
                                "HV" + String.format("%04d", s.getStudentId()),
                                s.getFullName(),
                                s.getDob() != null ? sdf.format(s.getDob()) : "",
                                genderStr,
                                s.getPhone() != null ? s.getPhone() : "-",
                                s.getParentName(),
                                s.getParentPhone(),
                                statusStr
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(StudentListPanel.this, result.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Hàm phụ trợ map trạng thái sang tiếng Việt
    private String mapStatus(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "ACTIVE": return "Đang học";
            case "RESERVED": return "Bảo lưu";
            case "DROPPED": return "Đã nghỉ";
            default: return status;
        }
    }

    // Wrapper class để chứa cả ID và Name trong ComboBox
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
            return name; // Hiển thị tên lớp lên UI
        }
    }
}