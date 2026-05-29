package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.model.InvoiceEmailDTO;
import com.mycompany.myapp.model.InvoiceEmailItemDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    // =====================================================
    // DEMO LOST UPDATE
    //
    // false: Bản lỗi - đọc amount_paid không khóa, dễ xảy ra Lost Update
    // true : Bản fix - dùng SELECT FOR UPDATE để khóa dòng INVOICE
    //
    // Khi demo lỗi: để false
    // Khi demo fix : đổi thành true rồi Clean and Build lại
    // =====================================================
    private static final boolean USE_LOCK_FIX = true;

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

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] findAll: " + e.getMessage());
        }

        return list;
    }

    public String getStudentNameById(int id) {
        String sql = "SELECT full_name FROM STUDENT WHERE student_id = ? AND is_deleted = 0";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getNString("full_name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy tên học sinh: " + e.getMessage());
        }

        return null;
    }

    public List<Invoice> findByFilter(String keyword, String status) {
        List<Invoice> list = new ArrayList<>();

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
                "  CASE WHEN i.student_id = ? THEN 1 ELSE 2 END, " +
                "  CASE WHEN i.status = 'UNPAID' THEN 1 WHEN i.status = 'PARTIAL' THEN 2 ELSE 3 END, " +
                "  s.full_name ASC, " +
                "  i.created_at DESC";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            int searchId = -1;

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

            if (status == null || status.isEmpty() || status.contains("Tất cả")) {
                ps.setNull(4, Types.VARCHAR);
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(4, "X");
                ps.setString(5, status);
            }

            ps.setInt(6, searchId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
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

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
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

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setInt(1, inv.getStudentId());

            int staffId = inv.getStaffId();

            if (staffId <= 0) {
                staffId = com.mycompany.myapp.utils.SessionStore.getUserId();
            }

            if (staffId <= 0) {
                staffId = 1;
            }

            ps.setInt(2, staffId);

            if (inv.getPromoId() > 0) {
                ps.setInt(3, inv.getPromoId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setDouble(4, inv.getTotalAmount());
            ps.setDouble(5, inv.getDiscountAmt());

            double finalAmt = inv.getTotalAmount() - inv.getDiscountAmt();
            ps.setDouble(6, finalAmt > 0 ? finalAmt : 0);

            ps.setDouble(7, inv.getAmountPaid());

            ps.setString(
                    8,
                    (inv.getPaymentMethod() == null || inv.getPaymentMethod().isEmpty())
                            ? "CASH"
                            : inv.getPaymentMethod()
            );

            String currentStatus = calcStatus(inv.getAmountPaid(), finalAmt);
            ps.setString(9, currentStatus);

            ps.setString(
                    10,
                    (inv.getApiStatus() == null) ? "PENDING" : inv.getApiStatus()
            );

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
                "       status         = ?, " +
                "       updated_at     = SYSDATE " +
                "WHERE  invoice_id = ? AND is_deleted = 0";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setInt(1, inv.getStudentId());
            ps.setInt(2, inv.getStaffId());

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
            ps.setInt(10, inv.getInvoiceId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] update: " + e.getMessage());
            return false;
        }
    }

    public boolean softDelete(int invoiceId) {
        String sql = "UPDATE INVOICE SET is_deleted = 1, updated_at = SYSDATE WHERE invoice_id = ?";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] softDelete: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật nộp thêm tiền.
     *
     * Bản fix chuẩn thực tế:
     * - Luôn khóa dòng INVOICE bằng SELECT FOR UPDATE trước khi tính tiền còn nợ.
     * - Nếu hóa đơn đã thanh toán đủ thì báo lỗi, không cho ghi nhận thêm.
     * - Nếu số tiền nộp thêm lớn hơn số còn nợ thì báo lỗi, không cho ghi vượt.
     * - Không tin vào finalAmount truyền từ UI; luôn đọc final_amount và amount_paid mới nhất từ DB.
     */
    public boolean updatePayment(int invoiceId, double newAmountIn, String method) throws SQLException {
        if (invoiceId <= 0) {
            throw new SQLException("Hóa đơn không hợp lệ.");
        }

        if (newAmountIn <= 0) {
            throw new SQLException("Số tiền thanh toán phải lớn hơn 0.");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new SQLException("Vui lòng chọn phương thức thanh toán.");
        }

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double oldAmountPaid;
            double finalAmount;
            String currentStatus;

            String selectSql =
                    "SELECT NVL(amount_paid, 0) AS amount_paid, " +
                    "       NVL(final_amount, 0) AS final_amount, " +
                    "       NVL(status, 'UNPAID') AS status " +
                    "FROM INVOICE " +
                    "WHERE invoice_id = ? AND is_deleted = 0 " +
                    "FOR UPDATE";

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, invoiceId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không tìm thấy hóa đơn hoặc hóa đơn đã bị xóa.");
                    }

                    oldAmountPaid = rs.getDouble("amount_paid");
                    finalAmount = rs.getDouble("final_amount");
                    currentStatus = rs.getString("status");
                }
            }

            if (finalAmount <= 0) {
                throw new SQLException("Hóa đơn không phát sinh số tiền phải thu, không cần ghi nhận thanh toán.");
            }

            double remainingAmount = finalAmount - oldAmountPaid;

            if ("PAID".equalsIgnoreCase(currentStatus) || remainingAmount <= 0.0001) {
                throw new SQLException(
                        "Hóa đơn này đã được thanh toán đủ. " +
                        "Không thể ghi nhận thêm thanh toán."
                );
            }

            if (newAmountIn - remainingAmount > 0.0001) {
                throw new SQLException(
                        "Số tiền thanh toán vượt quá số tiền còn nợ. " +
                        "Còn nợ: " + formatMoneyForMessage(remainingAmount) + "đ."
                );
            }

            double totalPaidNew = oldAmountPaid + newAmountIn;
            String newStatus = calcStatus(totalPaidNew, finalAmount);

            String updateSql =
                    "UPDATE INVOICE " +
                    "SET    amount_paid    = ?, " +
                    "       payment_method = ?, " +
                    "       status         = ?, " +
                    "       updated_at     = SYSDATE " +
                    "WHERE  invoice_id = ? AND is_deleted = 0";

            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setDouble(1, totalPaidNew);
                ps.setString(2, method.trim());
                ps.setString(3, newStatus);
                ps.setInt(4, invoiceId);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    throw new SQLException("Không thể cập nhật hóa đơn. Vui lòng tải lại dữ liệu.");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            System.err.println("[InvoiceRepo] updatePayment: " + e.getMessage());
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Hàm cũ dùng cho chức năng hóa đơn điện tử/demo API.
     */
    public boolean updateApiStatus(int invoiceId, String apiStatus) {
        String sql =
                "UPDATE INVOICE " +
                "SET api_status = ?, updated_at = SYSDATE " +
                "WHERE invoice_id = ? AND is_deleted = 0";

        try (
                Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)
        ) {
            ps.setString(1, apiStatus);
            ps.setInt(2, invoiceId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[InvoiceRepo] updateApiStatus: " + e.getMessage());
            return false;
        }
    }

    // =====================================================
    // MODULE GỬI HÓA ĐƠN QUA GMAIL
    // =====================================================

    public InvoiceEmailDTO getInvoiceEmailData(int invoiceId) throws SQLException {
        InvoiceEmailDTO invoice = null;

        String invoiceSql =
                "SELECT i.invoice_id, " +
                "       s.full_name AS student_name, " +
                "       s.parent_name, " +
                "       s.parent_phone, " +
                "       i.total_amount, " +
                "       i.discount_amt, " +
                "       i.final_amount, " +
                "       i.amount_paid, " +
                "       i.payment_method, " +
                "       i.status, " +
                "       i.api_status " +
                "FROM INVOICE i " +
                "JOIN STUDENT s ON i.student_id = s.student_id " +
                "WHERE i.invoice_id = ? " +
                "AND NVL(i.is_deleted, 0) = 0 " +
                "AND NVL(s.is_deleted, 0) = 0";

        String detailSql =
                "SELECT sc.class_name, " +
                "       sub.subject_name, " +
                "       id.amount " +
                "FROM INVOICE_DETAILS id " +
                "JOIN STUDY_CLASS sc ON id.class_id = sc.class_id " +
                "JOIN SUBJECT sub ON sc.subject_id = sub.subject_id " +
                "WHERE id.invoice_id = ? " +
                "AND NVL(id.is_deleted, 0) = 0 " +
                "AND NVL(sc.is_deleted, 0) = 0 " +
                "AND NVL(sub.is_deleted, 0) = 0 " +
                "ORDER BY sc.class_name";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(invoiceSql)
        ) {
            ps.setInt(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    invoice = new InvoiceEmailDTO();

                    invoice.invoiceId = rs.getInt("invoice_id");
                    invoice.studentName = rs.getNString("student_name");
                    invoice.parentName = rs.getNString("parent_name");
                    invoice.parentPhone = rs.getString("parent_phone");
                    invoice.totalAmount = rs.getDouble("total_amount");
                    invoice.discountAmount = rs.getDouble("discount_amt");
                    invoice.finalAmount = rs.getDouble("final_amount");
                    invoice.paymentMethod = rs.getString("payment_method");
                    invoice.status = rs.getString("status");
                    invoice.apiStatus = rs.getString("api_status");
                }
            }

            if (invoice == null) {
                throw new SQLException("Không tìm thấy hóa đơn có mã: " + invoiceId);
            }

            try (PreparedStatement psDetail = con.prepareStatement(detailSql)) {
                psDetail.setInt(1, invoiceId);

                try (ResultSet rsDetail = psDetail.executeQuery()) {
                    while (rsDetail.next()) {
                        invoice.items.add(new InvoiceEmailItemDTO(
                                rsDetail.getNString("class_name"),
                                rsDetail.getNString("subject_name"),
                                rsDetail.getDouble("amount")
                        ));
                    }
                }
            }
        }

        return invoice;
    }

    public void updateInvoiceApiStatus(int invoiceId, String apiStatus) throws SQLException {
        String sql =
                "UPDATE INVOICE " +
                "SET api_status = ?, " +
                "    updated_at = SYSDATE " +
                "WHERE invoice_id = ? " +
                "AND NVL(is_deleted, 0) = 0";

        try (
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, apiStatus);
            ps.setInt(2, invoiceId);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Không thể cập nhật trạng thái gửi hóa đơn.");
            }
        }
    }

    private String formatMoneyForMessage(double amount) {
        return String.format("%,.0f", Math.max(0, amount));
    }

    private String calcStatus(double paid, double finalAmt) {
        if (paid >= finalAmt && finalAmt > 0) {
            return "PAID";
        }

        if (paid > 0) {
            return "PARTIAL";
        }

        return "UNPAID";
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();

        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setStudentId(rs.getInt("student_id"));
        inv.setStudentName(rs.getNString("full_name"));
        inv.setStaffId(rs.getInt("staff_id"));
        inv.setPromoId(rs.getInt("promo_id"));
        inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setDiscountAmt(rs.getDouble("discount_amt"));
        inv.setFinalAmount(rs.getDouble("final_amount"));
        inv.setAmountPaid(rs.getDouble("amount_paid"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setStatus(rs.getString("status"));
        inv.setApiStatus(rs.getString("api_status"));
        inv.setCreatedAt(rs.getDate("created_at"));

        return inv;
    }
}