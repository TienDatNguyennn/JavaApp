package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.ClassStudentDTO;
import com.mycompany.myapp.repository.LearningRepository;
import com.mycompany.myapp.utils.Result;
import com.mycompany.myapp.utils.SessionStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LearningService {
    
    private final LearningRepository repo = new LearningRepository();

    // Lấy danh sách lớp của Giáo viên đang đăng nhập
    public Result<List<Map<String, Object>>> getMyTeachingClasses() {
        int teacherId = SessionStore.getUserId(); // Lấy ID user hiện tại
        if (teacherId <= 0) {
            return Result.failure("Vui lòng đăng nhập lại!");
        }

        try (Connection conn = DBConnection.getConnection()) {
            List<Map<String, Object>> classes = repo.getClassesByTeacher(conn, teacherId);
            return Result.success(classes, "Thành công");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi khi tải danh sách lớp học.");
        }
    }

    // Lấy danh sách học viên theo classId
    public Result<List<ClassStudentDTO>> getStudentsByClass(int classId) {
        try (Connection conn = DBConnection.getConnection()) {
            List<ClassStudentDTO> students = repo.getStudentsInClass(conn, classId);
            return Result.success(students, "Thành công");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Lỗi DB khi tải danh sách học viên.");
        }
    }
}