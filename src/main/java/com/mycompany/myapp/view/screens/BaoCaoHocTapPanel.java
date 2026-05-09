package com.mycompany.myapp.view.screens;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class BaoCaoHocTapPanel extends JPanel {

    private JTable tblResults;
    private DefaultTableModel tableModel;

    public BaoCaoHocTapPanel() {
        initComponents();
        loadMockData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // ==========================================
        // 1. TOP FILTERS (Bộ lọc)
        // ==========================================
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlFilters.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("BÁO CÁO HỌC TẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBorder(new EmptyBorder(0, 0, 0, 30));
        pnlFilters.add(lblTitle);

        pnlFilters.add(new JLabel("Lớp học:"));
        JComboBox<String> cbxClass = new JComboBox<>(new String[]{"Tất cả", "Lớp SE101", "Lớp SE102"});
        pnlFilters.add(cbxClass);

        pnlFilters.add(new JLabel("Môn học:"));
        JComboBox<String> cbxSubject = new JComboBox<>(new String[]{"Tất cả", "Java App", "Oracle SQL"});
        pnlFilters.add(cbxSubject);

        JButton btnFilter = new JButton("Lọc Dữ Liệu");
        btnFilter.setBackground(new Color(59, 130, 246));
        btnFilter.setForeground(Color.WHITE);
        pnlFilters.add(btnFilter);

        // ==========================================
        // 2. INSIGHT CARDS (Tổng quan)
        // ==========================================
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setBackground(new Color(248, 250, 252));
        pnlCards.add(createStatCard("Tổng Sĩ Số", "85 Học viên", new Color(100, 116, 139)));
        pnlCards.add(createStatCard("Tỷ lệ Đạt (>= 5.0)", "92%", new Color(34, 197, 94)));
        pnlCards.add(createStatCard("Điểm Trung Bình", "7.8 Điểm", new Color(245, 158, 11)));

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 20));
        pnlNorth.setBackground(new Color(248, 250, 252));
        pnlNorth.add(pnlFilters, BorderLayout.NORTH);
        pnlNorth.add(pnlCards, BorderLayout.CENTER);

        // ==========================================
        // 3. BẢNG CHI TIẾT & XUẤT FILE
        // ==========================================
        JPanel pnlTableContainer = new JPanel(new BorderLayout(0, 10));
        pnlTableContainer.setBackground(new Color(248, 250, 252));

        // Header của Bảng
        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(new Color(248, 250, 252));
        JLabel lblTableTitle = new JLabel("Bảng Điểm Chi Tiết");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton btnExport = new JButton("Xuất Excel (.xlsx)");
        btnExport.setBackground(new Color(16, 185, 129)); // Xanh lá đậm chuẩn Excel
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        
        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);
        pnlTableHeader.add(btnExport, BorderLayout.EAST);

        // Table
        String[] columns = {"STT", "Mã HV", "Họ Tên", "Lớp", "Môn Học", "Điểm TK", "Xếp Loại"};
        tableModel = new DefaultTableModel(columns, 0);
        tblResults = new JTable(tableModel);
        tblResults.setRowHeight(40);
        
        // Cấu hình Line Bảng
        tblResults.setShowVerticalLines(false);
        tblResults.setShowHorizontalLines(true);
        tblResults.setGridColor(new Color(226, 232, 240));

        JScrollPane scrollPane = new JScrollPane(tblResults);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        pnlTableContainer.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);
        add(pnlTableContainer, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String value, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(100, 116, 139));
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private void loadMockData() {
        tableModel.addRow(new Object[]{1, "HV001", "Nguyễn Thế Anh", "SE101", "Java App", "9.5", "Giỏi"});
        tableModel.addRow(new Object[]{2, "HV002", "Lê Văn B", "SE101", "Java App", "7.0", "Khá"});
        tableModel.addRow(new Object[]{3, "HV003", "Trần Thị C", "SE102", "Oracle SQL", "4.5", "Yếu"});
    }

    // HÀM MAIN ĐỂ CHẠY THỬ
    public static void main(String[] args) {
        JFrame testFrame = new JFrame("Gói 6 - Báo Cáo Học Tập");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(1100, 700);
        testFrame.setLocationRelativeTo(null);
        testFrame.add(new BaoCaoHocTapPanel());
        testFrame.setVisible(true);
    }
}