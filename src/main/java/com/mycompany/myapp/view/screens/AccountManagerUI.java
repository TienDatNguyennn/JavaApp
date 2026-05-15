package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.controller.AccountController;
import com.mycompany.myapp.controller.PermissionController;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.view.components.UIComponents.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AccountManagerUI extends JPanel {
    
    private AccountController accController;
    private PermissionController permController;
    
    // ==========================================
    // UI Biến cho Tab Tài Khoản
    // ==========================================
    private CustomTable accTable;
    private DefaultTableModel accTableModel;
    private JComboBox<String> cbFilter;
    private JButton btnRestore;
    private JButton btnDelete;
    
    // ==========================================
    // UI Biến cho Tab Phân Quyền
    // ==========================================
    private CustomTable permTable;
    private DefaultTableModel permTableModel;
    private JRadioButton rbGroup;
    private JRadioButton rbAccount;
    private JComboBox<Object> cbTarget; 
    private boolean isGroupMode = true; 
    private List<PermissionDTO> currentPermissions;

    // Biến cho Custom Tab Navigation
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnTabAcc;
    private JButton btnTabPerm;

    public AccountManagerUI() {
        this.accController = new AccountController();
        this.permController = new PermissionController();
        this.currentPermissions = new ArrayList<>();
        
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F8FAFC")); // Màu nền dịu mắt chuẩn Web

        // 1. TẠO CUSTOM TAB BAR (Thay thế JTabbedPane mặc định)
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));

        btnTabAcc = createTabButton("Quản Lý Tài Khoản", true);
        btnTabPerm = createTabButton("Ma Trận Phân Quyền", false);

        btnTabAcc.addActionListener(e -> switchTab("ACC", btnTabAcc, btnTabPerm));
        btnTabPerm.addActionListener(e -> switchTab("PERM", btnTabPerm, btnTabAcc));

        tabBar.add(btnTabAcc);
        tabBar.add(btnTabPerm);

        // 2. KHU VỰC NỘI DUNG (Sử dụng CardLayout)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.decode("#F8FAFC"));
        cardPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Bo góc nội dung như những tấm thẻ (Card)
        JPanel accWrapper = createCardWrapper(createAccountPanel());
        JPanel permWrapper = createCardWrapper(createPermissionPanel());

        cardPanel.add(accWrapper, "ACC");
        cardPanel.add(permWrapper, "PERM");

        add(tabBar, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        
        // Load dữ liệu ban đầu
        reloadAccountTable();
    }

    // ==========================================
    // UI HELPER: CHUYỂN TAB VÀ ĐỔI MÀU GIAO DIỆN
    // ==========================================
    private JButton createTabButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 42));
        
        if (isActive) {
            btn.setBackground(Color.decode("#F5F3FF")); // Tím Lavender
            btn.setForeground(Color.decode("#7C3AED")); // Chữ tím đậm
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.decode("#64748B")); // Chữ xám nhạt
        }
        return btn;
    }

    private void switchTab(String cardName, JButton activeBtn, JButton inactiveBtn) {
        cardLayout.show(cardPanel, cardName);
        activeBtn.setBackground(Color.decode("#F5F3FF"));
        activeBtn.setForeground(Color.decode("#7C3AED"));
        inactiveBtn.setBackground(Color.WHITE);
        inactiveBtn.setForeground(Color.decode("#64748B"));
    }

    private JPanel createCardWrapper(JPanel innerPanel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1));
        wrapper.add(innerPanel, BorderLayout.CENTER);
        return wrapper;
    }

    // ==========================================
    // TAB 1: QUẢN LÝ TÀI KHOẢN
    // ==========================================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);
        
        JLabel title = new JLabel("Danh Sách Tài Khoản Nhân Sự");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(45, 52, 54));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        
        cbFilter = new JComboBox<>(new String[]{"Đang hoạt động", "Tài khoản bị khóa", "Tất cả"});
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbFilter.setPreferredSize(new Dimension(160, 38));
        cbFilter.setBackground(Color.WHITE);
        cbFilter.addActionListener(e -> reloadAccountTable());

        GradientButton btnAdd = new GradientButton("+ Cấp phát mới"); 
        btnAdd.setPreferredSize(new Dimension(150, 38));
        
        // Tối ưu UI Nút Flat
        btnDelete = new JButton("Khóa / Xóa");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setBackground(Color.decode("#FEE2E2")); // Nền đỏ nhạt
        btnDelete.setForeground(Color.decode("#DC2626")); // Chữ đỏ đậm
        btnDelete.setFocusPainted(false); btnDelete.setBorderPainted(false); btnDelete.setOpaque(true);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setBorder(new EmptyBorder(8, 15, 8, 15));

        btnRestore = new JButton("Mở Khóa");
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRestore.setBackground(Color.decode("#DCFCE7")); // Nền xanh nhạt
        btnRestore.setForeground(Color.decode("#16A34A")); // Chữ xanh đậm
        btnRestore.setFocusPainted(false); btnRestore.setBorderPainted(false); btnRestore.setOpaque(true);
        btnRestore.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestore.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnRestore.setVisible(false);

        // Sự kiện các nút
        btnAdd.addActionListener(e -> {
            CreateAccountDialog dialog = new CreateAccountDialog((JFrame) SwingUtilities.getWindowAncestor(this), accController);
            dialog.setVisible(true);
            if (dialog.isSuccess()) reloadAccountTable();
        });

        btnDelete.addActionListener(e -> {
            int row = accTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần khóa!"); return; }
            int accId = (int) accTableModel.getValueAt(row, 0);
            if(JOptionPane.showConfirmDialog(this, "Thu hồi quyền truy cập của tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { 
                try { accController.deleteAccount(accId); reloadAccountTable(); } 
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage()); }
            }
        });

        btnRestore.addActionListener(e -> {
            int row = accTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần mở khóa!"); return; }
            int accId = (int) accTableModel.getValueAt(row, 0);
            if(JOptionPane.showConfirmDialog(this, "Khôi phục trạng thái hoạt động cho tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { 
                try { accController.restoreAccount(accId); reloadAccountTable(); } 
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi khi khôi phục: " + ex.getMessage()); }
            }
        });

        actionPanel.add(new JLabel("Lọc: ")); 
        actionPanel.add(cbFilter);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(btnAdd); 
        actionPanel.add(btnRestore); 
        actionPanel.add(btnDelete);
        
        headerPanel.add(title, BorderLayout.NORTH); 
        headerPanel.add(actionPanel, BorderLayout.CENTER);

        String[] cols = {"ID", "Username", "Họ và Tên", "Email", "Nhóm Quyền", "Trạng Thái"};
        accTableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        accTable = new CustomTable(accTableModel);
        
        JScrollPane scrollPane = new JScrollPane(accTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(headerPanel, BorderLayout.NORTH); 
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void reloadAccountTable() {
        try {
            accTableModel.setRowCount(0);
            String selectedFilter = cbFilter.getSelectedItem().toString();
            String dbFilter = "ACTIVE";
            
            if (selectedFilter.equals("Tài khoản bị khóa")) {
                dbFilter = "LOCKED";
                btnDelete.setVisible(false); btnRestore.setVisible(true);
            } else if (selectedFilter.equals("Tất cả")) {
                dbFilter = "ALL";
                btnDelete.setVisible(true); btnRestore.setVisible(true);
            } else {
                dbFilter = "ACTIVE";
                btnDelete.setVisible(true); btnRestore.setVisible(false);
            }

            for (AccountListDTO dto : accController.getAccountsByFilter(dbFilter)) {
                accTableModel.addRow(new Object[]{dto.accountId, dto.username, dto.fullName, dto.email, dto.roleGroupName, dto.status});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu tài khoản: " + e.getMessage());
        }
    }

    // ==========================================
    // TAB 2: MA TRẬN PHÂN QUYỀN
    // ==========================================
    private JPanel createPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 15));
        headerPanel.setOpaque(false);
        
        JLabel title = new JLabel("Ma Trận Phân Quyền Hệ Thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(45, 52, 54));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        filterPanel.setOpaque(false);
        
        rbGroup = new JRadioButton("Theo Nhóm Quyền", true);
        rbAccount = new JRadioButton("Theo Tài Khoản");
        rbGroup.setOpaque(false); rbAccount.setOpaque(false);
        rbGroup.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbAccount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rbGroup.setForeground(Color.decode("#4C1D95"));
        rbAccount.setForeground(Color.decode("#4C1D95"));
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbGroup); bg.add(rbAccount);

        cbTarget = new JComboBox<>();
        cbTarget.setPreferredSize(new Dimension(250, 38));
        cbTarget.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTarget.setBackground(Color.WHITE);
        
        cbTarget.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RoleGroup) {
                    setText(((RoleGroup) value).getNameRoleGroup());
                } else if (value instanceof AccountListDTO) {
                    AccountListDTO acc = (AccountListDTO) value;
                    setText(acc.username + " (" + acc.fullName + ")");
                }
                return this;
            }
        });

        cbTarget.addActionListener(e -> reloadPermissionTable());

        GradientButton btnSave = new GradientButton("Lưu Thay Đổi");
        btnSave.setPreferredSize(new Dimension(160, 38));
        btnSave.addActionListener(e -> savePermissions());

        filterPanel.add(rbGroup); 
        filterPanel.add(rbAccount);
        filterPanel.add(cbTarget);
        filterPanel.add(Box.createHorizontalStrut(10)); 
        filterPanel.add(btnSave);

        headerPanel.add(title, BorderLayout.NORTH); 
        headerPanel.add(filterPanel, BorderLayout.CENTER);

        String[] cols = {"Chức Năng (Module)", "Xem", "Thêm", "Sửa", "Xóa"};
        permTableModel = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 1 && columnIndex <= 4) return Boolean.class;
                return String.class;
            }
            @Override public boolean isCellEditable(int row, int column) { return column >= 1; }
        };

        permTable = new CustomTable(permTableModel);
        permTable.setRowHeight(45);
        permTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(permTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        rbGroup.addActionListener(e -> { isGroupMode = true; loadRoleGroupsForCombo(); });
        rbAccount.addActionListener(e -> { isGroupMode = false; loadAccountsForCombo(); });

        loadRoleGroupsForCombo();

        panel.add(headerPanel, BorderLayout.NORTH); 
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadRoleGroupsForCombo() {
        try {
            cbTarget.removeAllItems();
            for (RoleGroup rg : accController.getAvailableRoles()) { cbTarget.addItem(rg); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadAccountsForCombo() {
        try {
            cbTarget.removeAllItems();
            for (AccountListDTO acc : accController.getAccountsByFilter("ACTIVE")) { cbTarget.addItem(acc); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reloadPermissionTable() {
        Object selectedTarget = cbTarget.getSelectedItem();
        if (selectedTarget == null) return;
        
        try {
            permTableModel.setRowCount(0);
            if (isGroupMode) {
                RoleGroup rg = (RoleGroup) selectedTarget;
                currentPermissions = permController.getPermissions(rg.getRoleGroupId());
            } else {
                AccountListDTO acc = (AccountListDTO) selectedTarget;
                currentPermissions = permController.getPermissionsByAccount(acc.accountId);
            }
            
            for (PermissionDTO p : currentPermissions) {
                permTableModel.addRow(new Object[]{ p.functionName, p.canView, p.canAdd, p.canEdit, p.canDelete });
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Lỗi tải ma trận quyền: " + e.getMessage()); 
        }
    }

    private void savePermissions() {
        Object selectedTarget = cbTarget.getSelectedItem();
        if (selectedTarget == null || currentPermissions.isEmpty()) return;
        
        try {
            for (int i = 0; i < permTableModel.getRowCount(); i++) {
                currentPermissions.get(i).canView = (boolean) permTableModel.getValueAt(i, 1);
                currentPermissions.get(i).canAdd = (boolean) permTableModel.getValueAt(i, 2);
                currentPermissions.get(i).canEdit = (boolean) permTableModel.getValueAt(i, 3);
                currentPermissions.get(i).canDelete = (boolean) permTableModel.getValueAt(i, 4);
            }

            if (isGroupMode) {
                permController.updatePermissions(currentPermissions);
                JOptionPane.showMessageDialog(this, "Cập nhật quyền thành công cho NHÓM!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                AccountListDTO acc = (AccountListDTO) selectedTarget;
                permController.updateAccountCustomPermissions(acc.accountId, currentPermissions);
                JOptionPane.showMessageDialog(this, "Cập nhật đặc quyền thành công cho TÀI KHOẢN: " + acc.username, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); 
        }
    }
}