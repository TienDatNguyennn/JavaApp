package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.PermissionDTO;
import com.mycompany.myapp.service.AuthService;
import com.mycompany.myapp.service.PermissionService;
import com.mycompany.myapp.utils.PermissionManager;
import com.mycompany.myapp.utils.SessionStore;
import com.mycompany.myapp.view.screens.LoginUI;

import java.util.List;

public class LoginController {

    private final AuthService authService;
    private final PermissionService permissionService;

    public LoginController() {
        this.authService = new AuthService();
        this.permissionService = new PermissionService();
    }

    public void handleLogin(String username, String password, LoginUI ui) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            ui.showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        try {
            authService.login(username.trim(), password);

            int accountId = SessionStore.getAccountId();

            System.out.println("===== LOGIN SUCCESS =====");
            System.out.println("username input = " + username.trim());
            System.out.println("Session accountId = " + accountId);
            System.out.println("Session userId = " + SessionStore.getUserId());
            System.out.println("Session fullName = " + SessionStore.getFullName());

            if (accountId <= 0) {
                throw new Exception("Không lấy được account_id sau khi đăng nhập.");
            }

            List<PermissionDTO> permissions =
                    permissionService.getPermissionsByAccount(accountId);

            PermissionManager.setPermissions(permissions);

            ui.onLoginSuccess();

        } catch (Exception ex) {
            ui.showError(ex.getMessage());
            ex.printStackTrace();
        }
    }
}