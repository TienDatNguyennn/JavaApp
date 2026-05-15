package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.model.AcademicReportDTO;
import com.mycompany.myapp.service.ReportService;
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

public class AcademicResultPanel extends JPanel {

    private JComboBox<ComboItem> cbxClasses;
    private ModernTable table;
    private DefaultTableModel model;
    private ReportService service;
    
    // Các thẻ thống kê
    private JLabel lblTotal, lblPassed, lblFailed, lblNoScore;

    public AcademicResultPanel() {
        service = new ReportService();
        initUI();
        loadClasses();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 25));
        setBackground(new Color(245, 245, 249));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ================= HEADER & BỘ LỌC =================
        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setOpaque(false);
        
        // 1. Tiêu đề và nút xem
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterBar.setOpaque(false);
        JLabel lblTitle = new JLabel("Báo Cáo Kết Quả Học Tập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UIKit.TEXT_DARK);
        
        cbxClasses = new JComboBox<>();
        cbxClasses.setPreferredSize(new Dimension(250, 38));
        
        JButton btnLoad = new JButton("Trích Xuất Báo Cáo");
        btnLoad.setPreferredSize(new Dimension(160, 38));
        btnLoad.setBackground(new Color(99, 102, 241)); // Tím Indigo
        btnLoad.setForeground(Color.WHITE);
        btnLoad.setFocusPainted(false);
        btnLoad.setOpaque(true);
        btnLoad.setBorderPainted(false);
        btnLoad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLoad.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(e -> generateReport());

        filterBar.add(lblTitle);
        filterBar.add(Box.createHorizontalStrut(30));
        filterBar.add(new JLabel("Lớp:"));
        filterBar.add(cbxClasses);
        filterBar.add(btnLoad);
        
        topPanel.add(filterBar, BorderLayout.NORTH);

        // 2. Thẻ Thống Kê (Summary Cards)
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 100));

        lblTotal = new JLabel("0", SwingConstants.CENTER);
        lblPassed = new JLabel("0", SwingConstants.CENTER);
        lblFailed = new JLabel("0", SwingConstants.CENTER);
        lblNoScore = new JLabel("0", SwingConstants.CENTER);

        cardsPanel.add(createSummaryCard("Tổng Học Viên", lblTotal, new Color(59, 130, 246))); // Xanh dương
        cardsPanel.add(createSummaryCard("Số Lượng Đạt (>=5.0)", lblPassed, new Color(34, 197, 94))); // Xanh lá
        cardsPanel.add(createSummaryCard("Không Đạt (<5.0)", lblFailed, new Color(239, 68, 68))); // Đỏ
        cardsPanel.add(createSummaryCard("Chưa Nhập Điểm", lblNoScore, new Color(245, 158, 11))); // Cam

        topPanel.add(cardsPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ================= CENTER: BẢNG CHI TIẾT =================
        RoundedPanel tableContainer = new RoundedPanel(15);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Mã HV", "Họ và Tên", "Điểm Khóa Học", "Xếp Loại (Rank)", "Trạng Thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable();
        table.setModel(model);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 240));
        table.setRowHeight(35);

        // Đổ màu cột Trạng Thái (Đạt -> Xanh, Rớt -> Đỏ, Chưa điểm -> Xám)
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean focus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isSel, focus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!isSel) {
                    if ("ĐẠT".equals(v)) comp.setForeground(new Color(34, 197, 94));
                    else if ("KHÔNG ĐẠT".equals(v)) comp.setForeground(new Color(239, 68, 68));
                    else comp.setForeground(Color.GRAY);
                }
                return comp;
            }
        });

        tableContainer.add(new ModernScrollPane(table), BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    // Helper tạo thẻ thống kê bo góc
    private JPanel createSummaryCard(String title, JLabel valueLabel, Color accentColor) {
        RoundedPanel card = new RoundedPanel(15);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 4, 0, accentColor), 
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(107, 114, 128));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(new Color(31, 41, 55));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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

    private void generateReport() {
        ComboItem cls = (ComboItem) cbxClasses.getSelectedItem();
        if (cls == null || cls.getId() == -1) return;

        SwingWorker<Result<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected Result<Map<String, Object>> doInBackground() {
                return service.generateClassReport(cls.getId());
            }

            @Override
            protected void done() {
                try {
                    Result<Map<String, Object>> res = get();
                    model.setRowCount(0);

                    if (res.isSuccess()) {
                        Map<String, Object> data = res.getData();
                        
                        // Cập nhật 4 thẻ thống kê
                        lblTotal.setText(data.get("total").toString());
                        lblPassed.setText(data.get("passed").toString());
                        lblFailed.setText(data.get("failed").toString());
                        lblNoScore.setText(data.get("noScore").toString());

                        // Cập nhật bảng
                        List<AcademicReportDTO> details = (List<AcademicReportDTO>) data.get("details");
                        for (AcademicReportDTO dto : details) {
                            model.addRow(new Object[]{
                                "HV" + String.format("%04d", dto.getStudentId()),
                                dto.getFullName(),
                                dto.getFinalScore() != null ? dto.getFinalScore() : "---",
                                dto.getRank() != null ? dto.getRank() : "---",
                                dto.getStatus()
                            });
                        }
                    } else {
                        JOptionPane.showMessageDialog(AcademicResultPanel.this, res.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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