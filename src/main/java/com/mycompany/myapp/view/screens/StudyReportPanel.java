package com.mycompany.myapp.view.screens; // NHỚ ĐỔI PACKAGE

import com.mycompany.myapp.model.StudyReportDTO;
import com.mycompany.myapp.repository.StudyReportDAO;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class StudyReportPanel extends JPanel {

    private JTable tblReports;
    private DefaultTableModel tableModel;
    private StudyReportDAO reportDAO = new StudyReportDAO();
    private List<StudyReportDTO> allData; // Lưu cache toàn bộ dữ liệu từ DB
    
    private JLabel lblTotalStudents, lblPassed, lblFailed;
    private JComboBox<String> cbxClassFilter, cbxResultFilter;
    private JTextField txtSearchStudent;
    private boolean isDataLoaded = false; // Cờ kiểm soát việc load combo box

    public StudyReportPanel() {
        initComponents();
        loadDataFromDB(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(248, 250, 252)); 
        setBorder(new EmptyBorder(25, 30, 30, 30));

        // 1. TOP CARDS
        lblTotalStudents = new JLabel("0");
        lblPassed = new JLabel("0");
        lblFailed = new JLabel("0");

        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setBackground(new Color(248, 250, 252));
        pnlCards.add(createStatCard("Tổng số học viên", lblTotalStudents, Color.BLACK));
        pnlCards.add(createStatCard("Học viên ĐẠT", lblPassed, new Color(34, 197, 94)));  
        pnlCards.add(createStatCard("Học viên CHƯA ĐẠT", lblFailed, new Color(239, 68, 68)));    

        // ==========================================
        // 2. MIDDLE TOOLBAR (Cấu trúc: Title trên, Lọc trái, Tìm phải)
        // ==========================================
        JPanel pnlToolbar = new JPanel(new BorderLayout(0, 10)); // Gap dọc 10px giữa 2 dòng
        pnlToolbar.setBackground(new Color(248, 250, 252));
        pnlToolbar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // --- DÒNG 1: Tiêu đề đứng độc lập phía trên ---
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setBackground(new Color(248, 250, 252));
        
        JLabel lblTitle = new JLabel("BÁO CÁO HỌC TẬP"); // Đã viết hoa theo yêu cầu
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlTitle.add(lblTitle);

        // --- DÒNG 2: Khu vực công cụ (Chia làm 2 ngả Trái/Phải) ---
        JPanel pnlControls = new JPanel(new BorderLayout());
        pnlControls.setBackground(new Color(248, 250, 252));

        // 2.1 Bên Trái: Nhóm Bộ Lọc
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilters.setBackground(new Color(248, 250, 252));
        
        cbxClassFilter = new JComboBox<>();
        cbxClassFilter.setPreferredSize(new Dimension(180, 35));
        cbxClassFilter.addActionListener(e -> applyFilters());
        
        cbxResultFilter = new JComboBox<>(new String[]{"Tất cả kết quả", "ĐẠT", "CHƯA ĐẠT"});
        cbxResultFilter.setPreferredSize(new Dimension(140, 35));
        cbxResultFilter.addActionListener(e -> applyFilters());

        pnlFilters.add(new JLabel("Lớp học:")); // Bỏ khoảng trắng thừa cho sát lề
        pnlFilters.add(cbxClassFilter);
        pnlFilters.add(new JLabel("  Kết quả:"));
        pnlFilters.add(cbxResultFilter);

        // 2.2 Bên Phải: Nhóm Tìm kiếm & Nút
        JPanel pnlSearchAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlSearchAction.setBackground(new Color(248, 250, 252));

        txtSearchStudent = new JTextField(15);
        txtSearchStudent.setText("Tìm họ tên học viên...");
        txtSearchStudent.setPreferredSize(new Dimension(200, 35));
        txtSearchStudent.setForeground(Color.GRAY);
        txtSearchStudent.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearchStudent.getText().equals("Tìm họ tên học viên...")) {
                    txtSearchStudent.setText(""); txtSearchStudent.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearchStudent.getText().isEmpty()) {
                    txtSearchStudent.setText("Tìm họ tên học viên..."); txtSearchStudent.setForeground(Color.GRAY);
                }
            }
        });

        JButton btnSearch = createStyledButton("Tìm", new Color(59, 130, 246));
        btnSearch.setPreferredSize(new Dimension(70, 35));
        btnSearch.addActionListener(e -> applyFilters());
        
        JButton btnExport = createStyledButton("Xuất Excel", new Color(34, 197, 94));
        btnExport.setPreferredSize(new Dimension(110, 35));

        pnlSearchAction.add(txtSearchStudent);
        pnlSearchAction.add(btnSearch);
        pnlSearchAction.add(btnExport);

        // Gắn Trái và Phải vào Dòng 2
        pnlControls.add(pnlFilters, BorderLayout.WEST);
        pnlControls.add(pnlSearchAction, BorderLayout.EAST);

        // Cuối cùng: Gắn Dòng 1 (Title) và Dòng 2 (Controls) vào Toolbar tổng
        pnlToolbar.add(pnlTitle, BorderLayout.NORTH);
        pnlToolbar.add(pnlControls, BorderLayout.CENTER);

        // 3. BẢNG DỮ LIỆU
        String[] columns = {"STT", "Mã HV", "Họ và Tên", "Lớp Học", "Điểm TB", "Xếp Loại", "Kết Quả"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblReports = new JTable(tableModel);
        tblReports.setRowHeight(40);
        tblReports.setShowGrid(true);
        tblReports.setGridColor(new Color(226, 232, 240));

        JTableHeader header = tblReports.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        for (int i = 0; i < tblReports.getColumnModel().getColumnCount(); i++) {
            tblReports.getColumnModel().getColumn(i).setHeaderRenderer((table, value, isSelected, hasFocus, row, col) -> {
                JLabel label = new JLabel(value.toString());
                label.setOpaque(true);
                label.setBackground(new Color(37, 99, 235)); 
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
                return label;
            });
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblReports.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        tblReports.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); 
        tblReports.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 
        tblReports.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); 
        tblReports.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); 

        DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer();
        paddingRenderer.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        tblReports.getColumnModel().getColumn(2).setCellRenderer(paddingRenderer); 
        tblReports.getColumnModel().getColumn(3).setCellRenderer(paddingRenderer); 

        JScrollPane scrollPane = new JScrollPane(tblReports);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlNorth = new JPanel(new BorderLayout(0, 20));
        pnlNorth.setBackground(new Color(248, 250, 252));
        pnlNorth.add(pnlCards, BorderLayout.NORTH); pnlNorth.add(pnlToolbar, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH); add(scrollPane, BorderLayout.CENTER);
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ LOGIC DỮ LIỆU
    // ==========================================
    
    // Gọi DAO để kéo dữ liệu từ Oracle 1 lần duy nhất
    private void loadDataFromDB() {
        allData = reportDAO.getAllReports();
        
        Set<String> classNames = new HashSet<>();
        int totalDB = 0, passedDB = 0, failedDB = 0; // Biến đếm cho toàn bộ DB
        
        for (StudyReportDTO r : allData) {
            classNames.add(r.getClassName());
            // Tính toán thống kê gốc tại đây
            totalDB++;
            if (r.getResult().equals("ĐẠT")) passedDB++;
            else failedDB++;
        }
        
        // Gắn số liệu lên thẻ Card ngay lập tức (Số này sẽ đứng yên không bị bộ lọc làm ảnh hưởng)
        lblTotalStudents.setText(String.valueOf(totalDB));
        lblPassed.setText(String.valueOf(passedDB));
        lblFailed.setText(String.valueOf(failedDB));
        
        cbxClassFilter.addItem("--- Tất cả lớp học ---");
        for (String cName : classNames) {
            cbxClassFilter.addItem(cName);
        }
        
        isDataLoaded = true;
        applyFilters(); 
    }

    // Hàm xử lý lọc đa tầng
    private void applyFilters() {
        if (!isDataLoaded) return;
        tableModel.setRowCount(0); 
        
        String keyword = txtSearchStudent.getText().trim().toLowerCase();
        if (keyword.equals("tìm họ tên học viên...")) keyword = "";
        
        String classFilter = cbxClassFilter.getSelectedItem().toString();
        String resultFilter = cbxResultFilter.getSelectedItem().toString();
        
        int stt = 1;
        
        for (StudyReportDTO r : allData) {
            boolean matchKeyword = keyword.isEmpty() || r.getFullName().toLowerCase().contains(keyword) || r.getStudentId().toLowerCase().contains(keyword);
            boolean matchClass = classFilter.equals("--- Tất cả lớp học ---") || r.getClassName().equals(classFilter);
            boolean matchResult = resultFilter.equals("Tất cả kết quả") || r.getResult().equals(resultFilter);
            
            if (matchKeyword && matchClass && matchResult) {
                tableModel.addRow(new Object[]{
                    stt++, 
                    r.getStudentId(), 
                    r.getFullName(), 
                    r.getClassName(), 
                    r.getAverageScore(), 
                    r.getClassification(), 
                    r.getResult()
                });
            }
        }
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(100, 116, 139));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(accentColor);
        card.add(t, BorderLayout.NORTH); card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(false);
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
        JFrame f = new JFrame("Gói 6 - Báo Cáo Học Tập");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1200, 750); f.setLocationRelativeTo(null);
        f.add(new StudyReportPanel()); f.setVisible(true);
    }
}