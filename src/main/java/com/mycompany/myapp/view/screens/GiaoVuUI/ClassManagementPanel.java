package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.model.SubjectDTO;
import com.mycompany.myapp.service.StudyClassService;
import com.mycompany.myapp.utils.PermissionManager;
import com.mycompany.myapp.utils.PermissionUIHelper;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Panel quản lý lớp học.
 *
 * Module quyền: QUAN_LY_LOP_HOC
 *
 * Quyền áp dụng:
 * - Xem: được mở màn hình, xem danh sách lớp, xem chi tiết lớp.
 * - Thêm: được tạo lớp mới.
 * - Sửa: được cập nhật lớp đã chọn.
 * - Xóa: được xóa mềm lớp học.
 *
 * Logic chuẩn:
 * - Có quyền Xem nhưng không có Thêm/Sửa/Xóa:
 *   vẫn xem được dữ liệu, nhưng form chỉ đọc và nút thao tác bị khóa.
 */
public class ClassManagementPanel extends JPanel {

    private static final String MODULE_CODE = "QUAN_LY_LOP_HOC";

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
    private static final Color DANGER       = new Color(220,  38,  38);
    private static final Color DISABLED     = new Color(148, 163, 184);

    private final StudyClassService service = new StudyClassService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private DefaultTableModel classModel;
    private JTable tblClasses;
    private TableRowSorter<DefaultTableModel> classRowSorter;

    private JTextField txtClassId;
    private JTextField txtClassName;
    private JTextField txtFee;
    private JTextField txtAllowance;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JTextField txtClassSearch;

    private JComboBox<SubjectItem> cmbSubject;
    private JComboBox<String> cmbClassType;

    private JLabel lblFormMode;
    private JLabel lblTotalClasses;
    private JLabel lblClassStatus;

    private ActionButton btnNew;
    private ActionButton btnSave;
    private ActionButton btnDelete;

    public ClassManagementPanel() {
        sdf.setLenient(false);

        setLayout(new BorderLayout());
        setBackground(BG_PAGE);

        if (!PermissionManager.canView(MODULE_CODE)) {
            add(buildNoPermissionPanel(), BorderLayout.CENTER);
            return;
        }

        add(buildClassTab(), BorderLayout.CENTER);

        loadSubjects();
        refreshClassTab();
        applyPermission();
    }

    // ════════════════════════════════════════════════════════
    // UI BUILD
    // ════════════════════════════════════════════════════════

