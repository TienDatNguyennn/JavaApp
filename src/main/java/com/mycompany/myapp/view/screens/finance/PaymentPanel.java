package com.mycompany.myapp.view.screens.finance;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Ghi nhận thanh toán học phí
 * Nhân viên kế toán cập nhật số tiền học viên đã nộp.
 */
public class PaymentPanel extends JPanel {

    // ── Màu sắc theo theme project ──
    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33,  37,  41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25, 135, 84);
    private static final Color DANGER    = new Color(220, 53, 69);
    private static final Color WARNING   = new Color(255, 193, 7);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Form fields
    private JTextField   txtSearch, txtAmount, txtTxnId;
    private JComboBox<String> cmbMethod;
    private JSpinner     spnDate;

    // State
    private Invoice      selectedInvoice;

    // UI labels
    private JLabel lblStudentInfo;
    private JPanel pnlStudentCard;

    // Table lịch sử
    private DefaultTableModel tableModel;
    private JTable            tblRecent;

    public PaymentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        loadRecentTable();
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Ghi nhận thanh toán");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Cập nhật học phí và xác nhận thanh toán của học viên");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTE);

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);
        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(3));
        textBlock.add(sub);
        p.add(textBlock, BorderLayout.WEST);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    // BODY — 2 cột: Form bên trái, Lịch sử bên phải
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);
        body.add(buildFormCard());
        body.add(buildHistoryCard());
        return body;
    }

    // ── Card Form ──────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = createCard("Cập nhật thanh toán");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        // Tiêu đề card
        JLabel cardTitle = new JLabel("Cập nhật thanh toán");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTitle.setForeground(TEXT_MAIN);
        cardTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(14));

        // Row tìm kiếm học viên
        card.add(label("Tìm học viên (Tên / Mã HV)"));
        card.add(Box.createVerticalStrut(4));
        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        txtSearch = styledField("Nhập tên hoặc mã học viên...");
        CustomButton btnSearch = new CustomButton("Tìm");
        btnSearch.setColors(PRIMARY, PRIMARY.darker());
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setPreferredSize(new Dimension(70, 34));
        btnSearch.addActionListener(e -> searchStudent());
        searchRow.add(txtSearch,  BorderLayout.CENTER);
        searchRow.add(btnSearch,  BorderLayout.EAST);
        card.add(searchRow);
        card.add(Box.createVerticalStrut(10));

        // Card thông tin học viên (ẩn mặc định)
        pnlStudentCard = new JPanel(new BorderLayout());
        pnlStudentCard.setBackground(new Color(240, 237, 255));
        pnlStudentCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(180, 170, 240), 1, true),
            new EmptyBorder(10, 12, 10, 12)));
        pnlStudentCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        pnlStudentCard.setAlignmentX(LEFT_ALIGNMENT);
        lblStudentInfo = new JLabel("Chưa chọn học viên");
        lblStudentInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStudentInfo.setForeground(PRIMARY.darker());
        pnlStudentCard.add(lblStudentInfo, BorderLayout.CENTER);
        pnlStudentCard.setVisible(false);
        card.add(pnlStudentCard);
        card.add(Box.createVerticalStrut(10));

        // Số tiền
        card.add(label("Số tiền thanh toán (đ)"));
        card.add(Box.createVerticalStrut(4));
        txtAmount = styledField("0");
        txtAmount.setAlignmentX(LEFT_ALIGNMENT);
        card.add(txtAmount);
        card.add(Box.createVerticalStrut(10));

        // Phương thức
        card.add(label("Phương thức thanh toán"));
        card.add(Box.createVerticalStrut(4));
        cmbMethod = new JComboBox<>(new String[]{
            "Chuyển khoản ngân hàng", "Tiền mặt", "VNPay", "Momo"
        });
        cmbMethod.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbMethod.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbMethod);
        card.add(Box.createVerticalStrut(10));

        // Mã giao dịch
        card.add(label("Mã giao dịch"));
        card.add(Box.createVerticalStrut(4));
        txtTxnId = styledField("TXN-...");
        txtTxnId.setAlignmentX(LEFT_ALIGNMENT);
        card.add(txtTxnId);
        card.add(Box.createVerticalStrut(16));

        // Nút xác nhận
        CustomButton btnConfirm = new CustomButton("✔  Xác nhận thanh toán");
        btnConfirm.setColors(SUCCESS, SUCCESS.darker());
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnConfirm.setAlignmentX(LEFT_ALIGNMENT);
        btnConfirm.addActionListener(e -> confirmPayment());
        card.add(btnConfirm);

        card.add(Box.createVerticalGlue());
        return card;
    }

    // ── Card Lịch sử ───────────────────────────────────────────────
    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        JLabel title = new JLabel("Lịch sử thanh toán gần đây");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new String[]{"Học viên", "Số tiền", "Phương thức", "Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRecent = buildTable(tableModel);

        // Renderer màu cột Trạng thái
        tblRecent.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v == null ? "" : v.toString();
                l.setOpaque(true);
                if ("PAID".equals(s))    { l.setBackground(new Color(212,237,218)); l.setForeground(new Color(21,87,36)); l.setText("Đã đủ"); }
                else if ("PARTIAL".equals(s)) { l.setBackground(new Color(255,243,205)); l.setForeground(new Color(133,100,4)); l.setText("Còn nợ"); }
                else                     { l.setBackground(new Color(248,215,218)); l.setForeground(new Color(114,28,36));  l.setText("Chưa nộp"); }
                if (sel) l.setBackground(l.getBackground().darker());
                return l;
            }
        });

        card.add(new JScrollPane(tblRecent), BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void searchStudent() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên hoặc mã học viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Invoice> results = ctrl.searchInvoices(kw, null);
        if (results.isEmpty()) {
            pnlStudentCard.setVisible(false);
            selectedInvoice = null;
            JOptionPane.showMessageDialog(this, "Không tìm thấy học viên \"" + kw + "\".", "Không tìm thấy", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        selectedInvoice = results.get(0);
        double debt = selectedInvoice.getDebtAmount();
        lblStudentInfo.setText("<html><b>" + selectedInvoice.getStudentName() + "</b>"
            + "  &nbsp;|&nbsp; Học phí: " + nf.format(selectedInvoice.getFinalAmount()) + "đ"
            + "  &nbsp;|&nbsp; Còn nợ: <font color='#dc3545'><b>" + nf.format(debt) + "đ</b></font></html>");
        pnlStudentCard.setVisible(true);
        revalidate(); repaint();
    }

    private void confirmPayment() {
        if (selectedInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn học viên trước.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String amtText = txtAmount.getText().trim().replace(",", "").replace(".", "");
        double amount;
        try {
            amount = Double.parseDouble(amtText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String method = (String) cmbMethod.getSelectedItem();
        String result = ctrl.recordPayment(
            selectedInvoice.getInvoiceId(), amount,
            selectedInvoice.getFinalAmount(), method);

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                "✔ Ghi nhận thanh toán thành công!\n"
                + "Học viên: " + selectedInvoice.getStudentName() + "\n"
                + "Số tiền: " + nf.format(amount) + "đ",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            // Reset form
            txtAmount.setText("");
            txtTxnId.setText("");
            txtSearch.setText("");
            pnlStudentCard.setVisible(false);
            selectedInvoice = null;
            loadRecentTable();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRecentTable() {
        tableModel.setRowCount(0);
        List<Invoice> list = ctrl.getAllInvoices();
        for (Invoice inv : list) {
            tableModel.addRow(new Object[]{
                inv.getStudentName(),
                nf.format(inv.getAmountPaid()) + "đ",
                inv.getPaymentMethod() == null ? "—" : inv.getPaymentMethod(),
                inv.getStatus()
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════════════
    private JPanel createCard(String titleText) {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder) {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() || getText().equals(placeholder)) {
                    g.setColor(Color.LIGHT_GRAY);
                }
            }
        };
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(241, 243, 245));
        t.getTableHeader().setForeground(TEXT_MUTE);
        t.setGridColor(new Color(233, 236, 239));
        t.setSelectionBackground(new Color(232, 228, 252));
        t.setSelectionForeground(TEXT_MAIN);
        t.setShowVerticalLines(false);
        return t;
    }
}
