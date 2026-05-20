package com.mycompany.myapp.view.screens.ThanhToan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FinanceMainPanel extends JPanel {

    private static final Color BG_PAGE = new Color(248, 250, 252);
    private static final Color SIDEBAR_BG = new Color(255, 255, 255);
    private static final Color SIDEBAR_BORDER = new Color(226, 232, 240);

    private static final Color TEXT_MAIN = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color TEXT_SOFT = new Color(148, 163, 184);

    private static final Color PRIMARY = new Color(108, 92, 231);
    private static final Color PRIMARY_DARK = new Color(83, 68, 207);
    private static final Color PRIMARY_SOFT = new Color(238, 234, 255);

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color GREEN_SOFT = new Color(220, 252, 231);

    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color ORANGE_SOFT = new Color(255, 237, 213);

    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_SOFT = new Color(219, 234, 254);

    private JPanel paymentPanel;
    private JPanel managePanel;
    private JPanel lookupPanel;
    private JPanel issuePanel;
    private JPanel payrollPanel;

    private JPanel contentArea;
    private JPanel sidebar;
    private JPanel sidebarContent;
    private NavButton activeBtn;

    private boolean collapsed = false;

    public FinanceMainPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PAGE);

        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(BG_PAGE);

        buildContent();

        add(buildSidebarShell(), BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel buildSidebarShell() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, SIDEBAR_BORDER));

        sidebarContent = new JPanel();
        sidebarContent.setLayout(new BoxLayout(sidebarContent, BoxLayout.Y_AXIS));
        sidebarContent.setBackground(SIDEBAR_BG);
        sidebarContent.setBorder(new EmptyBorder(18, 14, 18, 14));

        sidebarContent.add(buildBrandHeader());
        sidebarContent.add(Box.createVerticalStrut(18));

        sidebarContent.add(sectionLabel("Học phí"));
        NavButton btnPayment = navBtn("Ghi nhận thanh toán", "payment", "payment", GREEN, GREEN_SOFT);
        NavButton btnManage = navBtn("Quản lý học phí", "manage", "invoice", PRIMARY, PRIMARY_SOFT);
        sidebarContent.add(btnPayment);
        sidebarContent.add(Box.createVerticalStrut(8));
        sidebarContent.add(btnManage);

        sidebarContent.add(Box.createVerticalStrut(18));
        sidebarContent.add(sectionLabel("Tra cứu & phát hành"));
        NavButton btnLookup = navBtn("Tra cứu phiếu thu", "lookup", "search", BLUE, BLUE_SOFT);
        NavButton btnIssue = navBtn("Phát hành hóa đơn", "issue", "issue", ORANGE, ORANGE_SOFT);
        sidebarContent.add(btnLookup);
        sidebarContent.add(Box.createVerticalStrut(8));
        sidebarContent.add(btnIssue);

        sidebarContent.add(Box.createVerticalStrut(18));
        sidebarContent.add(sectionLabel("Nhân sự"));
        NavButton btnPayroll = navBtn("Tính lương nhân viên", "payroll", "payroll", PRIMARY, PRIMARY_SOFT);
        sidebarContent.add(btnPayroll);

        sidebarContent.add(Box.createVerticalGlue());
        sidebarContent.add(buildFooterHint());

        JScrollPane sidebarScroll = new JScrollPane(sidebarContent);
        sidebarScroll.setBorder(null);
        sidebarScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.getViewport().setBackground(SIDEBAR_BG);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(14);

        SidebarToggleButton toggleButton = new SidebarToggleButton();
        toggleButton.addActionListener(e -> toggleSidebar(toggleButton));

        sidebar.add(sidebarScroll, BorderLayout.CENTER);
        sidebar.add(toggleButton, BorderLayout.EAST);

        setActive(btnPayment, "payment");
        return sidebar;
    }

    private JPanel buildBrandHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel icon = new FinanceBrandIcon();
        icon.setPreferredSize(new Dimension(44, 44));

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.putClientProperty("hideWhenCollapsed", true);

        JLabel title = new JLabel("Tài chính");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Quản lý thu chi");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_MUTED);

        textBox.add(title);
        textBox.add(Box.createVerticalStrut(3));
        textBox.add(sub);

        header.add(icon, BorderLayout.WEST);
        header.add(textBox, BorderLayout.CENTER);

        return header;
    }

    private JPanel buildFooterHint() {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(true);
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new EmptyBorder(12, 12, 12, 12));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        footer.putClientProperty("hideWhenCollapsed", true);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(GREEN_SOFT);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(GREEN);
                g2.fillOval(getWidth() / 2 - 4, getHeight() / 2 - 4, 8, 8);

                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(30, 30));

        JLabel text = new JLabel("<html><div style='width:150px;'>Dữ liệu tài chính cần được kiểm tra trước khi lưu.</div></html>");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        text.setForeground(TEXT_MUTED);

        footer.add(dot, BorderLayout.WEST);
        footer.add(text, BorderLayout.CENTER);
        return footer;
    }

    private void buildContent() {
        try {
            paymentPanel = new PaymentPanel();
            managePanel = new ManageInvoicePanel();
            lookupPanel = new LookupPanel();
            issuePanel = new InvoiceIssuePanel();
            payrollPanel = new PayrollPanel();

            contentArea.add(paymentPanel, "payment");
            contentArea.add(managePanel, "manage");
            contentArea.add(lookupPanel, "lookup");
            contentArea.add(issuePanel, "issue");
            contentArea.add(payrollPanel, "payroll");
        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new GridBagLayout());
            errorPanel.setBackground(BG_PAGE);

            RoundedCardPanel card = new RoundedCardPanel(18, Color.WHITE);
            card.setLayout(new BorderLayout(0, 10));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SIDEBAR_BORDER, 1),
                    new EmptyBorder(22, 26, 22, 26)
            ));

            JLabel title = new JLabel("Không thể tải module tài chính");
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setForeground(TEXT_MAIN);

            JLabel message = new JLabel("<html><div style='width:420px;'>Một hoặc nhiều màn hình con chưa sẵn sàng. Kiểm tra lại PaymentPanel, ManageInvoicePanel, LookupPanel, InvoiceIssuePanel hoặc PayrollPanel.</div></html>");
            message.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            message.setForeground(TEXT_MUTED);

            card.add(title, BorderLayout.NORTH);
            card.add(message, BorderLayout.CENTER);

            errorPanel.add(card);
            contentArea.add(errorPanel, "error");

            CardLayout cl = (CardLayout) contentArea.getLayout();
            cl.show(contentArea, "error");

            System.err.println("Lỗi khởi tạo các panel con: " + e.getMessage());
        }
    }

    private NavButton navBtn(String label, String key, String iconType, Color accent, Color soft) {
        NavButton btn = new NavButton(label, iconType, accent, soft);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(232, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setToolTipText(label);
        btn.addActionListener(e -> setActive(btn, key));
        return btn;
    }

    private void setActive(NavButton btn, String key) {
        if (activeBtn != null) {
            activeBtn.setActive(false);
        }

        btn.setActive(true);
        activeBtn = btn;

        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, key);
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_SOFT);
        label.setBorder(new EmptyBorder(8, 4, 8, 4));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.putClientProperty("hideWhenCollapsed", true);
        return label;
    }

    private void toggleSidebar(SidebarToggleButton toggleButton) {
        collapsed = !collapsed;

        int sidebarWidth = collapsed ? 84 : 260;
        int btnWidth = collapsed ? 54 : 232;

        sidebar.setPreferredSize(new Dimension(sidebarWidth, 0));
        sidebar.setMinimumSize(new Dimension(sidebarWidth, 0));

        for (Component component : sidebarContent.getComponents()) {
            if (component instanceof JComponent
                    && Boolean.TRUE.equals(((JComponent) component).getClientProperty("hideWhenCollapsed"))) {
                component.setVisible(!collapsed);
            }

            if (component instanceof NavButton) {
                NavButton btn = (NavButton) component;
                btn.setCollapsed(collapsed);
                btn.setPreferredSize(new Dimension(btnWidth, 46));
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            }
        }

        toggleButton.setCollapsed(collapsed);

        sidebar.revalidate();
        sidebar.repaint();

        Container parent = sidebar.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    private static class NavButton extends JButton {
        private final String label;
        private final String iconType;
        private final Color accent;
        private final Color soft;

        private boolean active = false;
        private boolean hovered = false;
        private boolean pressed = false;
        private boolean collapsed = false;

        NavButton(String label, String iconType, Color accent, Color soft) {
            super("");
            this.label = label;
            this.iconType = iconType;
            this.accent = accent;
            this.soft = soft;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = false;
                        repaint();
                    }
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color bg = active ? PRIMARY_SOFT : (hovered ? new Color(248, 250, 252) : Color.WHITE);
            if (pressed) bg = new Color(241, 245, 249);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 14, 14);

            if (active) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 8, 4, h - 16, 4, 4);
            }

            int iconSize = 28;
            int iconX = collapsed ? (w - iconSize) / 2 : 14;
            int iconY = (h - iconSize) / 2;

            g2.setColor(active ? Color.WHITE : soft);
            g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 12, 12);

            g2.setColor(active ? PRIMARY : accent);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawIcon(g2, iconType, iconX, iconY, iconSize);

            if (!collapsed) {
                g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
                g2.setColor(active ? PRIMARY_DARK : TEXT_MAIN);

                FontMetrics fm = g2.getFontMetrics();
                int tx = iconX + iconSize + 12;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(label, tx, ty);
            }

            g2.dispose();
        }

        private void drawIcon(Graphics2D g2, String type, int x, int y, int size) {
            int cx = x + size / 2;
            int cy = y + size / 2;

            switch (type) {
                case "payment":
                    g2.drawRoundRect(x + 6, y + 8, size - 12, size - 10, 4, 4);
                    g2.drawLine(x + 6, y + 13, x + size - 6, y + 13);
                    g2.drawLine(x + 10, y + 20, x + 16, y + 20);
                    break;

                case "manage":
                case "invoice":
                    g2.drawRoundRect(x + 8, y + 5, size - 14, size - 10, 4, 4);
                    g2.drawLine(x + 12, y + 11, x + size - 10, y + 11);
                    g2.drawLine(x + 12, y + 16, x + size - 10, y + 16);
                    g2.drawLine(x + 12, y + 21, x + size - 14, y + 21);
                    break;

                case "search":
                    g2.drawOval(x + 7, y + 7, 11, 11);
                    g2.drawLine(x + 16, y + 16, x + 22, y + 22);
                    break;

                case "issue":
                    g2.drawRoundRect(x + 8, y + 5, size - 14, size - 10, 4, 4);
                    g2.drawLine(x + 13, y + 11, x + size - 11, y + 11);
                    g2.drawLine(x + 13, y + 16, x + size - 13, y + 16);
                    g2.drawLine(cx - 4, y + 22, cx - 1, y + 25);
                    g2.drawLine(cx - 1, y + 25, cx + 6, y + 18);
                    break;

                case "payroll":
                    g2.drawOval(x + 6, y + 6, size - 12, size - 12);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    FontMetrics fm = g2.getFontMetrics();
                    String money = "₫";
                    g2.drawString(money, cx - fm.stringWidth(money) / 2, cy + fm.getAscent() / 2 - 2);
                    break;

                default:
                    g2.fillOval(cx - 3, cy - 3, 6, 6);
                    break;
            }
        }
    }

    private static class SidebarToggleButton extends JButton {
        private boolean collapsed = false;
        private boolean hovered = false;
        private boolean pressed = false;

        SidebarToggleButton() {
            super("");
            setPreferredSize(new Dimension(16, 64));
            setMinimumSize(new Dimension(16, 64));
            setMaximumSize(new Dimension(16, 64));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Thu gọn / mở rộng menu tài chính");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        pressed = false;
                        repaint();
                    }
                }
            });
        }

        void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int tabH = 54;
            int y = (getHeight() - tabH) / 2;
            Color color = pressed ? PRIMARY_DARK : hovered ? PRIMARY : new Color(148, 163, 184);

            g2.setColor(color);
            g2.fillRoundRect(0, y, getWidth(), tabH, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = y + tabH / 2;

            if (collapsed) {
                g2.drawLine(cx - 3, cy - 6, cx + 3, cy);
                g2.drawLine(cx - 3, cy + 6, cx + 3, cy);
            } else {
                g2.drawLine(cx + 3, cy - 6, cx - 3, cy);
                g2.drawLine(cx + 3, cy + 6, cx - 3, cy);
            }

            g2.dispose();
        }
    }

    private static class RoundedCardPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedCardPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(SIDEBAR_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class FinanceBrandIcon extends JPanel {
        FinanceBrandIcon() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(PRIMARY_SOFT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2.drawOval(cx - 10, cy - 10, 20, 20);
            g2.drawLine(cx - 6, cy, cx + 6, cy);
            g2.drawLine(cx, cy - 6, cx, cy + 6);

            g2.dispose();
        }
    }
}
