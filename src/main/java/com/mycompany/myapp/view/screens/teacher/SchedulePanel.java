package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.TeacherScheduleDTO;
import com.mycompany.myapp.service.TeacherScheduleService;
import com.mycompany.myapp.utils.SessionStore;
import com.mycompany.myapp.view.components.UIKit.*;
import com.mycompany.myapp.view.components.UIKit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SchedulePanel extends JPanel {
    private ModernTable table;
    private DefaultTableModel model;
    private TeacherScheduleService scheduleService;

    public SchedulePanel() {
        scheduleService = new TeacherScheduleService();
        initUI();
        loadDataFromDatabase(); 
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIKit.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Lịch Dạy Hàng Tuần");
        lblTitle.setFont(UIKit.FONT_TITLE);
        lblTitle.setForeground(UIKit.TEXT_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        
        // Nút làm mới dữ liệu
        JButton btnReload = new JButton("Làm mới");
        btnReload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReload.addActionListener(e -> loadDataFromDatabase());
        header.add(btnReload, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);

        // Schedule Table
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Thứ", "Giờ Bắt Đầu", "Giờ Kết Thúc", "Môn Học", "Lớp Học", "Phòng Học"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        table = new ModernTable();
        table.setModel(model);
        
        // ==========================================
        // THÊM: BẬT Ô KẺ DỌC/NGANG GIỐNG EXCEL
        // ==========================================
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(224, 224, 224)); // Màu xám nhạt cực kỳ thanh lịch
        
        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void loadDataFromDatabase() {
        Integer teacherId = SessionStore.getUserId(); 

        if (teacherId == null || teacherId == -1) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin phiên đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<Map<Integer, List<TeacherScheduleDTO>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<Integer, List<TeacherScheduleDTO>> doInBackground() {
                return scheduleService.getGroupedSchedule(teacherId.longValue());
            }

            @Override
            protected void done() {
                try {
                    Map<Integer, List<TeacherScheduleDTO>> data = get();
                    model.setRowCount(0); // Xóa dữ liệu cũ

                    // Duyệt bắt buộc từ Thứ 2 (2) đến Chủ Nhật (8)
                    for (int i = 2; i <= 8; i++) {
                        List<TeacherScheduleDTO> daySchedules = data.get(i);
                        String dayText = (i == 8) ? "Chủ Nhật" : "Thứ " + i;

                        // TRƯỜNG HỢP 1: NGÀY TRỐNG (Không có lịch)
                        if (daySchedules == null || daySchedules.isEmpty()) {
                            model.addRow(new Object[]{
                                dayText, 
                                "-", 
                                "-", 
                                "<html><i style='color:#9CA3AF;'>--- Nghỉ / Không có lịch dạy ---</i></html>", 
                                "-", 
                                "-"
                            });
                        } 
                        // TRƯỜNG HỢP 2: CÓ LỊCH DẠY
                        else {
                            for (int j = 0; j < daySchedules.size(); j++) {
                                TeacherScheduleDTO dto = daySchedules.get(j);
                                
                                // Thủ thuật UX: Chỉ in tên "Thứ" ở dòng đầu tiên của ngày hôm đó
                                String displayDayText = (j == 0) ? dayText : "";

                                model.addRow(new Object[]{
                                    displayDayText,
                                    dto.getStartTime(),
                                    dto.getEndTime(),
                                    dto.getSubjectName(), 
                                    dto.getClassName(),
                                    dto.getRoomName()
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SchedulePanel.this, 
                        "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}