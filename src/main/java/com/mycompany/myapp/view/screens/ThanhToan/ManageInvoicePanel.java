package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Panel Quan ly hoc phi
 * Chuc nang: Them / Sua / Xoa / Tim kiem / Loc hoa don
 */
public class ManageInvoicePanel extends JPanel {

    // ── Mau sac ──────────────────────────────────────────────────────
    private static final Color PRIMARY   = new Color(255, 159, 67);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33,  37,  41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25,  135, 84);
    private static final Color DANGER    = new Color(220, 53,  69);
    private static final Color WARNING   = new Color(255, 193, 7);
    private static final Color INFO      = new Color(13,  110, 253);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Controls tim kiem
    private JTextField        txtKeyword;
    private JComboBox<String> cmbStatus;

    // Metric labels
    private JLabel lblTotalRevenue, lblCountPaid, lblCountDebt, lblCountUnpaid;

    // Bang du lieu
    private DefaultTableModel tableModel;
    private JTable            tblInvoice;

    // Du lieu hien tai
    private List<Invoice> currentList;

    public ManageInvoicePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData(null, null);
    }

    // ================================================================
    // HEADER
    // ================================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Quản lý học phí");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Xem, thêm, sửa, xóa và đối soát danh sách hóa đơn học phí");
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

    // ================================================================
    // CONTENT: Metrics + Bang
    // ================================================================
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildMetrics(),   BorderLayout.NORTH);
        p.add(buildTableCard(), BorderLayout.CENTER);
        return p;
    }

    // ── 4 o metric ──────────────────────────────────────────────────
    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        lblTotalRevenue = new JLabel("—");
        lblCountPaid    = new JLabel("—");
        lblCountDebt    = new JLabel("—");
        lblCountUnpaid  = new JLabel("—");

        row.add(metricCard("Tổng thu kì này",    lblTotalRevenue, PRIMARY));
        row.add(metricCard("Đã thanh toán đủ",   lblCountPaid,    SUCCESS));
        row.add(metricCard("Chưa thanh toán",          lblCountUnpaid,  DANGER));
        return row;
    }

    private JPanel metricCard(String title, JLabel valLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 14, 16)));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTitle.setForeground(TEXT_MUTE);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(accent);
        card.add(lTitle,  BorderLayout.NORTH);
        card.add(valLbl,  BorderLayout.CENTER);
        return card;
    }

    // ── Card bang + toolbar ──────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));
        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(),   BorderLayout.CENTER);
        return card;
    }

    // ── Toolbar: tim kiem + nut CRUD ────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        // Title
        JLabel title = new JLabel("Danh sách");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        bar.add(title, BorderLayout.WEST);

        // Right side: tim kiem + cac nut
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Tim kiem
        txtKeyword = new JTextField(14);
        txtKeyword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKeyword.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        txtKeyword.setToolTipText("Tim theo ten hoc vien...");
        // Nhan Enter de tim kiem
        txtKeyword.addActionListener(e -> doSearch());

        // Combo trang thai
        cmbStatus = new JComboBox<>(new String[]{"Tat ca", "PAID", "UNPAID"});
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStatus.setRenderer(statusComboRenderer());

        // Nut Tim
        CustomButton btnSearch = makeBtn("Tìm", INFO, 70);
        btnSearch.addActionListener(e -> doSearch());

        // Nut Reset
        CustomButton btnReset = makeBtn("Làm mới", new Color(108,117,125), 90);
        btnReset.addActionListener(e -> {
            txtKeyword.setText("");
            cmbStatus.setSelectedIndex(0);
            loadData(null, null);
        });

        right.add(txtKeyword);
        right.add(new JLabel("Trạng thái:"));
        right.add(cmbStatus);
        right.add(btnSearch);
        right.add(btnReset);

        // Duong ke doc ngan cach
        right.add(makeDivider());

        // Nut THEM
        CustomButton btnAdd = makeBtn("+ Thêm mới", SUCCESS, 110);
        btnAdd.addActionListener(e -> doAdd());

        // Nut SUA (can chon dong)
        CustomButton btnEdit = makeBtn("Sửa", PRIMARY, 70);
        btnEdit.addActionListener(e -> doEdit());

        // Nut XOA (can chon dong)
        CustomButton btnDelete = makeBtn("Xóa", DANGER, 70);
        btnDelete.addActionListener(e -> doDelete());

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnDelete);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Bang du lieu ────────────────────────────────────────────────
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(
            new String[]{"Chọn", "Mã HĐ", "Học viên", "Học phí", "Giảm giá",
                         "Thực thu", "Đã nộp", "Còn nợ", "PT TT", "Trạng thái"}, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0; // Chi cho check/uncheck o checkbox
            }
        };

        tblInvoice = new JTable(tableModel);
        tblInvoice.setRowHeight(32);
        tblInvoice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblInvoice.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblInvoice.getTableHeader().setBackground(new Color(241, 243, 245));
        tblInvoice.getTableHeader().setForeground(TEXT_MUTE);
        tblInvoice.setGridColor(new Color(233, 236, 239));
        tblInvoice.setShowVerticalLines(false);
        tblInvoice.setSelectionBackground(new Color(232, 228, 252));
        tblInvoice.setSelectionForeground(TEXT_MAIN);
        tblInvoice.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Click doi dong -> chon 1 dong
        tblInvoice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) doEdit(); // Double-click = Sua
            }
        });

        // Do rong cot
        int[] widths = {50, 65, 150, 110, 90, 110, 100, 100, 130, 95};
        for (int i = 0; i < widths.length; i++)
            tblInvoice.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Can phai cot so tien
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{3, 4, 5, 6, 7})
            tblInvoice.getColumnModel().getColumn(c).setCellRenderer(rightAlign);

        // Renderer mau cho cot Trang thai
        tblInvoice.getColumnModel().getColumn(9).setCellRenderer(statusCellRenderer());

        return new JScrollPane(tblInvoice);
    }

    // ================================================================
    // LOAD DATA
    // ================================================================
    private void loadData(String keyword, String status) {
        currentList = ctrl.searchInvoices(keyword, status);
        tableModel.setRowCount(0);

        long paid = 0, partial = 0, unpaid = 0;
        double totalRevenue = 0;

        for (Invoice inv : currentList) {
            double debt = inv.getDebtAmount();
            tableModel.addRow(new Object[]{
                Boolean.FALSE,
                "HD-" + String.format("%03d", inv.getInvoiceId()),
                inv.getStudentName(),
                nf.format(inv.getTotalAmount())  + "d",
                nf.format(inv.getDiscountAmt())  + "d",
                nf.format(inv.getFinalAmount())  + "d",
                nf.format(inv.getAmountPaid())   + "d",
                nf.format(debt)                  + "d",
                inv.getPaymentMethod() == null ? "—" : inv.getPaymentMethod(),
                inv.getStatus()
            });

            totalRevenue += inv.getAmountPaid();
            switch (inv.getStatus() == null ? "" : inv.getStatus()) {
                case "PAID":    paid++;    break;
                case "PARTIAL": partial++; break;
                default:        unpaid++;  break;
            }
        }

        // Cap nhat metric
        lblTotalRevenue.setText(totalRevenue >= 1_000_000
            ? String.format("%.1fM", totalRevenue / 1_000_000)
            : nf.format(totalRevenue) + "d");
        lblCountPaid  .setText(String.valueOf(paid));
        lblCountDebt  .setText(String.valueOf(partial));
        lblCountUnpaid.setText(String.valueOf(unpaid));
    }

    // ================================================================
    // CRUD ACTIONS
    // ================================================================

    /** Tim kiem theo keyword + trang thai */
    private void doSearch() {
        String kw = txtKeyword.getText().trim();
        String st = cmbStatus.getSelectedItem().toString();
        loadData(kw.isEmpty() ? null : kw,
                 "Tat ca".equals(st) ? null : st);
    }

    /** THEM: mo InvoiceFormDialog de nhap thong tin moi */
    private void doAdd() {
        InvoiceFormDialog dlg = new InvoiceFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Invoice newInv = dlg.getResult();
            String res = ctrl.addInvoice(newInv);
            if ("SUCCESS".equals(res)) {
                showInfo("Da them hoa don moi thanh cong!");
                loadData(null, null);
            } else {
                showError(res);
            }
        }
    }

    /** SUA: lay dong dang chon, mo dialog voi du lieu san co */
    private void doEdit() {
        Invoice selected = getSelectedInvoice();
        if (selected == null) {
            showWarn("Vui long chon mot hoa don can sua.");
            return;
        }

        InvoiceFormDialog dlg = new InvoiceFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), selected);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            Invoice edited = dlg.getResult();
            String res = ctrl.updateInvoice(edited);
            if ("SUCCESS".equals(res)) {
                showInfo("Da cap nhat hoa don HD-"
                    + String.format("%03d", edited.getInvoiceId()) + " thanh cong!");
                loadData(null, null);
            } else {
                showError(res);
            }
        }
    }

    /** XOA: xac nhan roi soft-delete */
    private void doDelete() {
        Invoice selected = getSelectedInvoice();
        if (selected == null) {
            showWarn("Vui long chon mot hoa don can xoa.");
            return;
        }

        // Canh bao neu la hoa don da thanh toan
        String warningMsg = "PAID".equals(selected.getStatus())
            ? "HOA DON NAY DA DUOC THANH TOAN DAY DU.\n\n"
            : "";

        int confirm = JOptionPane.showConfirmDialog(this,
            warningMsg +
            "Ban co chac chan muon XOA hoa don?\n\n" +
            "   Hoa don: HD-" + String.format("%03d", selected.getInvoiceId()) + "\n" +
            "   Hoc vien: " + selected.getStudentName() + "\n" +
            "   So tien: " + nf.format(selected.getFinalAmount()) + "d\n\n" +
            "Du lieu se bi an di (khong hien thi) nhung van luu trong he thong.",
            "Xac nhan xoa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String res = ctrl.deleteInvoice(selected.getInvoiceId());
            if ("SUCCESS".equals(res)) {
                showInfo("Da xoa hoa don HD-"
                    + String.format("%03d", selected.getInvoiceId()) + ".");
                loadData(null, null);
            } else {
                showError(res);
            }
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /**
     * Lay Invoice dang duoc chon trong bang.
     * Uu tien: dong dang click chon (selection), sau do checkbox.
     */
    private Invoice getSelectedInvoice() {
        // Uu tien row dang highlight trong bang
        int row = tblInvoice.getSelectedRow();
        if (row >= 0 && row < currentList.size()) {
            return currentList.get(row);
        }
        // Kiem tra checkbox neu khong co row duoc click
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            Boolean checked = (Boolean) tableModel.getValueAt(r, 0);
            if (Boolean.TRUE.equals(checked) && r < currentList.size()) {
                return currentList.get(r);
            }
        }
        return null;
    }

    private CustomButton makeBtn(String label, Color color, int width) {
        CustomButton btn = new CustomButton(label);
        btn.setColors(color, color.darker());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(width, 30));
        return btn;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(BORDER_C);
        return sep;
    }

    private ListCellRenderer<Object> statusComboRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String s = value == null ? "" : value.toString();
                switch (s) {
                    case "PAID":    setText("Đã đóng"); break;
                    case "UNPAID":  setText("Chưa đóng");     break;
                    default:        setText("Tất cả");        break;
                }
                return this;
            }
        };
    }

    private DefaultTableCellRenderer statusCellRenderer() {
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
                        setBackground(new Color(212,237,218));
                        setForeground(new Color(21,87,36));
                        setText("Đã đóng");
                        break;
                    default:
                        setBackground(new Color(248,215,218));
                        setForeground(new Color(114,28,36));
                        setText("Chưa đóng");
                        break;
                }
                if (sel) setBackground(getBackground().darker());
                return this;
            }
        };
    }

    private void showInfo (String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thanh cong",    JOptionPane.INFORMATION_MESSAGE);
    }
    private void showWarn (String msg) {
        JOptionPane.showMessageDialog(this, msg, "Canh bao",      JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Loi",           JOptionPane.ERROR_MESSAGE);
    }
}