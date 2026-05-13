/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.AccountUpdateDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Tien Dat
 */
public class UserRepository {
    
    
    public boolean isDuplicateInfo(Connection conn, String fieldName, String value, Long excludeUserId) throws SQLException{
        if(value == null || value.trim().isEmpty()) return false;
        
        String sql = "SELECT 1 FROM USERS WHERE " + fieldName + " = ? AND user_id != ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, value);
            ps.setLong(2,excludeUserId);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next();
            }
        }
        
    }
    
    public boolean updateUserProfile(Connection conn, AccountUpdateDTO dto) throws SQLException {
       String sql = "UPDATE USERS SET full_name = ? , email  = ? , phone = ?, identity_card = ?" +
                    "WHERE user_id = ? AND is_deleted = 0";
       
       try(PreparedStatement ps = conn.prepareStatement(sql)){
           ps.setString(1, dto.getFullName());
           ps.setString(2, dto.getEmail());
           ps.setString(3, dto.getPhone());
           ps.setString(4, dto.getIdentityCard());
           ps.setLong(5, dto.getUserId());
           return ps.executeUpdate() > 0;
       }
    }
    
}
