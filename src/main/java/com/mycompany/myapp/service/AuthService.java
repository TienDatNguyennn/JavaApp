/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

import com.mycompany.myapp.repository.AccountRepository;
import com.mycompany.myapp.utils.PasswordUtil;
import com.mycompany.myapp.utils.TokenService;
import com.mycompany.myapp.utils.SessionStore;
import java.util.List;
import java.util.Map;

public class AuthService {
    private final AccountRepository accountRepo = new AccountRepository();
    
    

    public String login(String username, String password) throws Exception {
        // Lấy thông tin user
        Map<String, Object> userInfo = accountRepo.findLoginInfoByUsername(username);
        
        if (userInfo == null) throw new Exception("Tài khoản không tồn tại!");

        if ("LOCKED".equals(userInfo.get("status"))) throw new Exception("Tài khoản bị khóa!");

        // Kiểm tra mật khẩu bằng PasswordUtil của bạn
        if (!PasswordUtil.checkPassword(password, (String) userInfo.get("password_hash"))) {
            throw new Exception("Mật khẩu không chính xác!");
        }

        // Tạo JWT Token
        String fullName = (String) userInfo.get("full_name");
        String token = TokenService.generateToken(username, fullName);

        // Lấy Role Groups theo ACCOUNT_ID và Lưu vào Session
        int accountId = (int) userInfo.get("account_id");
        List<String> roles = accountRepo.findRoleGroupsByAccountId(accountId);
        SessionStore.saveSession(token, userInfo, roles);

        return token;
    }
}