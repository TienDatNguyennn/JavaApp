package com.mycompany.myapp.view.screens.QuanLyHeThong;

import com.mycompany.myapp.controller.AccountController;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.view.components.UIComponents.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CreateAccountDialog extends JDialog {
    private AccountController controller;
    private boolean success = false;

    private ModernTextField txtName, txtEmail, txtPhone, txtUsername;
    private ModernPasswordField txtPassword;
    private JComboBox<RoleGroup> cbRole;
    private JComboBox<String> cbStatus;

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_2 = new Color(142, 68, 173);
    private static final Color TEXT_MAIN = new Color(45, 52, 54);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);

    public CreateAccountDialog(JFrame parent, AccountController controller) {
        super(parent, "Cấp phát tài khoản hệ thống", true);
        this.controller = controller;

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        GradientPanel leftPanel = new GradientPanel(PRIMARY, PRIMARY_2);
        leftPanel.setPreferredSize(new Dimension(350, 600));
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBorder(new EmptyBorder(34, 30, 34, 30));

        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));

        LogoPanel logo = new LogoPanel(150, 104);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBrand = new JLabel("ALPHA LOGIC CENTER");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPortal = new JLabel("Internal Portal");
        lblPortal.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblPortal.setForeground(new Color(235, 232, 255));
        lblPortal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel(
                "<html><div style='text-align:center;width:245px;'>"
                        + "Cấp phát tài khoản, phân quyền và quản trị truy cập hệ thống"
                        + "</div></html>"
        );
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(230, 225, 255));
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandBox.add(logo);
        brandBox.add(Box.createVerticalStrut(22));
        brandBox.add(lblBrand);
        brandBox.add(Box.createVerticalStrut(6));
        brandBox.add(lblPortal);
        brandBox.add(Box.createVerticalStrut(18));
        brandBox.add(lblDesc);

        leftPanel.add(brandBox);
        return leftPanel;
    }

    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Khởi Tạo Tài Khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(TEXT_MAIN);

        JPanel formContainer = new JPanel(new GridLayout(2, 1, 0, 20));
        formContainer.setOpaque(false);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 13);
        Color titleColor = PRIMARY;

        JPanel pnlUser = new JPanel(new GridLayout(2, 2, 20, 15));
        pnlUser.setOpaque(false);
        pnlUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        "Thông tin nhân viên",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        titleFont,
                        titleColor
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        txtName = new ModernTextField("Họ và tên (*)");
        txtEmail = new ModernTextField("Email (*)");
        txtPhone = new ModernTextField("Số điện thoại (*)");

        pnlUser.add(txtName);
        pnlUser.add(txtEmail);
        pnlUser.add(txtPhone);
        pnlUser.add(new JLabel(""));

        JPanel pnlAccount = new JPanel(new GridLayout(2, 2, 20, 15));
        pnlAccount.setOpaque(false);
        pnlAccount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220)),
                        "Thông tin truy cập",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        titleFont,
                        titleColor
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        txtUsername = new ModernTextField("Tên đăng nhập (*)");
        txtPassword = new ModernPasswordField("Mật khẩu cấp phát (*)");

        cbRole = new JComboBox<>();
        try {
            for (RoleGroup rg : controller.getAvailableRoles()) {
                cbRole.addItem(rg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cbRole.setBackground(Color.WHITE);
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        cbStatus = new JComboBox<>(new String[]{"ACTIVE", "LOCKED"});
        cbStatus.setBackground(Color.WHITE);
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        pnlAccount.add(txtUsername);
        pnlAccount.add(txtPassword);
        pnlAccount.add(cbRole);
        pnlAccount.add(cbStatus);

        formContainer.add(pnlUser);
        formContainer.add(pnlAccount);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        btnPanel.setOpaque(false);

        GradientButton btnSubmit = new GradientButton("Lưu tài khoản");
        btnSubmit.setPreferredSize(new Dimension(180, 45));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancel.setForeground(TEXT_MUTE);
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> submitForm());

        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);

        rightPanel.add(title);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(formContainer);
        rightPanel.add(Box.createVerticalStrut(35));
        rightPanel.add(btnPanel);

        return rightPanel;
    }

    private void submitForm() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        RoleGroup selectedRole = (RoleGroup) cbRole.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin vào các trường bắt buộc (*).", txtName);
            return;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            showError("Định dạng email không hợp lệ (Ví dụ: nguyenvan.a@domain.com).", txtEmail);
            return;
        }

        String phoneRegex = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$";
        if (!phone.matches(phoneRegex)) {
            showError("Số điện thoại không hợp lệ. Vui lòng nhập đúng số điện thoại di động Việt Nam (10 số).", txtPhone);
            return;
        }

        String usernameRegex = "^[a-zA-Z0-9_]{5,20}$";
        if (!username.matches(usernameRegex)) {
            showError("Tên đăng nhập phải từ 5-20 ký tự, không chứa khoảng trắng hoặc ký tự đặc biệt.", txtUsername);
            return;
        }

        if (password.length() < 6) {
            showError("Mật khẩu cấp phát phải có độ dài tối thiểu 6 ký tự để đảm bảo an toàn.", txtPassword);
            return;
        }

        if (selectedRole == null) {
            showError("Vui lòng chọn một nhóm quyền hợp lệ cho tài khoản này.", cbRole);
            return;
        }

        try {
            controller.handleCreateAccount(
                    name,
                    email,
                    phone,
                    username,
                    password,
                    selectedRole,
                    cbStatus.getSelectedItem().toString()
            );

            success = true;
            JOptionPane.showMessageDialog(
                    this,
                    "Đã cấp phát tài khoản [" + username + "] thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi hệ thống / Dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(String message, JComponent targetComponent) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo dữ liệu", JOptionPane.WARNING_MESSAGE);
        if (targetComponent != null) {
            targetComponent.requestFocus();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private static class LogoPanel extends JPanel {
        private final int boxSize;
        private final int imageSize;
        private Image logoImage;

        LogoPanel(int boxSize, int imageSize) {
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
                java.net.URL url = CreateAccountDialog.class.getResource("/wappgpt_logo.png");
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

            int shadowOffset = 4;
            int cardSize = boxSize - shadowOffset;

            g2.setColor(new Color(32, 16, 92, 46));
            g2.fillRoundRect(shadowOffset, shadowOffset, cardSize, cardSize, 34, 34);

            g2.setColor(new Color(255, 255, 255, 36));
            g2.fillRoundRect(0, 0, cardSize, cardSize, 34, 34);

            if (logoImage != null) {
                int imgW = logoImage.getWidth(null);
                int imgH = logoImage.getHeight(null);

                if (imgW > 0 && imgH > 0) {
                    double scale = Math.min((double) imageSize / imgW, (double) imageSize / imgH);
                    int drawW = Math.max(1, (int) Math.round(imgW * scale));
                    int drawH = Math.max(1, (int) Math.round(imgH * scale));
                    int x = (cardSize - drawW) / 2;
                    int y = (cardSize - drawH) / 2;

                    g2.drawImage(logoImage, x, y, drawW, drawH, null);
                }
            } else {
                String fallback = "AL";
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                int x = (cardSize - fm.stringWidth(fallback)) / 2;
                int y = (cardSize - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(fallback, x, y);
            }

            g2.dispose();
        }
    }
}
