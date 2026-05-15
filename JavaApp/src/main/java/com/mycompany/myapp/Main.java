/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp;

/**
 *
 * @author Tien Dat
 */
public class Main {

    public static void main(String[] args) {
        // Tạo chuỗi Hash cho mật khẩu "123456"
        String plain = "123456";
        String salt = org.mindrot.jbcrypt.BCrypt.gensalt(10); // Thử cost factor 10 (mặc định)
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(plain, salt);
        boolean match = org.mindrot.jbcrypt.BCrypt.checkpw(plain, hash);

        System.out.println("Dòng Hash mới tạo: " + hash);
        System.out.println("Kết quả tự đối chiếu: " + match);
    }
    
}
