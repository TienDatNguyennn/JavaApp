package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
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
            // Gán mới (MERGE: tái kích hoạt nếu đã tồn tại, hoặc INSERT mới)
            dao.assignTeacher(teacherId, classId);
            DBConnection.commitTransaction();
        } catch (SQLException e) {
            DBConnection.rollbackTransaction();
            if (e.getErrorCode() == 20003) {
                throw new Exception("Giáo viên này đã bị trùng lịch dạy ở một lớp khác!");
            }
            // ORA-04098: trigger INVALID → yêu cầu chạy script sửa DB
            if (e.getErrorCode() == 4098) {
                throw new Exception(
                    "Trigger TRG_PREVENTTEACHERCOLLISION bị lỗi (INVALID).\n" +
                    "Vui lòng chạy file fix_teacher_collision_trigger.sql\n" +
                    "trong SQL Developer để sửa trước khi tiếp tục.");
            }
            throw new Exception("Lỗi hệ thống: " + e.getMessage());
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            throw e;
        }
    }
}