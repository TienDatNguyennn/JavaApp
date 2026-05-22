package com.mycompany.myapp.view.screens.GiaoVuUI;

import com.mycompany.myapp.model.StudyClass;
import com.mycompany.myapp.service.StudyClassService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Panel xếp lớp học viên (CLASS_PLACEMENT).
 * Tách từ ClassManagementPanel – Tab 2.
 */
public class ClassEnrollmentPanel extends JPanel {

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

    private final StudyClassService service = new StudyClassService();

    private JComboBox<ClassItem>              cmbClass;
    private DefaultTableModel                 enrolledModel, availableModel;
    private JTable                            tblEnrolled, tblAvailable;
    private TableRowSorter<DefaultTableModel> availableSorter;
    private JTextField                        txtAvaSearch;
    private JLabel                            lblEnrolledCnt, lblAvailableCnt, lblEnrollStatus;

    public ClassEnrollmentPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Xếp lớp học viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Thêm hoặc rút học viên khỏi các lớp đào tạo");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);
        titleBox.add(title); titleBox.add(sub);
        hdr.add(titleBox, BorderLayout.WEST);
        add(hdr, BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refreshClassCombo();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        // Selector bar
        JPanel bar = new RoundedPanel(14, BG_CARD);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bar.setPreferredSize(new Dimension(0, 54));

        JLabel lbl = new JLabel("Chọn lớp học:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_MAIN);
        cmbClass = new JComboBox<>();
        cmbClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbClass.setBackground(Color.WHITE);
        cmbClass.setPreferredSize(new Dimension(380, 34));
        cmbClass.addActionListener(e -> refreshEnrollment());

        ActionButton btnRfr = new ActionButton("Làm mới", Color.WHITE, PRIMARY, PRIMARY);
        btnRfr.setPreferredSize(new Dimension(110, 34));
        btnRfr.addActionListener(e -> { refreshClassCombo(); refreshEnrollment(); });

        lblEnrollStatus = new JLabel("");
        lblEnrollStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEnrollStatus.setForeground(TEXT_MUTE);

        bar.add(lbl); bar.add(cmbClass); bar.add(btnRfr);
        bar.add(Box.createHorizontalStrut(6)); bar.add(lblEnrollStatus);
        content.add(bar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildEnrolledCard(), buildAvailableCard());
        split.setResizeWeight(0.5);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setBorder(null);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildEnrolledCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 10, 14));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        lblEnrolledCnt = new JLabel("Học viên trong lớp  (0)");
        lblEnrolledCnt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEnrolledCnt.setForeground(TEXT_MAIN);
        ActionButton btnRm = new ActionButton("← Rút khỏi lớp", Color.WHITE, DANGER, DANGER);
        btnRm.setPreferredSize(new Dimension(148, 34));
        btnRm.addActionListener(e -> removeFromClass());
        top.add(lblEnrolledCnt, BorderLayout.CENTER);
        top.add(btnRm, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        enrolledModel = new DefaultTableModel(
            new String[]{"ID", "Họ và tên", "GT", "Số ĐT", "Phụ huynh", "Ngày vào lớp"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEnrolled = new JTable(enrolledModel);
        configureTable(tblEnrolled);
        tblEnrolled.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] ew = {48, 200, 40, 105, 155, 100};
        for (int i = 0; i < ew.length; i++) tblEnrolled.getColumnModel().getColumn(i).setPreferredWidth(ew[i]);

        JScrollPane sp = new JScrollPane(tblEnrolled);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("Giữ Ctrl/Shift để chọn nhiều → bấm Rút khỏi lớp");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12)); hint.setForeground(TEXT_MUTE);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildAvailableCard() {
        JPanel card = new RoundedPanel(16, BG_CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 14, 10, 14));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        lblAvailableCnt = new JLabel("Học viên chưa xếp  (0)");
        lblAvailableCnt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAvailableCnt.setForeground(TEXT_MAIN);
        ActionButton btnAdd = new ActionButton("Thêm vào lớp →", SUCCESS);
        btnAdd.setPreferredSize(new Dimension(155, 34));
        btnAdd.addActionListener(e -> addToClass());
        top.add(lblAvailableCnt, BorderLayout.CENTER);
        top.add(btnAdd, BorderLayout.EAST);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchRow.setOpaque(false);
        JLabel sl = new JLabel("Tìm nhanh:");
        sl.setFont(new Font("Segoe UI", Font.BOLD, 13)); sl.setForeground(TEXT_MAIN);
        txtAvaSearch = styledField("");
        txtAvaSearch.setPreferredSize(new Dimension(270, 32));
        txtAvaSearch.getDocument().addDocumentListener(docListener(() -> {
            String kw = txtAvaSearch.getText().trim();
            availableSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }));
        searchRow.add(sl); searchRow.add(txtAvaSearch);

