package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.service.TeacherAssignmentService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Phiên bản hoàn chỉnh hơn cho màn hình Phân công giáo viên.
 *
 * Đã rà soát và xử lý các lỗi UI/UX thường gặp:
 * - Nút lưu không còn bị ẩn/mờ khó nhìn khi disabled.
 * - Footer nút luôn nằm cố định, không bị mất khi màn hình thấp.
 * - Chọn dòng đúng khi bảng có sort/filter.
 * - Không lỗi search khi nhập ký tự đặc biệt.
 * - ComboBox dùng object TeacherItem, không phụ thuộc index dễ sai.
 * - Tự chọn giáo viên hiện tại trong combo nếu lớp đã có phân công.
 * - Không cho lưu nếu chưa chọn lớp, chưa chọn giáo viên, hoặc chọn trùng giáo viên hiện tại.
 * - Có xác nhận nghiệp vụ trước khi lưu.
 * - Load dữ liệu bằng SwingWorker để UI không bị đứng.
 * - Có empty state khi không có dữ liệu hoặc không có kết quả tìm kiếm.
 */
public class TeacherAssignmentPanel extends JPanel {

    private static final Color PRIMARY       = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK  = new Color(83, 70, 190);
    private static final Color PRIMARY_LIGHT = new Color(238, 234, 255);

    private static final Color BG_PAGE       = new Color(248, 249, 250);
    private static final Color BG_CARD       = Color.WHITE;
    private static final Color BORDER_C      = new Color(222, 226, 230);

    private static final Color TEXT_MAIN     = new Color(33, 37, 41);
    private static final Color TEXT_MUTE     = new Color(108, 117, 125);
    private static final Color SUCCESS       = new Color(25, 135, 84);
    private static final Color WARNING       = new Color(253, 126, 20);
    private static final Color DANGER        = new Color(220, 53, 69);
    private static final Color DISABLED_BG   = new Color(233, 236, 239);
    private static final Color EXCEL_GRID    = new Color(218, 220, 224);
    private static final Color EXCEL_HEADER  = new Color(248, 249, 250);
    private static final Color EXCEL_ROW_ALT = new Color(252, 253, 255);

    private final TeacherAssignmentService service = new TeacherAssignmentService();

    private DefaultTableModel tableModel;
    private JTable tblClasses;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtSearch;
    private JTextField txtSelectedClass;
    private JTextField txtCurrentTeacher;
    private JComboBox<TeacherItem> cmbTeachers;

    private JLabel lblTotalClasses;
    private JLabel lblAssignedClasses;
    private JLabel lblUnassignedClasses;
    private JLabel lblShowing;
    private JLabel lblStatus;
    private JLabel lblEmptyState;

    private JButton btnReload;
    private JButton btnClearSearch;
    private JButton btnClearSelection;
    private JButton btnSave;

    private JPanel tableContainer;
    private CardLayout tableCards;

    private List<TeacherItem> teacherItems = new ArrayList<>();

    private int currentClassId = -1;
    private String currentClassName = "";
    private String currentTeacherName = "";
    private int currentTeacherId = -1;

    private boolean loading = false;

    public TeacherAssignmentPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Phân công giáo viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Chọn lớp học, kiểm tra giáo viên hiện tại và lưu giáo viên phụ trách mới");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTE);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(subtitle);

        btnReload = new AppButton("Làm mới dữ liệu", ButtonStyle.OUTLINE, PRIMARY);
        btnReload.setPreferredSize(new Dimension(150, 44));
        btnReload.setToolTipText("Tải lại danh sách lớp và giáo viên");
        btnReload.addActionListener(e -> loadData());

