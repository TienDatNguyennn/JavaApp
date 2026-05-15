/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.model.AccountDTO;
import com.mycompany.myapp.service.PermissionService;
import com.mycompany.myapp.view.components.CustomButton;
import com.mycompany.myapp.view.components.PermissionPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private JTable tblAccounts;
    private DefaultTableModel tableModel;
    private PermissionPanel permissionPanel;
    private PermissionService service = new PermissionService();
    private List<AccountDTO> accountList;

    public MainFrame() {
        setTitle("Quản Lý Phân Quyền - Hệ Thống Đào Tạo");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadTableData();
    }

    private void initUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

        // --- BÊN TRÁI: DANH SÁCH TÀI KHOẢN ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBackground(Color.WHITE); 
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        JTextField txtSearch = new JTextField();
        searchPanel.add(new JLabel("Tìm tài khoản: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        
        CustomButton btnSearch = new CustomButton("Tìm");
        btnSearch.setPreferredSize(new Dimension(80, 30));
        searchPanel.add(btnSearch, BorderLayout.EAST);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Tên Nhân Viên/GV", "Trạng Thái"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblAccounts = new JTable(tableModel);
        tblAccounts.setRowHeight(30);
        tblAccounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftPanel.add(new JScrollPane(tblAccounts), BorderLayout.CENTER);

        // --- BÊN PHẢI: FORM PHÂN QUYỀN MA TRẬN ---
        permissionPanel = new PermissionPanel();

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(permissionPanel);
        add(splitPane);

        // Event click Table
        tblAccounts.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblAccounts.getSelectedRow() != -1) {
                AccountDTO selected = accountList.get(tblAccounts.getSelectedRow());
                permissionPanel.bindAccount(selected);
            }
        });
    }

    private void loadTableData() {
        accountList = service.getAllAccounts();
        for (AccountDTO acc : accountList) {
            tableModel.addRow(new Object[]{acc.getAccountId(), acc.getUsername(), acc.getFullName(), acc.getStatus()});
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}