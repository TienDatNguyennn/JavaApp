package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.model.PromotionRule;
import com.mycompany.myapp.repository.InvoiceRepository;
import com.mycompany.myapp.repository.PromotionDAO;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
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
    private final PromotionDAO promotionDAO = new PromotionDAO();

    // Danh sách mã giảm giá từ DB, index 0 luôn là "Không áp dụng"
    private List<PromotionRule> activePromos = new ArrayList<>();

    private JTextField   txtStudentId, txtStudentName, txtStaffId;
    private JTextField   txtTotalAmount, txtAmountPaid;
    private JComboBox<String> cmbPromo;       // Chọn mã khuyến mãi
    private JComboBox<String> cmbPaymentMethod;
    private JLabel       lblDiscountInfo;     // Hiển thị % và số tiền giảm
    private JLabel       lblFinalAmount;      // Hiển thị thực thu

    private final Invoice existingInvoice;
    private boolean confirmed = false;
    private Invoice result;

    public InvoiceFormDialog(Frame parent, Invoice existing) {
        super(parent, existing == null ? "Thêm hóa đơn mới" : "Chỉnh sửa hóa đơn", true);
        this.existingInvoice = existing;
        loadPromos();
        initUI();
        if (existing != null) fillForm(existing);
        pack();
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(parent);
    }

    /** Constructor dùng khi tạo HĐ mới từ màn hình tra cứu — pre-fill học viên. */
    public InvoiceFormDialog(Frame parent, int prefilledStudentId, String prefilledStudentName) {
        super(parent, "Tạo hóa đơn mới", true);
        this.existingInvoice = null;
        loadPromos();
        initUI();
        // Pre-fill và khóa mã học viên
        txtStudentId.setText(String.valueOf(prefilledStudentId));
        txtStudentId.setEditable(false);
        txtStudentId.setBackground(new Color(241, 243, 245));
        txtStudentName.setText(prefilledStudentName != null ? prefilledStudentName : "");
        pack();
        setMinimumSize(new Dimension(520, 580));
        setLocationRelativeTo(parent);
    }

    // ─── Load danh sách khuyến mãi ───────────────────────────────────
    private void loadPromos() {
        try {
            activePromos = promotionDAO.findAllActive();
        } catch (Exception e) {
            activePromos = new ArrayList<>();
        }
    }

    // ─── Build UI ────────────────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
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
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(5, 4, 5, 4);

        int row = 0;

        // ── Thông tin học viên ──
        addRow(card, gbc, row++, "Mã học viên *",        txtStudentId   = field("Nhập ID..."));
        addRow(card, gbc, row++, "Tên học viên",         txtStudentName = field("Tên hiển thị..."));
        txtStudentName.setEditable(false);
        txtStudentName.setBackground(new Color(241, 243, 245));

        addRow(card, gbc, row++, "Mã nhân viên phụ trách", txtStaffId = field("ID nhân viên..."));

        // ── Separator ──
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        card.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // ── Học phí ──
        addRow(card, gbc, row++, "Học phí gốc (đ) *", txtTotalAmount = field("0"));

        // ── Mã giảm giá (ComboBox) ──
        cmbPromo = buildPromoCombo();
        addRow(card, gbc, row++, "Mã giảm giá", cmbPromo);

        // ── Dòng info giảm giá ──
        gbc.gridx = 0; gbc.gridy = row;
        card.add(labelMuted("Tiền giảm (đ)"), gbc);
        gbc.gridx = 1;
        lblDiscountInfo = new JLabel("0đ  (0%)");
        lblDiscountInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDiscountInfo.setForeground(new Color(230, 81, 0));
        card.add(lblDiscountInfo, gbc);
        row++;

        // ── Thực thu ──
        gbc.gridx = 0; gbc.gridy = row;
        card.add(labelMuted("Thực thu (đ)"), gbc);
        gbc.gridx = 1;
        lblFinalAmount = new JLabel("0đ");
        lblFinalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFinalAmount.setForeground(PRIMARY);
        card.add(lblFinalAmount, gbc);
        row++;

        // ── Đã nộp & Phương thức ──
        addRow(card, gbc, row++, "Đã nộp (đ)", txtAmountPaid = field("0"));

        cmbPaymentMethod = new JComboBox<>(new String[]{
            "Chuyển khoản ngân hàng", "Tiền mặt", "VNPay", "Momo"});
        addRow(card, gbc, row++, "Phương thức", cmbPaymentMethod);

        // ── Events ──
        txtStudentId.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { lookupStudentName(); }
        });

        // Khi học phí thay đổi → tính lại
        txtTotalAmount.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { recalculate(); }
        });

        // Khi chọn mã KM khác → tính lại
        cmbPromo.addActionListener(e -> recalculate());

        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    /** Tạo ComboBox danh sách mã KM, index 0 = "Không áp dụng" */
    private JComboBox<String> buildPromoCombo() {
        List<String> items = new ArrayList<>();
        items.add("-- Không áp dụng --");
        for (PromotionRule p : activePromos) {
            items.add(p.getPromoName() + " (" + (int) p.getDiscountRate() + "%)");
        }
        JComboBox<String> cb = new JComboBox<>(items.toArray(new String[0]));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(220, 32));
        return cb;
    }

    // ─── Logic tra cứu tên học viên ──────────────────────────────────
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
        } catch (Exception e) {
            txtStudentName.setText("ID sai định dạng!");
        }
    }

    // ─── Tính toán giảm giá và cập nhật label ───────────────────────
    private void recalculate() {
        double total = parseDouble(txtTotalAmount.getText());
        int selectedIdx = cmbPromo.getSelectedIndex();

        double discountAmt = 0;
        double discountRate = 0;

        // index 0 = "Không áp dụng", index >= 1 tương ứng activePromos[idx-1]
        if (selectedIdx > 0 && selectedIdx - 1 < activePromos.size()) {
            PromotionRule promo = activePromos.get(selectedIdx - 1);
            discountRate = promo.getDiscountRate();
            discountAmt  = total * (discountRate / 100.0);
        }

        double finalAmt = total - discountAmt;

        lblDiscountInfo.setText(nf.format(discountAmt) + "đ  (" + (int) discountRate + "%)");
        lblFinalAmount.setText(nf.format(Math.max(0, finalAmt)) + "đ");
        lblFinalAmount.setForeground(finalAmt < 0 ? DANGER : PRIMARY);
    }

    /** Trả về mã KM đang được chọn, hoặc null nếu "Không áp dụng" */
    private PromotionRule getSelectedPromo() {
        int idx = cmbPromo.getSelectedIndex();
        if (idx > 0 && idx - 1 < activePromos.size()) {
            return activePromos.get(idx - 1);
        }
        return null;
    }

    // ─── Lưu hóa đơn ────────────────────────────────────────────────
    private void onSave() {
        try {
            // Tự động tra cứu tên nếu chưa tra (user chưa bấm Tab)
            if (txtStudentName.getText().trim().isEmpty()) {
                lookupStudentName();
            }

            String nameText = txtStudentName.getText().trim();
            if (nameText.isEmpty()
                    || nameText.contains("không tồn tại")
                    || nameText.contains("sai định dạng")
                    || nameText.contains("Học viên không tồn tại")) {
                showError("Vui lòng nhập Mã học viên hợp lệ.");
                return;
            }

            double total = parseDouble(txtTotalAmount.getText());
            if (total < 0) {
                showError("Học phí gốc không được âm.");
                return;
            }

            PromotionRule promo = getSelectedPromo();
            double discountAmt  = 0;
            int    promoId      = 0;

            if (promo != null) {
                discountAmt = total * (promo.getDiscountRate() / 100.0);
                promoId     = promo.getPromoId();
            }

            double finalAmt = total - discountAmt;
            if (finalAmt < 0) {
                showError("Giảm giá không được lớn hơn học phí.");
                return;
            }

            double paid = parseDouble(txtAmountPaid.getText());

            result = existingInvoice != null ? existingInvoice : new Invoice();
            result.setStudentId(Integer.parseInt(txtStudentId.getText().trim()));
            String staffText = txtStaffId.getText().trim();
            if (!staffText.isEmpty()) {
                result.setStaffId(Integer.parseInt(staffText));
            }
            // Nếu để trống staff_id, giữ nguyên giá trị cũ (khi edit) hoặc để 0 (service sẽ validate)
            result.setPromoId(promoId);
            result.setTotalAmount(total);
            result.setDiscountAmt(discountAmt);
            result.setFinalAmount(finalAmt);
            result.setAmountPaid(paid);
            result.setPaymentMethod((String) cmbPaymentMethod.getSelectedItem());
            String status;
            if (finalAmt == 0) {
                status = "PAID";
            } else if (paid >= finalAmt) {
                status = "PAID";
            } else if (paid > 0) {
                status = "PARTIAL";
            } else {
                status = "UNPAID";
            }
            result.setStatus(status);

            confirmed = true;
            dispose();
        } catch (Exception e) {
            showError("Vui lòng kiểm tra lại dữ liệu nhập (Mã số và Tiền).");
        }
    }

    // ─── Điền form khi chỉnh sửa ────────────────────────────────────
    private void fillForm(Invoice inv) {
        txtStudentId.setText(String.valueOf(inv.getStudentId()));
        txtStudentName.setText(inv.getStudentName() != null ? inv.getStudentName() : "");
        txtStaffId.setText(String.valueOf(inv.getStaffId()));
        txtTotalAmount.setText(String.valueOf((long) inv.getTotalAmount()));
        txtAmountPaid.setText(String.valueOf((long) inv.getAmountPaid()));

        // Chọn đúng mục KM trong ComboBox dựa theo promoId
        int storedPromoId = inv.getPromoId();
        cmbPromo.setSelectedIndex(0); // mặc định "Không áp dụng"
        if (storedPromoId > 0) {
            for (int i = 0; i < activePromos.size(); i++) {
                if (activePromos.get(i).getPromoId() == storedPromoId) {
                    cmbPromo.setSelectedIndex(i + 1); // +1 vì index 0 là "Không áp dụng"
                    break;
                }
            }
        }

        recalculate(); // Cập nhật label sau khi đã set đúng giá trị
    }

    // ─── Helpers ────────────────────────────────────────────────────
    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        String clean = s.replaceAll("[^\\d.]", "");
        return clean.isEmpty() ? 0 : Double.parseDouble(clean);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String text, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        panel.add(labelMuted(text), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
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
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
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
        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }
    public Invoice  getResult()  { return result; }
}