package com.mycompany.myapp.view.screens.QuanLyHeThong;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PromotionRule;
import com.mycompany.myapp.model.Room;
import com.mycompany.myapp.repository.PromotionDAO;
import com.mycompany.myapp.repository.RoomDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

public class SystemConfigUI extends JPanel {

    // ── Quy tắc validate dùng chung ──
    private static final int MIN_ROOM_CAPACITY = 1;
    private static final int MAX_ROOM_CAPACITY = 500;
    private static final int MIN_ROOM_FLOOR = 1;
    private static final int MAX_ROOM_FLOOR = 30;
    private static final int MIN_DISCOUNT_RATE = 0;
    private static final int MAX_DISCOUNT_RATE = 100;
    private static final int MIN_SUBJECTS = 1;
    private static final int MAX_SUBJECTS = 20;
    private static final String PROMO_FIX_VERSION = "PROMO_FIX_TEXTFIELD_VALIDATE_0_100";

    // ── DAO ──
    private final RoomDAO roomDAO = new RoomDAO();
    private final PromotionDAO promoDAO = new PromotionDAO();

    // ── Tab 1: Room ──
    private DefaultTableModel roomModel;
    private JTable roomTable;
    private TableRowSorter<DefaultTableModel> roomSorter;
    private JTextField txtRoomSearch;

    // ── Tab 2: Promotion ──
    private DefaultTableModel promoModel;
    private JTable promoTable;

    // ── Status labels ──
    private JLabel lblRoomStatus;
    private JLabel lblPromoStatus;

    public SystemConfigUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(new Color(45, 52, 54));
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabs.addTab("  Quản Lý Phòng Học  ", createRoomPanel());
        tabs.addTab("  Cấu Hình Khuyến Mãi  ", createPromotionPanel());
        tabs.addTab("  Quản Lý Nhân Sự  ", new PersonnelManagementPanel());
        tabs.addTab("  Sao Lưu & Phục Hồi  ", createBackupPanel());

        add(tabs, BorderLayout.CENTER);

