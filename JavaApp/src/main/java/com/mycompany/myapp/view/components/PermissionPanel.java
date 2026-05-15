/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Tien Dat
 */
package com.mycompany.myapp.view.components;


import com.mycompany.myapp.model.AccountDTO;
import com.mycompany.myapp.model.FunctionPermission;
import com.mycompany.myapp.model.RoleGroup;
import com.mycompany.myapp.service.PermissionService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionPanel extends JPanel {
    private JLabel lblTitle;
    private JRadioButton radGroup, radRole;
    private JPanel cardPanel;
    private JList<RoleGroup> listGroups;
    private JTable matrixTable;
    private MatrixTableModel matrixModel;
    private CustomButton btnSave, btnReset;

    private AccountDTO currentAccount;
    private PermissionService service = new PermissionService();
    private List<FunctionPermission> currentMatrixData;

    public PermissionPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadInitialData();
        toggleForm(false);
    }

    private void initUI() {
        lblTitle = new JLabel("Phân quyền cho: (Chưa chọn tài khoản)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);

        // Radio Buttons
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRadio.setOpaque(false);
        radGroup = new JRadioButton("Cấp theo Nhóm (Role Group)", true);
        radRole = new JRadioButton("Cấp Quyền lẻ (Ma trận Function)");
        radGroup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        radRole.setFont(new Font("Segoe UI", Font.BOLD, 14));
        radGroup.setOpaque(false); radRole.setOpaque(false);
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(radGroup); bg.add(radRole);
        pnlRadio.add(radGroup); pnlRadio.add(radRole);
        centerPanel.add(pnlRadio, BorderLayout.NORTH);

        // CardLayout
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setOpaque(false);

        // Card 1: Group List
        listGroups = new JList<>(new DefaultListModel<>());
        listGroups.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cardPanel.add(new JScrollPane(listGroups), "GROUP");

        // Card 2: Matrix Table (Cho bảng ROLE)
        currentMatrixData = new ArrayList<>();
        matrixModel = new MatrixTableModel(currentMatrixData);
        matrixTable = new JTable(matrixModel);
        matrixTable.setRowHeight(35);
        matrixTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        matrixTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardPanel.add(new JScrollPane(matrixTable), "MATRIX");

        centerPanel.add(cardPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);
        btnReset = new CustomButton("Khôi phục");
        btnReset.setColors(new Color(149, 165, 166), new Color(127, 140, 141));
        btnSave = new CustomButton("Lưu phân quyền");
        
        pnlBottom.add(btnReset); pnlBottom.add(btnSave);
        add(pnlBottom, BorderLayout.SOUTH);

        // Events
        radGroup.addActionListener(e -> ((CardLayout) cardPanel.getLayout()).show(cardPanel, "GROUP"));
        radRole.addActionListener(e -> ((CardLayout) cardPanel.getLayout()).show(cardPanel, "MATRIX"));
        btnSave.addActionListener(e -> processSave());
    }

    private void loadInitialData() {
        // Tải danh sách Actor Groups
        DefaultListModel<RoleGroup> model = (DefaultListModel<RoleGroup>) listGroups.getModel();
        for (RoleGroup g : service.getActorRoleGroups()) model.addElement(g);
        
        // Tải danh sách Function gốc cho Ma trận
        currentMatrixData.clear();
        currentMatrixData.addAll(service.getAllFunctionsForMatrix());
        matrixModel.fireTableDataChanged();
    }

    public void bindAccount(AccountDTO acc) {
        this.currentAccount = acc;
        lblTitle.setText("Phân quyền cho: " + acc.getUsername() + " - " + acc.getFullName());
        toggleForm(true);
        
        // Highlight Nhóm quyền đã có
        listGroups.clearSelection();
        DefaultListModel<RoleGroup> model = (DefaultListModel<RoleGroup>) listGroups.getModel();
        List<Integer> indicesToSelect = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            if (acc.getAssignedGroupIds().contains(model.getElementAt(i).getId())) {
                indicesToSelect.add(i);
            }
        }
        int[] arr = indicesToSelect.stream().mapToInt(i->i).toArray();
        listGroups.setSelectedIndices(arr);
        
        // Reset Matrix (Thực tế sẽ load customPermissions từ acc vào currentMatrixData)
        loadInitialData(); 
    }

    private void toggleForm(boolean active) {
        radGroup.setEnabled(active); radRole.setEnabled(active);
        listGroups.setEnabled(active); matrixTable.setEnabled(active);
        btnSave.setEnabled(active); btnReset.setEnabled(active);
    }

    private void processSave() {
        if (currentAccount == null) return;
        btnSave.setText("Đang xử lý...");
        toggleForm(false);

        boolean isMatrixMode = radRole.isSelected();
        List<Integer> selectedGroups = new ArrayList<>();
        if (!isMatrixMode) {
            for (RoleGroup g : listGroups.getSelectedValuesList()) selectedGroups.add(g.getId());
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isMatrixMode) {
                    service.saveMatrixPermissions(currentAccount.getAccountId(), currentMatrixData);
                } else {
                    if(selectedGroups.isEmpty()) throw new Exception("Vui lòng chọn ít nhất 1 nhóm!");
                    service.saveGroupAssignment(currentAccount.getAccountId(), selectedGroups);
                }
                return null;
            }
            @Override
            protected void done() {
                btnSave.setText("Lưu phân quyền");
                toggleForm(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(PermissionPanel.this, "Cập nhật quyền sử dụng thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PermissionPanel.this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Inner class tạo Model cho JTable tự động render Checkbox
    private class MatrixTableModel extends AbstractTableModel {
        private String[] columns = {"Chức năng hệ thống", "Xem", "Thêm", "Sửa", "Xóa"};
        private List<FunctionPermission> data;

        public MatrixTableModel(List<FunctionPermission> data) { this.data = data; }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }
        
        // Cực kỳ quan trọng: Trả về Boolean.class để JTable hiển thị Checkbox
        @Override public Class<?> getColumnClass(int col) {
            return (col == 0) ? String.class : Boolean.class;
        }
        
        @Override public boolean isCellEditable(int row, int col) {
            return col > 0; // Cột 0 (Tên chức năng) không được sửa
        }

        @Override public Object getValueAt(int row, int col) {
            FunctionPermission p = data.get(row);
            switch (col) {
                case 0: return p.getFunctionName();
                case 1: return p.isCanView();
                case 2: return p.isCanAdd();
                case 3: return p.isCanEdit();
                case 4: return p.isCanDelete();
                default: return null;
            }
        }

        @Override public void setValueAt(Object value, int row, int col) {
            FunctionPermission p = data.get(row);
            boolean val = (Boolean) value;
            switch (col) {
                case 1: p.setCanView(val); break;
                case 2: p.setCanAdd(val); break;
                case 3: p.setCanEdit(val); break;
                case 4: p.setCanDelete(val); break;
            }
            fireTableCellUpdated(row, col);
        }
    }
}