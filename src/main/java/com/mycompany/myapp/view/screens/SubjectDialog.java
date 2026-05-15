package com.mycompany.myapp.view.screens; // NHỚ ĐỔI PACKAGE

import com.mycompany.myapp.model.SubjectDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SubjectDialog extends JDialog {

    private JTextField txtSubjectName;
    private JTextArea txtDescription;
    private JComboBox<String> cbxStatus; // Đã thêm ComboBox trạng thái
    private JButton btnSave;
    private JButton btnCancel;
    
    private SubjectDTO subjectData;
    private boolean isSaved = false;

    public SubjectDialog(Window parent, String title, SubjectDTO data) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.subjectData = data;
        
        initComponents();
        if (subjectData != null) {
            loadDataToForm();
        }
    }

    private void initComponents() {
        setSize(450, 420); // Tăng chút chiều cao để chứa thêm trường Trạng thái
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Tên Môn
        JLabel lblName = new JLabel("Tên môn học (*):");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtSubjectName = new JTextField();
        txtSubjectName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtSubjectName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Mô tả
        JLabel lblDesc = new JLabel("Mô tả chi tiết:");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 3. Trạng thái (MỚI THÊM)
        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cbxStatus = new JComboBox<>(new String[]{"Đang giảng dạy", "Ngừng đào tạo"});
        cbxStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cbxStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Gắn các thành phần vào Panel
        pnlMain.add(lblName);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlMain.add(txtSubjectName);
        
        pnlMain.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlMain.add(lblDesc);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlMain.add(scrollDesc);
        
        pnlMain.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlMain.add(lblStatus);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlMain.add(cbxStatus);

        // 4. Panel Nút bấm
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(Color.WHITE);
        
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());
        
        btnSave = new JButton("Lưu thông tin");
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm hiệu ứng Hover cho nút Lưu
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(new Color(37, 99, 235).darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSave.setBackground(new Color(37, 99, 235));
            }
        });
        
        btnSave.addActionListener(e -> saveAction());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadDataToForm() {
        txtSubjectName.setText(subjectData.getSubjectName());
        txtDescription.setText(subjectData.getDescription());
        // Load trạng thái cũ lên ComboBox
        if (subjectData.getStatus() != null) {
            cbxStatus.setSelectedItem(subjectData.getStatus());
        }
    }

    private void saveAction() {
        if (txtSubjectName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên môn học không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (subjectData == null) {
            subjectData = new SubjectDTO();
        }
        
        subjectData.setSubjectName(txtSubjectName.getText().trim());
        subjectData.setDescription(txtDescription.getText().trim());
        subjectData.setStatus(cbxStatus.getSelectedItem().toString()); // Lấy trạng thái mới lưu vào DTO
        
        isSaved = true;
        dispose();
    }

    public SubjectDTO getSubjectData() {
        return subjectData;
    }

    public boolean isSaved() {
        return isSaved;
    }
}