package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.SubjectDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    /**
     * Lấy toàn bộ danh sách môn học từ bảng SUBJECT
     * Kết quả trả về là List các DTO đã được chuẩn hóa cho Giao diện.
     */
    public List<SubjectDTO> getAllSubjects() {
        List<SubjectDTO> subjectList = new ArrayList<>();
        
        // Câu lệnh SQL lấy các cột khớp 100% với file .sql của team
        String sql = "SELECT subject_id, subject_name, description, is_deleted FROM SUBJECT ORDER BY subject_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SubjectDTO subject = new SubjectDTO();
                
                // 1. Đổ dữ liệu thô từ các cột NUMBER, NVARCHAR2 vào DTO
                subject.setSubjectId(rs.getInt("subject_id"));
                subject.setSubjectName(rs.getString("subject_name"));
                subject.setDescription(rs.getString("description"));
                
                // 2. Logic BA: Chuyển đổi trạng thái xóa mềm sang ngôn ngữ hiển thị
                int isDeleted = rs.getInt("is_deleted");
                if (isDeleted == 0) {
                    subject.setStatus("Đang giảng dạy");
                } else {
                    subject.setStatus("Ngừng đào tạo");
                }

                subjectList.add(subject);
            }
        } catch (Exception e) {
            System.err.println("Loi khi truy van danh sach mon hoc!");
            e.printStackTrace();
        }
        return subjectList;
    }
    
    // ==========================================
    // 1. HÀM THÊM MỚI (INSERT)
    // ==========================================
    public boolean insertSubject(SubjectDTO subject) {
        // subject_id được Oracle tự động tăng (GENERATED AS IDENTITY) nên không cần truyền vào
        String sql = "INSERT INTO SUBJECT (subject_name, description) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getDescription());
            
            // executeUpdate() trả về số dòng bị ảnh hưởng, > 0 nghĩa là insert thành công
            return ps.executeUpdate() > 0; 
            
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm môn học mới!");
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 2. HÀM CẬP NHẬT (UPDATE)
    // ==========================================
    // ==========================================
    // 2. HÀM CẬP NHẬT (UPDATE) - ĐÃ FIX TRẠNG THÁI
    // ==========================================
    public boolean updateSubject(SubjectDTO subject) {
        // Bổ sung thêm is_deleted = ? vào câu SQL
        String sql = "UPDATE SUBJECT SET subject_name = ?, description = ?, is_deleted = ?, updated_at = SYSDATE WHERE subject_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getDescription());
            
            // Logic ép kiểu: Đang giảng dạy -> 0, Ngừng đào tạo -> 1
            int isDeleted = (subject.getStatus() != null && subject.getStatus().equals("Đang giảng dạy")) ? 0 : 1;
            ps.setInt(3, isDeleted);
            
            ps.setInt(4, subject.getSubjectId());
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật môn học!");
            e.printStackTrace();
            return false;
        }
    }

    // ==========================================
    // 3. HÀM XÓA MỀM (SOFT DELETE)
    // ==========================================
    public boolean deleteSubject(int subjectId) {
        // Tư duy BA: Không xóa hẳn dữ liệu bằng lệnh DELETE để giữ lịch sử hóa đơn/điểm số
        // Chỉ cập nhật is_deleted = 1 (1 là Ngừng đào tạo, 0 là Đang giảng dạy)
        String sql = "UPDATE SUBJECT SET is_deleted = 1, updated_at = SYSDATE WHERE subject_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa môn học!");
            e.printStackTrace();
            return false;
        }
    }
}