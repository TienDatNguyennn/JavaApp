/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.utils;

import com.mycompany.myapp.model.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import io.jsonwebtoken.Claims;
public class TokenService {
    
    // Key mã hóa - Trong dự án thực tế đi làm sẽ được giấu trong file .env
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // Thời gian sống: 24h

    public String getFullNameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("fullName", String.class);
        } catch (Exception e) {
            return "Người dùng";
        }
    }

    public String generateToken(Account account) {
    return Jwts.builder()
            .setSubject(account.getUsername())
            .claim("fullName", account.getFullName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(SECRET_KEY)
            // Không cần serializeWith nữa, Jackson sẽ tự động được nhận diện
            .compact();
}

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // Sai chữ ký, hết hạn, hoặc bị sửa đổi
        }
    }
}