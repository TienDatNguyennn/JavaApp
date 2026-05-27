package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final int LOCK_WAIT_SECONDS = 3;
    private static final int DEMO_DELAY_MS = 5000;

    public List<Student> findAllActive() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM STUDENT WHERE is_deleted = 0 ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }

        return list;
    }

    public void insert(Student s) throws SQLException {
        String sql =
            "INSERT INTO STUDENT " +
            "(student_id, full_name, dob, gender, phone, parent_name, parent_phone, address, managed_by) " +
            "VALUES ((SELECT NVL(MAX(student_id),0)+1 FROM STUDENT), ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, s.getFullName());
            ps.setDate(2, new java.sql.Date(s.getDob().getTime()));
            ps.setString(3, s.getGender());
            ps.setString(4, s.getPhone());
            ps.setNString(5, s.getParentName());
            ps.setString(6, s.getParentPhone());
            ps.setNString(7, s.getAddress());

            if (s.getManagedBy() != null && s.getManagedBy() > 0) {
                ps.setInt(8, s.getManagedBy());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.executeUpdate();
        }
    }

    /*
     * =========================================================
     * UPDATE BÌNH THƯỜNG
     * Dùng khi không demo deadlock.
     * =========================================================
     */
    public void update(Student s) throws SQLException {
        String sql =
            "UPDATE STUDENT SET full_name=?, dob=?, gender=?, phone=?, " +
            "parent_name=?, parent_phone=?, updated_at=SYSDATE " +
            "WHERE student_id=? AND is_deleted=0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, s.getFullName());
            ps.setDate(2, new java.sql.Date(s.getDob().getTime()));
            ps.setString(3, s.getGender());
            ps.setString(4, s.getPhone());
            ps.setNString(5, s.getParentName());
            ps.setString(6, s.getParentPhone());
            ps.setInt(7, s.getStudentId());

            ps.executeUpdate();
        }
    }

    /*
     * =========================================================
     * MODULE 5 - DEADLOCK DEMO TRỰC TIẾP TRÊN CHỨC NĂNG SỬA HỌC VIÊN
     *
     * Bên học viên luôn khóa đúng thứ tự:
     * STUDENT -> INVOICE
     *
     * Khi bên học phí khóa ngược:
     * INVOICE -> STUDENT
     *
     * => sinh deadlock / lock wait timeout.
     *
     * Khi bên học phí fix lại:
     * STUDENT -> INVOICE
     *
     * => hết deadlock.
     * =========================================================
     */
    public void updateForDeadlockDemo(Student s, int invoiceId) throws SQLException {
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            log("STUDENT", "BEGIN TRANSACTION");
            log("STUDENT", "LOCK 1: STUDENT student_id = " + s.getStudentId());

            lockStudent(conn, s.getStudentId());

            log("STUDENT", "LOCK STUDENT thành công");
            log("STUDENT", "Giữ lock STUDENT " + DEMO_DELAY_MS + "ms để tạo tranh chấp khóa");

            sleepDemo(DEMO_DELAY_MS);

            log("STUDENT", "LOCK 2: INVOICE invoice_id = " + invoiceId);

            lockInvoice(conn, invoiceId);

            log("STUDENT", "LOCK INVOICE thành công");

            String sql =
                "UPDATE STUDENT SET full_name=?, dob=?, gender=?, phone=?, " +
                "parent_name=?, parent_phone=?, updated_at=SYSDATE " +
                "WHERE student_id=? AND is_deleted=0";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setNString(1, s.getFullName());
                ps.setDate(2, new java.sql.Date(s.getDob().getTime()));
                ps.setString(3, s.getGender());
                ps.setString(4, s.getPhone());
                ps.setNString(5, s.getParentName());
                ps.setString(6, s.getParentPhone());
                ps.setInt(7, s.getStudentId());
                ps.executeUpdate();
            }

            touchInvoice(conn, invoiceId);

            conn.commit();
            log("STUDENT", "COMMIT thành công");

        } catch (Exception e) {
            rollbackQuietly(conn, "STUDENT", e);
            throw new SQLException(
                "[STUDENT] Demo deadlock: cập nhật học viên thất bại, đã rollback. Lỗi gốc: " + e.getMessage(),
                e
            );

        } finally {
            close(conn);
        }
    }

    public void softDelete(int studentId) throws SQLException {
        String sql =
            "UPDATE STUDENT SET is_deleted = 1, updated_at = SYSDATE WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
    }

    /*
     * =========================================================
     * Tìm hóa đơn gần nhất của học viên để demo trực tiếp
     * =========================================================
     */
    public int findLatestInvoiceIdByStudent(int studentId) throws SQLException {
        String sql =
            "SELECT invoice_id " +
            "FROM ( " +
            "    SELECT invoice_id " +
            "    FROM INVOICE " +
            "    WHERE student_id = ? AND is_deleted = 0 " +
            "    ORDER BY created_at DESC " +
            ") " +
            "WHERE ROWNUM = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("invoice_id");
                }
            }
        }

        throw new SQLException("Học viên ID = " + studentId + " chưa có hóa đơn để demo deadlock.");
    }

    /*
     * =========================================================
     * LOCK STUDENT
     * =========================================================
     */
    private void lockStudent(Connection conn, int studentId) throws SQLException {
        String sql =
            "SELECT student_id " +
            "FROM STUDENT " +
            "WHERE student_id = ? AND is_deleted = 0 " +
            "FOR UPDATE WAIT " + LOCK_WAIT_SECONDS;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy học viên ID = " + studentId);
                }
            }
        }
    }

    /*
     * =========================================================
     * LOCK INVOICE
     * =========================================================
     */
    private void lockInvoice(Connection conn, int invoiceId) throws SQLException {
        String sql =
            "SELECT invoice_id " +
            "FROM INVOICE " +
            "WHERE invoice_id = ? AND is_deleted = 0 " +
            "FOR UPDATE WAIT " + LOCK_WAIT_SECONDS;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy hóa đơn ID = " + invoiceId);
                }
            }
        }
    }

    private void touchInvoice(Connection conn, int invoiceId) throws SQLException {
        String sql =
            "UPDATE INVOICE " +
            "SET updated_at = SYSDATE " +
            "WHERE invoice_id = ? AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.executeUpdate();
        }
    }

    private void sleepDemo(long millis) throws SQLException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Demo sleep bị gián đoạn", e);
        }
    }

    private void rollbackQuietly(Connection conn, String module, Exception e) {
        try {
            if (conn != null) {
                conn.rollback();
                log(module, "ROLLBACK do lỗi: " + e.getMessage());
            }
        } catch (Exception rollbackEx) {
            log(module, "ROLLBACK ERROR: " + rollbackEx.getMessage());
        }
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void log(String module, String message) {
        System.out.println("[" + module + "] " + message);
    }

    private Student mapResultSetToEntity(ResultSet rs) throws SQLException {
        Student s = new Student();

        s.setStudentId(rs.getInt("student_id"));
        s.setFullName(rs.getNString("full_name"));
        s.setDob(rs.getDate("dob"));
        s.setGender(rs.getString("gender"));
        s.setPhone(rs.getString("phone"));
        s.setParentName(rs.getNString("parent_name"));
        s.setParentPhone(rs.getString("parent_phone"));
        s.setAddress(rs.getNString("address"));

        int managedBy = rs.getInt("managed_by");
        s.setManagedBy(rs.wasNull() ? null : managedBy);

        return s;
    }
}