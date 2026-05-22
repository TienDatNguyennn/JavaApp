package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.PersonnelDTO;
import com.mycompany.myapp.model.RoleGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý nhân sự: USERS + ACCOUNT + TEACHER_PROFILE / OFFICE_STAFF_PROFILE.
 * Caller gọi DBConnection.commitTransaction() sau mỗi write.
 */
public class PersonnelDAO {

    // ════════════════════════════════════════════════════════
    // READ
    // ════════════════════════════════════════════════════════

    /** Lấy toàn bộ nhân sự (giáo viên + nhân viên), kể cả tài khoản đã bị khóa. */
    public List<PersonnelDTO> findAll() throws SQLException {
        String sql =
            "SELECT u.user_id, u.full_name, u.email, u.phone, u.identity_card, " +
            "       a.account_id, a.username, a.status AS account_status, " +
            "       rg.role_group_id, rg.name_role_group, " +
            "       tp.major, tp.degree, " +
            "       osp.position, osp.base_salary, osp.salary_grade, " +
            "       CASE WHEN tp.teacher_id IS NOT NULL THEN 'TEACHER' " +
            "            WHEN osp.staff_id  IS NOT NULL THEN 'STAFF'   " +
            "            ELSE 'OTHER' END AS personnel_type " +
            "FROM USERS u " +
            // Bỏ điều kiện a.is_deleted để hiện cả tài khoản đã khóa
            "LEFT JOIN ACCOUNT a ON u.user_id = a.user_id " +
            "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON a.account_id = aarg.account_id AND aarg.is_deleted = 0 " +
            "LEFT JOIN ROLE_GROUP rg ON aarg.role_group_id = rg.role_group_id AND rg.is_deleted = 0 " +
            "LEFT JOIN TEACHER_PROFILE      tp  ON u.user_id = tp.teacher_id AND tp.is_deleted  = 0 " +
            "LEFT JOIN OFFICE_STAFF_PROFILE osp ON u.user_id = osp.staff_id  AND osp.is_deleted = 0 " +
            "WHERE u.is_deleted = 0 "+
            "ORDER BY u.user_id";
        List<PersonnelDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Tìm theo user_id (bao gồm cả đã xóa mềm – dùng cho edit). */
    public PersonnelDTO findById(int userId) throws SQLException {
        String sql =
            "SELECT u.user_id, u.full_name, u.email, u.phone, u.identity_card, " +
            "       a.account_id, a.username, a.status AS account_status, " +
            "       rg.role_group_id, rg.name_role_group, " +
            "       tp.major, tp.degree, " +
            "       osp.position, osp.base_salary, osp.salary_grade, " +
            "       CASE WHEN tp.teacher_id IS NOT NULL THEN 'TEACHER' " +
            "            WHEN osp.staff_id  IS NOT NULL THEN 'STAFF'   " +
            "            ELSE 'OTHER' END AS personnel_type " +
            "FROM USERS u " +
            "LEFT JOIN ACCOUNT a ON u.user_id = a.user_id " +
            "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg ON a.account_id = aarg.account_id AND aarg.is_deleted = 0 " +
            "LEFT JOIN ROLE_GROUP rg ON aarg.role_group_id = rg.role_group_id " +
            "LEFT JOIN TEACHER_PROFILE      tp  ON u.user_id = tp.teacher_id " +
            "LEFT JOIN OFFICE_STAFF_PROFILE osp ON u.user_id = osp.staff_id " +
            "WHERE u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Lấy danh sách nhóm quyền (dùng cho ComboBox). */
    public List<RoleGroup> findAllRoleGroups() throws SQLException {
        List<RoleGroup> list = new ArrayList<>();
        String sql = "SELECT role_group_id, name_role_group FROM ROLE_GROUP WHERE is_deleted = 0 ORDER BY role_group_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new RoleGroup(rs.getInt("role_group_id"), rs.getString("name_role_group")));
        }
        return list;
    }

    // ════════════════════════════════════════════════════════
    // INSERT  (dùng MAX+1 để tránh sequence lệch)
    // ════════════════════════════════════════════════════════

    /**
     * Thêm nhân sự mới: USERS → ACCOUNT → ACCOUNT_ASSIGN_ROLE_GROUP → Profile.
     * @param dto       dữ liệu nhân sự
     * @param passHash  mật khẩu đã hash bằng BCrypt
     */
    public void insert(PersonnelDTO dto, String passHash) throws SQLException {
        Connection conn = DBConnection.getConnection();

        // 0. Kiểm tra trùng lặp với tài khoản đang HOẠT ĐỘNG (is_deleted = 0)
        checkDuplicateBeforeInsert(conn, dto.getEmail(), dto.getUsername(), -1);

        // 1. Insert USERS
        int newUserId;
        String sqlUser =
            "INSERT INTO USERS (user_id, full_name, email, phone, identity_card) " +
            "VALUES ((SELECT NVL(MAX(user_id),0)+1 FROM USERS), ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlUser, new String[]{"USER_ID"})) {
            ps.setNString(1, dto.getFullName());
            ps.setString(2, dto.getEmail());
            ps.setString(3, nullIfBlank(dto.getPhone()));
            ps.setString(4, nullIfBlank(dto.getIdentityCard()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Không lấy được user_id sau khi INSERT USERS");
                newUserId = rs.getInt(1);
            }
        }

        // 2. Insert ACCOUNT
        int newAccountId;
        String sqlAcc =
            "INSERT INTO ACCOUNT (account_id, user_id, username, password_hash, status) " +
            "VALUES ((SELECT NVL(MAX(account_id),0)+1 FROM ACCOUNT), ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sqlAcc, new String[]{"ACCOUNT_ID"})) {
            ps.setInt(1, newUserId);
            ps.setString(2, dto.getUsername());
            ps.setString(3, passHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Không lấy được account_id sau khi INSERT ACCOUNT");
                newAccountId = rs.getInt(1);
            }
        }

        // 3. Insert ACCOUNT_ASSIGN_ROLE_GROUP
        if (dto.getRoleGroupId() > 0) {
            String sqlRole =
                "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (account_id, role_group_id) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlRole)) {
                ps.setInt(1, newAccountId);
                ps.setInt(2, dto.getRoleGroupId());
                ps.executeUpdate();
            }
        }

