package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.ClassStudentDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearningRepository {

    // 1. Lấy danh sách các lớp mà một giáo viên đang được phân công dạy
    public List<Map<String, Object>> getClassesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = "SELECT sc.class_id, sc.class_name " +
                     "FROM STUDY_CLASS sc " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "WHERE ta.teacher_id = ? AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
                     "ORDER BY sc.class_name ASC";
                     
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

    // 2. Lấy danh sách học viên của một lớp cụ thể
    public List<ClassStudentDTO> getStudentsInClass(Connection conn, int classId) throws SQLException {
        List<ClassStudentDTO> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, s.dob, s.gender, s.phone, s.parent_name, s.parent_phone, cp.status " +
                     "FROM STUDENT s " +
                     "JOIN CLASS_PLACEMENT cp ON s.student_id = cp.student_id " +
                     "WHERE cp.class_id = ? AND s.is_deleted = 0 AND cp.is_deleted = 0 " +
                     "ORDER BY s.full_name ASC";
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClassStudentDTO dto = new ClassStudentDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setDob(rs.getDate("dob"));
                    dto.setGender(rs.getString("gender"));
                    dto.setPhone(rs.getString("phone"));
                    dto.setParentName(rs.getString("parent_name"));
                    dto.setParentPhone(rs.getString("parent_phone"));
                    dto.setStatus(rs.getString("status"));
                    students.add(dto);
                }
            }
        }
        return students;
    }
}