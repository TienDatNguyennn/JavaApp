package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Invoice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " +
            "WHERE  i.is_deleted = 0 " +
            "ORDER BY i.created_at DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] findAll: " + e.getMessage());
        }
        return list;
    }
    
    public String getStudentNameById(int id) {
        String sql = "SELECT full_name FROM STUDENT WHERE student_id = ? AND is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getNString("full_name");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy tên học sinh: " + e.getMessage());
        }
        return null;
    }

    public List<Invoice> findByFilter(String keyword, String status) {
        List<Invoice> list = new ArrayList<>();
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " +
            "WHERE  i.is_deleted = 0 " +
            "  AND  (? IS NULL OR UPPER(s.full_name) LIKE UPPER(?) OR i.student_id = ?) " +
            "  AND  (? IS NULL OR i.status = ?) " +
            "ORDER BY i.created_at DESC";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            if (keyword == null || keyword.trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR); ps.setNull(2, Types.VARCHAR); ps.setNull(3, Types.INTEGER);
            } else {
                String kv = keyword.trim();
                ps.setString(1, "X"); ps.setString(2, "%" + kv + "%");
                try {
                    ps.setInt(3, Integer.parseInt(kv));
                } catch (NumberFormatException e) {
                    ps.setInt(3, -1); 
                }
            }

            if (status == null || status.isEmpty() || status.contains("Tất cả")) {
                ps.setNull(4, Types.VARCHAR); ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(4, "X"); ps.setString(5, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Invoice findById(int id) {
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " +
            "WHERE  i.invoice_id = ? AND i.is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] findById: " + e.getMessage());
        }
        return null;
    }
public boolean insert(Invoice inv) {
        // Cập nhật câu lệnh SQL: 
        // 1. Loại bỏ is_deleted (để DB tự dùng DEFAULT 0)
        // 2. Đảm bảo status và api_status luôn có giá trị
        String sql =
            "INSERT INTO INVOICE " +
            "  (student_id, staff_id, total_amount, discount_amt, final_amount, " +
            "   amount_paid, payment_method, status, api_status, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";
            
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            // 1. student_id (NOT NULL)
            ps.setInt(1, inv.getStudentId());
            
            // 2. staff_id (NOT NULL)
            ps.setInt(2, inv.getStaffId());
            
            // 3. total_amount (NOT NULL)
            ps.setDouble(3, inv.getTotalAmount());
            
            // 4. discount_amt (Sử dụng giá trị từ model hoặc mặc định 0)
            ps.setDouble(4, inv.getDiscountAmt());
            
            // 5. final_amount (NOT NULL) - Nên tính toán lại để đảm bảo chính xác
            double finalAmt = inv.getTotalAmount() - inv.getDiscountAmt();
            ps.setDouble(5, finalAmt > 0 ? finalAmt : 0);
            
            // 6. amount_paid
            ps.setDouble(6, inv.getAmountPaid());
            
            // 7. payment_method (Có thể NULL nên cần kiểm tra)
            ps.setString(7, (inv.getPaymentMethod() == null || inv.getPaymentMethod().isEmpty()) 
                             ? "CASH" : inv.getPaymentMethod());
            
            // 8. status (NOT NULL) - Gọi hàm calcStatus để lấy giá trị chính xác
            String currentStatus = calcStatus(inv.getAmountPaid(), finalAmt);
            ps.setString(8, currentStatus);
            
            // 9. api_status (NOT NULL)
            ps.setString(9, (inv.getApiStatus() == null) ? "PENDING" : inv.getApiStatus());

            int result = ps.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            // In lỗi chi tiết từ Oracle để debug (Ví dụ: ORA-00001, ORA-02291...)
            System.err.println("[InvoiceRepo] insert error: " + e.getMessage());
            return false;
        }
    }
    public boolean update(Invoice inv) {
        String sql =
            "UPDATE INVOICE " +
            "SET    student_id     = ?, " +
            "       staff_id       = ?, " +
            "       total_amount   = ?, " +
            "       discount_amt   = ?, " +
            "       final_amount   = ?, " +
            "       amount_paid    = ?, " +
            "       payment_method = ?, " +
            "       status         = ? " +
            "WHERE  invoice_id = ? AND is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, inv.getStudentId());
            ps.setInt   (2, inv.getStaffId());
            ps.setDouble(3, inv.getTotalAmount());
            ps.setDouble(4, inv.getDiscountAmt());
            ps.setDouble(5, inv.getFinalAmount());
            ps.setDouble(6, inv.getAmountPaid());
            ps.setString(7, inv.getPaymentMethod());
            ps.setString(8, calcStatus(inv.getAmountPaid(), inv.getFinalAmount()));
            ps.setInt   (9, inv.getInvoiceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] update: " + e.getMessage());
            return false;
        }
    }

    public boolean softDelete(int invoiceId) {
        String sql = "UPDATE INVOICE SET is_deleted = 1 WHERE invoice_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] softDelete: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logic sửa đổi: Cập nhật nộp thêm tiền (Cộng dồn)
     */
    public boolean updatePayment(int invoiceId, double newAmountIn, String method) {
        // 1. Tìm hóa đơn cũ để lấy số tiền đã nộp trước đó
        Invoice current = findById(invoiceId);
        if (current == null) return false;

        // 2. Tính toán số tiền cộng dồn mới
        double totalPaidNew = current.getAmountPaid() + newAmountIn;
        
        // 3. Tính toán trạng thái mới dựa trên thực thu (final_amount)
        String newStatus = calcStatus(totalPaidNew, current.getFinalAmount());

        String sql =
            "UPDATE INVOICE " +
            "SET    amount_paid    = ?, " +
            "       payment_method = ?, " +
            "       status         = ?, " +
            "       updated_at     = SYSDATE " +
            "WHERE  invoice_id = ? AND is_deleted = 0";
            
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, totalPaidNew);
            ps.setString(2, method);
            ps.setString(3, newStatus);
            ps.setInt   (4, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] updatePayment: " + e.getMessage());
            return false;
        }
    }

    public boolean updateApiStatus(int invoiceId, String apiStatus) {
        String sql = "UPDATE INVOICE SET api_status = ? WHERE invoice_id = ? AND is_deleted = 0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, apiStatus);
            ps.setInt   (2, invoiceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] updateApiStatus: " + e.getMessage());
            return false;
        }
    }

    private String calcStatus(double paid, double finalAmt) {
        if (paid >= finalAmt && finalAmt > 0) return "PAID";
        if (paid > 0) return "PARTIAL"; // Cập nhật thêm trạng thái nộp một phần
        return "UNPAID";
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId    (rs.getInt    ("invoice_id"));
        inv.setStudentId    (rs.getInt    ("student_id"));
        inv.setStudentName  (rs.getNString("full_name"));
        inv.setStaffId      (rs.getInt    ("staff_id"));
        inv.setTotalAmount  (rs.getDouble ("total_amount"));
        inv.setDiscountAmt  (rs.getDouble ("discount_amt"));
        inv.setFinalAmount  (rs.getDouble ("final_amount"));
        inv.setAmountPaid   (rs.getDouble ("amount_paid"));
        inv.setPaymentMethod(rs.getString ("payment_method"));
        inv.setStatus       (rs.getString ("status"));
        inv.setApiStatus    (rs.getString ("api_status"));
        inv.setCreatedAt    (rs.getDate    ("created_at"));
        return inv;
    }
}