package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.Room;
import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.repository.RoomDAO;
import com.mycompany.myapp.service.StudyClassService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Panel sắp lịch học cho lớp (CLASS_SCHEDULE).
 * Một lớp có thể có nhiều buổi học trong tuần.
 *
 * Layout:
 *   LEFT  (38%) – Danh sách lớp học (chọn lớp)
 *   RIGHT (62%) – Lịch học của lớp được chọn + form thêm/sửa
 */
public class ClassSchedulePanel extends JPanel {

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
    private static final Color TEAL         = new Color( 20, 184, 166);

    private static final String[] DAY_LABELS = {
        "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };
    private static final int[] DAY_VALUES = { 2, 3, 4, 5, 6, 7, 8 };

    private final StudyClassService service = new StudyClassService();
    private final RoomDAO           roomDAO  = new RoomDAO();

    // ── Class list (LEFT) ──
    private DefaultTableModel                 classModel;
    private JTable                            tblClasses;
    private TableRowSorter<DefaultTableModel> classSorter;
    private JTextField                        txtClassSearch;

    // ── Schedule table (RIGHT TOP) ──
    private DefaultTableModel                 schedModel;
    private JTable                            tblSchedules;
    private JLabel                            lblSelectedClass, lblSchedCount;

    // ── Form fields (RIGHT BOTTOM) ──
    private JComboBox<DayItem>  cmbDay;
    private JTextField          tfStart, tfEnd;
    private JComboBox<RoomItem> cmbRoom;
    private JLabel              lblFormMode;
    private ActionButton        btnSaveSchedule;

    private int selectedClassId   = -1;
    private int selectedScheduleId = -1; // -1 = chế độ thêm mới

    // ────────────────────────────────────────────────────────
    public ClassSchedulePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(22, 28, 22, 28));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        loadClasses();
        loadRooms();
    }

    // ═══════════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Sắp lịch học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Quản lý các buổi học trong tuần cho từng lớp");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);
        titles.add(title); titles.add(sub);

        ActionButton btnRefresh = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnRefresh.setPreferredSize(new Dimension(110, 40));
        btnRefresh.addActionListener(e -> { loadClasses(); loadRooms(); });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(btnRefresh);

        p.add(titles,  BorderLayout.WEST);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    // ═══════════════════════════════════════════════
    // MAIN CONTENT – SPLIT
    // ═══════════════════════════════════════════════

    private JPanel buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildClassListCard(), buildSchedulePanel());
        split.setResizeWeight(0.38);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_PAGE);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    // ── LEFT: danh sách lớp ──

    private JPanel buildClassListCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Title + search
        JLabel title = new JLabel("Danh sách lớp học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);

        txtClassSearch = new JTextField();
        txtClassSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtClassSearch.setPreferredSize(new Dimension(0, 34));
        txtClassSearch.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 10, 4, 10)));
        txtClassSearch.setToolTipText("Tìm theo tên lớp hoặc môn học...");
        txtClassSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterClasses(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterClasses(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(txtClassSearch, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        classModel = new DefaultTableModel(
                new String[]{"ID", "Tên lớp", "Môn học", "Số buổi"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblClasses = new JTable(classModel);
        configureTable(tblClasses);
        tblClasses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClasses.getColumnModel().getColumn(3).setCellRenderer(new CountRenderer());
        int[] cw = {44, 160, 110, 54};
        for (int i = 0; i < cw.length; i++) tblClasses.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        classSorter = new TableRowSorter<>(classModel);
        tblClasses.setRowSorter(classSorter);
        tblClasses.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClasses.getSelectedRow() >= 0) onClassSelected();
        });

        JScrollPane sp = new JScrollPane(tblClasses);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── RIGHT: lịch học + form ──

    private JPanel buildSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(buildScheduleCard(), BorderLayout.CENTER);
        panel.add(buildFormCard(),     BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildScheduleCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Title bar
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JPanel titleArea = new JPanel(new GridLayout(2, 1, 0, 2));
        titleArea.setOpaque(false);
        lblSelectedClass = new JLabel("← Chọn một lớp ở bên trái");
        lblSelectedClass.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSelectedClass.setForeground(TEXT_MUTE);
        lblSchedCount = new JLabel("");
        lblSchedCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSchedCount.setForeground(TEXT_MUTE);
        titleArea.add(lblSelectedClass); titleArea.add(lblSchedCount);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        ActionButton btnNew    = new ActionButton("+ Thêm buổi", TEAL);
        ActionButton btnEdit   = new ActionButton("Sửa",   Color.WHITE, PRIMARY, PRIMARY);
        ActionButton btnDelete = new ActionButton("Xóa",   Color.WHITE, DANGER, DANGER);
        btnNew   .setPreferredSize(new Dimension(118, 34));
        btnEdit  .setPreferredSize(new Dimension( 74, 34));
        btnDelete.setPreferredSize(new Dimension( 64, 34));
        btnNew   .addActionListener(e -> prepareNewSchedule());
        btnEdit  .addActionListener(e -> prepareEditSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnRow.add(btnNew); btnRow.add(btnEdit); btnRow.add(btnDelete);

        top.add(titleArea, BorderLayout.CENTER);
        top.add(btnRow,    BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        schedModel = new DefaultTableModel(
                new String[]{"#", "Thứ", "Giờ bắt đầu", "Giờ kết thúc", "Phòng học"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSchedules = new JTable(schedModel);
        configureTable(tblSchedules);
        tblSchedules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSchedules.getColumnModel().getColumn(0).setCellRenderer(new CountRenderer());
        int[] sw = {44, 90, 120, 120, 180};
        for (int i = 0; i < sw.length; i++) tblSchedules.getColumnModel().getColumn(i).setPreferredWidth(sw[i]);

        JScrollPane sp = new JScrollPane(tblSchedules);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFormCard() {
        JPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(0, 130));

        // Form title
        lblFormMode = new JLabel("Thêm buổi học mới");
        lblFormMode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormMode.setForeground(TEXT_MAIN);
        card.add(lblFormMode, BorderLayout.NORTH);

        // Fields row
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        // Day combo
        cmbDay = new JComboBox<>();
        for (int i = 0; i < DAY_LABELS.length; i++)
            cmbDay.addItem(new DayItem(DAY_VALUES[i], DAY_LABELS[i]));
        cmbDay.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbDay.setBackground(Color.WHITE);
        cmbDay.setPreferredSize(new Dimension(120, 36));

        // Time fields
        tfStart = timeField("07:00");
        tfEnd   = timeField("09:00");

        // Room combo
        cmbRoom = new JComboBox<>();
        cmbRoom.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbRoom.setBackground(Color.WHITE);
        cmbRoom.setPreferredSize(new Dimension(200, 36));

        // Labels
        row.add(fieldLabel("Thứ:")); row.add(cmbDay);
        row.add(Box.createHorizontalStrut(4));
        row.add(fieldLabel("Từ:")); row.add(tfStart);
        row.add(fieldLabel("Đến:")); row.add(tfEnd);
        row.add(Box.createHorizontalStrut(4));
        row.add(fieldLabel("Phòng:")); row.add(cmbRoom);

        card.add(row, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnSaveSchedule = new ActionButton("Lưu buổi học", TEAL);
        btnSaveSchedule.setPreferredSize(new Dimension(140, 36));
        btnSaveSchedule.addActionListener(e -> saveSchedule());

        ActionButton btnCancel = new ActionButton("Hủy", Color.WHITE, TEXT_MUTE, BORDER_C);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> clearForm());

        btnRow.add(btnCancel); btnRow.add(btnSaveSchedule);
        card.add(btnRow, BorderLayout.EAST);

        setFormEnabled(false);
        return card;
    }

    // ═══════════════════════════════════════════════
    // LOGIC
    // ═══════════════════════════════════════════════

    private void loadClasses() {
        try {
            List<Map<String, Object>> rows = service.getClassesWithDetails();
            classModel.setRowCount(0);
            for (Map<String, Object> r : rows) {
                classModel.addRow(new Object[]{
                    r.get("class_id"),
                    r.get("class_name"),
                    r.get("subject_name"),
                    0   // placeholder – sẽ update khi chọn lớp
                });
            }
            // Cập nhật số buổi cho mỗi lớp
            for (int i = 0; i < classModel.getRowCount(); i++) {
                int cid = (int) classModel.getValueAt(i, 0);
                try {
                    int cnt = service.getSchedulesByClass(cid).size();
                    classModel.setValueAt(cnt, i, 3);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) { showError("Lỗi tải danh sách lớp: " + e.getMessage()); }
    }

    private void loadRooms() {
        try {
            List<Room> rooms = roomDAO.findAllActive();
            cmbRoom.removeAllItems();
            cmbRoom.addItem(new RoomItem(0, "-- Chọn phòng --"));
            for (Room r : rooms)
                cmbRoom.addItem(new RoomItem(r.getRoomId(), r.getRoomName()));
        } catch (Exception e) { showError("Lỗi tải danh sách phòng: " + e.getMessage()); }
    }

    private void filterClasses() {
        String kw = txtClassSearch.getText().trim();
        classSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }

    private void onClassSelected() {
        int vr = tblClasses.getSelectedRow();
        if (vr < 0) return;
        int mr = tblClasses.convertRowIndexToModel(vr);
        selectedClassId = (int) classModel.getValueAt(mr, 0);
        String className = classModel.getValueAt(mr, 1).toString();
        lblSelectedClass.setText("Lịch học: " + className);
        lblSelectedClass.setForeground(TEXT_MAIN);
        refreshSchedules();
        clearForm();
        setFormEnabled(true);
    }

    private void refreshSchedules() {
        if (selectedClassId < 0) return;
        try {
            List<Map<String, Object>> list = service.getSchedulesByClass(selectedClassId);
            schedModel.setRowCount(0);
            for (Map<String, Object> r : list) {
                int dow = (int) r.get("day_of_week");
                schedModel.addRow(new Object[]{
                    r.get("schedule_id"),
                    dayLabel(dow),
                    r.get("start_time"),
                    r.get("end_time"),
                    r.get("room_name")
                });
            }
            lblSchedCount.setText(list.size() + " buổi/tuần");
            // Cập nhật cột số buổi trong bảng lớp
            for (int i = 0; i < classModel.getRowCount(); i++) {
                if ((int) classModel.getValueAt(i, 0) == selectedClassId) {
                    classModel.setValueAt(list.size(), i, 3);
                    break;
                }
            }
        } catch (Exception e) { showError("Lỗi tải lịch học: " + e.getMessage()); }
    }

    private void prepareNewSchedule() {
        if (selectedClassId < 0) { showWarn("Vui lòng chọn lớp học trước."); return; }
        selectedScheduleId = -1;
        lblFormMode.setText("Thêm buổi học mới");
        btnSaveSchedule.setText("Lưu buổi học");
        cmbDay.setSelectedIndex(0);
        tfStart.setText("07:00");
        tfEnd.setText("09:00");
        cmbRoom.setSelectedIndex(0);
        tfStart.requestFocusInWindow();
    }

    private void prepareEditSchedule() {
        int vr = tblSchedules.getSelectedRow();
        if (vr < 0) { showWarn("Vui lòng chọn một buổi học trong bảng để sửa."); return; }
        int mr = tblSchedules.convertRowIndexToModel(vr);

        selectedScheduleId = (int) schedModel.getValueAt(mr, 0);
        String dayStr  = schedModel.getValueAt(mr, 1).toString();
        String start   = schedModel.getValueAt(mr, 2).toString();
        String end     = schedModel.getValueAt(mr, 3).toString();
        String roomNm  = schedModel.getValueAt(mr, 4).toString();

        lblFormMode.setText("Sửa buổi học – ID " + selectedScheduleId);
        btnSaveSchedule.setText("Cập nhật");

        // Chọn đúng thứ
        for (int i = 0; i < cmbDay.getItemCount(); i++) {
            if (cmbDay.getItemAt(i).label.equals(dayStr)) { cmbDay.setSelectedIndex(i); break; }
        }
        tfStart.setText(start);
        tfEnd.setText(end);
        // Chọn đúng phòng
        for (int i = 0; i < cmbRoom.getItemCount(); i++) {
            if (cmbRoom.getItemAt(i).name.equals(roomNm)) { cmbRoom.setSelectedIndex(i); break; }
        }
    }

    private void saveSchedule() {
        if (selectedClassId < 0) { showWarn("Chưa chọn lớp học."); return; }
        RoomItem room = (RoomItem) cmbRoom.getSelectedItem();
        if (room == null || room.id == 0) { showWarn("Vui lòng chọn phòng học!"); return; }

        DayItem day   = (DayItem) cmbDay.getSelectedItem();
        String  start = tfStart.getText().trim();
        String  end   = tfEnd.getText().trim();

        try {
            if (selectedScheduleId < 0) {
                service.addSchedule(selectedClassId, room.id, day.value, start, end);
                showInfo("Đã thêm buổi học " + day.label + " " + start + "–" + end + " thành công!");
            } else {
                service.updateSchedule(selectedScheduleId, room.id, day.value, start, end);
                showInfo("Đã cập nhật buổi học thành công!");
            }
            refreshSchedules();
            clearForm();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void deleteSchedule() {
        int vr = tblSchedules.getSelectedRow();
        if (vr < 0) { showWarn("Vui lòng chọn một buổi học trong bảng để xóa."); return; }
        int mr  = tblSchedules.convertRowIndexToModel(vr);
        int sid = (int) schedModel.getValueAt(mr, 0);
        String info = schedModel.getValueAt(mr, 1) + " " +
                      schedModel.getValueAt(mr, 2) + "–" +
                      schedModel.getValueAt(mr, 3) + " (" + schedModel.getValueAt(mr, 4) + ")";

        if (JOptionPane.showConfirmDialog(this,
                "Xóa buổi học:\n  " + info + "\n\nBuổi học sẽ không còn hiển thị.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            try {
                service.deleteSchedule(sid);
                refreshSchedules();
                clearForm();
                showInfo("Đã xóa buổi học thành công!");
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void clearForm() {
        selectedScheduleId = -1;
        lblFormMode.setText("Thêm buổi học mới");
        btnSaveSchedule.setText("Lưu buổi học");
        cmbDay.setSelectedIndex(0);
        tfStart.setText("07:00");
        tfEnd.setText("09:00");
        if (cmbRoom.getItemCount() > 0) cmbRoom.setSelectedIndex(0);
        tblSchedules.clearSelection();
    }

    private void setFormEnabled(boolean on) {
        cmbDay.setEnabled(on); tfStart.setEnabled(on); tfEnd.setEnabled(on);
        cmbRoom.setEnabled(on); btnSaveSchedule.setEnabled(on);
        Color bg = on ? Color.WHITE : new Color(248, 250, 252);
        tfStart.setBackground(bg); tfEnd.setBackground(bg);
    }

    // ═══════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════

    private String dayLabel(int dow) {
        for (int i = 0; i < DAY_VALUES.length; i++)
            if (DAY_VALUES[i] == dow) return DAY_LABELS[i];
        return String.valueOf(dow);
    }

    private JTextField timeField(String def) {
        JTextField f = new JTextField(def, 6);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setPreferredSize(new Dimension(80, 36));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 8, 4, 8)));
        f.setToolTipText("Định dạng HH:mm, ví dụ: 07:30");
        return f;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_MUTE);
        return l;
    }

    private void configureTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_MAIN);
        t.setShowVerticalLines(true); t.setShowHorizontalLines(true);
        t.setGridColor(BORDER_C);
        t.setSelectionBackground(PRIMARY_SOFT); t.setSelectionForeground(PRIMARY_DARK);
        t.setFillsViewportHeight(true); t.setAutoCreateRowSorter(false);
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(241, 245, 249)); h.setForeground(new Color(71, 85, 105));
        h.setPreferredSize(new Dimension(0, 36)); h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new ZebraRenderer());
    }

    private void showInfo (String m) { JOptionPane.showMessageDialog(this, m, "Thông báo",   JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn (String m) { JOptionPane.showMessageDialog(this, m, "Cảnh báo",    JOptionPane.WARNING_MESSAGE);     }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Lỗi",         JOptionPane.ERROR_MESSAGE);       }

    // ═══════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════

    private static class DayItem {
        final int value; final String label;
        DayItem(int v, String l) { value = v; label = l; }
        @Override public String toString() { return label; }
    }

    private static class RoomItem {
        final int id; final String name;
        RoomItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return id == 0 ? name : name; }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(v == null ? "" : v.toString());
            if (sel) { setBackground(PRIMARY_SOFT); setForeground(PRIMARY_DARK); }
            else     { setBackground(r % 2 == 0 ? Color.WHITE : new Color(248,250,252)); setForeground(TEXT_MAIN); }
            setFont(c == 0 ? new Font("Segoe UI", Font.BOLD, 13) : new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    private static class CountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 8, 0, 8));
            if (!sel) {
                int cnt = v == null ? 0 : Integer.parseInt(v.toString());
                setBackground(cnt > 0 ? new Color(240, 253, 244) : new Color(248,250,252));
                setForeground(cnt > 0 ? new Color(22, 163, 74) : TEXT_MUTE);
            }
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
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setMargin(new Insets(8, 14, 8, 14));
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true;  repaint(); }
                public void mouseExited (java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(!isEnabled() ? new Color(226,232,240) : hover ? bg.darker() : bg);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            g2.setColor(!isEnabled() ? BORDER_C : border);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
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
            g2.setColor(BORDER_C); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.dispose(); super.paintComponent(g);
        }
    }
}
