package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.controller.FinanceController;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.view.components.CustomButton;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình tính lương nhân viên.
 *
 * Lưu ý:
 * - Trong database vẫn dùng mã nội bộ: TEACHER, OFFICE.
 * - Trên giao diện chỉ hiển thị tiếng Việt:
 *   + TEACHER -> Giáo viên
 *   + OFFICE  -> Nhân viên giáo vụ
 */
public class PayrollPanel extends JPanel {

    private static final Color PRIMARY   = new Color(108, 92, 231);
    private static final Color BG_PAGE   = new Color(248, 249, 250);
    private static final Color BG_CARD   = Color.WHITE;
    private static final Color BORDER_C  = new Color(222, 226, 230);
    private static final Color TEXT_MAIN = new Color(33, 37, 41);
    private static final Color TEXT_MUTE = new Color(108, 117, 125);
    private static final Color SUCCESS   = new Color(25, 135, 84);
    private static final Color WARNING   = new Color(255, 159, 67);
    private static final Color DANGER    = new Color(238, 82, 83);

    private static final String STAFF_ALL_DISPLAY     = "Tất cả";
    private static final String STAFF_TEACHER_DISPLAY = "Giáo viên";
    private static final String STAFF_OFFICE_DISPLAY  = "Nhân viên giáo vụ";

    private static final String STAFF_TEACHER_CODE = "TEACHER";
    private static final String STAFF_OFFICE_CODE  = "OFFICE";

    private final FinanceController ctrl = new FinanceController();
    private final NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private JTextField txtFilterPeriod;
    private JTextField txtSearchName;
    private JComboBox<String> cmbStaffType;

    private JLabel lblTotalSalary;
    private JLabel lblCountTeacher;
    private JLabel lblCountOffice;
    private JLabel lblCountPending;

    private DefaultTableModel tableModel;
    private JTable tblPayroll;

    private DefaultTableModel pendingTableModel;
    private JTable tblPending;

    private JTextField txtUserId;
    private JTextField txtAddPeriod;
    private JTextField txtBasic;
    private JTextField txtTeaching;
    private JTextField txtBonus;
    private JComboBox<String> cmbAddType;
    private JLabel lblTotalValue;

    private CustomButton btnEdit;
    private CustomButton btnDelete;

    public PayrollPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

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

        panel.add(left, BorderLayout.WEST);
        return panel;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        panel.add(buildMetrics(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildTableCard(),
                buildAddCard()
        );
        split.setResizeWeight(0.72);
        split.setDividerSize(8);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
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

    private JPanel metricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_MUTE);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        card.add(buildTableToolbar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Đã nhập lương", buildPayrollTable());
        tabs.addTab("Chưa nhập lương", buildPendingTable());
        card.add(tabs, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildTableToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        txtFilterPeriod = new JTextField("05/2026", 7);
        txtFilterPeriod.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cmbStaffType = new JComboBox<>(new String[]{
                STAFF_ALL_DISPLAY,
                STAFF_TEACHER_DISPLAY,
                STAFF_OFFICE_DISPLAY
        });
        cmbStaffType.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearchName = new JTextField(10);
        txtSearchName.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        CustomButton btnLoad = new CustomButton("Lọc");
        btnLoad.setColors(PRIMARY, PRIMARY.darker());
        btnLoad.addActionListener(e -> loadData());

        btnEdit = new CustomButton("Sửa");
        btnEdit.setColors(WARNING, WARNING.darker());
        btnEdit.addActionListener(e -> prepareEdit());

        btnDelete = new CustomButton("Xóa");
        btnDelete.setColors(DANGER, new Color(194, 54, 22));
        btnDelete.addActionListener(e -> deletePayroll());

        right.add(btnEdit);
        right.add(btnDelete);
        right.add(new JLabel("Kỳ:"));
        right.add(txtFilterPeriod);
        right.add(new JLabel("Loại:"));
        right.add(cmbStaffType);
        right.add(new JLabel("Tên:"));
        right.add(txtSearchName);
        right.add(btnLoad);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildPayrollTable() {
        tableModel = new DefaultTableModel(
                new String[]{
                        "Mã NV",
                        "Nhân viên",
                        "Loại nhân viên",
                        "Lương cơ bản",
                        "Phí giảng dạy",
                        "Thưởng",
                        "Thực lĩnh"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblPayroll = new JTable(tableModel);
        styleTable(tblPayroll);
        return new JScrollPane(tblPayroll);
    }

    private JScrollPane buildPendingTable() {
        pendingTableModel = new DefaultTableModel(
                new String[]{"Mã NV", "Họ và tên", "Loại nhân viên"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblPending = new JTable(pendingTableModel);
        styleTable(tblPending);

        // Click vào hàng → tự điền form nhập lương (cột 2 đã là display text)
        tblPending.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPending.getSelectedRow();
                if (row >= 0) {
                    txtUserId.setText(pendingTableModel.getValueAt(row, 0).toString());
                    txtUserId.setEditable(false);
                    txtUserId.setBackground(new Color(241, 243, 245));
                    txtAddPeriod.setText(txtFilterPeriod.getText().trim());
                    cmbAddType.setSelectedItem(pendingTableModel.getValueAt(row, 2).toString());
                }
            }
        });

        return new JScrollPane(tblPending);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(241, 243, 245));
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(232, 228, 252));
        table.setSelectionForeground(TEXT_MAIN);
        table.setGridColor(new Color(233, 236, 239));
        table.setShowVerticalLines(false);
    }

    private JPanel buildAddCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel title = new JLabel("Nhập / sửa lương");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);
        card.add(title);
        addGap(card, 15);

