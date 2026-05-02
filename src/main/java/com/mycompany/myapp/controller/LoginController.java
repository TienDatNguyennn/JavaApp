/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.controller;

import com.mycompany.myapp.service.AuthService;
import com.mycompany.myapp.utils.SessionStore;
import com.mycompany.myapp.view.screens.LoginUI;

public class LoginController {
    
    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    // Hàm này sẽ được gọi khi bấm nút "Đăng nhập" trên màn hình
    public void handleLogin(String username, String password, LoginUI ui) {
        // Kiểm tra rỗng cơ bản
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            ui.showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            // Thực hiện đăng nhập
            String token = authService.login(username, password);
            
            // Lưu token vào bộ nhớ để dùng cho các màn hình sau
            SessionStore.setCurrentToken(token);
            
            // Thông báo UI thành công
            ui.onLoginSuccess();
            
        } catch (Exception ex) {
            // Thông báo lỗi (sai pass, user không tồn tại...) ra UI
            ui.showError(ex.getMessage());
        }
    }
}