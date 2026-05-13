package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.StudentAttendanceDTO;
import com.mycompany.myapp.repository.AttendanceRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AttendanceService {
    
    private final AttendanceRepository repo = new AttendanceRepository();

    public Result<List<Map<String, Object>>> getTeacherClasses() {
        int teacherId = SessionStore.getUserId();
        if (teacherId <= 0) return Result.failure("Lỗi phiên đăng nhập.");
        try (Connection conn = DBConnection.getConnection()) {
            return Result.success(repo.getClassesByTeacher(conn, teacherId), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi tải lớp học: " + e.getMessage());
        }
    }

    public Result<List<Map<String, Object>>> getClassSchedules(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            return Result.success(repo.getSchedulesByClass(conn, classId), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi tải ca học: " + e.getMessage());
        }
    }

    public Result<List<StudentAttendanceDTO>> getAttendanceList(int classId, int scheduleId, java.util.Date date) {
        try (Connection conn = DBConnection.getConnection()) {
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            return Result.success(repo.getAttendanceList(conn, classId, scheduleId, sqlDate), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi tải danh sách điểm danh.");
        }
    }

    public Result<Void> saveAttendanceList(int scheduleId, java.util.Date date, List<StudentAttendanceDTO> list) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction
            
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            for (StudentAttendanceDTO dto : list) {
                if (dto.getAttendanceId() > 0) {
                    // Đã có trong DB -> Update
                    repo.updateAttendance(conn, dto.getAttendanceId(), dto.getStatus(), dto.getNote());
                } else {
                    // Lần đầu điểm danh -> Insert
                    repo.insertAttendance(conn, scheduleId, dto.getStudentId(), sqlDate, dto.getStatus(), dto.getNote());
                }
            }

            conn.commit(); // Hoàn tất Transaction
            return Result.success(null, "Lưu điểm danh thành công!");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return Result.failure("Lỗi lưu điểm danh: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
        }
    }
}