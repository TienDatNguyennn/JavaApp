package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;

import java.sql.*;
import java.util.*;

public class InvoiceDetailRepository {

    /*
     * =========================================================
     * MODULE 5 - CHỌN CHẾ ĐỘ DEMO TẠI ĐÂY
     *
     * FIXED:
     *     Bản đúng, dùng khi sản phẩm chạy bình thường.
     *     Học phí khóa STUDENT -> INVOICE.
     *
     * ERROR_DEADLOCK:
     *     Bản lỗi để demo.
     *     Học phí khóa INVOICE -> STUDENT.
     *
     * Khi demo:
     *     1. Đổi DEMO_MODE = ERROR_DEADLOCK để demo lỗi.
     *     2. Đổi DEMO_MODE = FIXED để demo bản fix.
     * =========================================================
     */
    private enum DemoMode {
        FIXED,
        ERROR_DEADLOCK
    }

    private static final DemoMode DEMO_MODE = DemoMode.ERROR_DEADLOCK;

    private static final int LOCK_WAIT_SECONDS = 3;
    private static final int DEMO_DELAY_MS = 5000;

    /** Danh sách học viên kèm tổng hợp hóa đơn. */
    public List<Map<String, Object>> findStudentSummary(String keyword) throws SQLException {
        String sql =
            "SELECT s.student_id, s.full_name, " +
            "       COUNT(DISTINCT i.invoice_id)          AS invoice_count, " +
            "       NVL(SUM(i.final_amount), 0)            AS total_fee, " +
            "       NVL(SUM(i.amount_paid),  0)            AS total_paid, " +
            "       CASE " +
            "         WHEN COUNT(DISTINCT i.invoice_id) = 0 THEN 'NONE' " +
            "         WHEN NVL(SUM(i.amount_paid),0) >= NVL(SUM(i.final_amount),1) THEN 'PAID' " +
            "         WHEN NVL(SUM(i.amount_paid),0) > 0 THEN 'PARTIAL' " +
            "         ELSE 'UNPAID' END AS overall_status " +
            "FROM STUDENT s " +
            "LEFT JOIN INVOICE i ON s.student_id = i.student_id AND i.is_deleted = 0 " +
            "WHERE s.is_deleted = 0 " +
            "  AND (? IS NULL " +
            "       OR UPPER(s.full_name) LIKE UPPER(?) " +
            "       OR CAST(s.student_id AS VARCHAR2(20)) LIKE ?) " +
            "GROUP BY s.student_id, s.full_name " +
            "ORDER BY s.full_name";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (keyword == null || keyword.isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(1, "X");
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("student_id", rs.getInt("student_id"));
                    row.put("full_name", rs.getNString("full_name"));
                    row.put("invoice_count", rs.getInt("invoice_count"));
                    row.put("total_fee", rs.getDouble("total_fee"));
                    row.put("total_paid", rs.getDouble("total_paid"));
                    row.put("overall_status", rs.getString("overall_status"));
                    list.add(row);
                }
            }
        }

        return list;
    }

    /** Danh sách hóa đơn của một học viên. */
    public List<Map<String, Object>> findInvoicesByStudent(int studentId) throws SQLException {
        String sql =
            "SELECT i.invoice_id, i.created_at, i.total_amount, i.discount_amt, " +
            "       i.final_amount, i.amount_paid, i.payment_method, i.status " +
            "FROM INVOICE i " +
            "WHERE i.student_id = ? AND i.is_deleted = 0 " +
            "ORDER BY i.created_at DESC";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("invoice_id", rs.getInt("invoice_id"));
                    row.put("created_at", rs.getDate("created_at"));
                    row.put("total_amount", rs.getDouble("total_amount"));
                    row.put("discount_amt", rs.getDouble("discount_amt"));
                    row.put("final_amount", rs.getDouble("final_amount"));
                    row.put("amount_paid", rs.getDouble("amount_paid"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("status", rs.getString("status"));
                    list.add(row);
                }
            }
        }

        return list;
    }

    /** Chi tiết hóa đơn theo invoice_id. */
    public List<Map<String, Object>> findDetailsByInvoice(int invoiceId) throws SQLException {
        String sql =
            "SELECT id.invoice_id, id.class_id, sc.class_name, id.amount " +
            "FROM INVOICE_DETAILS id " +
            "JOIN STUDY_CLASS sc ON id.class_id = sc.class_id " +
            "WHERE id.invoice_id = ? AND id.is_deleted = 0 " +
            "ORDER BY sc.class_name";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("invoice_id", rs.getInt("invoice_id"));
                    row.put("class_id", rs.getInt("class_id"));
                    row.put("class_name", rs.getNString("class_name"));
                    row.put("amount", rs.getDouble("amount"));
                    list.add(row);
                }
            }
        }

        return list;
    }

    /** Các lớp chưa có trong hóa đơn. */
    public List<Map<String, Object>> findAvailableClasses(int invoiceId) throws SQLException {
        String sql =
            "SELECT sc.class_id, sc.class_name, NVL(sc.tuition_fee, 0) AS tuition_fee " +
            "FROM STUDY_CLASS sc " +
            "WHERE sc.is_deleted = 0 " +
            "  AND sc.class_id NOT IN ( " +
            "      SELECT class_id FROM INVOICE_DETAILS " +
            "      WHERE invoice_id = ? AND is_deleted = 0 " +
            "  ) " +
            "ORDER BY sc.class_name";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("class_id", rs.getInt("class_id"));
                    row.put("class_name", rs.getNString("class_name"));
                    row.put("tuition_fee", rs.getDouble("tuition_fee"));
                    list.add(row);
                }
            }
        }

        return list;
    }

    /*
     * Tạo hóa đơn trống cho học viên.
     * Giữ nguyên logic của bạn nhưng bổ sung transaction để tránh insert nửa chừng.
     */
    public int createBlankInvoice(int studentId) throws SQLException {
        Connection c = null;

        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            int newId;

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT NVL(MAX(invoice_id),0)+1 FROM INVOICE");
                 ResultSet rs = ps.executeQuery()) {

                rs.next();
                newId = rs.getInt(1);
            }

            int staffId = com.mycompany.myapp.utils.SessionStore.getUserId();
            if (staffId <= 0) {
                staffId = 1;
            }

            String sql =
                "INSERT INTO INVOICE " +
                "  (invoice_id, student_id, staff_id, total_amount, discount_amt, final_amount, " +
                "   amount_paid, payment_method, status, api_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, 0, 0, 0, 0, 'CASH', 'UNPAID', 'PENDING', SYSDATE, SYSDATE)";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, newId);
                ps.setInt(2, studentId);
                ps.setInt(3, staffId);
                ps.executeUpdate();
            }

            c.commit();
            return newId;

        } catch (Exception e) {
            rollbackQuietly(c, "INVOICE", e);
            throw new SQLException("Lỗi tạo hóa đơn trống, transaction đã rollback: " + e.getMessage(), e);

        } finally {
            close(c);
        }
    }

    public boolean insert(int invoiceId, int classId, double amount) throws SQLException {
        return executeInvoiceDetailTransaction(
            "INSERT",
            invoiceId,
            classId,
            amount,
            (conn) -> {
                String sql =
                    "INSERT INTO INVOICE_DETAILS (invoice_id, class_id, amount) " +
                    "VALUES (?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, invoiceId);
                    ps.setInt(2, classId);
                    ps.setDouble(3, amount);
                    return ps.executeUpdate() > 0;
                }
            }
        );
    }

    public boolean update(int invoiceId, int classId, double amount) throws SQLException {
        return executeInvoiceDetailTransaction(
            "UPDATE",
            invoiceId,
            classId,
            amount,
            (conn) -> {
                String sql =
                    "UPDATE INVOICE_DETAILS SET amount = ? " +
                    "WHERE invoice_id = ? AND class_id = ? AND is_deleted = 0";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDouble(1, amount);
                    ps.setInt(2, invoiceId);
                    ps.setInt(3, classId);
                    return ps.executeUpdate() > 0;
                }
            }
        );
    }

    public boolean softDelete(int invoiceId, int classId) throws SQLException {
        return executeInvoiceDetailTransaction(
            "SOFT_DELETE",
            invoiceId,
            classId,
            0,
            (conn) -> {
                String sql =
                    "UPDATE INVOICE_DETAILS SET amount = 0, is_deleted = 1 " +
                    "WHERE invoice_id = ? AND class_id = ? AND is_deleted = 0";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, invoiceId);
                    ps.setInt(2, classId);
                    return ps.executeUpdate() > 0;
                }
            }
        );
    }

    /*
     * =========================================================
     * CORE TRANSACTION CHO INSERT / UPDATE / SOFT_DELETE
     *
     * Đây là phần quan trọng nhất để demo:
     * - ERROR_DEADLOCK: INVOICE -> STUDENT
     * - FIXED: STUDENT -> INVOICE
     * =========================================================
     */
    private boolean executeInvoiceDetailTransaction(
        String actionName,
        int invoiceId,
        int classId,
        double amount,
        SqlAction action
    ) throws SQLException {

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int studentId = getStudentIdByInvoice(conn, invoiceId);

            log("INVOICE", "BEGIN TRANSACTION action=" + actionName);
            log("INVOICE", "DEMO_MODE = " + DEMO_MODE);
            log("INVOICE", "invoice_id=" + invoiceId + ", student_id=" + studentId + ", class_id=" + classId);

            acquireLocksByMode(conn, studentId, invoiceId);

            boolean ok = action.execute(conn);

            touchStudent(conn, studentId);
            touchInvoice(conn, invoiceId);

            conn.commit();

            log("INVOICE", "COMMIT action=" + actionName + " thành công");

            return ok;

        } catch (Exception e) {
            rollbackQuietly(conn, "INVOICE", e);

            throw new SQLException(
                "[INVOICE] " + actionName + " thất bại, transaction đã rollback. Lỗi gốc: " + e.getMessage(),
                e
            );

        } finally {
            close(conn);
        }
    }

    private void acquireLocksByMode(Connection conn, int studentId, int invoiceId) throws SQLException {
        if (DEMO_MODE == DemoMode.ERROR_DEADLOCK) {
            /*
             * =====================================================
             * CASE ERROR:
             * Bên học phí khóa INVOICE trước.
             * Bên học viên khóa STUDENT trước.
             *
             * Hai bên chạy cùng lúc sẽ tạo vòng chờ:
             * INVOICE giữ INVOICE, chờ STUDENT.
             * STUDENT giữ STUDENT, chờ INVOICE.
             * =====================================================
             */
            log("INVOICE", "CASE ERROR: LOCK 1 = INVOICE invoice_id=" + invoiceId);
            lockInvoice(conn, invoiceId);
            log("INVOICE", "LOCK INVOICE thành công");

            log("INVOICE", "Giữ lock INVOICE " + DEMO_DELAY_MS + "ms để tạo tranh chấp khóa");
            sleepDemo(DEMO_DELAY_MS);

            log("INVOICE", "CASE ERROR: LOCK 2 = STUDENT student_id=" + studentId);
            lockStudent(conn, studentId);
            log("INVOICE", "LOCK STUDENT thành công");

        } else {
            /*
             * =====================================================
             * CASE FIX:
             * Bên học phí khóa cùng thứ tự với bên học viên:
             * STUDENT -> INVOICE.
             *
             * Transaction sau chỉ chờ transaction trước ở lock đầu tiên,
             * không tạo vòng chờ.
             * =====================================================
             */
            log("INVOICE", "CASE FIX: LOCK 1 = STUDENT student_id=" + studentId);
            lockStudent(conn, studentId);
            log("INVOICE", "LOCK STUDENT thành công");

            log("INVOICE", "Giữ lock STUDENT " + DEMO_DELAY_MS + "ms");
            sleepDemo(DEMO_DELAY_MS);

            log("INVOICE", "CASE FIX: LOCK 2 = INVOICE invoice_id=" + invoiceId);
            lockInvoice(conn, invoiceId);
            log("INVOICE", "LOCK INVOICE thành công");
        }
    }

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

    private int getStudentIdByInvoice(Connection conn, int invoiceId) throws SQLException {
        String sql =
            "SELECT student_id " +
            "FROM INVOICE " +
            "WHERE invoice_id = ? AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        }

        throw new SQLException("Không tìm thấy student_id của hóa đơn ID = " + invoiceId);
    }

    private void touchStudent(Connection conn, int studentId) throws SQLException {
        String sql =
            "UPDATE STUDENT " +
            "SET updated_at = SYSDATE " +
            "WHERE student_id = ? AND is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
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

    @FunctionalInterface
    private interface SqlAction {
        boolean execute(Connection conn) throws SQLException;
    }
}