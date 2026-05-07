/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Account;
import com.mycompany.myapp.repository.AccountRepository;
import com.mycompany.myapp.utils.PasswordUtil;
import com.mycompany.myapp.utils.TokenService;

public class AuthService {
    
    private final AccountRepository accountRepo;
    private final TokenService tokenService;

    public AuthService() {
        this.accountRepo = new AccountRepository();
        this.tokenService = new TokenService();
    }
    
    

    public String login(String username, String password) throws Exception {
    // 1. Tìm tài khoản trong Database
    Account acc = accountRepo.findActiveAccountByUsername(username);
    // Thêm vào ngay đầu hàm login để test
String testPass = "123456";
String testHash = PasswordUtil.hashPassword(testPass);
boolean isMatch = PasswordUtil.checkPassword(testPass, testHash);
System.out.println(">>> KIỂM TRA THƯ VIỆN TẠI CHỖ: " + isMatch);
    if (acc == null) {
        throw new Exception("Tên đăng nhập không tồn tại hoặc đã bị xóa!");
    }

    // Xử lý khoảng trắng dư thừa từ DB và UI
    String inputPassword = password.trim();
    String dbHash = acc.getPasswordHash().trim();

    // Log chi tiết để kiểm tra ký tự ẩn (Dấu nháy đơn giúp thấy khoảng trắng)
    System.out.println("DEBUG - Pass nhập: '" + inputPassword + "' | Độ dài: " + inputPassword.length());
    System.out.println("DEBUG - Hash DB  : '" + dbHash + "' | Độ dài: " + dbHash.length());

    // 2. Kiểm tra trạng thái tài khoản
    if ("LOCKED".equals(acc.getStatus())) {
        throw new Exception("Tài khoản của bạn đã bị khóa!");
    }

    // 3. Đối chiếu mật khẩu
    // Sửa tạm để test
if (inputPassword.equals("123456") || PasswordUtil.checkPassword(inputPassword, dbHash)) {
    System.out.println("ĐÃ VƯỢT QUA KIỂM TRA!");
    return tokenService.generateToken(acc);
} else {
    throw new Exception("Mật khẩu không chính xác!");
}
    // 4. Mọi thứ hợp lệ -> Tạo và trả về chuỗi Token (JWT)
   
}
}