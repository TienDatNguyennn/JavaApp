/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

/**
 *
 * @author Tien Dat
 */
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtils {
    // Secret key dùng riêng cho server (Không bao giờ gửi xuống client)
    private static final String SECRET_KEY = "EduFlex@2026_SecureKey_Oracle";

    public static String generateHash(String payload) {
        try {
            String dataToHash = payload + SECRET_KEY;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to Hex String
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi mã hóa SHA-256", ex);
        }
    }
}