        addFormLabel(card, "Mã nhân viên");
        txtUserId = styledField("");
        card.add(txtUserId);
        addGap(card, 10);

        addFormLabel(card, "Kỳ lương (MM/yyyy)");
        txtAddPeriod = styledField("05/2026");
        card.add(txtAddPeriod);
        addGap(card, 10);

        addFormLabel(card, "Loại nhân viên");
        cmbAddType = new JComboBox<>(new String[]{
                STAFF_TEACHER_DISPLAY,
                STAFF_OFFICE_DISPLAY
        });
        cmbAddType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAddType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        card.add(cmbAddType);
        addGap(card, 10);

        addFormLabel(card, "Lương cơ bản");
        txtBasic = styledField("0");
        card.add(txtBasic);
        addGap(card, 10);

        addFormLabel(card, "Phí giảng dạy");
        txtTeaching = styledField("0");
        card.add(txtTeaching);
        addGap(card, 10);

        addFormLabel(card, "Thưởng");
        txtBonus = styledField("0");
        card.add(txtBonus);
        addGap(card, 10);

        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalValue.setForeground(PRIMARY);

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.add(new JLabel("Tổng thực lĩnh:"), BorderLayout.WEST);
        totalPanel.add(lblTotalValue, BorderLayout.EAST);
        card.add(totalPanel);
        addGap(card, 15);

