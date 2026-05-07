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
    
    // UI Tab Tài Khoản
    private CustomTable accTable;
    private DefaultTableModel accTableModel;
    private JComboBox<String> cbFilter;
    private JButton btnRestore;
    private JButton btnDelete;
    
    // UI Tab Phân Quyền
    private CustomTable permTable;
    private DefaultTableModel permTableModel;
    private JComboBox<RoleGroup> cbRoleGroup;
    private List<PermissionDTO> currentPermissions;

    public AccountManagerUI() {
        this.accController = new AccountController();
        this.permController = new PermissionController();
        this.currentPermissions = new ArrayList<>();
        
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(new Color(45, 52, 54));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("👤 Quản Lý Tài Khoản", createAccountPanel());
        tabbedPane.addTab("🛡️ Ma Trận Phân Quyền", createPermissionPanel());

        add(tabbedPane, BorderLayout.CENTER);
        
        reloadAccountTable();
        loadRoleGroupsForPermissionTab();
    }

    // ==========================================
    // TAB 1: QUẢN LÝ TÀI KHOẢN (CÓ LỌC & KHÔI PHỤC)
    // ==========================================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setBackground(new Color(245, 246, 250));
        panel.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Danh Sách Tài Khoản Nhân Sự");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(45, 52, 54));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        
        // ComboBox Lọc Trạng Thái
        cbFilter = new JComboBox<>(new String[]{"Đang hoạt động", "Tài khoản bị khóa", "Tất cả"});
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbFilter.setPreferredSize(new Dimension(160, 38));
        cbFilter.setBackground(Color.WHITE);
        cbFilter.addActionListener(e -> reloadAccountTable());

        GradientButton btnAdd = new GradientButton("+ Cấp phát mới"); 
        btnAdd.setPreferredSize(new Dimension(150, 38));
        
        btnDelete = new JButton("Khóa / Xóa");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDelete.setForeground(new Color(231, 76, 60));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRestore = new JButton("Mở Khóa");
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRestore.setForeground(new Color(39, 174, 96));
        btnRestore.setContentAreaFilled(false);
        btnRestore.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRestore.setVisible(false);

        // Sự kiện
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

        actionPanel.add(new JLabel("Lọc: ")); actionPanel.add(cbFilter);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(btnAdd); actionPanel.add(btnRestore); actionPanel.add(btnDelete);
        
        headerPanel.add(title, BorderLayout.WEST); headerPanel.add(actionPanel, BorderLayout.EAST);

        String[] cols = {"ID", "Username", "Họ và Tên", "Email", "Nhóm Quyền", "Trạng Thái"};
        accTableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        accTable = new CustomTable(accTableModel);
        
        JScrollPane scrollPane = new JScrollPane(accTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(headerPanel, BorderLayout.NORTH); panel.add(scrollPane, BorderLayout.CENTER);
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

            // Gọi đúng hàm getAccountsByFilter
            for (AccountListDTO dto : accController.getAccountsByFilter(dbFilter)) {
                accTableModel.addRow(new Object[]{dto.accountId, dto.username, dto.fullName, dto.email, dto.roleGroupName, dto.status});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu tài khoản: " + e.getMessage());
        }
    }

    // ==========================================
    // TAB 2: MA TRẬN PHÂN QUYỀN (GIỮ NGUYÊN)
    // ==========================================
    private JPanel createPermissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setBackground(new Color(245, 246, 250));
        panel.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Ma Trận Phân Quyền Hệ Thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(45, 52, 54));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        filterPanel.setOpaque(false);
        
        JLabel lblSelect = new JLabel("Chọn Nhóm Quyền: ");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        cbRoleGroup = new JComboBox<>();
        cbRoleGroup.setPreferredSize(new Dimension(220, 38));
        cbRoleGroup.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRoleGroup.setBackground(Color.WHITE);
        cbRoleGroup.addActionListener(e -> reloadPermissionTable());

        GradientButton btnSave = new GradientButton("💾 Lưu Thay Đổi");
        btnSave.setPreferredSize(new Dimension(160, 38));
        btnSave.addActionListener(e -> savePermissions());

        filterPanel.add(lblSelect); filterPanel.add(cbRoleGroup);
        filterPanel.add(Box.createHorizontalStrut(10)); filterPanel.add(btnSave);

        headerPanel.add(title, BorderLayout.WEST); headerPanel.add(filterPanel, BorderLayout.EAST);

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

        panel.add(headerPanel, BorderLayout.NORTH); panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadRoleGroupsForPermissionTab() {
        try {
            cbRoleGroup.removeAllItems();
            for (RoleGroup rg : accController.getAvailableRoles()) { cbRoleGroup.addItem(rg); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void reloadPermissionTable() {
        RoleGroup selectedGroup = (RoleGroup) cbRoleGroup.getSelectedItem();
        if (selectedGroup == null) return;
        try {
            permTableModel.setRowCount(0);
            currentPermissions = permController.getPermissions(selectedGroup.getRoleGroupId());
            for (PermissionDTO p : currentPermissions) {
                permTableModel.addRow(new Object[]{ p.functionName, p.canView, p.canAdd, p.canEdit, p.canDelete });
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi tải ma trận quyền: " + e.getMessage()); }
    }

    private void savePermissions() {
        RoleGroup selectedGroup = (RoleGroup) cbRoleGroup.getSelectedItem();
        if (selectedGroup == null || currentPermissions.isEmpty()) return;
        try {
            for (int i = 0; i < permTableModel.getRowCount(); i++) {
                currentPermissions.get(i).canView = (boolean) permTableModel.getValueAt(i, 1);
                currentPermissions.get(i).canAdd = (boolean) permTableModel.getValueAt(i, 2);
                currentPermissions.get(i).canEdit = (boolean) permTableModel.getValueAt(i, 3);
                currentPermissions.get(i).canDelete = (boolean) permTableModel.getValueAt(i, 4);
            }
            permController.updatePermissions(currentPermissions);
            JOptionPane.showMessageDialog(this, "Cập nhật quyền thành công cho nhóm: " + selectedGroup.getNameRoleGroup(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi khi lưu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }
}