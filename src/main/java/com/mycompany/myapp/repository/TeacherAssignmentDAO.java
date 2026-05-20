/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.myapp.repository;

/**
 *
 * @author Tien Dat
 */
import com.mycompany.myapp.config.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherAssignmentDAO {

    // Lấy danh sách toàn bộ lớp học kèm lịch và giáo viên đang phụ trách
    public List<Map<String, Object>> getAllClassesWithAssignments() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.class_id, c.class_name, s.subject_name, " +
                     "LISTAGG(cs.day_of_week || ' (' || cs.start_time || '-' || cs.end_time || ')', ', ') WITHIN GROUP (ORDER BY cs.day_of_week) as schedule, " +
                     "u.full_name as teacher_name " +
                     "FROM STUDY_CLASS c " +
                     "JOIN SUBJECT s ON c.subject_id = s.subject_id " +
                     "LEFT JOIN CLASS_SCHEDULE cs ON c.class_id = cs.class_id AND cs.is_deleted = 0 " +
                     "LEFT JOIN TEACHING_ASSIGNMENT ta ON c.class_id = ta.class_id AND ta.is_deleted = 0 " +
                     "LEFT JOIN USERS u ON ta.teacher_id = u.user_id " +
                     "WHERE c.is_deleted = 0 " +
                     "GROUP BY c.class_id, c.class_name, s.subject_name, u.full_name " +
                     "ORDER BY c.class_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("class_id", rs.getInt("class_id"));
                map.put("class_name", rs.getNString("class_name"));
                map.put("subject_name", rs.getNString("subject_name"));
                map.put("schedule", rs.getString("schedule") != null ? rs.getString("schedule") : "Chưa có lịch");
                map.put("teacher_name", rs.getNString("teacher_name") != null ? rs.getNString("teacher_name") : "Chưa phân công");
                list.add(map);
            }
        }
        return list;
    }

    // Lấy danh sách giáo viên để đưa vào ComboBox
    public List<Map<String, Object>> getAvailableTeachers() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, tp.major " +
                     "FROM USERS u JOIN TEACHER_PROFILE tp ON u.user_id = tp.teacher_id " +
                     "WHERE u.is_deleted = 0";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("user_id", rs.getInt("user_id"));
                map.put("full_name", rs.getNString("full_name"));
                map.put("major", rs.getNString("major"));
                list.add(map);
            }
        }
        return list;
    }

    // Gán giáo viên vào lớp
    public void assignTeacher(int teacherId, int classId) throws SQLException {
        String sql = "INSERT INTO TEACHING_ASSIGNMENT (teacher_id, class_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, classId);
            ps.executeUpdate();
        }
    }

    // Hủy phân công cũ
    public void removeAssignment(int classId) throws SQLException {
        String sql = "UPDATE TEACHING_ASSIGNMENT SET is_deleted = 1, updated_at = SYSDATE WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.executeUpdate();
        }
    }
}