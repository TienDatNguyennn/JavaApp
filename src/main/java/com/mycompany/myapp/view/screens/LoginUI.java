package com.mycompany.myapp.view.screens;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.myapp.controller.LoginController;

public class LoginUI extends JFrame {
    private LoginController controller;

    public LoginUI() {
        controller = new LoginController();
        initUI();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Đào Tạo Trực Tuyến – EduFlex");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createLeftPanel(), BorderLayout.WEST);
        mainPanel.add(createRightPanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    // ==========================================
    // LEFT PANEL (Đồ họa S-Curve & 3D Illustration)
    // ==========================================
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                g2d.setColor(Color.decode("#8B5CF6"));
                Path2D path = new Path2D.Double();
                path.moveTo(0, 0);
                path.lineTo(w * 0.9, 0); 
                path.curveTo(w * 1.05, h * 0.3, w * 0.75, h * 0.65, w * 0.85, h);        
                path.lineTo(0, h);
                path.closePath();
                g2d.fill(path);
            }
        };
        leftPanel.setPreferredSize(new Dimension(450, 650));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS)); 

        leftPanel.add(Box.createRigidArea(new Dimension(0, 30))); 

        JPanel textWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        textWrapper.setOpaque(false);
        textWrapper.setMaximumSize(new Dimension(450, 100));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 40, 0, 0));

        JLabel lblIcon = new JLabel("");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblIcon.setForeground(Color.WHITE);
        
        JLabel lblInfo = new JLabel("<html>Chào mừng đến với<br>Hệ thống EduFlex.</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblInfo.setForeground(Color.WHITE);
        
        textPanel.add(lblIcon);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblInfo);
        textWrapper.add(textPanel);

        leftPanel.add(textWrapper);
        leftPanel.add(Box.createVerticalGlue());

        JLabel illustration = createIllustration();
        leftPanel.add(illustration);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        return leftPanel;
    }

    private JLabel createIllustration() {
        JLabel imgLabel = new JLabel("", JLabel.CENTER);
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        try {
            java.net.URL imgURL = getClass().getResource("/logo.png");
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image originalImg = originalIcon.getImage();
                int width = 390;  
                int height = 485; 
                Image scaled = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(scaled));
                
                int baseTop = 5; 
                int baseBottom = 15;
                imgLabel.setBorder(new EmptyBorder(baseTop, 10, baseBottom, 10));
                
                long startTime = System.currentTimeMillis();
                Timer timer = new Timer(30, e -> {
                    double time = (System.currentTimeMillis() - startTime) / 1000.0;
                    int offset = (int) (Math.sin(time * 3) * 4); 
                    imgLabel.setBorder(new EmptyBorder(baseTop + offset, 10, baseBottom - offset, 10));
                });
                timer.start();
            } else {
                imgLabel.setText("<html><div style='text-align:center;'><span style='font-size:50px'>🎨</span><br>EduFlex Logo</div></html>");
                imgLabel.setForeground(Color.WHITE);
                imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                imgLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            }
        } catch (Exception e) {
            imgLabel.setText("EduFlex Logo");
            imgLabel.setForeground(Color.WHITE);
        }
        return imgLabel;
    }

    // ==========================================
    // RIGHT PANEL (Form Đăng nhập)
    // ==========================================
    private JPanel createRightPanel() {
        JPanel container = new JPanel(new GridBagLayout()); 
        container.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(0, 40, 0, 40));

        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        langPanel.setBackground(Color.WHITE);
        JLabel lblLang = new JLabel("Tiếng Việt ▼");
        lblLang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLang.setForeground(Color.GRAY);
        langPanel.add(lblLang);
        form.add(langPanel);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTitle);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel socialRow = new JPanel(new GridLayout(1, 2, 15, 0));
        socialRow.setBackground(Color.WHITE);
        socialRow.setMaximumSize(new Dimension(415, 40));
        socialRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton btnGoogle = new RoundedButton("", Color.WHITE, Color.BLACK);
        styleGoogleButton(btnGoogle); 

        RoundedButton btnFacebook = new RoundedButton("", Color.WHITE, Color.BLACK);
        styleFacebookButton(btnFacebook); 

        socialRow.add(btnGoogle);
        socialRow.add(btnFacebook);
        form.add(socialRow);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        form.add(createDivider("- HOẶC -"));
        form.add(Box.createRigidArea(new Dimension(0, 15)));

        form.add(createInputLabel("Tài khoản"));
        PlaceholderTextField txtEmail = new PlaceholderTextField("");
        form.add(txtEmail);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        form.add(createInputLabel("Mật khẩu"));
        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setBackground(Color.WHITE);
        passWrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        passWrapper.setMaximumSize(new Dimension(415, 35));
        passWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBorder(null); 
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnTogglePass = new JButton("👁"); 
        btnTogglePass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnTogglePass.setForeground(Color.GRAY);
        btnTogglePass.setBorder(null);
        btnTogglePass.setBackground(Color.WHITE);
        btnTogglePass.setFocusPainted(false);
        btnTogglePass.setContentAreaFilled(false);
        btnTogglePass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        passWrapper.add(txtPass, BorderLayout.CENTER);
        passWrapper.add(btnTogglePass, BorderLayout.EAST);
        form.add(passWrapper);
        form.add(Box.createRigidArea(new Dimension(0, 40)));

        RoundedButton btnLogin = new RoundedButton("Đăng nhập", Color.decode("#8B5CF6"), Color.WHITE);
        btnLogin.setColors(Color.decode("#8B5CF6"), Color.decode("#7C3AED"), Color.decode("#6D28D9"));
        btnLogin.setBorderColors(Color.decode("#8B5CF6"), Color.decode("#7C3AED"));
        btnLogin.setRadius(35);
        btnLogin.setMaximumSize(new Dimension(415, 45));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblFoot = new JLabel("Chưa có tài khoản? ");
        lblFoot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFoot.setForeground(Color.GRAY);
        
        JLabel lblRegister = new JLabel("Đăng ký ngay");
        lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegister.setForeground(Color.decode("#8B5CF6"));
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        
        footerPanel.add(lblFoot);
        footerPanel.add(lblRegister);
        form.add(footerPanel);

        // Events
        btnTogglePass.addActionListener(e -> {
            if (txtPass.getEchoChar() == '•') {
                txtPass.setEchoChar((char) 0);
                btnTogglePass.setText("🙈");
            } else {
                txtPass.setEchoChar('•');
                btnTogglePass.setText("👁");
            }
        });

        btnLogin.addActionListener(e -> {
            String username = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            controller.handleLogin(username, pass, this);
        });

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { JOptionPane.showMessageDialog(LoginUI.this, "Vui lòng liên hệ Quản trị viên để được cấp phát tài khoản!"); }
            @Override public void mouseEntered(MouseEvent e) { lblRegister.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#8B5CF6"))); }
            @Override public void mouseExited(MouseEvent e) { lblRegister.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0)); }
        });

        container.add(form);
        return container;
    }

    // ==========================================
    // STYLING METHODS CHO BUTTON
    // ==========================================
    private void styleGoogleButton(RoundedButton btn) {
        btn.setText("<html><b style='color:#DB4437'>G</b> Đăng nhập Google</html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.decode("#374151"));
        btn.setColors(Color.decode("#FFFFFF"), Color.decode("#F9FAFB"), Color.decode("#F3F4F6"));
        btn.setBorderColors(Color.decode("#E5E7EB"), Color.decode("#D1D5DB"));
        btn.setRadius(35);
    }

    private void styleFacebookButton(RoundedButton btn) {
        btn.setText("<html><b>f</b> Đăng nhập Facebook</html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setColors(Color.decode("#1877F2"), Color.decode("#166FE5"), Color.decode("#145DD1"));
        btn.setBorderColors(Color.decode("#1877F2"), Color.decode("#166FE5"));
        btn.setRadius(35);
    }

    private JLabel createInputLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.decode("#9CA3AF")); 
        l.setBorder(new EmptyBorder(0, 0, 5, 0));
        return l;
    }

    private JPanel createDivider(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(415, 30));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.LIGHT_GRAY);
        p.add(l);
        return p;
    }

    class RoundedButton extends JButton {
        private Color bg, hoverBg, pressedBg;
        private Color borderColor, hoverBorderColor;
        private int radius = 15;
        private boolean isPressed = false;
        private boolean isHovered = false;

        public RoundedButton(String t, Color b, Color f) {
            super(t); 
            this.bg = b; 
            this.hoverBg = b.darker();
            this.pressedBg = b.darker().darker();
            this.borderColor = b;
            this.hoverBorderColor = b.darker();
            
            setBackground(b); 
            setForeground(f); 
            setFocusPainted(false); 
            setContentAreaFilled(false); 
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(5, 15, 5, 15)); 

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { if (SwingUtilities.isLeftMouseButton(e)) { isPressed = true; repaint(); } }
                @Override public void mouseReleased(MouseEvent e) { if (SwingUtilities.isLeftMouseButton(e)) { isPressed = false; repaint(); } }
            });
        }

        public void setColors(Color bg, Color hoverBg, Color pressedBg) {
            this.bg = bg; this.hoverBg = hoverBg; this.pressedBg = pressedBg; setBackground(bg);
        }

        public void setBorderColors(Color border, Color hoverBorder) {
            this.borderColor = border; this.hoverBorderColor = hoverBorder;
        }

        public void setRadius(int radius) { this.radius = radius; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isPressed) g2.setColor(pressedBg);
            else if (isHovered) g2.setColor(hoverBg);
            else g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            
            if (borderColor != null) {
                if (isHovered && !isPressed && hoverBorderColor != null) g2.setColor(hoverBorderColor);
                else g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class PlaceholderTextField extends JTextField {
        public PlaceholderTextField(String p) {
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(415, 35));
            setMaximumSize(new Dimension(415, 35));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        }
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi Đăng Nhập", JOptionPane.ERROR_MESSAGE);
    }

    // ==========================================
    // GIAO DIỆN CHÍNH (WEB DASHBOARD UI)
    // ==========================================
    // ==========================================
    // GIAO DIỆN CHÍNH (WEB DASHBOARD UI)
    // ==========================================
    public void onLoginSuccess() {
        String fullName = com.mycompany.myapp.utils.SessionStore.getFullName();
        List<String> roles = com.mycompany.myapp.utils.SessionStore.getUserRoles(); 

        this.dispose(); 

        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("EduFlex Dashboard - Hệ Thống Quản Lý");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1350, 850);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setLayout(new BorderLayout());

            // --- 1. TOP HEADER ---
            JPanel topHeader = createModernHeader(mainFrame, fullName);
            
            // --- 2. SIDEBAR CHÍNH (Màu Tím `#6E58D7`) ---
            JPanel sidebar = new JPanel();
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setBackground(Color.decode("#6E58D7")); 
            sidebar.setPreferredSize(new Dimension(270, 0));
            sidebar.setBorder(new EmptyBorder(0,0,0,0));

            JLabel lblMenuTitle = new JLabel("DANH MỤC QUẢN LÝ");
            lblMenuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblMenuTitle.setForeground(Color.decode("#E2E8F0")); 
            lblMenuTitle.setBorder(new EmptyBorder(30, 25, 15, 0));
            sidebar.add(lblMenuTitle);

            // --- 3. MAIN CONTENT (CardLayout) ---
            CardLayout cardLayout = new CardLayout();
            JPanel contentPanel = new JPanel(cardLayout);
            contentPanel.setBackground(Color.decode("#F1F5F9"));

            // Danh sách chứa các nút để xử lý logic Reset Active State
            List<JButton> navButtons = new ArrayList<>();

            // ==========================================
            // PHÂN QUYỀN MENU DỰA VÀO ROLES TỪ DATABASE
            // ==========================================
            
            if (roles.contains("Nhan_Vien_Quan_Ly_He_Thong")) {
                // QUYỀN QUẢN TRỊ HỆ THỐNG
                contentPanel.add(new com.mycompany.myapp.view.screens.system.AccountManagerUI(), "ACCOUNT_MGR");
                contentPanel.add(new com.mycompany.myapp.view.screens.system.SystemConfigUI(), "SYSTEM_CFG");
                
                sidebar.add(createNavBtn("Quản lý tài khoản", "ACCOUNT_MGR", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Cấu hình hệ thống", "SYSTEM_CFG", contentPanel, cardLayout, navButtons));
                
            } else if (roles.contains("Giao_Vien")) {
                // QUYỀN GIÁO VIÊN
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.DashboardPanel(), "DASHBOARD");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.SchedulePanel(), "SCHEDULE");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.StudentListPanel(), "STUDENT_LIST");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AttendancePanel(), "ATTENDANCE");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AttendanceAnalyticsPanel(), "ATTENDANCE_ANA");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.GradeEntryPanel(), "GRADE_ENTRY");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AcademicResultPanel(), "ACADEMIC_RESULT");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.TeacherProfilePanel(), "PROFILE");

                sidebar.add(createNavBtn("Tổng quan (Dashboard)", "DASHBOARD", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Thời khóa biểu", "SCHEDULE", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Danh sách học viên", "STUDENT_LIST", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Điểm danh lớp", "ATTENDANCE", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Theo dõi chuyên cần", "ATTENDANCE_ANA", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Nhập điểm lớp học", "GRADE_ENTRY", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Báo cáo kết quả", "ACADEMIC_RESULT", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Hồ sơ cá nhân", "PROFILE", contentPanel, cardLayout, navButtons));
                
            } else if (roles.contains("Nhan_Vien_Ke_Toan") || roles.contains("Nhan_Vien_Quan_Ly_Nghiep_Vu")) {
                
                // 1. Thêm trực tiếp các màn hình con vào contentPanel chính
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.PaymentPanel(), "FIN_PAYMENT");
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.ManageInvoicePanel(), "FIN_MANAGE");
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.LookupPanel(), "FIN_LOOKUP");
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.InvoiceIssuePanel(), "FIN_ISSUE");
                contentPanel.add(new com.mycompany.myapp.view.screens.finance.PayrollPanel(), "FIN_PAYROLL");

                // 2. Tạo Label phân cách (Section) "HỌC PHÍ"
                JLabel lblTuition = new JLabel("HỌC PHÍ");
                lblTuition.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblTuition.setForeground(Color.decode("#A5B4FC")); // Màu tím nhạt cho tiêu đề phụ
                lblTuition.setBorder(new EmptyBorder(15, 25, 5, 0));
                sidebar.add(lblTuition);

                // 3. Đưa các nút chức năng trực tiếp ra Sidebar chính
                sidebar.add(createNavBtn("Ghi nhận thanh toán", "FIN_PAYMENT", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Quản lý học phí", "FIN_MANAGE", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Tra cứu học phí", "FIN_LOOKUP", contentPanel, cardLayout, navButtons));
                sidebar.add(createNavBtn("Hóa đơn điện tử", "FIN_ISSUE", contentPanel, cardLayout, navButtons));

                // 4. Tạo Label phân cách (Section) "NHÂN SỰ"
                JLabel lblHR = new JLabel("NHÂN SỰ");
                lblHR.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblHR.setForeground(Color.decode("#A5B4FC")); 
                lblHR.setBorder(new EmptyBorder(15, 25, 5, 0));
                sidebar.add(lblHR);

                sidebar.add(createNavBtn("Tính lương nhân viên", "FIN_PAYROLL", contentPanel, cardLayout, navButtons));
            }
            
            sidebar.add(Box.createVerticalGlue()); // Đẩy menu lên trên

            // Lắp ráp các phần vào Frame chính
            mainFrame.add(topHeader, BorderLayout.NORTH);
            mainFrame.add(sidebar, BorderLayout.WEST);
            mainFrame.add(contentPanel, BorderLayout.CENTER);
            mainFrame.setVisible(true);

            // Tự động Click vào Tab đầu tiên khi vừa login xong
            if (!navButtons.isEmpty()) {
                navButtons.get(0).doClick();
            }
        });
    }

    // --- TẠO HEADER & CHỨC NĂNG DROPDOWN ĐĂNG XUẤT ---
    private JPanel createModernHeader(JFrame frame, String name) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        JLabel lblLogo = new JLabel("  EDUFLEX DASHBOARD");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogo.setForeground(Color.decode("#8B5CF6"));
        header.add(lblLogo, BorderLayout.WEST);

        // Khu vực hiển thị tên người dùng
        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        userArea.setOpaque(false);
        userArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblUser = new JLabel("" + name + "  ");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Color.decode("#334155"));
        userArea.add(lblUser);
        
        // Tạo Menu Đăng xuất (Dropdown)
        JPopupMenu dropMenu = new JPopupMenu();
        dropMenu.setBackground(Color.WHITE);
        dropMenu.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JMenuItem itemLogout = new JMenuItem("Đăng xuất khỏi hệ thống");
        itemLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemLogout.setForeground(Color.decode("#E74C3C")); // Chữ màu đỏ
        itemLogout.setMargin(new Insets(10, 15, 10, 15));
        itemLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Sự kiện click Đăng xuất
        itemLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, "Xác nhận đăng xuất khỏi hệ thống?", "EduFlex", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                com.mycompany.myapp.utils.SessionStore.clearSession(); // Xóa phiên đăng nhập
                frame.dispose(); // Đóng Dashboard
                new LoginUI().setVisible(true); // Trở lại màn hình Login
            }
        });
        dropMenu.add(itemLogout);

        // Bắt sự kiện click vào Tên để xổ Menu
        userArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { dropMenu.show(userArea, 0, userArea.getHeight()); }
        });

        header.add(userArea, BorderLayout.EAST);
        return header;
    }

    // --- TẠO NÚT MENU SIDEBAR (ĐÃ FIX LỖI GIAO DIỆN WINDOWS) ---
    private JButton createNavBtn(String text, String cardName, JPanel parent, CardLayout layout, java.util.List<JButton> navList) {
        
        // Tùy chỉnh JButton để bỏ qua hoàn toàn cách vẽ nút mặc định của Windows
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                // Tự vẽ màu nền cho nút dựa theo trạng thái (Hover/Active)
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g); // Vẽ chữ lên trên nền
            }
        };

        btn.setMaximumSize(new Dimension(270, 48));
        btn.setPreferredSize(new Dimension(270, 48));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(Color.decode("#6E58D7")); // Màu nền mặc định
        
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        // 3 DÒNG CODE QUAN TRỌNG NHẤT ĐỂ FIX LỖI NHƯ TRONG ẢNH
        btn.setContentAreaFilled(false); // Ngăn Windows tự tô nền (gây ra màu xanh nhạt/trắng)
        btn.setFocusPainted(false);      // Xóa khung viền gạch đứt khi click
        btn.setOpaque(false);            // Bắt buộc false để paintComponent phía trên hoạt động
        
        btn.setBorderPainted(true);      // Giữ lại viền để hiển thị đường kẻ đánh dấu
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Thiết lập 2 trạng thái viền
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 25, 0, 0);
        Border activeBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, Color.WHITE), // Cột đánh dấu màu trắng dày 4px
            BorderFactory.createEmptyBorder(0, 21, 0, 0)             // Bù padding để chữ không bị lệch
        );

        btn.setBorder(emptyBorder); // Mặc định không có viền trái
        navList.add(btn); 

        // Xử lý Sự kiện Click (Active State)
        btn.addActionListener(e -> {
            layout.show(parent, cardName);
            
            // 1. Reset toàn bộ các nút khác
            for (JButton b : navList) {
                b.setBackground(Color.decode("#6E58D7"));
                b.setBorder(emptyBorder);
                b.putClientProperty("isActive", false); // Gỡ cờ Active
            }
            
            // 2. Highlight nút hiện tại
            btn.setBackground(Color.decode("#503CC8")); // Tối hơn để nhấn mạnh
            btn.setBorder(activeBorder);
            btn.putClientProperty("isActive", true);    // Cắm cờ Active
        });
        
        // Xử lý Sự kiện Rê chuột (Hover State)
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                Boolean isActive = (Boolean) btn.getClientProperty("isActive");
                if (isActive == null || !isActive) {
                    btn.setBackground(Color.decode("#7E6BE0")); // Tím sáng hơn khi Hover
                }
            } 
            public void mouseExited(MouseEvent e) { 
                Boolean isActive = (Boolean) btn.getClientProperty("isActive");
                if (isActive == null || !isActive) {
                    btn.setBackground(Color.decode("#6E58D7")); // Trả về màu cũ khi rời chuột
                }
            } 
        });
        
        return btn;
    }

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
