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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton extends JButton {
    private Color defaultColor = new Color(108, 92, 231); 
    private Color hoverColor = new Color(85, 70, 200);

    public CustomButton(String text) {
        super(text);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(defaultColor);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(isEnabled()) setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { if(isEnabled()) setBackground(defaultColor); }
        });
    }
    public void setColors(Color bg, Color hover) {
        this.defaultColor = bg; this.hoverColor = hover; setBackground(bg);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? getBackground() : new Color(200, 200, 200));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        super.paintComponent(g);
        g2.dispose();
    }
}