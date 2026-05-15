package com.mycompany.myapp.view.screens.system;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class SystemConfigUI extends JPanel {

    public SystemConfigUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        // Tùy chỉnh TabbedPane hiện đại
        JTabbedPane configTabbedPane = new JTabbedPane();
        configTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        configTabbedPane.setBackground(Color.WHITE);
        configTabbedPane.setForeground(new Color(45, 52, 54));
        configTabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Thêm các tab chức năng cấu hình
        configTabbedPane.addTab("Quản Lý Phòng Học", createRoomPanel());
        configTabbedPane.addTab("Cấu Hình Khuyến Mãi", createPromotionPanel());
        configTabbedPane.addTab("Sao Lưu & Phục Hồi", createBackupPanel());

        add(configTabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: QUẢN LÝ PHÒNG HỌC
    // ==========================================
    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 30, 30));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Danh Mục Cơ Sở Vật Chất (Phòng Học)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(45, 52, 54));

        // --- ACTION PANEL (Search + Buttons) ---
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        
        // Search Bar bên trái
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(250, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(0, 10, 0, 10)
        ));
        
        // Nút tìm kiếm bo góc
        RoundedButton btnSearch = new RoundedButton("🔍 Tìm", "#F1F2F6", "#2D3436", 12);
        searchPanel.add(txtSearch);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(btnSearch);

        // Nút thao tác bo góc bên phải
        JPanel btnGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroupPanel.setOpaque(false);
        
        RoundedButton btnAdd = new RoundedButton("Thêm Phòng", "#3498DB", "#FFFFFF", 12);
        RoundedButton btnEdit = new RoundedButton("Sửa", "#F39C12", "#FFFFFF", 12);
        RoundedButton btnDelete = new RoundedButton("Xóa", "#E74C3C", "#FFFFFF", 12);

        btnGroupPanel.add(btnAdd);
        btnGroupPanel.add(btnEdit);
        btnGroupPanel.add(btnDelete);

        actionPanel.add(searchPanel, BorderLayout.WEST);
        actionPanel.add(btnGroupPanel, BorderLayout.EAST);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(actionPanel, BorderLayout.CENTER);

        // --- BẢNG DỮ LIỆU ---
        String[] cols = {"ID Phòng", "Tên Phòng", "Sức Chứa", "Tầng", "Loại Phòng", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        model.addRow(new Object[]{"1", "Phòng Lab 01", "20", "1", "Thực hành", "Sẵn sàng"});
        model.addRow(new Object[]{"2", "Phòng Lý thuyết A2", "40", "2", "Tiêu chuẩn", "Sẵn sàng"});
        model.addRow(new Object[]{"3", "Phòng Lab 02", "25", "2", "Thực hành", "Đang bảo trì"});
        
        JTable table = new JTable(model);
        styleModernTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 2: QUẢN LÝ QUY ĐỊNH KHUYẾN MÃI
    // ==========================================
    private JPanel createPromotionPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Cấu Hình Các Chương Trình Khuyến Mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(45, 52, 54));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        
        RoundedButton btnAdd = new RoundedButton("Tạo CT Khuyến Mãi", "#2ECC71", "#FFFFFF", 12);
        RoundedButton btnEdit = new RoundedButton("Chỉnh sửa", "#F39C12", "#FFFFFF", 12);
        RoundedButton btnDelete = new RoundedButton("Xóa/Vô hiệu", "#E74C3C", "#FFFFFF", 12);

        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(actionPanel, BorderLayout.CENTER);

        String[] cols = {"Mã KM", "Tên Chương Trình", "Tỷ lệ giảm (%)", "Số môn đăng ký tối thiểu"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        model.addRow(new Object[]{"1", "Chào hè 2026", "10%", "1"});
        model.addRow(new Object[]{"2", "Đăng ký nhóm", "15%", "2"});
        model.addRow(new Object[]{"3", "Học sinh giỏi", "20%", "1"});

        JTable table = new JTable(model);
        styleModernTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 3: SAO LƯU & PHỤC HỒI DỮ LIỆU
    // ==========================================
    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#F8F9F9"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(50, 60, 50, 60)
        ));

        JLabel iconLabel = new JLabel("🗄️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 65));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("An Toàn Dữ Liệu Hệ Thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(45, 52, 54));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><div style='text-align:center;'>Dữ liệu là tài sản quan trọng nhất của trung tâm.<br>Vui lòng thực hiện sao lưu (Backup) thường xuyên để tránh rủi ro mất mát.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDesc.setForeground(Color.GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nút bo góc tròn nhiều hơn (radius 20) để tạo điểm nhấn
        RoundedButton btnBackup = new RoundedButton("Tạo Bản Sao Lưu (.SQL)", "#8B5CF6", "#FFFFFF", 20);
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBackup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackup.setPreferredSize(new Dimension(230, 45));

        // Nút Outline bo góc
        RoundedButton btnRestore = new RoundedButton("Phục Hồi Dữ Liệu", "#FFFFFF", "#E67E22", "#E67E22", 20);
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRestore.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRestore.setPreferredSize(new Dimension(230, 45));

        // Sự kiện mẫu
        btnBackup.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đang tiến hành xuất file SQL Backup...", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(lblDesc);
        card.add(Box.createRigidArea(new Dimension(0, 40)));
        card.add(btnBackup);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(btnRestore);

        panel.add(card);
        return panel;
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================

    // Hàm thiết kế Table hiển thị dạng lưới chuẩn Excel
    private void styleModernTable(JTable table) {
        table.setRowHeight(35); // Chiều cao dòng vừa phải, tối ưu không gian như Excel
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // BẬT lưới dọc và ngang
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(Color.decode("#D4D4D4")); // Màu xám viền ô chuẩn Excel
        
        // Hiệu ứng khi chọn dòng
        table.setSelectionBackground(Color.decode("#E8F0FE")); 
        table.setSelectionForeground(Color.BLACK);

        // Tùy chỉnh Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.decode("#F1F2F6"));
        header.setForeground(new Color(45, 52, 54));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        // Ép viền cho Header để đồng bộ với các ô bên dưới
        header.setBorder(BorderFactory.createLineBorder(Color.decode("#D4D4D4")));
        
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    // ==========================================
    // INNER CLASS: NÚT BẤM BO GÓC (CUSTOM BUTTON)
    // ==========================================
    class RoundedButton extends JButton {
        private Color bgColor;
        private Color fgColor;
        private Color borderColor;
        private int radius;

        // Constructor cho nút nền đặc
        public RoundedButton(String text, String bgHex, String fgHex, int radius) {
            this(text, bgHex, fgHex, null, radius);
        }

        // Constructor cho nút có viền (Outline Button)
        public RoundedButton(String text, String bgHex, String fgHex, String borderHex, int radius) {
            super(text);
            this.bgColor = Color.decode(bgHex);
            this.fgColor = Color.decode(fgHex);
            this.borderColor = borderHex != null ? Color.decode(borderHex) : null;
            this.radius = radius;
            
            setForeground(this.fgColor);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setContentAreaFilled(false); // Bắt buộc false để đồ họa tự vẽ bo góc
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));
            
            // Hiệu ứng Hover đổi màu nhẹ
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (borderColor == null) setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Hiệu ứng khi Click
            if (getModel().isPressed()) {
                g2.setColor(bgColor.darker());
            } else {
                g2.setColor(bgColor);
            }
            
            // Vẽ nền bo góc
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            
            // Vẽ viền bo góc nếu có (dùng cho Outline Button)
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }
}