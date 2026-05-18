package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.StudentAttendanceDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceRepository {

    /**
     * 1. Lấy danh sách lớp của Giáo viên
     * Tự động quản lý đóng/mở kết nối để tránh rò rỉ bộ nhớ (Connection Leak)
     */
    public List<Map<String, Object>> getClassesByTeacher(int teacherId) {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = "SELECT sc.class_id, sc.class_name FROM STUDY_CLASS sc " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "WHERE ta.teacher_id = ? AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
                     "ORDER BY sc.class_name";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("class_id", rs.getInt("class_id"));
                    map.put("class_name", rs.getString("class_name"));
                    classes.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 2. Lấy các ca học (Schedule) của một lớp
     */
    public List<Map<String, Object>> getSchedulesByClass(int classId) {
        List<Map<String, Object>> schedules = new ArrayList<>();
        String sql = "SELECT schedule_id, day_of_week, start_time, end_time FROM CLASS_SCHEDULE " +
                     "WHERE class_id = ? AND is_deleted = 0 ORDER BY day_of_week, start_time";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * 3. Lấy danh sách điểm danh của lớp theo Ca học & Ngày
     * Giải quyết triệt để lỗi ORA-00904 bằng cách đồng bộ trường TRUNC(a.created_at)
     */
    public List<StudentAttendanceDTO> getAttendanceList(int classId, int scheduleId, java.util.Date targetDate) {
        List<StudentAttendanceDTO> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, a.attendance_id, a.status, a.note " +
                     "FROM CLASS_PLACEMENT cp " +
                     "JOIN STUDENT s ON cp.student_id = s.student_id " +
                     "LEFT JOIN ATTENDANCE a ON s.student_id = a.student_id " +
                     "AND a.schedule_id = ? " +
                     "AND TRUNC(a.created_at) = TRUNC(?) " +
                     "WHERE cp.class_id = ? AND cp.is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, scheduleId);
            ps.setDate(2, new java.sql.Date(targetDate.getTime()));
            ps.setInt(3, classId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentAttendanceDTO dto = new StudentAttendanceDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getNString("full_name"));
                    
                    String status = rs.getString("status");
                    dto.setStatus(status != null ? status : "ABSENT"); // Mặc định vắng mặt nếu chưa quét mã
                    dto.setNote(rs.getNString("note"));
                    
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 4. Giải pháp thay thế nâng cấp cho hàm Lưu / Cập nhật điểm danh thủ công
     * Sử dụng MERGE INTO tích hợp Batch Processing để tối ưu hóa hiệu năng giao dịch
     */
    public void saveAttendanceList(int scheduleId, java.util.Date targetDate, List<StudentAttendanceDTO> dtoList) {
        String sql = "MERGE INTO ATTENDANCE a " +
                     "USING (SELECT ? AS sch_id, ? AS stu_id FROM dual) src " +
                     "ON (a.schedule_id = src.sch_id AND a.student_id = src.stu_id AND TRUNC(a.created_at) = TRUNC(?)) " +
                     "WHEN MATCHED THEN " +
                     "  UPDATE SET a.status = ?, a.note = ?, a.updated_at = SYSDATE " +
                     "WHEN NOT MATCHED THEN " +
                     "  INSERT (schedule_id, student_id, status, note, created_at, updated_at) " +
                     "  VALUES (src.sch_id, src.stu_id, ?, ?, ?, SYSDATE)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Kích hoạt cơ chế kiểm soát Transaction thủ công

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                java.sql.Date sqlDate = new java.sql.Date(targetDate.getTime());

                for (StudentAttendanceDTO dto : dtoList) {
                    ps.setInt(1, scheduleId);
                    ps.setInt(2, dto.getStudentId());
                    ps.setDate(3, sqlDate);
                    
                    // Nhánh UPDATE nếu dữ liệu đã tồn tại
                    ps.setString(4, dto.getStatus());
                    ps.setNString(5, dto.getNote());
                    
                    // Nhánh INSERT nếu dữ liệu chưa tồn tại
                    ps.setString(6, dto.getStatus());
                    ps.setNString(7, dto.getNote());
                    ps.setDate(8, sqlDate);
                    
                    ps.addBatch(); // Cho lệnh vào hàng đợi
                }
                ps.executeBatch(); // Đẩy toàn bộ dữ liệu xuống Oracle xử lý trong 1 request duy nhất
                conn.commit(); // Hoàn tất phiên giao dịch an toàn
            } catch (SQLException e) {
                conn.rollback(); // Hoàn tác dữ liệu nếu xảy ra lỗi xung đột mạng giữa chừng
                throw e;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}