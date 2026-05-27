package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.Room;
import com.mycompany.myapp.repository.RoomDAO;
import com.mycompany.myapp.service.StudyClassService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Màn hình sắp lịch học cho lớp.
 *
 * Thiết kế lại theo hướng nghiệp vụ:
 * - Trái: chọn lớp học.
 * - Phải: xem lịch học của lớp.
 * - Thêm/Sửa lịch bằng dialog, không để form cố định gây rối.
 * - Ẩn ID kỹ thuật khỏi giao diện.
 * - Nút thao tác tự bật/tắt theo trạng thái chọn.
 */
public class ClassSchedulePanel extends JPanel {

    // =========================================================
    // DESIGN TOKENS
    // =========================================================
    private static final Color PRIMARY      = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);
    private static final Color BG_PAGE      = new Color(248, 250, 252);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color BORDER_C     = new Color(226, 232, 240);
    private static final Color TEXT_MAIN    = new Color(15, 23, 42);
    private static final Color TEXT_MUTE    = new Color(100, 116, 139);
    private static final Color SUCCESS      = new Color(22, 163, 74);
    private static final Color DANGER       = new Color(220, 38, 38);
    private static final Color TEAL         = new Color(20, 184, 166);
    private static final Color WARNING_BG   = new Color(255, 251, 235);
    private static final Color WARNING_TXT  = new Color(180, 83, 9);

    private static final String[] DAY_LABELS = {
        "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };

    private static final int[] DAY_VALUES = {2, 3, 4, 5, 6, 7, 8};

    // =========================================================
    // SERVICES
    // =========================================================
    private final StudyClassService service = new StudyClassService();
    private final RoomDAO roomDAO = new RoomDAO();

    // =========================================================
    // LEFT: CLASS LIST
    // =========================================================
    private DefaultTableModel classModel;
    private JTable tblClasses;
    private TableRowSorter<DefaultTableModel> classSorter;
    private JTextField txtClassSearch;
    private JLabel lblClassCount;

    // =========================================================
    // RIGHT: SCHEDULE
    // =========================================================
    private DefaultTableModel schedModel;
    private JTable tblSchedules;
    private JLabel lblSelectedClass;
    private JLabel lblSchedCount;
    private JLabel lblEmptyHint;

    private ActionButton btnAddSchedule;
    private ActionButton btnEditSchedule;
    private ActionButton btnDeleteSchedule;

    // =========================================================
    // STATE
    // =========================================================
    private int selectedClassId = -1;
    private String selectedClassName = "";
    private final List<RoomItem> roomItems = new ArrayList<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public ClassSchedulePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadRooms();
        loadClasses();
        updateActionState();
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 4));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Sắp lịch học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);

        JLabel subTitle = new JLabel("Quản lý lịch học theo lớp, phòng học và thời gian");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(TEXT_MUTE);

        titlePanel.add(title);
        titlePanel.add(subTitle);

        ActionButton btnRefresh = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(112, 40));
        btnRefresh.addActionListener(e -> {
            loadRooms();
            loadClasses();
            if (selectedClassId > 0) {
                refreshSchedules();
            }
        });

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnRefresh);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actionPanel, BorderLayout.EAST);

        return header;
    }

    // =========================================================
    // MAIN CONTENT
    // =========================================================
    private JPanel buildContent() {
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildClassListCard(),
            buildScheduleCard()
        );

        splitPane.setResizeWeight(0.30);
        splitPane.setDividerSize(10);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_PAGE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitPane, BorderLayout.CENTER);

        return wrapper;
    }

    // =========================================================
    // LEFT CARD: CLASS LIST
    // =========================================================
    private JPanel buildClassListCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);

        JLabel title = new JLabel("Danh sách lớp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);

        lblClassCount = new JLabel("0 lớp");
        lblClassCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblClassCount.setForeground(TEXT_MUTE);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(lblClassCount, BorderLayout.EAST);

        txtClassSearch = new JTextField();
        txtClassSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClassSearch.setPreferredSize(new Dimension(0, 36));
        txtClassSearch.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        txtClassSearch.setToolTipText("Tìm theo tên lớp hoặc môn học...");
        txtClassSearch.putClientProperty("JTextField.placeholderText", "Tìm lớp học...");

        txtClassSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterClasses(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterClasses(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        top.add(titleRow, BorderLayout.NORTH);
        top.add(txtClassSearch, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);

        /*
         * Model vẫn giữ ID để xử lý,
         * nhưng giao diện sẽ ẩn cột ID.
         */
        classModel = new DefaultTableModel(
            new String[]{"ID", "Tên lớp", "Môn học", "Số buổi"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblClasses = new JTable(classModel);
        configureTable(tblClasses);
        tblClasses.setRowHeight(54);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClasses.setDefaultRenderer(Object.class, new ClassListRenderer());

        classSorter = new TableRowSorter<>(classModel);
        tblClasses.setRowSorter(classSorter);

        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onClassSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblClasses);
        scrollPane.setBorder(new LineBorder(BORDER_C, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // RIGHT CARD: SCHEDULE LIST
    // =========================================================
    private JPanel buildScheduleCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        card.add(buildScheduleHeader(), BorderLayout.NORTH);
        card.add(buildScheduleTableArea(), BorderLayout.CENTER);

        return card;
    }

    private JPanel buildScheduleHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleArea = new JPanel(new GridLayout(2, 1, 0, 3));
        titleArea.setOpaque(false);

        lblSelectedClass = new JLabel("Chọn một lớp để xem lịch học");
        lblSelectedClass.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSelectedClass.setForeground(TEXT_MAIN);

        lblSchedCount = new JLabel("Chưa có lớp nào được chọn");
        lblSchedCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSchedCount.setForeground(TEXT_MUTE);

        titleArea.add(lblSelectedClass);
        titleArea.add(lblSchedCount);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        btnAddSchedule = new ActionButton("+ Thêm lịch", TEAL);
        btnEditSchedule = new ActionButton("Sửa lịch", Color.WHITE, PRIMARY, PRIMARY);
        btnDeleteSchedule = new ActionButton("Xóa lịch", Color.WHITE, DANGER, DANGER);

        btnAddSchedule.setPreferredSize(new Dimension(112, 36));
        btnEditSchedule.setPreferredSize(new Dimension(92, 36));
        btnDeleteSchedule.setPreferredSize(new Dimension(92, 36));

        btnAddSchedule.addActionListener(e -> openScheduleDialog(false));
        btnEditSchedule.addActionListener(e -> {
            if (getSelectedScheduleModelRow() < 0) {
                showWarn("Vui lòng chọn một lịch học để sửa.");
                return;
            }
            openScheduleDialog(true);
        });
        btnDeleteSchedule.addActionListener(e -> deleteSchedule());

        buttons.add(btnAddSchedule);
        buttons.add(btnEditSchedule);
        buttons.add(btnDeleteSchedule);

        header.add(titleArea, BorderLayout.CENTER);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    private JPanel buildScheduleTableArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        /*
         * Model vẫn giữ ID ở cột 0.
         * Khi hiển thị sẽ ẩn cột ID.
         */
        schedModel = new DefaultTableModel(
            new String[]{"ID", "Thứ", "Thời gian", "Phòng học"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSchedules = new JTable(schedModel);
        configureTable(tblSchedules);
        tblSchedules.setRowHeight(42);
        tblSchedules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSchedules.setDefaultRenderer(Object.class, new ScheduleRenderer());

        tblSchedules.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionState();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblSchedules);
        scrollPane.setBorder(new LineBorder(BORDER_C, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        lblEmptyHint = new JLabel("Chọn lớp ở danh sách bên trái để xem lịch học", SwingConstants.CENTER);
        lblEmptyHint.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblEmptyHint.setForeground(TEXT_MUTE);
        lblEmptyHint.setBorder(new EmptyBorder(60, 0, 60, 0));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // DATA LOADING
    // =========================================================
    private void loadClasses() {
        try {
            List<Map<String, Object>> rows = service.getClassesWithDetails();

            classModel.setRowCount(0);

            for (Map<String, Object> row : rows) {
                int classId = asInt(row.get("class_id"));
                String className = asString(row.get("class_name"));
                String subjectName = asString(row.get("subject_name"));

                /*
                 * Nếu service/repository của bạn đã có schedule_count,
                 * code này sẽ tự dùng. Nếu chưa có thì tạm để 0 và update sau.
                 */
                int scheduleCount = row.containsKey("schedule_count")
                    ? asInt(row.get("schedule_count"))
                    : 0;

                classModel.addRow(new Object[]{
                    classId,
                    className,
                    subjectName,
                    scheduleCount
                });
            }

            /*
             * Nếu hiện tại service chưa trả schedule_count,
             * vẫn cập nhật được bằng cách gọi từng lớp.
             * Sau này nên tối ưu ở query để tránh N+1 query.
             */
            if (!rows.isEmpty() && !rows.get(0).containsKey("schedule_count")) {
                updateScheduleCountForClasses();
            }

            lblClassCount.setText(classModel.getRowCount() + " lớp");

            hideColumn(tblClasses, 0);

        } catch (Exception e) {
            showError("Lỗi tải danh sách lớp: " + e.getMessage());
        }
    }

    private void updateScheduleCountForClasses() {
        for (int i = 0; i < classModel.getRowCount(); i++) {
            int classId = asInt(classModel.getValueAt(i, 0));
            try {
                int count = service.getSchedulesByClass(classId).size();
                classModel.setValueAt(count, i, 3);
            } catch (Exception ignored) {
            }
        }
    }

    private void loadRooms() {
        try {
            roomItems.clear();
            roomItems.add(new RoomItem(0, "-- Chọn phòng --"));

            List<Room> rooms = roomDAO.findAllActive();

            for (Room room : rooms) {
                roomItems.add(new RoomItem(room.getRoomId(), room.getRoomName()));
            }

        } catch (Exception e) {
            showError("Lỗi tải danh sách phòng: " + e.getMessage());
        }
    }

    private void refreshSchedules() {
        if (selectedClassId <= 0) {
            schedModel.setRowCount(0);
            updateActionState();
            return;
        }

        try {
            List<Map<String, Object>> schedules = service.getSchedulesByClass(selectedClassId);

            schedModel.setRowCount(0);

            for (Map<String, Object> row : schedules) {
                int scheduleId = asInt(row.get("schedule_id"));
                int dayOfWeek = asInt(row.get("day_of_week"));
                String startTime = normalizeTime(asString(row.get("start_time")));
                String endTime = normalizeTime(asString(row.get("end_time")));
                String roomName = asString(row.get("room_name"));

                schedModel.addRow(new Object[]{
                    scheduleId,
                    dayLabel(dayOfWeek),
                    startTime + "–" + endTime,
                    roomName
                });
            }

            lblSchedCount.setText(
                schedules.size() == 0
                    ? "Lớp này chưa có lịch học"
                    : schedules.size() + " buổi/tuần"
            );

            updateClassScheduleCount(selectedClassId, schedules.size());
            hideColumn(tblSchedules, 0);
            updateActionState();

        } catch (Exception e) {
            showError("Lỗi tải lịch học: " + e.getMessage());
        }
    }

    // =========================================================
    // EVENTS
    // =========================================================
    private void onClassSelected() {
        int viewRow = tblClasses.getSelectedRow();

        if (viewRow < 0) {
            selectedClassId = -1;
            selectedClassName = "";
            lblSelectedClass.setText("Chọn một lớp để xem lịch học");
            lblSchedCount.setText("Chưa có lớp nào được chọn");
            schedModel.setRowCount(0);
            updateActionState();
            return;
        }

        int modelRow = tblClasses.convertRowIndexToModel(viewRow);

        selectedClassId = asInt(classModel.getValueAt(modelRow, 0));
        selectedClassName = asString(classModel.getValueAt(modelRow, 1));

        lblSelectedClass.setText("Lịch học: " + selectedClassName);
        lblSchedCount.setText("Đang tải lịch học...");

        refreshSchedules();
    }

    private void filterClasses() {
        String keyword = txtClassSearch.getText().trim();

        if (keyword.isEmpty()) {
            classSorter.setRowFilter(null);
        } else {
            classSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
    }

    // =========================================================
    // DIALOG ADD/EDIT
    // =========================================================
    private void openScheduleDialog(boolean editMode) {
        if (selectedClassId <= 0) {
            showWarn("Vui lòng chọn lớp học trước.");
            return;
        }

        int modelRow = editMode ? getSelectedScheduleModelRow() : -1;

        if (editMode && modelRow < 0) {
            showWarn("Vui lòng chọn một lịch học để sửa.");
            return;
        }

        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            editMode ? "Sửa lịch học" : "Thêm lịch học",
            Dialog.ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(18, 20, 16, 20));

        JLabel title = new JLabel(editMode ? "Sửa lịch học" : "Thêm lịch học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Lớp: " + selectedClassName);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTE);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(sub);

        root.add(titlePanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<DayItem> cmbDayDialog = new JComboBox<>();
        for (int i = 0; i < DAY_LABELS.length; i++) {
            cmbDayDialog.addItem(new DayItem(DAY_VALUES[i], DAY_LABELS[i]));
        }
        configureCombo(cmbDayDialog);

        JTextField tfStartDialog = timeField("07:00");
        JTextField tfEndDialog = timeField("09:00");

        JComboBox<RoomItem> cmbRoomDialog = new JComboBox<>();
        for (RoomItem item : roomItems) {
            cmbRoomDialog.addItem(item);
        }
        configureCombo(cmbRoomDialog);

        int scheduleId = -1;

        if (editMode) {
            scheduleId = asInt(schedModel.getValueAt(modelRow, 0));
            String dayText = asString(schedModel.getValueAt(modelRow, 1));
            String timeText = asString(schedModel.getValueAt(modelRow, 2));
            String roomText = asString(schedModel.getValueAt(modelRow, 3));

            selectDay(cmbDayDialog, dayText);

            String[] parts = timeText.split("–");
            if (parts.length == 2) {
                tfStartDialog.setText(parts[0].trim());
                tfEndDialog.setText(parts[1].trim());
            }

            selectRoom(cmbRoomDialog, roomText);
        }

        addFormRow(form, gbc, 0, "Thứ", cmbDayDialog);
        addFormRow(form, gbc, 1, "Giờ bắt đầu", tfStartDialog);
        addFormRow(form, gbc, 2, "Giờ kết thúc", tfEndDialog);
        addFormRow(form, gbc, 3, "Phòng học", cmbRoomDialog);

        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        ActionButton btnCancel = new ActionButton("Hủy", Color.WHITE, TEXT_MUTE, BORDER_C);
        ActionButton btnSave = new ActionButton(editMode ? "Cập nhật" : "Lưu lịch", TEAL);

        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnSave.setPreferredSize(new Dimension(118, 38));

        final int finalScheduleId = scheduleId;

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                DayItem day = (DayItem) cmbDayDialog.getSelectedItem();
                RoomItem room = (RoomItem) cmbRoomDialog.getSelectedItem();

                String start = tfStartDialog.getText().trim();
                String end = tfEndDialog.getText().trim();

                if (day == null) {
                    showWarn("Vui lòng chọn thứ học.");
                    return;
                }

                if (room == null || room.id == 0) {
                    showWarn("Vui lòng chọn phòng học.");
                    return;
                }

                if (!isValidTime(start) || !isValidTime(end)) {
                    showWarn("Giờ học phải đúng định dạng HH:mm, ví dụ 07:30.");
                    return;
                }

                if (!isStartBeforeEnd(start, end)) {
                    showWarn("Giờ bắt đầu phải nhỏ hơn giờ kết thúc.");
                    return;
                }

                if (editMode) {
                    service.updateSchedule(finalScheduleId, room.id, day.value, start, end);
                    showInfo("Đã cập nhật lịch học thành công.");
                } else {
                    service.addSchedule(selectedClassId, room.id, day.value, start, end);
                    showInfo("Đã thêm lịch học thành công.");
                }

                dialog.dispose();
                refreshSchedules();
                tblSchedules.clearSelection();
                updateActionState();

            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setSize(new Dimension(430, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSchedule() {
        int modelRow = getSelectedScheduleModelRow();

        if (modelRow < 0) {
            showWarn("Vui lòng chọn một lịch học để xóa.");
            return;
        }

        int scheduleId = asInt(schedModel.getValueAt(modelRow, 0));
        String day = asString(schedModel.getValueAt(modelRow, 1));
        String time = asString(schedModel.getValueAt(modelRow, 2));
        String room = asString(schedModel.getValueAt(modelRow, 3));

        String message =
            "Bạn có chắc muốn xóa lịch học này?\n\n" +
            "Lớp: " + selectedClassName + "\n" +
            "Thời gian: " + day + ", " + time + "\n" +
            "Phòng: " + room;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            message,
            "Xác nhận xóa lịch",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteSchedule(scheduleId);
            showInfo("Đã xóa lịch học thành công.");
            refreshSchedules();
            tblSchedules.clearSelection();
            updateActionState();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // =========================================================
    // STATE
    // =========================================================
    private void updateActionState() {
        boolean hasClass = selectedClassId > 0;
        boolean hasSchedule = getSelectedScheduleModelRow() >= 0;

        if (btnAddSchedule != null) {
            btnAddSchedule.setEnabled(hasClass);
        }

        if (btnEditSchedule != null) {
            btnEditSchedule.setEnabled(hasClass && hasSchedule);
        }

        if (btnDeleteSchedule != null) {
            btnDeleteSchedule.setEnabled(hasClass && hasSchedule);
        }

        if (lblEmptyHint != null) {
            lblEmptyHint.setVisible(!hasClass);
        }
    }

    private void updateClassScheduleCount(int classId, int count) {
        for (int i = 0; i < classModel.getRowCount(); i++) {
            if (asInt(classModel.getValueAt(i, 0)) == classId) {
                classModel.setValueAt(count, i, 3);
                break;
            }
        }
    }

    private int getSelectedScheduleModelRow() {
        if (tblSchedules == null || tblSchedules.getSelectedRow() < 0) {
            return -1;
        }

        return tblSchedules.convertRowIndexToModel(tblSchedules.getSelectedRow());
    }

    // =========================================================
    // FORM HELPERS
    // =========================================================
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        JLabel lbl = fieldLabel(label);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.gridwidth = 1;
        field.setPreferredSize(new Dimension(240, 38));
        panel.add(field, gbc);
    }

    private JTextField timeField(String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        field.setToolTipText("Định dạng HH:mm, ví dụ 07:30");
        return field;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MUTE);
        return label;
    }

    private void configureCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(new LineBorder(BORDER_C, 1, true));
    }

    private void selectDay(JComboBox<DayItem> combo, String label) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).label.equals(label)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectRoom(JComboBox<RoomItem> combo, String roomName) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).name.equals(roomName)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private boolean isValidTime(String time) {
        return time != null && time.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private boolean isStartBeforeEnd(String start, String end) {
        return start.compareTo(end) < 0;
    }

    // =========================================================
    // TABLE CONFIG
    // =========================================================
    private void configureTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_C);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
    }

    private void hideColumn(JTable table, int modelColumnIndex) {
        try {
            TableColumnModel columnModel = table.getColumnModel();

            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                TableColumn column = columnModel.getColumn(i);

                if (table.convertColumnIndexToModel(i) == modelColumnIndex) {
                    columnModel.removeColumn(column);
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private String dayLabel(int dayOfWeek) {
        for (int i = 0; i < DAY_VALUES.length; i++) {
            if (DAY_VALUES[i] == dayOfWeek) {
                return DAY_LABELS[i];
            }
        }

        return String.valueOf(dayOfWeek);
    }

    private String normalizeTime(String value) {
        if (value == null) {
            return "";
        }

        value = value.trim();

        if (value.length() >= 5 && value.charAt(2) == ':') {
            return value.substring(0, 5);
        }

        return value;
    }

    private int asInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return Integer.parseInt(value.toString());
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarn(String message) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================
    // INNER CLASSES
    // =========================================================
    private static class DayItem {
        final int value;
        final String label;

        DayItem(int value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class RoomItem {
        final int id;
        final String name;

        RoomItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class ClassListRenderer extends DefaultTableCellRenderer {
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

            int modelRow = table.convertRowIndexToModel(row);

            String className = asString(classModel.getValueAt(modelRow, 1));
            String subjectName = asString(classModel.getValueAt(modelRow, 2));
            int count = asInt(classModel.getValueAt(modelRow, 3));

            if (column == 0) {
                setText("<html><b>" + escapeHtml(className) + "</b><br>" +
                    "<span style='color:#64748b'>" + escapeHtml(subjectName) + "</span></html>");
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setText(count + " buổi");
                setHorizontalAlignment(SwingConstants.CENTER);
            }

            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (isSelected) {
                setBackground(PRIMARY_SOFT);
                setForeground(PRIMARY_DARK);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(TEXT_MAIN);
            }

            return this;
        }
    }

    private class ScheduleRenderer extends DefaultTableCellRenderer {
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

            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (column == 0) {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
            }

            if (isSelected) {
                setBackground(PRIMARY_SOFT);
                setForeground(PRIMARY_DARK);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(TEXT_MAIN);
            }

            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);

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

            Color fill;

            if (!isEnabled()) {
                fill = new Color(226, 232, 240);
            } else if (hover) {
                fill = bg.darker();
            } else {
                fill = bg;
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.setColor(!isEnabled() ? BORDER_C : border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

            g2.dispose();

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

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }

        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}