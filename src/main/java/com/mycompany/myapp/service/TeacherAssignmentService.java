package com.mycompany.myapp.service;

import com.mycompany.myapp.repository.TeacherAssignmentDAO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TeacherAssignmentService {
    private final TeacherAssignmentDAO dao = new TeacherAssignmentDAO();

    public List<Map<String, Object>> getAllClasses() throws Exception {
        return dao.getAllClassesWithAssignments();
    }

    public List<Map<String, Object>> getTeachers() throws Exception {
        return dao.getAvailableTeachers();
    }

    public void assignTeacherToClass(int teacherId, int classId) throws Exception {
        try {
            // Hủy phân công cũ trước khi gán mới (nếu có)
            dao.removeAssignment(classId);
            // Gán mới
            dao.assignTeacher(teacherId, classId);
        } catch (SQLException e) {
            // Bắt lỗi từ Trigger trg_PreventTeacherCollision (ORA-20003)
            if (e.getErrorCode() == 20003) {
                throw new Exception("Lỗi: Giáo viên này đã bị trùng lịch dạy ở một lớp khác!");
            }
            throw new Exception("Lỗi hệ thống: " + e.getMessage());
        }
    }
}