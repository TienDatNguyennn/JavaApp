package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class ConcurrencyAttendanceRepository {

    /**
     * Xử lý lưu điểm danh an toàn song song dưới Oracle Database (Đã sửa lỗi ORA-00904)
     */
    public boolean processAttendanceWithLock(int scheduleId, int studentId, String qrToken) {
        // SỬA ĐỔI: Thay thế cột attendance_date bằng attendance_time và created_at
        String sql = "INSERT INTO ATTENDANCE (schedule_id, student_id, status, qr_token, attendance_time, created_at) "
                   + "VALUES (?, ?, 'PRESENT', ?, CURRENT_TIMESTAMP, SYSDATE)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Bật tính năng quản trị Transaction thủ công để rollback khi trùng

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, scheduleId);
                ps.setInt(2, studentId);
                ps.setString(3, qrToken);
                ps.executeUpdate();
                
                conn.commit(); // Lưu vĩnh viễn vào DB nếu mượt mà
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                conn.rollback(); // Hủy bỏ thao tác thừa nếu học sinh cố tình spam
                return false;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
} 