package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import com.mycompany.myapp.service.AttendanceAnalyticsService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsPanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private AttendanceAnalyticsService service;

    public AttendanceAnalyticsPanel() {
        service = new AttendanceAnalyticsService();
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
        
        JLabel lblTitle = new JLabel("Theo Dõi Chuyên Cần");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIKit.TEXT_DARK);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(250, 38));

        JButton btnLoad = new JButton("Xem Thống Kê");
        btnLoad.setPreferredSize(new Dimension(150, 38));
        btnLoad.setBackground(new Color(99, 102, 241)); 
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setOpaque(true);
        btnLoad.setBorderPainted(false);
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLoad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(e -> loadAnalyticsData());

        JLabel lblFilter = new JLabel("Chọn lớp học:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(new Color(70, 70, 80));

        filterBar.add(lblFilter);
        filterBar.add(cbxClasses);
        filterBar.add(btnLoad);

        topPanel.add(filterBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER: BẢNG DỮ LIỆU =================
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã HV", "Họ và Tên", "Tổng Buổi Đã Dạy", "Có Mặt", "Vắng Mặt", "Tỷ Lệ (%)"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; } // Read-only
        };

        table = new ModernTable();
        table.setModel(model);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 240));
        table.setRowHeight(35);

        // Canh giữa cho các cột số liệu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // KỸ THUẬT UX: Tô màu cột Tỷ lệ chuyên cần (<80% là Báo động Đỏ, >=80 là Xanh)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                
                try {
                    String valStr = value.toString().replace("%", "").trim();
                    double pct = Double.parseDouble(valStr);
                    if (pct < 80.0) {
                        c.setForeground(new Color(239, 68, 68)); // Đỏ cảnh báo
                    } else {
                        c.setForeground(new Color(34, 197, 94)); // Xanh lá an toàn
                    }
                } catch(Exception ex) {
                    c.setForeground(Color.BLACK);
                }
                
                // Nếu dòng đang được select thì đổi màu chữ thành trắng cho dễ nhìn
                if(isSelected) c.setForeground(Color.WHITE);
                
                return c;
            }
        });

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void loadClasses() {
        Result<List<Map<String, Object>>> res = service.getTeacherClasses();
        if (res.isSuccess() && !res.getData().isEmpty()) {
            for (Map<String, Object> map : res.getData()) {
                cbxClasses.addItem(new ComboItem((int) map.get("class_id"), (String) map.get("class_name")));
            }
        } else {
            cbxClasses.addItem(new ComboItem(-1, "Không có lớp"));
        }
    }

    private void loadAnalyticsData() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();
        if (cls == null || cls.getId() == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp học hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
                        if(data.isEmpty()) {
                            JOptionPane.showMessageDialog(AttendanceAnalyticsPanel.this, "Lớp này chưa có dữ liệu học viên.");
                            return;
                        }
                        
                        for (AttendanceAnalyticsDTO dto : data) {
                            model.addRow(new Object[]{
                                "HV" + String.format("%04d", dto.getStudentId()),
                                dto.getFullName(),
                                dto.getTotalSessions(),
                                dto.getPresentCount(),
                                dto.getAbsentCount(),
                                String.format("%.1f %%", dto.getAttendanceRate()) // Format 1 chữ số thập phân kèm dấu %
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(AttendanceAnalyticsPanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private class ComboItem {
        private int id; private String name;
        public ComboItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }
}