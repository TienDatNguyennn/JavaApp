package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.CourseResultDTO;
import com.mycompany.myapp.service.GradeService;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.UIKit.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class GradeEntryPanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private GradeService service;
    
    private List<CourseResultDTO> currentGrades;

    public GradeEntryPanel() {
        service = new GradeService();
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
        
        JLabel lblTitle = new JLabel("Nhập Điểm Tổng Kết");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIKit.TEXT_DARK);
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 235)));

        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(250, 38));

        JButton btnLoad = new JButton("Tải Danh Sách");
        btnLoad.setPreferredSize(new Dimension(140, 38));
        btnLoad.setBackground(new Color(99, 102, 241));
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setOpaque(true);
        btnLoad.setBorderPainted(false);
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLoad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(e -> loadGradeData());

        JLabel lblFilter = new JLabel("Chọn lớp học:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(new Color(70, 70, 80));

        filterBar.add(lblFilter);
        filterBar.add(cbxClasses);
        filterBar.add(btnLoad);

        topPanel.add(filterBar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER: BẢNG NHẬP ĐIỂM =================
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã HV", "Họ và Tên", "Điểm Tổng Kết (0-10)", "Xếp Loại (Tự động)"};
        
        // CẤU HÌNH TABLE THÔNG MINH
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2; // CHỈ cho phép gõ vào cột Điểm (Cột 2)
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 2) {
                    try {
                        String valStr = aValue.toString().trim().replace(",", ".");
                        if (valStr.isEmpty()) {
                            super.setValueAt("", row, 2);
                            super.setValueAt("", row, 3); // Xóa xếp loại nếu xóa điểm
                            return;
                        }
                        
                        double score = Double.parseDouble(valStr);
                        // Validate khoảng điểm
                        if (score < 0.0 || score > 10.0) {
                            JOptionPane.showMessageDialog(null, "Điểm số phải từ 0 đến 10!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                            return; // Từ chối cập nhật
                        }
                        
                        // Cập nhật điểm hợp lệ
                        super.setValueAt(score, row, 2);
                        
                        // Tự động nhảy xếp loại
                        String rank = GradeService.calculateRank(score);
                        super.setValueAt(rank, row, 3);
                        
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Vui lòng chỉ nhập số!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        table = new ModernTable();
        table.setModel(model);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 240));
        table.setRowHeight(40);
        
        // Focus màu khác cho cột Điểm để báo hiệu đây là ô được phép gõ
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    c.setBackground(new Color(250, 250, 255)); // Tô nền xanh nhạt cho cột nhập
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    setForeground(new Color(17, 24, 39));
                }
                return c;
            }
        });
        
        // Căn giữa cột Xếp Loại
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // ================= BOTTOM: NÚT LƯU =================
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        
        JButton btnSave = new JButton("Lưu Bảng Điểm");
        btnSave.setPreferredSize(new Dimension(180, 45));
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener((ActionEvent e) -> saveGradeData());
        
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadClasses() {
        Result<List<Map<String, Object>>> res = service.getTeacherClasses();
        if (res.isSuccess() && !res.getData().isEmpty()) {
            cbxClasses.removeAllItems();
            for (Map<String, Object> map : res.getData()) {
                cbxClasses.addItem(new ComboItem((int) map.get("class_id"), (String) map.get("class_name")));
            }
        }
    }

    private void loadGradeData() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();
        if (cls == null || cls.getId() == -1) return;

        Result<List<CourseResultDTO>> res = service.getStudentGrades(cls.getId());
        if (res.isSuccess()) {
            currentGrades = res.getData();
            model.setRowCount(0);
            
            for (CourseResultDTO dto : currentGrades) {
                model.addRow(new Object[]{
                    "HV" + String.format("%04d", dto.getStudentId()),
                    dto.getFullName(),
                    dto.getFinalScore() != null ? dto.getFinalScore() : "",
                    dto.getRank() != null ? dto.getRank() : ""
                });
            }
        }
    }

    private void saveGradeData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing(); // Phải dừng con trỏ nhấp nháy trước khi lưu
        if (currentGrades == null || currentGrades.isEmpty()) return;

        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();
        if (cls == null) return;

        // Cập nhật DTO từ dữ liệu trên bảng
        for (int i = 0; i < table.getRowCount(); i++) {
            String scoreStr = table.getValueAt(i, 2).toString();
            CourseResultDTO dto = currentGrades.get(i);
            
            if (scoreStr.isEmpty()) {
                dto.setFinalScore(null);
                dto.setRank(null);
            } else {
                dto.setFinalScore(Double.parseDouble(scoreStr));
            }
        }

        Result<Void> res = service.saveGrades(cls.getId(), currentGrades);
        if (res.isSuccess()) {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadGradeData(); // Tải lại để gán result_id cho các học sinh mới nhập điểm lần đầu
        } else {
            JOptionPane.showMessageDialog(this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ComboItem {
        private int id; private String name;
        public ComboItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }
}