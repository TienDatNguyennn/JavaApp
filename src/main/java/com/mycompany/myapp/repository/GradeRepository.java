package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.CourseResultDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeRepository {

    // Lấy danh sách lớp của Giáo viên
    public List<Map<String, Object>> getClassesByTeacher(Connection conn, int teacherId) throws SQLException {
        List<Map<String, Object>> classes = new ArrayList<>();
        String sql = "SELECT sc.class_id, sc.class_name FROM STUDY_CLASS sc " +
                     "JOIN TEACHING_ASSIGNMENT ta ON sc.class_id = ta.class_id " +
                     "WHERE ta.teacher_id = ? AND sc.is_deleted = 0 AND ta.is_deleted = 0 " +
                     "ORDER BY sc.class_name";
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

    // Lấy danh sách điểm của một lớp học
    public List<CourseResultDTO> getStudentGrades(Connection conn, int classId) throws SQLException {
        List<CourseResultDTO> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, cr.result_id, cr.final_score, cr.rank " +
                     "FROM CLASS_PLACEMENT cp " +
                     "JOIN STUDENT s ON cp.student_id = s.student_id " +
                     "LEFT JOIN COURSE_RESULT cr ON cp.student_id = cr.student_id " +
                     "    AND cr.class_id = ? AND cr.is_deleted = 0 " +
                     "WHERE cp.class_id = ? AND cp.status = 'ACTIVE' AND cp.is_deleted = 0 AND s.is_deleted = 0 " +
                     "ORDER BY s.full_name ASC";
                     
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseResultDTO dto = new CourseResultDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    dto.setResultId(rs.getInt("result_id"));
                    
                    // Xử lý Double null an toàn
                    double score = rs.getDouble("final_score");
                    if (!rs.wasNull()) {
                        dto.setFinalScore(score);
                    }
                    dto.setRank(rs.getString("rank"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    // Insert điểm mới — dùng MAX+1 để tránh xung đột với identity sequence
    public void insertGrade(Connection conn, int studentId, int classId, double finalScore, String rank) throws SQLException {
        String sql = "INSERT INTO COURSE_RESULT (result_id, student_id, class_id, final_score, rank, is_deleted) " +
                     "VALUES ((SELECT NVL(MAX(result_id), 0) + 1 FROM COURSE_RESULT), ?, ?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            ps.setDouble(3, finalScore);
            ps.setString(4, rank);
            ps.executeUpdate();
        }
    }

    // Update điểm cũ
    public void updateGrade(Connection conn, int resultId, double finalScore, String rank) throws SQLException {
        String sql = "UPDATE COURSE_RESULT SET final_score = ?, rank = ? WHERE result_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, finalScore);
            ps.setString(2, rank);
            ps.setInt(3, resultId);
            ps.executeUpdate();
        }
    }
}