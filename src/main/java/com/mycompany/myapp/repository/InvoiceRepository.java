package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Invoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực hiện tất cả truy vấn DB liên quan đến bảng INVOICE & INVOICE_DETAILS
 * Cấu trúc mới: Join với bảng STUDENT thay vì USERS
 */
public class InvoiceRepository {

    // ── Lấy tất cả hóa đơn (kèm tên học viên từ bảng STUDENT) ────────────
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " + // Đổi sang bảng STUDENT
            "WHERE  i.is_deleted = 0 " +
            "ORDER BY i.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceRepository] findAll lỗi: " + e.getMessage());
        }
        return list;
    }
public List<Invoice> findByFilter(String keyword, String status) {
    List<Invoice> list = new ArrayList<>();
    // SQL mới: Tìm kiếm thông minh theo cả ID và Tên
    String sql =
        "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
        "       i.total_amount, i.discount_amt, i.final_amount, " +
        "       i.amount_paid, i.payment_method, i.status, " +
        "       i.api_status, i.created_at " +
        "FROM   INVOICE i " +
        "JOIN   STUDENT s ON i.student_id = s.student_id " +
        "WHERE  i.is_deleted = 0 " +
        "  AND  (? IS NULL OR ( " +
        "         UPPER(s.full_name) LIKE UPPER(?) OR " + // Tìm theo tên
        "         TO_CHAR(s.student_id) = ? " +           // Hoặc tìm chính xác theo mã ID
        "       )) " +
        "  AND  (? IS NULL OR i.status = ?) " +
        "ORDER BY i.created_at DESC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        // Xử lý keyword (Tham số 1, 2, 3 cho Keyword)
        if (keyword == null || keyword.trim().isEmpty()) {
            ps.setNull(1, Types.VARCHAR);
            ps.setNull(2, Types.VARCHAR);
            ps.setNull(3, Types.VARCHAR);
        } else {
            String kw = keyword.trim();
            ps.setString(1, "X");             // Kích hoạt điều kiện
            ps.setString(2, "%" + kw + "%");  // Cho LIKE tên
            ps.setString(3, kw);              // Cho so khớp ID
        }

        // Xử lý status (Tham số 4, 5 cho Status)
        if (status == null || status.trim().isEmpty() || "Tất cả".equals(status)) {
            ps.setNull(4, Types.VARCHAR);
            ps.setNull(5, Types.VARCHAR);
        } else {
            ps.setString(4, "X");
            ps.setString(5, status);
        }

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
    } catch (SQLException e) {
        System.err.println("[InvoiceRepository] findByFilter lỗi: " + e.getMessage());
    }
    return list;
}

    // ── Tìm 1 hóa đơn theo ID ──────────────────────────────────────
    public Invoice findById(int invoiceId) {
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " + // Đổi sang bảng STUDENT
            "WHERE  i.invoice_id = ? AND i.is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceRepository] findById lỗi: " + e.getMessage());
        }
        return null;
    }
// ── Cập nhật thanh toán (Cộng dồn số tiền đã nộp) ──
    public boolean updatePayment(int invoiceId, double newAmountPaid, String method) {
        // Logic mới: 
        // 1. amount_paid = số tiền cũ + số tiền mới nộp
        // 2. status dựa trên (số tiền cũ + số tiền mới nộp) so với final_amount
        String sql =
            "UPDATE INVOICE " +
            "SET    amount_paid    = amount_paid + ?, " + // Cộng dồn tiền mới vào tiền đã nộp
            "       payment_method = ?, " +
            "       updated_at     = SYSDATE, " +
            "       status = CASE " +
            "           WHEN (amount_paid + ?) >= final_amount THEN 'PAID' " +
            "           WHEN (amount_paid + ?) > 0             THEN 'PARTIAL' " +
            "           ELSE 'UNPAID' END " +
            "WHERE  invoice_id = ? AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, newAmountPaid); // Tiền nộp thêm
            ps.setString(2, method);
            ps.setDouble(3, newAmountPaid); // Dùng để so sánh trong CASE của SQL
            ps.setDouble(4, newAmountPaid); // Dùng để so sánh trong CASE của SQL
            ps.setInt   (5, invoiceId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepository] updatePayment lỗi: " + e.getMessage());
            return false;
        }
    }
    // ── Cập nhật api_status (Giữ nguyên) ───
    public boolean updateApiStatus(int invoiceId, String apiStatus) {
        String sql = "UPDATE INVOICE SET api_status = ? WHERE invoice_id = ? AND is_deleted = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, apiStatus);
            ps.setInt   (2, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepository] updateApiStatus lỗi: " + e.getMessage());
            return false;
        }
    }

    // ── Ánh xạ ResultSet → Invoice ──────────────────────────────────
    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId    (rs.getInt    ("invoice_id"));
        inv.setStudentId    (rs.getInt    ("student_id"));
        inv.setStudentName  (rs.getNString("full_name")); // Lấy từ cột full_name của bảng STUDENT
        inv.setStaffId      (rs.getInt    ("staff_id"));
        inv.setTotalAmount  (rs.getDouble("total_amount"));
        inv.setDiscountAmt  (rs.getDouble("discount_amt"));
        inv.setFinalAmount  (rs.getDouble("final_amount"));
        inv.setAmountPaid   (rs.getDouble("amount_paid"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setStatus       (rs.getString("status"));
        inv.setApiStatus    (rs.getString("api_status"));
        inv.setCreatedAt    (rs.getDate  ("created_at"));
        return inv;
    }
}