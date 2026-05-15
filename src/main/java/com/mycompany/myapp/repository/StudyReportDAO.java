package com.mycompany.myapp.repository; // Đổi lại package của bạn

import com.mycompany.myapp.model.StudyReportDTO;
import com.mycompany.myapp.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudyReportDAO {
    
    public List<StudyReportDTO> getAllReports() {
        List<StudyReportDTO> list = new ArrayList<>();
        
        // TODO: SỬA LẠI TÊN BẢNG VÀ TÊN CỘT DƯỚI ĐÂY CHO ĐÚNG VỚI DATABASE CỦA TEAM BẠN
        // Câu lệnh mẫu đang JOIN 3 bảng: Học Viên (h), Điểm (d), Lớp học (l)
        String sql = "SELECT h.student_id, h.full_name, l.class_name, d.final_score " +
                     "FROM STUDENT h " +
                     "JOIN COURSE_RESULT d ON h.student_id = d.student_id " +
                     "JOIN STUDY_CLASS l ON d.class_id = l.class_id";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                StudyReportDTO report = new StudyReportDTO(
                    rs.getString("student_id"), // Đổi lại tên cột mã học viên
                    rs.getString("full_name"),  // Đổi lại tên cột họ tên
                    rs.getString("class_name"), // Đổi lại tên cột tên lớp
                    rs.getDouble("final_score") // Đổi lại tên cột điểm
                );
                list.add(report);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load dữ liệu báo cáo học tập!");
            e.printStackTrace();
        }
        return list;
    }
}