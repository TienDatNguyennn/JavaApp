package com.mycompany.myapp.view.screens;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class QuanLySubjectPanel extends JPanel {

    private JTable tblSubjects;
    private DefaultTableModel tableModel;

    public QuanLySubjectPanel() {
        initComponents();
        loadMockData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252)); 
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // ==========================================
        // 1. TOP CARDS (Thống kê - Đã Việt hóa)
        // ==========================================
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setBackground(new Color(248, 250, 252));
        pnlCards.add(createStatCard("Đang giảng dạy", "24", new Color(59, 130, 246)));
        pnlCards.add(createStatCard("Sắp mở", "12", new Color(99, 102, 241)));
        pnlCards.add(createStatCard("Chờ duyệt", "8", new Color(14, 165, 233)));
        pnlCards.add(createStatCard("Tổng môn học", "44", new Color(100, 116, 139)));

        // ==========================================
        // 2. MIDDLE TOOLBAR (Công cụ & Nút bấm)
        // ==========================================
        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setBackground(new Color(248, 250, 252));
        pnlToolbar.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lblTitle = new JLabel("Danh sách Môn học");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlToolbar.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setBackground(new Color(248, 250, 252));
        
        JTextField txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setText(" Tìm kiếm...");
        
        // Thêm các nút chức năng
        JButton btnAdd = createStyledButton("+ Thêm mới", new Color(34, 197, 94)); // Xanh lá
        JButton btnEdit = createStyledButton("Sửa", new Color(245, 158, 11));      // Cam
        JButton btnDelete = createStyledButton("Xóa", new Color(239, 68, 68));     // Đỏ

        // Xử lý luồng: Nhấn dòng -> Bấm sửa -> Hiện Pop-up
        btnEdit.addActionListener(e -> {
            int selectedRow = tblSubjects.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng click chọn một môn học trong bảng để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            } else {
                String subjectId = tableModel.getValueAt(selectedRow, 1).toString();
                String subjectName = tableModel.getValueAt(selectedRow, 2).toString();
                // Nơi này sau này bạn sẽ gọi JDialog (Pop-up Form) lên
                JOptionPane.showMessageDialog(this, "Đang mở form chỉnh sửa cho môn:\n[" + subjectId + "] " + subjectName, "Sửa thông tin", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        pnlActions.add(txtSearch);
        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 20));
        pnlNorth.setBackground(new Color(248, 250, 252));
        pnlNorth.add(pnlCards, BorderLayout.NORTH);
        pnlNorth.add(pnlToolbar, BorderLayout.CENTER);

        // ==========================================
        // 3. BẢNG DỮ LIỆU (Chỉ giữ line ngang)
        // ==========================================
        String[] columns = {"STT", "Mã Môn", "Tên Môn Học", "Số Buổi", "Học Phí", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0);
        tblSubjects = new JTable(tableModel);
        tblSubjects.setRowHeight(40);
        
        // Thiết lập UX cho bảng theo yêu cầu: Bỏ kẻ dọc, giữ kẻ ngang
        tblSubjects.setShowVerticalLines(false);
        tblSubjects.setShowHorizontalLines(true);
        tblSubjects.setGridColor(new Color(226, 232, 240));
        
        JScrollPane scrollPane = new JScrollPane(tblSubjects);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(pnlNorth, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }

    private void loadMockData() {
        tableModel.addRow(new Object[]{1, "SUB01", "Cấu trúc dữ liệu", "45", "2,500,000 đ", "Đang mở"});
        tableModel.addRow(new Object[]{2, "SUB02", "Lập trình Java App", "60", "3,200,000 đ", "Sắp mở"});
        tableModel.addRow(new Object[]{3, "SUB03", "Cơ sở dữ liệu Oracle", "45", "Miễn phí", "Đang mở"});
    }

    // HÀM MAIN ĐỂ CHẠY THỬ
    public static void main(String[] args) {
        JFrame testFrame = new JFrame("Gói 2 - Quản Lý Môn Học");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(1100, 700);
        testFrame.setLocationRelativeTo(null);
        testFrame.add(new QuanLySubjectPanel());
        testFrame.setVisible(true);
    }
}