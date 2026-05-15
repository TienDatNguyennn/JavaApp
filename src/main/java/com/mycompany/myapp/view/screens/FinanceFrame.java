package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.view.screens.finance.FinanceMainPanel;
import javax.swing.*;
import java.awt.*;

// Đổi tên từ MainFrame thành FinanceFrame
public class FinanceFrame extends JFrame {

    public FinanceFrame() {
        setTitle("Hệ Thống Quản Lý Tài Chính"); // Cập nhật tiêu đề phù hợp
        setSize(1200, 750); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
    }
    
    private void initUI() {
        // Thiết lập Layout chính là BorderLayout
        this.setLayout(new BorderLayout());

        // Khởi tạo Panel quản lý tài chính
        FinanceMainPanel financePanel = new FinanceMainPanel();

        /* 
           Vì bạn chỉ giữ lại một chức năng duy nhất, 
           không cần dùng JTabbedPane nữa. 
           Thêm trực tiếp financePanel vào vùng giữa của Frame.
        */
        this.add(financePanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try { 
            // Giữ giao diện hệ thống cho đẹp
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } 
        catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo lớp mới đã đổi tên
            new FinanceFrame().setVisible(true);
        });
    }
}