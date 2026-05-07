package com.mycompany.myapp.view.screens;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// ==========================================
// 1. MAIN FRAME / DASHBOARD PANEL
// ==========================================
public class TeacherDashboardUI extends JPanel {

    public TeacherDashboardUI() {
        // Thiết lập Anti-aliasing cho toàn cục
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F7FA")); // Background app màu xám siêu nhạt

        // Lấy thông tin user (Mô phỏng SessionStore)
        String sessionName = "Nguyễn Duy Nam";
        try {
            if (com.mycompany.myapp.utils.SessionStore.getFullName() != null) {
                sessionName = com.mycompany.myapp.utils.SessionStore.getFullName();
            }
        } catch (Exception e) {} // Bỏ qua lỗi nếu SessionStore chưa sẵn sàng

        // NORTH: Header Gradient
        add(new HeaderPanel(sessionName), BorderLayout.NORTH);

        // CENTER: Main Card Wrapper
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Nền trắng, bo góc, viền xám nhạt
        JPanel mainCard = new JPanel(new BorderLayout());
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
            new DashboardRoundedBorder(12, Color.decode("#E5E7EB")),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // TẠO TABS
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new ModernTabbedPaneUI()); // Áp dụng Custom Tab UI
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("Điểm Danh Học Viên", new AttendancePanel());
        tabbedPane.addTab("Nhập Điểm", new GradingPanel());

        mainCard.add(tabbedPane, BorderLayout.CENTER);
        centerWrapper.add(mainCard, BorderLayout.CENTER);

        add(centerWrapper, BorderLayout.CENTER);
    }
}

// ==========================================
// 2. HEADER PANEL (GRADIENT)
// ==========================================
class HeaderPanel extends JPanel {
    private String userName;

    public HeaderPanel(String userName) {
        this.userName = userName;
        setPreferredSize(new Dimension(0, 70));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel lblTitle = new JLabel("Bảng Điều Khiển Giáo Viên");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSession = new JLabel("Phiên làm việc: " + userName);
        lblSession.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSession.setForeground(new Color(255, 255, 255, 200));

        add(lblTitle, BorderLayout.WEST);
        add(lblSession, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Gradient: #4F46E5 -> #7C3AED
        GradientPaint gp = new GradientPaint(0, 0, Color.decode("#4F46E5"), getWidth(), 0, Color.decode("#7C3AED"));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}

// ==========================================
// 3. PANELS CHỨC NĂNG (TABS)
// ==========================================
class AttendancePanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public AttendancePanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // FILTER BAR
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filterBar.setOpaque(false);

        JLabel lblClass = new JLabel("Chọn lớp:");
        lblClass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblClass.setForeground(Color.decode("#111827"));

        JComboBox<String> cbClass = new JComboBox<>(new String[]{"Java Backend K1", "Lập trình C++ K2"});
        cbClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbClass.setPreferredSize(new Dimension(200, 36));
        cbClass.setBackground(Color.WHITE);

        JButton btnSave = new RoundedButton("Lưu điểm danh", "#4F46E5", Color.WHITE);
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Lưu điểm danh thành công!"));

        filterBar.add(lblClass);
        filterBar.add(cbClass);
        filterBar.add(btnSave); 

        // TABLE
        String[] cols = {"Mã HV", "Họ và Tên", "Trạng Thái", "Ghi chú"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c >= 2; }
        };
        table = new JTable(model);
        TableConfigurator.applyModernStyle(table);
        TableConfigurator.setColumnWidths(table);

        // ÁP DỤNG STATUS BADGE RENDERER VÀ EDITOR CHO CỘT TRẠNG THÁI
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Có mặt", "Vắng", "Trễ"});
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(statusCombo));

        // Mock data
        model.addRow(new Object[]{"HV001", "Nguyễn Duy Nam", "Có mặt", ""});
        model.addRow(new Object[]{"HV002", "Trần Thị B", "Vắng", "Bệnh"});
        model.addRow(new Object[]{"HV003", "Lê Văn C", "Trễ", ""});

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E7EB")));
        scroll.getViewport().setBackground(Color.WHITE);

        add(filterBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }
}

class GradingPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public GradingPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // FILTER BAR
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filterBar.setOpaque(false);

        JLabel lblClass = new JLabel("Chọn lớp:");
        lblClass.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JComboBox<String> cbClass = new JComboBox<>(new String[]{"Java Backend K1", "Lập trình C++ K2"});
        cbClass.setPreferredSize(new Dimension(200, 36));

        JButton btnSave = new RoundedButton("Lưu bảng điểm", "#4F46E5", Color.WHITE);

        filterBar.add(lblClass);
        filterBar.add(cbClass);
        filterBar.add(btnSave);

        // TABLE
        String[] cols = {"Mã HV", "Họ và Tên", "Điểm Chuyên Cần", "Điểm Thi"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c >= 2; }
        };
        table = new JTable(model);
        TableConfigurator.applyModernStyle(table);
        TableConfigurator.setColumnWidths(table);

        // Mock data
        model.addRow(new Object[]{"HV001", "Nguyễn Duy Nam", "9.0", "8.5"});
        model.addRow(new Object[]{"HV002", "Trần Thị B", "", ""});

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E7EB")));
        scroll.getViewport().setBackground(Color.WHITE);

        add(filterBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }
}

// ==========================================
// 4. CUSTOM UI COMPONENTS VÀ TABLE THỰC TẾ
// ==========================================

