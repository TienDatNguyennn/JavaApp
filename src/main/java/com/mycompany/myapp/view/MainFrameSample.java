package com.mycompany.myapp.view;

import com.mycompany.myapp.view.screens.QuanLySubjectPanel; // Panel Gói 2
import com.mycompany.myapp.view.screens.BaoCaoHocTapPanel;  // Panel Gói 6
import java.awt.*;
import javax.swing.*;

/**
 * MainFrame - Khung sườn chính của hệ thống.
 * Sử dụng CardLayout để chuyển đổi giữa các chức năng mà không cần mở nhiều cửa sổ.
 */
public class MainFrameSample extends JFrame {

    private JPanel pnlSideNav;    // Thanh điều hướng bên trái
    private JPanel pnlContent;    // Vùng lõi hiển thị nội dung
    private CardLayout cardLayout; // Bộ điều khiển chuyển màn hình

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
        
        // Header của Sidebar (Hiện tên User)
        JLabel lblUser = new JLabel("Xin chào, " + userName);
        lblUser.setForeground(Color.WHITE);
        lblUser.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pnlSideNav.add(lblUser);
        pnlSideNav.add(new JSeparator());

        // 2. VÙNG NỘI DUNG CHÍNH (CONTENT AREA)
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(Color.WHITE);

        // 3. PHÂN QUYỀN VÀ NẠP CÁC PANEL (Dựa trên Role từ Gói 1)
        if ("GIAO_VU".equals(userRole)) {
            setupGiaoVuFeatures();
        } else if ("GIAO_VIEN".equals(userRole)) {
            setupGiaoVienFeatures();
        }

        add(pnlSideNav, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
    }

    private void setupGiaoVuFeatures() {
        // Tạo các nút menu cho Giáo vụ
        addButtonToMenu("Quản lý Môn học (Gói 2)", "GOI_2");
        addButtonToMenu("Báo cáo Học tập (Gói 6)", "GOI_6");
        addButtonToMenu("Đăng xuất", "LOGOUT");

        // Nạp các Panel chức năng vào CardLayout
        pnlContent.add(new QuanLySubjectPanel(), "GOI_2");
        pnlContent.add(new BaoCaoHocTapPanel(), "GOI_6");
    }

    private void setupGiaoVienFeatures() {
        addButtonToMenu("Nhập điểm lớp học", "NHAP_DIEM");
        addButtonToMenu("Đăng xuất", "LOGOUT");
        
        // Giả sử có Panel nhập điểm ở đây
        // pnlContent.add(new NhapDiemPanel(), "NHAP_DIEM");
    }

    /**
     * Hàm hỗ trợ tạo nút bấm trên thanh Menu nhanh chóng
     */
    private void addButtonToMenu(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(250, 50));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(31, 41, 55));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        btn.addActionListener(e -> {
            if ("LOGOUT".equals(cardName)) {
                this.dispose(); // Đóng MainFrame
                // Gọi lại màn hình Login của Gói 1 tại đây
            } else {
                cardLayout.show(pnlContent, cardName);
            }
        });
        
        pnlSideNav.add(btn);
    }
    
    // =========================================
    // HÀM MAIN ĐỂ CHẠY THỬ GIAO DIỆN CHUẨN SWING
    // =========================================
    public static void main(String args[]) {
        // Thiết lập giao diện nhìn cho giống Windows/Mac thay vì giao diện cổ điển của Java
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Chạy Frame an toàn trong luồng sự kiện của Swing
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Giả lập tình huống: Gói 1 đã login thành công tài khoản của bạn với quyền Giáo Vụ
                MainFrameSample frame = new MainFrameSample("GIAO_VU", "Nguyễn Thế Anh");
                frame.setVisible(true);
            }
        });
    }
}