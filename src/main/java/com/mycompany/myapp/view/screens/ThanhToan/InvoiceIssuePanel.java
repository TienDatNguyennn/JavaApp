package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.util.EmailService;
import com.mycompany.myapp.util.InvoicePdfGenerator;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel ghép 2 chức năng:
 *   - Tab 1: Phát hành hóa đơn điện tử (gửi sang hệ thống HĐ ĐT)
 *   - Tab 2: Cập nhật / điều chỉnh hóa đơn đã phát hành
 */
public class InvoiceIssuePanel extends JPanel {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33,  37,  41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25,  135, 84);
    private static final Color DANGER    = new Color(220, 53,  69);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ── Tab 1: Phát hành ──
    private JComboBox<String> cmbIssueInvoice;
    private JComboBox<String> cmbInvoiceType;
    private JTextField        txtBuyerName, txtTaxCode, txtAddress, txtEmail;
    private JLabel            lblPayload;
    private List<Invoice>     paidInvoices;

    // ── Tab 2: Điều chỉnh ──
    private JTextField        txtAdjustId;
    private JComboBox<String> cmbAdjustReason, cmbAdjustType;
    private JTextArea         txtAdjustNote;
    private JLabel            lblFoundInvoice;
    private Invoice           foundInvoice;

    // ── Bảng trạng thái phát hành ──
    private DefaultTableModel historyModel;
    private JTable            tblHistory;

    public InvoiceIssuePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab(" Phát hành hóa đơn  ", buildIssueTab());
        tabs.addTab(" Cập nhật hóa đơn  ",  buildAdjustTab());
        add(tabs, BorderLayout.CENTER);

        loadPaidInvoices();
        loadHistory();
    }

    // ── Header ──────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Hóa đơn điện tử");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Phát hành và điều chỉnh hóa đơn qua hệ thống hóa đơn điện tử");
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

    // ═══════════════════════════════════════════════════════════════
    // TAB 1 — PHÁT HÀNH HÓA ĐƠN
    // ═══════════════════════════════════════════════════════════════
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
            new EmptyBorder(16, 16, 16, 16)));
        card.setPreferredSize(new Dimension(360, 0));

        addCardTitle(card, "Tạo yêu cầu phát hành hóa đơn");

        // Chọn hóa đơn học phí
        addLabel(card, "Hóa đơn học phí (đã thanh toán đủ)");
        cmbIssueInvoice = new JComboBox<>();
        cmbIssueInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbIssueInvoice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbIssueInvoice.setAlignmentX(LEFT_ALIGNMENT);
        cmbIssueInvoice.addActionListener(e -> updatePayload());
        card.add(cmbIssueInvoice);
        addGap(card, 10);

        // Loại hóa đơn
        addLabel(card, "Loại hóa đơn");
        cmbInvoiceType = new JComboBox<>(new String[]{
            "Hóa đơn GTGT (VAT 8%)", "Hóa đơn bán hàng"
        });
        cmbInvoiceType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbInvoiceType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbInvoiceType.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbInvoiceType);
        addGap(card, 10);

        // Tên người mua
        addLabel(card, "Tên người mua");
        txtBuyerName = styledField("Nhập tên người mua...");
        card.add(txtBuyerName);
        addGap(card, 8);

        // MST
        addLabel(card, "Mã số thuế (nếu có)");
        txtTaxCode = styledField("VD: 0123456789");
        card.add(txtTaxCode);
        addGap(card, 8);

        // Địa chỉ
        addLabel(card, "Địa chỉ");
        txtAddress = styledField("Địa chỉ người mua...");
        card.add(txtAddress);
        addGap(card, 8);

        // Email
        addLabel(card, "Email nhận hóa đơn");
        txtEmail = styledField("email@example.com");
        card.add(txtEmail);
        addGap(card, 12);

        // Payload preview
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
            new EmptyBorder(8, 10, 8, 10)));
        lblPayload.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        lblPayload.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblPayload);
        addGap(card, 14);

        // Nút phát hành
        CustomButton btnIssue = new CustomButton("Phát hành hóa đơn");
        btnIssue.setColors(PRIMARY, PRIMARY.darker());
        btnIssue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIssue.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnIssue.setAlignmentX(LEFT_ALIGNMENT);
        btnIssue.addActionListener(e -> issueInvoice());
        card.add(btnIssue);
        addGap(card, 8);

        // Nút in PDF
        CustomButton btnPdf = new CustomButton("In hóa đơn PDF");
        btnPdf.setColors(new Color(13, 110, 253), new Color(10, 88, 202));
        btnPdf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPdf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnPdf.setAlignmentX(LEFT_ALIGNMENT);
        btnPdf.addActionListener(e -> printFromForm());
        card.add(btnPdf);
        addGap(card, 8);

        // Nút gửi email
        CustomButton btnEmail = new CustomButton("Gửi Email hóa đơn");
        btnEmail.setColors(new Color(25, 135, 84), new Color(20, 108, 67));
        btnEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnEmail.setAlignmentX(LEFT_ALIGNMENT);
        btnEmail.addActionListener(e -> sendEmailAction());
        card.add(btnEmail);
        addGap(card, 6);

        // Link cài đặt Gmail
        JButton btnCfg = new JButton("⚙  Cài đặt tài khoản Gmail gửi...");
        btnCfg.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCfg.setForeground(TEXT_MUTE);
        btnCfg.setBorderPainted(false);
        btnCfg.setContentAreaFilled(false);
        btnCfg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCfg.setAlignmentX(LEFT_ALIGNMENT);
        btnCfg.addActionListener(e -> showEmailConfig());
        card.add(btnCfg);

        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        // Header row: title + print button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Trạng thái phát hành gần đây");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        header.add(title, BorderLayout.WEST);

        CustomButton btnPdfHistory = new CustomButton("In PDF (hàng đã chọn)");
        btnPdfHistory.setColors(new Color(13, 110, 253), new Color(10, 88, 202));
        btnPdfHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnPdfHistory.addActionListener(e -> printFromHistory());
        header.add(btnPdfHistory, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        historyModel = new DefaultTableModel(
            new String[]{"Mã HĐ", "Học viên", "Số tiền", "API Status", "Thời gian"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHistory = new JTable(historyModel);
        styleTable(tblHistory);
        tblHistory.getColumnModel().getColumn(3).setCellRenderer(apiStatusRenderer());
        card.add(new JScrollPane(tblHistory), BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // TAB 2 — CẬP NHẬT / ĐIỀU CHỈNH HÓA ĐƠN
    // ═══════════════════════════════════════════════════════════════
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
            new EmptyBorder(16, 16, 16, 16)));
        card.setPreferredSize(new Dimension(360, 0));

        addCardTitle(card, "Tìm hóa đơn cần điều chỉnh");

        // Tìm theo mã
        addLabel(card, "Mã hóa đơn");
        JPanel searchRow = new JPanel(new BorderLayout(6, 0));
        searchRow.setOpaque(false);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchRow.setAlignmentX(LEFT_ALIGNMENT);
        txtAdjustId = styledField("VD: 1");
        CustomButton btnFind = new CustomButton("Tìm");
        btnFind.setColors(new Color(52, 58, 64), new Color(33, 37, 41));
        btnFind.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFind.setPreferredSize(new Dimension(60, 34));
        btnFind.addActionListener(e -> findInvoice());
        searchRow.add(txtAdjustId, BorderLayout.CENTER);
        searchRow.add(btnFind,     BorderLayout.EAST);
        card.add(searchRow);
        addGap(card, 10);

        // Card thông tin hóa đơn tìm được
        lblFoundInvoice = new JLabel("<html><i style='color:gray'>Chưa tìm hóa đơn nào</i></html>");
        lblFoundInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFoundInvoice.setBackground(new Color(241, 243, 245));
        lblFoundInvoice.setOpaque(true);
        lblFoundInvoice.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(10, 12, 10, 12)));
        lblFoundInvoice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        lblFoundInvoice.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblFoundInvoice);
        addGap(card, 12);

        // Lý do điều chỉnh
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

        // Loại điều chỉnh
        addLabel(card, "Loại điều chỉnh");
        cmbAdjustType = new JComboBox<>(new String[]{
            "Điều chỉnh tăng", "Điều chỉnh giảm", "Thay thế"
        });
        cmbAdjustType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAdjustType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbAdjustType.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cmbAdjustType);
        addGap(card, 8);

        // Ghi chú
        addLabel(card, "Ghi chú điều chỉnh");
        txtAdjustNote = new JTextArea(4, 20);
        txtAdjustNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAdjustNote.setLineWrap(true);
        txtAdjustNote.setWrapStyleWord(true);
        txtAdjustNote.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(6, 8, 6, 8)));
        JScrollPane spNote = new JScrollPane(txtAdjustNote);
        spNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        spNote.setAlignmentX(LEFT_ALIGNMENT);
        card.add(spNote);
        addGap(card, 14);

        // Nút gửi điều chỉnh
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
            new EmptyBorder(16, 16, 16, 16)));

        JLabel title = new JLabel("Hướng dẫn điều chỉnh hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        JTextArea guide = new JTextArea(
            "QUY TRÌNH ĐIỀU CHỈNH HÓA ĐƠN ĐIỆN TỬ\n" +
            "─────────────────────────────────────\n\n" +
            "1. Tìm hóa đơn cần điều chỉnh theo mã số.\n\n" +
            "2. Kiểm tra trạng thái — chỉ hóa đơn có\n" +
            "   API Status = SENT mới được điều chỉnh.\n\n" +
            "3. Chọn lý do và loại điều chỉnh phù hợp:\n" +
            "   • Điều chỉnh tăng / giảm: giữ nguyên số\n" +
            "     hóa đơn gốc, phát sinh HĐ điều chỉnh.\n" +
            "   • Thay thế: hủy HĐ gốc và phát hành mới.\n" +
            "   • Hủy hóa đơn: toàn bộ giá trị về 0.\n\n" +
            "4. Nhập ghi chú rõ ràng để lưu hồ sơ.\n\n" +
            "5. Hệ thống sẽ gửi yêu cầu đến cơ quan\n" +
            "   thuế và cập nhật trạng thái API.\n\n" +
            "⚠  Không thể hoàn tác sau khi gửi yêu cầu."
        );
        guide.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        guide.setForeground(TEXT_MUTE);
        guide.setBackground(new Color(248, 249, 250));
        guide.setEditable(false);
        guide.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 14, 16)));
        card.add(guide, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void loadPaidInvoices() {
        paidInvoices = ctrl.searchInvoices(null, "PAID");
        cmbIssueInvoice.removeAllItems();
        if (paidInvoices.isEmpty()) {
            cmbIssueInvoice.addItem("(Không có hóa đơn đã thanh toán đủ)");
            return;
        }
        for (Invoice inv : paidInvoices) {
            cmbIssueInvoice.addItem("INV-" + String.format("%03d", inv.getInvoiceId())
                + "  —  " + inv.getStudentName()
                + "  (" + nf.format(inv.getFinalAmount()) + "đ)");
        }
        updatePayload();
    }

    private void updatePayload() {
        int idx = cmbIssueInvoice.getSelectedIndex();
        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            lblPayload.setText("<html><font face='Courier New' size='2'>{}</font></html>");
            return;
        }
        Invoice inv = paidInvoices.get(idx);
        String json = "{ invoice_id: " + inv.getInvoiceId()
            + ", amount: " + (long) inv.getFinalAmount()
            + ", buyer: \"" + inv.getStudentName() + "\""
            + ", items: [\"Học phí khóa học\"] }";
        lblPayload.setText("<html><font face='Courier New' size='2'>"
            + json.replace("<", "&lt;").replace(">", "&gt;")
            + "</font></html>");
    }

    private void issueInvoice() {
        int idx = cmbIssueInvoice.getSelectedIndex();
        if (paidInvoices == null || paidInvoices.isEmpty() || idx < 0 || idx >= paidInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần phát hành.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String buyer = txtBuyerName.getText().trim();
        if (buyer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên người mua.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Invoice inv = paidInvoices.get(idx);
        String result = ctrl.issueInvoice(inv.getInvoiceId());
        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                "✔ Đã gửi yêu cầu phát hành hóa đơn điện tử!\n"
                + "Hóa đơn: INV-" + String.format("%03d", inv.getInvoiceId()) + "\n"
                + "Người mua: " + buyer + "\n"
                + "Email xác nhận sẽ được gửi trong 5 phút.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadPaidInvoices();
            loadHistory();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
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
                lblFoundInvoice.setText("<html>"
                    + "<b>INV-" + String.format("%03d", foundInvoice.getInvoiceId()) + "</b>"
                    + "&nbsp;&nbsp;|&nbsp;&nbsp;" + foundInvoice.getStudentName()
                    + "<br>Số tiền: " + nf.format(foundInvoice.getFinalAmount()) + "đ"
                    + "&nbsp;&nbsp;|&nbsp;&nbsp;API: <b>" + safeStr(foundInvoice.getApiStatus()) + "</b>"
                    + "</html>");
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
        String result = ctrl.adjustInvoice(foundInvoice.getInvoiceId(), reason + " — " + note);
        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this,
                "✔ Đã gửi yêu cầu điều chỉnh thành công!\n"
                + "Lý do: " + reason + "\n"
                + "Loại: " + cmbAdjustType.getSelectedItem(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        List<Invoice> all = ctrl.getAllInvoices();
        for (Invoice inv : all) {
            String api = safeStr(inv.getApiStatus());
            if ("SENT".equals(api) || "ADJUSTED".equals(api) || "ERROR".equals(api)) {
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

    // ── Helpers ─────────────────────────────────────────────────────
    private String safeStr(String s) { return s == null ? "—" : s; }

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

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
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

    // ── PDF export ───────────────────────────────────────────────────

    /** In PDF từ form phát hành (dùng combo + các field thông tin người mua). */
    private void printFromForm() {
        int idx = cmbIssueInvoice.getSelectedIndex();
        if (paidInvoices == null || paidInvoices.isEmpty()
                || idx < 0 || idx >= paidInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Invoice inv = paidInvoices.get(idx);
        String buyer = txtBuyerName.getText().trim();
        if (buyer.isEmpty()) buyer = inv.getStudentName();
        exportPdf(inv, buyer,
                txtTaxCode.getText().trim(),
                txtAddress.getText().trim(),
                txtEmail.getText().trim(),
                cmbInvoiceType.getSelectedItem().toString());
    }

    /** In PDF từ bảng lịch sử (dùng hàng đang được chọn). */
    private void printFromHistory() {
        int row = tblHistory.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong bảng.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = historyModel.getValueAt(row, 0).toString(); // "INV-001"
        try {
            int id = Integer.parseInt(code.replace("INV-", "").trim());
            Invoice inv = ctrl.getInvoiceById(id);
            if (inv == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn #" + id,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            exportPdf(inv, safeStr(inv.getStudentName()), "", "", "",
                    "Hóa đơn điện tử");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc mã hóa đơn.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Email ─────────────────────────────────────────────────────────

    /** Gửi hóa đơn PDF qua Gmail đến địa chỉ email đã nhập. */
    private void sendEmailAction() {
        // Kiểm tra cấu hình
        if (!EmailService.isConfigured()) {
            int ans = JOptionPane.showConfirmDialog(this,
                    "Chưa cấu hình Gmail gửi hóa đơn.\nMở cài đặt ngay?",
                    "Cần cấu hình", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION) showEmailConfig();
            return;
        }

        int idx = cmbIssueInvoice.getSelectedIndex();
        if (paidInvoices == null || paidInvoices.isEmpty()
                || idx < 0 || idx >= paidInvoices.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String toEmail = txtEmail.getText().trim();
        if (toEmail.isEmpty() || !toEmail.contains("@")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập địa chỉ email người nhận hợp lệ\nvào ô \"Email nhận hóa đơn\".",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        Invoice inv = paidInvoices.get(idx);
        String buyer   = txtBuyerName.getText().trim().isEmpty()
                       ? inv.getStudentName() : txtBuyerName.getText().trim();
        String taxCode = txtTaxCode.getText().trim();
        String address = txtAddress.getText().trim();
        String invType = cmbInvoiceType.getSelectedItem().toString();

        // Chạy trong background thread để không đóng băng UI
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                // Sinh PDF vào bộ nhớ
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InvoicePdfGenerator.exportToStream(inv, buyer, taxCode,
                        address, toEmail, invType, bos);
                byte[] pdfBytes = bos.toByteArray();

                // Gửi email
                EmailService.sendInvoice(toEmail, buyer, inv, pdfBytes);

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(InvoiceIssuePanel.this,
                            "✔ Đã gửi hóa đơn thành công!\n\nGửi đến: " + toEmail
                            + "\nHóa đơn: INV-" + String.format("%03d", inv.getInvoiceId()),
                            "Gửi thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(InvoiceIssuePanel.this,
                            "Lỗi gửi email: " + ex.getMessage()
                            + "\n\nGợi ý: Kiểm tra lại App Password và bật 2-Step Verification.",
                            "Lỗi gửi email", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "email-sender").start();
    }

    /** Dialog cài đặt Gmail gửi hóa đơn. */
    private void showEmailConfig() {
        JTextField txtGmail = new JTextField(26);
        JPasswordField txtPass = new JPasswordField(26);

        // Load config hiện tại nếu có
        try {
            java.util.Properties p = EmailService.loadConfig();
            txtGmail.setText(p.getProperty("sender.email", ""));
        } catch (Exception ignored) {}

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Hướng dẫn
        JTextArea guide = new JTextArea(
                "Hướng dẫn lấy App Password:\n"
                + "1. Truy cập https://myaccount.google.com/security\n"
                + "2. Bật 2-Step Verification (nếu chưa bật)\n"
                + "3. Tìm \"App passwords\" → chọn \"Mail\" → Generate\n"
                + "4. Copy 16 ký tự vào ô bên dưới (không cần dấu cách)");
        guide.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guide.setEditable(false);
        guide.setBackground(new Color(255, 253, 230));
        guide.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 213, 79), 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        form.add(guide);
        form.add(Box.createVerticalStrut(12));

        form.add(new JLabel("Gmail gửi hóa đơn:"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtGmail);
        form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("App Password (16 ký tự):"));
        form.add(Box.createVerticalStrut(4));
        form.add(txtPass);
        form.add(Box.createVerticalStrut(4));

        JLabel note = new JLabel("* Mật khẩu được lưu cục bộ trên máy, không gửi đi đâu.");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(TEXT_MUTE);
        form.add(note);

        int result = JOptionPane.showConfirmDialog(this, form,
                "Cài đặt Gmail", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String gmail = txtGmail.getText().trim();
            String pass  = new String(txtPass.getPassword()).trim();
            if (gmail.isEmpty() || !gmail.contains("@")) {
                JOptionPane.showMessageDialog(this, "Gmail không hợp lệ!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pass.length() < 16) {
                JOptionPane.showMessageDialog(this,
                        "App Password phải có ít nhất 16 ký tự!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                EmailService.saveConfig(gmail, pass);
                JOptionPane.showMessageDialog(this,
                        "✔ Đã lưu cấu hình Gmail!\n" + gmail,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi lưu cấu hình: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Hiển thị hộp thoại lưu file rồi mở PDF. */
    private void exportPdf(Invoice inv, String buyerName, String taxCode,
                            String address, String email, String invType) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu hóa đơn PDF");
        fc.setSelectedFile(new File(
                "HoaDon_INV-" + String.format("%03d", inv.getInvoiceId()) + ".pdf"));
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new File(file.getAbsolutePath() + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            InvoicePdfGenerator.exportToStream(inv, buyerName, taxCode,
                    address, email, invType, fos);
            JOptionPane.showMessageDialog(this,
                    "Đã lưu PDF thành công!\n" + file.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi xuất PDF: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private DefaultTableCellRenderer apiStatusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setOpaque(true);
                String s = v == null ? "—" : v.toString();
                switch (s) {
                    case "SENT":
                        setBackground(new Color(212,237,218)); setForeground(new Color(21,87,36));   setText("Đã phát hành"); break;
                    case "ADJUSTED":
                        setBackground(new Color(207,226,255)); setForeground(new Color(13,71,161));  setText("Đã điều chỉnh"); break;
                    case "ERROR":
                        setBackground(new Color(248,215,218)); setForeground(new Color(114,28,36));  setText("Lỗi API"); break;
                    default:
                        setBackground(new Color(241,243,245)); setForeground(TEXT_MUTE);             setText("Chờ xử lý"); break;
                }
                if (sel) setBackground(getBackground().darker());
                return this;
            }
        };
    }
}