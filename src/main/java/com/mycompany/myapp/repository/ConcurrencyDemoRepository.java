package com.mycompany.myapp.repository;

import java.sql.*;

/**
 * Repository phục vụ demo 4 vấn đề truy xuất đồng thời:
 * Lost Update, Unrepeatable Read, Phantom Read, Deadlock.
 *
 * Mỗi method nhận Connection từ ngoài để caller kiểm soát
 * transaction boundary và isolation level.
 */
public class ConcurrencyDemoRepository {

    // ════════════════════════════════════════════════════════
    // LOST UPDATE  (dùng bảng INVOICE – cột amount_paid)
    // ════════════════════════════════════════════════════════

    /** Đọc amount_paid KHÔNG khóa hàng (dùng cho demo "có vấn đề"). */
    public double readAmountPaid(Connection conn, int invoiceId) throws SQLException {
        String sql = "SELECT amount_paid FROM INVOICE WHERE invoice_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("amount_paid");
                throw new SQLException("Không tìm thấy invoice_id=" + invoiceId);
            }
        }
    }

    /** Đọc amount_paid VÀ khóa hàng (FOR UPDATE) để bảo vệ khỏi lost update. */
    public double readAmountPaidForUpdate(Connection conn, int invoiceId) throws SQLException {
        String sql = "SELECT amount_paid FROM INVOICE WHERE invoice_id = ? AND is_deleted = 0 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("amount_paid");
                throw new SQLException("Không tìm thấy invoice_id=" + invoiceId);
            }
        }
    }

    /** Ghi đè amount_paid bằng giá trị mới. */
    public void updateAmountPaid(Connection conn, int invoiceId, double newAmount) throws SQLException {
        String sql = "UPDATE INVOICE SET amount_paid = ?, updated_at = SYSDATE "
                   + "WHERE invoice_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newAmount);
            ps.setInt(2, invoiceId);
            ps.executeUpdate();
        }
    }

    // ════════════════════════════════════════════════════════
    // UNREPEATABLE READ  (dùng bảng COURSE_RESULT – cột final_score)
    // ════════════════════════════════════════════════════════

    /** Đọc final_score của một kết quả học tập. */
    public double readFinalScore(Connection conn, int resultId) throws SQLException {
        String sql = "SELECT NVL(final_score, 0) AS final_score FROM COURSE_RESULT "
                   + "WHERE result_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resultId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("final_score");
                throw new SQLException("Không tìm thấy result_id=" + resultId);
            }
        }
    }

    /** Cập nhật điểm final_score. */
    public void updateFinalScore(Connection conn, int resultId, double newScore) throws SQLException {
        String sql = "UPDATE COURSE_RESULT SET final_score = ?, updated_at = SYSDATE "
                   + "WHERE result_id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newScore);
            ps.setInt(2, resultId);
            ps.executeUpdate();
        }
    }

    // ════════════════════════════════════════════════════════
    // PHANTOM READ  (dùng bảng STUDENT – đếm hàng)
    // ════════════════════════════════════════════════════════

    /** Đếm số học sinh đang hoạt động. */
    public int countActiveStudents(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Chèn một học sinh tạm để tạo phantom row. */
    public void insertTempStudent(Connection conn, String fullName) throws SQLException {
        String sql = "INSERT INTO STUDENT (full_name, dob, parent_name, parent_phone) "
                   + "VALUES (?, SYSDATE, N'PhantomTest', '0000000001')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, fullName);
            ps.executeUpdate();
        }
    }

    /** Xóa mềm các học sinh tạm sau khi demo xong. */
    public void deleteTempStudents(Connection conn, String namePrefix) throws SQLException {
        String sql = "UPDATE STUDENT SET is_deleted = 1, updated_at = SYSDATE "
                   + "WHERE full_name LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, namePrefix + "%");
            ps.executeUpdate();
        }
    }

    // ════════════════════════════════════════════════════════
    // DEADLOCK  (khóa INVOICE rồi STUDENT theo thứ tự ngược nhau)
    // ════════════════════════════════════════════════════════

    /** Khóa hàng trong bảng INVOICE (SELECT FOR UPDATE). */
    public void lockInvoiceRow(Connection conn, int invoiceId) throws SQLException {
        String sql = "SELECT invoice_id FROM INVOICE WHERE invoice_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy invoice_id=" + invoiceId);
            }
        }
    }

    /** Khóa hàng trong bảng STUDENT (SELECT FOR UPDATE). */
    public void lockStudentRow(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT student_id FROM STUDENT WHERE student_id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy student_id=" + studentId);
            }
        }
    }
}
