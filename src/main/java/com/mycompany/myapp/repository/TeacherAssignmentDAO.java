package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherAssignmentDAO {

    // =========================================================
    // 1. Lấy danh sách toàn bộ lớp học kèm lịch và giáo viên
    // =========================================================
    public List<Map<String, Object>> getAllClassesWithAssignments() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT c.class_id, " +
                "       c.class_name, " +
                "       s.subject_name, " +
                "       LISTAGG(cs.day_of_week || ' (' || cs.start_time || '-' || cs.end_time || ')', ', ') " +
                "           WITHIN GROUP (ORDER BY cs.day_of_week, cs.start_time) AS schedule, " +
                "       ta.teacher_id, " +
                "       u.full_name AS teacher_name " +
                "FROM STUDY_CLASS c " +
                "JOIN SUBJECT s " +
                "    ON c.subject_id = s.subject_id " +
                "LEFT JOIN CLASS_SCHEDULE cs " +
                "    ON c.class_id = cs.class_id " +
                "   AND cs.is_deleted = 0 " +
                "LEFT JOIN TEACHING_ASSIGNMENT ta " +
                "    ON c.class_id = ta.class_id " +
                "   AND ta.is_deleted = 0 " +
                "LEFT JOIN USERS u " +
                "    ON ta.teacher_id = u.user_id " +
                "WHERE c.is_deleted = 0 " +
                "GROUP BY c.class_id, c.class_name, s.subject_name, ta.teacher_id, u.full_name " +
                "ORDER BY c.class_id DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();

                map.put("class_id", rs.getInt("class_id"));
                map.put("class_name", rs.getString("class_name"));
                map.put("subject_name", rs.getString("subject_name"));

                String schedule = rs.getString("schedule");
                map.put("schedule", schedule != null ? schedule : "Chưa có lịch");

                int teacherId = rs.getInt("teacher_id");
                if (rs.wasNull()) {
                    map.put("teacher_id", -1);
                } else {
                    map.put("teacher_id", teacherId);
                }

                String teacherName = rs.getString("teacher_name");
                map.put("teacher_name", teacherName != null ? teacherName : "Chưa phân công");

                list.add(map);
            }
        }

        return list;
    }

    // =========================================================
    // 2. Lấy danh sách giáo viên đưa vào ComboBox
    // =========================================================
    public List<Map<String, Object>> getAvailableTeachers() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
                "SELECT u.user_id, " +
                "       u.full_name, " +
                "       tp.major " +
                "FROM USERS u " +
                "JOIN TEACHER_PROFILE tp " +
                "    ON u.user_id = tp.teacher_id " +
                "WHERE u.is_deleted = 0 " +
                "  AND NVL(tp.is_deleted, 0) = 0 " +
                "ORDER BY u.full_name";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();

                map.put("user_id", rs.getInt("user_id"));
                map.put("full_name", rs.getString("full_name"));
                map.put("major", rs.getString("major"));

                list.add(map);
            }
        }

        return list;
    }

    // =========================================================
    // 3. Gán / đổi giáo viên cho lớp
    //
    // Logic đúng:
    // - Nếu class_id đã từng có phân công: UPDATE dòng cũ.
    // - Nếu class_id chưa từng có phân công: INSERT dòng mới.
    //
    // Không INSERT bừa khi lớp đã có giáo viên,
    // tránh lỗi ORA-00001 unique constraint.
    // =========================================================
    public void assignTeacher(int teacherId, int classId) throws SQLException {
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Bước 1: kiểm tra giáo viên có bị trùng lịch với lớp khác không
            checkScheduleConflict(conn, teacherId, classId);

            // Bước 2: kiểm tra class_id này đã có dòng phân công chưa
            boolean exists = assignmentExistsByClass(conn, classId);

            if (exists) {
                updateAssignmentByClass(conn, teacherId, classId);
            } else {
                insertAssignment(conn, teacherId, classId);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }

            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    // =========================================================
    // 4. Kiểm tra lớp đã có dòng phân công chưa
    //
    // Cố ý không lọc is_deleted.
    // Vì nếu dòng cũ is_deleted = 1 nhưng unique constraint nằm trên class_id,
    // INSERT mới vẫn có thể bị ORA-00001.
    // Do đó chỉ cần class_id từng tồn tại thì UPDATE khôi phục lại.
    // =========================================================
    private boolean assignmentExistsByClass(Connection conn, int classId) throws SQLException {
        String sql =
                "SELECT COUNT(*) " +
                "FROM TEACHING_ASSIGNMENT " +
                "WHERE class_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =========================================================
    // 5. UPDATE phân công cũ theo class_id
    // =========================================================
    private void updateAssignmentByClass(Connection conn, int teacherId, int classId) throws SQLException {
        String sql =
                "UPDATE TEACHING_ASSIGNMENT " +
                "SET teacher_id = ?, " +
                "    assigned_date = SYSDATE, " +
                "    updated_at = SYSDATE, " +
                "    is_deleted = 0 " +
                "WHERE class_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);

            int affected = ps.executeUpdate();

            if (affected == 0) {
                throw new SQLException("Không tìm thấy lớp để cập nhật phân công giáo viên.");
            }
        }
    }

    // =========================================================
    // 6. INSERT phân công mới
    // =========================================================
    private void insertAssignment(Connection conn, int teacherId, int classId) throws SQLException {
        String sql =
                "INSERT INTO TEACHING_ASSIGNMENT " +
                "    (teacher_id, class_id, assigned_date, is_deleted) " +
                "VALUES " +
                "    (?, ?, SYSDATE, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // 7. Kiểm tra trùng lịch giáo viên bằng Java-side query
    //
    // Thay thế trigger trg_PreventTeacherCollision để tránh ORA-04091.
    // =========================================================
    private void checkScheduleConflict(Connection conn, int teacherId, int classId) throws SQLException {
        String sql =
                "SELECT COUNT(*) " +
                "FROM CLASS_SCHEDULE cs_new " +
                "JOIN CLASS_SCHEDULE cs_old " +
                "    ON cs_new.day_of_week = cs_old.day_of_week " +
                "JOIN TEACHING_ASSIGNMENT ta " +
                "    ON ta.class_id = cs_old.class_id " +
                "WHERE cs_new.class_id = ? " +
                "  AND ta.teacher_id = ? " +
                "  AND ta.is_deleted = 0 " +
                "  AND cs_new.is_deleted = 0 " +
                "  AND cs_old.is_deleted = 0 " +
                "  AND ta.class_id <> ? " +
                "  AND cs_new.start_time < cs_old.end_time " +
                "  AND cs_new.end_time > cs_old.start_time";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, teacherId);
            ps.setInt(3, classId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException(
                            "Giáo viên này đã có lịch dạy ở một lớp khác trong khung giờ này.",
                            "45000",
                            20003
                    );
                }
            }
        }
    }

    // =========================================================
    // 8. Hủy phân công giáo viên khỏi lớp
    // =========================================================
    public void removeAssignment(int classId) throws SQLException {
        String sql =
                "UPDATE TEACHING_ASSIGNMENT " +
                "SET is_deleted = 1, " +
                "    updated_at = SYSDATE " +
                "WHERE class_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, classId);
            ps.executeUpdate();
        }
    }
}