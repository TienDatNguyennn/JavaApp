package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Payroll;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực hiện tất cả truy vấn DB liên quan đến bảng PAYROLL
 */
public class PayrollRepository {

    // ── Lấy danh sách lương theo kỳ (và lọc loại nhân viên nếu cần) ─
    public List<Payroll> findByPeriod(String payPeriod, String staffType) {
        List<Payroll> list = new ArrayList<>();
        String sql =
            "SELECT p.payroll_id, p.user_id, u.full_name, " +
            "       p.staff_type, p.pay_period, " +
            "       p.total_teaching_fee, p.basic_salary, " +
            "       p.bonus_amount, p.total_net " +
            "FROM   PAYROLL p " +
            "JOIN   USERS   u ON p.user_id = u.user_id " +
            "WHERE  p.pay_period  = ? " +
            "  AND  p.is_deleted  = 0 " +
            "  AND  (? IS NULL OR p.staff_type = ?) " +
            "ORDER BY p.staff_type, u.full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payPeriod);
            if (staffType == null || staffType.isEmpty() || "Tất cả".equals(staffType)) {
                ps.setNull  (2, Types.VARCHAR);
                ps.setNull  (3, Types.VARCHAR);
            } else {
                ps.setString(2, "X");
                ps.setString(3, staffType);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] findByPeriod lỗi: " + e.getMessage());
        }
        return list;
    }

    // ── Chèn mới 1 bản ghi lương ────────────────────────────────────
    public boolean save(Payroll p) {
        String sql =
            "INSERT INTO PAYROLL " +
            "  (user_id, staff_type, pay_period, total_teaching_fee, " +
            "   basic_salary, bonus_amount, total_net) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, p.getUserId());
            ps.setString(2, p.getStaffType());
            ps.setString(3, p.getPayPeriod());
            ps.setDouble(4, p.getTotalTeachingFee());
            ps.setDouble(5, p.getBasicSalary());
            ps.setDouble(6, p.getBonusAmount());
            ps.setDouble(7, p.getTotalNet());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] save lỗi: " + e.getMessage());
            return false;
        }
    }

    // ── Cập nhật bản ghi lương đã có ────────────────────────────────
    public boolean update(Payroll p) {
        String sql =
            "UPDATE PAYROLL " +
            "SET total_teaching_fee = ?, basic_salary = ?, " +
            "    bonus_amount = ?, total_net = ? " +
            "WHERE payroll_id = ? AND is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, p.getTotalTeachingFee());
            ps.setDouble(2, p.getBasicSalary());
            ps.setDouble(3, p.getBonusAmount());
            ps.setDouble(4, p.getTotalNet());
            ps.setInt   (5, p.getPayrollId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PayrollRepository] update lỗi: " + e.getMessage());
            return false;
        }
    }

    // ── Ánh xạ ResultSet → Payroll ──────────────────────────────────
    private Payroll mapRow(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();
        p.setPayrollId       (rs.getInt   ("payroll_id"));
        p.setUserId          (rs.getInt   ("user_id"));
        p.setFullName        (rs.getNString("full_name"));
        p.setStaffType       (rs.getString("staff_type"));
        p.setPayPeriod       (rs.getString("pay_period"));
        p.setTotalTeachingFee(rs.getDouble("total_teaching_fee"));
        p.setBasicSalary     (rs.getDouble("basic_salary"));
        p.setBonusAmount     (rs.getDouble("bonus_amount"));
        p.setTotalNet        (rs.getDouble("total_net"));
        return p;
    }
}