package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PermissionDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionRepository {

    // =========================================================
    // 1. LẤY QUYỀN THEO NHÓM QUYỀN
    // =========================================================
    public List<PermissionDTO> getPermissionsByRoleGroup(int roleGroupId) throws SQLException {
        List<PermissionDTO> list = new ArrayList<>();

        String sql =
            "SELECT f.function_id, f.name_function, " +
            "       NVL(r.role_id, 0) AS role_id, " +
            "       NVL(r.view_perm, 0) AS view_perm, " +
            "       NVL(r.add_perm, 0) AS add_perm, " +
            "       NVL(r.edit_perm, 0) AS edit_perm, " +
            "       NVL(r.delete_perm, 0) AS delete_perm " +
            "FROM FUNCTION f " +
            "LEFT JOIN ( " +
            "    SELECT r.function_id, " +
            "           MAX(r.role_id) AS role_id, " +
            "           MAX(r.view_perm) AS view_perm, " +
            "           MAX(r.add_perm) AS add_perm, " +
            "           MAX(r.edit_perm) AS edit_perm, " +
            "           MAX(r.delete_perm) AS delete_perm " +
            "    FROM ROLE_GROUP_ASSIGN_ROLE rgar " +
            "    JOIN ROLE r ON rgar.role_id = r.role_id " +
            "    WHERE rgar.role_group_id = ? " +
            "      AND rgar.is_deleted = 0 " +
            "      AND r.is_deleted = 0 " +
            "    GROUP BY r.function_id " +
            ") r ON f.function_id = r.function_id " +
            "WHERE f.is_deleted = 0 " +
            "ORDER BY f.function_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roleGroupId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPermission(rs));
                }
            }
        }

        return list;
    }

    // =========================================================
    // 2. LƯU QUYỀN THEO NHÓM QUYỀN
    // =========================================================
    public void updateRoleGroupPermissions(int roleGroupId, List<PermissionDTO> permissions) throws SQLException {
        if (roleGroupId <= 0) {
            throw new SQLException("roleGroupId không hợp lệ.");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new SQLException("Danh sách quyền trống.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (PermissionDTO p : permissions) {
                    if (p.functionId <= 0) {
                        continue;
                    }

                    if (p.roleId > 0) {
                        updateRole(conn, p.roleId, p);
                    } else {
                        int newRoleId = getNextRoleId(conn);
                        insertRole(conn, newRoleId, p);
                        assignRoleToGroup(conn, roleGroupId, newRoleId);
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /*
     * Hàm cũ để tương thích code cũ.
     * Chỉ dùng khi tất cả dòng quyền đã có roleId.
     * Nếu roleId = 0 thì phải gọi updateRoleGroupPermissions(roleGroupId, permissions).
     */
    public void updatePermissions(List<PermissionDTO> permissions) throws SQLException {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (PermissionDTO p : permissions) {
                    if (p.roleId <= 0) {
                        throw new SQLException(
                            "Không thể lưu quyền nhóm vì thiếu role_id. " +
                            "Hãy dùng updateRoleGroupPermissions(roleGroupId, permissions)."
                        );
                    }

                    updateRole(conn, p.roleId, p);
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // =========================================================
    // 3. LẤY QUYỀN THEO TÀI KHOẢN
    //
    // Nguyên tắc:
    // - Luôn lấy đủ FUNCTION.
    // - Quyền nhóm lấy từ ACCOUNT_ASSIGN_ROLE_GROUP.
    // - Quyền riêng tài khoản lấy từ ACCOUNT_ASSIGN_ROLE.
    // - Quyền riêng tài khoản ưu tiên quyền nhóm.
    // - Dùng MAX để gom trùng function_id, tránh role cũ/duplicate làm sai kết quả.
    // =========================================================
    public List<PermissionDTO> getPermissionsByAccount(int accountId) throws SQLException {
        if (accountId <= 0) {
            throw new SQLException("accountId không hợp lệ.");
        }

        List<PermissionDTO> list = new ArrayList<>();

        String sql =
            "SELECT f.function_id, f.name_function, " +
            "       NVL(ar.role_id, NVL(gr.role_id, 0)) AS role_id, " +
            "       NVL(ar.view_perm, NVL(gr.view_perm, 0)) AS view_perm, " +
            "       NVL(ar.add_perm, NVL(gr.add_perm, 0)) AS add_perm, " +
            "       NVL(ar.edit_perm, NVL(gr.edit_perm, 0)) AS edit_perm, " +
            "       NVL(ar.delete_perm, NVL(gr.delete_perm, 0)) AS delete_perm " +
            "FROM FUNCTION f " +

            "LEFT JOIN ( " +
            "    SELECT r.function_id, " +
            "           MAX(r.role_id) AS role_id, " +
            "           MAX(r.view_perm) AS view_perm, " +
            "           MAX(r.add_perm) AS add_perm, " +
            "           MAX(r.edit_perm) AS edit_perm, " +
            "           MAX(r.delete_perm) AS delete_perm " +
            "    FROM ACCOUNT_ASSIGN_ROLE_GROUP aarg " +
            "    JOIN ROLE_GROUP_ASSIGN_ROLE rgar ON aarg.role_group_id = rgar.role_group_id " +
            "    JOIN ROLE r ON rgar.role_id = r.role_id " +
            "    WHERE aarg.account_id = ? " +
            "      AND aarg.is_deleted = 0 " +
            "      AND rgar.is_deleted = 0 " +
            "      AND r.is_deleted = 0 " +
            "    GROUP BY r.function_id " +
            ") gr ON f.function_id = gr.function_id " +

            "LEFT JOIN ( " +
            "    SELECT r.function_id, " +
            "           MAX(r.role_id) AS role_id, " +
            "           MAX(r.view_perm) AS view_perm, " +
            "           MAX(r.add_perm) AS add_perm, " +
            "           MAX(r.edit_perm) AS edit_perm, " +
            "           MAX(r.delete_perm) AS delete_perm " +
            "    FROM ACCOUNT_ASSIGN_ROLE aar " +
            "    JOIN ROLE r ON aar.role_id = r.role_id " +
            "    WHERE aar.account_id = ? " +
            "      AND aar.is_deleted = 0 " +
            "      AND r.is_deleted = 0 " +
            "    GROUP BY r.function_id " +
            ") ar ON f.function_id = ar.function_id " +

            "WHERE f.is_deleted = 0 " +
            "ORDER BY f.function_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ps.setInt(2, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PermissionDTO dto = mapPermission(rs);

                    System.out.println(
                        "[DB PERMISSION] accountId=" + accountId
                            + " | " + dto.functionName
                            + " | view=" + dto.canView
                            + " | add=" + dto.canAdd
                            + " | edit=" + dto.canEdit
                            + " | delete=" + dto.canDelete
                    );

                    list.add(dto);
                }
            }
        }

        return list;
    }

    // =========================================================
    // 4. LƯU QUYỀN RIÊNG THEO TÀI KHOẢN
    //
    // Cách làm:
    // - Xóa mềm quyền riêng cũ của account.
    // - Tạo ROLE riêng mới cho từng FUNCTION.
    // - Gán ROLE riêng mới vào ACCOUNT_ASSIGN_ROLE.
    // =========================================================
    public void saveAccountCustomPermissions(int accountId, List<PermissionDTO> permissions) throws SQLException {
        if (accountId <= 0) {
            throw new SQLException("accountId không hợp lệ.");
        }

        if (permissions == null || permissions.isEmpty()) {
            throw new SQLException("Danh sách quyền trống.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                softDeleteOldAccountAssignRoles(conn, accountId);

                for (PermissionDTO p : permissions) {
                    if (p.functionId <= 0) {
                        continue;
                    }

                    int newRoleId = getNextRoleId(conn);

                    insertRole(conn, newRoleId, p);
                    assignRoleToAccount(conn, accountId, newRoleId);

                    System.out.println(
                        "[SAVE ACCOUNT PERMISSION] accountId=" + accountId
                            + " | functionId=" + p.functionId
                            + " | functionName=" + p.functionName
                            + " | view=" + p.canView
                            + " | add=" + p.canAdd
                            + " | edit=" + p.canEdit
                            + " | delete=" + p.canDelete
                    );
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // =========================================================
    // 5. DEBUG: KIỂM TRA QUYỀN RIÊNG ACCOUNT
    // Có thể gọi tạm khi cần kiểm tra dữ liệu.
    // =========================================================
    public void debugAccountPermissions(String username) throws SQLException {
        String sql =
            "SELECT a.account_id, a.username, f.function_id, f.name_function, " +
            "       r.view_perm, r.add_perm, r.edit_perm, r.delete_perm, " +
            "       aar.is_deleted AS aar_deleted, r.is_deleted AS role_deleted " +
            "FROM ACCOUNT a " +
            "JOIN ACCOUNT_ASSIGN_ROLE aar ON a.account_id = aar.account_id " +
            "JOIN ROLE r ON aar.role_id = r.role_id " +
            "JOIN FUNCTION f ON r.function_id = f.function_id " +
            "WHERE a.username = ? " +
            "ORDER BY f.function_id, aar.is_deleted, r.is_deleted";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("===== DEBUG ACCOUNT PERMISSIONS: " + username + " =====");

                while (rs.next()) {
                    System.out.println(
                        rs.getInt("account_id")
                            + " | " + rs.getString("username")
                            + " | functionId=" + rs.getInt("function_id")
                            + " | " + rs.getString("name_function")
                            + " | view=" + rs.getInt("view_perm")
                            + " | add=" + rs.getInt("add_perm")
                            + " | edit=" + rs.getInt("edit_perm")
                            + " | delete=" + rs.getInt("delete_perm")
                            + " | aar_deleted=" + rs.getInt("aar_deleted")
                            + " | role_deleted=" + rs.getInt("role_deleted")
                    );
                }
            }
        }
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    private PermissionDTO mapPermission(ResultSet rs) throws SQLException {
        return new PermissionDTO(
            rs.getInt("role_id"),
            rs.getInt("function_id"),
            rs.getString("name_function"),
            rs.getInt("view_perm") == 1,
            rs.getInt("add_perm") == 1,
            rs.getInt("edit_perm") == 1,
            rs.getInt("delete_perm") == 1
        );
    }

    private void updateRole(Connection conn, int roleId, PermissionDTO p) throws SQLException {
        String sql =
            "UPDATE ROLE " +
            "SET view_perm = ?, " +
            "    add_perm = ?, " +
            "    edit_perm = ?, " +
            "    delete_perm = ?, " +
            "    is_deleted = 0 " +
            "WHERE role_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.canView ? 1 : 0);
            ps.setInt(2, p.canAdd ? 1 : 0);
            ps.setInt(3, p.canEdit ? 1 : 0);
            ps.setInt(4, p.canDelete ? 1 : 0);
            ps.setInt(5, roleId);
            ps.executeUpdate();
        }
    }

    private void insertRole(Connection conn, int roleId, PermissionDTO p) throws SQLException {
        String sql =
            "INSERT INTO ROLE " +
            "    (role_id, function_id, view_perm, add_perm, edit_perm, delete_perm, is_deleted) " +
            "VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, p.functionId);
            ps.setInt(3, p.canView ? 1 : 0);
            ps.setInt(4, p.canAdd ? 1 : 0);
            ps.setInt(5, p.canEdit ? 1 : 0);
            ps.setInt(6, p.canDelete ? 1 : 0);
            ps.executeUpdate();
        }
    }

    private int getNextRoleId(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(role_id), 0) + 1 AS next_id FROM ROLE";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }

        throw new SQLException("Không thể sinh role_id mới.");
    }

    private void assignRoleToAccount(Connection conn, int accountId, int roleId) throws SQLException {
        String sql =
            "INSERT INTO ACCOUNT_ASSIGN_ROLE " +
            "    (account_id, role_id, is_deleted) " +
            "VALUES (?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    private void assignRoleToGroup(Connection conn, int roleGroupId, int roleId) throws SQLException {
        String sql =
            "INSERT INTO ROLE_GROUP_ASSIGN_ROLE " +
            "    (role_group_id, role_id, is_deleted) " +
            "VALUES (?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleGroupId);
            ps.setInt(2, roleId);
            ps.executeUpdate();
        }
    }

    private void softDeleteOldAccountAssignRoles(Connection conn, int accountId) throws SQLException {
        String sql =
            "UPDATE ACCOUNT_ASSIGN_ROLE " +
            "SET is_deleted = 1 " +
            "WHERE account_id = ? " +
            "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        }
    }
}