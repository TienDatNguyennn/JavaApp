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
 * Panel Quản lý học phí — đối soát với cổng thanh toán
 */
public class ManageInvoicePanel extends JPanel {

    private static final Color PRIMARY  = new Color(108, 92, 231);
    private static final Color BG_PAGE  = new Color(248, 249, 250);
    private static final Color BG_CARD  = Color.WHITE;
    private static final Color BORDER_C = new Color(222, 226, 230);
    private static final Color TEXT_MAIN= new Color(33,  37,  41);
    private static final Color TEXT_MUTE= new Color(108, 117, 125);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private DefaultTableModel tableModel;
    private JTable            tblInvoice;
    private JTextField        txtKeyword;
    private JComboBox<String> cmbStatus;

    // Metric labels
    private JLabel lblTotalRevenue, lblCountPaid, lblCountDebt, lblCountPending;

    public ManageInvoicePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(),   BorderLayout.NORTH);
        add(buildContent(),  BorderLayout.CENTER);
        loadData(null, null);
    }

    // ── Header ──────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Quản lý học phí");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Theo dõi và đối soát học phí với cổng thanh toán");
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

    // ── Nội dung: Metrics + Filter + Bảng ───────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildMetrics(), BorderLayout.NORTH);
        p.add(buildTableCard(), BorderLayout.CENTER);
        return p;
    }

    // ── 4 ô Metric ──────────────────────────────────────────────────
    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        lblTotalRevenue = metricCard("Tổng thu kỳ này",    "—",  new Color(108, 92, 231));
        lblCountPaid    = metricCard("Đã thanh toán đủ",   "—",  new Color(25, 135, 84));
        lblCountDebt    = metricCard("Còn nợ học phí",     "—",  new Color(255, 193, 7));
        lblCountPending = metricCard("Chờ đối soát",       "—",  new Color(220, 53, 69));

        row.add(wrapMetric("Tổng thu kỳ này",  lblTotalRevenue, new Color(108, 92, 231)));
        row.add(wrapMetric("Đã thanh toán đủ", lblCountPaid,    new Color(25,  135, 84)));
        row.add(wrapMetric("Còn nợ học phí",   lblCountDebt,    new Color(255, 193, 7)));
        row.add(wrapMetric("Chờ đối soát",     lblCountPending, new Color(220, 53,  69)));
        return row;
    }

    /** Tạo panel metric và trả về JLabel giá trị để cập nhật sau */
    private JLabel metricCard(String title, String value, Color accent) { return new JLabel(value); }

    private JPanel wrapMetric(String titleText, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        JLabel lTitle = new JLabel(titleText);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTitle.setForeground(TEXT_MUTE);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(accent);

        card.add(lTitle,    BorderLayout.NORTH);
        card.add(valLabel,  BorderLayout.CENTER);
        return card;
    }

    // ── Bảng hóa đơn + Toolbar filter ───────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        // ─ Toolbar ─
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JLabel tTitle = new JLabel("Danh sách hóa đơn học phí");
        tTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tTitle.setForeground(TEXT_MAIN);
        toolbar.add(tTitle, BorderLayout.WEST);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setOpaque(false);

        txtKeyword = new JTextField(14);
        txtKeyword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKeyword.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        txtKeyword.setToolTipText("Tìm theo tên học viên...");

        cmbStatus = new JComboBox<>(new String[]{"Tất cả", "PAID", "PARTIAL", "UNPAID"});
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        CustomButton btnFilter = new CustomButton("Lọc");
        btnFilter.setColors(PRIMARY, PRIMARY.darker());
        btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFilter.setPreferredSize(new Dimension(65, 30));
        btnFilter.addActionListener(e -> {
            String kw = txtKeyword.getText().trim();
            String st = cmbStatus.getSelectedItem().toString();
            loadData(kw.isEmpty() ? null : kw, "Tất cả".equals(st) ? null : st);
        });

        CustomButton btnRefresh = new CustomButton("↺ Làm mới");
        btnRefresh.setColors(new Color(52, 58, 64), new Color(33, 37, 41));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(e -> { txtKeyword.setText(""); cmbStatus.setSelectedIndex(0); loadData(null, null); });

        filterRow.add(new JLabel("Tìm:"));
        filterRow.add(txtKeyword);
        filterRow.add(new JLabel("Trạng thái:"));
        filterRow.add(cmbStatus);
        filterRow.add(btnFilter);
        filterRow.add(btnRefresh);
        toolbar.add(filterRow, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // ─ Bảng ─
        tableModel = new DefaultTableModel(
            new String[]{"Mã HĐ", "Học viên", "Tổng phí", "Giảm giá", "Thực thu", "Đã nộp", "Còn nợ", "Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblInvoice = new JTable(tableModel);
        styleTable(tblInvoice);

        // Cột Trạng thái màu sắc
        tblInvoice.getColumnModel().getColumn(7).setCellRenderer(statusRenderer());
        // Cột số — căn phải
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{2, 3, 4, 5, 6}) {
            tblInvoice.getColumnModel().getColumn(c).setCellRenderer(rightAlign);
        }

        card.add(new JScrollPane(tblInvoice), BorderLayout.CENTER);
        return card;
    }

    // ── Load / refresh dữ liệu ──────────────────────────────────────
    private void loadData(String keyword, String status) {
        List<Invoice> list = ctrl.searchInvoices(keyword, status);
        tableModel.setRowCount(0);

        long countPaid = 0, countDebt = 0, countPending = 0;
        double totalRevenue = 0;

        for (Invoice inv : list) {
            double debt = inv.getDebtAmount();
            tableModel.addRow(new Object[]{
                "INV-" + String.format("%03d", inv.getInvoiceId()),
                inv.getStudentName(),
                nf.format(inv.getTotalAmount()) + "đ",
                nf.format(inv.getDiscountAmt()) + "đ",
                nf.format(inv.getFinalAmount()) + "đ",
                nf.format(inv.getAmountPaid()) + "đ",
                nf.format(debt) + "đ",
                inv.getStatus()
            });

            totalRevenue += inv.getAmountPaid();
            if ("PAID".equals(inv.getStatus()))       countPaid++;
            else if ("PARTIAL".equals(inv.getStatus()))countDebt++;
            else if ("UNPAID".equals(inv.getStatus())) countPending++;
        }

        lblTotalRevenue.setText(formatMillions(totalRevenue));
        lblCountPaid.setText   (String.valueOf(countPaid));
        lblCountDebt.setText   (String.valueOf(countDebt));
        lblCountPending.setText(String.valueOf(countPending));
    }

    private String formatMillions(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        return nf.format(v) + "đ";
    }

    // ── Helpers ─────────────────────────────────────────────────────
    private void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(241, 243, 245));
        t.getTableHeader().setForeground(TEXT_MUTE);
        t.setGridColor(new Color(233, 236, 239));
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(232, 228, 252));
        t.setSelectionForeground(TEXT_MAIN);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(CENTER);
                setOpaque(true);
                String s = v == null ? "" : v.toString();
                switch (s) {
                    case "PAID":
                        setBackground(new Color(212,237,218)); setForeground(new Color(21,87,36));  setText("Đã đủ");
                        break;
                    case "PARTIAL":
                        setBackground(new Color(255,243,205)); setForeground(new Color(133,100,4)); setText("Còn nợ");
                        break;
                    default:
                        setBackground(new Color(248,215,218)); setForeground(new Color(114,28,36)); setText("Chưa nộp");
                }
                if (sel) setBackground(getBackground().darker());
                return this;
            }
        };
    }
}