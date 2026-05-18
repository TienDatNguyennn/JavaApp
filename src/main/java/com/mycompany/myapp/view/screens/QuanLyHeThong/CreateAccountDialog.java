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

    public CreateAccountDialog(JFrame parent, AccountController controller) {
        super(parent, "Cấp phát tài khoản hệ thống", true);
        this.controller = controller;
        setSize(900, 600); 
        setLocationRelativeTo(parent); 
        setLayout(new BorderLayout());

        // --- MẢNG TRÁI ---
        GradientPanel leftPanel = new GradientPanel(new Color(108, 92, 231), new Color(142, 68, 173));
        leftPanel.setPreferredSize(new Dimension(350, 600)); 
        leftPanel.setLayout(new GridBagLayout());
        JLabel lblArt = new JLabel("<html><center><h1 style='color:white; font-family:Segoe UI;'>INTERNAL<br>PORTAL</h1><p style='color:#E0E0E0; font-family:Segoe UI;'>Phân Quyền Hệ Thống</p></center></html>");
        leftPanel.add(lblArt);

        // --- MẢNG PHẢI ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE); 
        rightPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Khởi Tạo Tài Khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(new Color(45, 52, 54));

        JPanel formContainer = new JPanel(new GridLayout(2, 1, 0, 20)); 
        formContainer.setOpaque(false);

        // Phương thức tạo viền gom nhóm đẹp
        Font titleFont = new Font("Segoe UI", Font.BOLD, 13);
        Color titleColor = new Color(108, 92, 231);
        
        // Group 1: User
        JPanel pnlUser = new JPanel(new GridLayout(2, 2, 20, 15)); 
        pnlUser.setOpaque(false);
        pnlUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220,220,220)), "Thông tin nhân viên", TitledBorder.LEFT, TitledBorder.TOP, titleFont, titleColor),
            new EmptyBorder(10, 15, 15, 15)
        ));
        txtName = new ModernTextField("Họ và tên (*)"); 
        txtEmail = new ModernTextField("Email (*)"); 
        txtPhone = new ModernTextField("Số điện thoại (*)");
        pnlUser.add(txtName); 
        pnlUser.add(txtEmail); 
        pnlUser.add(txtPhone); 
        pnlUser.add(new JLabel("")); // Dummy để lấp chỗ trống

        // Group 2: Account
        JPanel pnlAccount = new JPanel(new GridLayout(2, 2, 20, 15)); 
        pnlAccount.setOpaque(false);
        pnlAccount.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220,220,220)), "Thông tin truy cập", TitledBorder.LEFT, TitledBorder.TOP, titleFont, titleColor),
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
        btnCancel.setContentAreaFilled(false); 
        btnCancel.setBorderPainted(false); 
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
        
        add(leftPanel, BorderLayout.WEST); 
        add(rightPanel, BorderLayout.CENTER);
    }

    private void submitForm() {
        // Lấy và chuẩn hóa dữ liệu đầu vào (loại bỏ khoảng trắng thừa ở 2 đầu)
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        RoleGroup selectedRole = (RoleGroup) cbRole.getSelectedItem();

        // 1. Kiểm tra bỏ trống toàn bộ các trường bắt buộc
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin vào các trường bắt buộc (*).", txtName);
            return;
        }

        // 2. Kiểm tra định dạng Email chuẩn thực tế
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            showError("Định dạng email không hợp lệ (Ví dụ: nguyenvan.a@domain.com).", txtEmail);
            return;
        }

        // 3. Kiểm tra định dạng Số điện thoại (Chuẩn Việt Nam)
        String phoneRegex = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$";
        if (!phone.matches(phoneRegex)) {
            showError("Số điện thoại không hợp lệ. Vui lòng nhập đúng số điện thoại di động Việt Nam (10 số).", txtPhone);
            return;
        }

        // 4. Kiểm tra Tên đăng nhập (Chỉ cho phép chữ cái, số, dấu gạch dưới và phải từ 5-20 ký tự)
        String usernameRegex = "^[a-zA-Z0-9_]{5,20}$";
        if (!username.matches(usernameRegex)) {
            showError("Tên đăng nhập phải từ 5-20 ký tự, không chứa khoảng trắng hoặc ký tự đặc biệt.", txtUsername);
            return;
        }

        // 5. Kiểm tra Mật khẩu
        if (password.length() < 6) {
            showError("Mật khẩu cấp phát phải có độ dài tối thiểu 6 ký tự để đảm bảo an toàn.", txtPassword);
            return;
        }

        // 6. Kiểm tra quyền (Role)
        if (selectedRole == null) {
            showError("Vui lòng chọn một nhóm quyền hợp lệ cho tài khoản này.", cbRole);
            return;
        }

        // Vượt qua toàn bộ validation -> Bắt đầu đẩy xuống Controller
        try {
            controller.handleCreateAccount(
                name, email, phone, username, password, selectedRole, cbStatus.getSelectedItem().toString()
            );
            
            success = true;
            JOptionPane.showMessageDialog(this, "Đã cấp phát tài khoản [" + username + "] thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi hệ thống / Dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Hàm hỗ trợ hiển thị lỗi và focus con trỏ chuột về đúng component bị lỗi
     * Giúp tăng trải nghiệm người dùng (UX)
     */
    private void showError(String message, JComponent targetComponent) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo dữ liệu", JOptionPane.WARNING_MESSAGE);
        if (targetComponent != null) {
            targetComponent.requestFocus();
        }
    }

    public boolean isSuccess() { 
        return success; 
    }
}