    private JPanel buildClassTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 16));
        tab.setBackground(BG_PAGE);
        tab.setBorder(new EmptyBorder(22, 28, 22, 28));

        tab.add(buildClassHeader(), BorderLayout.NORTH);
        tab.add(buildClassMain(), BorderLayout.CENTER);

        return tab;
    }

    private JPanel buildNoPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PAGE);

        JLabel label = new JLabel(
            "<html><div style='text-align:center;'>"
                + "<h2>Không có quyền truy cập</h2>"
                + "<p>Tài khoản hiện tại không có quyền xem chức năng Quản lý lớp học.</p>"
                + "</div></html>",
            SwingConstants.CENTER
        );

        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(TEXT_MUTE);

        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

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

        btnNew = new ActionButton("+ Tạo mới", PRIMARY);
        ActionButton btnRefresh = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);

        btnNew.setPreferredSize(new Dimension(118, 40));
        btnRefresh.setPreferredSize(new Dimension(108, 40));

        btnNew.addActionListener(e -> {
            if (!PermissionUIHelper.requireAdd(this, MODULE_CODE)) {
                return;
            }

            clearClassForm();
        });

        btnRefresh.addActionListener(e -> {
            loadSubjects();
            refreshClassTab();
            applyPermission();
        });

        actions.add(lblClassStatus);
        actions.add(btnNew);
        actions.add(btnRefresh);

        p.add(titleBox, BorderLayout.WEST);
        p.add(actions, BorderLayout.EAST);

        return p;
    }

    private JPanel buildClassMain() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        JPanel strip = new RoundedPanel(14, BG_CARD);
        strip.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        strip.setPreferredSize(new Dimension(0, 52));

        lblTotalClasses = new JLabel("0");
        lblTotalClasses.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalClasses.setForeground(PRIMARY);

        JLabel lbl1 = new JLabel("Tổng lớp học đang hoạt động");
        lbl1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl1.setForeground(TEXT_MUTE);

        strip.add(lblTotalClasses);
        strip.add(lbl1);

        content.add(strip, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildClassTableCard(),
            buildClassFormCard()
        );

        split.setResizeWeight(0.65);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_PAGE);

        content.add(split, BorderLayout.CENTER);

        return content;
    }

    private JPanel buildClassTableCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

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
        txtClassSearch.setToolTipText("Tìm theo tên lớp, môn học, loại lớp...");
        txtClassSearch.getDocument().addDocumentListener(docListener(this::filterClassTable));

        searchRow.add(lblS);
        searchRow.add(txtClassSearch);

        toolbar.add(searchRow, BorderLayout.CENTER);
        card.add(toolbar, BorderLayout.NORTH);

        classModel = new DefaultTableModel(
            new String[]{
                "ID", "Tên lớp", "Môn học", "Loại", "Học phí (VNĐ)",
                "Ngày KG", "Ngày KT", "Số HV"
            },
            0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblClasses = new JTable(classModel);
        configureTable(tblClasses);

        tblClasses.getColumnModel().getColumn(3).setCellRenderer(new TypeBadgeRenderer());
        tblClasses.getColumnModel().getColumn(7).setCellRenderer(new CountRenderer());

        int[] cw = {50, 210, 150, 65, 130, 100, 100, 60};
        for (int i = 0; i < cw.length; i++) {
            tblClasses.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);
        }

        classRowSorter = new TableRowSorter<>(classModel);
        tblClasses.setRowSorter(classRowSorter);

        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClasses.getSelectedRow() >= 0) {
                fillClassForm();
            }
        });

        JScrollPane sp = new JScrollPane(tblClasses);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildClassFormCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 10, 0, 0));
        wrapper.setPreferredSize(new Dimension(380, 0));

        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout());

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

        txtFee = fieldRow(fields, "Học phí (VNĐ) (*)", true);
        txtAllowance = fieldRow(fields, "Phụ cấp giáo viên (VNĐ)", true);
        txtStartDate = fieldRow(fields, "Ngày khai giảng  dd/MM/yyyy (*)", true);
        txtEndDate = fieldRow(fields, "Ngày kết thúc  dd/MM/yyyy (*)", true);

        JScrollPane fs = new JScrollPane(fields);
        fs.setBorder(null);
        fs.setOpaque(false);
        fs.getViewport().setOpaque(false);
        fs.getVerticalScrollBar().setUnitIncrement(16);
        fs.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(fs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 3, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        footer.setPreferredSize(new Dimension(0, 66));

        ActionButton btnClear = new ActionButton("Nhập lại", Color.WHITE, TEXT_MUTE, TEXT_MUTE);
        btnDelete = new ActionButton("Xóa lớp", Color.WHITE, DANGER, DANGER);
        btnSave = new ActionButton("Lưu", SUCCESS);

        btnClear.addActionListener(e -> {
            if (!PermissionManager.canAdd(MODULE_CODE)) {
                showWarn("Bạn không có quyền tạo lớp học mới.");
                return;
            }
            clearClassForm();
        });

        btnDelete.addActionListener(e -> deleteClass());
        btnSave.addActionListener(e -> saveClass());

        footer.add(btnClear);
        footer.add(btnDelete);
        footer.add(btnSave);

        card.add(footer, BorderLayout.SOUTH);

        wrapper.add(card, BorderLayout.CENTER);

        return wrapper;
    }

    // ════════════════════════════════════════════════════════
    // PERMISSION
    // ════════════════════════════════════════════════════════

    private void applyPermission() {
        boolean canAdd = PermissionManager.canAdd(MODULE_CODE);
        boolean canEdit = PermissionManager.canEdit(MODULE_CODE);
        boolean canDelete = PermissionManager.canDelete(MODULE_CODE);

        boolean isUpdateMode = txtClassId != null && !txtClassId.getText().trim().isEmpty();
        boolean hasSelectedClass = isUpdateMode;

        /*
         * Có quyền Xem:
         * - Màn hình vẫn hiển thị.
         * - Danh sách vẫn xem được.
         *
         * Không có quyền Thêm:
         * - Không được tạo mới.
         *
         * Không có quyền Sửa:
         * - Khi chọn lớp vẫn xem form chi tiết.
         * - Form readonly, không cho cập nhật.
         *
         * Không có quyền Xóa:
         * - Không cho xóa lớp.
         */
        if (isUpdateMode) {
            setFormEditable(canEdit);
        } else {
            setFormEditable(canAdd);
        }

        if (btnNew != null) {
            btnNew.setEnabled(canAdd);
            btnNew.setToolTipText(canAdd ? null : "Bạn không có quyền tạo lớp học mới.");
        }

        if (btnSave != null) {
            if (isUpdateMode) {
                btnSave.setText("Cập nhật");
                btnSave.setEnabled(canEdit);
                btnSave.setToolTipText(canEdit ? null : "Bạn không có quyền cập nhật lớp học.");
            } else {
                btnSave.setText("Lưu");
                btnSave.setEnabled(canAdd);
                btnSave.setToolTipText(canAdd ? null : "Bạn không có quyền tạo lớp học mới.");
            }
        }

        if (btnDelete != null) {
            btnDelete.setEnabled(hasSelectedClass && canDelete);
            btnDelete.setToolTipText(canDelete ? null : "Bạn không có quyền xóa lớp học.");
        }
    }

    private void setFormEditable(boolean editable) {
        if (txtClassName == null) {
            return;
        }

        txtClassName.setEditable(editable);
        txtFee.setEditable(editable);
        txtAllowance.setEditable(editable);
        txtStartDate.setEditable(editable);
        txtEndDate.setEditable(editable);

        cmbSubject.setEnabled(editable);
        cmbClassType.setEnabled(editable);

        Color bg = editable ? Color.WHITE : new Color(248, 250, 252);

        txtClassName.setBackground(bg);
        txtFee.setBackground(bg);
        txtAllowance.setBackground(bg);
        txtStartDate.setBackground(bg);
        txtEndDate.setBackground(bg);
    }

    // ════════════════════════════════════════════════════════
    // DATA
    // ════════════════════════════════════════════════════════

    private void loadSubjects() {
        try {
            List<SubjectDTO> subs = service.getAllActiveSubjects();

            cmbSubject.removeAllItems();
            cmbSubject.addItem(new SubjectItem(0, "-- Chọn môn học --"));

            for (SubjectDTO s : subs) {
                cmbSubject.addItem(new SubjectItem(s.getSubjectId(), s.getSubjectName()));
            }

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
                String ed = r.get("end_date") != null ? df.format(r.get("end_date")) : "";

                double tuitionFee = safeDouble(r.get("tuition_fee"));
                int studentCount = safeInt(r.get("student_count"));

                classModel.addRow(new Object[]{
                    r.get("class_id"),
                    r.get("class_name"),
                    r.get("subject_name"),
                    r.get("class_type"),
                    nf.format(tuitionFee),
                    sd,
                    ed,
                    studentCount
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

        if (vr < 0) {
            return;
        }

        int mr = tblClasses.convertRowIndexToModel(vr);

        txtClassId.setText(val(mr, 0));
        txtClassName.setText(val(mr, 1));

        String subjectName = val(mr, 2);

        for (int i = 0; i < cmbSubject.getItemCount(); i++) {
            if (cmbSubject.getItemAt(i).name.equals(subjectName)) {
                cmbSubject.setSelectedIndex(i);
                break;
            }
        }

        cmbClassType.setSelectedIndex("ADV".equals(val(mr, 3)) ? 1 : 0);

        txtFee.setText(val(mr, 4).replaceAll("[^\\d]", ""));
        txtAllowance.setText("");
        txtStartDate.setText(val(mr, 5));
        txtEndDate.setText(val(mr, 6));

        lblFormMode.setText("Đang xem / sửa – mã lớp: " + val(mr, 0));
        btnSave.setText("Cập nhật");

        applyPermission();
    }

    private void clearClassForm() {
        txtClassId.setText("");
        txtClassName.setText("");
        txtFee.setText("");
        txtAllowance.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");

        if (cmbSubject.getItemCount() > 0) {
            cmbSubject.setSelectedIndex(0);
        }

        cmbClassType.setSelectedIndex(0);
        tblClasses.clearSelection();

        lblFormMode.setText("Tạo lớp mới");
        btnSave.setText("Lưu");

        applyPermission();

        if (PermissionManager.canAdd(MODULE_CODE)) {
            txtClassName.requestFocusInWindow();
        }
    }

    private void filterClassTable() {
        if (classRowSorter == null) {
            return;
        }

        String keyword = txtClassSearch.getText().trim();

        if (keyword.isEmpty()) {
            classRowSorter.setRowFilter(null);
        } else {
            classRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
    }

    // ════════════════════════════════════════════════════════
    // BUSINESS ACTIONS
    // ════════════════════════════════════════════════════════

    private void saveClass() {
        boolean isCreateMode = txtClassId.getText().trim().isEmpty();

        if (isCreateMode) {
            if (!PermissionUIHelper.requireAdd(this, MODULE_CODE)) {
                return;
            }
        } else {
            if (!PermissionUIHelper.requireEdit(this, MODULE_CODE)) {
                return;
            }
        }

        try {
            StudyClass studyClass = buildStudyClassFromForm(isCreateMode);

            if (isCreateMode) {
                service.addClass(studyClass);
                showInfo("Tạo lớp học mới thành công!");
            } else {
                service.updateClass(studyClass);
                showInfo("Cập nhật lớp học thành công!");
            }

            clearClassForm();
            refreshClassTab();
            applyPermission();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private StudyClass buildStudyClassFromForm(boolean isCreateMode) throws Exception {
        String name = txtClassName.getText().trim();

        if (name.isEmpty()) {
            throw new Exception("Vui lòng nhập tên lớp học.");
        }

        if (name.length() < 3) {
            throw new Exception("Tên lớp học phải có ít nhất 3 ký tự.");
        }

        SubjectItem subject = (SubjectItem) cmbSubject.getSelectedItem();

        if (subject == null || subject.id == 0) {
            throw new Exception("Vui lòng chọn môn học.");
        }

        double fee = parseNumber(txtFee.getText(), "Học phí");

        if (fee <= 0) {
            throw new Exception("Học phí phải lớn hơn 0.");
        }

        double allowance = txtAllowance.getText().trim().isEmpty()
            ? 0
            : parseNumber(txtAllowance.getText(), "Phụ cấp giáo viên");

        if (allowance < 0) {
            throw new Exception("Phụ cấp giáo viên không được âm.");
        }

        java.sql.Date startDate = parseDate(txtStartDate.getText(), "Ngày khai giảng");
        java.sql.Date endDate = parseDate(txtEndDate.getText(), "Ngày kết thúc");

        if (endDate.before(startDate)) {
            throw new Exception("Ngày kết thúc phải sau hoặc bằng ngày khai giảng.");
        }

        String typeCode = cmbClassType.getSelectedIndex() == 0 ? "REG" : "ADV";

        StudyClass sc = new StudyClass();

        if (!isCreateMode) {
            int classId = Integer.parseInt(txtClassId.getText().trim());
            sc.setClassId(classId);
        }

        sc.setClassName(name);
        sc.setSubjectId(subject.id);
        sc.setClassType(typeCode);
        sc.setTuitionFee(fee);
        sc.setTeacherAllowance(allowance);
        sc.setStartDate(startDate);
        sc.setEndDate(endDate);

        return sc;
    }

    private void deleteClass() {
        if (!PermissionUIHelper.requireDelete(this, MODULE_CODE)) {
            return;
        }

        if (txtClassId.getText().trim().isEmpty()) {
            showWarn("Vui lòng chọn lớp học từ bảng trước khi xóa.");
            return;
        }

        int studentCount = getSelectedClassStudentCount();

        if (studentCount > 0) {
            showWarn(
                "Không thể xóa lớp đang có học viên.\n"
                    + "Lớp hiện có " + studentCount + " học viên.\n\n"
                    + "Vui lòng rút học viên khỏi lớp trước khi xóa."
            );
            return;
        }

        String name = txtClassName.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Xóa lớp học \"" + name + "\"?\n"
                + "Dữ liệu sẽ được xóa mềm và lớp không còn hiển thị trong danh sách.",
            "Xác nhận xóa lớp",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int classId = Integer.parseInt(txtClassId.getText().trim());

            service.deleteClass(classId);

            showInfo("Đã xóa lớp học \"" + name + "\".");

            clearClassForm();
            refreshClassTab();
            applyPermission();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private int getSelectedClassStudentCount() {
        int vr = tblClasses.getSelectedRow();

        if (vr < 0) {
            return 0;
        }

        int mr = tblClasses.convertRowIndexToModel(vr);
        return safeInt(classModel.getValueAt(mr, 7));
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

        if (!editable) {
            f.setBackground(new Color(248, 250, 252));
        }

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
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (f.isEditable()) {
                    f.setBorder(new CompoundBorder(
                        new LineBorder(PRIMARY, 1, true),
                        new EmptyBorder(5, 10, 5, 10)
                    ));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_C, 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
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
            @Override
            public void insertUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
    }

    private String val(int row, int col) {
        Object v = classModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private double parseNumber(String raw, String fieldName) throws Exception {
        String s = raw == null ? "" : raw.trim().replaceAll("[.,\\s]", "");

        if (s.isEmpty()) {
            throw new Exception(fieldName + " không được để trống.");
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new Exception(fieldName + " phải là số hợp lệ.");
        }
    }

    private java.sql.Date parseDate(String raw, String fieldName) throws Exception {
        if (raw == null || raw.trim().isEmpty()) {
            throw new Exception(fieldName + " không được để trống.");
        }

        try {
            return new java.sql.Date(sdf.parse(raw.trim()).getTime());
        } catch (ParseException e) {
            throw new Exception(fieldName + " không đúng định dạng dd/MM/yyyy.\nVí dụ: 15/06/2026");
        }
    }

    private int safeInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(value.toString().replaceAll("[^\\d-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private double safeDouble(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString().replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private void setStatus(JLabel lbl, String msg, Color color) {
        if (lbl == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            lbl.setText(msg);
            lbl.setForeground(color);
        });
    }

    private void showInfo(String m) {
        JOptionPane.showMessageDialog(this, m, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarn(String m) {
        JOptionPane.showMessageDialog(this, m, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String m) {
        JOptionPane.showMessageDialog(this, m, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
    }

    // ════════════════════════════════════════════════════════
    // INNER CLASSES
    // ════════════════════════════════════════════════════════

    private static class SubjectItem {
        final int id;
        final String name;

        SubjectItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable t,
            Object v,
            boolean sel,
            boolean focus,
            int r,
            int c
        ) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);

            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(v == null ? "" : v.toString());

            if (sel) {
                setBackground(PRIMARY_SOFT);
                setForeground(PRIMARY_DARK);
            } else {
                setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(TEXT_MAIN);
            }

            boolean isId = c == 0;

            setFont(isId
                ? new Font("Segoe UI", Font.BOLD, 13)
                : new Font("Segoe UI", Font.PLAIN, 13)
            );

            setHorizontalAlignment(isId ? SwingConstants.CENTER : SwingConstants.LEFT);

            return this;
        }
    }

    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable t,
            Object v,
            boolean sel,
            boolean focus,
            int r,
            int c
        ) {
            String raw = v == null ? "" : v.toString();
            String text = "ADV".equals(raw) ? "Nâng cao" : "Thường";

            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));

            if (sel) {
                lbl.setBackground(PRIMARY_SOFT);
                lbl.setForeground(PRIMARY_DARK);
            } else if ("ADV".equals(raw)) {
                lbl.setBackground(new Color(254, 240, 138));
                lbl.setForeground(new Color(133, 77, 14));
            } else {
                lbl.setBackground(new Color(220, 252, 231));
                lbl.setForeground(new Color(22, 101, 52));
            }

            return lbl;
        }
    }

    private static class CountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable t,
            Object v,
            boolean sel,
            boolean focus,
            int r,
            int c
        ) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);

            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (!sel) {
                int count = 0;

                try {
                    count = v == null ? 0 : Integer.parseInt(v.toString());
                } catch (Exception ignored) {
                }

                setBackground(count > 0 ? new Color(239, 246, 255) : new Color(248, 250, 252));
                setForeground(count > 0 ? PRIMARY_DARK : TEXT_MUTE);
            }

            return this;
        }
    }

    private static class ActionButton extends JButton {
        private final Color bg;
        private final Color fg;
        private final Color border;
        private boolean hover;

        ActionButton(String text, Color bg) {
            this(text, bg, Color.WHITE, bg);
        }

        ActionButton(String text, Color bg, Color fg, Color border) {
            super(text);

            this.bg = bg;
            this.fg = fg;
            this.border = border;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 14, 8, 14));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = !isEnabled()
                ? new Color(226, 232, 240)
                : hover ? bg.darker() : bg;

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.setColor(!isEnabled() ? BORDER_C : border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.dispose();

            setForeground(!isEnabled() ? DISABLED : fg);

            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(BORDER_C);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }
}