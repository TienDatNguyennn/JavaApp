package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsRepository {

    /*
     * Dành cho giáo viên: chỉ lấy lớp được phân công.
     */
    public List<Map<String, Object>> getClassesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> classes = new ArrayList<>();

        String sql =
                "SELECT DISTINCT sc.class_id, sc.class_name " +
                "FROM STUDY_CLASS sc " +
                "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                "WHERE ta.teacher_id = ? " +
                "  AND NVL(sc.is_deleted, 0) = 0 " +
                "  AND NVL(ta.is_deleted, 0) = 0 " +
                "ORDER BY sc.class_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("class_id", rs.getInt("class_id"));
                    map.put("class_name", rs.getString("class_name"));
                    classes.add(map);
                }
            }
        }

        return classes;
    }

    /*
     * FIX CHÍNH:
     * Dành cho giáo vụ/admin/kế toán: xem được tất cả lớp.
     */
    public List<Map<String, Object>> getAllClasses(Connection conn) throws SQLException {
        List<Map<String, Object>> classes = new ArrayList<>();

        String sql =
                "SELECT sc.class_id, sc.class_name " +
                "FROM STUDY_CLASS sc " +
                "WHERE NVL(sc.is_deleted, 0) = 0 " +
                "ORDER BY sc.class_name";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("class_id", rs.getInt("class_id"));
                map.put("class_name", rs.getString("class_name"));
                classes.add(map);
            }
        }

        return classes;
    }

    /*
     * classId = 0: thống kê tất cả lớp.
     * classId > 0: thống kê một lớp.
     */
    public List<AttendanceAnalyticsDTO> getAttendanceAnalytics(Connection conn, int classId) throws SQLException {
        List<AttendanceAnalyticsDTO> list = new ArrayList<>();

        String sql =
                "SELECT " +
                "    s.student_id, " +
                "    s.full_name, " +
                "    sc.class_id, " +
                "    sc.class_name, " +
                "    COUNT(a.attendance_id) AS total_sessions, " +
                "    NVL(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END), 0) AS present_count, " +
                "    NVL(SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END), 0) AS absent_count " +
                "FROM CLASS_PLACEMENT cp " +
                "JOIN STUDENT s ON cp.student_id = s.student_id " +
                "JOIN STUDY_CLASS sc ON cp.class_id = sc.class_id " +
                "LEFT JOIN CLASS_SCHEDULE cs ON cs.class_id = sc.class_id " +
                "    AND NVL(cs.is_deleted, 0) = 0 " +
                "LEFT JOIN ATTENDANCE a ON a.schedule_id = cs.schedule_id " +
                "    AND a.student_id = s.student_id " +
                "    AND NVL(a.is_deleted, 0) = 0 " +
                "WHERE cp.status = 'ACTIVE' " +
                "  AND NVL(cp.is_deleted, 0) = 0 " +
                "  AND NVL(s.is_deleted, 0) = 0 " +
                "  AND NVL(sc.is_deleted, 0) = 0 " +
                "  AND (? = 0 OR sc.class_id = ?) " +
                "GROUP BY s.student_id, s.full_name, sc.class_id, sc.class_name " +
                "ORDER BY sc.class_name ASC, s.full_name ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceAnalyticsDTO dto = new AttendanceAnalyticsDTO();

                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setClassId(rs.getInt("class_id"));
                    dto.setClassName(rs.getString("class_name"));

                    int total = rs.getInt("total_sessions");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");

                    dto.setTotalSessions(total);
                    dto.setPresentCount(present);
                    dto.setAbsentCount(absent);

                    double rate = total == 0 ? 0.0 : ((double) present / total) * 100;
                    dto.setAttendanceRate(rate);

                    list.add(dto);
                }
            }
        }

        return list;
    }
}
