package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsRepository {

    // Lấy danh sách lớp của Giáo viên (Tái sử dụng logic cũ)
    public List<Map<String, Object>> getClassesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = "SELECT sc.class_id, sc.class_name FROM STUDY_CLASS sc " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "WHERE ta.teacher_id = ? AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
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

    // Lấy thống kê chuyên cần bằng truy vấn nội suy (Aggregation)
    public List<AttendanceAnalyticsDTO> getAttendanceAnalytics(Connection conn, int classId) throws SQLException {
        List<AttendanceAnalyticsDTO> list = new ArrayList<>();
        
        // SQL dùng COUNT và SUM kết hợp CASE WHEN để thống kê dữ liệu trực tiếp từ DB
        String sql = "SELECT s.student_id, s.full_name, " +
                     "COUNT(a.attendance_id) as total_sessions, " +
                     "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_count, " +
                     "SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count " +
                     "FROM CLASS_PLACEMENT cp " +
                     "JOIN STUDENT s ON cp.student_id = s.student_id " +
                     "LEFT JOIN ATTENDANCE a ON cp.student_id = a.student_id " +
                     "    AND a.schedule_id IN (SELECT schedule_id FROM CLASS_SCHEDULE WHERE class_id = ? AND is_deleted = 0) " +
                     "    AND a.is_deleted = 0 " +
                     "WHERE cp.class_id = ? AND cp.status = 'ACTIVE' AND cp.is_deleted = 0 AND s.is_deleted = 0 " +
                     "GROUP BY s.student_id, s.full_name " +
                     "ORDER BY s.full_name ASC";
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceAnalyticsDTO dto = new AttendanceAnalyticsDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    
                    int total = rs.getInt("total_sessions");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");
                    
                    dto.setTotalSessions(total);
                    dto.setPresentCount(present);
                    dto.setAbsentCount(absent);
                    
                    // Xử lý chia cho 0 trong trường hợp lớp chưa học buổi nào
                    double rate = (total == 0) ? 0.0 : ((double) present / total) * 100;
                    dto.setAttendanceRate(rate);
                    
                    list.add(dto);
                }
            }
        }
        return list;
    }
}