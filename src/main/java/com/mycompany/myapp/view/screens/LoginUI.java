package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.controller.LoginController;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class LoginUI extends JFrame {
    private final LoginController controller;

    private static final Color PRIMARY = Color.decode("#8B5CF6");
    private static final Color PRIMARY_DARK = Color.decode("#6D28D9");
    private static final Color SIDEBAR_BG = Color.decode("#5B47D6");
    private static final Color SIDEBAR_HOVER = Color.decode("#6D5BE3");
    private static final Color SIDEBAR_ACTIVE = Color.decode("#FFFFFF");
    private static final Color SIDEBAR_TEXT = Color.decode("#F8FAFC");
    private static final Color SIDEBAR_TEXT_MUTED = Color.decode("#D8D2FF");
    private static final Color SIDEBAR_ICON_BG = Color.decode("#7657F2");
    private static final Color SIDEBAR_ACTIVE_TEXT = Color.decode("#4631B9");
    private static final Color TEXT_DARK = Color.decode("#1E293B");
    private static final Color TEXT_MUTED = Color.decode("#64748B");
    private static final Color BORDER = Color.decode("#E2E8F0");
    private static final Color PAGE_BG = Color.decode("#F1F5F9");
    private static final Color DANGER = Color.decode("#EF4444");

    public LoginUI() {
        controller = new LoginController();
        initUI();
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Đào Tạo Trực Tuyến – Alpha Logic Center");
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

    private ImageIcon createScaledIcon(String pathOrUrl, int width, int height) {
        try {
            Image img;
            if (pathOrUrl.startsWith("http")) {
                img = javax.imageio.ImageIO.read(new java.net.URL(pathOrUrl));
            } else {
                java.net.URL resource = getClass().getResource(pathOrUrl);
                if (resource == null) return null;
                img = new ImageIcon(resource).getImage();
            }

            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            System.err.println("Không thể load icon từ: " + pathOrUrl);
            return null;
        }
    }



    private JComponent createHeaderLogoComponent(int boxSize, int imageSize) {
        return new LogoImagePanel("/wappgpt_logo.png", boxSize, imageSize);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                g2d.setColor(PRIMARY);
                Path2D path = new Path2D.Double();
                path.moveTo(0, 0);
                path.lineTo(w * 0.9, 0);
                path.curveTo(w * 1.05, h * 0.3, w * 0.75, h * 0.65, w * 0.85, h);
                path.lineTo(0, h);
                path.closePath();
                g2d.fill(path);

                g2d.dispose();
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

        JLabel lblInfo = new JLabel("<html>Chào mừng đến với<br>Hệ thống Alpha Logic Center</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblInfo.setForeground(Color.WHITE);

        textPanel.add(lblInfo);
        textWrapper.add(textPanel);

        leftPanel.add(textWrapper);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(createIllustration());
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
                Image scaled = originalImg.getScaledInstance(390, 485, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(scaled));

                final int baseTop = 5;
                final int baseBottom = 15;
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

    private JPanel createRightPanel() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(34, 40, 34, 52));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setPreferredSize(new Dimension(470, 560));
        form.setMaximumSize(new Dimension(470, 560));
        form.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        langPanel.setBackground(Color.WHITE);
        langPanel.setMaximumSize(new Dimension(470, 28));
        langPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLang = new JLabel("Tiếng Việt ▼");
        lblLang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLang.setForeground(TEXT_MUTED);
        langPanel.add(lblLang);

        form.add(langPanel);
        form.add(Box.createRigidArea(new Dimension(0, 26)));

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lblTitle.setForeground(Color.BLACK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTitle);

        JLabel lblHint = new JLabel("Vui lòng đăng nhập để tiếp tục quản lý hệ thống");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHint.setForeground(TEXT_MUTED);
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createRigidArea(new Dimension(0, 6)));
        form.add(lblHint);

        form.add(Box.createRigidArea(new Dimension(0, 28)));

        JPanel socialRow = new JPanel(new GridLayout(1, 2, 14, 0));
        socialRow.setBackground(Color.WHITE);
        socialRow.setPreferredSize(new Dimension(470, 42));
        socialRow.setMaximumSize(new Dimension(470, 42));
        socialRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        SocialLoginButton btnGoogle = new SocialLoginButton("G", "Đăng nhập bằng Google",
                Color.WHITE, Color.decode("#334155"), Color.decode("#D1D5DB"));
        btnGoogle.setIconColor(Color.decode("#4285F4"));
        btnGoogle.setCompactMode(true);

        SocialLoginButton btnFacebook = new SocialLoginButton("f", "Đăng nhập bằng Facebook",
                Color.WHITE, Color.decode("#334155"), Color.decode("#D1D5DB"));
        btnFacebook.setIconColor(Color.WHITE);
        btnFacebook.setIconBackground(Color.decode("#1877F2"));
        btnFacebook.setCompactMode(true);

        socialRow.add(btnGoogle);
        socialRow.add(btnFacebook);

        form.add(socialRow);
        form.add(Box.createRigidArea(new Dimension(0, 24)));
        form.add(createDivider("HOẶC"));
        form.add(Box.createRigidArea(new Dimension(0, 18)));

        form.add(createInputLabel("Tài khoản"));
        PlaceholderTextField txtEmail = new PlaceholderTextField("");
        txtEmail.setPreferredSize(new Dimension(470, 42));
        txtEmail.setMaximumSize(new Dimension(470, 42));
        form.add(txtEmail);

        form.add(Box.createRigidArea(new Dimension(0, 20)));
        form.add(createInputLabel("Mật khẩu"));

        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setBackground(Color.WHITE);
        passWrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#CBD5E1")));
        passWrapper.setPreferredSize(new Dimension(470, 42));
        passWrapper.setMaximumSize(new Dimension(470, 42));
        passWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPass = new JPasswordField();
        txtPass.setBorder(new EmptyBorder(0, 0, 0, 0));
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtPass.setEchoChar('•');

        ImageIcon iconEyeOpen = createScaledIcon("https://cdn-icons-png.flaticon.com/512/159/159604.png", 20, 20);
        ImageIcon iconEyeClosed = createScaledIcon("https://cdn-icons-png.flaticon.com/512/2767/2767146.png", 20, 20);

        JButton btnTogglePass = new JButton(iconEyeClosed);
        btnTogglePass.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 4));
        btnTogglePass.setBackground(Color.WHITE);
        btnTogglePass.setFocusPainted(false);
        btnTogglePass.setContentAreaFilled(false);
        btnTogglePass.setCursor(new Cursor(Cursor.HAND_CURSOR));

        passWrapper.add(txtPass, BorderLayout.CENTER);
        passWrapper.add(btnTogglePass, BorderLayout.EAST);

        form.add(passWrapper);
        form.add(Box.createRigidArea(new Dimension(0, 36)));

        RoundedButton btnLogin = new RoundedButton("Đăng nhập", PRIMARY, Color.WHITE);
        btnLogin.setColors(PRIMARY, Color.decode("#7C3AED"), PRIMARY_DARK);
        btnLogin.setBorderColors(PRIMARY, Color.decode("#7C3AED"));
        btnLogin.setRadius(36);
        btnLogin.setPreferredSize(new Dimension(470, 46));
        btnLogin.setMaximumSize(new Dimension(470, 46));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(btnLogin);
        form.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setPreferredSize(new Dimension(470, 28));
        footerPanel.setMaximumSize(new Dimension(470, 28));
        footerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFoot = new JLabel("Chưa có tài khoản? ");
        lblFoot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFoot.setForeground(TEXT_MUTED);

        JLabel lblRegister = new JLabel("Đăng ký ngay");
        lblRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRegister.setForeground(PRIMARY);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));

        footerPanel.add(lblFoot);
        footerPanel.add(lblRegister);
        form.add(footerPanel);

        btnTogglePass.addActionListener(e -> {
            if (txtPass.getEchoChar() == '•') {
                txtPass.setEchoChar((char) 0);
                btnTogglePass.setIcon(iconEyeOpen);
            } else {
                txtPass.setEchoChar('•');
                btnTogglePass.setIcon(iconEyeClosed);
            }
        });

        btnLogin.addActionListener(e -> {
            String username = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            controller.handleLogin(username, pass, this);
        });

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginUI.this, "Vui lòng liên hệ Quản trị viên để được cấp phát tài khoản!");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegister.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARY));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblRegister.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        container.add(form, gbc);
        return container;
    }

    private void styleGoogleButton(RoundedButton btn) {
        btn.setText("<html><b style='color:#DB4437'>G</b>&nbsp;&nbsp;Đăng nhập Google</html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.decode("#374151"));
        btn.setColors(Color.decode("#FFFFFF"), Color.decode("#F9FAFB"), Color.decode("#F3F4F6"));
        btn.setBorderColors(Color.decode("#E5E7EB"), Color.decode("#D1D5DB"));
        btn.setRadius(35);
    }

    private void styleFacebookButton(RoundedButton btn) {
        btn.setText("<html><b>f</b>&nbsp;&nbsp;Đăng nhập Facebook</html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setColors(Color.decode("#1877F2"), Color.decode("#166FE5"), Color.decode("#145DD1"));
        btn.setBorderColors(Color.decode("#1877F2"), Color.decode("#166FE5"));
        btn.setRadius(35);
    }

    private JLabel createInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#94A3B8"));
        label.setBorder(new EmptyBorder(0, 0, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(470, 24));
        return label;
    }

    private JPanel createDivider(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(470, 26));
        panel.setMaximumSize(new Dimension(470, 26));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel leftLine = new JPanel();
        leftLine.setBackground(Color.decode("#E2E8F0"));
        leftLine.setPreferredSize(new Dimension(1, 1));

        JPanel rightLine = new JPanel();
        rightLine.setBackground(Color.decode("#E2E8F0"));
        rightLine.setPreferredSize(new Dimension(1, 1));

        JLabel label = new JLabel("  " + text + "  ", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.decode("#B0B7C3"));

        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(leftLine, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        panel.add(rightLine, gbc);

        return panel;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi Đăng Nhập", JOptionPane.ERROR_MESSAGE);
    }

    public void onLoginSuccess() {
        String fullName = com.mycompany.myapp.utils.SessionStore.getFullName();
        List<String> roles = com.mycompany.myapp.utils.SessionStore.getUserRoles();

        this.dispose();

        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = new JFrame("Alpha Logic Center - Hệ Thống Quản Lý");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1350, 850);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setLayout(new BorderLayout());

            JPanel sidebarContent = new JPanel();
            sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
            sidebarContent.setBackground(SIDEBAR_BG);
            sidebarContent.setBorder(new EmptyBorder(14, 0, 18, 0));

            JPanel sidebar = new JPanel(new BorderLayout());
            sidebar.setBackground(SIDEBAR_BG);
            sidebar.setPreferredSize(new Dimension(306, 0));
            sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(94, 75, 210)));

            sidebarContent.add(createSidebarTitle("DANH MỤC QUẢN LÝ", 6, 22, 10));

            CardLayout cardLayout = new CardLayout();
            JPanel contentPanel = new JPanel(cardLayout);
            contentPanel.setBackground(PAGE_BG);

            List<JButton> navButtons = new ArrayList<>();

            if (roles.contains("Nhan_Vien_Quan_Ly_He_Thong")) {
                contentPanel.add(new com.mycompany.myapp.view.screens.QuanLyHeThong.AccountManagerUI(), "ACCOUNT_MGR");
                contentPanel.add(new com.mycompany.myapp.view.screens.QuanLyHeThong.SystemConfigUI(), "SYSTEM_CFG");

                sidebarContent.add(createNavBtn("Quản lý tài khoản", "ACCOUNT_MGR", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Cấu hình hệ thống", "SYSTEM_CFG", contentPanel, cardLayout, navButtons));

            } else if (roles.contains("Giao_Vien")) {
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.DashboardPanel(), "DASHBOARD");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.SchedulePanel(), "SCHEDULE");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.StudentListPanel(), "STUDENT_LIST");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AttendancePanel(), "ATTENDANCE");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AttendanceAnalyticsPanel(), "ATTENDANCE_ANA");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.GradeEntryPanel(), "GRADE_ENTRY");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AcademicResultPanel(), "ACADEMIC_RESULT");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.TeacherProfilePanel(), "PROFILE");

                sidebarContent.add(createNavBtn("Tổng quan", "DASHBOARD", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Thời khóa biểu", "SCHEDULE", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Danh sách học viên", "STUDENT_LIST", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Điểm danh lớp", "ATTENDANCE", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Theo dõi chuyên cần", "ATTENDANCE_ANA", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Nhập điểm lớp học", "GRADE_ENTRY", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Báo cáo kết quả", "ACADEMIC_RESULT", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Hồ sơ cá nhân", "PROFILE", contentPanel, cardLayout, navButtons));

            } else if (roles.contains("Nhan_Vien_Quan_Ly_Nghiep_Vu")) {
                contentPanel.add(new com.mycompany.myapp.view.screens.GiaoVuUI.StudentManagementPanel(), "GV_STUDENT_MGR");
                contentPanel.add(new com.mycompany.myapp.view.screens.GiaoVuUI.TeacherAssignmentPanel(), "GV_ASSIGN");
                contentPanel.add(new com.mycompany.myapp.view.screens.teacher.AttendanceAnalyticsPanel(), "GV_ATTENDANCE");
                contentPanel.add(new com.mycompany.myapp.view.screens.GiaoVuUI.ManageSubjectPanel(), "GV_SUBJECT");
                contentPanel.add(new com.mycompany.myapp.view.screens.GiaoVuUI.StudyReportPanel(), "GV_REPORT");

                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.PaymentPanel(), "FIN_PAYMENT");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.ManageInvoicePanel(), "FIN_MANAGE");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.LookupPanel(), "FIN_LOOKUP");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.InvoiceIssuePanel(), "FIN_ISSUE");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.PayrollPanel(), "FIN_PAYROLL");

                sidebarContent.add(createSidebarTitle("HỌC VỤ & ĐÀO TẠO", 16, 22, 6));
                sidebarContent.add(createNavBtn("Quản lý học viên", "GV_STUDENT_MGR", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Phân công giáo viên", "GV_ASSIGN", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Tình trạng điểm danh", "GV_ATTENDANCE", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Quản lý môn học", "GV_SUBJECT", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Báo cáo học tập", "GV_REPORT", contentPanel, cardLayout, navButtons));

                sidebarContent.add(createSidebarTitle("HỌC PHÍ", 16, 22, 6));
                sidebarContent.add(createNavBtn("Ghi nhận thanh toán", "FIN_PAYMENT", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Quản lý học phí", "FIN_MANAGE", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Tra cứu học phí", "FIN_LOOKUP", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Hóa đơn điện tử", "FIN_ISSUE", contentPanel, cardLayout, navButtons));

                sidebarContent.add(createSidebarTitle("NHÂN SỰ", 16, 22, 6));
                sidebarContent.add(createNavBtn("Tính lương nhân viên", "FIN_PAYROLL", contentPanel, cardLayout, navButtons));

            } else if (roles.contains("Nhan_Vien_Ke_Toan")) {
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.PaymentPanel(), "FIN_PAYMENT");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.ManageInvoicePanel(), "FIN_MANAGE");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.LookupPanel(), "FIN_LOOKUP");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.InvoiceIssuePanel(), "FIN_ISSUE");
                contentPanel.add(new com.mycompany.myapp.view.screens.ThanhToan.PayrollPanel(), "FIN_PAYROLL");

                sidebarContent.add(createSidebarTitle("HỌC PHÍ", 16, 22, 6));
                sidebarContent.add(createNavBtn("Ghi nhận thanh toán", "FIN_PAYMENT", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Quản lý học phí", "FIN_MANAGE", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Tra cứu học phí", "FIN_LOOKUP", contentPanel, cardLayout, navButtons));
                sidebarContent.add(createNavBtn("Hóa đơn điện tử", "FIN_ISSUE", contentPanel, cardLayout, navButtons));

                sidebarContent.add(createSidebarTitle("NHÂN SỰ", 16, 22, 6));
                sidebarContent.add(createNavBtn("Tính lương nhân viên", "FIN_PAYROLL", contentPanel, cardLayout, navButtons));
            }

            sidebarContent.add(Box.createVerticalGlue());

            JScrollPane sidebarScroll = new JScrollPane(sidebarContent);
            sidebarScroll.setBorder(null);
            sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sidebarScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            sidebarScroll.getViewport().setBackground(SIDEBAR_BG);
            sidebarScroll.getVerticalScrollBar().setUnitIncrement(14);

            JButton sidebarToggle = createSidebarToggleButton(sidebar, sidebarContent);
            sidebar.add(sidebarScroll, BorderLayout.CENTER);
            sidebar.add(sidebarToggle, BorderLayout.EAST);

            JPanel topHeader = createModernHeader(mainFrame, fullName, roles, sidebar, sidebarContent);

            mainFrame.add(topHeader, BorderLayout.NORTH);
            mainFrame.add(sidebar, BorderLayout.WEST);
            mainFrame.add(contentPanel, BorderLayout.CENTER);
            mainFrame.setVisible(true);

            if (!navButtons.isEmpty()) {
                navButtons.get(0).doClick();
            }
        });
    }

    private JLabel createSidebarTitle(String text, int top, int left, int bottom) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(SIDEBAR_TEXT_MUTED);
        label.setBorder(new EmptyBorder(top, left, bottom, 0));
        label.setMaximumSize(new Dimension(288, 30));
        label.setPreferredSize(new Dimension(288, 30));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createSidebarToggleButton(JPanel sidebar, JPanel sidebarContent) {
        JButton button = new SidebarEdgeToggleButton();
        button.putClientProperty("collapsed", false);
        button.setToolTipText("Thu gọn thanh chức năng");
        button.addActionListener(e -> toggleSidebar(sidebar, sidebarContent, button));
        return button;
    }

    private void toggleSidebar(JPanel sidebar, JPanel sidebarContent, JButton toggleButton) {
        boolean collapsed = !Boolean.TRUE.equals(sidebar.getClientProperty("collapsed"));
        sidebar.putClientProperty("collapsed", collapsed);

        int contentWidth = collapsed ? 72 : 288;
        int sidebarWidth = contentWidth + 18;
        sidebar.setPreferredSize(new Dimension(sidebarWidth, 0));
        sidebar.setMinimumSize(new Dimension(sidebarWidth, 0));

        for (Component component : sidebarContent.getComponents()) {
            if (component instanceof JLabel) {
                component.setVisible(!collapsed);
            }

            if (component instanceof SidebarNavButton) {
                component.setPreferredSize(new Dimension(contentWidth, 44));
                component.setMaximumSize(new Dimension(contentWidth, 44));
                ((JComponent) component).putClientProperty("collapsed", collapsed);
                component.repaint();
            }
        }

        sidebarContent.setBorder(collapsed
                ? new EmptyBorder(14, 0, 18, 0)
                : new EmptyBorder(14, 0, 18, 0));

        toggleButton.putClientProperty("collapsed", collapsed);
        toggleButton.setToolTipText(collapsed ? "Mở rộng thanh chức năng" : "Thu gọn thanh chức năng");
        toggleButton.repaint();

        sidebar.revalidate();
        sidebar.repaint();

        Container parent = sidebar.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private JPanel createModernHeader(JFrame frame, String name, List<String> roles, JPanel sidebar, JPanel sidebarContent) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 76));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        brand.setOpaque(false);

        JComponent logoCircle = createHeaderLogoComponent(46, 30);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel lblLogo = new JLabel("ALPHA LOGIC CENTER");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblLogo.setForeground(PRIMARY);

        JLabel lblSub = new JLabel("Hệ thống quản lý đào tạo");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        brandText.add(lblLogo);
        brandText.add(Box.createVerticalStrut(2));
        brandText.add(lblSub);

        brand.add(logoCircle);
        brand.add(brandText);

        header.add(brand, BorderLayout.WEST);
        header.add(createUserMenu(frame, name, roles), BorderLayout.EAST);

        return header;
    }

    private JPanel createUserMenu(JFrame frame, String name, List<String> roles) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 13));
        wrapper.setOpaque(false);

        RoundedPanel accountBox = new RoundedPanel(16, Color.decode("#F8FAFC"));
        accountBox.setLayout(new BorderLayout(12, 0));
        accountBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        accountBox.setPreferredSize(new Dimension(312, 48));
        accountBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        accountBox.setToolTipText("Mở menu tài khoản");

        JLabel avatar = new JLabel(getInitials(name), SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);

        JPanel avatarCircle = new RoundedPanel(18, PRIMARY);
        avatarCircle.setPreferredSize(new Dimension(34, 34));
        avatarCircle.setLayout(new BorderLayout());
        avatarCircle.add(avatar, BorderLayout.CENTER);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(name == null || name.trim().isEmpty() ? "Người dùng" : name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(TEXT_DARK);

        JLabel lblRole = new JLabel(getRoleDisplayName(roles));
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(TEXT_MUTED);

        info.add(lblName);
        info.add(Box.createVerticalStrut(1));
        info.add(lblRole);

        JComponent arrow = new AccountUserIcon();
        arrow.setToolTipText("Mở menu tài khoản");

        accountBox.add(avatarCircle, BorderLayout.WEST);
        accountBox.add(info, BorderLayout.CENTER);
        accountBox.add(arrow, BorderLayout.EAST);

        JPopupMenu menu = createLogoutMenu(frame, name, roles);

        accountBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                accountBox.setBackground(Color.decode("#F1F5F9"));
                accountBox.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                accountBox.setBackground(Color.decode("#F8FAFC"));
                accountBox.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                menu.show(accountBox, 0, accountBox.getHeight() + 8);
            }
        });

        wrapper.add(accountBox);
        return wrapper;
    }

    private JPopupMenu createLogoutMenu(JFrame frame, String name, List<String> roles) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);
        menu.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JMenuItem profile = new JMenuItem("  Tài khoản: " + (name == null || name.trim().isEmpty() ? "Người dùng" : name));
        profile.setEnabled(false);
        profile.setFont(new Font("Segoe UI", Font.BOLD, 13));
        profile.setForeground(TEXT_DARK);
        profile.setBorder(new EmptyBorder(8, 10, 6, 10));
        menu.add(profile);

        JMenuItem role = new JMenuItem("  Vai trò: " + getRoleDisplayName(roles));
        role.setEnabled(false);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        role.setForeground(TEXT_MUTED);
        role.setBorder(new EmptyBorder(4, 10, 10, 10));
        menu.add(role);

        menu.addSeparator();

        JMenuItem logout = new JMenuItem("  Đăng xuất khỏi hệ thống");
        logout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logout.setForeground(DANGER);
        logout.setBackground(Color.WHITE);
        logout.setBorder(new EmptyBorder(10, 10, 10, 10));
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> confirmLogout(frame));
        menu.add(logout);

        return menu;
    }

    private void confirmLogout(JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Bạn có chắc muốn đăng xuất khỏi hệ thống?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            com.mycompany.myapp.utils.SessionStore.clearSession();
            frame.dispose();
            new LoginUI().setVisible(true);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String getRoleDisplayName(List<String> roles) {
        if (roles == null || roles.isEmpty()) return "Người dùng hệ thống";
        if (roles.contains("Nhan_Vien_Quan_Ly_He_Thong")) return "Quản trị hệ thống";
        if (roles.contains("Nhan_Vien_Quan_Ly_Nghiep_Vu")) return "Nhân viên giáo vụ";
        if (roles.contains("Nhan_Vien_Ke_Toan")) return "Nhân viên kế toán";
        if (roles.contains("Giao_Vien")) return "Giáo viên";
        return roles.get(0);
    }

    private JButton createNavBtn(String text, String cardName, JPanel parent, CardLayout layout, List<JButton> navList) {
        SidebarNavButton btn = new SidebarNavButton(getNavIcon(cardName, text), text);
        btn.setMaximumSize(new Dimension(288, 44));
        btn.setPreferredSize(new Dimension(288, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        navList.add(btn);

        btn.addActionListener(e -> {
            layout.show(parent, cardName);

            for (JButton b : navList) {
                b.putClientProperty("isActive", false);
                b.repaint();
            }

            btn.putClientProperty("isActive", true);
            btn.repaint();
        });

        return btn;
    }

    private String getNavIcon(String cardName, String text) {
        if (cardName == null) return "dot";

        String key = cardName.toUpperCase();
        String label = text == null ? "" : text.toLowerCase();

        if (key.contains("ACCOUNT")) return "user";
        if (key.contains("SYSTEM")) return "gear";
        if (key.contains("DASHBOARD")) return "home";
        if (key.contains("SCHEDULE")) return "calendar";
        if (key.contains("STUDENT")) return "students";

        // Tách riêng hai nghiệp vụ dễ bị trùng icon:
        // - Điểm danh lớp: checklist.
        // - Theo dõi chuyên cần / tình trạng điểm danh: chart/analytics.
        if (key.contains("ATTENDANCE_ANA") || label.contains("chuyên cần") || label.contains("tình trạng")) {
            return "analytics";
        }
        if (key.contains("ATTENDANCE")) return "checklist";

        if (key.contains("GRADE")) return "grade";
        if (key.contains("ACADEMIC") || key.contains("REPORT")) return "report";
        if (key.contains("PROFILE")) return "profile";
        if (key.contains("ASSIGN")) return "assign";
        if (key.contains("SUBJECT")) return "book";
        if (key.contains("PAYMENT")) return "payment";
        if (key.contains("MANAGE")) return "manage";
        if (key.contains("LOOKUP")) return "search";
        if (key.contains("ISSUE")) return "invoice";
        if (key.contains("PAYROLL")) return "salary";

        return "dot";
    }

    private static class SidebarEdgeToggleButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;

        SidebarEdgeToggleButton() {
            super("");
            setPreferredSize(new Dimension(18, 64));
            setMinimumSize(new Dimension(18, 64));
            setMaximumSize(new Dimension(18, 64));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = false;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            boolean collapsed = Boolean.TRUE.equals(getClientProperty("collapsed"));

            int w = getWidth();
            int h = getHeight();
            int tabH = 54;
            int y = Math.max(6, (h - tabH) / 2);

            Color tabColor;
            if (pressed) {
                tabColor = Color.decode("#06A4C7");
            } else if (hovered) {
                tabColor = Color.decode("#12B8D8");
            } else {
                tabColor = Color.decode("#0891B2");
            }

            // Notch nằm sát cạnh phải sidebar, giống các dashboard web hiện đại.
            java.awt.geom.Path2D tab = new java.awt.geom.Path2D.Double();
            tab.moveTo(w, y);
            tab.lineTo(7, y);
            tab.quadTo(0, y, 0, y + 8);
            tab.lineTo(0, y + tabH - 8);
            tab.quadTo(0, y + tabH, 7, y + tabH);
            tab.lineTo(w, y + tabH);
            tab.closePath();

            g2.setColor(new Color(15, 23, 42, hovered ? 38 : 24));
            g2.translate(1, 2);
            g2.fill(tab);
            g2.translate(-1, -2);

            g2.setColor(tabColor);
            g2.fill(tab);

            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.WHITE);

            int cx = 8;
            int cy = y + tabH / 2;

            if (collapsed) {
                // Mở rộng: mũi tên sang phải.
                g2.drawLine(cx - 3, cy - 6, cx + 3, cy);
                g2.drawLine(cx - 3, cy + 6, cx + 3, cy);
            } else {
                // Thu gọn: mũi tên sang trái.
                g2.drawLine(cx + 3, cy - 6, cx - 3, cy);
                g2.drawLine(cx + 3, cy + 6, cx - 3, cy);
            }

            // Dot nhỏ tăng khả năng nhận biết đây là tay nắm kéo/mở.
            g2.fillOval(cx - 1, cy - 1, 2, 2);

            g2.dispose();
        }
    }

    private static class SocialLoginButton extends JButton {
        private final String iconText;
        private final String labelText;
        private Color bgColor;
        private Color textColor;
        private Color borderColor;
        private Color iconColor = Color.WHITE;
        private Color iconBackground = Color.decode("#F8FAFC");
        private boolean hovered = false;
        private boolean pressed = false;
        private boolean compactMode = false;

        SocialLoginButton(String iconText, String labelText, Color bgColor, Color textColor, Color borderColor) {
            super("");
            this.iconText = iconText;
            this.labelText = labelText;
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.borderColor = borderColor;

            setPreferredSize(new Dimension(228, 42));
            setMaximumSize(new Dimension(228, 42));
            setMinimumSize(new Dimension(190, 42));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(labelText);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = false;
                        repaint();
                    }
                }
            });
        }

        void setIconColor(Color iconColor) {
            this.iconColor = iconColor;
        }

        void setIconBackground(Color iconBackground) {
            this.iconBackground = iconBackground;
        }

        void setCompactMode(boolean compactMode) {
            this.compactMode = compactMode;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int arc = compactMode ? 28 : 30;
            Color currentBg = bgColor;

            if (pressed) {
                currentBg = darken(bgColor, 0.06f);
            } else if (hovered) {
                currentBg = lighten(bgColor, 0.04f);
            }

            // Shadow nhẹ kiểu pill button website.
            if (compactMode) {
                g2.setColor(new Color(15, 23, 42, hovered ? 26 : 16));
                g2.fillRoundRect(1, 2, getWidth() - 3, getHeight() - 3, arc, arc);
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 3, arc, arc);

            g2.setColor(hovered ? darken(borderColor, 0.10f) : borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 3, arc, arc);

            int iconSize = compactMode ? 24 : 24;
            int iconX = compactMode ? 16 : 22;
            int iconY = (getHeight() - iconSize) / 2 - 1;

            Color actualIconBg = iconBackground;
            if (bgColor.equals(Color.WHITE) && iconBackground.equals(Color.decode("#F8FAFC"))) {
                actualIconBg = Color.WHITE;
            }

            g2.setColor(actualIconBg);
            g2.fillOval(iconX, iconY, iconSize, iconSize);

            g2.setColor(iconColor);
            Font iconFont = new Font("Segoe UI", Font.BOLD, iconText.equals("f") ? 17 : 15);
            g2.setFont(iconFont);
            FontMetrics ifm = g2.getFontMetrics();
            int ix = iconX + (iconSize - ifm.stringWidth(iconText)) / 2;
            int iy = iconY + (iconSize - ifm.getHeight()) / 2 + ifm.getAscent();
            g2.drawString(iconText, ix, iy);

            g2.setColor(textColor);
            Font labelFont = new Font("Segoe UI", Font.BOLD, compactMode ? 12 : 14);
            g2.setFont(labelFont);
            FontMetrics lfm = g2.getFontMetrics();

            String shown = labelText;
            int textX = iconX + iconSize + 9;
            int maxWidth = getWidth() - textX - 10;
            while (lfm.stringWidth(shown) > maxWidth && shown.length() > 4) {
                shown = shown.substring(0, shown.length() - 4) + "...";
            }

            int textY = (getHeight() - lfm.getHeight()) / 2 + lfm.getAscent() - 1;
            g2.drawString(shown, textX, textY);

            g2.dispose();
        }

        private Color darken(Color color, float fraction) {
            int r = Math.max(0, Math.round(color.getRed() * (1 - fraction)));
            int g = Math.max(0, Math.round(color.getGreen() * (1 - fraction)));
            int b = Math.max(0, Math.round(color.getBlue() * (1 - fraction)));
            return new Color(r, g, b);
        }

        private Color lighten(Color color, float fraction) {
            int r = Math.min(255, Math.round(color.getRed() + (255 - color.getRed()) * fraction));
            int g = Math.min(255, Math.round(color.getGreen() + (255 - color.getGreen()) * fraction));
            int b = Math.min(255, Math.round(color.getBlue() + (255 - color.getBlue()) * fraction));
            return new Color(r, g, b);
        }
    }

    private static class RoundedButton extends JButton {
        private Color bg;
        private Color hoverBg;
        private Color pressedBg;
        private Color borderColor;
        private Color hoverBorderColor;
        private int radius = 15;
        private boolean isPressed = false;
        private boolean isHovered = false;

        RoundedButton(String text, Color background, Color foreground) {
            super(text);
            this.bg = background;
            this.hoverBg = background.darker();
            this.pressedBg = background.darker().darker();
            this.borderColor = background;
            this.hoverBorderColor = background.darker();

            setBackground(background);
            setForeground(foreground);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(5, 15, 5, 15));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = false;
                        repaint();
                    }
                }
            });
        }

        void setColors(Color bg, Color hoverBg, Color pressedBg) {
            this.bg = bg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            setBackground(bg);
        }

        void setBorderColors(Color border, Color hoverBorder) {
            this.borderColor = border;
            this.hoverBorderColor = hoverBorder;
        }

        void setRadius(int radius) {
            this.radius = radius;
        }

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

    private static class PlaceholderTextField extends JTextField {
        PlaceholderTextField(String placeholder) {
            super(placeholder);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setPreferredSize(new Dimension(415, 35));
            setMaximumSize(new Dimension(415, 35));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        }
    }

    private static class SidebarNavButton extends JButton {
        private final String iconType;
        private final String labelText;
        private boolean hovered = false;

        SidebarNavButton(String iconType, String labelText) {
            super("");
            this.iconType = iconType == null ? "dot" : iconType;
            this.labelText = labelText == null ? "" : labelText;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(SIDEBAR_TEXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setToolTipText(this.labelText);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Boolean activeProp = (Boolean) getClientProperty("isActive");
            boolean active = activeProp != null && activeProp;

            int x = 10;
            int y = 3;
            int w = getWidth() - 20;
            int h = getHeight() - 6;

            Color bg = active ? SIDEBAR_ACTIVE : (hovered ? SIDEBAR_HOVER : SIDEBAR_BG);
            Color iconBg = active ? Color.decode("#EEE9FF") : new Color(255, 255, 255, hovered ? 40 : 28);
            Color iconFg = active ? SIDEBAR_ACTIVE_TEXT : Color.WHITE;
            Color textFg = active ? SIDEBAR_ACTIVE_TEXT : SIDEBAR_TEXT;

            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, 14, 14);

            if (active) {
                g2.setColor(SIDEBAR_ACTIVE_TEXT);
                g2.fillRoundRect(x + 3, y + 10, 4, h - 20, 4, 4);
            }

            boolean collapsed = Boolean.TRUE.equals(getClientProperty("collapsed"));

            int iconSize = 28;
            int iconX = collapsed ? x + (w - iconSize) / 2 : x + 16;
            int iconY = y + (h - iconSize) / 2;

            g2.setColor(iconBg);
            g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);

            drawVectorIcon(g2, iconType, iconX, iconY, iconSize, iconFg);

            if (!collapsed) {
                Font textFont = new Font("Segoe UI", Font.BOLD, 13);
                g2.setFont(textFont);
                FontMetrics tfm = g2.getFontMetrics();

                String shown = labelText;
                int maxTextWidth = w - 78;
                while (tfm.stringWidth(shown) > maxTextWidth && shown.length() > 4) {
                    shown = shown.substring(0, shown.length() - 4) + "...";
                }

                int tx = iconX + iconSize + 12;
                int ty = y + (h - tfm.getHeight()) / 2 + tfm.getAscent();

                g2.setColor(textFg);
                g2.drawString(shown, tx, ty);
            }

            g2.dispose();
        }

        private static void drawVectorIcon(Graphics2D g2, String type, int x, int y, int size, Color color) {
            int cx = x + size / 2;
            int cy = y + size / 2;

            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case "students":
                    g2.fillOval(x + 7, y + 7, 6, 6);
                    g2.fillOval(x + 15, y + 7, 6, 6);
                    g2.fillOval(x + 11, y + 4, 6, 6);
                    g2.drawArc(x + 5, y + 14, 10, 8, 0, 180);
                    g2.drawArc(x + 13, y + 14, 10, 8, 0, 180);
                    g2.drawArc(x + 9, y + 12, 10, 9, 0, 180);
                    break;

                case "assign":
                    g2.drawLine(x + 7, y + 10, x + 20, y + 10);
                    g2.drawLine(x + 17, y + 7, x + 21, y + 10);
                    g2.drawLine(x + 17, y + 13, x + 21, y + 10);
                    g2.drawLine(x + 20, y + 18, x + 7, y + 18);
                    g2.drawLine(x + 10, y + 15, x + 6, y + 18);
                    g2.drawLine(x + 10, y + 21, x + 6, y + 18);
                    break;

                case "checklist":
                    g2.drawRoundRect(x + 7, y + 5, 15, 18, 3, 3);
                    g2.drawLine(x + 10, y + 10, x + 12, y + 12);
                    g2.drawLine(x + 12, y + 12, x + 16, y + 8);
                    g2.drawLine(x + 10, y + 17, x + 12, y + 19);
                    g2.drawLine(x + 12, y + 19, x + 17, y + 14);
                    break;

                case "analytics":
                    g2.drawLine(x + 7, y + 21, x + 22, y + 21);
                    g2.fillRoundRect(x + 8, y + 14, 3, 7, 2, 2);
                    g2.fillRoundRect(x + 13, y + 9, 3, 12, 2, 2);
                    g2.fillRoundRect(x + 18, y + 6, 3, 15, 2, 2);
                    break;

                case "grade":
                    Polygon star = new Polygon();
                    star.addPoint(cx, y + 6);
                    star.addPoint(x + 16, y + 13);
                    star.addPoint(x + 22, y + 13);
                    star.addPoint(x + 17, y + 17);
                    star.addPoint(x + 19, y + 23);
                    star.addPoint(cx, y + 19);
                    star.addPoint(x + 9, y + 23);
                    star.addPoint(x + 11, y + 17);
                    star.addPoint(x + 6, y + 13);
                    star.addPoint(x + 12, y + 13);
                    g2.drawPolygon(star);
                    break;

                case "book":
                    g2.drawRoundRect(x + 7, y + 6, 14, 17, 3, 3);
                    g2.drawLine(x + 12, y + 6, x + 12, y + 23);
                    g2.drawLine(x + 15, y + 11, x + 20, y + 11);
                    g2.drawLine(x + 15, y + 15, x + 20, y + 15);
                    break;

                case "report":
                    g2.drawRoundRect(x + 7, y + 5, 14, 18, 3, 3);
                    g2.drawLine(x + 11, y + 11, x + 18, y + 11);
                    g2.drawLine(x + 11, y + 15, x + 18, y + 15);
                    g2.drawLine(x + 11, y + 19, x + 16, y + 19);
                    break;

                case "payment":
                    g2.drawRoundRect(x + 5, y + 9, 18, 12, 3, 3);
                    g2.drawLine(x + 5, y + 13, x + 23, y + 13);
                    g2.drawLine(x + 9, y + 18, x + 14, y + 18);
                    break;

                case "manage":
                    g2.drawRoundRect(x + 6, y + 7, 16, 15, 3, 3);
                    g2.drawLine(x + 9, y + 11, x + 19, y + 11);
                    g2.drawLine(x + 9, y + 15, x + 19, y + 15);
                    g2.drawLine(x + 9, y + 19, x + 15, y + 19);
                    break;

                case "search":
                    g2.drawOval(x + 7, y + 7, 10, 10);
                    g2.drawLine(x + 15, y + 15, x + 22, y + 22);
                    break;

                case "invoice":
                    g2.drawRoundRect(x + 8, y + 5, 13, 18, 2, 2);
                    g2.drawLine(x + 11, y + 10, x + 18, y + 10);
                    g2.drawLine(x + 11, y + 14, x + 18, y + 14);
                    g2.drawLine(x + 11, y + 18, x + 16, y + 18);
                    break;

                case "salary":
                    g2.drawOval(x + 6, y + 6, 16, 16);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics moneyFm = g2.getFontMetrics();
                    String money = "₫";
                    g2.drawString(money, cx - moneyFm.stringWidth(money) / 2, cy + moneyFm.getAscent() / 2 - 2);
                    break;

                case "home":
                    g2.drawLine(x + 6, y + 14, cx, y + 7);
                    g2.drawLine(cx, y + 7, x + 22, y + 14);
                    g2.drawRoundRect(x + 9, y + 14, 10, 8, 2, 2);
                    break;

                case "calendar":
                    g2.drawRoundRect(x + 7, y + 8, 15, 14, 3, 3);
                    g2.drawLine(x + 7, y + 12, x + 22, y + 12);
                    g2.drawLine(x + 11, y + 6, x + 11, y + 10);
                    g2.drawLine(x + 18, y + 6, x + 18, y + 10);
                    break;

                case "profile":
                case "user":
                    g2.drawOval(x + 10, y + 6, 8, 8);
                    g2.drawArc(x + 7, y + 14, 14, 10, 0, 180);
                    break;

                case "gear":
                    g2.drawOval(x + 9, y + 9, 10, 10);
                    g2.drawLine(cx, y + 5, cx, y + 8);
                    g2.drawLine(cx, y + 20, cx, y + 23);
                    g2.drawLine(x + 5, cy, x + 8, cy);
                    g2.drawLine(x + 20, cy, x + 23, cy);
                    break;

                default:
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                    break;
            }
        }
    }

    private static class AccountUserIcon extends JPanel {
        AccountUserIcon() {
            setOpaque(false);
            setPreferredSize(new Dimension(32, 32));
            setMinimumSize(new Dimension(32, 32));
            setMaximumSize(new Dimension(32, 32));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();

            // Nền bo góc nhẹ giống icon account trên website hiện đại.
            g2.setColor(Color.decode("#F8FAFC"));
            g2.fillRoundRect(0, 0, w, h, 10, 10);

            // Icon user silhouette theo mẫu người dùng gợi ý.
            g2.setColor(Color.decode("#111827"));

            int headSize = 10;
            int headX = (w - headSize) / 2;
            int headY = 6;
            g2.fillOval(headX, headY, headSize, headSize);

            int bodyW = 20;
            int bodyH = 12;
            int bodyX = (w - bodyW) / 2;
            int bodyY = 18;

            java.awt.geom.Path2D body = new java.awt.geom.Path2D.Double();
            body.moveTo(bodyX + bodyW / 2.0, bodyY - 2);
            body.curveTo(bodyX + 4, bodyY, bodyX + 2, bodyY + 5, bodyX + 1, bodyY + bodyH - 2);
            body.quadTo(bodyX + 1, bodyY + bodyH, bodyX + 4, bodyY + bodyH);
            body.lineTo(bodyX + bodyW - 4, bodyY + bodyH);
            body.quadTo(bodyX + bodyW - 1, bodyY + bodyH, bodyX + bodyW - 1, bodyY + bodyH - 2);
            body.curveTo(bodyX + bodyW - 2, bodyY + 5, bodyX + bodyW - 4, bodyY, bodyX + bodyW / 2.0, bodyY - 2);
            body.closePath();
            g2.fill(body);

            g2.dispose();
        }
    }

    private static class LogoImagePanel extends JPanel {
        private final String resourcePath;
        private final int boxSize;
        private final int imageSize;
        private Image logoImage;

        LogoImagePanel(String resourcePath, int boxSize, int imageSize) {
            this.resourcePath = resourcePath;
            this.boxSize = boxSize;
            this.imageSize = imageSize;
            setOpaque(false);
            setPreferredSize(new Dimension(boxSize, boxSize));
            setMinimumSize(new Dimension(boxSize, boxSize));
            setMaximumSize(new Dimension(boxSize, boxSize));
            loadLogo();
        }

        private void loadLogo() {
            try {
                java.net.URL url = LoginUI.class.getResource(resourcePath);
                if (url != null) {
                    logoImage = new ImageIcon(url).getImage();
                }
            } catch (Exception ignored) {
                logoImage = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int arc = 14;
            g2.setColor(PRIMARY);
            g2.fillRoundRect(0, 0, boxSize, boxSize, arc, arc);

            if (logoImage != null) {
                int imgW = logoImage.getWidth(null);
                int imgH = logoImage.getHeight(null);

                if (imgW > 0 && imgH > 0) {
                    double scale = Math.min((double) imageSize / imgW, (double) imageSize / imgH);
                    int drawW = Math.max(1, (int) Math.round(imgW * scale));
                    int drawH = Math.max(1, (int) Math.round(imgH * scale));
                    int x = (boxSize - drawW) / 2;
                    int y = (boxSize - drawH) / 2;

                    g2.drawImage(logoImage, x, y, drawW, drawH, null);
                }
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String fallback = "AL";
                int x = (boxSize - fm.stringWidth(fallback)) / 2;
                int y = (boxSize - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(fallback, x, y);
            }

            g2.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
            setBackground(bg);
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            this.bg = bg;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bg == null ? getBackground() : bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
