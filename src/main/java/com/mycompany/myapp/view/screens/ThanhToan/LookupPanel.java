package com.mycompany.myapp.view.screens.ThanhToan;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.service.InvoiceDetailService;
import com.mycompany.myapp.service.InvoiceService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Panel Chi tiết học phí học viên.
 * Layout: LEFT = danh sách học viên | RIGHT TOP = hóa đơn | RIGHT BOTTOM = chi tiết HĐ + form CRUD
 */
public class LookupPanel extends JPanel {

    // ── Design tokens ──
    private static final Color PRIMARY      = new Color(108,  92, 231);
    private static final Color PRIMARY_DARK = new Color( 83,  68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);
    private static final Color BG_PAGE      = new Color(248, 250, 252);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color BORDER_C     = new Color(226, 232, 240);
    private static final Color TEXT_MAIN    = new Color( 15,  23,  42);
    private static final Color TEXT_MUTE    = new Color(100, 116, 139);
    private static final Color SUCCESS      = new Color( 22, 163,  74);
    private static final Color WARNING_C    = new Color(202, 138,   4);
    private static final Color DANGER       = new Color(220,  38,  38);
    private static final Color TEAL         = new Color( 20, 184, 166);

    private final InvoiceDetailService service        = new InvoiceDetailService();
    private final InvoiceService        invoiceService = new InvoiceService();
    private final NumberFormat nf  = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    // Metric labels
    private JLabel lblTotal, lblPaid, lblPartial, lblUnpaid;

    // Student panel (LEFT)
    private DefaultTableModel                 stuModel;
    private JTable                            tblStudents;
    private TableRowSorter<DefaultTableModel> stuSorter;
    private JTextField                        txtSearch;

    // Invoice panel (RIGHT TOP)
    private DefaultTableModel invModel;
    private JTable            tblInvoices;
    private JLabel            lblStudentTitle;

    // Detail panel (RIGHT BOTTOM)
    private DefaultTableModel    detModel;   // cols: class_id(hidden), class_name, amount(Double)
    private JTable               tblDetails;
    private JLabel               lblInvoiceTitle;
    private JComboBox<ClassItem> cmbClass;
    private JTextField           tfAmount;
    private JLabel               lblFormMode;
    private ActionButton         btnSaveDetail;

    // State
    private int selectedStudentId = -1;
    private int selectedInvoiceId = -1;
    private int editingClassId    = -1; // -1 = chế độ thêm mới

