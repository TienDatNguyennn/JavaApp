/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountRepository {

    public Account findActiveAccountByUsername(String username) {
        // Query kết hợp JOIN lấy fullName và check is_deleted = 0 theo chuẩn DB của bạn
        String sql = "SELECT a.account_id, a.user_id, a.username, a.password_hash, a.status, u.full_name " +
                     "FROM ACCOUNT a " +
                     "INNER JOIN USERS u ON a.user_id = u.user_id " +
                     "WHERE a.username = ? AND a.is_deleted = 0 AND u.is_deleted = 0";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setUserId(rs.getInt("user_id"));
                    acc.setUsername(rs.getString("username"));
                    acc.setPasswordHash(rs.getString("password_hash"));
                    acc.setStatus(rs.getString("status")); 
                    acc.setFullName(rs.getNString("full_name"));
                    return acc;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn Database: " + e.getMessage());
        }
        return null; // Trả về null nếu không tìm thấy account
    }
}