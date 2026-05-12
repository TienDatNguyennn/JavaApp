package com.mycompany.myapp.view.screens.finance;

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

/**
 * Panel Tra cứu tình trạng học phí
 * Nhân viên quản lý học vụ theo dõi tình trạng thanh toán của học viên.
 */
public class LookupPanel extends JPanel {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33,  37,  41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25,  135, 84);
    private static final Color WARNING   = new Color(255, 193, 7);
    private static final Color DANGER    = new Color(220, 53,  69);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JTextField        txtKeyword;
    private JComboBox<String> cmbStatus;
    private DefaultTableModel tableModel;
    private JTable            tblStudents;

    // Metric labels
    private JLabel lblTotal, lblPaid, lblPartial, lblUnpaid;

    public LookupPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData(null, null);
    }

    // ═══════════════════════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Tra cứu tình trạng học phí");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Theo dõi tình trạng thanh toán học phí của từng học viên");
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
    // CONTENT: Metrics + SearchBar + Table
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildMetrics(),   BorderLayout.NORTH);
        p.add(buildTableCard(), BorderLayout.CENTER);
        return p;
    }

    // ── 4 ô thống kê ────────────────────────────────────────────────
    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);

        lblTotal   = new JLabel("—");
        lblPaid    = new JLabel("—");
        lblPartial = new JLabel("—");
        lblUnpaid  = new JLabel("—");

        row.add(metricCard("Tổng học viên",   lblTotal,   PRIMARY));
        row.add(metricCard("Đã đóng đủ",      lblPaid,    SUCCESS));
        row.add(metricCard("Đóng một phần",   lblPartial, WARNING));
        row.add(metricCard("Chưa đóng",       lblUnpaid,  DANGER));
        return row;
    }

    private JPanel metricCard(String titleText, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        JLabel lTitle = new JLabel(titleText);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTitle.setForeground(TEXT_MUTE);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valLabel.setForeground(accent);

        card.add(lTitle,   BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Card bảng + search bar ───────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(16, 16, 16, 16)));

        card.add(buildToolbar(), BorderLayout.NORTH);
        card.add(buildTable(),   BorderLayout.CENTER);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JLabel title = new JLabel("Danh sách học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        txtKeyword = new JTextField(16);
        txtKeyword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKeyword.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        txtKeyword.setToolTipText("Tìm theo tên, mã học viên...");

        cmbStatus = new JComboBox<>(new String[]{"Tất cả", "PAID", "PARTIAL", "UNPAID"});
        cmbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Hiển thị nhãn tiếng Việt
        cmbStatus.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(statusLabel(value == null ? "" : value.toString()));
                return this;
            }
        });

        CustomButton btnSearch = new CustomButton("🔍 Tìm");
        btnSearch.setColors(PRIMARY, PRIMARY.darker());
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setPreferredSize(new Dimension(80, 30));
        btnSearch.addActionListener(e -> {
            String kw = txtKeyword.getText().trim();
            String st = cmbStatus.getSelectedItem().toString();
            loadData(kw.isEmpty() ? null : kw, "Tất cả".equals(st) ? null : st);
        });

        CustomButton btnRemind = new CustomButton("🔔 Nhắc nợ hàng loạt");
        btnRemind.setColors(new Color(220, 53, 69), new Color(185, 40, 55));
        btnRemind.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemind.setPreferredSize(new Dimension(160, 30));
        btnRemind.addActionListener(e -> remindAll());

        CustomButton btnReset = new CustomButton("↺");
        btnReset.setColors(new Color(108,117,125), new Color(73,80,87));
        btnReset.setPreferredSize(new Dimension(36, 30));
        btnReset.addActionListener(e -> {
            txtKeyword.setText("");
            cmbStatus.setSelectedIndex(0);
            loadData(null, null);
        });

        right.add(txtKeyword);
        right.add(new JLabel("Trạng thái:"));
        right.add(cmbStatus);
        right.add(btnSearch);
        right.add(btnRemind);
        right.add(btnReset);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(
            new String[]{"Học viên", "Mã HV", "Học phí", "Đã nộp", "Còn nợ", "Tỷ lệ", "Trạng thái", "Nhắc nợ"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        tblStudents = new JTable(tableModel);
        tblStudents.setRowHeight(32);
        tblStudents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStudents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblStudents.getTableHeader().setBackground(new Color(241, 243, 245));
        tblStudents.getTableHeader().setForeground(TEXT_MUTE);
        tblStudents.setGridColor(new Color(233, 236, 239));
        tblStudents.setShowVerticalLines(false);
        tblStudents.setSelectionBackground(new Color(232, 228, 252));

        // Căn phải cột số
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{2, 3, 4}) {
            tblStudents.getColumnModel().getColumn(c).setCellRenderer(rightAlign);
        }

        // Renderer cột Tỷ lệ — progress bar mini
        tblStudents.getColumnModel().getColumn(5).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int row, int col) {
                    int pct = 0;
                    try { pct = Integer.parseInt(v.toString().replace("%", "")); }
                    catch (Exception ignored) {}
                    JProgressBar bar = new JProgressBar(0, 100);
                    bar.setValue(pct);
                    bar.setString(pct + "%");
                    bar.setStringPainted(true);
                    bar.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    Color barColor = pct >= 100 ? SUCCESS : pct > 0 ? WARNING : DANGER;
                    bar.setForeground(barColor);
                    bar.setBackground(new Color(233, 236, 239));
                    if (sel) bar.setBackground(new Color(220, 215, 250));
                    return bar;
                }
            });

        // Renderer cột Trạng thái
        tblStudents.getColumnModel().getColumn(6).setCellRenderer(statusCellRenderer());

        // Cột Nhắc nợ — nút trong ô
        tblStudents.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        tblStudents.getColumnModel().getColumn(7).setCellEditor(
            new ButtonEditor(new JCheckBox(), this));

        // Độ rộng cột
        int[] widths = {160, 90, 110, 110, 110, 90, 90, 80};
        for (int i = 0; i < widths.length; i++)
            tblStudents.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return new JScrollPane(tblStudents);
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════════════════════════
    private void loadData(String keyword, String status) {
        List<Invoice> list = ctrl.searchInvoices(keyword, status);
        tableModel.setRowCount(0);

        int total = list.size(), paid = 0, partial = 0, unpaid = 0;

        for (Invoice inv : list) {
            double debt = inv.getDebtAmount();
            int pct = inv.getFinalAmount() > 0
                ? (int) (inv.getAmountPaid() / inv.getFinalAmount() * 100) : 0;
            pct = Math.min(pct, 100);

            String btnLabel = "UNPAID".equals(inv.getStatus()) || "PARTIAL".equals(inv.getStatus())
                ? "Nhắc" : "—";

            tableModel.addRow(new Object[]{
                inv.getStudentName(),
                "HV" + String.format("%07d", inv.getStudentId()),
                nf.format(inv.getFinalAmount()) + "đ",
                nf.format(inv.getAmountPaid()) + "đ",
                nf.format(debt) + "đ",
                pct + "%",
                inv.getStatus(),
                btnLabel
            });

            if ("PAID".equals(inv.getStatus()))        paid++;
            else if ("PARTIAL".equals(inv.getStatus())) partial++;
            else                                         unpaid++;
        }

        lblTotal.setText  (String.valueOf(total));
        lblPaid.setText   (String.valueOf(paid));
        lblPartial.setText(String.valueOf(partial));
        lblUnpaid.setText (String.valueOf(unpaid));
    }

    /** Nhắc nợ cho 1 học viên cụ thể */
    public void remindOne(int row) {
        String name = tableModel.getValueAt(row, 0).toString();
        String debt = tableModel.getValueAt(row, 4).toString();
        JOptionPane.showMessageDialog(this,
            "✔ Đã gửi nhắc nhở đến: " + name + "\nSố tiền còn nợ: " + debt,
            "Nhắc học phí", JOptionPane.INFORMATION_MESSAGE);
    }

    private void remindAll() {
        int count = 0;
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            String st = tableModel.getValueAt(r, 6).toString();
            if ("UNPAID".equals(st) || "PARTIAL".equals(st)) count++;
        }
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Không có học viên nào cần nhắc học phí.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Gửi nhắc nhở học phí đến " + count + " học viên chưa đóng đủ?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                "✔ Đã gửi nhắc nhở đến " + count + " học viên.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Helper: nhãn tiếng Việt ──────────────────────────────────────
    private String statusLabel(String s) {
        switch (s) {
            case "PAID":    return "Đã đóng đủ";
            case "PARTIAL": return "Còn nợ";
            case "UNPAID":  return "Chưa đóng";
            default:        return "Tất cả";
        }
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
                        setBackground(new Color(212,237,218)); setForeground(new Color(21,87,36));  setText("Đã đủ");    break;
                    case "PARTIAL":
                        setBackground(new Color(255,243,205)); setForeground(new Color(133,100,4)); setText("Còn nợ");   break;
                    default:
                        setBackground(new Color(248,215,218)); setForeground(new Color(114,28,36)); setText("Chưa nộp"); break;
                }
                if (sel) setBackground(getBackground().darker());
                return this;
            }
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // Inner classes: nút Nhắc trong ô bảng
    // ═══════════════════════════════════════════════════════════════
    static class ButtonRenderer extends DefaultTableCellRenderer {
        private final JButton btn = new JButton();
        ButtonRenderer() {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            String label = v == null ? "—" : v.toString();
            if ("Nhắc".equals(label)) {
                btn.setText("Nhắc");
                btn.setBackground(new Color(220, 53, 69));
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            } else {
                btn.setText("—");
                btn.setBackground(new Color(233, 236, 239));
                btn.setForeground(new Color(108, 117, 125));
                btn.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            }
            return btn;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private final LookupPanel panel;
        private int currentRow;
        private String label;

        ButtonEditor(JCheckBox cb, LookupPanel panel) {
            super(cb);
            this.panel = panel;
            btn = new JButton();
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setOpaque(true);
            btn.addActionListener(e -> {
                fireEditingStopped();
                if ("Nhắc".equals(label)) panel.remindOne(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int row, int col) {
            label = v == null ? "—" : v.toString();
            currentRow = row;
            if ("Nhắc".equals(label)) {
                btn.setText("Nhắc");
                btn.setBackground(new Color(220, 53, 69));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setText("—");
                btn.setBackground(new Color(233, 236, 239));
                btn.setForeground(new Color(108, 117, 125));
            }
            return btn;
        }

        @Override public Object getCellEditorValue() { return label; }
    }
}