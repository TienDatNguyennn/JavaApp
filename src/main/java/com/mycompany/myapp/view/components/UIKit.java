/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.view.components;

/**
 *
 * @author Tien Dat
 */
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIKit {
    // 1. CONSTANTS
    public static final Color PRIMARY = Color.decode("#6C63FF");
    public static final Color BACKGROUND = Color.decode("#F5F7FB");
    public static final Color CARD_BG = Color.decode("#FFFFFF");
    public static final Color TEXT_DARK = Color.decode("#1E293B");
    public static final Color TEXT_MUTED = Color.decode("#64748B");
    public static final Color BORDER_COLOR = Color.decode("#E2E8F0");
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    // 2. ROUNDED PANEL
    public static class RoundedPanel extends JPanel {
        private int radius;
        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBackground(CARD_BG);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 3. MODERN TABLE (Chuẩn CRM)
    public static class ModernTable extends JTable {
        public ModernTable() {
            setShowVerticalLines(false);
            setShowHorizontalLines(true);
            setGridColor(BORDER_COLOR);
            setRowHeight(45); // Row padding rộng
            setFont(FONT_NORMAL);
            setForeground(TEXT_DARK);
            setSelectionBackground(new Color(108, 99, 255, 30));
            setSelectionForeground(TEXT_DARK);
            
            JTableHeader header = getTableHeader();
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setBackground(PRIMARY);
                    c.setForeground(Color.WHITE);
                    c.setFont(FONT_HEADER);
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    return c;
                }
            });
            
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            setDefaultRenderer(Object.class, centerRenderer);
            setDefaultRenderer(String.class, centerRenderer);
            setDefaultRenderer(Integer.class, centerRenderer);
            setDefaultRenderer(Double.class, centerRenderer);
        }
    }

    // 4. MODERN SCROLL PANE (Ẩn border, custom thanh cuộn)
    public static class ModernScrollPane extends JScrollPane {
        public ModernScrollPane(Component view) {
            super(view);
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(CARD_BG);
            getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() { this.thumbColor = new Color(200, 200, 200); this.trackColor = CARD_BG; }
                @Override protected JButton createDecreaseButton(int orientation) { return createZeroBtn(); }
                @Override protected JButton createIncreaseButton(int orientation) { return createZeroBtn(); }
                private JButton createZeroBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
                @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(thumbColor);
                    g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
                    g2.dispose();
                }
            });
        }
    }
}