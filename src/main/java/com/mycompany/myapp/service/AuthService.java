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
        Map<String, Object> userInfo = accountRepo.findLoginInfoByUsername(username);

        if (userInfo == null) {
            throw new Exception("Tài khoản không tồn tại!");
        }

        if ("LOCKED".equals(userInfo.get("status"))) {
            throw new Exception("Tài khoản bị khóa!");
        }

        if (!PasswordUtil.checkPassword(password, (String) userInfo.get("password_hash"))) {
            throw new Exception("Mật khẩu không chính xác!");
        }

        String fullName = (String) userInfo.get("full_name");
        String token = TokenService.generateToken(username, fullName);

        int accountId = ((Number) userInfo.get("account_id")).intValue();
        List<String> roles = accountRepo.findRoleGroupsByAccountId(accountId);

        SessionStore.saveSession(token, userInfo, roles);

        System.out.println("===== AUTH SERVICE LOGIN =====");
        System.out.println("username = " + username);
        System.out.println("account_id = " + accountId);
        System.out.println("full_name = " + fullName);
        System.out.println("roles = " + roles);

        return token;
    }
}