package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.Student;
import com.mycompany.myapp.service.StudentService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

public class StudentManagementPanel extends JPanel {

    private static final Color PRIMARY       = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK  = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT  = new Color(238, 234, 255);
    private static final Color BG_PAGE       = new Color(248, 250, 252);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color BORDER_C      = new Color(226, 232, 240);
    private static final Color TEXT_MAIN     = new Color(15, 23, 42);
    private static final Color TEXT_MUTE     = new Color(100, 116, 139);
    private static final Color SUCCESS       = new Color(22, 163, 74);
    private static final Color DANGER        = new Color(220, 38, 38);
    private static final Color DISABLED      = new Color(148, 163, 184);

    private final StudentService studentService = new StudentService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private DefaultTableModel tableModel;
    private JTable tblStudents;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtId, txtName, txtDob, txtPhone, txtParent, txtParentPhone, txtSearch;
    private JComboBox<String> cmbGender;
    private JLabel lblTotalStudents, lblShowingStudents, lblClassInsight, lblFormMode, lblStatus, lblTableTitle, lblTableSubtitle, lblFilterBadge;
    private ActionButton btnSave, btnDelete, btnNew;

    public StudentManagementPanel() {
        sdf.setLenient(false);

        setLayout(new BorderLayout(0, 18));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 30, 26, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        refreshData();
        setFormModeNew();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Quản lý học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Quản lý hồ sơ học viên, thông tin liên hệ và lọc nhanh theo lớp");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTE);

        btnNew = new ActionButton("+ Tạo mới", PRIMARY);
        btnNew.setPreferredSize(new Dimension(120, 42));
        btnNew.addActionListener(e -> clearForm());

        ActionButton btnReload = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnReload.setPreferredSize(new Dimension(105, 42));
        btnReload.addActionListener(e -> refreshData());

        actions.add(lblStatus);
        actions.add(btnNew);
        actions.add(btnReload);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        content.add(buildBusinessSummary(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildFormCard());
        split.setResizeWeight(0.68);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setOpaque(false);
        split.setBackground(BG_PAGE);
        content.add(split, BorderLayout.CENTER);

        return content;
    }

    private JPanel buildBusinessSummary() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(8, 16, 8, 16));
        card.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        lblTotalStudents = new JLabel("0");
        lblShowingStudents = new JLabel("0");

        left.add(summaryItem("Tổng học viên", lblTotalStudents, PRIMARY));
        left.add(summaryItem("Đang hiển thị", lblShowingStudents, SUCCESS));

        lblClassInsight = new JLabel("Đang xem toàn bộ học viên");
        lblClassInsight.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClassInsight.setForeground(TEXT_MUTE);
        lblClassInsight.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(left, BorderLayout.WEST);
        card.add(lblClassInsight, BorderLayout.CENTER);

