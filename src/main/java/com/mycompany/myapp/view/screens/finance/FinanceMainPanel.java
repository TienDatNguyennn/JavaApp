package com.mycompany.myapp.view.screens.finance;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class FinanceMainPanel extends JPanel {

    private static final Color SIDEBAR_BG     = new Color(30,  27,  38);
    private static final Color SIDEBAR_HOVER  = new Color(50,  46,  62);
    private static final Color SIDEBAR_ACTIVE = new Color(108, 92, 231);
    private static final Color TEXT_LIGHT     = new Color(200, 195, 220);
    private static final Color TEXT_DIM       = new Color(120, 115, 140);
    private static final Color SECTION_CLR    = new Color(90,  85, 110);

    // Khai báo các Panel thành phần
    private JPanel paymentPanel;
    private JPanel managePanel;
    private JPanel lookupPanel;
    private JPanel issuePanel;
    private JPanel payrollPanel;

    private JPanel  contentArea;
    private JButton activeBtn;

    public FinanceMainPanel() {
        setLayout(new BorderLayout());
        // Khởi tạo Content Area trước để tránh lỗi NullPointerException khi setActive được gọi
        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(new Color(248, 249, 250));
        
        buildContent(); // Khởi tạo các panel con
        
        add(buildSidebar(), BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(215, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SIDEBAR_BG);
        header.setBorder(new EmptyBorder(20, 16, 16, 16));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lblTitle = new JLabel("Tài chính");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.NORTH);
        
        sidebar.add(header);
        sidebar.add(makeSep());

        sidebar.add(sectionLabel("HỌC PHÍ"));
        JButton btnPayment = navBtn("   Ghi nhận thanh toán", "payment");
        JButton btnManage  = navBtn("   Quản lý học phí",      "manage");
        sidebar.add(btnPayment);
        sidebar.add(btnManage);

        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sectionLabel("NHÂN SỰ"));
        JButton btnPayroll = navBtn("   Tính lương nhân viên", "payroll");
        sidebar.add(btnPayroll);

        sidebar.add(Box.createVerticalGlue());
        
        // Mặc định chọn nút đầu tiên
        setActive(btnPayment, "payment");
        
        return sidebar;
    }

    private void buildContent() {
        try {
            // Khởi tạo các lớp con. 
            // LƯU Ý: Phải đảm bảo các file PaymentPanel.java... đã có sẵn và không có lỗi cú pháp.
            paymentPanel = new PaymentPanel();
            managePanel  = new ManageInvoicePanel();
            lookupPanel  = new LookupPanel();
            issuePanel   = new InvoiceIssuePanel();
            payrollPanel = new PayrollPanel();

            contentArea.add(paymentPanel, "payment");
            contentArea.add(managePanel,  "manage");
            contentArea.add(lookupPanel,  "lookup");
            contentArea.add(issuePanel,   "issue");
            contentArea.add(payrollPanel, "payroll");
        } catch (Exception e) {
            // Nếu các file con chưa xong, tạo Panel tạm để không bị lỗi Build
            JPanel errorPanel = new JPanel();
            errorPanel.add(new JLabel("Đang tải dữ liệu hoặc lỗi file thành phần..."));
            contentArea.add(errorPanel, "error");
            System.err.println("Lỗi khởi tạo các panel con: " + e.getMessage());
        }
    }

    // Các hàm phụ trợ (giữ nguyên logic của bạn nhưng sửa lại lỗi hiển thị)
    private JButton navBtn(String label, String key) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> setActive(btn, key));
        return btn;
    }

    private void setActive(JButton btn, String key) {
        if (activeBtn != null) {
            activeBtn.setForeground(TEXT_LIGHT);
            activeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        activeBtn = btn;

        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, key);
    }

    private JPanel makeSep() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(50, 46, 62));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(SECTION_CLR);
        l.setBorder(new EmptyBorder(10, 16, 4, 0));
        return l;
    }
}