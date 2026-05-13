package com.mycompany.myapp.repository;

import com.mycompany.myapp.model.AcademicReportDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRepository {

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

    // Lấy báo cáo điểm chi tiết
    public List<AcademicReportDTO> getClassReport(Connection conn, int classId) throws SQLException {
        List<AcademicReportDTO> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, cr.final_score, cr.rank " +
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
                    AcademicReportDTO dto = new AcademicReportDTO();
                    dto.setStudentId(rs.getInt("student_id"));
                    dto.setFullName(rs.getString("full_name"));
                    
                    double score = rs.getDouble("final_score");
                    if (!rs.wasNull()) {
                        dto.setFinalScore(score);
                        dto.setStatus(score >= 5.0 ? "ĐẠT" : "KHÔNG ĐẠT");
                    } else {
                        dto.setStatus("CHƯA CÓ ĐIỂM");
                    }
                    
                    dto.setRank(rs.getString("rank"));
                    list.add(dto);
                }
            }
        }
        return list;
    }
}