        header.add(titleBox, BorderLayout.WEST);
        header.add(btnReload, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        body.add(buildMetrics(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildTableCard(),
                buildAssignCard()
        );
        split.setResizeWeight(0.68);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setOpaque(false);

        body.add(split, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildMetrics() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblTotalClasses = new JLabel("0");
        lblAssignedClasses = new JLabel("0");
        lblUnassignedClasses = new JLabel("0");

        row.add(metricCard("Tổng số lớp", lblTotalClasses, PRIMARY));
        row.add(metricCard("Đã phân công", lblAssignedClasses, SUCCESS));
        row.add(metricCard("Chưa phân công", lblUnassignedClasses, WARNING));

        return row;
    }

    private JPanel metricCard(String title, JLabel value, Color accent) {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(TEXT_MUTE);

        value.setFont(new Font("Segoe UI", Font.BOLD, 26));
        value.setForeground(accent);

        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 3));
        line.setBackground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(line, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMinimumSize(new Dimension(650, 430));

        card.add(buildTableToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Tên lớp", "Môn học", "Lịch học", "Giáo viên phụ trách", "teacher_id"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 5) return Integer.class;
                return String.class;
            }
        };

        tblClasses = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : EXCEL_ROW_ALT);
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(PRIMARY_LIGHT);
                    c.setForeground(PRIMARY_DARK);
                }

                if (c instanceof JComponent) {
                    Object value = getValueAt(row, col);
                    ((JComponent) c).setToolTipText(value == null ? "" : value.toString());
                }

                return c;
            }
        };

        tblClasses.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblClasses.setRowHeight(40);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Excel-like table: hiện đầy đủ ô ngang/dọc để dễ dò dữ liệu.
        tblClasses.setShowGrid(true);
        tblClasses.setShowHorizontalLines(true);
        tblClasses.setShowVerticalLines(true);
        tblClasses.setGridColor(EXCEL_GRID);
        tblClasses.setIntercellSpacing(new Dimension(1, 1));
        tblClasses.setFillsViewportHeight(true);
        tblClasses.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tblClasses.setSelectionBackground(PRIMARY_LIGHT);
        tblClasses.setSelectionForeground(PRIMARY_DARK);
        tblClasses.setRowMargin(1);

        JTableHeader header = tblClasses.getTableHeader();
        header.setDefaultRenderer(new ExcelHeaderRenderer());
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(TEXT_MUTE);
        header.setBackground(EXCEL_HEADER);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        tblClasses.setDefaultRenderer(Object.class, new ExcelCellRenderer(SwingConstants.LEFT));
        tblClasses.setDefaultRenderer(Integer.class, new ExcelCellRenderer(SwingConstants.CENTER));

        TableColumnModel columns = tblClasses.getColumnModel();
        columns.getColumn(0).setPreferredWidth(55);
        columns.getColumn(1).setPreferredWidth(185);
        columns.getColumn(2).setPreferredWidth(170);
        columns.getColumn(3).setPreferredWidth(170);
        columns.getColumn(4).setPreferredWidth(230);

        // Ẩn teacher_id khỏi UI nhưng vẫn giữ để xử lý nghiệp vụ chính xác.
        columns.removeColumn(columns.getColumn(5));

        rowSorter = new TableRowSorter<>(tableModel);
        tblClasses.setRowSorter(rowSorter);

        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onClassSelected();
            }
        });

        tblClasses.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && currentClassId != -1) {
                    cmbTeachers.requestFocusInWindow();
                    cmbTeachers.showPopup();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tblClasses);
        tableScroll.setBorder(new LineBorder(EXCEL_GRID, 1, false));
        tableScroll.getViewport().setBackground(Color.WHITE);

        lblEmptyState = new JLabel("Không có lớp học phù hợp với từ khóa tìm kiếm", SwingConstants.CENTER);
        lblEmptyState.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblEmptyState.setForeground(TEXT_MUTE);
        lblEmptyState.setBorder(new EmptyBorder(40, 20, 40, 20));

        tableCards = new CardLayout();
        tableContainer = new JPanel(tableCards);
        tableContainer.setBackground(BG_CARD);
        tableContainer.add(tableScroll, "TABLE");
        tableContainer.add(lblEmptyState, "EMPTY");

        card.add(tableContainer, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm lớp");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_MAIN);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(310, 38));
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.setToolTipText("Tìm theo tên lớp, môn học, lịch học hoặc giáo viên");

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearch(); }
            public void removeUpdate(DocumentEvent e) { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }
        });

        btnClearSearch = new AppButton("Xóa tìm kiếm", ButtonStyle.OUTLINE, TEXT_MUTE);
        btnClearSearch.setPreferredSize(new Dimension(120, 38));
        btnClearSearch.addActionListener(e -> txtSearch.setText(""));

        JPanel searchGroup = new JPanel(new BorderLayout(10, 0));
        searchGroup.setOpaque(false);
        searchGroup.add(lblSearch, BorderLayout.WEST);
        searchGroup.add(txtSearch, BorderLayout.CENTER);
        searchGroup.add(btnClearSearch, BorderLayout.EAST);

        lblShowing = new JLabel("Đang hiển thị: 0");
        lblShowing.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblShowing.setForeground(TEXT_MUTE);

        toolbar.add(searchGroup, BorderLayout.WEST);
        toolbar.add(lblShowing, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildAssignCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        card.setMinimumSize(new Dimension(390, 430));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_CARD);
        form.setBorder(new EmptyBorder(22, 24, 18, 24));

        JLabel title = new JLabel("Thông tin phân công");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);

        lblStatus = new JLabel("Chưa chọn lớp");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTE);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(Box.createVerticalStrut(6));
        form.add(lblStatus);

        form.add(Box.createVerticalStrut(22));

        addFormLabel(form, "Lớp đang chọn");
        txtSelectedClass = readonlyField("Chọn một lớp ở bảng bên trái");
        form.add(txtSelectedClass);
        form.add(Box.createVerticalStrut(14));

        addFormLabel(form, "Giáo viên hiện tại");
        txtCurrentTeacher = readonlyField("Chưa có thông tin");
        form.add(txtCurrentTeacher);
        form.add(Box.createVerticalStrut(14));

        addFormLabel(form, "Chọn giáo viên mới");
        cmbTeachers = new JComboBox<>();
        cmbTeachers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbTeachers.setBackground(Color.WHITE);
        cmbTeachers.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbTeachers.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbTeachers.setRenderer(new TeacherRenderer());
        cmbTeachers.addActionListener(e -> updateButtonState());
        form.add(cmbTeachers);

        JLabel hint = new JLabel("<html><div style='width:310px'>Mẹo: nhấp đúp vào một lớp để chuyển nhanh sang ô chọn giáo viên.</div></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTE);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(12, 0, 0, 0));
        form.add(hint);

        form.add(Box.createVerticalGlue());

        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setBackground(BG_CARD);
        footer.setBorder(new EmptyBorder(14, 24, 22, 24));

        btnClearSelection = new AppButton("Bỏ chọn", ButtonStyle.OUTLINE, TEXT_MUTE);
        btnClearSelection.setToolTipText("Bỏ chọn lớp hiện tại");
        btnClearSelection.addActionListener(e -> clearSelection());

        btnSave = new AppButton("Lưu phân công", ButtonStyle.FILLED, PRIMARY);
        btnSave.setToolTipText("Lưu giáo viên phụ trách cho lớp đang chọn");
        btnSave.addActionListener(e -> saveAssignment());

        footer.add(btnClearSelection);
        footer.add(btnSave);

        card.add(form, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void loadData() {
        if (loading) return;

        setLoading(true);
        int keepClassId = currentClassId;

        SwingWorker<LoadResult, Void> worker = new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                LoadResult result = new LoadResult();
                result.classes = service.getAllClasses();
                result.teachers = service.getTeachers();
                return result;
            }

            @Override
            protected void done() {
                try {
                    LoadResult result = get();
                    renderLoadedData(result);
                    applySearch();

                    if (keepClassId != -1) {
                        restoreSelection(keepClassId);
                    } else {
                        clearSelection();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            TeacherAssignmentPanel.this,
                            "Không thể tải dữ liệu.\nChi tiết: " + ex.getMessage(),
                            "Lỗi tải dữ liệu",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoading(false);
                    updateButtonState();
                }
            }
        };

        worker.execute();
    }

    private void renderLoadedData(LoadResult result) {
        tableModel.setRowCount(0);

        int assigned = 0;
        int unassigned = 0;

        for (Map<String, Object> map : result.classes) {
            int classId = toInt(map.get("class_id"));
            String className = safeText(map.get("class_name"));
            String subjectName = safeText(map.get("subject_name"));
            String schedule = safeText(map.get("schedule"));
            String teacherName = safeText(map.get("teacher_name"));
            int teacherId = toInt(firstNotNull(map.get("teacher_id"), map.get("user_id")));

            if (teacherName.isEmpty()) {
                teacherName = "Chưa phân công";
                teacherId = -1;
                unassigned++;
            } else {
                assigned++;
            }

            tableModel.addRow(new Object[]{
                    classId,
                    className,
                    subjectName,
                    schedule,
                    teacherName,
                    teacherId
            });
        }

        teacherItems.clear();
        cmbTeachers.removeAllItems();

        if (result.teachers != null) {
            for (Map<String, Object> teacher : result.teachers) {
                TeacherItem item = new TeacherItem(
                        toInt(teacher.get("user_id")),
                        safeText(teacher.get("full_name")),
                        safeText(teacher.get("major"))
                );

                if (!item.name.isEmpty()) {
                    teacherItems.add(item);
                    cmbTeachers.addItem(item);
                }
            }
        }

        lblTotalClasses.setText(String.valueOf(result.classes.size()));
        lblAssignedClasses.setText(String.valueOf(assigned));
        lblUnassignedClasses.setText(String.valueOf(unassigned));

        updateButtonState();
    }

    private void onClassSelected() {
        int viewRow = tblClasses.getSelectedRow();

        if (viewRow < 0) {
            resetSelectionInfo();
            updateButtonState();
            return;
        }

        int modelRow = tblClasses.convertRowIndexToModel(viewRow);

        currentClassId = toInt(tableModel.getValueAt(modelRow, 0));
        currentClassName = safeText(tableModel.getValueAt(modelRow, 1));
        currentTeacherName = safeText(tableModel.getValueAt(modelRow, 4));
        currentTeacherId = toInt(tableModel.getValueAt(modelRow, 5));

        txtSelectedClass.setText(currentClassName);
        txtCurrentTeacher.setText(currentTeacherName.isEmpty() ? "Chưa phân công" : currentTeacherName);

        lblStatus.setText("Đang chọn lớp mã: " + currentClassId);
        lblStatus.setForeground(PRIMARY_DARK);

        selectCurrentTeacherInCombo();
        updateButtonState();
    }

    private void selectCurrentTeacherInCombo() {
        if (currentTeacherId == -1) return;

        for (int i = 0; i < cmbTeachers.getItemCount(); i++) {
            TeacherItem item = cmbTeachers.getItemAt(i);
            if (item != null && item.id == currentTeacherId) {
                cmbTeachers.setSelectedIndex(i);
                return;
            }
        }

        // Fallback khi service không trả teacher_id trong danh sách lớp.
        if (!currentTeacherName.isEmpty()) {
            for (int i = 0; i < cmbTeachers.getItemCount(); i++) {
                TeacherItem item = cmbTeachers.getItemAt(i);
                if (item != null && currentTeacherName.equalsIgnoreCase(item.name)) {
                    cmbTeachers.setSelectedIndex(i);
                    currentTeacherId = item.id;
                    return;
                }
            }
        }
    }

    private void clearSelection() {
        if (tblClasses != null) {
            tblClasses.clearSelection();
        }
        resetSelectionInfo();
        updateButtonState();
    }

    private void resetSelectionInfo() {
        currentClassId = -1;
        currentClassName = "";
        currentTeacherName = "";
        currentTeacherId = -1;

        if (txtSelectedClass != null) txtSelectedClass.setText("Chọn một lớp ở bảng bên trái");
        if (txtCurrentTeacher != null) txtCurrentTeacher.setText("Chưa có thông tin");

        if (lblStatus != null) {
            lblStatus.setText("Chưa chọn lớp");
            lblStatus.setForeground(TEXT_MUTE);
        }
    }

    private void restoreSelection(int classId) {
        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            if (toInt(tableModel.getValueAt(modelRow, 0)) == classId) {
                int viewRow = tblClasses.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    tblClasses.setRowSelectionInterval(viewRow, viewRow);
                    tblClasses.scrollRectToVisible(tblClasses.getCellRect(viewRow, 0, true));
                    onClassSelected();
                    return;
                }
            }
        }

        clearSelection();
    }

    private void applySearch() {
        if (rowSorter == null) return;

        String q = txtSearch == null ? "" : txtSearch.getText().trim();

        if (q.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        }

        updateShowingState();
        updateButtonState();

        // Nếu dòng đang chọn bị filter mất thì reset form để tránh lưu nhầm.
        if (tblClasses.getSelectedRow() < 0 && currentClassId != -1) {
            resetSelectionInfo();
        }
    }

    private void updateShowingState() {
        int visible = tblClasses == null ? 0 : tblClasses.getRowCount();
        if (lblShowing != null) lblShowing.setText("Đang hiển thị: " + visible);

        if (tableCards != null && tableContainer != null) {
            tableCards.show(tableContainer, visible == 0 ? "EMPTY" : "TABLE");
        }

        if (btnClearSearch != null) {
            btnClearSearch.setEnabled(txtSearch != null && !txtSearch.getText().trim().isEmpty());
        }
    }

    private void saveAssignment() {
        if (currentClassId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một lớp học trong bảng trước khi lưu phân công.",
                    "Chưa chọn lớp",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        TeacherItem selectedTeacher = (TeacherItem) cmbTeachers.getSelectedItem();
        if (selectedTeacher == null || selectedTeacher.id == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn giáo viên cần phân công.",
                    "Chưa chọn giáo viên",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (selectedTeacher.id == currentTeacherId) {
            JOptionPane.showMessageDialog(
                    this,
                    "Giáo viên này đang được phân công cho lớp.\nVui lòng chọn giáo viên khác nếu muốn thay đổi.",
                    "Không có thay đổi",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String oldTeacher = currentTeacherName.isEmpty() || currentTeacherName.equals("Chưa phân công")
                ? "Chưa phân công"
                : currentTeacherName;

        String msg =
                "Xác nhận lưu phân công?\n\n" +
                "Lớp: " + currentClassName + "\n" +
                "Giáo viên hiện tại: " + oldTeacher + "\n" +
                "Giáo viên mới: " + selectedTeacher.name;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Xác nhận phân công",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        setLoading(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.assignTeacherToClass(selectedTeacher.id, currentClassId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            TeacherAssignmentPanel.this,
                            "Đã lưu phân công giáo viên thành công.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            TeacherAssignmentPanel.this,
                            "Không thể lưu phân công.\nChi tiết: " + ex.getMessage(),
                            "Cảnh báo xung đột",
                            JOptionPane.WARNING_MESSAGE
                    );
                } finally {
                    setLoading(false);
                    updateButtonState();
                }
            }
        };

        worker.execute();
    }

    private void updateButtonState() {
        boolean hasClass = currentClassId != -1;
        boolean hasTeacher = cmbTeachers != null && cmbTeachers.getSelectedItem() instanceof TeacherItem;
        TeacherItem selected = hasTeacher ? (TeacherItem) cmbTeachers.getSelectedItem() : null;
        boolean changed = selected != null && selected.id != currentTeacherId;
        boolean canSave = !loading && hasClass && hasTeacher && changed;

        if (btnSave != null) {
            btnSave.setEnabled(canSave);
            if (!hasClass) {
                btnSave.setToolTipText("Chọn một lớp trước khi lưu phân công");
            } else if (!hasTeacher) {
                btnSave.setToolTipText("Chưa có giáo viên để phân công");
            } else if (!changed) {
                btnSave.setToolTipText("Giáo viên đang chọn trùng với giáo viên hiện tại");
            } else {
                btnSave.setToolTipText("Lưu giáo viên phụ trách mới cho lớp đang chọn");
            }
        }

        if (btnClearSelection != null) btnClearSelection.setEnabled(!loading && hasClass);
        if (btnReload != null) btnReload.setEnabled(!loading);
        if (cmbTeachers != null) cmbTeachers.setEnabled(!loading && cmbTeachers.getItemCount() > 0);
        if (txtSearch != null) txtSearch.setEnabled(!loading);
        if (btnClearSearch != null) btnClearSearch.setEnabled(!loading && txtSearch != null && !txtSearch.getText().trim().isEmpty());
    }

    private void setLoading(boolean value) {
        loading = value;
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());

        if (btnReload != null) btnReload.setText(value ? "Đang tải..." : "Làm mới dữ liệu");
        updateButtonState();
    }

    private void addFormLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MUTE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
    }

    private JTextField readonlyField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_MAIN);
        field.setBackground(new Color(247, 248, 250));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private Object firstNotNull(Object a, Object b) {
        return a != null ? a : b;
    }

    private String safeText(Object value) {
        if (value == null) return "";
        String text = value.toString().trim();
        if (text.equalsIgnoreCase("null")) return "";
        return text;
    }

    private int toInt(Object value) {
        if (value == null) return -1;
        if (value instanceof Number) return ((Number) value).intValue();

        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private enum ButtonStyle {
        FILLED,
        OUTLINE
    }

    private static class LoadResult {
        List<Map<String, Object>> classes = new ArrayList<>();
        List<Map<String, Object>> teachers = new ArrayList<>();
    }

    private static class TeacherItem {
        final int id;
        final String name;
        final String major;

        TeacherItem(int id, String name, String major) {
            this.id = id;
            this.name = name == null ? "" : name;
            this.major = major == null ? "" : major;
        }

        @Override
        public String toString() {
            return major.isEmpty() ? name : name + " - " + major;
        }
    }

    private static class TeacherRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBorder(new EmptyBorder(6, 10, 6, 10));

            if (value instanceof TeacherItem) {
                TeacherItem item = (TeacherItem) value;
                setText(item.major.isEmpty() ? item.name : item.name + " - " + item.major);
            }

            return this;
        }
    }

    /**
     * Header giống Excel: nền sáng, viền rõ, chữ đậm.
     */
    private static class ExcelHeaderRenderer extends DefaultTableCellRenderer {
        ExcelHeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(TEXT_MUTE);
            setBackground(EXCEL_HEADER);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 1, EXCEL_GRID),
                    new EmptyBorder(0, 10, 0, 10)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    /**
     * Cell giống Excel: có padding, có viền ô, không bị dính chữ vào mép.
     */
    private static class ExcelCellRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        ExcelCellRenderer(int alignment) {
            this.alignment = alignment;
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 1, EXCEL_GRID),
                    new EmptyBorder(0, 10, 0, 10)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(alignment);
            setText(value == null ? "" : value.toString());

            if (isSelected) {
                setBackground(PRIMARY_LIGHT);
                setForeground(PRIMARY_DARK);
                setBorder(new CompoundBorder(
                        new MatteBorder(1, 1, 1, 1, PRIMARY),
                        new EmptyBorder(0, 9, 0, 9)
                ));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : EXCEL_ROW_ALT);
                setForeground(TEXT_MAIN);
                setBorder(new CompoundBorder(
                        new MatteBorder(0, 0, 1, 1, EXCEL_GRID),
                        new EmptyBorder(0, 10, 0, 10)
                ));
            }

            if (value != null) {
                setToolTipText(value.toString());
            } else {
                setToolTipText("");
            }

            return this;
        }
    }

    /**
     * Button custom để khắc phục lỗi LookAndFeel làm chữ bị mờ/ẩn khi enabled/disabled.
     */
    private static class AppButton extends JButton {
        private final ButtonStyle style;
        private final Color mainColor;

        AppButton(String text, ButtonStyle style, Color mainColor) {
            super(text);
            this.style = style;
            this.mainColor = mainColor;

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 42));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    repaint();
                }

                @Override public void mouseExited(MouseEvent e) {
                    repaint();
                }
            });
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean hover = getModel().isRollover() && isEnabled();
            boolean pressed = getModel().isPressed() && isEnabled();

            Color bg;
            Color fg;
            Color border;

            if (!isEnabled()) {
                bg = DISABLED_BG;
                fg = TEXT_MUTE;
                border = BORDER_C;
            } else if (style == ButtonStyle.FILLED) {
                bg = pressed ? mainColor.darker() : (hover ? mainColor.darker() : mainColor);
                fg = Color.WHITE;
                border = bg;
            } else {
                bg = hover ? PRIMARY_LIGHT : Color.WHITE;
                fg = hover ? PRIMARY_DARK : mainColor;
                border = mainColor;
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.setFont(getFont());
            g2.setColor(fg);
            g2.drawString(getText(), x, y);

            g2.dispose();
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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