        // 4. Insert profile
        insertProfile(conn, newUserId, dto);
    }

    private void insertProfile(Connection conn, int userId, PersonnelDTO dto) throws SQLException {
        if ("TEACHER".equals(dto.getPersonnelType())) {
            String sql = "INSERT INTO TEACHER_PROFILE (teacher_id, major, degree) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setNString(2, dto.getMajor());
                ps.setNString(3, nullIfBlank(dto.getDegree()));
                ps.executeUpdate();
            }
        } else if ("STAFF".equals(dto.getPersonnelType())) {
            String sql = "INSERT INTO OFFICE_STAFF_PROFILE (staff_id, position, base_salary, salary_grade) " +
                         "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setNString(2, dto.getPosition());
                ps.setDouble(3, dto.getBaseSalary());
                if (dto.getSalaryGrade() > 0) ps.setInt(4, dto.getSalaryGrade());
                else                           ps.setNull(4, Types.INTEGER);
                ps.executeUpdate();
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // UPDATE
    // ════════════════════════════════════════════════════════

    /** Cập nhật thông tin cơ bản (USERS + profile). Không đổi password/username ở đây. */
    public void update(PersonnelDTO dto) throws SQLException {
        Connection conn = DBConnection.getConnection();

        // 1. Update USERS
        String sqlUser =
            "UPDATE USERS SET full_name = ?, email = ?, phone = ?, identity_card = ?, " +
            "                 updated_at = SYSDATE " +
            "WHERE user_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setNString(1, dto.getFullName());
            ps.setString(2, dto.getEmail());
            ps.setString(3, nullIfBlank(dto.getPhone()));
            ps.setString(4, nullIfBlank(dto.getIdentityCard()));
            ps.setInt(5, dto.getUserId());
            ps.executeUpdate();
        }

        // 2. Update role group (xóa cũ, thêm mới nếu thay đổi)
        if (dto.getAccountId() > 0 && dto.getRoleGroupId() > 0) {
            String sqlDelRole = "UPDATE ACCOUNT_ASSIGN_ROLE_GROUP SET is_deleted = 1 " +
                                "WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelRole)) {
                ps.setInt(1, dto.getAccountId());
                ps.executeUpdate();
            }
            // Upsert role group
            String sqlChk = "SELECT 1 FROM ACCOUNT_ASSIGN_ROLE_GROUP " +
                             "WHERE account_id = ? AND role_group_id = ?";
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(sqlChk)) {
                ps.setInt(1, dto.getAccountId());
                ps.setInt(2, dto.getRoleGroupId());
                try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
            }
            if (exists) {
                String sqlRestore = "UPDATE ACCOUNT_ASSIGN_ROLE_GROUP SET is_deleted = 0 " +
                                    "WHERE account_id = ? AND role_group_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlRestore)) {
                    ps.setInt(1, dto.getAccountId());
                    ps.setInt(2, dto.getRoleGroupId());
                    ps.executeUpdate();
                }
            } else {
                String sqlIns = "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (account_id, role_group_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
                    ps.setInt(1, dto.getAccountId());
                    ps.setInt(2, dto.getRoleGroupId());
                    ps.executeUpdate();
                }
            }
        }

        // 3. Update profile
        if ("TEACHER".equals(dto.getPersonnelType())) {
            String chk = "SELECT 1 FROM TEACHER_PROFILE WHERE teacher_id = ?";
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(chk)) {
                ps.setInt(1, dto.getUserId());
                try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
            }
            if (exists) {
                String sql = "UPDATE TEACHER_PROFILE SET major = ?, degree = ?, updated_at = SYSDATE, is_deleted = 0 " +
                             "WHERE teacher_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setNString(1, dto.getMajor());
                    ps.setNString(2, nullIfBlank(dto.getDegree()));
                    ps.setInt(3, dto.getUserId());
                    ps.executeUpdate();
                }
            } else {
                insertProfile(conn, dto.getUserId(), dto);
            }
        } else if ("STAFF".equals(dto.getPersonnelType())) {
            String chk = "SELECT 1 FROM OFFICE_STAFF_PROFILE WHERE staff_id = ?";
            boolean exists = false;
            try (PreparedStatement ps = conn.prepareStatement(chk)) {
                ps.setInt(1, dto.getUserId());
                try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
            }
            if (exists) {
                String sql = "UPDATE OFFICE_STAFF_PROFILE SET position = ?, base_salary = ?, " +
                             "salary_grade = ?, updated_at = SYSDATE, is_deleted = 0 " +
                             "WHERE staff_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setNString(1, dto.getPosition());
                    ps.setDouble(2, dto.getBaseSalary());
                    if (dto.getSalaryGrade() > 0) ps.setInt(3, dto.getSalaryGrade());
                    else                           ps.setNull(3, Types.INTEGER);
                    ps.setInt(4, dto.getUserId());
                    ps.executeUpdate();
                }
            } else {
                insertProfile(conn, dto.getUserId(), dto);
            }
        }
    }

    /** Đặt lại mật khẩu tài khoản. */
    public void resetPassword(int accountId, String newPassHash) throws SQLException {
        String sql = "UPDATE ACCOUNT SET password_hash = ?, updated_at = SYSDATE WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassHash);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    // ════════════════════════════════════════════════════════
    // TOGGLE LOCK / SOFT DELETE
    // ════════════════════════════════════════════════════════

    /** Khóa tài khoản (ACCOUNT.is_deleted=1, status=LOCKED). USERS vẫn còn. */
    public void lockAccount(int userId) throws SQLException {
        String sql = "UPDATE ACCOUNT SET is_deleted = 1, status = 'LOCKED', updated_at = SYSDATE " +
                     "WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Mở khóa tài khoản. */
    public void unlockAccount(int userId) throws SQLException {
        String sql = "UPDATE ACCOUNT SET is_deleted = 0, status = 'ACTIVE', updated_at = SYSDATE " +
                     "WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Xóa mềm hoàn toàn: USERS + ACCOUNT + profiles. */
    public void softDelete(int userId) throws SQLException {
        Connection conn = DBConnection.getConnection();
        for (String sql : new String[]{
            "UPDATE USERS              SET is_deleted = 1, updated_at = SYSDATE WHERE user_id = ?",
            "UPDATE ACCOUNT            SET is_deleted = 1, status = 'LOCKED', updated_at = SYSDATE WHERE user_id = ?",
            "UPDATE TEACHER_PROFILE    SET is_deleted = 1, updated_at = SYSDATE WHERE teacher_id = ?",
            "UPDATE OFFICE_STAFF_PROFILE SET is_deleted = 1, updated_at = SYSDATE WHERE staff_id = ?"
        }) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // ASSIGN PROFILE TO EXISTING ACCOUNT
    // ════════════════════════════════════════════════════════

    /**
     * Lấy danh sách user chưa có TEACHER_PROFILE hoặc OFFICE_STAFF_PROFILE.
     * Dùng để chọn tài khoản cần gán hồ sơ nhân sự.
     */
    public List<PersonnelDTO> findUsersWithoutProfile() throws SQLException {
        String sql =
            "SELECT u.user_id, u.full_name, u.email, u.phone, u.identity_card, " +
            "       a.account_id, a.username, a.status AS account_status, " +
            "       rg.role_group_id, rg.name_role_group " +
            "FROM USERS u " +
            "LEFT JOIN ACCOUNT a " +
            "       ON u.user_id = a.user_id " +
            "LEFT JOIN ACCOUNT_ASSIGN_ROLE_GROUP aarg " +
            "       ON a.account_id = aarg.account_id AND aarg.is_deleted = 0 " +
            "LEFT JOIN ROLE_GROUP rg " +
            "       ON aarg.role_group_id = rg.role_group_id AND rg.is_deleted = 0 " +
            "WHERE u.is_deleted = 0 " +
            "  AND u.user_id NOT IN " +
            "      (SELECT teacher_id FROM TEACHER_PROFILE      WHERE is_deleted = 0) " +
            "  AND u.user_id NOT IN " +
            "      (SELECT staff_id   FROM OFFICE_STAFF_PROFILE WHERE is_deleted = 0) " +
            "ORDER BY u.user_id";
        List<PersonnelDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PersonnelDTO d = new PersonnelDTO();
                d.setUserId(rs.getInt("user_id"));
                d.setFullName(rs.getNString("full_name"));
                d.setEmail(rs.getString("email"));
                d.setPhone(rs.getString("phone"));
                d.setIdentityCard(rs.getString("identity_card"));
                d.setAccountId(rs.getInt("account_id"));
                d.setUsername(rs.getString("username"));
                d.setAccountStatus(rs.getString("account_status"));
                d.setRoleGroupId(rs.getInt("role_group_id"));
                d.setRoleGroupName(rs.getString("name_role_group"));
                list.add(d);
            }
        }
        return list;
    }

    /**
     * Gán TEACHER_PROFILE hoặc OFFICE_STAFF_PROFILE cho tài khoản đã tồn tại.
     * Đồng thời cập nhật nhóm quyền nếu dto.getRoleGroupId() > 0.
     * Caller phải gọi DBConnection.commitTransaction() sau khi hoàn tất.
     */
    public void assignProfile(PersonnelDTO dto) throws SQLException {
        Connection conn = DBConnection.getConnection();

        // 1. Tạo profile (TEACHER_PROFILE hoặc OFFICE_STAFF_PROFILE)
        insertProfile(conn, dto.getUserId(), dto);

        // 2. Cập nhật nhóm quyền nếu account tồn tại và role được chỉ định
        if (dto.getAccountId() > 0 && dto.getRoleGroupId() > 0) {
            // Xóa mềm tất cả role cũ của account này
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ACCOUNT_ASSIGN_ROLE_GROUP SET is_deleted = 1 WHERE account_id = ?")) {
                ps.setInt(1, dto.getAccountId());
                ps.executeUpdate();
            }
            // Kiểm tra combo (account_id, role_group_id) đã từng tồn tại chưa
            boolean existsAny;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM ACCOUNT_ASSIGN_ROLE_GROUP " +
                    "WHERE account_id = ? AND role_group_id = ?")) {
                ps.setInt(1, dto.getAccountId());
                ps.setInt(2, dto.getRoleGroupId());
                try (ResultSet rs = ps.executeQuery()) { existsAny = rs.next(); }
            }
            if (existsAny) {
                // Khôi phục record cũ
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE ACCOUNT_ASSIGN_ROLE_GROUP SET is_deleted = 0 " +
                        "WHERE account_id = ? AND role_group_id = ?")) {
                    ps.setInt(1, dto.getAccountId());
                    ps.setInt(2, dto.getRoleGroupId());
                    ps.executeUpdate();
                }
            } else {
                // Thêm mới
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO ACCOUNT_ASSIGN_ROLE_GROUP (account_id, role_group_id) " +
                        "VALUES (?, ?)")) {
                    ps.setInt(1, dto.getAccountId());
                    ps.setInt(2, dto.getRoleGroupId());
                    ps.executeUpdate();
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════

    /**
     * Kiểm tra email và username có bị trùng với tài khoản đang hoạt động không.
     * Chỉ so sánh với các dòng có is_deleted = 0, nên tài khoản đã xóa mềm
     * không ảnh hưởng → email/phone/username cũ có thể tái sử dụng.
     *
     * @param excludeUserId  Truyền userId hiện tại khi update (để bỏ qua chính nó),
     *                       truyền -1 khi insert mới.
     */
    private void checkDuplicateBeforeInsert(Connection conn,
                                             String email,
                                             String username,
                                             int excludeUserId) throws SQLException {
        // Kiểm tra email (trong bảng USERS, chỉ tính dòng chưa xóa)
        String sqlEmail = excludeUserId > 0
            ? "SELECT 1 FROM USERS WHERE LOWER(email) = LOWER(?) AND is_deleted = 0 AND user_id <> ?"
            : "SELECT 1 FROM USERS WHERE LOWER(email) = LOWER(?) AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlEmail)) {
            ps.setString(1, email);
            if (excludeUserId > 0) ps.setInt(2, excludeUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    throw new SQLException("Email \"" + email + "\" đã được sử dụng bởi tài khoản khác!");
            }
        }

        // Kiểm tra username (trong bảng ACCOUNT, chỉ tính dòng chưa xóa)
        if (username != null && !username.isBlank()) {
            String sqlUser = excludeUserId > 0
                ? "SELECT 1 FROM ACCOUNT a JOIN USERS u ON a.user_id = u.user_id " +
                  "WHERE LOWER(a.username) = LOWER(?) AND a.is_deleted = 0 AND u.user_id <> ?"
                : "SELECT 1 FROM ACCOUNT WHERE LOWER(username) = LOWER(?) AND is_deleted = 0";
            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, username);
                if (excludeUserId > 0) ps.setInt(2, excludeUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        throw new SQLException("Tên đăng nhập \"" + username + "\" đã được sử dụng!");
                }
            }
        }
    }

    private PersonnelDTO mapRow(ResultSet rs) throws SQLException {
        PersonnelDTO d = new PersonnelDTO();
        d.setUserId(rs.getInt("user_id"));
        d.setFullName(rs.getNString("full_name"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setIdentityCard(rs.getString("identity_card"));
        d.setAccountId(rs.getInt("account_id"));
        d.setUsername(rs.getString("username"));
        d.setAccountStatus(rs.getString("account_status"));
        d.setRoleGroupId(rs.getInt("role_group_id"));
        d.setRoleGroupName(rs.getString("name_role_group"));
        d.setPersonnelType(rs.getString("personnel_type"));
        // Teacher fields
        d.setMajor(rs.getNString("major"));
        d.setDegree(rs.getNString("degree"));
        // Staff fields
        d.setPosition(rs.getNString("position"));
        d.setBaseSalary(rs.getDouble("base_salary"));
        d.setSalaryGrade(rs.getInt("salary_grade"));
        return d;
    }

    private String nullIfBlank(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
