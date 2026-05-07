package com.mycompany.myapp.view.screens;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BaoCaoHocTapPanel extends JPanel {

    private JComboBox<String> cbxClass, cbxSubject;
    private JTable tblResults;
    private DefaultTableModel tableModel;

    public BaoCaoHocTapPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- HEADER & FILTERS ---
        JPanel pnlTop = new JPanel(new GridLayout(2, 1, 0, 15));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("BÁO CÁO TỔNG HỢP KẾT QUẢ HỌC TẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pnlTop.add(lblTitle);

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlFilters.setBackground(Color.WHITE);

        pnlFilters.add(new JLabel("Lớp học:"));
        cbxClass = new JComboBox<>(new String[]{"Tất cả", "L01", "L02"});
        pnlFilters.add(cbxClass);

        pnlFilters.add(new JLabel("Môn học:"));
        cbxSubject = new JComboBox<>(new String[]{"Tất cả", "Java App", "Database"});
        pnlFilters.add(cbxSubject);

        JButton btnExport = new JButton("Xuất File Excel");
        btnExport.setBackground(new Color(34, 197, 94));
        btnExport.setForeground(Color.WHITE);
        pnlFilters.add(btnExport);

        pnlTop.add(pnlFilters);

        // --- TABLE (Khớp Lược đồ COURSE_RESULT) ---
        String[] columns = {"STT", "Mã Học Viên", "Họ Tên", "Lớp", "Môn Học", "Điểm Tổng Kết", "Xếp Loại"};
        tableModel = new DefaultTableModel(columns, 0);
        tblResults = new JTable(tableModel);
        tblResults.setRowHeight(35);

        // Mock data
        tableModel.addRow(new Object[]{1, "HV101", "Nguyễn Thế Anh", "L01", "Java App", 9.5, "Giỏi"});

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(tblResults), BorderLayout.CENTER);
    }
}