package com.mycompany.myapp.view.screens.QuanLyHeThong;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PersonnelDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.repository.PersonnelDAO;
import com.mycompany.myapp.utils.PasswordUtil;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Panel Quản lý Nhân sự (Giáo viên & Nhân viên).
 * Bảng trái: danh sách nhân sự (USERS + ACCOUNT + ROLE).
 * Panel phải: chi tiết hồ sơ tương ứng (TEACHER_PROFILE / OFFICE_STAFF_PROFILE).
 * Hỗ trợ: Thêm – Sửa – Xóa – Khóa/Mở khóa – Đặt lại mật khẩu.
 */
public class PersonnelManagementPanel extends JPanel {

    // ── Design tokens ──────────────────────────────────────────
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
    private static final Color WARNING_C    = new Color(245, 158,  11);
    private static final Color TEACHER_C    = new Color( 59, 130, 246);
    private static final Color STAFF_C      = new Color(168,  85, 247);

    private final PersonnelDAO dao = new PersonnelDAO();
    private final NumberFormat nf  = NumberFormat.getInstance(new Locale("vi", "VN"));

    // ── Table ──────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField        txtSearch;
    private JLabel            lblStatus;

    // ── Detail panel fields ────────────────────────────────────
    private JLabel  lblDetailTitle, lblDetailType;
    // User info
    private JLabel  dUserId, dFullName, dEmail, dPhone, dIdCard;
    // Account info
    private JLabel  dUsername, dAccountStatus, dRole;
    // Teacher profile
    private JPanel  pnlTeacher;
    private JLabel  dMajor, dDegree;
    // Staff profile
    private JPanel  pnlStaff;
    private JLabel  dPosition, dSalary, dGrade;

    // ── Action buttons ─────────────────────────────────────────
    private JButton btnAdd, btnAssignProfile, btnEdit, btnDelete, btnToggleLock, btnResetPwd;

    // ── Current selection ──────────────────────────────────────
    private List<PersonnelDTO> dataList;
    private PersonnelDTO       selected;