        JPanel north = new JPanel(new BorderLayout(0, 8)); north.setOpaque(false);
        north.add(top, BorderLayout.NORTH); north.add(searchRow, BorderLayout.SOUTH);
        card.add(north, BorderLayout.NORTH);

        availableModel = new DefaultTableModel(
            new String[]{"ID", "Họ và tên", "GT", "Số ĐT", "Phụ huynh"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAvailable = new JTable(availableModel);
        configureTable(tblAvailable);
        tblAvailable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int[] aw = {48, 200, 40, 105, 180};
        for (int i = 0; i < aw.length; i++) tblAvailable.getColumnModel().getColumn(i).setPreferredWidth(aw[i]);
        availableSorter = new TableRowSorter<>(availableModel);
        tblAvailable.setRowSorter(availableSorter);

        JScrollPane sp = new JScrollPane(tblAvailable);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("Giữ Ctrl/Shift để chọn nhiều → bấm Thêm vào lớp");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12)); hint.setForeground(TEXT_MUTE);
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    // ── Logic ──

    private void refreshClassCombo() {
        try {
            ClassItem prev = (ClassItem) cmbClass.getSelectedItem();
            cmbClass.removeAllItems();
            cmbClass.addItem(new ClassItem(0, "-- Chọn lớp học --"));
            for (StudyClass sc : service.getAllActiveClasses())
                cmbClass.addItem(new ClassItem(sc.getClassId(), sc.getClassName()));
            if (prev != null && prev.id > 0) {
                for (int i = 0; i < cmbClass.getItemCount(); i++) {
                    if (cmbClass.getItemAt(i).id == prev.id) { cmbClass.setSelectedIndex(i); break; }
                }
            }
        } catch (Exception e) { showError("Lỗi tải danh sách lớp: " + e.getMessage()); }
    }

    private void refreshEnrollment() {
        ClassItem sel = (ClassItem) cmbClass.getSelectedItem();
        if (sel == null || sel.id == 0) {
            enrolledModel.setRowCount(0); availableModel.setRowCount(0);
            lblEnrolledCnt.setText("Học viên trong lớp  (0)");
            lblAvailableCnt.setText("Học viên chưa xếp  (0)");
            return;
        }
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try {
            List<Map<String, Object>> list = service.getStudentsInClass(sel.id);
            enrolledModel.setRowCount(0);
            for (Map<String, Object> r : list) {
                String g  = "M".equals(r.get("gender")) ? "Nam" : "Nữ";
                String dt = r.get("enroll_date") != null ? df.format(r.get("enroll_date")) : "";
                enrolledModel.addRow(new Object[]{
                    r.get("student_id"), r.get("full_name"), g, nvl(r.get("phone")), nvl(r.get("parent_name")), dt
                });
            }
            lblEnrolledCnt.setText("Học viên trong lớp  (" + list.size() + ")");
        } catch (Exception e) { showError("Lỗi tải học viên trong lớp: " + e.getMessage()); }

        try {
            List<Map<String, Object>> list = service.getStudentsNotInClass(sel.id);
            availableModel.setRowCount(0);
            for (Map<String, Object> r : list) {
                String g = "M".equals(r.get("gender")) ? "Nam" : "Nữ";
                availableModel.addRow(new Object[]{
                    r.get("student_id"), r.get("full_name"), g, nvl(r.get("phone")), nvl(r.get("parent_name"))
                });
            }
            lblAvailableCnt.setText("Học viên chưa xếp  (" + list.size() + ")");
        } catch (Exception e) { showError("Lỗi tải học viên khả dụng: " + e.getMessage()); }

        txtAvaSearch.setText("");
        availableSorter.setRowFilter(null);
    }

