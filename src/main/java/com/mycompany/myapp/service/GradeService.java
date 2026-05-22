package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.CourseResultDTO;
import com.mycompany.myapp.repository.GradeRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class GradeService {

    private final GradeRepository repo = new GradeRepository();

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

    public Result<List<CourseResultDTO>> getStudentGrades(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            return Result.success(repo.getStudentGrades(conn, classId), "OK");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tải danh sách điểm.");
        }
    }

    // Lưu điểm hàng loạt an toàn với Transaction
    public Result<Void> saveGrades(int classId, List<CourseResultDTO> grades) {
        // Dùng getNewConnection() để có connection riêng — tránh đóng/thay đổi shared singleton
        Connection conn = null;
        try {
            conn = DBConnection.getNewConnection(); // autoCommit=false, SERIALIZABLE

            for (CourseResultDTO dto : grades) {
                if (dto.getFinalScore() == null) continue;

                String calculatedRank = calculateRank(dto.getFinalScore());

                if (dto.getResultId() > 0) {
                    repo.updateGrade(conn, dto.getResultId(), dto.getFinalScore(), calculatedRank);
                } else {
                    repo.insertGrade(conn, dto.getStudentId(), classId, dto.getFinalScore(), calculatedRank);
                }
            }

            conn.commit();
            return Result.success(null, "Lưu bảng điểm thành công!");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return Result.failure("Lỗi hệ thống khi lưu bảng điểm: " + e.getMessage());
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ex) {}
        }
    }

    // Logic xếp loại tự động (Chuẩn quy chế giáo dục)
    public static String calculateRank(Double score) {
        if (score == null) return "";
        if (score >= 8.0) return "GIỎI";
        if (score >= 6.5) return "KHÁ";
        if (score >= 5.0) return "TRUNG BÌNH";
        return "YẾU";
    }
}