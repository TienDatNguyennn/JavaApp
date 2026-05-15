package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.repository.DashboardRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardService {
    
    private final DashboardRepository repo = new DashboardRepository();

    public Result<Map<String, Object>> getFullDashboardData() {
        int teacherId = SessionStore.getUserId();
        if (teacherId <= 0) return Result.failure("Vui lòng đăng nhập lại.");

        try (Connection conn = DBConnection.getConnection()) {
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Lấy 4 chỉ số thẻ
            Map<String, Integer> metrics = repo.getSummaryMetrics(conn, teacherId);
            dashboardData.put("metrics", metrics);
            
            // Lấy lịch dạy
            List<Map<String, Object>> schedules = repo.getUpcomingSchedules(conn, teacherId);
            dashboardData.put("schedules", schedules);
            
            // Lấy thống kê lớp
            List<Map<String, Object>> attendance = repo.getAttendanceOverview(conn, teacherId);
            dashboardData.put("attendance", attendance);
            
            return Result.success(dashboardData, "OK");

        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi khi tải dữ liệu Dashboard: " + e.getMessage());
        }
    }
}