    private void addToClass() {
        int[] rows = tblAvailable.getSelectedRows();
        if (rows.length == 0) { showWarn("Vui lòng chọn ít nhất một học viên."); return; }
        ClassItem cls = (ClassItem) cmbClass.getSelectedItem();
        if (cls == null || cls.id == 0) { showWarn("Vui lòng chọn lớp học trước."); return; }

        List<int[]> toAdd = new ArrayList<>(); List<String> names = new ArrayList<>();
        for (int vr : rows) {
            int mr = tblAvailable.convertRowIndexToModel(vr);
            toAdd.add(new int[]{ (int) availableModel.getValueAt(mr, 0) });
            names.add((String) availableModel.getValueAt(mr, 1));
        }
        int ok = 0; List<String> failed = new ArrayList<>();
        for (int i = 0; i < toAdd.size(); i++) {
            try { service.enrollStudent(toAdd.get(i)[0], cls.id); ok++; }
            catch (Exception e) { failed.add("• " + names.get(i) + ": " + e.getMessage()); }
        }
        if (ok > 0) { setStatus("✓  Đã thêm " + ok + " học viên vào lớp " + cls.name, SUCCESS); refreshEnrollment(); }
        if (!failed.isEmpty()) showWarn("Không thể thêm " + failed.size() + " học viên:\n" + String.join("\n", failed));
    }

    private void removeFromClass() {
        int[] rows = tblEnrolled.getSelectedRows();
        if (rows.length == 0) { showWarn("Vui lòng chọn ít nhất một học viên."); return; }
        ClassItem cls = (ClassItem) cmbClass.getSelectedItem();
        if (cls == null || cls.id == 0) { showWarn("Vui lòng chọn lớp học trước."); return; }

        List<Integer> sids = new ArrayList<>(); List<String> names = new ArrayList<>();
        for (int vr : rows) {
            int mr = tblEnrolled.convertRowIndexToModel(vr);
            sids.add((int) enrolledModel.getValueAt(mr, 0));
            names.add((String) enrolledModel.getValueAt(mr, 1));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(names.size(), 5); i++) sb.append("  • ").append(names.get(i)).append("\n");
        if (names.size() > 5) sb.append("  • ... và ").append(names.size() - 5).append(" học viên khác");

        if (JOptionPane.showConfirmDialog(this,
                "Rút " + names.size() + " học viên khỏi lớp \"" + cls.name + "\"?\n\n" + sb.toString().trim(),
                "Xác nhận rút khỏi lớp", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
            int ok = 0; List<String> failed = new ArrayList<>();
            for (int i = 0; i < sids.size(); i++) {
                try { service.removeStudentFromClass(sids.get(i), cls.id); ok++; }
                catch (Exception e) { failed.add("• " + names.get(i) + ": " + e.getMessage()); }
            }
            setStatus("✗  Đã rút " + ok + " học viên khỏi lớp " + cls.name, WARNING_CLR);
            refreshEnrollment();
            if (!failed.isEmpty()) showWarn("Không thể rút " + failed.size() + " học viên:\n" + String.join("\n", failed));
        }
    }

    // ── Helpers ──

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

    private JTextField styledField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true), new EmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private DocumentListener docListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) {}
        };
    }

    private void setStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> { lblEnrollStatus.setText(msg); lblEnrollStatus.setForeground(color); });
    }

    private String nvl(Object v) { return v == null ? "" : v.toString(); }
    private void showInfo(String m)  { JOptionPane.showMessageDialog(this, m, "Thông báo",   JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn(String m)  { JOptionPane.showMessageDialog(this, m, "Cảnh báo",    JOptionPane.WARNING_MESSAGE);     }
    private void showError(String m) { JOptionPane.showMessageDialog(this, m, "Lỗi",         JOptionPane.ERROR_MESSAGE);       }

    // ── Inner classes ──

    private static class ClassItem {
        final int id; final String name;
        ClassItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return id == 0 ? name : "[" + id + "]  " + name; }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            setBorder(new EmptyBorder(0, 8, 0, 8));
            setToolTipText(v == null ? "" : v.toString());
            if (sel) { setBackground(PRIMARY_SOFT); setForeground(PRIMARY_DARK); }
            else { setBackground(r % 2 == 0 ? Color.WHITE : new Color(248,250,252)); setForeground(TEXT_MAIN); }
            setFont(c == 0 ? new Font("Segoe UI", Font.BOLD, 13) : new Font("Segoe UI", Font.PLAIN, 13));
            setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
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
