package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class InvoiceIssuePanel extends JPanel {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33, 37, 41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JComboBox<String> cmbIssueInvoice;
    private JComboBox<String> cmbInvoiceType;
    private JTextField txtBuyerName;
    private JTextField txtTaxCode;
    private JTextField txtAddress;
    private JTextField txtEmail;
    private JLabel lblPayload;
    private List<Invoice> paidInvoices;

    private JTextField txtAdjustId;
    private JComboBox<String> cmbAdjustReason;
    private JComboBox<String> cmbAdjustType;
    private JTextArea txtAdjustNote;
    private JLabel lblFoundInvoice;
    private Invoice foundInvoice;

    private DefaultTableModel historyModel;

    public InvoiceIssuePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab(" Phát hành & gửi Gmail ", buildIssueTab());
        tabs.addTab(" Cập nhật hóa đơn ", buildAdjustTab());

        add(tabs, BorderLayout.CENTER);

        loadPaidInvoices();
        loadHistory();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Hóa đơn điện tử");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Phát hành hóa đơn và gửi hóa đơn học phí qua Gmail");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTE);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JPanel buildIssueTab() {
        JPanel page = new JPanel(new BorderLayout(14, 0));
        page.setBackground(BG_PAGE);
        page.setBorder(new EmptyBorder(14, 0, 0, 0));
        page.add(buildIssueForm(), BorderLayout.WEST);
        page.add(buildHistoryCard(), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildIssueForm() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setPreferredSize(new Dimension(380, 0));

        addCardTitle(card, "Tạo yêu cầu phát hành hóa đơn");

        addLabel(card, "Hóa đơn học phí đã thanh toán đủ");
        cmbIssueInvoice = new JComboBox<>();
        cmbIssueInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbIssueInvoice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbIssueInvoice.setAlignmentX(LEFT_ALIGNMENT);
        cmbIssueInvoice.addActionListener(e -> updatePayload());
        card.add(cmbIssueInvoice);
        addGap(card, 10);

        addLabel(card, "Loại hóa đơn");
        cmbInvoiceType = new JComboBox<>(new String[]{
                "Hóa đơn GTGT (VAT 8%)",
                "Hóa đơn bán hàng"
        });
        cmbInvoiceType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbInvoiceType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbInvoiceType.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbInvoiceType);
        addGap(card, 10);

        addLabel(card, "Tên người mua");
        txtBuyerName = styledField("");
        card.add(txtBuyerName);
        addGap(card, 8);

        addLabel(card, "Mã số thuế nếu có");
        txtTaxCode = styledField("");
        card.add(txtTaxCode);
        addGap(card, 8);

        addLabel(card, "Địa chỉ");
        txtAddress = styledField("");
        card.add(txtAddress);
        addGap(card, 8);

        addLabel(card, "Email nhận hóa đơn");
        txtEmail = styledField("");
        card.add(txtEmail);
        addGap(card, 12);

        JLabel lblPre = new JLabel("Dữ liệu gửi sang hệ thống:");
        lblPre.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPre.setForeground(TEXT_MUTE);
        lblPre.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblPre);
        addGap(card, 4);

        lblPayload = new JLabel("<html><font face='Courier New' size='2'>{}</font></html>");
        lblPayload.setBackground(new Color(241, 243, 245));
        lblPayload.setOpaque(true);
        lblPayload.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        lblPayload.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        lblPayload.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblPayload);
        addGap(card, 14);

        CustomButton btnIssue = new CustomButton("Phát hành & gửi Gmail");
        btnIssue.setColors(PRIMARY, PRIMARY.darker());
        btnIssue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIssue.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnIssue.setAlignmentX(LEFT_ALIGNMENT);
        btnIssue.addActionListener(e -> issueInvoiceAndSendEmail());
        card.add(btnIssue);
        addGap(card, 8);

        CustomButton btnOnlyEmail = new CustomButton("Chỉ gửi lại Gmail");
        btnOnlyEmail.setColors(new Color(25, 135, 84), new Color(20, 108, 67));
        btnOnlyEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOnlyEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnOnlyEmail.setAlignmentX(LEFT_ALIGNMENT);
        btnOnlyEmail.addActionListener(e -> sendInvoiceEmailOnly());
        card.add(btnOnlyEmail);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Trạng thái phát hành và gửi Gmail");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
                new String[]{"Mã HĐ", "Học viên", "Số tiền", "API Status", "Thời gian"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tbl = new JTable(historyModel);
        styleTable(tbl);
        tbl.getColumnModel().getColumn(3).setCellRenderer(apiStatusRenderer());
        card.add(new JScrollPane(tbl), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildAdjustTab() {
        JPanel page = new JPanel(new BorderLayout(14, 0));
        page.setBackground(BG_PAGE);
        page.setBorder(new EmptyBorder(14, 0, 0, 0));
        page.add(buildAdjustForm(), BorderLayout.WEST);
        page.add(buildAdjustInfo(), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildAdjustForm() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setPreferredSize(new Dimension(360, 0));

        addCardTitle(card, "Tìm hóa đơn cần điều chỉnh");

        addLabel(card, "Mã hóa đơn");
        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchRow.setAlignmentX(LEFT_ALIGNMENT);

        txtAdjustId = styledField("");
        CustomButton btnFind = new CustomButton("Tìm");
        btnFind.setColors(new Color(52, 58, 64), new Color(33, 37, 41));
        btnFind.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFind.setPreferredSize(new Dimension(60, 34));
        btnFind.addActionListener(e -> findInvoice());

        searchRow.add(txtAdjustId, BorderLayout.CENTER);
        searchRow.add(btnFind, BorderLayout.EAST);
        card.add(searchRow);
        addGap(card, 10);

        lblFoundInvoice = new JLabel("<html><i style='color:gray'>Chưa tìm hóa đơn nào</i></html>");
        lblFoundInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFoundInvoice.setBackground(new Color(241, 243, 245));
        lblFoundInvoice.setOpaque(true);
        lblFoundInvoice.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        lblFoundInvoice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        lblFoundInvoice.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblFoundInvoice);
        addGap(card, 12);

        addLabel(card, "Lý do điều chỉnh");
        cmbAdjustReason = new JComboBox<>(new String[]{
                "Sai thông tin người mua",
                "Sai số tiền",
                "Sai nội dung dịch vụ",
                "Hủy hóa đơn"
        });
        cmbAdjustReason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAdjustReason.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbAdjustReason.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbAdjustReason);
        addGap(card, 8);

        addLabel(card, "Loại điều chỉnh");
        cmbAdjustType = new JComboBox<>(new String[]{
                "Điều chỉnh tăng",
                "Điều chỉnh giảm",
                "Thay thế"
        });
        cmbAdjustType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAdjustType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbAdjustType.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbAdjustType);
        addGap(card, 8);

        addLabel(card, "Ghi chú điều chỉnh");
        txtAdjustNote = new JTextArea(4, 20);
        txtAdjustNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAdjustNote.setLineWrap(true);
        txtAdjustNote.setWrapStyleWord(true);
        txtAdjustNote.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JScrollPane spNote = new JScrollPane(txtAdjustNote);
        spNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        spNote.setAlignmentX(LEFT_ALIGNMENT);
        card.add(spNote);
        addGap(card, 14);

        CustomButton btnAdj = new CustomButton("Gửi yêu cầu điều chỉnh");
        btnAdj.setColors(new Color(253, 126, 20), new Color(210, 100, 10));
        btnAdj.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdj.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnAdj.setAlignmentX(LEFT_ALIGNMENT);
        btnAdj.addActionListener(e -> adjustInvoice());
        card.add(btnAdj);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildAdjustInfo() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Hướng dẫn điều chỉnh hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        JTextArea guide = new JTextArea(
                "QUY TRÌNH ĐIỀU CHỈNH HÓA ĐƠN ĐIỆN TỬ\n" +
                "─────────────────────────────────────\n\n" +
                "1. Tìm hóa đơn cần điều chỉnh theo mã số.\n\n" +
                "2. Kiểm tra trạng thái hóa đơn.\n\n" +
                "3. Chọn lý do và loại điều chỉnh phù hợp.\n\n" +
                "4. Nhập ghi chú rõ ràng để lưu hồ sơ.\n\n" +
                "5. Hệ thống cập nhật trạng thái API.\n\n" +
                "Lưu ý: chức năng phát hành Gmail dùng email ở tab bên trái."
        );
        guide.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        guide.setForeground(TEXT_MUTE);
        guide.setBackground(new Color(248, 249, 250));
        guide.setEditable(false);
        guide.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.add(guide, BorderLayout.CENTER);

        return card;
    }

    private void loadPaidInvoices() {
        paidInvoices = ctrl.searchInvoices(null, "PAID");
        cmbIssueInvoice.removeAllItems();

        if (paidInvoices == null || paidInvoices.isEmpty()) {
            cmbIssueInvoice.addItem("(Không có hóa đơn đã thanh toán đủ)");
            updatePayload();
            return;
        }

        for (Invoice inv : paidInvoices) {
            cmbIssueInvoice.addItem(
                    "INV-" + String.format("%03d", inv.getInvoiceId())
                            + " — " + inv.getStudentName()
                            + " (" + nf.format(inv.getFinalAmount()) + "đ)"
            );
        }

        cmbIssueInvoice.setSelectedIndex(0);
        fillBuyerNameFromSelectedInvoice();
        updatePayload();
    }

    private void fillBuyerNameFromSelectedInvoice() {
        int idx = cmbIssueInvoice.getSelectedIndex();

        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            return;
        }

        Invoice inv = paidInvoices.get(idx);

        if (txtBuyerName != null) {
            txtBuyerName.setText(inv.getStudentName());
        }
    }

    private void updatePayload() {
        int idx = cmbIssueInvoice.getSelectedIndex();

        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            if (lblPayload != null) {
                lblPayload.setText("<html><font face='Courier New' size='2'>{}</font></html>");
            }
            return;
        }

        fillBuyerNameFromSelectedInvoice();

        Invoice inv = paidInvoices.get(idx);

        String json =
                "{ invoice_id: " + inv.getInvoiceId()
                        + ", amount: " + (long) inv.getFinalAmount()
                        + ", buyer: \"" + safeStr(inv.getStudentName()) + "\""
                        + ", status: \"" + safeStr(inv.getStatus()) + "\""
                        + ", api_status: \"" + safeStr(inv.getApiStatus()) + "\" }";

        lblPayload.setText(
                "<html><font face='Courier New' size='2'>"
                        + escapeHtml(json)
                        + "</font></html>"
        );
    }

    private void issueInvoiceAndSendEmail() {
        int idx = cmbIssueInvoice.getSelectedIndex();

        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần phát hành.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String buyer = txtBuyerName.getText().trim();
        String email = txtEmail.getText().trim();

        if (buyer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người mua.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email nhận hóa đơn.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice inv = paidInvoices.get(idx);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn muốn phát hành và gửi hóa đơn qua Gmail?\n\n"
                        + "Hóa đơn: INV-" + String.format("%03d", inv.getInvoiceId()) + "\n"
                        + "Người mua: " + buyer + "\n"
                        + "Email: " + email,
                "Xác nhận gửi hóa đơn",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            String issueResult = ctrl.issueInvoice(inv.getInvoiceId());

            if (!"SUCCESS".equals(issueResult)) {
                JOptionPane.showMessageDialog(this, issueResult, "Lỗi phát hành hóa đơn", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String emailResult = ctrl.sendInvoiceEmail(inv.getInvoiceId(), email);

            if ("SUCCESS".equals(emailResult)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Phát hành và gửi Gmail thành công.\n"
                                + "Hóa đơn: INV-" + String.format("%03d", inv.getInvoiceId()) + "\n"
                                + "Email nhận: " + email,
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                loadPaidInvoices();
                loadHistory();
            } else {
                JOptionPane.showMessageDialog(this, emailResult, "Lỗi gửi Gmail", JOptionPane.ERROR_MESSAGE);
                loadHistory();
            }

        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void sendInvoiceEmailOnly() {
        int idx = cmbIssueInvoice.getSelectedIndex();

        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần gửi lại Gmail.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email nhận hóa đơn.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Invoice inv = paidInvoices.get(idx);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            String result = ctrl.sendInvoiceEmail(inv.getInvoiceId(), email);

            if ("SUCCESS".equals(result)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã gửi lại hóa đơn qua Gmail thành công.\nEmail nhận: " + email,
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadHistory();
            } else {
                JOptionPane.showMessageDialog(this, result, "Lỗi gửi Gmail", JOptionPane.ERROR_MESSAGE);
                loadHistory();
            }

        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void findInvoice() {
        String idStr = txtAdjustId.getText().trim();

        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã hóa đơn.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            foundInvoice = ctrl.getInvoiceById(id);

            if (foundInvoice == null) {
                lblFoundInvoice.setText("<html><font color='red'>Không tìm thấy hóa đơn #" + id + "</font></html>");
            } else {
                lblFoundInvoice.setText(
                        "<html>"
                                + "<b>INV-" + String.format("%03d", foundInvoice.getInvoiceId()) + "</b>"
                                + "&nbsp;&nbsp;|&nbsp;&nbsp;" + foundInvoice.getStudentName()
                                + "<br>Số tiền: " + nf.format(foundInvoice.getFinalAmount()) + "đ"
                                + "&nbsp;&nbsp;|&nbsp;&nbsp;API: <b>" + safeStr(foundInvoice.getApiStatus()) + "</b>"
                                + "</html>"
                );
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã hóa đơn phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adjustInvoice() {
        if (foundInvoice == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn hóa đơn trước.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String note = txtAdjustNote.getText().trim();
        String reason = cmbAdjustReason.getSelectedItem().toString();
        String adjustType = cmbAdjustType.getSelectedItem().toString();

        String result = ctrl.adjustInvoice(
                foundInvoice.getInvoiceId(),
                reason + " — " + adjustType + " — " + note
        );

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đã gửi yêu cầu điều chỉnh thành công.\n"
                            + "Lý do: " + reason + "\n"
                            + "Loại: " + adjustType,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            txtAdjustId.setText("");
            txtAdjustNote.setText("");
            foundInvoice = null;
            lblFoundInvoice.setText("<html><i style='color:gray'>Chưa tìm hóa đơn nào</i></html>");
            loadHistory();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHistory() {
        if (historyModel == null) {
            return;
        }

        historyModel.setRowCount(0);

        List<Invoice> all = ctrl.getAllInvoices();

        if (all == null) {
            return;
        }

        for (Invoice inv : all) {
            String api = safeStr(inv.getApiStatus());

            if ("SENT".equals(api)
                    || "SENDING".equals(api)
                    || "FAILED".equals(api)
                    || "ADJUSTED".equals(api)
                    || "ERROR".equals(api)) {

                historyModel.addRow(new Object[]{
                        "INV-" + String.format("%03d", inv.getInvoiceId()),
                        inv.getStudentName(),
                        nf.format(inv.getFinalAmount()) + "đ",
                        api,
                        inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : "—"
                });
            }
        }
    }

    private String safeStr(String s) {
        return s == null ? "—" : s;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void addCardTitle(JPanel card, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(TEXT_MAIN);
        l.setAlignmentX(LEFT_ALIGNMENT);
        card.add(l);
        addGap(card, 14);
    }

    private void addLabel(JPanel card, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        card.add(l);
        addGap(card, 4);
    }

    private void addGap(JPanel card, int h) {
        card.add(Box.createVerticalStrut(h));
    }

    private JTextField styledField(String value) {
        JTextField f = new JTextField(value);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return f;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(241, 243, 245));
        t.getTableHeader().setForeground(TEXT_MUTE);
        t.setGridColor(new Color(233, 236, 239));
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(232, 228, 252));
    }

    private DefaultTableCellRenderer apiStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t,
                    Object v,
                    boolean sel,
                    boolean foc,
                    int row,
                    int col
            ) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);

                setHorizontalAlignment(CENTER);
                setOpaque(true);

                String s = v == null ? "—" : v.toString();

                switch (s) {
                    case "SENT":
                        setBackground(new Color(212, 237, 218));
                        setForeground(new Color(21, 87, 36));
                        setText("Đã gửi Gmail");
                        break;

                    case "SENDING":
                        setBackground(new Color(255, 243, 205));
                        setForeground(new Color(102, 77, 3));
                        setText("Đang gửi");
                        break;

                    case "FAILED":
                        setBackground(new Color(248, 215, 218));
                        setForeground(new Color(114, 28, 36));
                        setText("Gửi lỗi");
                        break;

                    case "ADJUSTED":
                        setBackground(new Color(207, 226, 255));
                        setForeground(new Color(13, 71, 161));
                        setText("Đã điều chỉnh");
                        break;

                    case "ERROR":
                        setBackground(new Color(248, 215, 218));
                        setForeground(new Color(114, 28, 36));
                        setText("Lỗi API");
                        break;

                    default:
                        setBackground(new Color(241, 243, 245));
                        setForeground(TEXT_MUTE);
                        setText("Chờ xử lý");
                        break;
                }

                if (sel) {
                    setBackground(getBackground().darker());
                }

                return this;
            }
        };
    }
}