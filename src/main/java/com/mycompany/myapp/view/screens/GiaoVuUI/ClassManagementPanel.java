package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.model.SubjectDTO;
import com.mycompany.myapp.service.StudyClassService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Panel quản lý lớp học (CRUD bảng STUDY_CLASS).
 * Xếp lớp học viên đã tách sang ClassEnrollmentPanel.
 */
public class ClassManagementPanel extends JPanel {

    // ── Design tokens (khớp với StudentManagementPanel) ──
    private static final Color PRIMARY      = new Color(108,  92, 231);
    private static final Color PRIMARY_DARK = new Color( 83,  68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);
    private static final Color BG_PAGE      = new Color(248, 250, 252);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color BORDER_C     = new Color(226, 232, 240);
    private static final Color TEXT_MAIN    = new Color( 15,  23,  42);
    private static final Color TEXT_MUTE    = new Color(100, 116, 139);
    private static final Color SUCCESS      = new Color( 22, 163,  74);
    private static final Color DANGER       = new Color(220,  38,  38);
    private static final Color WARNING_CLR  = new Color(245, 158,  11);
    private static final Color DISABLED     = new Color(148, 163, 184);

    private final StudyClassService service = new StudyClassService();
    private final SimpleDateFormat  sdf     = new SimpleDateFormat("dd/MM/yyyy");

    // ── Tab 1 components ──
    private DefaultTableModel          classModel;
    private JTable                     tblClasses;
    private TableRowSorter<DefaultTableModel> classRowSorter;
    private JTextField txtClassId, txtClassName, txtFee, txtAllowance,
                       txtStartDate, txtEndDate, txtClassSearch;
    private JComboBox<SubjectItem>     cmbSubject;
    private JComboBox<String>          cmbClassType;
    private JLabel lblFormMode, lblTotalClasses, lblClassStatus;
    private ActionButton btnSave, btnDelete;

