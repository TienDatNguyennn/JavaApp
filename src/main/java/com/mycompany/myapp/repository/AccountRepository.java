package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AccountListDTO;
import com.mycompany.myapp.model.AccountUpdateDTO;
import com.mycompany.myapp.model.RoleGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountRepository {

    // =========================================================================
    // PHỤC VỤ CHỨC NĂNG ĐĂNG NHẬP
    // =========================================================================

    /*
     * Fix chính:
     * - So sánh username không phân biệt hoa/thường.
     * - Chỉ lấy ACCOUNT và USERS chưa bị xóa mềm.
     * - Ưu tiên account_id mới nhất nếu DB đang có username trùng.
     *
     * Lý do:
     * Trường hợp của bạn có Quang123 ở account_id 24 và 110.
     * Nếu query không kiểm soát rõ, admin có thể cấp quyền cho 24
     * nhưng login lại dùng 110.
     */
    public Map<String, Object> findLoginInfoByUsername(String username) throws SQLException {
        String sql =
            "SELECT a.account_id, a.user_id, a.username, a.password_hash, a.status, u.full_name " +
            "FROM ACCOUNT a " +
            "JOIN USERS u ON a.user_id = u.user_id " +
            "WHERE UPPER(a.username) = UPPER(?) " +
            "  AND a.is_deleted = 0 " +
            "  AND u.is_deleted = 0 " +
            "ORDER BY a.account_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username == null ? "" : username.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();

                    result.put("account_id", rs.getInt("account_id"));
                    result.put("user_id", rs.getInt("user_id"));
                    result.put("username", rs.getString("username"));
                    result.put("password_hash", rs.getString("password_hash"));
                    result.put("status", rs.getString("status"));
                    result.put("full_name", rs.getString("full_name"));

                    System.out.println("===== FIND LOGIN INFO =====");
                    System.out.println("input username = " + username);
                    System.out.println("selected account_id = " + rs.getInt("account_id"));
                    System.out.println("selected username = " + rs.getString("username"));
                    System.out.println("selected status = " + rs.getString("status"));

                    return result;
                }
            }
        }

        return null;
    }

    public List<String> findRoleGroupsByAccountId(int accountId) throws SQLException {
        List<String> roles = new ArrayList<>();

        String sql =
            "SELECT rg.name_role_group " +
            "FROM ACCOUNT_ASSIGN_ROLE_GROUP aarg " +
            "JOIN ROLE_GROUP rg ON aarg.role_group_id = rg.role_group_id " +
            "WHERE aarg.account_id = ? " +
            "  AND aarg.is_deleted = 0 " +
            "  AND rg.is_deleted = 0";

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

    public List<RoleGroup> findAllRoleGroups() throws SQLException {
        List<RoleGroup> roleGroups = new ArrayList<>();

        String sql =
            "SELECT role_group_id, name_role_group " +
            "FROM ROLE_GROUP " +
            "WHERE is_deleted = 0 " +
            "ORDER BY role_group_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roleGroups.add(new RoleGroup(
                    rs.getInt("role_group_id"),
                    rs.getString("name_role_group")
                ));
            }
        }

        return roleGroups;
    }

    // =========================================================================
    // HỖ TRỢ LỌC TÀI KHOẢN
    // =========================================================================

    /*
     * filterStatus:
     * - ACTIVE: chỉ tài khoản đang hoạt động, chưa xóa.
     * - LOCKED: tài khoản bị khóa hoặc đã xóa mềm.
     * - ALL: tất cả.
     *
     * Fix:
     * - LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP có điều kiện aarg.is_deleted = 0.
     * - LEFT JOIN ROLE_GROUP có điều kiện rg.is_deleted = 0.
     * - ORDER BY username, account_id để dễ phát hiện username trùng.
     */
    public List<AccountListDTO> findAccountsByFilter(String filterStatus) throws SQLException {
        List<AccountListDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.account_id, a.username, u.full_name, u.email, ")
           .append("       rg.name_role_group, a.status ")
           .append("FROM ACCOUNT a ")
           .append("JOIN USERS u ON a.user_id = u.user_id ")
           .append("LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ")
           .append("       ON a.account_id = aarg.account_id ")
           .append("      AND aarg.is_deleted = 0 ")
           .append("LEFT JOIN ROLE_GROUP rg ")
           .append("       ON aarg.role_group_id = rg.role_group_id ")
           .append("      AND rg.is_deleted = 0 ");

        if ("ACTIVE".equalsIgnoreCase(filterStatus)) {
            sql.append("WHERE a.is_deleted = 0 ")
               .append("  AND u.is_deleted = 0 ")
               .append("  AND NVL(a.status, 'ACTIVE') <> 'LOCKED' ");
        } else if ("LOCKED".equalsIgnoreCase(filterStatus)) {
            sql.append("WHERE (a.is_deleted = 1 OR NVL(a.status, 'ACTIVE') = 'LOCKED') ");
        } else {
            sql.append("WHERE 1 = 1 ");
        }

        sql.append("ORDER BY a.username, a.account_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString());
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new AccountListDTO(
                    rs.getInt("account_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("name_role_group") != null
                        ? rs.getString("name_role_group")
                        : "Chưa phân quyền",
                    rs.getString("status")
                ));
            }
        }

        return list;
    }

    // =========================================================================
    // KIỂM TRA TRÙNG LẶP DỮ LIỆU
    // =========================================================================

    /*
     * Fix:
     * Chỉ kiểm tra trùng trong ACCOUNT chưa xóa mềm.
     * So sánh không phân biệt hoa/thường.
     */
    public boolean checkUsernameExists(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql =
            "SELECT 1 " +
            "FROM ACCOUNT " +
            "WHERE UPPER(username) = UPPER(?) " +
            "  AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /*
     * Fix:
     * Chỉ kiểm tra email của USERS chưa xóa mềm.
     * So sánh không phân biệt hoa/thường.
     */
    public boolean checkEmailExists(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String sql =
            "SELECT 1 " +
            "FROM USERS " +
            "WHERE UPPER(email) = UPPER(?) " +
            "  AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /*
     * Fix:
     * Chỉ kiểm tra phone của USERS chưa xóa mềm.
     */
    public boolean checkPhoneExists(String phone) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        String sql =
            "SELECT 1 " +
            "FROM USERS " +
            "WHERE phone = ? " +
            "  AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, phone.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =========================================================================
    // TRANSACTION CẤP PHÁT TÀI KHOẢN
    // =========================================================================

    public int insertUser(Connection conn, String fullName, String email, String phone) throws SQLException {
        String sql =
            "INSERT INTO USERS " +
            "    (full_name, email, phone, is_deleted) " +
            "VALUES (?, ?, ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"USER_ID"})) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new SQLException("Lỗi: Không lấy được user_id");
            }
        }
    }

    public int insertAccount(
            Connection conn,
            int userId,
            String username,
            String hash,
            String status
    ) throws SQLException {

        String sql =
            "INSERT INTO ACCOUNT " +
            "    (user_id, username, password_hash, status, is_deleted) " +
            "VALUES (?, ?, ?, ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ACCOUNT_ID"})) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, hash);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new SQLException("Lỗi: Không lấy được account_id");
            }
        }
    }

    public void insertAccountRoleGroup(Connection conn, int accountId, int roleGroupId) throws SQLException {
        String sql =
            "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP " +
            "    (account_id, role_group_id, is_deleted) " +
            "VALUES (?, ?, 0)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, roleGroupId);
            pstmt.executeUpdate();
        }
    }

    // =========================================================================
    // KHÓA / XÓA MỀM / KHÔI PHỤC TÀI KHOẢN
    // =========================================================================

    public void softDeleteAccount(int accountId) throws SQLException {
        String sql =
            "UPDATE ACCOUNT " +
            "SET is_deleted = 1, " +
            "    status = 'LOCKED' " +
            "WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        }
    }

    public void restoreAccount(int accountId) throws SQLException {
        String sql =
            "UPDATE ACCOUNT " +
            "SET is_deleted = 0, " +
            "    status = 'ACTIVE' " +
            "WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        }
    }

    // =========================================================================
    // MODULE: CẬP NHẬT TÀI KHOẢN / HỒ SƠ / MẬT KHẨU
    // =========================================================================

    public AccountUpdateDTO getAccountFullInfo(Connection conn, int accountId) throws SQLException {
        String sql =
            "SELECT a.account_id, a.user_id, a.username, " +
            "       u.full_name, u.email, u.phone, u.identity_card " +
            "FROM ACCOUNT a " +
            "JOIN USERS u ON a.user_id = u.user_id " +
            "WHERE a.account_id = ? " +
            "  AND a.status = 'ACTIVE' " +
            "  AND a.is_deleted = 0 " +
            "  AND u.is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountUpdateDTO dto = new AccountUpdateDTO();

                    dto.setAccountId(rs.getInt("account_id"));
                    dto.setUserId(rs.getInt("user_id"));
                    dto.setUsername(rs.getString("username"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setEmail(rs.getString("email"));
                    dto.setPhone(rs.getString("phone"));
                    dto.setIdentityCard(rs.getString("identity_card"));

                    return dto;
                }
            }
        }

        return null;
    }

    /*
     * fieldName chỉ nên là một trong các cột được phép.
     * Tránh truyền tên cột tùy ý từ UI để giảm rủi ro SQL injection.
     */
    public boolean isDuplicateInfo(
            Connection conn,
            String fieldName,
            String value,
            int excludeUserId
    ) throws SQLException {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        if (!isAllowedUserField(fieldName)) {
            throw new SQLException("Trường kiểm tra trùng không hợp lệ: " + fieldName);
        }

        String sql =
            "SELECT 1 " +
            "FROM USERS " +
            "WHERE " + fieldName + " = ? " +
            "  AND user_id != ? " +
            "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value.trim());
            ps.setInt(2, excludeUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isAllowedUserField(String fieldName) {
        return "email".equals(fieldName)
            || "phone".equals(fieldName)
            || "identity_card".equals(fieldName);
    }

    public boolean updateUserProfile(Connection conn, AccountUpdateDTO dto) throws SQLException {
        String sql =
            "UPDATE USERS " +
            "SET full_name = ?, " +
            "    email = ?, " +
            "    phone = ?, " +
            "    identity_card = ? " +
            "WHERE user_id = ? " +
            "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getFullName());
            ps.setString(2, dto.getEmail());
            ps.setString(3, dto.getPhone());
            ps.setString(4, dto.getIdentityCard());
            ps.setInt(5, dto.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    public String getPasswordHash(Connection conn, int accountId) throws SQLException {
        String sql =
            "SELECT password_hash " +
            "FROM ACCOUNT " +
            "WHERE account_id = ? " +
            "  AND status = 'ACTIVE' " +
            "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash");
                }
            }
        }

        return null;
    }

    public boolean updatePassword(Connection conn, int accountId, String newPasswordHash) throws SQLException {
        String sql =
            "UPDATE ACCOUNT " +
            "SET password_hash = ? " +
            "WHERE account_id = ? " +
            "  AND status = 'ACTIVE' " +
            "  AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, accountId);

            return ps.executeUpdate() > 0;
        }
    }
}