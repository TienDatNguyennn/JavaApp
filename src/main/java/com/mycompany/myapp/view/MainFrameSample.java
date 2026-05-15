package com.mycompany.myapp.view;

import com.mycompany.myapp.view.screens.ManageSubjectPanel; 
import com.mycompany.myapp.view.screens.StudyReportPanel;  
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MainFrameSample extends JFrame {

    private JPanel pnlSideNav;    
    private JPanel pnlContent;    
    private CardLayout cardLayout; 
    
    // Thêm danh sách để quản lý màu sắc của các nút trên Menu
    private List<JButton> menuButtons = new ArrayList<>();

    public MainFrameSample(String userRole, String userName) {
        initComponents(userRole, userName);
    }

    private void initComponents(String userRole, String userName) {
        setTitle("Hệ thống Quản lý Thông tin Sinh viên - SIS (UIT)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. THANH ĐIỀU HƯỚNG BÊN TRÁI (SIDEBAR)
        pnlSideNav = new JPanel();
        pnlSideNav.setBackground(new Color(31, 41, 55)); // Màu xám đậm chuyên nghiệp
        pnlSideNav.setPreferredSize(new Dimension(250, 0));
        pnlSideNav.setLayout(new BoxLayout(pnlSideNav, BoxLayout.Y_AXIS));
        
        // Header của Sidebar
        JLabel lblUser = new JLabel("Xin chào, " + userName);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pnlSideNav.add(lblUser);
        
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(500, 1));
        separator.setBackground(new Color(55, 65, 81));
        pnlSideNav.add(separator);

        // 2. VÙNG NỘI DUNG CHÍNH (CONTENT AREA)
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(Color.WHITE);

        // 3. PHÂN QUYỀN VÀ NẠP CÁC PANEL
        if ("GIAO_VU".equals(userRole)) {
            setupGiaoVuFeatures();
        } else if ("GIAO_VIEN".equals(userRole)) {
            setupGiaoVienFeatures();
        }

        add(pnlSideNav, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
        
        // Tự động click vào nút đầu tiên khi mới mở app lên
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).doClick();
        }
    }

    private void setupGiaoVuFeatures() {
        addButtonToMenu("Quản lý Môn học (Gói 2)", "GOI_2");
        addButtonToMenu("Báo cáo Học tập (Gói 6)", "GOI_6");
        
        pnlSideNav.add(Box.createVerticalGlue()); // Đẩy nút Đăng xuất xuống đáy
        addButtonToMenu("Đăng xuất", "LOGOUT");

        pnlContent.add(new ManageSubjectPanel(), "GOI_2");
        pnlContent.add(new StudyReportPanel(), "GOI_6");
    }

    private void setupGiaoVienFeatures() {
        addButtonToMenu("Nhập điểm lớp học", "NHAP_DIEM");
        
        pnlSideNav.add(Box.createVerticalGlue());
        addButtonToMenu("Đăng xuất", "LOGOUT");
    }

    private void addButtonToMenu(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Trạng thái mặc định ban đầu
        btn.setBackground(new Color(31, 41, 55));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        // Lưu nút vào danh sách (trừ nút Logout)
        if (!"LOGOUT".equals(cardName)) {
            menuButtons.add(btn);
        }

        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                this.dispose(); 
            } else {
                // 1. Reset màu toàn bộ các nút về tối
                for (JButton b : menuButtons) {
                    b.setBackground(new Color(31, 41, 55));
                    b.setForeground(Color.WHITE);
                }
                // 2. Chuyển riêng nút đang được click sang màu Sáng (Nền trắng, CHỮ ĐEN)
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK); // ĐÂY CHÍNH LÀ DÒNG SỬA LỖI CHỮ TÀNG HÌNH
                
                // Hiển thị màn hình tương ứng
                cardLayout.show(pnlContent, cardName);
            }
        });
        
        pnlSideNav.add(btn);
    }
    
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {}

        java.awt.EventQueue.invokeLater(() -> {
            MainFrameSample frame = new MainFrameSample("GIAO_VU", "Nguyễn Thế Anh");
            frame.setVisible(true);
        });
    }
}