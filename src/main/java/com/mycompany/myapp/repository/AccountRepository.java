package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.RoleGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountRepository {
    
    // ==========================================
    // PHỤC VỤ CHỨC NĂNG ĐĂNG NHẬP (AUTH SERVICE)
    // ==========================================
    
    public Map<String, Object> findLoginInfoByUsername(String username) throws SQLException {
        String sql = "SELECT a.account_id, a.user_id, a.username, a.password_hash, a.status, u.full_name " +
                     "FROM ACCOUNT a " +
                     "JOIN USERS u ON a.user_id = u.user_id " +
                     "WHERE a.username = ? AND a.is_deleted = 0 AND u.is_deleted = 0";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("account_id", rs.getInt("account_id"));
                    result.put("user_id", rs.getInt("user_id"));
                    result.put("username", rs.getString("username"));
                    result.put("password_hash", rs.getString("password_hash"));
                    result.put("status", rs.getString("status"));
                    result.put("full_name", rs.getString("full_name"));
                    return result;
                }
            }
        }
        return null;
    }

    public List<String> findRoleGroupsByAccountId(int accountId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT rg.name_role_group FROM ACCOUNT_ASSIGN_ROLE_GROUP aarg " +
                     "JOIN ROLE_GROUP rg ON aarg.role_group_id = rg.role_group_id " +
                     "WHERE aarg.account_id = ? AND aarg.is_deleted = 0 AND rg.is_deleted = 0";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("name_role_group"));
                }
            }
        }
        return roles;
    }

    // 1. Lấy danh sách nhóm quyền từ DB (Sẽ ra: Giao_Vien_Nhom_Truong, Nhan_Vien_Tu_Van)
    public List<RoleGroup> findAllRoleGroups() throws SQLException {
        List<RoleGroup> roleGroups = new ArrayList<>();
        String sql = "SELECT role_group_id, name_role_group FROM ROLE_GROUP WHERE is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roleGroups.add(new RoleGroup(rs.getInt("role_group_id"), rs.getString("name_role_group")));
            }
        }
        return roleGroups;
    }

    // =========================================================================
    // [ĐÃ SỬA]: HỖ TRỢ LỌC TÀI KHOẢN (ACTIVE / LOCKED / ALL) ĐỂ LÀM TÍNH NĂNG KHÔI PHỤC
    // =========================================================================
    public List<AccountListDTO> findAccountsByFilter(String filterStatus) throws SQLException {
        List<AccountListDTO> list = new ArrayList<>();
        String sql = "SELECT a.account_id, a.username, u.full_name, u.email, rg.name_role_group, a.status " +
                     "FROM ACCOUNT a " +
                     "JOIN USERS u ON a.user_id = u.user_id " +
                     "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON a.account_id = aarg.account_id " +
                     "LEFT JOIN ROLE_GROUP rg ON aarg.role_group_id = rg.role_group_id ";
        
        // Điều kiện lọc
        if ("ACTIVE".equals(filterStatus)) {
            sql += "WHERE a.is_deleted = 0 AND u.is_deleted = 0 ";
        } else if ("LOCKED".equals(filterStatus)) {
            sql += "WHERE a.is_deleted = 1 "; // Chỉ lấy các tài khoản đã bị khóa/xóa mềm
        }
        // Nếu filterStatus là "ALL" thì không thêm WHERE, lấy tất cả
        
        sql += "ORDER BY a.account_id DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new AccountListDTO(
                    rs.getInt("account_id"), 
                    rs.getString("username"),
                    rs.getString("full_name"), 
                    rs.getString("email"),
                    rs.getString("name_role_group") != null ? rs.getString("name_role_group") : "Chưa phân quyền",
                    rs.getString("status")
                ));
            }
        }
        return list;
    }

    // 3. Kiểm tra Username tồn tại
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM ACCOUNT WHERE username = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next(); }
        }
    }

    // ===== CÁC HÀM TRANSACTION CẤP PHÁT TÀI KHOẢN =====
    
    public int insertUser(Connection conn, String fullName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO USERS (full_name, email, phone, is_deleted) VALUES (?, ?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"USER_ID"})) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Lỗi: Không lấy được user_id");
            }
        }
    }

    public int insertAccount(Connection conn, int userId, String username, String hash, String status) throws SQLException {
        String sql = "INSERT INTO ACCOUNT (user_id, username, password_hash, status, is_deleted) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ACCOUNT_ID"})) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, hash);
            pstmt.setString(4, status);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("Lỗi: Không lấy được account_id");
            }
        }
    }

    public void insertAccountRoleGroup(Connection conn, int accountId, int roleGroupId) throws SQLException {
        String sql = "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (account_id, role_group_id, is_deleted) VALUES (?, ?, 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, roleGroupId);
            pstmt.executeUpdate();
        }
    }

    // =========================================================================
    // [THÊM MỚI]: HÀM KHÓA/XÓA MỀM VÀ HÀM MỞ KHÓA/KHÔI PHỤC
    // =========================================================================

    public void softDeleteAccount(int accountId) throws SQLException {
        String sql = "UPDATE ACCOUNT SET is_deleted = 1, status = 'LOCKED' WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        }
    }

    public void restoreAccount(int accountId) throws SQLException {
        String sql = "UPDATE ACCOUNT SET is_deleted = 0, status = 'ACTIVE' WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        }
    }
}