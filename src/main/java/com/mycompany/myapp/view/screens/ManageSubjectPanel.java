package com.mycompany.myapp.view.screens; // NHỚ ĐỔI PACKAGE

import com.mycompany.myapp.model.SubjectDTO;
import com.mycompany.myapp.repository.SubjectDAO;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class ManageSubjectPanel extends JPanel {

    private JTable tblSubjects;
    private DefaultTableModel tableModel;
    private SubjectDAO subjectDAO = new SubjectDAO();

    private JLabel lblTotal, lblActive, lblInactive;
    private JComboBox<String> cbxFilterStatus;
    private JTextField txtSearch;

    public ManageSubjectPanel() {
        initComponents();
        loadData(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252)); 
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // 1. TOP CARDS
        lblTotal = new JLabel("0"); lblActive = new JLabel("0"); lblInactive = new JLabel("0");
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setBackground(new Color(248, 250, 252));
        pnlCards.add(createStatCard("Tổng môn học", lblTotal, new Color(100, 116, 139))); 
        pnlCards.add(createStatCard("Đang giảng dạy", lblActive, new Color(34, 197, 94)));  
        pnlCards.add(createStatCard("Ngừng đào tạo", lblInactive, new Color(239, 68, 68)));    

        // ==========================================
        // 2. MIDDLE TOOLBAR (Cấu trúc: Title trên, Lọc trái, Công cụ phải)
        // ==========================================
        JPanel pnlToolbar = new JPanel(new BorderLayout(0, 10)); // Gap dọc 10px
        pnlToolbar.setBackground(new Color(248, 250, 252));
        pnlToolbar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // --- DÒNG 1: Tiêu đề đứng độc lập ---
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setBackground(new Color(248, 250, 252));
        
        JLabel lblTitle = new JLabel("QUẢN LÝ MÔN HỌC"); // Viết hoa đồng bộ với Gói 6
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlTitle.add(lblTitle);

        // --- DÒNG 2: Khu vực công cụ (Trái/Phải) ---
        JPanel pnlControls = new JPanel(new BorderLayout());
        pnlControls.setBackground(new Color(248, 250, 252));

        // 2.1 Bên Trái: Nhóm Bộ Lọc
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilters.setBackground(new Color(248, 250, 252));
        
        cbxFilterStatus = new JComboBox<>(new String[]{"Tất cả", "Đang giảng dạy", "Ngừng đào tạo"});
        cbxFilterStatus.setPreferredSize(new Dimension(150, 35));
        cbxFilterStatus.addActionListener(e -> {
            txtSearch.setText("Nhập tên môn học...");
            txtSearch.setForeground(Color.GRAY);
            loadData();
        });
        
        pnlFilters.add(new JLabel("Lọc trạng thái:"));
        pnlFilters.add(cbxFilterStatus);

        // 2.2 Bên Phải: Nhóm Tìm kiếm & Nút chức năng
        JPanel pnlSearchAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearchAction.setBackground(new Color(248, 250, 252));

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(180, 35));
        txtSearch.setText("Nhập tên môn học...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Nhập tên môn học...")) {
                    txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY); txtSearch.setText("Nhập tên môn học...");
                }
            }
        });

        JButton btnSearch = createStyledButton("Tìm", new Color(59, 130, 246));
        JButton btnAdd = createStyledButton("+ Thêm mới", new Color(34, 197, 94));
        btnAdd.setPreferredSize(new Dimension(130, 35));
        JButton btnEdit = createStyledButton("Sửa", new Color(245, 158, 11));     
        JButton btnDelete = createStyledButton("Xóa", new Color(239, 68, 68));    

        // Gọi hàm gắn sự kiện đã có sẵn bên dưới
        setupActionListeners(btnAdd, btnEdit, btnDelete); 

        pnlSearchAction.add(txtSearch);
        pnlSearchAction.add(btnSearch);
        pnlSearchAction.add(btnAdd);
        pnlSearchAction.add(btnEdit);
        pnlSearchAction.add(btnDelete);

        // Gắn Trái và Phải vào Dòng 2
        pnlControls.add(pnlFilters, BorderLayout.WEST);
        pnlControls.add(pnlSearchAction, BorderLayout.EAST);

        // Cuối cùng: Gắn Dòng 1 (Title) và Dòng 2 (Controls) vào Toolbar tổng
        pnlToolbar.add(pnlTitle, BorderLayout.NORTH);
        pnlToolbar.add(pnlControls, BorderLayout.CENTER);

        // 3. BẢNG DỮ LIỆU
        tableModel = new DefaultTableModel(new String[]{"STT", "Mã Môn", "Tên Môn Học", "Mô Tả", "Trạng Thái"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblSubjects = new JTable(tableModel);
        tblSubjects.setRowHeight(40);
        tblSubjects.setShowGrid(true); // Hiển thị đường kẻ bảng
        tblSubjects.setGridColor(new Color(226, 232, 240));

        // --- CẤU HÌNH HEADER MỚI (FIX LỖI VIỀN MỜ + CĂN GIỮA) ---
        JTableHeader header = tblSubjects.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        for (int i = 0; i < tblSubjects.getColumnModel().getColumnCount(); i++) {
            tblSubjects.getColumnModel().getColumn(i).setHeaderRenderer((table, value, isSelected, hasFocus, row, col) -> {
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(new Color(37, 99, 235)); 
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
                return label;
            });
        }

        // Căn giữa STT, Mã môn và Trạng thái
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblSubjects.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblSubjects.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblSubjects.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Thêm Padding cho Tên môn và Mô tả
        DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer();
        paddingRenderer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        tblSubjects.getColumnModel().getColumn(2).setCellRenderer(paddingRenderer);
        tblSubjects.getColumnModel().getColumn(3).setCellRenderer(paddingRenderer);

        JScrollPane scrollPane = new JScrollPane(tblSubjects);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 20));
        pnlNorth.setBackground(new Color(248, 250, 252));
        pnlNorth.add(pnlCards, BorderLayout.NORTH); pnlNorth.add(pnlToolbar, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH); add(scrollPane, BorderLayout.CENTER);
    }

    private void setupActionListeners(JButton btnAdd, JButton btnEdit, JButton btnDelete) {
        btnAdd.addActionListener(e -> {
            SubjectDialog dialog = new SubjectDialog(SwingUtilities.getWindowAncestor(this), "Thêm Môn Học Mới", null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                if (subjectDAO.insertSubject(dialog.getSubjectData())) {
                    JOptionPane.showMessageDialog(this, "Đã thêm thành công!"); loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = tblSubjects.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn môn cần sửa!"); return; }
            SubjectDTO s = new SubjectDTO(Integer.parseInt(tableModel.getValueAt(row, 1).toString()), 
                    tableModel.getValueAt(row, 2).toString(), tableModel.getValueAt(row, 3).toString(), "");
            SubjectDialog dialog = new SubjectDialog(SwingUtilities.getWindowAncestor(this), "Sửa Môn Học", s);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                if (subjectDAO.updateSubject(dialog.getSubjectData())) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật!"); loadData();
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = tblSubjects.getSelectedRow();
            if (row == -1) return;
            if (JOptionPane.showConfirmDialog(this, "Ngừng đào tạo môn này?", "Xóa", JOptionPane.YES_NO_OPTION) == 0) {
                if (subjectDAO.deleteSubject(Integer.parseInt(tableModel.getValueAt(row, 1).toString()))) loadData();
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0); 
        List<SubjectDTO> list = subjectDAO.getAllSubjects();

        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.equals("nhập tên môn học...")) keyword = "";
        String filterStatus = cbxFilterStatus.getSelectedItem().toString();

        int countTotal = 0, countActive = 0, countInactive = 0;
        int stt = 1;

        for (SubjectDTO s : list) {
            // FIX: Tính toán thống kê dựa trên TOÀN BỘ DB trước khi lọc
            countTotal++;
            if (s.getStatus().equals("Đang giảng dạy")) countActive++;
            else countInactive++;

            // FIX: Đã gỡ bỏ dòng code "tàng hình" (if status == ngừng đào tạo thì continue).
            // Áp dụng bộ lọc ComboBox một cách tự nhiên
            boolean matchKeyword = keyword.isEmpty() || s.getSubjectName().toLowerCase().contains(keyword);
            boolean matchStatus = filterStatus.equals("Tất cả") || s.getStatus().equals(filterStatus);

            if (matchKeyword && matchStatus) {
                tableModel.addRow(new Object[]{
                    stt++, 
                    s.getSubjectId(), 
                    s.getSubjectName(), 
                    s.getDescription(), 
                    s.getStatus() 
                });
            }
        }

        lblTotal.setText(String.valueOf(countTotal));
        lblActive.setText(String.valueOf(countActive));
        lblInactive.setText(String.valueOf(countInactive));
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color iconColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), new EmptyBorder(15, 20, 15, 20)));
        JLabel t = new JLabel(title); t.setForeground(new Color(100, 116, 139));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        card.add(t, BorderLayout.NORTH); card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(100, 35));
        // THÊM HIỆU ỨNG HOVER LÀM ĐẬM MÀU
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker()); // Làm đậm màu nền khi trỏ chuột vào
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg); // Trả lại màu gốc khi chuột rời đi
            }
        });
        return btn;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        JFrame f = new JFrame("SIS - Quản Lý Môn Học");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setSize(1100, 700);
        f.setLocationRelativeTo(null); f.add(new ManageSubjectPanel()); f.setVisible(true);
    }
}