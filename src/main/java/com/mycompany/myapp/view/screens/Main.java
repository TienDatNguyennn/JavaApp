/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.view.screens;

/**
 *
 * @author Tien Dat
 */

import com.mycompany.myapp.view.screens.AccountManagerUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) { e.printStackTrace(); }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ Thống Quản Lý Đào Tạo - Admin Portal (Debug Mode)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null); 
            
            // Bỏ qua Login, gọi thẳng vào UI Quản lý
            AccountManagerUI adminPanel = new AccountManagerUI();
            frame.add(adminPanel);
            frame.setVisible(true);
        });
    }
}