        return card;
    }

    private JPanel summaryItem(String title, JLabel value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(accent);

        value.setFont(new Font("Segoe UI", Font.BOLD, 20));
        value.setForeground(accent);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_MUTE);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(label);
        text.add(value);

        JPanel dotWrap = new JPanel(new GridBagLayout());
        dotWrap.setOpaque(false);
        dotWrap.add(dot);

        p.add(dotWrap, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildTableCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(12, 14, 14, 14));
        card.setMinimumSize(new Dimension(680, 420));

        card.add(buildTableToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Họ tên", "Ngày sinh", "Giới tính", "SĐT", "Phụ huynh", "SĐT PH"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblStudents = new JTable(tableModel);
        configureTable(tblStudents);
        setColumnWidths();

        rowSorter = new TableRowSorter<>(tableModel);
        tblStudents.setRowSorter(rowSorter);

        tblStudents.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblStudents.getSelectedRow() != -1) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblStudents);
        scrollPane.setBorder(new LineBorder(BORDER_C, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(0, 10));
        toolbar.setOpaque(false);

        JPanel tableHeader = new JPanel(new BorderLayout(10, 0));
        tableHeader.setOpaque(false);
        tableHeader.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        lblTableTitle = new JLabel("Danh sách hồ sơ học viên");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTableTitle.setForeground(TEXT_MAIN);

        lblTableSubtitle = new JLabel("Tra cứu / lọc nhanh");
        lblTableSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSubtitle.setForeground(TEXT_MUTE);

        titleRow.add(lblTableTitle);
        titleRow.add(lblTableSubtitle);

        lblFilterBadge = new JLabel("Tất cả lớp");
        lblFilterBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFilterBadge.setForeground(PRIMARY_DARK);
        lblFilterBadge.setOpaque(true);
        lblFilterBadge.setBackground(PRIMARY_SOFT);
        lblFilterBadge.setBorder(new EmptyBorder(5, 10, 5, 10));

        tableHeader.add(titleRow, BorderLayout.WEST);
        tableHeader.add(lblFilterBadge, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setOpaque(true);
        filterPanel.setBackground(new Color(248, 250, 252));
        filterPanel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtSearch = styledField("");
        txtSearch.setPreferredSize(new Dimension(400, 34));
        txtSearch.setToolTipText("Tìm theo mã, tên, SĐT hoặc phụ huynh");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        gbc.gridx = 0;
        gbc.weightx = 0;
        filterPanel.add(toolbarLabel("Tìm kiếm"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        filterPanel.add(txtSearch, gbc);

        toolbar.add(tableHeader, BorderLayout.NORTH);
        toolbar.add(filterPanel, BorderLayout.CENTER);

        return toolbar;
    }

    private JLabel toolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MAIN);
        return label;
    }

    private JPanel buildFormCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 14, 0, 0));
        wrapper.setMinimumSize(new Dimension(360, 420));
        wrapper.setPreferredSize(new Dimension(390, 520));

        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 0));

        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);
        formHeader.setBorder(new EmptyBorder(18, 22, 12, 22));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hồ sơ học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(TEXT_MAIN);

        lblFormMode = new JLabel("Thêm mới học viên");
        lblFormMode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormMode.setForeground(TEXT_MUTE);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblFormMode);

        formHeader.add(titleBox, BorderLayout.WEST);
        card.add(formHeader, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBorder(new EmptyBorder(0, 22, 12, 22));

        txtId = addField(fields, "Mã học viên", false);
        txtId.setFocusable(false);
        txtId.setBackground(new Color(248, 250, 252));

        txtName = addField(fields, "Họ và tên (*)", true);
        txtDob = addField(fields, "Ngày sinh dd/MM/yyyy (*)", true);

        addFormLabel(fields, "Giới tính");
        cmbGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cmbGender.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbGender.setBackground(Color.WHITE);
        cmbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(cmbGender);
        fields.add(Box.createVerticalStrut(12));

        txtPhone = addField(fields, "Số điện thoại (*)", true);
        txtParent = addField(fields, "Tên phụ huynh (*)", true);
        txtParentPhone = addField(fields, "SĐT phụ huynh", true);

        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(null);
        formScroll.getViewport().setOpaque(false);
        formScroll.setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(formScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 3, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 22, 18, 22));
        footer.setPreferredSize(new Dimension(0, 74));

        ActionButton btnClear = new ActionButton("Nhập lại", Color.WHITE, TEXT_MUTE, TEXT_MUTE);
        btnDelete = new ActionButton("Xóa", Color.WHITE, DANGER, DANGER);
        btnSave = new ActionButton("Lưu", SUCCESS);

        btnClear.addActionListener(e -> clearForm());
        btnDelete.addActionListener(e -> deleteStudent());
        btnSave.addActionListener(e -> saveStudent());

        footer.add(btnClear);
        footer.add(btnDelete);
        footer.add(btnSave);
        card.add(footer, BorderLayout.SOUTH);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JTextField addField(JPanel parent, String label, boolean editable) {
        addFormLabel(parent, label);
        JTextField field = styledField("");
        field.setEditable(editable);
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
        return field;
    }

    private void configureTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setAutoCreateRowSorter(false);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new TooltipCellRenderer());
    }

    private void setColumnWidths() {
        TableColumnModel cm = tblStudents.getColumnModel();
        int[] widths = {58, 230, 110, 76, 130, 165, 130};
        for (int i = 0; i < widths.length; i++) cm.getColumn(i).setPreferredWidth(widths[i]);
    }

    private void fillFormFromSelectedRow() {
        int viewRow = tblStudents.getSelectedRow();
        if (viewRow < 0) return;

        int modelRow = tblStudents.convertRowIndexToModel(viewRow);
        // Cols: 0=ID, 1=Họ tên, 2=Ngày sinh, 3=Giới tính, 4=SĐT, 5=Phụ huynh, 6=SĐT PH
        txtId.setText(valueAt(modelRow, 0));
        txtName.setText(valueAt(modelRow, 1));
        txtDob.setText(valueAt(modelRow, 2));
        cmbGender.setSelectedItem("Nam".equals(valueAt(modelRow, 3)) ? "Nam" : "Nữ");
        txtPhone.setText(valueAt(modelRow, 4));
        txtParent.setText(valueAt(modelRow, 5));
        txtParentPhone.setText(valueAt(modelRow, 6));
        setFormModeEdit();
    }

    private String valueAt(int row, int col) {
        Object v = tableModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void search() {
        if (rowSorter == null) return;
        String keyword = txtSearch == null ? "" : txtSearch.getText().trim();
        rowSorter.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        updateShowingCounter();
    }

    private void updateShowingCounter() {
        if (lblShowingStudents != null && tblStudents != null) {
            lblShowingStudents.setText(String.valueOf(tblStudents.getRowCount()));
        }
        updateClassInsight();
    }

    private void updateClassInsight() {
        if (lblClassInsight == null || tblStudents == null) return;
        int showing = tblStudents.getRowCount();
        lblClassInsight.setText("Đang hiển thị " + showing + " học viên");
        if (lblFilterBadge != null) lblFilterBadge.setText(showing + " kết quả");
        if (lblTableSubtitle != null) lblTableSubtitle.setText("Tra cứu / lọc nhanh");
    }

    private void refreshData() {
        setLoadingState(true, "Đang tải dữ liệu...");
        try {
            List<Student> list = studentService.getAllStudents();
            tableModel.setRowCount(0);

            for (Student s : list) {
                String gender = "M".equalsIgnoreCase(s.getGender()) ? "Nam" : "Nữ";
                tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    safe(s.getFullName()),
                    s.getDob() != null ? sdf.format(s.getDob()) : "",
                    gender,
                    safe(s.getPhone()),
                    safe(s.getParentName()),
                    safe(s.getParentPhone())
                });
            }

            lblTotalStudents.setText(String.valueOf(list.size()));
            search();
            setLoadingState(false, "Dữ liệu đã cập nhật");
        } catch (Exception ex) {
            setLoadingState(false, "Lỗi tải dữ liệu");
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setLoadingState(boolean loading, String message) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        if (lblStatus != null) lblStatus.setText(message);
    }

    private String safe(Object v) {
        return v == null ? "" : v.toString();
    }

    private void saveStudent() {
        try {
            validateForm();

            java.util.Date parsedDate;
            try {
                parsedDate = sdf.parse(txtDob.getText().trim());
            } catch (ParseException e) {
                throw new Exception("Ngày sinh không đúng định dạng dd/MM/yyyy. Ví dụ: 25/12/2010");
            }

            Student s = new Student();
            s.setFullName(txtName.getText().trim());
            s.setGender(cmbGender.getSelectedItem().toString().equals("Nam") ? "M" : "F");
            s.setPhone(txtPhone.getText().trim());
            s.setParentName(txtParent.getText().trim());
            s.setParentPhone(txtParentPhone.getText().trim());
            s.setDob(new java.sql.Date(parsedDate.getTime()));

            if (txtId.getText().trim().isEmpty()) {
                studentService.addStudent(s);
                JOptionPane.showMessageDialog(this, "Đã thêm học viên mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                s.setStudentId(Integer.parseInt(txtId.getText().trim()));
                studentService.updateStudent(s);
                JOptionPane.showMessageDialog(this, "Cập nhật học viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }

            clearForm();
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void validateForm() throws Exception {
        if (txtName.getText().trim().isEmpty()) throw new Exception("Vui lòng nhập họ và tên.");
        if (txtDob.getText().trim().isEmpty()) throw new Exception("Vui lòng nhập ngày sinh.");
        if (txtPhone.getText().trim().isEmpty()) throw new Exception("Vui lòng nhập số điện thoại.");
        if (txtParent.getText().trim().isEmpty()) throw new Exception("Vui lòng nhập tên phụ huynh.");

        String phoneRegex = "0\\d{9,10}";
        if (!txtPhone.getText().trim().matches(phoneRegex)) {
            throw new Exception("Số điện thoại học viên phải bắt đầu bằng 0 và có 10-11 số.");
        }

        String parentPhone = txtParentPhone.getText().trim();
        if (!parentPhone.isEmpty() && !parentPhone.matches(phoneRegex)) {
            throw new Exception("SĐT phụ huynh phải bắt đầu bằng 0 và có 10-11 số.");
        }
    }

    private void deleteStudent() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học viên cần xóa từ bảng.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xóa học viên này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(Integer.parseInt(txtId.getText().trim()));
                JOptionPane.showMessageDialog(this, "Đã xóa học viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi xóa dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtDob.setText("");
        txtPhone.setText("");
        txtParent.setText("");
        txtParentPhone.setText("");
        cmbGender.setSelectedIndex(0);
        tblStudents.clearSelection();
        setFormModeNew();
        txtName.requestFocusInWindow();
    }

    private void setFormModeNew() {
        if (lblFormMode != null) lblFormMode.setText("Tạo hồ sơ mới");
        if (btnSave != null) btnSave.setText("Lưu");
        if (btnDelete != null) btnDelete.setEnabled(false);
    }

    private void setFormModeEdit() {
        if (lblFormMode != null) lblFormMode.setText("Đang sửa mã: " + txtId.getText().trim());
        if (btnSave != null) btnSave.setText("Cập nhật");
        if (btnDelete != null) btnDelete.setEnabled(true);
    }

    private void addFormLabel(JPanel card, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createVerticalStrut(5));
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

    private static class TooltipCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(PRIMARY_SOFT);
                setForeground(PRIMARY_DARK);
            } else {
                setForeground(TEXT_MAIN);
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }

            if (column == 0 && !isSelected) {
                setForeground(PRIMARY_DARK);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 3) { // Giới tính
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    String v = value == null ? "" : value.toString();
                    if ("Nam".equals(v))      { setForeground(new Color(37, 99, 235)); }
                    else if ("Nữ".equals(v))  { setForeground(new Color(219, 39, 119)); }
                }
            } else {
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            return this;
        }
    }

    private static class ClassBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();

            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 8, 4, 8));
            label.setToolTipText(text);

            if (isSelected) {
                label.setBackground(PRIMARY_SOFT);
                label.setForeground(PRIMARY_DARK);
                return label;
            }

            if ("Chưa xếp lớp".equalsIgnoreCase(text)) {
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(new Color(100, 116, 139));
            } else {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));
            }

            return label;
        }
    }

    private static class ActionButton extends JButton {
        private final Color bg;
        private final Color fg;
        private final Color border;
        private boolean hover = false;

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
            setMargin(new Insets(8, 12, 8, 12));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { hover = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent evt) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentBg;
            Color currentBorder;
            Color currentFg;

            if (!isEnabled()) {
                currentBg = new Color(248, 250, 252);
                currentBorder = new Color(226, 232, 240);
                currentFg = DISABLED;
            } else {
                currentBg = hover ? darken(bg, 0.94f) : bg;
                currentBorder = border;
                currentFg = fg;
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(currentBorder);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();

            setForeground(currentFg);
            super.paintComponent(g);
        }

        private static Color darken(Color c, float factor) {
            return new Color(
                    Math.max((int)(c.getRed() * factor), 0),
                    Math.max((int)(c.getGreen() * factor), 0),
                    Math.max((int)(c.getBlue() * factor), 0)
            );
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
