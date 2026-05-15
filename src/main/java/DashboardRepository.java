package com.mycompany.myapp.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {

    // 1. Lấy 4 chỉ số tổng quát cho các Thẻ (Cards)
    public Map<String, Integer> getSummaryMetrics(Connection conn, int teacherId) throws SQLException {
        Map<String, Integer> metrics = new HashMap<>();
        
        // 1. Tổng số lớp đang dạy
        String sqlClasses = "SELECT COUNT(class_id) FROM TEACHING_ASSIGNMENT WHERE teacher_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlClasses)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                metrics.put("totalClasses", rs.next() ? rs.getInt(1) : 0);
            }
        }

        // 2. Tổng số học viên
        String sqlStudents = "SELECT COUNT(DISTINCT cp.student_id) FROM CLASS_PLACEMENT cp " +
                             "JOIN TEACHING_ASSIGNMENT ta ON cp.class_id = ta.class_id " +
                             "WHERE ta.teacher_id = ? AND cp.status = 'ACTIVE' AND cp.is_deleted = 0 AND ta.is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                metrics.put("totalStudents", rs.next() ? rs.getInt(1) : 0);
            }
        }

        // 3. Số ca dạy hôm nay (Đồng bộ ngày của Java với ngày của DB: T2=2, ..., CN=8)
        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue() + 1; 
        String sqlToday = "SELECT COUNT(cs.schedule_id) FROM CLASS_SCHEDULE cs " +
                          "JOIN TEACHING_ASSIGNMENT ta ON cs.class_id = ta.class_id " +
                          "WHERE ta.teacher_id = ? AND cs.day_of_week = ? AND cs.is_deleted = 0 AND ta.is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlToday)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, currentDayOfWeek);
            try (ResultSet rs = ps.executeQuery()) {
                metrics.put("todaySchedules", rs.next() ? rs.getInt(1) : 0);
            }
        }

        // 4. Số bài chờ chấm (Học sinh đang học nhưng final_score bị null)
        String sqlPending = "SELECT COUNT(cp.student_id) FROM CLASS_PLACEMENT cp " +
                            "JOIN TEACHING_ASSIGNMENT ta ON cp.class_id = ta.class_id " +
                            "LEFT JOIN COURSE_RESULT cr ON cp.student_id = cr.student_id AND cp.class_id = cr.class_id " +
                            "WHERE ta.teacher_id = ? AND cr.final_score IS NULL " +
                            "AND cp.status = 'ACTIVE' AND cp.is_deleted = 0 AND ta.is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sqlPending)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                metrics.put("pendingGrades", rs.next() ? rs.getInt(1) : 0);
            }
        }

        return metrics;
    }

    // 2. Lấy danh sách lịch dạy (Lấy tất cả các ca trong tuần của giáo viên này)
    public List<Map<String, Object>> getUpcomingSchedules(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT cs.day_of_week, cs.start_time, cs.end_time, sc.class_name, sub.subject_name " +
                     "FROM CLASS_SCHEDULE cs " +
                     "JOIN STUDY_CLASS sc ON cs.class_id = sc.class_id " +
                     "JOIN SUBJECT sub ON sc.subject_id = sub.subject_id " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "WHERE ta.teacher_id = ? AND cs.is_deleted = 0 AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
                     "ORDER BY cs.day_of_week ASC, cs.start_time ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    int day = rs.getInt("day_of_week");
                    map.put("day", (day == 8) ? "Chủ Nhật" : "Thứ " + day);
                    map.put("time", rs.getString("start_time") + " - " + rs.getString("end_time"));
                    map.put("class_name", rs.getString("class_name"));
                    map.put("subject", rs.getString("subject_name"));
                    list.add(map);
                }
            }
        }
        return list;
    }

    // 3. Thống kê tỷ lệ chuyên cần trung bình theo từng lớp
    public List<Map<String, Object>> getAttendanceOverview(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sc.class_name, " +
                     "COUNT(a.attendance_id) as total_records, " +
                     "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present_count " +
                     "FROM STUDY_CLASS sc " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "LEFT JOIN CLASS_PLACEMENT cp ON sc.class_id = cp.class_id AND cp.is_deleted = 0 " +
                     "LEFT JOIN ATTENDANCE a ON cp.student_id = a.student_id AND a.is_deleted = 0 " +
                     "WHERE ta.teacher_id = ? AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
                     "GROUP BY sc.class_name " +
                     "ORDER BY sc.class_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("class_name", rs.getString("class_name"));
                    int total = rs.getInt("total_records");
                    int present = rs.getInt("present_count");
                    double rate = (total == 0) ? 0.0 : ((double) present / total) * 100;
                    map.put("rate", rate);
                    list.add(map);
                }
            }
        }
        return list;
    }
}