        loadRoomData();
        loadPromoData();
    }

    // ════════════════════════════════════════════════════════
    // TAB 1 – QUẢN LÝ PHÒNG HỌC
    // ════════════════════════════════════════════════════════
    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh Mục Cơ Sở Vật Chất (Phòng Học)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(45, 52, 54));

        lblRoomStatus = new JLabel("Sẵn sàng");
        lblRoomStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRoomStatus.setForeground(Color.GRAY);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblRoomStatus, BorderLayout.EAST);

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        txtRoomSearch = new JTextField(22);
        txtRoomSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtRoomSearch.setPreferredSize(new Dimension(260, 38));
        txtRoomSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(0, 10, 0, 10)));
        txtRoomSearch.setToolTipText("Tìm theo tên phòng, tầng, loại...");

        RoundedButton btnSearch = new RoundedButton("Tìm", "#F1F2F6", "#2D3436", 12);
        btnSearch.addActionListener(e -> filterRoomTable());

        txtRoomSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterRoomTable(); }
            public void insertUpdate(DocumentEvent e) { filterRoomTable(); }
            public void removeUpdate(DocumentEvent e) { filterRoomTable(); }
        });

        searchPanel.add(txtRoomSearch);
        searchPanel.add(Box.createHorizontalStrut(6));
        searchPanel.add(btnSearch);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);
        RoundedButton btnAdd = new RoundedButton("Thêm Phòng", "#3498DB", "#FFFFFF", 12);
        RoundedButton btnEdit = new RoundedButton("Sửa", "#F39C12", "#FFFFFF", 12);
        RoundedButton btnDelete = new RoundedButton("Xóa", "#E74C3C", "#FFFFFF", 12);
        btnGroup.add(btnAdd);
        btnGroup.add(btnEdit);
        btnGroup.add(btnDelete);

        actionPanel.add(searchPanel, BorderLayout.WEST);
        actionPanel.add(btnGroup, BorderLayout.EAST);

        headerPanel.add(titleRow, BorderLayout.NORTH);
        headerPanel.add(actionPanel, BorderLayout.CENTER);

        String[] cols = {"ID", "Tên Phòng", "Sức Chứa", "Tầng", "Loại Phòng", "Trạng Thái"};
        roomModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomModel);
        styleModernTable(roomTable);
        roomTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        int[] rw = {50, 220, 90, 60, 160, 120};
        for (int i = 0; i < rw.length; i++) {
            roomTable.getColumnModel().getColumn(i).setPreferredWidth(rw[i]);
        }

        roomSorter = new TableRowSorter<>(roomModel);
        roomTable.setRowSorter(roomSorter);

        JScrollPane sp = new JScrollPane(roomTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showRoomDialog(null));

        btnEdit.addActionListener(e -> {
            int row = roomTable.getSelectedRow();
            if (row < 0) {
                showWarn("Vui lòng chọn phòng cần sửa.");
                return;
            }
            int modelRow = roomTable.convertRowIndexToModel(row);
            int id = (int) roomModel.getValueAt(modelRow, 0);
            try {
                Room r = roomDAO.findById(id);
                if (r != null) {
                    showRoomDialog(r);
                } else {
                    showWarn("Phòng học không còn tồn tại hoặc đã bị xóa.");
                    loadRoomData();
                }
            } catch (Exception ex) {
                showError(normalizeErrorMessage(ex));
            }
        });

        btnDelete.addActionListener(e -> deleteRoom());

        return panel;
    }

    private void loadRoomData() {
        try {
            List<Room> list = roomDAO.findAllActive();
            roomModel.setRowCount(0);
            for (Room r : list) {
                roomModel.addRow(new Object[]{
                        r.getRoomId(),
                        safeText(r.getRoomName()),
                        r.getCapacity(),
                        "Tầng " + r.getFloor(),
                        safeText(r.getRoomType()),
                        "Sẵn sàng"
                });
            }
            setStatus(lblRoomStatus, "Đã tải – " + list.size() + " phòng", new Color(22, 163, 74));
        } catch (Exception ex) {
            setStatus(lblRoomStatus, "Lỗi tải dữ liệu", Color.RED);
            showError("Không tải được danh sách phòng:\n" + normalizeErrorMessage(ex));
        }
    }

    private void filterRoomTable() {
        if (roomSorter == null) return;
        String kw = txtRoomSearch == null ? "" : txtRoomSearch.getText().trim();
        roomSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }

    private void showRoomDialog(Room existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa thông tin phòng học" : "Thêm phòng học mới",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 20, 28));
        content.setBackground(Color.WHITE);

        JTextField txtName = dlgField(content, "Tên phòng học (*)");
        JSpinner spnCapacity = dlgSpinner(content, "Sức chứa (người) (*)", MIN_ROOM_CAPACITY, MAX_ROOM_CAPACITY, 1);
        JSpinner spnFloor = dlgSpinner(content, "Tầng (*)", MIN_ROOM_FLOOR, MAX_ROOM_FLOOR, 1);
        String[] roomTypes = {"Thực hành", "Tiêu chuẩn", "Hội trường", "Phòng họp", "Khác"};
        JComboBox<String> cmbType = dlgCombo(content, "Loại phòng (*)", roomTypes);

        if (isEdit) {
            txtName.setText(safeText(existing.getRoomName()));
            spnCapacity.setValue(clamp(existing.getCapacity(), MIN_ROOM_CAPACITY, MAX_ROOM_CAPACITY));
            spnFloor.setValue(clamp(existing.getFloor(), MIN_ROOM_FLOOR, MAX_ROOM_FLOOR));
            boolean found = false;
            for (String roomType : roomTypes) {
                if (roomType.equals(existing.getRoomType())) {
                    cmbType.setSelectedItem(roomType);
                    found = true;
                    break;
                }
            }
            if (!found && existing.getRoomType() != null && !existing.getRoomType().trim().isEmpty()) {
                cmbType.addItem(existing.getRoomType());
                cmbType.setSelectedItem(existing.getRoomType());
            }
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        RoundedButton btnCancel = new RoundedButton("Hủy", "#F1F2F6", "#2D3436", 10);
        RoundedButton btnSave = new RoundedButton("Lưu", "#2ECC71", "#FFFFFF", 10);
        btnSave.setPreferredSize(new Dimension(90, 36));
        btnCancel.setPreferredSize(new Dimension(70, 36));
        footer.add(btnCancel);
        footer.add(btnSave);
        content.add(footer);

        dialog.add(content);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                showWarn("Tên phòng không được để trống!");
                txtName.requestFocus();
                return;
            }

            if (name.length() > 100) {
                showWarn("Tên phòng không được vượt quá 100 ký tự!");
                txtName.requestFocus();
                return;
            }

            Integer capacity = readSpinnerInt(spnCapacity, "Sức chứa", MIN_ROOM_CAPACITY, MAX_ROOM_CAPACITY);
            if (capacity == null) return;

            Integer floor = readSpinnerInt(spnFloor, "Tầng", MIN_ROOM_FLOOR, MAX_ROOM_FLOOR);
            if (floor == null) return;

            Object typeValue = cmbType.getSelectedItem();
            if (typeValue == null || typeValue.toString().trim().isEmpty()) {
                showWarn("Loại phòng không được để trống!");
                return;
            }

            Room r = isEdit ? existing : new Room();
            r.setRoomName(name);
            r.setCapacity(capacity);
            r.setFloor(floor);
            r.setRoomType(typeValue.toString().trim());

            try {
                if (isEdit) {
                    roomDAO.update(r);
                    DBConnection.commitTransaction();
                    showInfo("Cập nhật phòng học thành công!");
                } else {
                    roomDAO.insert(r);
                    DBConnection.commitTransaction();
                    showInfo("Thêm phòng học mới thành công!");
                }
                dialog.dispose();
                loadRoomData();
            } catch (Exception ex) {
                DBConnection.rollbackTransaction();
                if (isUniqueConstraintError(ex)) {
                    showError("Tên phòng này đã tồn tại trong hệ thống.\nVui lòng chọn tên khác!");
                } else {
                    showError("Lỗi khi lưu phòng học:\n" + normalizeErrorMessage(ex));
                }
            }
        });

        dialog.setVisible(true);
    }

    private void deleteRoom() {
        int row = roomTable.getSelectedRow();
        if (row < 0) {
            showWarn("Vui lòng chọn phòng cần xóa.");
            return;
        }
        int modelRow = roomTable.convertRowIndexToModel(row);
        int id = (int) roomModel.getValueAt(modelRow, 0);
        String name = String.valueOf(roomModel.getValueAt(modelRow, 1));

        int ok = JOptionPane.showConfirmDialog(this,
                "Xóa phòng \"" + name + "\"?\n(Phòng sẽ không còn xuất hiện trong lịch học mới)",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                roomDAO.softDelete(id);
                DBConnection.commitTransaction();
                showInfo("Đã xóa phòng \"" + name + "\"!");
                loadRoomData();
            } catch (Exception ex) {
                DBConnection.rollbackTransaction();
                showError("Lỗi khi xóa phòng:\n" + normalizeErrorMessage(ex));
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // TAB 2 – CẤU HÌNH KHUYẾN MÃI
    // ════════════════════════════════════════════════════════
    private JPanel createPromotionPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cấu Hình Các Chương Trình Khuyến Mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(45, 52, 54));

        lblPromoStatus = new JLabel("Sẵn sàng");
        lblPromoStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPromoStatus.setForeground(Color.GRAY);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblPromoStatus, BorderLayout.EAST);

        JLabel lblDesc = new JLabel(
                "<html><span style='color:#636e72;font-size:12px'>" +
                        "Chương trình giảm học phí sẽ được áp dụng khi tạo hóa đơn cho học viên." +
                        "</span></html>");

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        RoundedButton btnAdd = new RoundedButton("Tạo CT Khuyến Mãi", "#2ECC71", "#FFFFFF", 12);
        RoundedButton btnEdit = new RoundedButton("Chỉnh sửa", "#F39C12", "#FFFFFF", 12);
        RoundedButton btnDelete = new RoundedButton("Xóa / Vô hiệu", "#E74C3C", "#FFFFFF", 12);
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);

        headerPanel.add(titleRow, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.CENTER);
        headerPanel.add(actionPanel, BorderLayout.SOUTH);

        String[] cols = {"Mã KM", "Tên Chương Trình", "Tỷ lệ giảm (%)", "Số môn tối thiểu"};
        promoModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        promoTable = new JTable(promoModel);
        styleModernTable(promoTable);
        promoTable.getColumnModel().getColumn(2).setCellRenderer(new DiscountRenderer());
        int[] pw = {70, 320, 140, 160};
        for (int i = 0; i < pw.length; i++) {
            promoTable.getColumnModel().getColumn(i).setPreferredWidth(pw[i]);
        }

        JScrollPane sp = new JScrollPane(promoTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showPromoDialog(null));

        btnEdit.addActionListener(e -> {
            int row = promoTable.getSelectedRow();
            if (row < 0) {
                showWarn("Vui lòng chọn chương trình cần sửa.");
                return;
            }
            int modelRow = promoTable.convertRowIndexToModel(row);
            int id = (int) promoModel.getValueAt(modelRow, 0);
            try {
                PromotionRule p = promoDAO.findById(id);
                if (p != null) {
                    showPromoDialog(p);
                } else {
                    showWarn("Chương trình khuyến mãi không còn tồn tại hoặc đã bị vô hiệu hóa.");
                    loadPromoData();
                }
            } catch (Exception ex) {
                showError(normalizeErrorMessage(ex));
            }
        });

        btnDelete.addActionListener(e -> deletePromo());

        return panel;
    }

    private void loadPromoData() {
        try {
            List<PromotionRule> list = promoDAO.findAllActive();
            promoModel.setRowCount(0);
            for (PromotionRule p : list) {
                int discount = (int) p.getDiscountRate();
                promoModel.addRow(new Object[]{
                        p.getPromoId(),
                        safeText(p.getPromoName()),
                        discount + "%",
                        p.getMinSubjects() + " môn"
                });
            }
            setStatus(lblPromoStatus, "Đã tải – " + list.size() + " chương trình | " + PROMO_FIX_VERSION, new Color(22, 163, 74));
        } catch (Exception ex) {
            setStatus(lblPromoStatus, "Lỗi tải dữ liệu", Color.RED);
            showError("Không tải được danh sách khuyến mãi:\n" + normalizeErrorMessage(ex));
        }
    }

    private void showPromoDialog(PromotionRule existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Chỉnh sửa chương trình khuyến mãi" : "Tạo chương trình khuyến mãi mới",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 20, 28));
        content.setBackground(Color.WHITE);

        JTextField txtName = dlgField(content, "Tên chương trình khuyến mãi (*)");
        JTextField txtDiscount = dlgField(content, "Tỷ lệ giảm (%)  [0 – 100] (*)");
        JSpinner spnMinSub = dlgSpinner(content, "Số môn đăng ký tối thiểu (*)", MIN_SUBJECTS, MAX_SUBJECTS, 1);

        if (isEdit) {
            txtName.setText(safeText(existing.getPromoName()));
            int currentDiscount = (int) existing.getDiscountRate();
            txtDiscount.setText(String.valueOf(currentDiscount));
            spnMinSub.setValue(clamp(existing.getMinSubjects(), MIN_SUBJECTS, MAX_SUBJECTS));
        }

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));
        RoundedButton btnCancel = new RoundedButton("Hủy", "#F1F2F6", "#2D3436", 10);
        RoundedButton btnSave = new RoundedButton("Lưu", "#2ECC71", "#FFFFFF", 10);
        btnSave.setPreferredSize(new Dimension(90, 36));
        btnCancel.setPreferredSize(new Dimension(70, 36));
        footer.add(btnCancel);
        footer.add(btnSave);
        content.add(footer);

        dialog.add(content);

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                showWarn("Tên chương trình không được để trống!");
                txtName.requestFocus();
                return;
            }

            if (name.length() > 150) {
                showWarn("Tên chương trình khuyến mãi không được vượt quá 150 ký tự!");
                txtName.requestFocus();
                return;
            }

            Integer discount = readTextFieldInt(txtDiscount, "Tỷ lệ khuyến mãi", MIN_DISCOUNT_RATE, MAX_DISCOUNT_RATE);
            if (discount == null) return;

            Integer minSubjects = readSpinnerInt(spnMinSub, "Số môn đăng ký tối thiểu", MIN_SUBJECTS, MAX_SUBJECTS);
            if (minSubjects == null) return;

            PromotionRule p = isEdit ? existing : new PromotionRule();
            p.setPromoName(name);
            p.setDiscountRate(discount);
            p.setMinSubjects(minSubjects);

            try {
                if (isEdit) {
                    promoDAO.update(p);
                    DBConnection.commitTransaction();
                    showInfo("Cập nhật chương trình khuyến mãi thành công!");
                } else {
                    promoDAO.insert(p);
                    DBConnection.commitTransaction();
                    showInfo("Đã tạo chương trình khuyến mãi mới!");
                }
                dialog.dispose();
                loadPromoData();
            } catch (Exception ex) {
                DBConnection.rollbackTransaction();
                showError(getFriendlyErrorMessage(ex));
            }
        });

        dialog.setVisible(true);
    }

    private void deletePromo() {
        int row = promoTable.getSelectedRow();
        if (row < 0) {
            showWarn("Vui lòng chọn chương trình cần xóa.");
            return;
        }
        int modelRow = promoTable.convertRowIndexToModel(row);
        int id = (int) promoModel.getValueAt(modelRow, 0);
        String name = String.valueOf(promoModel.getValueAt(modelRow, 1));

        int ok = JOptionPane.showConfirmDialog(this,
                "Vô hiệu hóa chương trình \"" + name + "\"?\n" +
                        "(Chương trình sẽ không còn xuất hiện khi lập hóa đơn)",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                promoDAO.softDelete(id);
                DBConnection.commitTransaction();
                showInfo("Đã vô hiệu hóa \"" + name + "\"!");
                loadPromoData();
            } catch (Exception ex) {
                DBConnection.rollbackTransaction();
                showError("Lỗi khi xóa chương trình khuyến mãi:\n" + normalizeErrorMessage(ex));
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // TAB 3 – SAO LƯU & PHỤC HỒI
    // ════════════════════════════════════════════════════════
    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.decode("#F8F9F9"));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(50, 60, 50, 60)));

        JLabel iconLabel = new JLabel("🗄️", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 65));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("An Toàn Dữ Liệu Hệ Thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(45, 52, 54));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel(
                "<html><div style='text-align:center;'>Dữ liệu là tài sản quan trọng nhất của trung tâm.<br>" +
                        "Vui lòng thực hiện sao lưu (Backup) thường xuyên để tránh rủi ro mất mát.</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDesc.setForeground(Color.GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        RoundedButton btnBackup = new RoundedButton("Tạo Bản Sao Lưu (.SQL)", "#8B5CF6", "#FFFFFF", 20);
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBackup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackup.setPreferredSize(new Dimension(260, 46));

        RoundedButton btnRestore = new RoundedButton("Phục Hồi Dữ Liệu", "#FFFFFF", "#E67E22", "#E67E22", 20);
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRestore.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRestore.setPreferredSize(new Dimension(260, 46));

        btnBackup.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Đang tiến hành xuất file SQL Backup...", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE));

        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(lblDesc);
        card.add(Box.createRigidArea(new Dimension(0, 36)));
        card.add(btnBackup);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnRestore);

        panel.add(card);
        return panel;
    }

    // ════════════════════════════════════════════════════════
    // DIALOG HELPER BUILDERS
    // ════════════════════════════════════════════════════════
    private JTextField dlgField(JPanel p, String label) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));

        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(4, 10, 4, 10)));
        p.add(f);
        p.add(Box.createVerticalStrut(14));
        return f;
    }

    private JSpinner dlgSpinner(JPanel p, String label, int min, int max, int step) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));

        JSpinner sp = new JSpinner(new SpinnerNumberModel(min, min, max, step));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComponent editor = sp.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField field = ((JSpinner.DefaultEditor) editor).getTextField();
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(4, 10, 4, 10)));
        }

        p.add(sp);
        p.add(Box.createVerticalStrut(14));
        return sp;
    }

    private JComboBox<String> dlgCombo(JPanel p, String label, String[] items) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(100, 116, 139));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));

        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
        p.add(Box.createVerticalStrut(14));
        return c;
    }

    // ════════════════════════════════════════════════════════
    // TABLE STYLING
    // ════════════════════════════════════════════════════════
    private void styleModernTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(Color.decode("#D4D4D4"));
        table.setSelectionBackground(Color.decode("#E8F0FE"));
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.decode("#F1F2F6"));
        header.setForeground(new Color(45, 52, 54));
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createLineBorder(Color.decode("#D4D4D4")));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════
    private Integer readSpinnerInt(JSpinner spinner, String fieldName, int min, int max) {
        try {
            spinner.commitEdit();
            int value = ((Number) spinner.getValue()).intValue();
            if (value < min || value > max) {
                if ("Tỷ lệ khuyến mãi".equals(fieldName)) {
                    showWarn("Tỷ lệ khuyến mãi phải nằm trong khoảng từ 0% đến 100%!");
                } else {
                    showWarn(fieldName + " phải nằm trong khoảng từ " + min + " đến " + max + "!");
                }
                spinner.requestFocus();
                return null;
            }
            return value;
        } catch (ParseException | ClassCastException ex) {
            if ("Tỷ lệ khuyến mãi".equals(fieldName)) {
                showWarn("Tỷ lệ khuyến mãi phải là số nguyên từ 0% đến 100%!");
            } else {
                showWarn(fieldName + " phải là số nguyên hợp lệ!");
            }
            spinner.requestFocus();
            return null;
        }
    }

    private Integer readTextFieldInt(JTextField field, String fieldName, int min, int max) {
        String raw = field.getText().trim();

        if (raw.isEmpty()) {
            showWarn(fieldName + " không được để trống!");
            field.requestFocus();
            return null;
        }

        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            if ("Tỷ lệ khuyến mãi".equals(fieldName)) {
                showWarn("Tỷ lệ khuyến mãi phải là số nguyên từ 0% đến 100%!");
            } else {
                showWarn(fieldName + " phải là số nguyên hợp lệ!");
            }
            field.requestFocus();
            field.selectAll();
            return null;
        }

        if (value < min || value > max) {
            if ("Tỷ lệ khuyến mãi".equals(fieldName)) {
                showWarn("Tỷ lệ khuyến mãi phải nằm trong khoảng từ 0% đến 100%!");
            } else {
                showWarn(fieldName + " phải nằm trong khoảng từ " + min + " đến " + max + "!");
            }
            field.requestFocus();
            field.selectAll();
            return null;
        }

        return value;
    }

    private String getFriendlyErrorMessage(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            return "Đã xảy ra lỗi không xác định. Vui lòng thử lại.";
        }

        if (msg.contains("ORA-00001")) {
            return "Tên chương trình khuyến mãi đã tồn tại.\nVui lòng nhập tên khác.";
        }

        if (msg.contains("ORA-02289")) {
            return "Hệ thống chưa cấu hình mã tự động cho chương trình khuyến mãi.\n"
                 + "Vui lòng kiểm tra lại PromotionDAO hoặc cấu hình database.";
        }

        if (msg.contains("ORA-02290")) {
            return "Dữ liệu khuyến mãi không hợp lệ.\n"
                 + "Tỷ lệ khuyến mãi phải nằm trong khoảng từ 0% đến 100%.";
        }

        if (msg.contains("ORA-01400")) {
            return "Thiếu thông tin bắt buộc.\nVui lòng kiểm tra lại các ô nhập liệu.";
        }

        if (msg.contains("ORA-12899")) {
            return "Dữ liệu nhập quá dài.\nVui lòng rút ngắn nội dung.";
        }

        if (msg.contains("ORA-00054")) {
            return "Dữ liệu đang được người dùng khác xử lý.\nVui lòng thử lại sau vài giây.";
        }

        if (msg.contains("Tỷ lệ khuyến mãi") || msg.contains("Tên chương trình") || msg.contains("Số môn")) {
            return msg;
        }

        return "Không thể lưu chương trình khuyến mãi.\n"
             + "Vui lòng kiểm tra lại dữ liệu hoặc thử lại sau.";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isUniqueConstraintError(Exception ex) {
        String msg = ex.getMessage();
        return msg != null && msg.contains("ORA-00001");
    }

    private String normalizeErrorMessage(Exception ex) {
        String msg = ex.getMessage();
        return (msg == null || msg.trim().isEmpty()) ? ex.getClass().getSimpleName() : msg;
    }

    private void setStatus(JLabel lbl, String msg, Color color) {
        if (lbl == null) return;
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
    // INNER RENDERERS
    // ════════════════════════════════════════════════════════
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int r, int c) {
            String text = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
            if (sel) {
                lbl.setBackground(Color.decode("#E8F0FE"));
                lbl.setForeground(Color.BLACK);
            } else {
                lbl.setBackground(new Color(220, 252, 231));
                lbl.setForeground(new Color(22, 101, 52));
            }
            return lbl;
        }
    }

    private static class DiscountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, f, r, c);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setOpaque(true);

            if (sel) {
                setBackground(t.getSelectionBackground());
                setForeground(t.getSelectionForeground());
                return this;
            }

            String raw = v == null ? "0%" : v.toString();
            double rate = 0;
            try {
                rate = Double.parseDouble(raw.replace("%", "").trim());
            } catch (Exception ignored) {
                rate = 0;
            }

            if (rate >= 20) {
                setBackground(new Color(254, 240, 138));
                setForeground(new Color(133, 77, 14));
            } else if (rate >= 10) {
                setBackground(new Color(254, 215, 215));
                setForeground(new Color(185, 28, 28));
            } else {
                setBackground(new Color(239, 246, 255));
                setForeground(new Color(29, 78, 216));
            }
            return this;
        }
    }

    // ════════════════════════════════════════════════════════
    // INNER CLASS: NÚT BO GÓC
    // ════════════════════════════════════════════════════════
    class RoundedButton extends JButton {
        private final Color bgColor;
        private final Color fgColor;
        private final Color borderColor;
        private final int radius;

        RoundedButton(String text, String bgHex, String fgHex, int radius) {
            this(text, bgHex, fgHex, null, radius);
        }

        RoundedButton(String text, String bgHex, String fgHex, String borderHex, int radius) {
            super(text);
            this.bgColor = Color.decode(bgHex);
            this.fgColor = Color.decode(fgHex);
            this.borderColor = borderHex != null ? Color.decode(borderHex) : null;
            this.radius = radius;
            setForeground(this.fgColor);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