    // ════════════════════════════════════════════════════════
    public LookupPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadStudents(null);
    }

    // ═══════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Chi tiết học phí học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Xem và quản lý chi tiết hóa đơn học phí theo từng học viên");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);
        titles.add(title); titles.add(sub);

        ActionButton btnRefresh = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(110, 40));
        btnRefresh.addActionListener(e -> loadStudents(txtSearch.getText().trim()));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(btnRefresh);
        p.add(titles,  BorderLayout.WEST);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    // ═══════════════════════════════════════════
    // CONTENT
    // ═══════════════════════════════════════════
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildMetrics(), BorderLayout.NORTH);
        p.add(buildMain(),    BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 78));
        lblTotal   = new JLabel("—"); lblPaid    = new JLabel("—");
        lblPartial = new JLabel("—"); lblUnpaid  = new JLabel("—");
        row.add(metricCard("Tổng học viên",  lblTotal,   PRIMARY));
        row.add(metricCard("Đã đóng đủ",     lblPaid,    SUCCESS));
        row.add(metricCard("Đóng một phần",  lblPartial, WARNING_C));
        row.add(metricCard("Chưa đóng",      lblUnpaid,  DANGER));
        return row;
    }

    private JPanel metricCard(String t, JLabel val, Color accent) {
        JPanel card = new RoundedPanel(12, BG_CARD);
        card.setLayout(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel(t);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTE);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(accent);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMain() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildStudentCard(), buildRightPanel());
        split.setResizeWeight(0.32);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_PAGE);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ── LEFT: danh sách học viên ──
    private JPanel buildStudentCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Danh sách học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(0, 34));
        txtSearch.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 10, 4, 10)));
        txtSearch.setToolTipText("Tìm theo tên hoặc mã học viên...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterStudents(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title,     BorderLayout.NORTH);
        north.add(txtSearch, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        stuModel = new DefaultTableModel(
                new String[]{"Mã HV", "Tên học viên", "HĐ", "Trạng thái"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStudents = new JTable(stuModel);
        configureTable(tblStudents);
        tblStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStudents.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        int[] cw = {58, 170, 32, 78};
        for (int i = 0; i < cw.length; i++) tblStudents.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        stuSorter = new TableRowSorter<>(stuModel);
        tblStudents.setRowSorter(stuSorter);
        tblStudents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblStudents.getSelectedRow() >= 0) onStudentSelected();
        });

        JScrollPane sp = new JScrollPane(tblStudents);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── RIGHT: hóa đơn (trên) + chi tiết (dưới) ──
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        JPanel invCard = buildInvoiceCard();
        invCard.setPreferredSize(new Dimension(0, 210));
        p.add(invCard,          BorderLayout.NORTH);
        p.add(buildDetailCard(), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildInvoiceCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Title bar + nút tạo HĐ mới
        JPanel invNorth = new JPanel(new BorderLayout(8, 0));
        invNorth.setOpaque(false);

        lblStudentTitle = new JLabel("← Chọn một học viên ở bên trái");
        lblStudentTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStudentTitle.setForeground(TEXT_MUTE);

        ActionButton btnNewInvoice = new ActionButton("+ Tạo HĐ mới", PRIMARY);
        btnNewInvoice.setPreferredSize(new Dimension(130, 32));
        btnNewInvoice.setToolTipText("Tạo hóa đơn trống mới cho học viên đang chọn");
        btnNewInvoice.addActionListener(e -> createNewInvoice());

        invNorth.add(lblStudentTitle, BorderLayout.CENTER);
        invNorth.add(btnNewInvoice,   BorderLayout.EAST);
        card.add(invNorth, BorderLayout.NORTH);

        invModel = new DefaultTableModel(
                new String[]{"Mã HĐ", "Ngày lập", "Học phí", "Giảm giá", "Thực thu", "Đã nộp", "Còn nợ", "TT"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblInvoices = new JTable(invModel);
        configureTable(tblInvoices);
        tblInvoices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblInvoices.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : new int[]{2, 3, 4, 5, 6})
            tblInvoices.getColumnModel().getColumn(c).setCellRenderer(rightR);
        int[] iw = {54, 86, 110, 86, 110, 110, 90, 72};
        for (int i = 0; i < iw.length; i++) tblInvoices.getColumnModel().getColumn(i).setPreferredWidth(iw[i]);

        tblInvoices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblInvoices.getSelectedRow() >= 0) onInvoiceSelected();
        });

        JScrollPane sp = new JScrollPane(tblInvoices);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDetailCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Title bar + action buttons
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        lblInvoiceTitle = new JLabel("← Chọn một hóa đơn ở trên");
        lblInvoiceTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblInvoiceTitle.setForeground(TEXT_MUTE);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        ActionButton btnNew    = new ActionButton("+ Thêm", TEAL);
        ActionButton btnEdit   = new ActionButton("Sửa",    Color.WHITE, PRIMARY, PRIMARY);
        ActionButton btnDelete = new ActionButton("Xóa",    Color.WHITE, DANGER,  DANGER);
        btnNew   .setPreferredSize(new Dimension(90, 32));
        btnEdit  .setPreferredSize(new Dimension(70, 32));
        btnDelete.setPreferredSize(new Dimension(64, 32));
        btnNew   .addActionListener(e -> prepareAdd());
        btnEdit  .addActionListener(e -> prepareEdit());
        btnDelete.addActionListener(e -> deleteDetail());
        btnRow.add(btnNew); btnRow.add(btnEdit); btnRow.add(btnDelete);

        top.add(lblInvoiceTitle, BorderLayout.CENTER);
        top.add(btnRow,          BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Detail table — col 0: class_id (ẩn), col 1: tên lớp, col 2: amount (Double)
        detModel = new DefaultTableModel(
                new String[]{"_id", "Lớp học", "Học phí (đ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDetails = new JTable(detModel);
        configureTable(tblDetails);
        tblDetails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Ẩn cột class_id
        tblDetails.getColumnModel().getColumn(0).setMinWidth(0);
        tblDetails.getColumnModel().getColumn(0).setMaxWidth(0);
        tblDetails.getColumnModel().getColumn(0).setWidth(0);
        tblDetails.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblDetails.getColumnModel().getColumn(2).setPreferredWidth(160);

        // Renderer cột học phí
        tblDetails.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Double) setText(nf.format((Double) v) + "đ");
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (sel) { setBackground(PRIMARY_SOFT); setForeground(PRIMARY_DARK); }
                else     { setBackground(r % 2 == 0 ? Color.WHITE : new Color(248,250,252)); setForeground(TEXT_MAIN); }
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tblDetails);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);

        card.add(buildDetailForm(), BorderLayout.SOUTH);
        setDetailFormEnabled(false);
        return card;
    }

    private JPanel buildDetailForm() {
        JPanel form = new RoundedPanel(10, new Color(248, 250, 252));
        form.setLayout(new BorderLayout(0, 8));
        form.setBorder(new EmptyBorder(10, 12, 10, 12));
        form.setPreferredSize(new Dimension(0, 100));

        lblFormMode = new JLabel("Thêm chi tiết mới");
        lblFormMode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFormMode.setForeground(TEXT_MAIN);
        form.add(lblFormMode, BorderLayout.NORTH);

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fields.setOpaque(false);

        cmbClass = new JComboBox<>();
        cmbClass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbClass.setBackground(Color.WHITE);
        cmbClass.setPreferredSize(new Dimension(230, 34));
        cmbClass.addActionListener(e -> {
            // Auto-điền học phí khi chọn lớp ở chế độ thêm mới
            if (editingClassId < 0) {
                ClassItem item = (ClassItem) cmbClass.getSelectedItem();
                if (item != null && item.id > 0)
                    tfAmount.setText(String.valueOf((long) item.fee));
            }
        });

        tfAmount = new JTextField("0", 10);
        tfAmount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfAmount.setPreferredSize(new Dimension(140, 34));
        tfAmount.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 8, 4, 8)));
        tfAmount.setToolTipText("Nhập học phí (VNĐ)");

        fields.add(fieldLabel("Lớp:"));    fields.add(cmbClass);
        fields.add(Box.createHorizontalStrut(8));
        fields.add(fieldLabel("Học phí:")); fields.add(tfAmount);
        form.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        ActionButton btnCancel = new ActionButton("Hủy", Color.WHITE, TEXT_MUTE, BORDER_C);
        btnCancel.setPreferredSize(new Dimension(74, 32));
        btnCancel.addActionListener(e -> clearDetailForm());

        btnSaveDetail = new ActionButton("Lưu chi tiết", TEAL);
        btnSaveDetail.setPreferredSize(new Dimension(120, 32));
        btnSaveDetail.addActionListener(e -> saveDetail());

        btnRow.add(btnCancel); btnRow.add(btnSaveDetail);
        form.add(btnRow, BorderLayout.EAST);
        return form;
    }

    // ═══════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════

    private void loadStudents(String keyword) {
        try {
            List<Map<String, Object>> rows = service.getStudentSummary(
                    (keyword == null || keyword.isEmpty()) ? null : keyword);
            stuModel.setRowCount(0);
            int total = rows.size(), paid = 0, partial = 0, unpaid = 0;
            for (Map<String, Object> r : rows) {
                String st = r.get("overall_status").toString();
                stuModel.addRow(new Object[]{
                    r.get("student_id"),
                    r.get("full_name"),
                    r.get("invoice_count"),
                    st
                });
                if ("PAID".equals(st))         paid++;
                else if ("PARTIAL".equals(st)) partial++;
                else if ("UNPAID".equals(st))  unpaid++;
            }
            lblTotal.setText(String.valueOf(total));
            lblPaid.setText(String.valueOf(paid));
            lblPartial.setText(String.valueOf(partial));
            lblUnpaid.setText(String.valueOf(unpaid));
        } catch (Exception e) { showError("Lỗi tải danh sách học viên: " + e.getMessage()); }
    }

    private void filterStudents() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { stuSorter.setRowFilter(null); return; }
        final String kwLower = kw.toLowerCase();
        stuSorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Cột 0: Mã HV (Integer) — chuyển sang String để so sánh
                String id   = String.valueOf(entry.getValue(0));
                // Cột 1: Tên học viên (String)
                String name = entry.getStringValue(1).toLowerCase();
                return id.contains(kwLower) || name.contains(kwLower);
            }
        });
    }

    private void createNewInvoice() {
        if (selectedStudentId < 0) {
            showWarn("Vui lòng chọn một học viên trước khi tạo hóa đơn!");
            return;
        }
        // Lấy tên học viên từ bảng
        String studentName = "";
        int vr = tblStudents.getSelectedRow();
        if (vr >= 0) {
            int mr = tblStudents.convertRowIndexToModel(vr);
            studentName = stuModel.getValueAt(mr, 1).toString();
        }

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        InvoiceFormDialog dlg = new InvoiceFormDialog(frame, selectedStudentId, studentName);
        dlg.setVisible(true);

        if (!dlg.isConfirmed()) return;
        Invoice inv = dlg.getResult();

        String msg = invoiceService.addInvoice(inv);
        if (!"SUCCESS".equals(msg)) {
            showError("Lỗi tạo hóa đơn: " + msg);
            return;
        }

        refreshInvoices();
        loadStudents(txtSearch.getText().trim());
        // Tự động chọn hóa đơn mới nhất (đầu danh sách — ORDER BY created_at DESC)
        if (invModel.getRowCount() > 0) {
            tblInvoices.setRowSelectionInterval(0, 0);
            tblInvoices.scrollRectToVisible(tblInvoices.getCellRect(0, 0, true));
        }
        showInfo("Đã tạo hóa đơn mới thành công!");
    }

    private void onStudentSelected() {
        int vr = tblStudents.getSelectedRow();
        if (vr < 0) return;
        int mr = tblStudents.convertRowIndexToModel(vr);
        selectedStudentId = (Integer) stuModel.getValueAt(mr, 0);
        String name = stuModel.getValueAt(mr, 1).toString();
        lblStudentTitle.setText("Hóa đơn của: " + name);
        lblStudentTitle.setForeground(TEXT_MAIN);
        refreshInvoices();
        // Reset detail
        detModel.setRowCount(0);
        selectedInvoiceId = -1;
        lblInvoiceTitle.setText("← Chọn một hóa đơn ở trên");
        lblInvoiceTitle.setForeground(TEXT_MUTE);
        setDetailFormEnabled(false);
        clearDetailForm();
    }

    private void refreshInvoices() {
        if (selectedStudentId < 0) return;
        try {
            List<Map<String, Object>> rows = service.getInvoicesByStudent(selectedStudentId);
            invModel.setRowCount(0);
            for (Map<String, Object> r : rows) {
                double fin  = (Double) r.get("final_amount");
                double paid = (Double) r.get("amount_paid");
                double debt = Math.max(0, fin - paid);
                java.util.Date dt = (java.util.Date) r.get("created_at");
                invModel.addRow(new Object[]{
                    r.get("invoice_id"),
                    dt != null ? sdf.format(dt) : "—",
                    nf.format((Double) r.get("total_amount")) + "đ",
                    nf.format((Double) r.get("discount_amt")) + "đ",
                    nf.format(fin)  + "đ",
                    nf.format(paid) + "đ",
                    nf.format(debt) + "đ",
                    r.get("status")
                });
            }
        } catch (Exception e) { showError("Lỗi tải hóa đơn: " + e.getMessage()); }
    }

    private void onInvoiceSelected() {
        int vr = tblInvoices.getSelectedRow();
        if (vr < 0) return;
        int mr = tblInvoices.convertRowIndexToModel(vr);
        selectedInvoiceId = (Integer) invModel.getValueAt(mr, 0);
        lblInvoiceTitle.setText("Chi tiết HĐ #" + selectedInvoiceId);
        lblInvoiceTitle.setForeground(TEXT_MAIN);
        refreshDetails();
        clearDetailForm();
        setDetailFormEnabled(true);
    }

    private void refreshDetails() {
        if (selectedInvoiceId < 0) return;
        try {
            List<Map<String, Object>> rows = service.getDetailsByInvoice(selectedInvoiceId);
            detModel.setRowCount(0);
            for (Map<String, Object> r : rows) {
                detModel.addRow(new Object[]{
                    r.get("class_id"),   // col 0 (ẩn)
                    r.get("class_name"), // col 1
                    r.get("amount")      // col 2 (Double → renderer format)
                });
            }
        } catch (Exception e) { showError("Lỗi tải chi tiết: " + e.getMessage()); }
    }

    private void prepareAdd() {
        if (selectedInvoiceId < 0) { showWarn("Vui lòng chọn hóa đơn trước."); return; }
        editingClassId = -1;
        lblFormMode.setText("Thêm chi tiết mới");
        btnSaveDetail.setText("Lưu chi tiết");
        cmbClass.setEnabled(true);
        loadClassCombo();
        tfAmount.setText("0");
    }

    private void prepareEdit() {
        if (selectedInvoiceId < 0) { showWarn("Vui lòng chọn hóa đơn trước."); return; }
        int vr = tblDetails.getSelectedRow();
        if (vr < 0) { showWarn("Vui lòng chọn một chi tiết để sửa."); return; }
        int mr = tblDetails.convertRowIndexToModel(vr);

        editingClassId       = (Integer) detModel.getValueAt(mr, 0);
        String className     = detModel.getValueAt(mr, 1).toString();
        double amount        = (Double)  detModel.getValueAt(mr, 2);

        lblFormMode.setText("Sửa: " + className);
        btnSaveDetail.setText("Cập nhật");

        cmbClass.removeAllItems();
        cmbClass.addItem(new ClassItem(editingClassId, className, amount));
        cmbClass.setEnabled(false); // không đổi lớp, chỉ đổi học phí

        tfAmount.setText(String.valueOf((long) amount));
        tfAmount.requestFocusInWindow();
    }

    private void deleteDetail() {
        if (selectedInvoiceId < 0) { showWarn("Vui lòng chọn hóa đơn trước."); return; }
        int vr = tblDetails.getSelectedRow();
        if (vr < 0) { showWarn("Vui lòng chọn một chi tiết để xóa."); return; }
        int mr = tblDetails.convertRowIndexToModel(vr);

        int    classId   = (Integer) detModel.getValueAt(mr, 0);
        String className = detModel.getValueAt(mr, 1).toString();

        if (JOptionPane.showConfirmDialog(this,
                "Xóa lớp \"" + className + "\" khỏi HĐ #" + selectedInvoiceId + "?\n" +
                "Học phí lớp này sẽ được trừ khỏi tổng hóa đơn.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            try {
                service.deleteDetail(selectedInvoiceId, classId);
                refreshDetails();
                refreshInvoices();
                clearDetailForm();
                showInfo("Đã xóa chi tiết thành công!");
            } catch (Exception e) { showError(e.getMessage()); }
        }
    }

    private void saveDetail() {
        if (selectedInvoiceId < 0) { showWarn("Chưa chọn hóa đơn."); return; }
        double amount;
        try {
            amount = Double.parseDouble(tfAmount.getText().trim().replace(",", ""));
        } catch (NumberFormatException e) { showWarn("Học phí không hợp lệ!"); return; }

        try {
            if (editingClassId < 0) {
                ClassItem cls = (ClassItem) cmbClass.getSelectedItem();
                if (cls == null || cls.id <= 0) { showWarn("Vui lòng chọn lớp học!"); return; }
                service.addDetail(selectedInvoiceId, cls.id, amount);
                showInfo("Đã thêm chi tiết thành công!");
            } else {
                service.updateDetail(selectedInvoiceId, editingClassId, amount);
                showInfo("Đã cập nhật học phí thành công!");
            }
            refreshDetails();
            refreshInvoices();
            clearDetailForm();
        } catch (Exception e) { showError(e.getMessage()); }
    }

    private void loadClassCombo() {
        try {
            List<Map<String, Object>> rows = service.getAvailableClasses(selectedInvoiceId);
            cmbClass.removeAllItems();
            cmbClass.addItem(new ClassItem(0, "-- Chọn lớp học --", 0));
            for (Map<String, Object> r : rows) {
                cmbClass.addItem(new ClassItem(
                    (Integer) r.get("class_id"),
                    r.get("class_name").toString(),
                    (Double)  r.get("tuition_fee")
                ));
            }
        } catch (Exception e) { showError("Lỗi tải danh sách lớp: " + e.getMessage()); }
    }

    private void clearDetailForm() {
        editingClassId = -1;
        lblFormMode.setText("Thêm chi tiết mới");
        btnSaveDetail.setText("Lưu chi tiết");
        cmbClass.setEnabled(true);
        cmbClass.removeAllItems();
        tfAmount.setText("0");
        tblDetails.clearSelection();
    }

    private void setDetailFormEnabled(boolean on) {
        cmbClass.setEnabled(on);
        tfAmount.setEnabled(on);
        btnSaveDetail.setEnabled(on);
        tfAmount.setBackground(on ? Color.WHITE : new Color(248, 250, 252));
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_MUTE);
        return l;
    }

    private void configureTable(JTable t) {
        t.setRowHeight(34);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_MAIN);
        t.setShowVerticalLines(true); t.setShowHorizontalLines(true);
        t.setGridColor(BORDER_C);
        t.setSelectionBackground(PRIMARY_SOFT); t.setSelectionForeground(PRIMARY_DARK);
        t.setFillsViewportHeight(true); t.setAutoCreateRowSorter(false);
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setBackground(new Color(241, 245, 249)); h.setForeground(new Color(71, 85, 105));
        h.setPreferredSize(new Dimension(0, 34)); h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private void showInfo (String m) { JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn (String m) { JOptionPane.showMessageDialog(this, m, "Cảnh báo",  JOptionPane.WARNING_MESSAGE);     }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Lỗi",       JOptionPane.ERROR_MESSAGE);       }

    // ═══════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════
    private static class ClassItem {
        final int id; final String name; final double fee;
        ClassItem(int id, String name, double fee) { this.id = id; this.name = name; this.fee = fee; }
        @Override public String toString() { return name; }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setHorizontalAlignment(CENTER);
            setOpaque(true);
            setBorder(new EmptyBorder(2, 6, 2, 6));
            String s = v == null ? "" : v.toString();
            switch (s) {
                case "PAID":
                    setBackground(sel ? new Color(187,247,208) : new Color(220,252,231));
                    setForeground(new Color(22, 101, 52));  setText("Đã đủ");    break;
                case "PARTIAL":
                    setBackground(sel ? new Color(254,240,138) : new Color(254,249,195));
                    setForeground(new Color(133, 77,  14)); setText("Còn nợ");   break;
                case "UNPAID":
                    setBackground(sel ? new Color(254,202,202) : new Color(254,226,226));
                    setForeground(new Color(153,  27,  27)); setText("Chưa nộp"); break;
                case "NONE":
                    setBackground(sel ? new Color(226,232,240) : new Color(241,245,249));
                    setForeground(new Color(100,116,139)); setText("Chưa có HĐ"); break;
                default:
                    setBackground(sel ? new Color(226,232,240) : Color.WHITE);
                    setForeground(new Color(15,23,42)); setText(s); break;
            }
            return this;
        }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(v == null ? "" : v.toString());
            if (sel) { setBackground(new Color(238,234,255)); setForeground(new Color(83,68,207)); }
            else     { setBackground(r%2==0 ? Color.WHITE : new Color(248,250,252)); setForeground(new Color(15,23,42)); }
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return this;
        }
    }

    private static class ActionButton extends JButton {
        private final Color bg, fg, border; private boolean hover;
        ActionButton(String t, Color bg) { this(t, bg, Color.WHITE, bg); }
        ActionButton(String t, Color bg, Color fg, Color brd) {
            super(t); this.bg = bg; this.fg = fg; this.border = brd;
            setFont(new Font("Segoe UI", Font.BOLD, 13)); setForeground(fg);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setMargin(new Insets(6, 12, 6, 12));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? new Color(226,232,240) : hover ? bg.darker() : bg);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            g2.setColor(!isEnabled() ? new Color(226,232,240) : border);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius; private final Color bg;
        RoundedPanel(int r, Color bg) { this.radius = r; this.bg = bg; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.setColor(new Color(226,232,240)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.dispose(); super.paintComponent(g);
        }
    }
}
