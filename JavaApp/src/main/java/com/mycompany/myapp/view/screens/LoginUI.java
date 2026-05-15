/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.myapp.view.screens;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.view.screens.MainFrame;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.regex.Pattern;
import com.mycompany.myapp.controller.LoginController;
public class LoginUI extends JFrame {
    private LoginController controller;

    public LoginUI() {
        // Thêm dòng này: Khởi tạo Controller
        controller = new LoginController();
        initUI();
    }


    private void initUI() {
        setTitle("Giao diện Đăng Nhập - Chuẩn UI");
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

        JLabel lblIcon = new JLabel("🪄");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblIcon.setForeground(Color.WHITE);
        
        JLabel lblInfo = new JLabel("<html>Find 3D Objects, Mockups and<br>Illustrations here.</html>");
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
                imgLabel.setText("<html><div style='text-align:center;'><span style='font-size:50px'>🎨</span><br>3D Illustration</div></html>");
                imgLabel.setForeground(Color.WHITE);
                imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                imgLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            }
        } catch (Exception e) {
            imgLabel.setText("3D Illustration");
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
        JLabel lblLang = new JLabel("English (UK) ▼");
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

        // --- 3. Social Buttons ---
        JPanel socialRow = new JPanel(new GridLayout(1, 2, 15, 0));
        socialRow.setBackground(Color.WHITE);
        socialRow.setMaximumSize(new Dimension(415, 40));
        socialRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton btnGoogle = new RoundedButton("", Color.WHITE, Color.BLACK);
        styleGoogleButton(btnGoogle); // Áp dụng method style riêng

        RoundedButton btnFacebook = new RoundedButton("", Color.WHITE, Color.BLACK);
        styleFacebookButton(btnFacebook); // Áp dụng method style riêng

        socialRow.add(btnGoogle);
        socialRow.add(btnFacebook);
        form.add(socialRow);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        // --- 4. Divider ---
        form.add(createDivider("- HOẶC -"));
        form.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- 5. Input: Email ---
        form.add(createInputLabel("User Name"));
        PlaceholderTextField txtEmail = new PlaceholderTextField("");
        form.add(txtEmail);
        form.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- 6. Input: Password ---
        form.add(createInputLabel("Password"));
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

        // --- 7. Login Button (Áp dụng style động) ---
        RoundedButton btnLogin = new RoundedButton("Đăng nhập", Color.decode("#8B5CF6"), Color.WHITE);
        btnLogin.setColors(Color.decode("#8B5CF6"), Color.decode("#7C3AED"), Color.decode("#6D28D9"));
        btnLogin.setBorderColors(Color.decode("#8B5CF6"), Color.decode("#7C3AED"));
        btnLogin.setRadius(35);
        btnLogin.setMaximumSize(new Dimension(415, 45));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);
        form.add(Box.createRigidArea(new Dimension(0, 25)));

        // --- 8. Footer ---
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

        // ==========================================
        // EVENTS
        // ==========================================
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
            // 1. Lấy dữ liệu từ UI
            String username = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());

            // 2. Chuyển giao toàn bộ trách nhiệm xử lý logic, check DB, tạo JWT cho Controller
            controller.handleLogin(username, pass, this);
        });

        lblRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginUI.this, "Chuyển hướng sang giao diện Đăng Ký...");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                lblRegister.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#8B5CF6")));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblRegister.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
            }
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
        // Tham số: bg, hover, pressed
        btn.setColors(Color.decode("#FFFFFF"), Color.decode("#F9FAFB"), Color.decode("#F3F4F6"));
        // Tham số: borderColor, hoverBorderColor
        btn.setBorderColors(Color.decode("#E5E7EB"), Color.decode("#D1D5DB"));
        btn.setRadius(35);
    }

    private void styleFacebookButton(RoundedButton btn) {
        btn.setText("<html><b>f</b> Đăng nhập Facebook</html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        // Tham số: bg, hover, pressed
        btn.setColors(Color.decode("#1877F2"), Color.decode("#166FE5"), Color.decode("#145DD1"));
        // Tham số: borderColor, hoverBorderColor
        btn.setBorderColors(Color.decode("#1877F2"), Color.decode("#166FE5"));
        btn.setRadius(35);
    }

    // ==========================================
    // UI HELPERS & CUSTOM COMPONENTS
    // ==========================================
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

    // Class RoundedButton đã được nâng cấp để hỗ trợ State Color (Hover/Pressed)
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
            setBorder(new EmptyBorder(5, 15, 5, 15)); // Padding chuẩn thay vì dùng Border object

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

        public void setColors(Color bg, Color hoverBg, Color pressedBg) {
            this.bg = bg;
            this.hoverBg = hoverBg;
            this.pressedBg = pressedBg;
            setBackground(bg);
        }

        public void setBorderColors(Color border, Color hoverBorder) {
            this.borderColor = border;
            this.hoverBorderColor = hoverBorder;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ Background theo State
            if (isPressed) g2.setColor(pressedBg);
            else if (isHovered) g2.setColor(hoverBg);
            else g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            
            // Vẽ Border theo State
            if (borderColor != null) {
                if (isHovered && !isPressed && hoverBorderColor != null) {
                    g2.setColor(hoverBorderColor);
                } else {
                    g2.setColor(borderColor);
                }
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedBorder implements Border {
        private Color c; int t, r;
        public RoundedBorder(Color c, int t, int r) { this.c = c; this.t = t; this.r = r; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            g2.setStroke(new BasicStroke(t));
            g2.drawRoundRect(x, y, w-1, h-1, r, r);
        }
        public Insets getBorderInsets(Component c) { return new Insets(5, 15, 5, 15); }
        public boolean isBorderOpaque() { return false; }
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

    // Trong file LoginUI.java
public void onLoginSuccess() {
    // 1. Lấy token đã lưu trong SessionStore
    String token = com.mycompany.myapp.utils.SessionStore.getCurrentToken();
    
    // 2. Lấy tên người dùng từ token thông qua TokenService
    com.mycompany.myapp.utils.TokenService tokenService = new com.mycompany.myapp.utils.TokenService();
    String name = tokenService.getFullNameFromToken(token);
    
    // 3. Hiển thị thông báo và chuyển trang
    JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    this.dispose(); 
    
    // 4. Mở màn hình chính và truyền tên vào
    new MainFrame().setVisible(true); 
}
    
    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}