    // ────────────────────────────────────────────────────────
    public ClassManagementPanel() {
        sdf.setLenient(false);
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);
        add(buildClassTab(), BorderLayout.CENTER);
        loadSubjects();
        refreshClassTab();
    }

    // ════════════════════════════════════════════════════════
    // TAB 1 – QUẢN LÝ LỚP HỌC
    // ════════════════════════════════════════════════════════

    private JPanel buildClassTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 16));
        tab.setBackground(BG_PAGE);
        tab.setBorder(new EmptyBorder(22, 28, 22, 28));
        tab.add(buildClassHeader(), BorderLayout.NORTH);
        tab.add(buildClassMain(),   BorderLayout.CENTER);
        return tab;
    }

    // ── Header ──
    private JPanel buildClassHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Quản lý lớp học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Tạo mới, chỉnh sửa và quản lý các lớp đào tạo");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);
        titleBox.add(title);
        titleBox.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        lblClassStatus = new JLabel("Sẵn sàng");
        lblClassStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClassStatus.setForeground(TEXT_MUTE);

        ActionButton btnNew     = new ActionButton("+ Tạo mới", PRIMARY);
        ActionButton btnRefresh = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnNew    .setPreferredSize(new Dimension(118, 40));
        btnRefresh.setPreferredSize(new Dimension(108, 40));
        btnNew    .addActionListener(e -> clearClassForm());
        btnRefresh.addActionListener(e -> { loadSubjects(); refreshClassTab(); });

        actions.add(lblClassStatus);
        actions.add(btnNew);
        actions.add(btnRefresh);

        p.add(titleBox, BorderLayout.WEST);
        p.add(actions,  BorderLayout.EAST);
        return p;
    }

    // ── Summary + split ──
    private JPanel buildClassMain() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        // Summary strip
        JPanel strip = new RoundedPanel(14, BG_CARD);
        strip.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        strip.setPreferredSize(new Dimension(0, 52));
        lblTotalClasses = new JLabel("0");
        lblTotalClasses.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalClasses.setForeground(PRIMARY);
        JLabel lbl1 = new JLabel("  Tổng lớp học đang hoạt động");
        lbl1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl1.setForeground(TEXT_MUTE);
        strip.add(lblTotalClasses);
        strip.add(lbl1);
        content.add(strip, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildClassTableCard(), buildClassFormCard());
        split.setResizeWeight(0.65);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_PAGE);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    // ── Bảng danh sách lớp ──
    private JPanel buildClassTableCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(0, 8));
        toolbar.setOpaque(false);
        JLabel tableTitle = new JLabel("Danh sách lớp học");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_MAIN);
        toolbar.add(tableTitle, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        JLabel lblS = new JLabel("Tìm kiếm:");
        lblS.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblS.setForeground(TEXT_MAIN);
        txtClassSearch = styledField("");
        txtClassSearch.setPreferredSize(new Dimension(320, 34));
        txtClassSearch.setToolTipText("Tìm theo tên lớp, môn học, loại...");
        txtClassSearch.getDocument().addDocumentListener(docListener(() -> filterClassTable()));
        searchRow.add(lblS);
        searchRow.add(txtClassSearch);
        toolbar.add(searchRow, BorderLayout.CENTER);
        card.add(toolbar, BorderLayout.NORTH);

        // Table
        classModel = new DefaultTableModel(
            new String[]{"ID", "Tên lớp", "Môn học", "Loại", "Học phí (VNĐ)",
                         "Ngày KG", "Ngày KT", "Số HV"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblClasses = new JTable(classModel);
        configureTable(tblClasses);
        tblClasses.getColumnModel().getColumn(3).setCellRenderer(new TypeBadgeRenderer());
        tblClasses.getColumnModel().getColumn(7).setCellRenderer(new CountRenderer());
        int[] cw = {50, 210, 150, 65, 130, 100, 100, 60};
        for (int i = 0; i < cw.length; i++)
            tblClasses.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        classRowSorter = new TableRowSorter<>(classModel);
        tblClasses.setRowSorter(classRowSorter);
        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClasses.getSelectedRow() >= 0)
                fillClassForm();
        });

        JScrollPane sp = new JScrollPane(tblClasses);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── Form thêm/sửa lớp ──
    private JPanel buildClassFormCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 10, 0, 0));
        wrapper.setPreferredSize(new Dimension(380, 0));

        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout());

        // Form header
        JPanel fhdr = new JPanel(new BorderLayout());
        fhdr.setOpaque(false);
        fhdr.setBorder(new EmptyBorder(16, 20, 10, 20));
        JLabel fTitle = new JLabel("Thông tin lớp học");
        fTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fTitle.setForeground(TEXT_MAIN);
        lblFormMode = new JLabel("Tạo lớp mới");
        lblFormMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormMode.setForeground(TEXT_MUTE);
        JPanel fb = new JPanel(new GridLayout(2, 1, 0, 4));
        fb.setOpaque(false);
        fb.add(fTitle);
        fb.add(lblFormMode);
        fhdr.add(fb);
        card.add(fhdr, BorderLayout.NORTH);

        // Fields
        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBorder(new EmptyBorder(0, 20, 10, 20));

        txtClassId = fieldRow(fields, "Mã lớp (tự sinh)", false);
        txtClassId.setBackground(new Color(248, 250, 252));
        txtClassName = fieldRow(fields, "Tên lớp học (*)", true);

        formLabel(fields, "Môn học (*)");
        cmbSubject = new JComboBox<>();
        styleCombo(cmbSubject);
        fields.add(cmbSubject);
        fields.add(Box.createVerticalStrut(12));

        formLabel(fields, "Loại lớp (*)");
        cmbClassType = new JComboBox<>(new String[]{"REG – Lớp thường", "ADV – Lớp nâng cao"});
        styleCombo(cmbClassType);
        fields.add(cmbClassType);
        fields.add(Box.createVerticalStrut(12));

        txtFee        = fieldRow(fields, "Học phí (VNĐ) (*)", true);
        txtAllowance  = fieldRow(fields, "Phụ cấp giáo viên (VNĐ)", true);
        txtStartDate  = fieldRow(fields, "Ngày khai giảng  dd/MM/yyyy (*)", true);
        txtEndDate    = fieldRow(fields, "Ngày kết thúc  dd/MM/yyyy (*)", true);

        JScrollPane fs = new JScrollPane(fields);
        fs.setBorder(null);
        fs.setOpaque(false);
        fs.getViewport().setOpaque(false);
        fs.getVerticalScrollBar().setUnitIncrement(16);
        fs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(fs, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 3, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        footer.setPreferredSize(new Dimension(0, 66));

        ActionButton btnClear = new ActionButton("Nhập lại", Color.WHITE, TEXT_MUTE, TEXT_MUTE);
        btnDelete = new ActionButton("Xóa lớp", Color.WHITE, DANGER, DANGER);
        btnSave   = new ActionButton("Lưu", SUCCESS);

        btnClear .addActionListener(e -> clearClassForm());
        btnDelete.addActionListener(e -> deleteClass());
        btnSave  .addActionListener(e -> saveClass());

        footer.add(btnClear);
        footer.add(btnDelete);
        footer.add(btnSave);
        card.add(footer, BorderLayout.SOUTH);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }


    // ════════════════════════════════════════════════════════
    // LOGIC – TAB 1
    // ════════════════════════════════════════════════════════

    private void loadSubjects() {
        try {
            List<SubjectDTO> subs = service.getAllActiveSubjects();
            cmbSubject.removeAllItems();
            cmbSubject.addItem(new SubjectItem(0, "-- Chọn môn học --"));
            for (SubjectDTO s : subs)
                cmbSubject.addItem(new SubjectItem(s.getSubjectId(), s.getSubjectName()));
        } catch (Exception e) {
            showError("Không tải được danh sách môn học:\n" + e.getMessage());
        }
    }

    private void refreshClassTab() {
        setStatus(lblClassStatus, "Đang tải...", TEXT_MUTE);
        try {
            List<Map<String, Object>> rows = service.getClassesWithDetails();
            classModel.setRowCount(0);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

            for (Map<String, Object> r : rows) {
                String sd = r.get("start_date") != null ? df.format(r.get("start_date")) : "";
                String ed = r.get("end_date")   != null ? df.format(r.get("end_date"))   : "";
                classModel.addRow(new Object[]{
                    r.get("class_id"),
                    r.get("class_name"),
                    r.get("subject_name"),
                    r.get("class_type"),
                    nf.format((double) r.get("tuition_fee")),
                    sd, ed,
                    r.get("student_count")
                });
            }

            lblTotalClasses.setText(String.valueOf(rows.size()));
            setStatus(lblClassStatus, "Đã tải – " + rows.size() + " lớp", SUCCESS);
        } catch (Exception e) {
            setStatus(lblClassStatus, "Lỗi tải dữ liệu", DANGER);
            showError("Lỗi tải danh sách lớp:\n" + e.getMessage());
        }
    }

    private void fillClassForm() {
        int vr = tblClasses.getSelectedRow();
        if (vr < 0) return;
        int mr = tblClasses.convertRowIndexToModel(vr);

        txtClassId  .setText(val(mr, 0));
        txtClassName.setText(val(mr, 1));

        // Môn học
        String sName = val(mr, 2);
        for (int i = 0; i < cmbSubject.getItemCount(); i++) {
            if (cmbSubject.getItemAt(i).name.equals(sName)) {
                cmbSubject.setSelectedIndex(i);
                break;
            }
        }

        // Loại lớp
        cmbClassType.setSelectedIndex("ADV".equals(val(mr, 3)) ? 1 : 0);

        // Học phí (hiển thị dạng số thuần)
        txtFee.setText(val(mr, 4).replaceAll("[^\\d]", ""));
        txtAllowance.setText("");   // teacher_allowance không hiển thị trong bảng
        txtStartDate.setText(val(mr, 5));
        txtEndDate  .setText(val(mr, 6));

        lblFormMode.setText("Đang sửa – mã lớp: " + val(mr, 0));
        btnSave  .setText("Cập nhật");
        btnDelete.setEnabled(true);
    }

    private void saveClass() {
        try {
            String name = txtClassName.getText().trim();
            if (name.isEmpty()) { showWarn("Vui lòng nhập tên lớp học!"); return; }

            SubjectItem si = (SubjectItem) cmbSubject.getSelectedItem();
            if (si == null || si.id == 0) { showWarn("Vui lòng chọn môn học!"); return; }

            double fee = parseNumber(txtFee.getText(), "Học phí");
            double alw = txtAllowance.getText().trim().isEmpty() ? 0
                       : parseNumber(txtAllowance.getText(), "Phụ cấp GV");

            java.sql.Date sd = parseDate(txtStartDate.getText(), "Ngày khai giảng");
            java.sql.Date ed = parseDate(txtEndDate.getText(),   "Ngày kết thúc");

            String typeCode = cmbClassType.getSelectedIndex() == 0 ? "REG" : "ADV";

            StudyClass sc = new StudyClass();
            sc.setClassName(name);
            sc.setSubjectId(si.id);
            sc.setClassType(typeCode);
            sc.setTuitionFee(fee);
            sc.setTeacherAllowance(alw);
            sc.setStartDate(sd);
            sc.setEndDate(ed);

            if (txtClassId.getText().trim().isEmpty()) {
                service.addClass(sc);
                showInfo("Tạo lớp học mới thành công!");
            } else {
                sc.setClassId(Integer.parseInt(txtClassId.getText().trim()));
                service.updateClass(sc);
                showInfo("Cập nhật lớp học thành công!");
            }

            clearClassForm();
            refreshClassTab();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deleteClass() {
        if (txtClassId.getText().trim().isEmpty()) {
            showWarn("Vui lòng chọn lớp học từ bảng trước khi xóa."); return;
        }
        String name = txtClassName.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa lớp học \"" + name + "\"?\n(Dữ liệu được lưu giữ, lớp sẽ không còn hiển thị)",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.deleteClass(Integer.parseInt(txtClassId.getText().trim()));
                showInfo("Đã xóa lớp học \"" + name + "\"!");
                clearClassForm();
                refreshClassTab();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    private void clearClassForm() {
        txtClassId  .setText("");
        txtClassName.setText("");
        txtFee      .setText("");
        txtAllowance.setText("");
        txtStartDate.setText("");
        txtEndDate  .setText("");
        cmbSubject  .setSelectedIndex(0);
        cmbClassType.setSelectedIndex(0);
        tblClasses  .clearSelection();
        lblFormMode .setText("Tạo lớp mới");
        btnSave     .setText("Lưu");
        btnDelete   .setEnabled(false);
        txtClassName.requestFocusInWindow();
    }

    private void filterClassTable() {
        if (classRowSorter == null) return;
        String kw = txtClassSearch.getText().trim();
        classRowSorter.setRowFilter(
            kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }


    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════

    private void configureTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_MAIN);
        t.setShowVerticalLines(true);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER_C);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(PRIMARY_SOFT);
        t.setSelectionForeground(PRIMARY_DARK);
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(false);
        t.setIntercellSpacing(new Dimension(1, 1));
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(new Color(71, 85, 105));
        h.setPreferredSize(new Dimension(0, 36));
        h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private JTextField fieldRow(JPanel p, String label, boolean editable) {
        formLabel(p, label);
        JTextField f = styledField("");
        f.setEditable(editable);
        if (!editable) f.setBackground(new Color(248, 250, 252));
        p.add(f);
        p.add(Box.createVerticalStrut(12));
        return f;
    }

    private void formLabel(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
    }

    private JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setPreferredSize(new Dimension(0, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(5, 10, 5, 10)));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(PRIMARY, 1, true), new EmptyBorder(5, 10, 5, 10)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(5, 10, 5, 10)));
            }
        });
        return f;
    }

    private <T> void styleCombo(JComboBox<T> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private DocumentListener docListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) {}
        };
    }

    private String val(int row, int col) {
        Object v = classModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private String nvl(Object v) { return v == null ? "" : v.toString(); }

    private double parseNumber(String raw, String fieldName) throws Exception {
        String s = raw.trim().replaceAll("[.,\\s]", "");
        if (s.isEmpty()) return 0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { throw new Exception(fieldName + " phải là số!"); }
    }

    private java.sql.Date parseDate(String raw, String fieldName) throws Exception {
        if (raw.trim().isEmpty()) throw new Exception(fieldName + " không được để trống!");
        try {
            return new java.sql.Date(sdf.parse(raw.trim()).getTime());
        } catch (ParseException e) {
            throw new Exception(fieldName + " không đúng định dạng dd/MM/yyyy.\nVí dụ: 15/06/2025");
        }
    }

    private void setStatus(JLabel lbl, String msg, Color color) {
        if (lbl == null) return;
        SwingUtilities.invokeLater(() -> { lbl.setText(msg); lbl.setForeground(color); });
    }

    private void showInfo (String m) { JOptionPane.showMessageDialog(this, m, "Thông báo",  JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn (String m) { JOptionPane.showMessageDialog(this, m, "Cảnh báo",   JOptionPane.WARNING_MESSAGE);     }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);    }

    // ════════════════════════════════════════════════════════
    // INNER CLASSES
    // ════════════════════════════════════════════════════════

    /** Item cho combobox môn học (giữ id kèm name). */
    private static class SubjectItem {
        final int id; final String name;
        SubjectItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    /** Renderer dải zebra + ID in đậm + căn giữa. */
    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(v == null ? "" : v.toString());
            if (sel) {
                setBackground(PRIMARY_SOFT); setForeground(PRIMARY_DARK);
            } else {
                setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(TEXT_MAIN);
            }
            boolean isId = (c == 0);
            setFont(isId ? new Font("Segoe UI", Font.BOLD,  13)
                         : new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(isId ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    /** Badge màu cho cột Loại lớp (REG / ADV). */
    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            String raw = v == null ? "" : v.toString();
            String text = "ADV".equals(raw) ? "Nâng cao" : "Thường";
            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
            if (sel) {
                lbl.setBackground(PRIMARY_SOFT); lbl.setForeground(PRIMARY_DARK);
            } else if ("ADV".equals(raw)) {
                lbl.setBackground(new Color(254, 240, 138)); lbl.setForeground(new Color(133, 77, 14));
            } else {
                lbl.setBackground(new Color(220, 252, 231)); lbl.setForeground(new Color(22, 101, 52));
            }
            return lbl;
        }
    }

    /** Renderer cho cột Số HV – nền xanh nhạt khi > 0. */
    private static class CountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            if (!sel) {
                int cnt = v == null ? 0 : Integer.parseInt(v.toString());
                setBackground(cnt > 0 ? new Color(239, 246, 255) : new Color(248, 250, 252));
                setForeground(cnt > 0 ? PRIMARY_DARK : TEXT_MUTE);
            }
            return this;
        }
    }

    /** Nút bo góc với hover effect. */
    private static class ActionButton extends JButton {
        private final Color bg, fg, border;
        private boolean hover;

        ActionButton(String t, Color bg)                       { this(t, bg, Color.WHITE, bg); }
        ActionButton(String t, Color bg, Color fg, Color brd)  {
            super(t);
            this.bg = bg; this.fg = fg; this.border = brd;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(fg);
            setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false); setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 14, 8, 14));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = !isEnabled() ? new Color(226, 232, 240) : hover ? bg.darker() : bg;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            g2.setColor(!isEnabled() ? BORDER_C : border);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            g2.dispose();
            setForeground(!isEnabled() ? DISABLED : fg);
            super.paintComponent(g);
        }
    }

    /** Panel với nền bo góc + viền xám nhạt. */
    private static class RoundedPanel extends JPanel {
        private final int radius; private final Color bg;
        RoundedPanel(int r, Color bg) { this.radius = r; this.bg = bg; setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.setColor(BORDER_C);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Quản lý lớp học");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 760); f.setLocationRelativeTo(null);
            f.add(new ClassManagementPanel()); f.setVisible(true);
        });
    }
}
