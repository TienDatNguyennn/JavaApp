package com.mycompany.myapp.view.screens.finance;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.repository.InvoiceRepository;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

public class InvoiceFormDialog extends JDialog {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG        = new Color(248, 249, 250);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25,  135, 84);
    private static final Color DANGER    = new Color(220, 53,  69);

    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final InvoiceRepository invoiceRepo = new InvoiceRepository();

    private JTextField txtStudentId, txtStudentName, txtStaffId;
    private JTextField txtTotalAmount, txtDiscountAmt, txtAmountPaid;
    private JComboBox<String> cmbPaymentMethod;
    private JLabel lblFinalAmount;

    private final Invoice existingInvoice;
    private boolean confirmed = false;
    private Invoice result;

    public InvoiceFormDialog(Frame parent, Invoice existing) {
        super(parent, existing == null ? "Thêm hóa đơn mới" : "Chỉnh sửa hóa đơn", true);
        this.existingInvoice = existing;
        initUI();
        if (existing != null) fillForm(existing);
        pack();
        setMinimumSize(new Dimension(500, 550));
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildForm(),    BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PRIMARY);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel lbl = new JLabel(existingInvoice == null ? "THÊM HÓA ĐƠN MỚI" : "CHỈNH SỬA HÓA ĐƠN");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JPanel buildForm() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(new EmptyBorder(16, 20, 8, 20));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        int row = 0;
        addRow(card, gbc, row++, "Mã học viên *", txtStudentId = field("Nhập ID..."));
        addRow(card, gbc, row++, "Tên học viên", txtStudentName = field("Tên hiển thị..."));
        txtStudentName.setEditable(false);
        txtStudentName.setBackground(new Color(241, 243, 245));

        addRow(card, gbc, row++, "Mã nhân viên phu trách", txtStaffId = field("ID nhân viên..."));

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addRow(card, gbc, row++, "Học phí gốc (đ) *", txtTotalAmount = field("0"));
        addRow(card, gbc, row++, "Giảm giá (đ)", txtDiscountAmt = field("0"));

        gbc.gridx = 0; gbc.gridy = row;
        card.add(labelMuted("Thực thu (đ)"), gbc);
        gbc.gridx = 1;
        lblFinalAmount = new JLabel("0đ");
        lblFinalAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFinalAmount.setForeground(PRIMARY);
        card.add(lblFinalAmount, gbc);
        row++;

        addRow(card, gbc, row++, "Đã nộp (đ)", txtAmountPaid = field("0"));

        cmbPaymentMethod = new JComboBox<>(new String[]{"Chuyen khoan ngan hang", "Tien mat", "VNPay", "Momo"});
        addRow(card, gbc, row++, "Phương thức", cmbPaymentMethod);

        // Event lookup tên
        txtStudentId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { lookupStudentName(); }
        });

        addAutoCalcListener(txtTotalAmount);
        addAutoCalcListener(txtDiscountAmt);

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private void lookupStudentName() {
        try {
            String idStr = txtStudentId.getText().trim();
            if (idStr.isEmpty()) return;
            int id = Integer.parseInt(idStr);
            String name = invoiceRepo.getStudentNameById(id);
            if (name != null) {
                txtStudentName.setText(name);
                txtStudentName.setForeground(Color.BLACK);
            } else {
                txtStudentName.setText("Học viên không tồn tại!");
                txtStudentName.setForeground(DANGER);
            }
        } catch (Exception e) { txtStudentName.setText("ID sai định dạng!"); }
    }

    private void onSave() {
        try {
            if (txtStudentName.getText().isEmpty() || txtStudentName.getText().contains("không tồn tại")) {
                showError("Vui lòng nhập Mã học viên hợp lệ.");
                return;
            }

            double total = parseDouble(txtTotalAmount.getText());
            double disc = parseDouble(txtDiscountAmt.getText());
            double paid = parseDouble(txtAmountPaid.getText());
            double finalAmt = total - disc;

            if (finalAmt < 0) { showError("Giảm giá không được lớn hơn học phí."); return; }

            result = existingInvoice != null ? existingInvoice : new Invoice();
            result.setStudentId(Integer.parseInt(txtStudentId.getText().trim()));
            result.setStaffId(txtStaffId.getText().isEmpty() ? 0 : Integer.parseInt(txtStaffId.getText().trim()));
            result.setTotalAmount(total);
            result.setDiscountAmt(disc);
            result.setFinalAmount(finalAmt);
            result.setAmountPaid(paid);
            result.setPaymentMethod((String) cmbPaymentMethod.getSelectedItem());
            
            // Tự động set status dựa trên số tiền đã nộp
            result.setStatus(paid >= finalAmt ? "PAID" : "UNPAID");

            confirmed = true;
            dispose();
        } catch (Exception e) {
            showError("Vui lòng kiểm tra lại dữ liệu nhập (Mã số và Tiền).");
        }
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        String clean = s.replaceAll("[^\\d.]", "");
        return clean.isEmpty() ? 0 : Double.parseDouble(clean);
    }

    private void addAutoCalcListener(JTextField tf) {
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { updateFinalLabel(); }
        });
    }

    private void updateFinalLabel() {
        try {
            double final_ = parseDouble(txtTotalAmount.getText()) - parseDouble(txtDiscountAmt.getText());
            lblFinalAmount.setText(nf.format(final_) + "đ");
            lblFinalAmount.setForeground(final_ < 0 ? DANGER : PRIMARY);
        } catch (Exception ignored) { lblFinalAmount.setText("0đ"); }
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String text, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(labelMuted(text), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private JLabel labelMuted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTE);
        return l;
    }

    private JTextField field(String hint) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(220, 32));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        p.setBackground(BG);
        CustomButton btnCancel = new CustomButton("Hủy");
        btnCancel.addActionListener(e -> dispose());
        CustomButton btnSave = new CustomButton("Lưu hóa đơn");
        btnSave.setColors(SUCCESS, SUCCESS.darker());
        btnSave.addActionListener(e -> onSave());
        p.add(btnCancel); p.add(btnSave);
        return p;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }
    public Invoice getResult() { return result; }

    private void fillForm(Invoice inv) {
        txtStudentId.setText(String.valueOf(inv.getStudentId()));
        txtStudentName.setText(inv.getStudentName());
        txtStaffId.setText(String.valueOf(inv.getStaffId()));
        txtTotalAmount.setText(String.valueOf((long) inv.getTotalAmount()));
        txtDiscountAmt.setText(String.valueOf((long) inv.getDiscountAmt()));
        txtAmountPaid.setText(String.valueOf((long) inv.getAmountPaid()));
        updateFinalLabel();
    }
}