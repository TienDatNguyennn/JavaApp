package com.mycompany.myapp.view.screens.finance;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PayrollPanel extends JPanel {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33,  37,  41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25,  135, 84);

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Toolbar controls
    private JTextField txtFilterPeriod, txtSearchName;
    private JComboBox<String> cmbStaffType;

    // Metric labels
    private JLabel lblTotalSalary, lblCountTeacher, lblCountOffice, lblCountPending;

    // Table
    private DefaultTableModel tableModel;
    private JTable tblPayroll;

    // Add payroll form
    private JTextField txtUserId, txtAddPeriod, txtBasic, txtTeaching, txtBonus;
    private JComboBox<String> cmbAddType;
    private JLabel lblTotalValue; // Nhãn hiển thị tổng tiền tự động

    public PayrollPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Tính lương nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Xử lý tính toán lương định kỳ theo tháng/năm (MM/yyyy)");
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

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildMetrics(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildAddCard());
        split.setResizeWeight(0.72);
        split.setDividerSize(8);
        split.setBorder(null);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        lblTotalSalary  = new JLabel("—");
        lblCountTeacher = new JLabel("—");
        lblCountOffice  = new JLabel("—");
        lblCountPending = new JLabel("—");

        row.add(metricCard("Tổng chi lương", lblTotalSalary, PRIMARY));
        row.add(metricCard("Giáo viên", lblCountTeacher, new Color(13, 110, 253)));
        row.add(metricCard("Nhân viên giáo vụ", lblCountOffice, new Color(102, 16, 242)));
        row.add(metricCard("Chờ nhập", lblCountPending, new Color(253, 126, 20)));
        return row;
    }

    private JPanel metricCard(String title, JLabel val, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(14, 16, 14, 16)));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTitle.setForeground(TEXT_MUTE);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(accent);
        card.add(lTitle, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 16, 16, 16)));
        card.add(buildTableToolbar(), BorderLayout.NORTH);
        card.add(buildTable(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        txtFilterPeriod = new JTextField("05/2026", 7);
        cmbStaffType = new JComboBox<>(new String[]{"Tất cả","TEACHER","OFFICE"});
        txtSearchName = new JTextField(10);

        CustomButton btnLoad = new CustomButton("📊 Lọc dữ liệu");
        btnLoad.setColors(PRIMARY, PRIMARY.darker());
        btnLoad.setPreferredSize(new Dimension(110, 30));
        btnLoad.addActionListener(e -> loadData());

        right.add(new JLabel("Kỳ (MM/yyyy):"));
        right.add(txtFilterPeriod);
        right.add(new JLabel("Loại:"));
        right.add(cmbStaffType);
        right.add(new JLabel("Tên:"));
        right.add(txtSearchName);
        right.add(btnLoad);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(new String[]{"Nhân viên", "Loại", "Lương cơ bản", "Phí giảng dạy", "Thưởng", "Tổng thực lĩnh"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPayroll = new JTable(tableModel);
        tblPayroll.setRowHeight(35);
        return new JScrollPane(tblPayroll);
    }

    private JPanel buildAddCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(16, 16, 16, 16)));

        JLabel title = new JLabel("Nhập lương mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(title); addGap(card, 15);

        addFormLabel(card, "Mã nhân viên (User ID)");
        txtUserId = styledField(""); card.add(txtUserId); addGap(card, 10);

        addFormLabel(card, "Kỳ lương (MM/yyyy)");
        txtAddPeriod = styledField("05/2026"); card.add(txtAddPeriod); addGap(card, 10);

        addFormLabel(card, "Loại");
        cmbAddType = new JComboBox<>(new String[]{"TEACHER", "OFFICE"});
        cmbAddType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        card.add(cmbAddType); addGap(card, 10);

        addFormLabel(card, "Lương cơ bản");
        txtBasic = styledField("0"); card.add(txtBasic); addGap(card, 10);

        addFormLabel(card, "Phí giảng dạy");
        txtTeaching = styledField("0"); card.add(txtTeaching); addGap(card, 10);

        addFormLabel(card, "Thưởng");
        txtBonus = styledField("0"); card.add(txtBonus); addGap(card, 10);

        // Hiển thị tổng thực lĩnh tự động
        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalValue.setForeground(PRIMARY);
        JPanel pTotal = new JPanel(new BorderLayout());
        pTotal.setOpaque(false);
        pTotal.add(new JLabel("Tổng thực lĩnh: "), BorderLayout.WEST);
        pTotal.add(lblTotalValue, BorderLayout.EAST);
        card.add(pTotal); addGap(card, 15);

        // Lắng nghe sự kiện để tự động tính tổng (Recalc)
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ recalc(lblTotalValue); }
        };
        txtBasic.getDocument().addDocumentListener(dl);
        txtTeaching.getDocument().addDocumentListener(dl);
        txtBonus.getDocument().addDocumentListener(dl);

        CustomButton btnSave = new CustomButton("💾 Lưu bảng lương");
        btnSave.setColors(SUCCESS, SUCCESS.darker());
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSave.addActionListener(e -> savePayroll());
        card.add(btnSave);

        return card;
    }

    private void loadData() {
        String period = txtFilterPeriod.getText().trim();
        String type = cmbStaffType.getSelectedItem().toString();
        String keyword = txtSearchName.getText().trim().toLowerCase();

        List<Payroll> list = ctrl.getPayroll(period, type);
        tableModel.setRowCount(0);
        double totalSum = 0;
        int tCount = 0, oCount = 0;

        for (Payroll p : list) {
            if (!keyword.isEmpty() && !p.getFullName().toLowerCase().contains(keyword)) continue;

            tableModel.addRow(new Object[]{
                p.getFullName(), p.getStaffType(),
                nf.format(p.getBasicSalary()) + "đ",
                nf.format(p.getTotalTeachingFee()) + "đ",
                nf.format(p.getBonusAmount()) + "đ",
                nf.format(p.getTotalNet()) + "đ"
            });
            totalSum += p.getTotalNet();
            if ("TEACHER".equals(p.getStaffType())) tCount++; else oCount++;
        }
        lblTotalSalary.setText(nf.format(totalSum) + "đ");
        lblCountTeacher.setText(String.valueOf(tCount));
        lblCountOffice.setText(String.valueOf(oCount));
    }

    private void savePayroll() {
        try {
            String userIdStr = txtUserId.getText().trim();
            if (userIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã nhân viên!");
                return;
            }

            String period = txtAddPeriod.getText().trim();
            if (!period.matches("\\d{2}/\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Định dạng kỳ lương phải là MM/yyyy (VD: 05/2026)");
                return;
            }

            Payroll p = new Payroll();
            p.setUserId(Integer.parseInt(userIdStr));
            p.setPayPeriod(period);
            p.setStaffType(cmbAddType.getSelectedItem().toString());
            p.setBasicSalary(parseMoney(txtBasic.getText()));
            p.setTotalTeachingFee(parseMoney(txtTeaching.getText()));
            p.setBonusAmount(parseMoney(txtBonus.getText()));

            String res = ctrl.savePayroll(p);
            if ("SUCCESS".equals(res)) {
                JOptionPane.showMessageDialog(this, "✔ Đã lưu thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + res, "Lỗi Database", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
        }
    }

    private void recalc(JLabel lblTotal) {
        try {
            double b = parseMoney(txtBasic.getText());
            double t = parseMoney(txtTeaching.getText());
            double bn = parseMoney(txtBonus.getText());
            lblTotal.setText(nf.format(b + t + bn) + "đ");
        } catch (Exception ignored) {
            lblTotal.setText("0đ");
        }
    }

    private double parseMoney(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        String clean = input.trim().replace(",", "").replace(".", "").replace("đ", "");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void addFormLabel(JPanel card, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        card.add(l);
    }

    private void addGap(JPanel card, int h) { card.add(Box.createVerticalStrut(h)); }

    private JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 8, 4, 8)));
        return f;
    }
}