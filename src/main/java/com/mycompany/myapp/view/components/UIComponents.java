package com.mycompany.myapp.view.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class UIComponents {

    public static class ModernTextField extends JTextField {
        private String hint;
        public ModernTextField(String hint) {
            this.hint = hint; setFont(new Font("Segoe UI", Font.PLAIN, 14)); setOpaque(false); 
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10)
            ));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getText().isEmpty() && !hasFocus()) { 
                g2.setColor(new Color(150, 150, 150)); 
                g2.drawString(hint, 10, getHeight() / 2 + g.getFontMetrics().getAscent() / 2 - 2); 
            }
        }
    }

    public static class ModernPasswordField extends JPasswordField {
        private String hint;
        public ModernPasswordField(String hint) {
            this.hint = hint; setFont(new Font("Segoe UI", Font.PLAIN, 14)); setOpaque(false); 
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10)
            ));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getPassword().length == 0 && !hasFocus()) { 
                g2.setColor(new Color(150, 150, 150)); 
                g2.drawString(hint, 10, getHeight() / 2 + g.getFontMetrics().getAscent() / 2 - 2); 
            }
        }
    }

    public static class GradientButton extends JButton {
        public GradientButton(String text) {
            super(text); setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(Color.WHITE);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, new Color(108, 92, 231), getWidth(), getHeight(), new Color(142, 68, 173)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); super.paintComponent(g);
        }
    }

public static class CustomTable extends JTable {
        public CustomTable(DefaultTableModel model) {
            super(model); 
            setRowHeight(45); 
            
            // [CẬP NHẬT] Bật cả đường kẻ dọc và ngang, set khoảng cách giữa các ô
            setShowVerticalLines(true); 
            setShowHorizontalLines(true);
            setGridColor(new Color(220, 220, 220)); // Màu đường kẻ xám nhạt, dễ nhìn
            setIntercellSpacing(new Dimension(1, 1)); // Bắt buộc > 0 để đường kẻ hiện rõ
            
            setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
            setSelectionBackground(new Color(232, 229, 250)); 
            setSelectionForeground(Color.BLACK);

            // Ép màu Header và thêm đường vạch phân cách
            JTableHeader header = getTableHeader(); 
            header.setPreferredSize(new Dimension(0, 45));
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setBackground(new Color(108, 92, 231)); 
                    label.setForeground(Color.WHITE);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    
                    // Tạo vạch kẻ dọc mờ cho Header để phân làn
                    javax.swing.border.Border rightBorder = BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(130, 115, 240));
                    
                    if (table.getModel().getColumnClass(column) == Boolean.class) {
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        label.setBorder(BorderFactory.createCompoundBorder(rightBorder, BorderFactory.createEmptyBorder()));
                    } else {
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createCompoundBorder(rightBorder, BorderFactory.createEmptyBorder(0, 15, 0, 0)));
                    }
                    return label;
                }
            });

            // Chỉ tô màu cột Trạng thái nếu bảng có từ 6 cột trở lên (Bảng Tài khoản)
            if (this.getColumnCount() >= 6) {
                getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        label.setHorizontalAlignment(SwingConstants.CENTER); 
                        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        if ("ACTIVE".equals(value)) label.setForeground(new Color(39, 174, 96)); else label.setForeground(new Color(231, 76, 60));
                        return label;
                    }
                });
            }
        }

        // Tạo hiệu ứng ngựa vằn (Zebra-striping)
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 253));
            }
            if (c instanceof JComponent) {
                if (getModel().getColumnClass(column) == Boolean.class) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder());
                } else {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
                }
            }
            return c;
        }
    }
    
    public static class GradientPanel extends JPanel {
        private Color c1, c2;
        public GradientPanel(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2)); g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}