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

    private JTextField txtFilterPeriod, txtSearchName;
    private JComboBox<String> cmbStaffType;
    private JLabel lblTotalSalary, lblCountTeacher, lblCountOffice, lblCountPending;

    private DefaultTableModel tableModel;
    private JTable tblPayroll;

    private JTextField txtUserId, txtAddPeriod, txtBasic, txtTeaching, txtBonus;
    private JComboBox<String> cmbAddType;
    private JLabel lblTotalValue;
    private CustomButton btnEdit, btnDelete;

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

        CustomButton btnLoad = new CustomButton("Lọc");
        btnLoad.setColors(PRIMARY, PRIMARY.darker());
        btnLoad.addActionListener(e -> loadData());

        btnEdit = new CustomButton("Sửa");
        btnEdit.setColors(new Color(255, 159, 67), new Color(255, 159, 67).darker());
        btnEdit.addActionListener(e -> prepareEdit());

        btnDelete = new CustomButton("Xóa");
        btnDelete.setColors(new Color(238, 82, 83), new Color(194, 54, 22));
        btnDelete.addActionListener(e -> deletePayroll());

        right.add(btnEdit);
        right.add(btnDelete);
        right.add(new JLabel("Kỳ:"));
        right.add(txtFilterPeriod);
        right.add(cmbStaffType);
        right.add(new JLabel("Tên:"));
        right.add(txtSearchName);
        right.add(btnLoad);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(new String[]{"Mã NV", "Nhân viên", "Loại", "Cơ bản", "Giảng dạy", "Thưởng", "Thực lĩnh"}, 0) {
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

        JLabel title = new JLabel("Nhập/Sửa lương");
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

        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalValue.setForeground(PRIMARY);
        JPanel pTotal = new JPanel(new BorderLayout());
        pTotal.setOpaque(false);
        pTotal.add(new JLabel("Tổng tính toán: "), BorderLayout.WEST);
        pTotal.add(lblTotalValue, BorderLayout.EAST);
        card.add(pTotal); addGap(card, 15);

        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { recalc(lblTotalValue); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ recalc(lblTotalValue); }
        };
        txtBasic.getDocument().addDocumentListener(dl);
        txtTeaching.getDocument().addDocumentListener(dl);
        txtBonus.getDocument().addDocumentListener(dl);

      // ── KHU VỰC NÚT BẤM CHỨC NĂNG: LÀM MỚI & LƯU (SỬA ĐỔI) ──
        JPanel pButtons = new JPanel(new GridLayout(1, 2, 8, 0));
        pButtons.setOpaque(false);
        pButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // 1. Tạo nút Làm mới
        CustomButton btnReset = new CustomButton("Làm mới");
        btnReset.setColors(new Color(108, 117, 125), new Color(108, 117, 125).darker()); // Màu xám nhạt chuyên nghiệp
        btnReset.addActionListener(e -> clearForm());

        // 2. Tạo nút Lưu
        CustomButton btnSave = new CustomButton("Lưu lương");
        btnSave.setColors(SUCCESS, SUCCESS.darker());
        btnSave.addActionListener(e -> savePayroll());

        // Thêm các nút vào hàng và đưa vào Card form
        pButtons.add(btnReset);
        pButtons.add(btnSave);
        card.add(pButtons);
        // ────────────────────────────────────────────────────────

        return card;
    }

    // --- LOGIC FUNCTIONS ---
// Hàm xóa trắng form và đưa về trạng thái ban đầu (BỔ SUNG MỚI)
    private void clearForm() {
        txtUserId.setText("");
        txtUserId.setEditable(true); // Mở khóa lại ô nhập ID nhân viên
        txtAddPeriod.setText(txtFilterPeriod.getText()); // Đồng bộ lại kỳ lương theo bộ lọc
        cmbAddType.setSelectedIndex(0); // Đặt lại loại đầu tiên (TEACHER)
        txtBasic.setText("0");
        txtTeaching.setText("0");
        txtBonus.setText("0");
        lblTotalValue.setText("0đ");
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
                p.getUserId(), p.getFullName(), p.getStaffType(),
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

private void prepareEdit() {
        int row = tblPayroll.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng trong bảng!");
            return;
        }
        txtUserId.setText(tableModel.getValueAt(row, 0).toString());
        txtUserId.setEditable(false); // KHÓA ô nhập mã NV lại, tránh việc sửa nhầm ID gốc
        
        txtAddPeriod.setText(txtFilterPeriod.getText());
        cmbAddType.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        txtBasic.setText(tableModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", ""));
        txtTeaching.setText(tableModel.getValueAt(row, 4).toString().replaceAll("[^0-9]", ""));
        txtBonus.setText(tableModel.getValueAt(row, 5).toString().replaceAll("[^0-9]", ""));
        JOptionPane.showMessageDialog(this, "Đã chuyển dữ liệu sang form. Hãy sửa và nhấn Lưu.");
    }
    private void deletePayroll() {
        int row = tblPayroll.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa bản ghi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String uid = tableModel.getValueAt(row, 0).toString();
            String period = txtFilterPeriod.getText();
            String res = ctrl.deletePayroll(uid, period); 
            if ("SUCCESS".equals(res)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Đã xóa!");
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + res);
            }
        }
    }

 private void savePayroll() {
        try {
            // Kiểm tra dữ liệu đầu vào bắt buộc
            if (txtUserId.getText().trim().isEmpty() || txtAddPeriod.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã nhân viên và Kỳ lương!");
                return;
            }

            Payroll p = new Payroll();
            p.setUserId(Integer.parseInt(txtUserId.getText().trim()));
            p.setPayPeriod(txtAddPeriod.getText().trim());
            p.setStaffType(cmbAddType.getSelectedItem().toString());
            
            double basic = parseMoney(txtBasic.getText());
            double teaching = parseMoney(txtTeaching.getText());
            double bonus = parseMoney(txtBonus.getText());
            
            // Tính toán tổng số tiền thực nhận (Total Net)
            double totalNet = basic + teaching + bonus;

            p.setBasicSalary(basic);
            p.setTotalTeachingFee(teaching);
            p.setBonusAmount(bonus);
            p.setTotalNet(totalNet); // Đảm bảo dữ liệu Thực lĩnh được đẩy vào DB chính xác

            String res = ctrl.savePayroll(p);
            if ("SUCCESS".equals(res)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Cập nhật bảng lương thành công!");
                
                // Tùy chọn: Cho phép nhập tiếp mã khác bằng cách làm sạch form hoặc mở lại edit
               if ("SUCCESS".equals(res)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Cập nhật bảng lương thành công!");
                clearForm(); // Tự động đưa form về ban đầu sau khi lưu thành công
            }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: " + res);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã nhân viên phải là số nguyên!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi nhập liệu: " + ex.getMessage());
        }
    }
    private void recalc(JLabel lblTotal) {
        try {
            double sum = parseMoney(txtBasic.getText()) + parseMoney(txtTeaching.getText()) + parseMoney(txtBonus.getText());
            lblTotal.setText(nf.format(sum) + "đ");
        } catch (Exception ignored) { lblTotal.setText("0đ"); }
    }

    private double parseMoney(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try { return Double.parseDouble(input.trim().replaceAll("[^0-9]", "")); } 
        catch (Exception e) { return 0; }
    }

    private void addFormLabel(JPanel card, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MUTE);
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
