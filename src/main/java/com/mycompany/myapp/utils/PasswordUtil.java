/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    // Dùng khi bạn thêm mới một Account vào DB
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Dùng khi đối chiếu lúc đăng nhập
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    // Hàm kiểm tra mật khẩu (Dùng BCrypt)
    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return org.mindrot.jbcrypt.BCrypt.checkpw(rawPassword, hashedPassword);
    }
}