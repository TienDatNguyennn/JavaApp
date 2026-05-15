package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.AcademicReportDTO;
import com.mycompany.myapp.repository.ReportRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final ReportRepository repo = new ReportRepository();

    public Result<List<Map<String, Object>>> getTeacherClasses() {
        int teacherId = SessionStore.getUserId();
        if (teacherId <= 0) return Result.failure("Phiên đăng nhập không hợp lệ.");
        
        try (Connection conn = DBConnection.getConnection()) {
            return Result.success(repo.getClassesByTeacher(conn, teacherId), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi tải danh sách lớp: " + e.getMessage());
        }
    }

    // Trả về cả List chi tiết và các con số thống kê tổng hợp
    public Result<Map<String, Object>> generateClassReport(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            List<AcademicReportDTO> details = repo.getClassReport(conn, classId);
            
            int total = details.size();
            int passed = 0;
            int failed = 0;
            int noScore = 0;

            for (AcademicReportDTO dto : details) {
                if ("ĐẠT".equals(dto.getStatus())) passed++;
                else if ("KHÔNG ĐẠT".equals(dto.getStatus())) failed++;
                else noScore++;
            }

            // Đóng gói dữ liệu trả về cho UI
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("details", details);
            reportData.put("total", total);
            reportData.put("passed", passed);
            reportData.put("failed", failed);
            reportData.put("noScore", noScore);

            return Result.success(reportData, "OK");
            
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi khi xử lý dữ liệu báo cáo.");
        }
    }
}