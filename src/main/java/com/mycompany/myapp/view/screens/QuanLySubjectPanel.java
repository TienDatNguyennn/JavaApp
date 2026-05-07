package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.model.SubjectDTO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLySubjectPanel extends JPanel {

    private JTable tblSubjects;
    private DefaultTableModel tableModel;

    public QuanLySubjectPanel() {
        initComponents();
        loadMockData(); // Chờ tầng Repo để load data thật
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ DANH MỤC MÔN HỌC (SUBJECT)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(31, 41, 55));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // --- TOOLBAR ---
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlToolbar.setBackground(Color.WHITE);

        JButton btnAdd = createStyledButton("Thêm Môn học", new Color(79, 70, 229));
        JButton btnUpdate = createStyledButton("Cập nhật", new Color(245, 158, 11));
        JButton btnDelete = createStyledButton("Xóa", new Color(239, 68, 68));

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnUpdate);
        pnlToolbar.add(btnDelete);
        pnlHeader.add(pnlToolbar, BorderLayout.SOUTH);

        // --- TABLE ---
        String[] columns = {"STT", "Mã Môn Học", "Tên Môn Học", "Số Buổi", "Học Phí"};
        tableModel = new DefaultTableModel(columns, 0);
        tblSubjects = new JTable(tableModel);
        tblSubjects.setRowHeight(35);
        tblSubjects.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        add(pnlHeader, BorderLayout.NORTH);
        add(new JScrollPane(tblSubjects), BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private void loadMockData() {
        // Giả lập dữ liệu từ DTO
        Object[][] data = {
            {1, "SUB01", "Cấu trúc dữ liệu", 45, "2,500,000 VNĐ"},
            {2, "SUB02", "Lập trình Java", 60, "3,200,000 VNĐ"}
        };
        for (Object[] row : data) tableModel.addRow(row);
    }
}