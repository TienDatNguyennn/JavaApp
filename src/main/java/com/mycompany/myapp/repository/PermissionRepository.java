package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PermissionDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionRepository {

    // ==========================================
    // 1. LẤY MA TRẬN QUYỀN THEO NHÓM (ROLE GROUP)
    // ==========================================
    public List<PermissionDTO> getPermissionsByRoleGroup(int roleGroupId) throws SQLException {
        List<PermissionDTO> list = new ArrayList<>();
        String sql = "SELECT r.role_id, f.function_id, f.name_function, " +
                     "r.view_perm, r.add_perm, r.edit_perm, r.delete_perm " +
                     "FROM ROLE_GROUP_ASSIGN_ROLE rgar " +
                     "JOIN ROLE r ON rgar.role_id = r.role_id " +
                     "JOIN FUNCTION f ON r.function_id = f.function_id " +
                     "WHERE rgar.role_group_id = ? AND rgar.is_deleted = 0 AND r.is_deleted = 0";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleGroupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PermissionDTO(
                        rs.getInt("role_id"),
                        rs.getInt("function_id"),
                        rs.getString("name_function"),
                        rs.getInt("view_perm") == 1,
                        rs.getInt("add_perm") == 1,
                        rs.getInt("edit_perm") == 1,
                        rs.getInt("delete_perm") == 1
                    ));
                }
            }
        }
        return list;
    }

    // ==========================================
    // 2. CẬP NHẬT QUYỀN CHO NHÓM (BATCH UPDATE)
    // ==========================================
    public void updatePermissions(List<PermissionDTO> permissions) throws SQLException {
        String sql = "UPDATE ROLE SET view_perm = ?, add_perm = ?, edit_perm = ?, delete_perm = ?, updated_at = SYSDATE WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (PermissionDTO p : permissions) {
                pstmt.setInt(1, p.canView ? 1 : 0);
                pstmt.setInt(2, p.canAdd ? 1 : 0);
                pstmt.setInt(3, p.canEdit ? 1 : 0);
                pstmt.setInt(4, p.canDelete ? 1 : 0);
                pstmt.setInt(5, p.roleId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
    }

    // ==========================================
    // 3. LẤY QUYỀN CÁ NHÂN (CHỈ HIỆN MODULE THUỘC NHÓM)
    // ==========================================
    public List<PermissionDTO> getPermissionsByAccount(int accountId) throws SQLException {
        List<PermissionDTO> list = new ArrayList<>();
        // Câu SQL đã được tối ưu để lọc đúng Module của User
        String sql = "SELECT DISTINCT f.function_id, f.name_function, " +
                     "NVL(ar.view_perm, gr.view_perm) AS view_perm, " +
                     "NVL(ar.add_perm, gr.add_perm) AS add_perm, " +
                     "NVL(ar.edit_perm, gr.edit_perm) AS edit_perm, " +
                     "NVL(ar.delete_perm, gr.delete_perm) AS delete_perm " +
                     "FROM FUNCTION f " +
                     "LEFT JOIN (SELECT r.function_id, r.view_perm, r.add_perm, r.edit_perm, r.delete_perm " +
                     "           FROM ROLE_GROUP_ASSIGN_ROLE rgar " +
                     "           JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON rgar.role_group_id = aarg.role_group_id " +
                     "           JOIN ROLE r ON rgar.role_id = r.role_id " +
                     "           WHERE aarg.account_id = ? AND aarg.is_deleted = 0) gr ON f.function_id = gr.function_id " +
                     "LEFT JOIN (SELECT r.function_id, r.view_perm, r.add_perm, r.edit_perm, r.delete_perm " +
                     "           FROM ACCOUNT_ASSIGN_ROLE aar " +
                     "           JOIN ROLE r ON aar.role_id = r.role_id " +
                     "           WHERE aar.account_id = ? AND aar.is_deleted = 0) ar ON f.function_id = ar.function_id " +
                     "WHERE f.is_deleted = 0 " +
                     "AND (gr.function_id IS NOT NULL OR ar.function_id IS NOT NULL)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PermissionDTO(
                        0, 
                        rs.getInt("function_id"),
                        rs.getString("name_function"),
                        rs.getInt("view_perm") == 1,
                        rs.getInt("add_perm") == 1,
                        rs.getInt("edit_perm") == 1,
                        rs.getInt("delete_perm") == 1
                    ));
                }
            }
        }
        return list;
    }

    // ==========================================
    // 4. LƯU ĐẶC QUYỀN RIÊNG CHO TÀI KHOẢN
    // ==========================================
    public void saveAccountCustomPermissions(int accountId, List<PermissionDTO> permissions) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Bước 1: Xóa các ROLE đặc quyền cũ của tài khoản này
                String sqlDeleteAssigned = "DELETE FROM ACCOUNT_ASSIGN_ROLE WHERE account_id = ?";
                try(PreparedStatement ps1 = conn.prepareStatement(sqlDeleteAssigned)){
                    ps1.setInt(1, accountId);
                    ps1.executeUpdate();
                }

                // Bước 2: Tạo các ROLE mới với quyền tùy chỉnh và gán vào Account
                String sqlInsertRole = "INSERT INTO ROLE (function_id, view_perm, add_perm, edit_perm, delete_perm) VALUES (?, ?, ?, ?, ?)";
                String sqlAssignRole = "INSERT INTO ACCOUNT_ASSIGN_ROLE (account_id, role_id) VALUES (?, ?)";
                String[] returnId = { "role_id" };

                for (PermissionDTO p : permissions) {
                    try (PreparedStatement psRole = conn.prepareStatement(sqlInsertRole, returnId)) {
                        psRole.setInt(1, p.functionId);
                        psRole.setInt(2, p.canView ? 1 : 0);
                        psRole.setInt(3, p.canAdd ? 1 : 0);
                        psRole.setInt(4, p.canEdit ? 1 : 0);
                        psRole.setInt(5, p.canDelete ? 1 : 0);
                        psRole.executeUpdate();
                        
                        try (ResultSet rs = psRole.getGeneratedKeys()) {
                            if (rs.next()) {
                                int newRoleId = rs.getInt(1);
                                try(PreparedStatement psAssign = conn.prepareStatement(sqlAssignRole)){
                                    psAssign.setInt(1, accountId);
                                    psAssign.setInt(2, newRoleId);
                                    psAssign.executeUpdate();
                                }
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}