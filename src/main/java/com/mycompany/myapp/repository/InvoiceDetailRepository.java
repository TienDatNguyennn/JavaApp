package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import java.sql.*;
import java.util.*;

public class InvoiceDetailRepository {

    /** Danh sách học viên kèm tổng hợp hóa đơn (dùng cho bảng bên trái). */
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
                    row.put("student_id",    rs.getInt("student_id"));
                    row.put("full_name",      rs.getNString("full_name"));
                    row.put("invoice_count",  rs.getInt("invoice_count"));
                    row.put("total_fee",      rs.getDouble("total_fee"));
                    row.put("total_paid",     rs.getDouble("total_paid"));
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
                    row.put("invoice_id",     rs.getInt("invoice_id"));
                    row.put("created_at",     rs.getDate("created_at"));
                    row.put("total_amount",   rs.getDouble("total_amount"));
                    row.put("discount_amt",   rs.getDouble("discount_amt"));
                    row.put("final_amount",   rs.getDouble("final_amount"));
                    row.put("amount_paid",    rs.getDouble("amount_paid"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("status",         rs.getString("status"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /** Chi tiết hóa đơn (INVOICE_DETAILS) theo invoice_id. */
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
                    row.put("class_id",   rs.getInt("class_id"));
                    row.put("class_name", rs.getNString("class_name"));
                    row.put("amount",     rs.getDouble("amount"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /** Các lớp chưa có trong hóa đơn (dùng cho combobox thêm mới). */
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
                    row.put("class_id",    rs.getInt("class_id"));
                    row.put("class_name",  rs.getNString("class_name"));
                    row.put("tuition_fee", rs.getDouble("tuition_fee"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Tạo hóa đơn trống cho học viên, trả về invoice_id mới.
     * Dùng khi cần thêm chi tiết mà chưa có hóa đơn sẵn.
     */
    public int createBlankInvoice(int studentId) throws SQLException {
        Connection c = DBConnection.getConnection();

        // 1. Lấy ID mới (MAX+1) — cùng connection với INSERT bên dưới
        int newId;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT NVL(MAX(invoice_id),0)+1 FROM INVOICE");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            newId = rs.getInt(1);
        }

        // 2. Chèn hóa đơn — staff_id lấy từ SessionStore
        int staffId = com.mycompany.myapp.utils.SessionStore.getUserId();
        if (staffId <= 0) staffId = 1; // fallback nếu chưa login

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
        return newId;
    }

    public boolean insert(int invoiceId, int classId, double amount) throws SQLException {
        String sql = "INSERT INTO INVOICE_DETAILS (invoice_id, class_id, amount) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.setInt(2, classId);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(int invoiceId, int classId, double amount) throws SQLException {
        String sql = "UPDATE INVOICE_DETAILS SET amount = ? " +
                     "WHERE invoice_id = ? AND class_id = ? AND is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, invoiceId);
            ps.setInt(3, classId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Soft-delete: đặt amount=0 trước để trigger trg_sync_invoice_total
     * tự động trừ số tiền khỏi INVOICE.total_amount.
     */
    public boolean softDelete(int invoiceId, int classId) throws SQLException {
        String sql = "UPDATE INVOICE_DETAILS SET amount = 0, is_deleted = 1 " +
                     "WHERE invoice_id = ? AND class_id = ? AND is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        }
    }
}
