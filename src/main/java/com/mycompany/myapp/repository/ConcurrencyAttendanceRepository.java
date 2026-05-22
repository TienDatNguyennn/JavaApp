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
        String sql = "INSERT INTO ATTENDANCE (schedule_id, student_id, status, qr_token, attendance_time, created_at) "
                   + "VALUES (?, ?, 'PRESENT', ?, CURRENT_TIMESTAMP, SYSDATE)";

        // Mỗi điểm danh cần connection độc lập để có thể rollback riêng lẻ
        // mà không ảnh hưởng đến shared connection của ứng dụng.
        try (Connection conn = DBConnection.getNewConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, scheduleId);
                ps.setInt(2, studentId);
                ps.setString(3, qrToken);
                ps.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                conn.rollback();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
} 