    // ════════════════════════════════════════════════════════════
    public PersonnelManagementPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);

        loadData();
    }

    // ════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Title
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Quản lý Nhân sự");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_MAIN);
        JLabel sub = new JLabel("Giáo viên & Nhân viên — thông tin tài khoản và hồ sơ chuyên môn");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTE);
        titleBox.add(title);
        titleBox.add(sub);

        // Actions
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);

        lblStatus = new JLabel("Sẵn sàng");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_MUTE);

        btnAdd          = mkBtn("+ Thêm mới",       SUCCESS);
        btnAssignProfile= mkBtn("📋 Gán hồ sơ",    new Color(20, 184, 166));
        btnEdit         = mkBtn("✎ Sửa",           PRIMARY);
        btnToggleLock   = mkBtn("🔒 Khóa TK",      WARNING_C);
        btnResetPwd     = mkBtn("🔑 Đặt lại MK",   new Color(14, 165, 233));
        btnDelete       = mkBtn("✕ Xóa",           DANGER);

        btnEdit.setEnabled(false);
        btnToggleLock.setEnabled(false);
        btnResetPwd.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd          .addActionListener(e -> openAddDialog());
        btnAssignProfile.addActionListener(e -> openAssignProfileDialog());
        btnEdit         .addActionListener(e -> openEditDialog());
        btnToggleLock   .addActionListener(e -> toggleLock());
        btnResetPwd     .addActionListener(e -> resetPassword());
        btnDelete       .addActionListener(e -> deletePersonnel());

        acts.add(lblStatus);
        acts.add(Box.createHorizontalStrut(12));
        acts.add(btnAdd);
        acts.add(btnAssignProfile);
        acts.add(btnEdit);
        acts.add(btnToggleLock);
        acts.add(btnResetPwd);
        acts.add(btnDelete);

        p.add(titleBox, BorderLayout.WEST);
        p.add(acts,     BorderLayout.EAST);
        return p;
    }

    // ════════════════════════════════════════════════════════════
    // BODY: split pane
    // ════════════════════════════════════════════════════════════
    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTableCard(), buildDetailCard());
        split.setResizeWeight(0.62);
        split.setDividerSize(10);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setBackground(BG_PAGE);
        return split;
    }

    // ── Left: bảng danh sách ───────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = roundCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Search bar
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
        JLabel lbl = new JLabel("Danh sách nhân sự");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(TEXT_MAIN);

        JPanel srch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        srch.setOpaque(false);
        txtSearch = styledField("Tìm tên, email, username...", 240);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        srch.add(new JLabel("🔍"));
        srch.add(txtSearch);

        topBar.add(lbl,  BorderLayout.WEST);
        topBar.add(srch, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new String[]{"Mã", "Họ và tên", "Email", "SĐT", "Tên đăng nhập",
                         "Loại", "Vai trò", "Trạng thái TK"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        int[] cw = {50, 200, 200, 100, 140, 90, 170, 110};
        for (int i = 0; i < cw.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER_C, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ── Right: panel chi tiết ──────────────────────────────────
    private JPanel buildDetailCard() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel card = roundCard();
        card.setLayout(new BorderLayout());

        // Header của detail
        JPanel dhdr = new JPanel(new GridLayout(2, 1, 0, 4));
        dhdr.setOpaque(false);
        dhdr.setBorder(new EmptyBorder(16, 20, 10, 20));
        lblDetailTitle = new JLabel("Chọn nhân sự để xem chi tiết");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetailTitle.setForeground(TEXT_MAIN);
        lblDetailType = new JLabel(" ");
        lblDetailType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDetailType.setForeground(TEXT_MUTE);
        dhdr.add(lblDetailTitle);
        dhdr.add(lblDetailType);
        card.add(dhdr, BorderLayout.NORTH);

        // Scrollable content
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0, 20, 20, 20));

        // ── Section: Thông tin cá nhân ──
        body.add(sectionTitle("👤  Thông tin cơ bản"));
        body.add(Box.createVerticalStrut(8));
        JPanel gUser = infoGrid();
        dUserId   = addInfoRow(gUser, "Mã nhân sự");
        dFullName = addInfoRow(gUser, "Họ và tên");
        dEmail    = addInfoRow(gUser, "Email");
        dPhone    = addInfoRow(gUser, "Số điện thoại");
        dIdCard   = addInfoRow(gUser, "CMND / CCCD");
        body.add(gUser);
        body.add(Box.createVerticalStrut(16));

        // ── Section: Tài khoản ──
        body.add(sectionTitle("🔐  Thông tin tài khoản"));
        body.add(Box.createVerticalStrut(8));
        JPanel gAcc = infoGrid();
        dUsername      = addInfoRow(gAcc, "Tên đăng nhập");
        dAccountStatus = addInfoRow(gAcc, "Trạng thái");
        dRole          = addInfoRow(gAcc, "Nhóm quyền");
        body.add(gAcc);
        body.add(Box.createVerticalStrut(16));

        // ── Section: Hồ sơ Giáo viên ──
        pnlTeacher = new JPanel();
        pnlTeacher.setOpaque(false);
        pnlTeacher.setLayout(new BoxLayout(pnlTeacher, BoxLayout.Y_AXIS));
        pnlTeacher.add(sectionTitle("📚  Hồ sơ Giáo viên"));
        pnlTeacher.add(Box.createVerticalStrut(8));
        JPanel gT = infoGrid();
        dMajor  = addInfoRow(gT, "Chuyên ngành");
        dDegree = addInfoRow(gT, "Học vị / Chứng chỉ");
        pnlTeacher.add(gT);
        body.add(pnlTeacher);

        // ── Section: Hồ sơ Nhân viên ──
        pnlStaff = new JPanel();
        pnlStaff.setOpaque(false);
        pnlStaff.setLayout(new BoxLayout(pnlStaff, BoxLayout.Y_AXIS));
        pnlStaff.add(sectionTitle("🏢  Hồ sơ Nhân viên"));
        pnlStaff.add(Box.createVerticalStrut(8));
        JPanel gS = infoGrid();
        dPosition = addInfoRow(gS, "Chức vụ");
        dSalary   = addInfoRow(gS, "Lương cơ bản");
        dGrade    = addInfoRow(gS, "Bậc lương");
        pnlStaff.add(gS);
        body.add(pnlStaff);
        body.add(Box.createVerticalStrut(16));

        pnlTeacher.setVisible(false);
        pnlStaff.setVisible(false);

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(sp, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ════════════════════════════════════════════════════════════
    // DATA LOADING
    // ════════════════════════════════════════════════════════════
    private void loadData() {
        try {
            dataList = dao.findAll();
            tableModel.setRowCount(0);
            for (PersonnelDTO d : dataList) {
                tableModel.addRow(new Object[]{
                    d.getUserId(),
                    d.getFullName(),
                    nvl(d.getEmail()),
                    nvl(d.getPhone()),
                    nvl(d.getUsername()),
                    d.getPersonnelTypeLabel(),
                    nvl(d.getRoleGroupName()),
                    nvl(d.getAccountStatus())
                });
            }
            setStatus("Đã tải – " + dataList.size() + " nhân sự", SUCCESS);
        } catch (Exception ex) {
            setStatus("Lỗi tải dữ liệu", DANGER);
            showErr("Lỗi tải danh sách nhân sự:\n" + ex.getMessage());
        }
    }

    private void filterTable() {
        String kw = txtSearch == null ? "" : txtSearch.getText().trim();
        sorter.setRowFilter(
            kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }

    // ════════════════════════════════════════════════════════════
    // SELECTION → fill detail panel
    // ════════════════════════════════════════════════════════════
    private void onRowSelected() {
        int vr = table.getSelectedRow();
        if (vr < 0) {
            selected = null;
            clearDetail();
            setActionButtons(false);
            return;
        }
        int mr   = table.convertRowIndexToModel(vr);
        if (mr >= dataList.size()) return;
        selected = dataList.get(mr);
        fillDetail(selected);
        setActionButtons(true);

        boolean locked = "LOCKED".equals(selected.getAccountStatus());
        btnToggleLock.setText(locked ? "🔓 Mở khóa TK" : "🔒 Khóa TK");
        btnToggleLock.setBackground(locked ? SUCCESS : WARNING_C);
    }

    private void fillDetail(PersonnelDTO d) {
        lblDetailTitle.setText(d.getFullName());
        lblDetailType .setText(d.getPersonnelTypeLabel() + "  •  " + nvl(d.getRoleGroupName()));

        dUserId  .setText(String.valueOf(d.getUserId()));
        dFullName.setText(d.getFullName());
        dEmail   .setText(nvl(d.getEmail()));
        dPhone   .setText(nvl(d.getPhone(), "Chưa cập nhật"));
        dIdCard  .setText(nvl(d.getIdentityCard(), "Chưa cập nhật"));

        dUsername     .setText(nvl(d.getUsername()));
        dAccountStatus.setText(nvl(d.getAccountStatus()));
        dRole         .setText(nvl(d.getRoleGroupName(), "Chưa phân quyền"));

        boolean isTeacher = "TEACHER".equals(d.getPersonnelType());
        boolean isStaff   = "STAFF"  .equals(d.getPersonnelType());

        pnlTeacher.setVisible(isTeacher);
        pnlStaff  .setVisible(isStaff);

        if (isTeacher) {
            dMajor .setText(nvl(d.getMajor(),  "—"));
            dDegree.setText(nvl(d.getDegree(), "—"));
        }
        if (isStaff) {
            dPosition.setText(nvl(d.getPosition(), "—"));
            dSalary  .setText(d.getBaseSalary() > 0 ? nf.format(d.getBaseSalary()) + " ₫" : "—");
            dGrade   .setText(d.getSalaryGrade() > 0 ? "Bậc " + d.getSalaryGrade() : "—");
        }
    }

    private void clearDetail() {
        lblDetailTitle.setText("Chọn nhân sự để xem chi tiết");
        lblDetailType .setText(" ");
        for (JLabel l : new JLabel[]{dUserId,dFullName,dEmail,dPhone,dIdCard,
                                      dUsername,dAccountStatus,dRole,
                                      dMajor,dDegree,dPosition,dSalary,dGrade}) {
            if (l != null) l.setText("—");
        }
        pnlTeacher.setVisible(false);
        pnlStaff  .setVisible(false);
    }

    private void setActionButtons(boolean enabled) {
        btnEdit.setEnabled(enabled);
        btnToggleLock.setEnabled(enabled);
        btnResetPwd .setEnabled(enabled);
        btnDelete   .setEnabled(enabled);
    }

    // ════════════════════════════════════════════════════════════
    // CRUD ACTIONS
    // ════════════════════════════════════════════════════════════

    private void openAddDialog() {
        try {
            List<RoleGroup> roles = dao.findAllRoleGroups();
            PersonnelDialog dlg = new PersonnelDialog(
                    SwingUtilities.getWindowAncestor(this), null, roles);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                PersonnelDTO dto = dlg.getResult();
                String pass = dlg.getPassword();
                dao.insert(dto, PasswordUtil.hashPassword(pass));
                DBConnection.commitTransaction();
                showInfo("Thêm nhân sự \"" + dto.getFullName() + "\" thành công!");
                loadData();
            }
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi thêm nhân sự:\n" + ex.getMessage());
        }
    }

    private void openEditDialog() {
        if (selected == null) { showWarn("Vui lòng chọn nhân sự cần sửa."); return; }
        try {
            List<RoleGroup> roles = dao.findAllRoleGroups();
            PersonnelDialog dlg = new PersonnelDialog(
                    SwingUtilities.getWindowAncestor(this), selected, roles);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                dao.update(dlg.getResult());
                DBConnection.commitTransaction();
                showInfo("Cập nhật thành công!");
                loadData();
            }
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi cập nhật:\n" + ex.getMessage());
        }
    }

    private void toggleLock() {
        if (selected == null) return;
        boolean locked = "LOCKED".equals(selected.getAccountStatus());
        String action  = locked ? "MỞ KHÓA" : "KHÓA";
        int ok = JOptionPane.showConfirmDialog(this,
            action + " tài khoản \"" + selected.getUsername() + "\"?",
            "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            if (locked) dao.unlockAccount(selected.getUserId());
            else        dao.lockAccount(selected.getUserId());
            DBConnection.commitTransaction();
            showInfo("Tài khoản đã " + (locked ? "được mở khóa." : "bị khóa."));
            loadData();
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi:\n" + ex.getMessage());
        }
    }

    private void resetPassword() {
        if (selected == null) return;
        String newPwd = JOptionPane.showInputDialog(this,
            "Nhập mật khẩu mới cho tài khoản \"" + selected.getUsername() + "\":",
            "Đặt lại mật khẩu", JOptionPane.QUESTION_MESSAGE);
        if (newPwd == null || newPwd.trim().isEmpty()) return;
        if (newPwd.length() < 6) { showWarn("Mật khẩu phải có ít nhất 6 ký tự!"); return; }
        try {
            dao.resetPassword(selected.getAccountId(), PasswordUtil.hashPassword(newPwd));
            DBConnection.commitTransaction();
            showInfo("Đã đặt lại mật khẩu thành công!");
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi đặt lại mật khẩu:\n" + ex.getMessage());
        }
    }

    private void deletePersonnel() {
        if (selected == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
            "<html>Xóa nhân sự <b>" + selected.getFullName() + "</b>?<br>" +
            "Tài khoản và hồ sơ sẽ bị ẩn khỏi hệ thống.<br>" +
            "Dữ liệu vẫn được giữ lại trong DB.</html>",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.softDelete(selected.getUserId());
            DBConnection.commitTransaction();
            showInfo("Đã xóa nhân sự \"" + selected.getFullName() + "\".");
            selected = null;
            clearDetail();
            setActionButtons(false);
            loadData();
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi xóa:\n" + ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    // ASSIGN PROFILE DIALOG
    // ════════════════════════════════════════════════════════════
    private void openAssignProfileDialog() {
        try {
            List<RoleGroup> roles = dao.findAllRoleGroups();
            AssignProfileDialog dlg = new AssignProfileDialog(
                    SwingUtilities.getWindowAncestor(this), roles);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                PersonnelDTO dto = dlg.getResult();
                dao.assignProfile(dto);
                DBConnection.commitTransaction();
                showInfo("Đã gán hồ sơ "
                    + ("TEACHER".equals(dto.getPersonnelType()) ? "Giáo viên" : "Nhân viên")
                    + " cho \"" + nvl(dto.getFullName()) + "\" thành công!");
                loadData();
            }
        } catch (Exception ex) {
            DBConnection.rollbackTransaction();
            showErr("Lỗi gán hồ sơ:\n" + ex.getMessage());
        }
    }

    private class AssignProfileDialog extends JDialog {
        private boolean       confirmed;
        private PersonnelDTO  selectedUser;
        private PersonnelDTO  result;

        // Bảng chọn user
        private DefaultTableModel                 userModel;
        private JTable                            userTable;
        private TableRowSorter<DefaultTableModel> userSorter;
        private JTextField                        txtUserSearch;
        private List<PersonnelDTO>                userList;

        // Form hồ sơ
        private JComboBox<String>    cmbType;
        private JComboBox<RoleGroup> cmbRole;
        private JTextField           tfMajor, tfDegree;
        private JTextField           tfPosition, tfSalary, tfGrade;
        private JPanel               profileContainer; // CardLayout
        private JLabel               lblSelected;

        AssignProfileDialog(Window owner, List<RoleGroup> roles) {
            super(owner, "Gán hồ sơ nhân sự cho tài khoản có sẵn",
                  Dialog.ModalityType.APPLICATION_MODAL);
            setSize(740, 600);
            setResizable(false);
            setLocationRelativeTo(owner);
            buildUI(roles);
            loadUsers();
        }

        private void buildUI(List<RoleGroup> roles) {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);

            // ── Header ──
            JPanel hdr = new JPanel(new BorderLayout());
            hdr.setBackground(PRIMARY);
            hdr.setBorder(new EmptyBorder(12, 20, 12, 20));
            JPanel hdrTxt = new JPanel(new GridLayout(2, 1, 0, 3));
            hdrTxt.setOpaque(false);
            JLabel ht = new JLabel("GÁN HỒ SƠ NHÂN SỰ");
            ht.setFont(new Font("Segoe UI", Font.BOLD, 15));
            ht.setForeground(Color.WHITE);
            JLabel hs = new JLabel("Chọn tài khoản chưa có hồ sơ  →  điền thông tin giáo viên / nhân viên");
            hs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hs.setForeground(new Color(200, 215, 255));
            hdrTxt.add(ht); hdrTxt.add(hs);
            hdr.add(hdrTxt, BorderLayout.WEST);
            root.add(hdr, BorderLayout.NORTH);

            // ── Center ──
            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(Color.WHITE);

            // ── Part A: Bảng chọn user ──
            JPanel partA = new JPanel(new BorderLayout(0, 8));
            partA.setBackground(Color.WHITE);
            partA.setBorder(new EmptyBorder(14, 20, 8, 20));

            JPanel topHdr = new JPanel(new BorderLayout(8, 0));
            topHdr.setOpaque(false);
            JLabel step1 = new JLabel("① Chọn tài khoản cần gán hồ sơ");
            step1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            step1.setForeground(PRIMARY);
            txtUserSearch = new JTextField();
            txtUserSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtUserSearch.setPreferredSize(new Dimension(230, 28));
            txtUserSearch.setBorder(new CompoundBorder(
                new LineBorder(BORDER_C, 1, true), new EmptyBorder(2, 8, 2, 8)));
            txtUserSearch.setToolTipText("Tìm tên, email, tên đăng nhập...");
            txtUserSearch.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e)  { filterUsers(); }
                public void removeUpdate(DocumentEvent e)  { filterUsers(); }
                public void changedUpdate(DocumentEvent e) {}
            });
            JPanel srchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            srchWrap.setOpaque(false);
            srchWrap.add(new JLabel("🔍"));
            srchWrap.add(txtUserSearch);
            topHdr.add(step1,   BorderLayout.WEST);
            topHdr.add(srchWrap, BorderLayout.EAST);
            partA.add(topHdr, BorderLayout.NORTH);

            userModel = new DefaultTableModel(
                new String[]{"Mã", "Họ và tên", "Email", "Tên đăng nhập", "Vai trò hiện tại"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            userTable = new JTable(userModel);
            userTable.setRowHeight(32);
            userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userTable.setSelectionBackground(PRIMARY_SOFT);
            userTable.setSelectionForeground(PRIMARY_DARK);
            userTable.setShowGrid(true);
            userTable.setGridColor(BORDER_C);
            userTable.setFillsViewportHeight(true);
            JTableHeader uth = userTable.getTableHeader();
            uth.setFont(new Font("Segoe UI", Font.BOLD, 13));
            uth.setBackground(new Color(241, 245, 249));
            uth.setForeground(new Color(71, 85, 105));
            uth.setReorderingAllowed(false);
            int[] ucw = {50, 180, 210, 150, 130};
            for (int i = 0; i < ucw.length; i++)
                userTable.getColumnModel().getColumn(i).setPreferredWidth(ucw[i]);
            userSorter = new TableRowSorter<>(userModel);
            userTable.setRowSorter(userSorter);
            userTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) onUserSelected();
            });
            JScrollPane spU = new JScrollPane(userTable);
            spU.setBorder(new LineBorder(BORDER_C, 1, true));
            spU.getViewport().setBackground(Color.WHITE);
            spU.getVerticalScrollBar().setUnitIncrement(16);
            spU.setPreferredSize(new Dimension(0, 160));
            partA.add(spU, BorderLayout.CENTER);
            center.add(partA, BorderLayout.NORTH);

            // ── Dải phân cách + tên user được chọn ──
            JPanel sepBar = new JPanel(new BorderLayout());
            sepBar.setBackground(new Color(248, 250, 252));
            sepBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 1, 0, BORDER_C),
                new EmptyBorder(7, 20, 7, 20)));
            lblSelected = new JLabel("  ② Chưa chọn tài khoản — hãy click chọn dòng ở trên");
            lblSelected.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblSelected.setForeground(TEXT_MUTE);
            sepBar.add(lblSelected, BorderLayout.WEST);
            center.add(sepBar, BorderLayout.CENTER);

            // ── Part B: Form hồ sơ ──
            JPanel partB = new JPanel();
            partB.setLayout(new BoxLayout(partB, BoxLayout.Y_AXIS));
            partB.setBackground(Color.WHITE);
            partB.setBorder(new EmptyBorder(12, 20, 10, 20));

            // Hàng loại nhân sự + nhóm quyền
            JPanel comboRow = new JPanel(new GridLayout(1, 2, 20, 0));
            comboRow.setOpaque(false);
            comboRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            comboRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel pType = new JPanel();
            pType.setLayout(new BoxLayout(pType, BoxLayout.Y_AXIS));
            pType.setOpaque(false);
            JLabel ltLbl = new JLabel("Loại nhân sự (*)");
            ltLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            ltLbl.setForeground(TEXT_MUTE);
            ltLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbType = new JComboBox<>(new String[]{"Giáo viên", "Nhân viên"});
            cmbType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbType.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbType.setEnabled(false);
            pType.add(ltLbl);
            pType.add(Box.createVerticalStrut(4));
            pType.add(cmbType);

            JPanel pRole = new JPanel();
            pRole.setLayout(new BoxLayout(pRole, BoxLayout.Y_AXIS));
            pRole.setOpaque(false);
            JLabel lrLbl = new JLabel("Nhóm quyền");
            lrLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lrLbl.setForeground(TEXT_MUTE);
            lrLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbRole = new JComboBox<>(roles.toArray(new RoleGroup[0]));
            cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbRole.setEnabled(false);
            cmbRole.setRenderer((list, val, idx, sel, foc) -> {
                JLabel lb = new JLabel(val == null ? "" : val.getNameRoleGroup());
                lb.setBorder(new EmptyBorder(4, 10, 4, 10));
                if (sel) { lb.setBackground(PRIMARY_SOFT); lb.setOpaque(true); }
                return lb;
            });
            pRole.add(lrLbl);
            pRole.add(Box.createVerticalStrut(4));
            pRole.add(cmbRole);

            comboRow.add(pType);
            comboRow.add(pRole);
            partB.add(comboRow);
            partB.add(Box.createVerticalStrut(12));

            // CardLayout: Giáo viên vs Nhân viên
            profileContainer = new JPanel(new CardLayout());
            profileContainer.setOpaque(false);
            profileContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

            JPanel pT = new JPanel();
            pT.setLayout(new BoxLayout(pT, BoxLayout.Y_AXIS));
            pT.setOpaque(false);
            tfMajor  = dlgRow(pT, "Chuyên ngành (*)");
            tfDegree = dlgRow(pT, "Học vị / Chứng chỉ");
            tfMajor.setEnabled(false);
            tfDegree.setEnabled(false);
            profileContainer.add(pT, "TEACHER");

            JPanel pS = new JPanel();
            pS.setLayout(new BoxLayout(pS, BoxLayout.Y_AXIS));
            pS.setOpaque(false);
            tfPosition = dlgRow(pS, "Chức vụ (*)");
            tfSalary   = dlgRow(pS, "Lương cơ bản (số, VNĐ) (*)");
            tfGrade    = dlgRow(pS, "Bậc lương (số nguyên)");
            tfPosition.setEnabled(false);
            tfSalary.setEnabled(false);
            tfGrade.setEnabled(false);
            profileContainer.add(pS, "STAFF");

            partB.add(profileContainer);
            center.add(partB, BorderLayout.SOUTH);

            cmbType.addActionListener(e -> {
                boolean isT = cmbType.getSelectedIndex() == 0;
                ((CardLayout) profileContainer.getLayout())
                    .show(profileContainer, isT ? "TEACHER" : "STAFF");
            });

            root.add(center, BorderLayout.CENTER);

            // ── Footer ──
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(new Color(248, 249, 250));
            footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
            JButton btnC = new JButton("Hủy");
            btnC.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JButton btnA = new JButton("✔  Gán hồ sơ");
            btnA.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnA.setBackground(new Color(20, 184, 166));
            btnA.setForeground(Color.WHITE);
            btnA.setOpaque(true);
            btnA.setBorderPainted(false);
            btnA.setPreferredSize(new Dimension(140, 36));
            btnC.addActionListener(e -> dispose());
            btnA.addActionListener(e -> onAssign());
            footer.add(btnC);
            footer.add(btnA);
            root.add(footer, BorderLayout.SOUTH);

            setContentPane(root);
        }

        private void loadUsers() {
            try {
                userList = dao.findUsersWithoutProfile();
                userModel.setRowCount(0);
                for (PersonnelDTO d : userList) {
                    userModel.addRow(new Object[]{
                        d.getUserId(),
                        d.getFullName(),
                        nvl(d.getEmail()),
                        nvl(d.getUsername()),
                        nvl(d.getRoleGroupName(), "Chưa phân quyền")
                    });
                }
                if (userList.isEmpty()) {
                    // Table rỗng — hiển thị qua trạng thái
                    lblSelected.setText("  Không có tài khoản nào chưa có hồ sơ.");
                    lblSelected.setForeground(TEXT_MUTE);
                }
            } catch (Exception ex) {
                showErr("Lỗi tải danh sách:\n" + ex.getMessage());
            }
        }

        private void filterUsers() {
            String kw = txtUserSearch.getText().trim();
            userSorter.setRowFilter(
                kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }

        private void onUserSelected() {
            int vr = userTable.getSelectedRow();
            if (vr < 0) {
                selectedUser = null;
                lblSelected.setText("  ② Chưa chọn tài khoản — hãy click chọn dòng ở trên");
                lblSelected.setForeground(TEXT_MUTE);
                setFieldsEnabled(false);
                return;
            }
            int mr = userTable.convertRowIndexToModel(vr);
            selectedUser = userList.get(mr);
            lblSelected.setText("  ② Đang gán cho:  "
                + selectedUser.getFullName()
                + "   (tài khoản: " + nvl(selectedUser.getUsername(), "—") + ")");
            lblSelected.setForeground(new Color(15, 118, 110));
            // Chọn sẵn nhóm quyền nếu tài khoản đã có
            if (selectedUser.getRoleGroupId() > 0) {
                for (int i = 0; i < cmbRole.getItemCount(); i++) {
                    if (cmbRole.getItemAt(i).getRoleGroupId() == selectedUser.getRoleGroupId()) {
                        cmbRole.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // Reset form fields
            tfMajor.setText(""); tfDegree.setText("");
            tfPosition.setText(""); tfSalary.setText(""); tfGrade.setText("");
            setFieldsEnabled(true);
        }

        private void setFieldsEnabled(boolean on) {
            cmbType    .setEnabled(on);
            cmbRole    .setEnabled(on);
            tfMajor    .setEnabled(on);
            tfDegree   .setEnabled(on);
            tfPosition .setEnabled(on);
            tfSalary   .setEnabled(on);
            tfGrade    .setEnabled(on);
            Color bg = on ? Color.WHITE : new Color(245, 247, 250);
            tfMajor.setBackground(bg);    tfDegree.setBackground(bg);
            tfPosition.setBackground(bg); tfSalary.setBackground(bg);
            tfGrade.setBackground(bg);
        }

        private void onAssign() {
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn tài khoản cần gán hồ sơ!", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isT = cmbType.getSelectedIndex() == 0;
            if (isT) {
                if (tfMajor.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập chuyên ngành!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                if (tfPosition.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập chức vụ!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tfSalary.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập lương cơ bản!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    Double.parseDouble(tfSalary.getText().trim().replaceAll("[.,]", ""));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        "Lương cơ bản phải là số!", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Gán dữ liệu vào DTO (selectedUser)
            PersonnelDTO dto = selectedUser;
            dto.setPersonnelType(isT ? "TEACHER" : "STAFF");
            RoleGroup rg = (RoleGroup) cmbRole.getSelectedItem();
            if (rg != null) {
                dto.setRoleGroupId(rg.getRoleGroupId());
                dto.setRoleGroupName(rg.getNameRoleGroup());
            }
            if (isT) {
                dto.setMajor(tfMajor.getText().trim());
                String deg = tfDegree.getText().trim();
                dto.setDegree(deg.isEmpty() ? null : deg);
            } else {
                dto.setPosition(tfPosition.getText().trim());
                dto.setBaseSalary(
                    Double.parseDouble(tfSalary.getText().trim().replaceAll("[.,]", "")));
                String g = tfGrade.getText().trim();
                dto.setSalaryGrade(g.isEmpty() ? 0 : Integer.parseInt(g));
            }
            confirmed = true;
            result = dto;
            dispose();
        }

        boolean isConfirmed()     { return confirmed; }
        PersonnelDTO getResult()  { return result; }
    }

    // ════════════════════════════════════════════════════════════
    // ADD / EDIT DIALOG (inner class)
    // ════════════════════════════════════════════════════════════
    private class PersonnelDialog extends JDialog {
        private boolean confirmed;
        private PersonnelDTO result;
        private String password;

        // User fields
        private JTextField tfName, tfEmail, tfPhone, tfIdCard, tfUsername;
        private JPasswordField pfPassword;
        private JComboBox<RoleGroup> cmbRole;
        private JComboBox<String>    cmbType;

        // Teacher fields
        private JTextField tfMajor, tfDegree;
        // Staff fields
        private JTextField tfPosition, tfSalary, tfGrade;

        // Dynamic panels
        private JPanel pTeacher, pStaff;
        private JPanel tab3Container;   // CardLayout wrapper for Tab 3

        PersonnelDialog(Window owner, PersonnelDTO existing, List<RoleGroup> roles) {
            super(owner, existing == null ? "Thêm nhân sự mới" : "Chỉnh sửa nhân sự",
                  Dialog.ModalityType.APPLICATION_MODAL);
            setSize(560, 560);
            setResizable(false);
            setLocationRelativeTo(owner);
            buildUI(existing, roles);
            if (existing != null) prefill(existing);
        }

        private void buildUI(PersonnelDTO ex, List<RoleGroup> roles) {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);

            // ── Header ──
            JPanel hdr = new JPanel(new BorderLayout());
            hdr.setBackground(PRIMARY);
            hdr.setBorder(new EmptyBorder(14, 20, 14, 20));
            JLabel headerLbl = new JLabel(ex == null ? "THÊM NHÂN SỰ MỚI" : "CHỈNH SỬA NHÂN SỰ");
            headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            headerLbl.setForeground(Color.WHITE);
            hdr.add(headerLbl);
            root.add(hdr, BorderLayout.NORTH);

            // ── TabbedPane ──
            JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
            tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

            // ── Tab 1: Thông tin cá nhân ──
            JPanel p1 = tabForm();
            tfName   = dlgRow(p1, "Họ và tên (*)");
            tfEmail  = dlgRow(p1, "Email (*)");
            tfPhone  = dlgRow(p1, "Số điện thoại");
            tfIdCard = dlgRow(p1, "CMND / CCCD");
            tabs.addTab("👤  Cá nhân", scrollForm(p1));

            // ── Tab 2: Tài khoản hệ thống ──
            JPanel p2 = tabForm();
            tfUsername = dlgRow(p2, "Tên đăng nhập (*)");
            if (ex == null) {
                JLabel lp = new JLabel("Mật khẩu (*)");
                lp.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lp.setForeground(TEXT_MUTE);
                lp.setAlignmentX(Component.LEFT_ALIGNMENT);
                pfPassword = new JPasswordField();
                pfPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                pfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                pfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
                pfPassword.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_C, 1, true), new EmptyBorder(4, 10, 4, 10)));
                p2.add(lp);
                p2.add(Box.createVerticalStrut(4));
                p2.add(pfPassword);
                p2.add(Box.createVerticalStrut(10));
            }
            JLabel lr = new JLabel("Nhóm quyền (*)");
            lr.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lr.setForeground(TEXT_MUTE);
            lr.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbRole = new JComboBox<>(roles.toArray(new RoleGroup[0]));
            cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cmbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cmbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbRole.setRenderer((list, val, idx, sel, foc) -> {
                JLabel lb = new JLabel(val == null ? "" : val.getNameRoleGroup());
                lb.setBorder(new EmptyBorder(4, 10, 4, 10));
                if (sel) { lb.setBackground(PRIMARY_SOFT); lb.setOpaque(true); }
                return lb;
            });
            JLabel lt = new JLabel("Loại nhân sự (*)");
            lt.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lt.setForeground(TEXT_MUTE);
            lt.setAlignmentX(Component.LEFT_ALIGNMENT);
            cmbType = new JComboBox<>(new String[]{"Giáo viên", "Nhân viên"});
            cmbType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cmbType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cmbType.setAlignmentX(Component.LEFT_ALIGNMENT);
            p2.add(lr);
            p2.add(Box.createVerticalStrut(4));
            p2.add(cmbRole);
            p2.add(Box.createVerticalStrut(10));
            p2.add(lt);
            p2.add(Box.createVerticalStrut(4));
            p2.add(cmbType);
            p2.add(Box.createVerticalStrut(10));
            tabs.addTab("🔐  Tài khoản", scrollForm(p2));

            // ── Tab 3: Hồ sơ chuyên môn (CardLayout) ──
            tab3Container = new JPanel(new CardLayout());
            tab3Container.setBackground(Color.WHITE);

            pTeacher = tabForm();
            tfMajor  = dlgRow(pTeacher, "Chuyên ngành (*)");
            tfDegree = dlgRow(pTeacher, "Học vị / Chứng chỉ");
            tab3Container.add(scrollForm(pTeacher), "TEACHER");

            pStaff = tabForm();
            tfPosition = dlgRow(pStaff, "Chức vụ (*)");
            tfSalary   = dlgRow(pStaff, "Lương cơ bản (số, VNĐ) (*)");
            tfGrade    = dlgRow(pStaff, "Bậc lương (số nguyên)");
            tab3Container.add(scrollForm(pStaff), "STAFF");

            tabs.addTab("📚  Hồ sơ GV", tab3Container);

            // Khi đổi loại → đổi card Tab 3 + tên tab
            cmbType.addActionListener(e -> {
                boolean isT = cmbType.getSelectedIndex() == 0;
                ((CardLayout) tab3Container.getLayout()).show(tab3Container, isT ? "TEACHER" : "STAFF");
                tabs.setTitleAt(2, isT ? "📚  Hồ sơ GV" : "🏢  Hồ sơ NV");
            });

            root.add(tabs, BorderLayout.CENTER);

            // ── Footer ──
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(new Color(248, 249, 250));
            footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_C));
            JButton btnCancel = new JButton("Hủy");
            btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JButton btnSave = new JButton(ex == null ? "Thêm nhân sự" : "Lưu thay đổi");
            btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnSave.setBackground(SUCCESS);
            btnSave.setForeground(Color.WHITE);
            btnSave.setOpaque(true);
            btnSave.setBorderPainted(false);
            btnSave.setPreferredSize(new Dimension(150, 36));
            btnCancel.addActionListener(e -> dispose());
            btnSave  .addActionListener(e -> onSave(ex));
            footer.add(btnCancel);
            footer.add(btnSave);
            root.add(footer, BorderLayout.SOUTH);

            setContentPane(root);
        }

        /** BoxLayout Y_AXIS form panel cho mỗi tab */
        private JPanel tabForm() {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(Color.WHITE);
            p.setBorder(new EmptyBorder(16, 24, 16, 24));
            return p;
        }

        /** Bọc nội dung tab trong JScrollPane */
        private JScrollPane scrollForm(JPanel p) {
            JScrollPane sp = new JScrollPane(p);
            sp.setBorder(null);
            sp.getViewport().setBackground(Color.WHITE);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            return sp;
        }

        private void updateTypeVisibility() {
            if (tab3Container == null) return;
            boolean isTeacher = cmbType.getSelectedIndex() == 0;
            ((CardLayout) tab3Container.getLayout()).show(tab3Container, isTeacher ? "TEACHER" : "STAFF");
        }

        private void prefill(PersonnelDTO d) {
            // Dùng "" cho null thay vì "—" để tránh lưu chuỗi "—" vào DB
            tfName   .setText(d.getFullName()     != null ? d.getFullName()     : "");
            tfEmail  .setText(d.getEmail()         != null ? d.getEmail()        : "");
            tfPhone  .setText(d.getPhone()         != null ? d.getPhone()        : "");
            tfIdCard .setText(d.getIdentityCard()  != null ? d.getIdentityCard() : "");
            tfUsername.setText(d.getUsername()     != null ? d.getUsername()     : "");
            tfUsername.setEditable(false);
            tfUsername.setBackground(new Color(248, 250, 252));
            // Role
            for (int i = 0; i < cmbRole.getItemCount(); i++) {
                if (cmbRole.getItemAt(i).getRoleGroupId() == d.getRoleGroupId()) {
                    cmbRole.setSelectedIndex(i); break;
                }
            }
            // Type – setSelectedIndex fires ActionListener which updates Tab 3 card + title
            cmbType.setSelectedIndex("TEACHER".equals(d.getPersonnelType()) ? 0 : 1);
            cmbType.setEnabled(false);
            // Profile
            if ("TEACHER".equals(d.getPersonnelType())) {
                tfMajor .setText(d.getMajor()  != null ? d.getMajor()  : "");
                tfDegree.setText(d.getDegree() != null ? d.getDegree() : "");
            } else {
                tfPosition.setText(d.getPosition() != null ? d.getPosition() : "");
                tfSalary  .setText(d.getBaseSalary() > 0
                                   ? String.valueOf((long) d.getBaseSalary()) : "");
                tfGrade   .setText(d.getSalaryGrade() > 0
                                   ? String.valueOf(d.getSalaryGrade()) : "");
            }
            updateTypeVisibility();
        }

        private void onSave(PersonnelDTO existing) {
            try {
                String name  = tfName.getText().trim();
                String email = tfEmail.getText().trim();
                String uname = tfUsername.getText().trim();
                if (name .isEmpty()) { showWarnD("Vui lòng nhập họ và tên!"); return; }
                if (email.isEmpty()) { showWarnD("Vui lòng nhập email!"); return; }
                if (uname.isEmpty()) { showWarnD("Vui lòng nhập tên đăng nhập!"); return; }
                if (existing == null && (pfPassword == null
                        || new String(pfPassword.getPassword()).trim().isEmpty())) {
                    showWarnD("Vui lòng nhập mật khẩu!"); return;
                }
                if (existing == null && new String(pfPassword.getPassword()).length() < 6) {
                    showWarnD("Mật khẩu phải có ít nhất 6 ký tự!"); return;
                }

                boolean isTeacher = cmbType.getSelectedIndex() == 0;
                if (isTeacher && tfMajor.getText().trim().isEmpty()) {
                    showWarnD("Vui lòng nhập chuyên ngành!"); return;
                }
                if (!isTeacher) {
                    if (tfPosition.getText().trim().isEmpty()) {
                        showWarnD("Vui lòng nhập chức vụ!"); return;
                    }
                    // Khi thêm mới: bắt buộc nhập lương. Khi sửa: để trống = giữ nguyên giá trị cũ
                    String salaryTxt = tfSalary.getText().trim();
                    if (existing == null && salaryTxt.isEmpty()) {
                        showWarnD("Vui lòng nhập lương cơ bản!"); return;
                    }
                    if (!salaryTxt.isEmpty()) {
                        try { Double.parseDouble(salaryTxt.replaceAll("[.,]", "")); }
                        catch (NumberFormatException e) { showWarnD("Lương cơ bản phải là số!"); return; }
                    }
                }

                PersonnelDTO dto = existing != null ? existing : new PersonnelDTO();
                dto.setFullName(name);
                dto.setEmail(email);
                dto.setPhone(blankAsNull(tfPhone.getText()));
                dto.setIdentityCard(blankAsNull(tfIdCard.getText()));
                dto.setUsername(uname);
                dto.setPersonnelType(isTeacher ? "TEACHER" : "STAFF");
                RoleGroup rg = (RoleGroup) cmbRole.getSelectedItem();
                if (rg != null) { dto.setRoleGroupId(rg.getRoleGroupId()); dto.setRoleGroupName(rg.getNameRoleGroup()); }
                if (isTeacher) {
                    dto.setMajor(tfMajor.getText().trim());
                    dto.setDegree(blankAsNull(tfDegree.getText()));
                } else {
                    dto.setPosition(tfPosition.getText().trim());
                    String salaryTxt = tfSalary.getText().trim();
                    if (!salaryTxt.isEmpty()) {
                        dto.setBaseSalary(Double.parseDouble(salaryTxt.replaceAll("[.,]", "")));
                    }
                    // Nếu để trống khi sửa → giữ nguyên baseSalary cũ (đã có trong dto/existing)
                    String g = tfGrade.getText().trim();
                    dto.setSalaryGrade(g.isEmpty() ? dto.getSalaryGrade() : Integer.parseInt(g));
                }

                result = dto;
                password = (pfPassword != null) ? new String(pfPassword.getPassword()) : null;
                confirmed = true;
                dispose();
            } catch (Exception ex) {
                showWarnD("Lỗi dữ liệu nhập: " + ex.getMessage());
            }
        }

        private String blankAsNull(String s) {
            return (s == null || s.trim().isEmpty()) ? null : s.trim();
        }
        private void showWarnD(String m) {
            JOptionPane.showMessageDialog(this, m, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }

        boolean isConfirmed() { return confirmed; }
        PersonnelDTO getResult()  { return result; }
        String getPassword()  { return password; }
    }

    // ════════════════════════════════════════════════════════════
    // TABLE RENDERERS
    // ════════════════════════════════════════════════════════════

    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int r, int c) {
            String txt = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
            if (sel) {
                lbl.setBackground(PRIMARY_SOFT); lbl.setForeground(PRIMARY_DARK);
            } else if ("Giáo viên".equals(txt)) {
                lbl.setBackground(new Color(219, 234, 254)); lbl.setForeground(new Color(29, 78, 216));
            } else {
                lbl.setBackground(new Color(243, 232, 255)); lbl.setForeground(new Color(109, 40, 217));
            }
            return lbl;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int r, int c) {
            String txt = v == null ? "" : v.toString();
            JLabel lbl = new JLabel("ACTIVE".equals(txt) ? "Hoạt động" : "Đã khóa", SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
            if (sel) {
                lbl.setBackground(PRIMARY_SOFT); lbl.setForeground(PRIMARY_DARK);
            } else if ("ACTIVE".equals(txt)) {
                lbl.setBackground(new Color(220, 252, 231)); lbl.setForeground(new Color(22, 101, 52));
            } else {
                lbl.setBackground(new Color(254, 226, 226)); lbl.setForeground(new Color(185, 28, 28));
            }
            return lbl;
        }
    }

    // ════════════════════════════════════════════════════════════
    // UI HELPERS
    // ════════════════════════════════════════════════════════════

    private void styleTable(JTable t) {
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
        t.setIntercellSpacing(new Dimension(1, 1));
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(new Color(71, 85, 105));
        h.setPreferredSize(new Dimension(0, 36));
        h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(TEXT_MAIN);
                }
                return this;
            }
        });
    }

    private JPanel roundCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.setColor(BORDER_C);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
            { setOpaque(false); }
        };
    }

    private JPanel infoGrid() {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private JLabel addInfoRow(JPanel grid, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTE);
        JLabel val = new JLabel("—");
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground(TEXT_MAIN);
        grid.add(lbl);
        grid.add(val);
        return val;
    }

    private JPanel sectionTitle(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(8, 0, 4, 0));
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(PRIMARY);
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(226, 232, 240));
        p.add(l,   BorderLayout.WEST);
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    /** Tạo 1 label + field cho form dialog */
    private JTextField dlgRow(JPanel p, String label) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_MUTE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true),
                                       new EmptyBorder(4, 10, 4, 10)));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
        return f;
    }

    private JPanel dlgSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(8, 0, 6, 0));
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(PRIMARY);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_C);
        p.add(l,   BorderLayout.WEST);
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    private JTextField styledField(String hint, int width) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(width, 32));
        f.setBorder(new CompoundBorder(new LineBorder(BORDER_C, 1, true),
                                       new EmptyBorder(3, 8, 3, 8)));
        f.setToolTipText(hint);
        return f;
    }

    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }

    private void setStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> { lblStatus.setText(msg); lblStatus.setForeground(color); });
    }
    private String nvl(String s)            { return s == null ? "—" : s; }
    private String nvl(String s, String def){ return (s == null || s.isBlank()) ? def : s; }
    private void showInfo(String m) { JOptionPane.showMessageDialog(this, m, "Thông báo",    JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn(String m) { JOptionPane.showMessageDialog(this, m, "Cảnh báo",     JOptionPane.WARNING_MESSAGE);     }
    private void showErr (String m) { JOptionPane.showMessageDialog(this, m, "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);       }

    // ── Standalone test ──
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("EduFlex – Quản lý Nhân sự");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1300, 800);
            f.setLocationRelativeTo(null);
            f.add(new PersonnelManagementPanel());
            f.setVisible(true);
        });
    }
}