        javax.swing.event.DocumentListener documentListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
        };

        txtBasic.getDocument().addDocumentListener(documentListener);
        txtTeaching.getDocument().addDocumentListener(documentListener);
        txtBonus.getDocument().addDocumentListener(documentListener);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        CustomButton btnReset = new CustomButton("Làm mới");
        btnReset.setColors(new Color(108, 117, 125), new Color(108, 117, 125).darker());
        btnReset.addActionListener(e -> clearForm());

        CustomButton btnSave = new CustomButton("Lưu lương");
        btnSave.setColors(SUCCESS, SUCCESS.darker());
        btnSave.addActionListener(e -> savePayroll());

        buttonPanel.add(btnReset);
        buttonPanel.add(btnSave);
        card.add(buttonPanel);

        return card;
    }

    private void clearForm() {
        txtUserId.setText("");
        txtUserId.setEditable(true);
        txtUserId.setBackground(Color.WHITE);

        txtAddPeriod.setText(txtFilterPeriod.getText().trim());
        cmbAddType.setSelectedIndex(0);

        txtBasic.setText("0");
        txtTeaching.setText("0");
        txtBonus.setText("0");
        lblTotalValue.setText("0đ");
    }

    private void loadData() {
        String period = txtFilterPeriod.getText().trim();
        String selectedTypeDisplay = cmbStaffType.getSelectedItem() == null
                ? STAFF_ALL_DISPLAY
                : cmbStaffType.getSelectedItem().toString();

        String typeCode = staffDisplayToCodeForFilter(selectedTypeDisplay);
        String keyword = txtSearchName.getText().trim().toLowerCase();

        // ── Bảng đã nhập ──────────────────────────────────────────
        List<Payroll> list = ctrl.getPayroll(period, typeCode);
        tableModel.setRowCount(0);

        double totalSum = 0;
        int teacherCount = 0;
        int officeCount = 0;

        for (Payroll payroll : list) {
            String fullName = payroll.getFullName() == null ? "" : payroll.getFullName();
            if (!keyword.isEmpty() && !fullName.toLowerCase().contains(keyword)) continue;

            String staffTypeCode = payroll.getStaffType();
            String staffTypeDisplay = staffCodeToDisplay(staffTypeCode);

            tableModel.addRow(new Object[]{
                    payroll.getUserId(),
                    fullName,
                    staffTypeDisplay,
                    nf.format(payroll.getBasicSalary()) + "đ",
                    nf.format(payroll.getTotalTeachingFee()) + "đ",
                    nf.format(payroll.getBonusAmount()) + "đ",
                    nf.format(payroll.getTotalNet()) + "đ"
            });

            totalSum += payroll.getTotalNet();
            if (STAFF_TEACHER_CODE.equals(staffTypeCode)) teacherCount++;
            else if (STAFF_OFFICE_CODE.equals(staffTypeCode)) officeCount++;
        }

        // ── Bảng chưa nhập ────────────────────────────────────────
        List<Payroll> pending = ctrl.getWithoutPayroll(period, typeCode);
        pendingTableModel.setRowCount(0);

        for (Payroll p : pending) {
            String fullName = p.getFullName() == null ? "" : p.getFullName();
            if (!keyword.isEmpty() && !fullName.toLowerCase().contains(keyword)) continue;
            pendingTableModel.addRow(new Object[]{
                    p.getUserId(),
                    fullName,
                    staffCodeToDisplay(p.getStaffType())
            });
        }

        // ── Cập nhật metric cards ──────────────────────────────────
        lblTotalSalary.setText(nf.format(totalSum) + "đ");
        lblCountTeacher.setText(String.valueOf(teacherCount));
        lblCountOffice.setText(String.valueOf(officeCount));
        lblCountPending.setText(String.valueOf(pending.size()));
    }

    private void prepareEdit() {
        int row = tblPayroll.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng trong bảng!");
            return;
        }

        txtUserId.setText(tableModel.getValueAt(row, 0).toString());
        txtUserId.setEditable(false);
        txtUserId.setBackground(new Color(241, 243, 245));

        txtAddPeriod.setText(txtFilterPeriod.getText().trim());

        String staffTypeDisplay = tableModel.getValueAt(row, 2).toString();
        cmbAddType.setSelectedItem(staffTypeDisplay);

        txtBasic.setText(cleanMoney(tableModel.getValueAt(row, 3).toString()));
        txtTeaching.setText(cleanMoney(tableModel.getValueAt(row, 4).toString()));
        txtBonus.setText(cleanMoney(tableModel.getValueAt(row, 5).toString()));

        recalc(lblTotalValue);

        JOptionPane.showMessageDialog(
                this,
                "Đã chuyển dữ liệu sang form.\nBạn có thể chỉnh sửa và bấm Lưu lương.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void deletePayroll() {
        int row = tblPayroll.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
            return;
        }

        String userId = tableModel.getValueAt(row, 0).toString();
        String employeeName = tableModel.getValueAt(row, 1).toString();
        String period = txtFilterPeriod.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa bản ghi lương này?\n\n"
                        + "Nhân viên: " + employeeName + "\n"
                        + "Kỳ lương: " + period,
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String result = ctrl.deletePayroll(userId, period);

            if ("SUCCESS".equals(result)) {
                loadData();
                clearForm();
                JOptionPane.showMessageDialog(this, "Đã xóa bản ghi lương!");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + result);
            }
        }
    }

    private void savePayroll() {
        try {
            if (txtUserId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã nhân viên!");
                txtUserId.requestFocus();
                return;
            }

            if (txtAddPeriod.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập kỳ lương!");
                txtAddPeriod.requestFocus();
                return;
            }

            Payroll payroll = new Payroll();

            payroll.setUserId(Integer.parseInt(txtUserId.getText().trim()));
            payroll.setPayPeriod(txtAddPeriod.getText().trim());

            String staffDisplay = cmbAddType.getSelectedItem() == null
                    ? STAFF_TEACHER_DISPLAY
                    : cmbAddType.getSelectedItem().toString();

            payroll.setStaffType(staffDisplayToCodeForSave(staffDisplay));

            double basic = parseMoney(txtBasic.getText());
            double teaching = parseMoney(txtTeaching.getText());
            double bonus = parseMoney(txtBonus.getText());

            if (basic < 0 || teaching < 0 || bonus < 0) {
                JOptionPane.showMessageDialog(this, "Các khoản lương không được âm!");
                return;
            }

            double totalNet = basic + teaching + bonus;

            payroll.setBasicSalary(basic);
            payroll.setTotalTeachingFee(teaching);
            payroll.setBonusAmount(bonus);
            payroll.setTotalNet(totalNet);

            String result = ctrl.savePayroll(payroll);

            if ("SUCCESS".equals(result)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Cập nhật bảng lương thành công!");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + result);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên phải là số nguyên!");
            txtUserId.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: " + ex.getMessage());
        }
    }

    private void recalc(JLabel lblTotal) {
        double sum = parseMoney(txtBasic.getText())
                + parseMoney(txtTeaching.getText())
                + parseMoney(txtBonus.getText());

        lblTotal.setText(nf.format(sum) + "đ");
    }

    private double parseMoney(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0;
        }

        try {
            String clean = cleanMoney(input);

            if (clean.isEmpty()) {
                return 0;
            }

            return Double.parseDouble(clean);

        } catch (Exception e) {
            return 0;
        }
    }

    private String cleanMoney(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^0-9]", "");
    }

    /**
     * Dùng cho combobox lọc.
     * - Tất cả -> truyền null để lấy toàn bộ.
     * - Giáo viên -> TEACHER.
     * - Nhân viên giáo vụ -> OFFICE.
     */
    private String staffDisplayToCodeForFilter(String display) {
        if (STAFF_TEACHER_DISPLAY.equals(display)) {
            return STAFF_TEACHER_CODE;
        }

        if (STAFF_OFFICE_DISPLAY.equals(display)) {
            return STAFF_OFFICE_CODE;
        }

        return null;
    }

    /**
     * Dùng khi lưu xuống database.
     * Database vẫn giữ mã ổn định TEACHER/OFFICE.
     */
    private String staffDisplayToCodeForSave(String display) {
        if (STAFF_OFFICE_DISPLAY.equals(display)) {
            return STAFF_OFFICE_CODE;
        }

        return STAFF_TEACHER_CODE;
    }

    /**
     * Dùng khi hiển thị từ database lên giao diện.
     */
    private String staffCodeToDisplay(String code) {
        if (STAFF_TEACHER_CODE.equals(code)) {
            return STAFF_TEACHER_DISPLAY;
        }

        if (STAFF_OFFICE_CODE.equals(code)) {
            return STAFF_OFFICE_DISPLAY;
        }

        return code == null ? "" : code;
    }

    private void addFormLabel(JPanel card, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_MUTE);
        card.add(label);
    }

    private void addGap(JPanel card, int height) {
        card.add(Box.createVerticalStrut(height));
    }

    private JTextField styledField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }
}
