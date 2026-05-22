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
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, i.promo_id, " +
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
        
        // Thêm logic ORDER BY: 
        // 1. Nếu mã học viên trùng khớp chính xác (i.student_id = số tìm kiếm) -> Ưu tiên số 1
        // 2. Tiếp tục sắp xếp theo trạng thái (UNPAID -> PARTIAL -> Khác) -> Ưu tiên số 2
        // 3. Cuối cùng mới sắp xếp theo ngày tạo giảm dần -> Ưu tiên số 3
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, i.promo_id, " +
            "       i.total_amount, i.discount_amt, i.final_amount, " +
            "       i.amount_paid, i.payment_method, i.status, " +
            "       i.api_status, i.created_at " +
            "FROM   INVOICE i " +
            "JOIN   STUDENT s ON i.student_id = s.student_id " +
            "WHERE  i.is_deleted = 0 " +
            "  AND  (? IS NULL OR UPPER(s.full_name) LIKE UPPER(?) OR i.student_id = ?) " +
            "  AND  (? IS NULL OR i.status = ?) " +
            "ORDER BY " +
            "  CASE WHEN i.student_id = ? THEN 1 ELSE 2 END, " + // <-- ƯU TIÊN ID CHÍNH XÁC LÊN ĐẦU
            "  CASE WHEN i.status = 'UNPAID' THEN 1 WHEN i.status = 'PARTIAL' THEN 2 ELSE 3 END, " +
            "  s.full_name ASC, " +                             
            "  i.created_at DESC";                               

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            int searchId = -1; // Biến tạm lưu trữ ID nếu keyword là số
            
            // 1. Thiết lập các tham số bộ lọc từ khóa (Tìm cả Tên và ID)
            if (keyword == null || keyword.trim().isEmpty() || keyword.equals("Nhập mã số học viên...")) {
                ps.setNull(1, Types.VARCHAR); 
                ps.setNull(2, Types.VARCHAR); 
                ps.setNull(3, Types.INTEGER);
            } else {
                String kv = keyword.trim();
                ps.setString(1, "X"); 
                ps.setString(2, "%" + kv + "%");
                try {
                    searchId = Integer.parseInt(kv);
                    ps.setInt(3, searchId);
                } catch (NumberFormatException e) {
                    ps.setInt(3, -1); 
                }
            }

            // 2. Thiết lập tham số bộ lọc Trạng thái hóa đơn
            if (status == null || status.isEmpty() || status.contains("Tất cả")) {
                ps.setNull(4, Types.VARCHAR); 
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(4, "X"); 
                ps.setString(5, status);
            }
            
            // 3. Thiết lập tham số cho mệnh đề ORDER BY (Tham số thứ 6)
            // Truyền ID tìm được vào đây để Oracle đối chiếu và đưa bản ghi đó lên đầu
            ps.setInt(6, searchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] findByFilter error: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public Invoice findById(int id) {
        String sql =
            "SELECT i.invoice_id, i.student_id, s.full_name, i.staff_id, i.promo_id, " +
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
public boolean insert(Invoice inv) throws SQLException {
        String sql =
            "INSERT INTO INVOICE " +
            "  (invoice_id, student_id, staff_id, promo_id, total_amount, discount_amt, final_amount, " +
            "   amount_paid, payment_method, status, api_status, created_at, updated_at) " +
            "VALUES ((SELECT NVL(MAX(invoice_id), 0) + 1 FROM INVOICE), " +
            "        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, inv.getStudentId());
            int staffId = inv.getStaffId();
            if (staffId <= 0) staffId = com.mycompany.myapp.utils.SessionStore.getUserId();
            if (staffId <= 0) staffId = 1; // fallback
            ps.setInt(2, staffId);

            if (inv.getPromoId() > 0) ps.setInt(3, inv.getPromoId());
            else                       ps.setNull(3, Types.INTEGER);

            ps.setDouble(4, inv.getTotalAmount());
            ps.setDouble(5, inv.getDiscountAmt());

            double finalAmt = inv.getTotalAmount() - inv.getDiscountAmt();
            ps.setDouble(6, finalAmt > 0 ? finalAmt : 0);

            ps.setDouble(7, inv.getAmountPaid());
            ps.setString(8, (inv.getPaymentMethod() == null || inv.getPaymentMethod().isEmpty())
                             ? "CASH" : inv.getPaymentMethod());

            String currentStatus = calcStatus(inv.getAmountPaid(), finalAmt);
            ps.setString(9, currentStatus);
            ps.setString(10, (inv.getApiStatus() == null) ? "PENDING" : inv.getApiStatus());

            return ps.executeUpdate() > 0;
        }
    }
    public boolean update(Invoice inv) {
        String sql =
            "UPDATE INVOICE " +
            "SET    student_id     = ?, " +
            "       staff_id       = ?, " +
            "       promo_id       = ?, " +
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
            if (inv.getPromoId() > 0) {
                ps.setInt(3, inv.getPromoId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setDouble(4, inv.getTotalAmount());
            ps.setDouble(5, inv.getDiscountAmt());
            ps.setDouble(6, inv.getFinalAmount());
            ps.setDouble(7, inv.getAmountPaid());
            ps.setString(8, inv.getPaymentMethod());
            ps.setString(9, calcStatus(inv.getAmountPaid(), inv.getFinalAmount()));
            ps.setInt   (10, inv.getInvoiceId());
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
        inv.setPromoId      (rs.getInt    ("promo_id"));
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