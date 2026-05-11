package com.mycompany.myapp.view.screens; // Đổi lại cho đúng package của bạn

import com.mycompany.myapp.model.SubjectDTO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Kế thừa JDialog để tạo cửa sổ Pop-up nổi lên trên MainFrame
public class SubjectDialog extends JDialog {

    private JTextField txtSubjectName;
    private JTextArea txtDescription;
    private JButton btnSave;
    private JButton btnCancel;
    
    private SubjectDTO subjectData; // Chứa dữ liệu nếu là chế độ Sửa
    private boolean isSaved = false; // Cờ kiểm tra xem người dùng có bấm Lưu không

    // Constructor dùng chung cho cả Thêm và Sửa
    public SubjectDialog(Window parent, String title, SubjectDTO data) {
        super(parent, title, ModalityType.APPLICATION_MODAL); // Bắt buộc thao tác xong mới được click ra ngoài
        this.subjectData = data;
        
        initComponents();
        if (subjectData != null) {
            loadDataToForm(); // Nếu có truyền data vào -> Đây là chế độ Sửa
        }
    }

    private void initComponents() {
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Label và Input Tên Môn
        JLabel lblName = new JLabel("Tên môn học (*):");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtSubjectName = new JTextField();
        txtSubjectName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtSubjectName.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Label và Input Mô tả
        JLabel lblDesc = new JLabel("Mô tả chi tiết:");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlMain.add(lblName);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlMain.add(txtSubjectName);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlMain.add(lblDesc);
        pnlMain.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlMain.add(scrollDesc);

        // 3. Panel Nút bấm (Lưu & Hủy)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(Color.WHITE);
        
        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose()); // Tắt Pop-up
        
        btnSave = new JButton("Lưu thông tin");
        btnSave.setBackground(new Color(37, 99, 235)); // Xanh dương
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setOpaque(true); 
        btnSave.setBorderPainted(false);
        btnSave.addActionListener(e -> saveAction());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadDataToForm() {
        txtSubjectName.setText(subjectData.getSubjectName());
        txtDescription.setText(subjectData.getDescription());
    }

    private void saveAction() {
        // Validate sương sương
        if (txtSubjectName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên môn học không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Đóng gói dữ liệu lại vào DTO
        if (subjectData == null) {
            subjectData = new SubjectDTO();
            subjectData.setStatus("Đang giảng dạy"); // Mặc định khi thêm mới
        }
        subjectData.setSubjectName(txtSubjectName.getText().trim());
        subjectData.setDescription(txtDescription.getText().trim());
        
        isSaved = true; // Bật cờ thành công
        dispose(); // Đóng Pop-up
    }

    // Hàm public để panel bên ngoài gọi vào lấy dữ liệu
    public SubjectDTO getSubjectData() {
        return subjectData;
    }

    public boolean isSaved() {
        return isSaved;
    }
}