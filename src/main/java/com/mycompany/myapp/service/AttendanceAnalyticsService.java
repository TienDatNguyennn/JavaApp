package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AttendanceAnalyticsDTO;
import com.mycompany.myapp.repository.AttendanceAnalyticsRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AttendanceAnalyticsService {
    
    private final AttendanceAnalyticsRepository repo = new AttendanceAnalyticsRepository();

    public Result<List<Map<String, Object>>> getTeacherClasses() {
        int teacherId = SessionStore.getUserId();
        if (teacherId <= 0) return Result.failure("Phiên đăng nhập hết hạn.");
        
        try (Connection conn = DBConnection.getConnection()) {
            return Result.success(repo.getClassesByTeacher(conn, teacherId), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tải danh sách lớp.");
        }
    }

    public Result<List<AttendanceAnalyticsDTO>> getClassAnalytics(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            List<AttendanceAnalyticsDTO> data = repo.getAttendanceAnalytics(conn, classId);
            return Result.success(data, "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tính toán dữ liệu chuyên cần.");
        }
    }
}