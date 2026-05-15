package com.mycompany.myapp.view.screens.teacher;

import com.mycompany.myapp.view.components.UIKit;
import com.mycompany.myapp.view.components.RoundedPanel;
import com.mycompany.myapp.model.AccountUpdateDTO;
import com.mycompany.myapp.service.AccountService;
import com.mycompany.myapp.exception.DuplicateDataException;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TeacherProfilePanel extends JPanel {
    
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtIdentityCard; 
    private JTextField txtMajor;
    private JTextField txtDegree;
    
    private Border defaultBorder;
    private AccountService accountService;
    private int currentAccountId;
    private int currentUserId;

    public TeacherProfilePanel() {
        accountService = new AccountService();
        this.currentAccountId = SessionStore.getAccountId(); 
        
        initComponents();
        loadProfileData(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 249)); 
        setBorder(new EmptyBorder(30, 40, 30, 40)); 

        RoundedPanel formPanel = new RoundedPanel(20);
        formPanel.setBackground(Color.WHITE); 
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 20, 12, 20); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // XÓA BỎ KHUNG AVATAR Ở ĐÂY, ĐẨY FORM SANG CỘT 0

        // ==========================================
        // KHỞI TẠO FORM FIELDS
        // ==========================================
        txtFullName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtIdentityCard = new JTextField();
        txtMajor = new JTextField();
        txtDegree = new JTextField();
        
        defaultBorder = txtFullName.getBorder();

        gbc.gridheight = 1; 
        gbc.weightx = 0.7; // Chiếm 70% không gian để form không bị giãn quá đà
        
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(createInput("Họ và Tên (*)", txtFullName), gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(createInput("Email (*)", txtEmail), gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(createInput("Số Điện Thoại (*)", txtPhone), gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(createInput("Căn Cước Công Dân (*)", txtIdentityCard), gbc);
        
        Color disabledBg = new Color(243, 244, 246);
        Color disabledText = new Color(107, 114, 128);
        
        txtMajor.setEditable(false); txtMajor.setBackground(disabledBg); txtMajor.setForeground(disabledText);
        txtDegree.setEditable(false); txtDegree.setBackground(disabledBg); txtDegree.setForeground(disabledText);
        
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(createInput("Chuyên Môn (Admin quản lý)", txtMajor), gbc);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(createInput("Bằng Cấp (Admin quản lý)", txtDegree), gbc);

        // NÚT LƯU
        JButton btnSave = new JButton("Cập Nhật Hồ Sơ");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(99, 102, 241)); 
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setOpaque(true); 
        btnSave.setBorderPainted(false); 
        btnSave.setPreferredSize(new Dimension(180, 45));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridx = 0; gbc.gridy = 6; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.insets = new Insets(25, 20, 20, 20); 
        formPanel.add(btnSave, gbc);

        // Cột giả (Dummy Column) ở bên phải để chống giãn form
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.3; formPanel.add(Box.createGlue(), gbc); 

        btnSave.addActionListener((ActionEvent e) -> handleUpdateProfile());

        // Bọc form vào một FlowLayout để căn lề trái mượt mà
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapperPanel.setOpaque(false); wrapperPanel.add(formPanel);

        add(wrapperPanel, BorderLayout.CENTER);
    }

    private JPanel createInput(String label, JTextField txtField) {
        JPanel p = new JPanel(new BorderLayout(0, 8)); 
        p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI", Font.BOLD, 13)); lbl.setForeground(new Color(55, 65, 81)); 
        txtField.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtField.setPreferredSize(new Dimension(450, 40)); 
        txtField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        p.add(lbl, BorderLayout.NORTH); p.add(txtField, BorderLayout.CENTER);
        return p;
    }

    private void loadProfileData() {
        if (currentAccountId <= 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Result<AccountUpdateDTO> result = accountService.getAccountInfo(currentAccountId);
        if (result.isSuccess()) {
            AccountUpdateDTO data = result.getData();
            currentUserId = data.getUserId(); 
            
            txtFullName.setText(data.getFullName());
            txtEmail.setText(data.getEmail());
            txtPhone.setText(data.getPhone());
            txtIdentityCard.setText(data.getIdentityCard() != null ? data.getIdentityCard() : "");
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateProfile() {
        resetBorders();

        AccountUpdateDTO dto = new AccountUpdateDTO();
        dto.setAccountId(currentAccountId);
        dto.setUserId(currentUserId);
        dto.setFullName(txtFullName.getText().trim());
        dto.setEmail(txtEmail.getText().trim());
        dto.setPhone(txtPhone.getText().trim());
        dto.setIdentityCard(txtIdentityCard.getText().trim());

        try {
            Result<Void> result = accountService.updateProfile(dto);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(), "Thất bại", JOptionPane.ERROR_MESSAGE);
            }
        } catch (DuplicateDataException ex) {
            highlightErrorField(ex.getErrorField());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu trùng lặp", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void highlightErrorField(String fieldName) {
        Border errorBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(239, 68, 68), 2), BorderFactory.createEmptyBorder(5, 10, 5, 10));
        switch (fieldName) {
            case "email": txtEmail.setBorder(errorBorder); txtEmail.requestFocus(); break;
            case "phone": txtPhone.setBorder(errorBorder); txtPhone.requestFocus(); break;
            case "identityCard": txtIdentityCard.setBorder(errorBorder); txtIdentityCard.requestFocus(); break;
        }
    }

    private void resetBorders() {
        Border normalBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)), BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtFullName.setBorder(normalBorder); txtEmail.setBorder(normalBorder); txtPhone.setBorder(normalBorder); txtIdentityCard.setBorder(normalBorder);
    }
}