class TableConfigurator {
    public static void applyModernStyle(JTable table) {
        table.setRowHeight(46); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // ==========================================
        // FIX 1: LƯỚI CHUẨN EXCEL
        // Đặt khoảng cách 1x1 để nền ô không đè mất đường lưới
        // ==========================================
        table.setShowGrid(true); 
        table.setGridColor(Color.decode("#D1D5DB")); // Màu viền đậm hơn xíu để dễ nhìn
        table.setIntercellSpacing(new Dimension(1, 1)); // QUAN TRỌNG: Phải là 1, 1
        table.setFillsViewportHeight(true); 
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        table.setSelectionBackground(Color.decode("#F3F4F6"));
        table.setSelectionForeground(Color.decode("#111827"));

        // Click ra ngoài để bỏ chọn hàng
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (table.rowAtPoint(e.getPoint()) == -1) {
                    table.clearSelection(); 
                }
            }
        });

        // Custom Header
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 46)); 
        header.setBackground(Color.WHITE);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setBackground(Color.decode("#F9FAFB"));
                l.setForeground(Color.decode("#6B7280"));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.decode("#D1D5DB")));
                l.setHorizontalAlignment((c == 0 || c >= 2) ? SwingConstants.CENTER : SwingConstants.LEFT);
                return l;
            }
        });

        // ==========================================
        // FIX 2: TẮT IN ĐẬM/FOCUS MẶC ĐỊNH
        // ==========================================
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                // Tham số thứ 4 luôn ép là 'false' để triệt tiêu cái khung viền in đậm rác của Swing
                Component comp = super.getTableCellRendererComponent(t, v, isS, false, r, c); 
                
                if (!isS) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : Color.decode("#F9FAFB"));
                }
                
                if (comp instanceof JLabel) {
                    JLabel l = (JLabel) comp;
                    if (c == 0 || c >= 2) {
                        l.setHorizontalAlignment(SwingConstants.CENTER);
                        l.setBorder(null); 
                    } else {
                        l.setHorizontalAlignment(SwingConstants.LEFT);
                        l.setBorder(new EmptyBorder(0, 16, 0, 0)); 
                    }
                }
                return comp;
            }
        });
    }

    public static void setColumnWidths(JTable table) {
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
    }
}

// --- RENDERER BADGE TRẠNG THÁI BO TRÒN (PILL BADGE) ---
class StatusCellRenderer extends DefaultTableCellRenderer {
    private JPanel panel = new JPanel(new GridBagLayout()); 
    private StatusBadge badge = new StatusBadge();

    public StatusCellRenderer() {
        panel.setOpaque(true);
        panel.add(badge);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String status = (value != null) ? value.toString() : "";
        badge.setText(status);

        if ("Có mặt".equals(status)) {
            badge.setColors(Color.decode("#15803D"), Color.decode("#DCFCE7"));
        } else if ("Vắng".equals(status)) {
            badge.setColors(Color.decode("#B91C1C"), Color.decode("#FEE2E2"));
        } else if ("Trễ".equals(status)) {
            badge.setColors(Color.decode("#B45309"), Color.decode("#FEF3C7"));
        } else {
            badge.setColors(Color.decode("#6B7280"), Color.decode("#F3F4F6"));
        }

        // Đồng bộ màu nền để không bị lệch màu Zebra (Trắng / Xám nhạt)
        if (isSelected) {
            panel.setBackground(Color.decode("#F3F4F6"));
        } else {
            panel.setBackground(row % 2 == 0 ? Color.WHITE : Color.decode("#F9FAFB"));
        }

        return panel;
    }

    class StatusBadge extends JLabel {
        private Color bgColor = Color.WHITE;
        public StatusBadge() {
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(4, 12, 4, 12));
            setOpaque(false); 
        }
        public void setColors(Color fg, Color bg) {
            setForeground(fg);
            this.bgColor = bg;
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

// --- CUSTOM TAB UI ---
class ModernTabbedPaneUI extends BasicTabbedPaneUI {
    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(10, 20, 10, 20);
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, w, h);
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(Color.decode("#E5E7EB"));
        g2.drawLine(x, y + h - 1, x + w, y + h - 1);

        if (isSelected) {
            g2.setColor(Color.decode("#4F46E5")); 
            g2.fillRect(x, y + h - 3, w, 3);
        }
        g2.dispose();
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (isSelected) {
            g2.setFont(font.deriveFont(Font.BOLD));
            g2.setColor(Color.decode("#4F46E5")); 
        } else {
            g2.setFont(font.deriveFont(Font.PLAIN));
            g2.setColor(Color.decode("#6B7280")); 
        }
        super.paintText(g, tabPlacement, g2.getFont(), metrics, tabIndex, title, textRect, isSelected);
    }
    
    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
}

// --- BO GÓC PANEL ---
class DashboardRoundedBorder implements Border {
    private int radius;
    private Color color;

    public DashboardRoundedBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
    }

    public boolean isBorderOpaque() { return true; }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}

// --- NÚT BẤM CUSTOM ---
class RoundedButton extends JButton {
    private Color bgColor;

    public RoundedButton(String text, String hexColor, Color fgColor) {
        super(text);
        this.bgColor = Color.decode(hexColor);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(fgColor);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(140, 36));

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                bgColor = Color.decode(hexColor).darker();
                repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                bgColor = Color.decode(hexColor);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }
}