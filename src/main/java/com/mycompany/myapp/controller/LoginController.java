/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.controller;

import com.mycompany.myapp.service.AuthService;
import com.mycompany.myapp.view.screens.LoginUI; // Đã sửa: Phải có .screens mới đúng

public class LoginController {

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    
    public void handleLogin(String username, String password, LoginUI ui) {
        // 1. Kiểm tra rỗng cơ bản
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            ui.showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            // 2. Thực hiện đăng nhập
            // AuthService sẽ tự verify mật khẩu, tạo Token và lưu Session nội bộ
            authService.login(username, password);

            // 3. Thông báo UI thành công và chuyển màn hình
            ui.onLoginSuccess();

        } catch (Exception ex) {
            // Thông báo lỗi (sai pass, user không tồn tại, tài khoản bị khóa...) ra UI
            ui.showError(ex.getMessage());
        }
    }
}