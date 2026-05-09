package com.mycompany.myapp.view.screens;

import com.mycompany.myapp.model.AccountDTO;
import com.mycompany.myapp.service.PermissionService;
import com.mycompany.myapp.view.components.CustomButton;
import com.mycompany.myapp.view.components.PermissionPanel;
import com.mycompany.myapp.view.screens.finance.FinanceMainPanel; // Đã import
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private JTable tblAccounts;
    private DefaultTableModel tableModel;
    private PermissionPanel permissionPanel;
    private JTabbedPane tabbedPane; // 1. Khai báo biến này để hết lỗi "cannot find symbol"
    private PermissionService service = new PermissionService();
    private List<AccountDTO> accountList;

    public MainFrame() {
        setTitle("Hệ Thống Quản Lý Đào Tạo");
        setSize(1200, 700); // Tăng kích thước một chút để chứa giao diện tài chính
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadTableData();
    }
    
    private void initUI() {
        // 2. Khởi tạo JTabbedPane
        tabbedPane = new JTabbedPane();

        // --- TAB 1: QUẢN LÝ PHÂN QUYỀN (Giao diện cũ của bạn) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

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

        permissionPanel = new PermissionPanel();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(permissionPanel);

        // --- TAB 2: QUẢN LÝ TÀI CHÍNH ---
        FinanceMainPanel financePanel = new FinanceMainPanel();

        // 3. Thêm các Tab vào tabbedPane
        tabbedPane.addTab("Phân Quyền Hệ Thống", splitPane);
        tabbedPane.addTab("Quản Lý Tài Chính", financePanel);

        // 4. Thêm tabbedPane vào Frame thay vì splitPane
        this.add(tabbedPane);

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
        if (accountList != null) {
            for (AccountDTO acc : accountList) {
                tableModel.addRow(new Object[]{acc.getAccountId(), acc.getUsername(), acc.getFullName(), acc.getStatus()});
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}