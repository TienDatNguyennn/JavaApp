/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.repository;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PermissionDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionRepository {

    // Lấy danh sách ma trận quyền của một Nhóm Quyền cụ thể
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

    // Cập nhật ma trận quyền (Dùng Batch Update để tối ưu hiệu suất)
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
                pstmt.addBatch(); // Đưa vào hàng chờ
            }
            pstmt.executeBatch(); // Thực thi tất cả cùng lúc
            conn.commit();
            
        } catch (SQLException e) {
            throw e;
        }
    }
}