package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.StudentAttendanceDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {

    // 1. Lấy danh sách lớp của Giáo viên
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

    // 2. Lấy các ca học (Schedule) của một lớp
    public List<Map<String, Object>> getSchedulesByClass(Connection conn, int classId) throws SQLException {
        List<Map<String, Object>> schedules = new ArrayList<>();
        String sql = "SELECT schedule_id, day_of_week, start_time, end_time FROM CLASS_SCHEDULE " +
                     "WHERE class_id = ? AND is_deleted = 0 ORDER BY day_of_week, start_time";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("schedule_id", rs.getInt("schedule_id"));
                    int day = rs.getInt("day_of_week");
                    String dayStr = (day == 8) ? "Chủ Nhật" : ("Thứ " + day);
                    map.put("schedule_name", dayStr + " (" + rs.getString("start_time") + " - " + rs.getString("end_time") + ")");
                    schedules.add(map);
                }
            }
        }
        return schedules;
    }

    // 3. Lấy danh sách điểm danh của lớp theo Ca học & Ngày
    public List<StudentAttendanceDTO> getAttendanceList(Connection conn, int classId, int scheduleId, java.sql.Date date) throws SQLException {
        List<StudentAttendanceDTO> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, a.attendance_id, a.status, a.note " +
                     "FROM CLASS_PLACEMENT cp " +
                     "JOIN STUDENT s ON cp.student_id = s.student_id " +
                     "LEFT JOIN ATTENDANCE a ON cp.student_id = a.student_id " +
                     "    AND a.schedule_id = ? AND TRUNC(a.attendance_date) = TRUNC(?) AND a.is_deleted = 0 " +
                     "WHERE cp.class_id = ? AND cp.status = 'ACTIVE' AND cp.is_deleted = 0 AND s.is_deleted = 0 " +
                     "ORDER BY s.full_name ASC";
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setDate(2, date);
            ps.setInt(3, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentAttendanceDTO dto = new StudentAttendanceDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setAttendanceId(rs.getInt("attendance_id")); // rs.getInt trả về 0 nếu DB là NULL
                    // Mặc định là PRESENT (Có mặt) nếu chưa điểm danh
                    dto.setStatus(rs.getString("status") != null ? rs.getString("status") : "PRESENT");
                    dto.setNote(rs.getString("note"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    // 4. Lưu mới điểm danh
    public void insertAttendance(Connection conn, int scheduleId, int studentId, java.sql.Date date, String status, String note) throws SQLException {
        String sql = "INSERT INTO ATTENDANCE (schedule_id, student_id, attendance_date, status, note, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            ps.setInt(2, studentId);
            ps.setDate(3, date);
            ps.setString(4, status);
            ps.setString(5, note);
            ps.executeUpdate();
        }
    }

    // 5. Cập nhật điểm danh đã tồn tại (updated_at sẽ do trigger tự xử lý)
    public void updateAttendance(Connection conn, int attendanceId, String status, String note) throws SQLException {
        String sql = "UPDATE ATTENDANCE SET status = ?, note = ? WHERE attendance_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, note);
            ps.setInt(3, attendanceId);
            ps.executeUpdate();
        }
    }
}