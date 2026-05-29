package com.mycompany.myapp.repository;

import com.mycompany.myapp.config.DBConnection;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.model.StaffOptionDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollRepository {

    // ── 1. SELECT BẢNG LƯƠNG THEO KỲ ──────────────────────────────
    public List<Payroll> findByPeriod(String payPeriod, String staffType) {
        List<Payroll> list = new ArrayList<>();

        String sql =
                "SELECT p.payroll_id, p.user_id, u.full_name, " +
                "       p.staff_type, p.pay_period, " +
                "       p.total_teaching_fee, p.basic_salary, " +
                "       p.bonus_amount, p.total_net " +
                "FROM   PAYROLL p " +
                "JOIN   USERS u ON p.user_id = u.user_id " +
                "WHERE  p.pay_period = ? " +
                "  AND  NVL(p.is_deleted, 0) = 0 " +
                "  AND  NVL(u.is_deleted, 0) = 0 " +
                "  AND  (? IS NULL OR p.staff_type = ?) " +
                "ORDER BY p.staff_type, u.full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, payPeriod);

            if (isAllStaffType(staffType)) {
                ps.setNull(2, Types.VARCHAR);
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(2, "FILTERED");
                ps.setString(3, staffType.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] findByPeriod error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // ── 2. LẤY DANH SÁCH GIÁO VIÊN / NHÂN VIÊN CHO COMBOBOX ───────
    public List<StaffOptionDTO> getStaffOptions() {
        List<StaffOptionDTO> list = new ArrayList<>();

        String sql =
                "SELECT u.user_id, u.full_name, 'TEACHER' AS staff_type, 0 AS base_salary " +
                "FROM USERS u " +
                "JOIN TEACHER_PROFILE tp ON u.user_id = tp.teacher_id " +
                "WHERE NVL(u.is_deleted, 0) = 0 " +
                "AND NVL(tp.is_deleted, 0) = 0 " +

                "UNION ALL " +

                "SELECT u.user_id, u.full_name, 'OFFICE' AS staff_type, NVL(osp.base_salary, 0) AS base_salary " +
                "FROM USERS u " +
                "JOIN OFFICE_STAFF_PROFILE osp ON u.user_id = osp.staff_id " +
                "WHERE NVL(u.is_deleted, 0) = 0 " +
                "AND NVL(osp.is_deleted, 0) = 0 " +

                "ORDER BY staff_type, full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new StaffOptionDTO(
                        rs.getInt("user_id"),
                        rs.getNString("full_name"),
                        rs.getString("staff_type"),
                        rs.getDouble("base_salary")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] getStaffOptions error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // ── 3. KIỂM TRA NHÂN SỰ ĐÃ CÓ BẢNG LƯƠNG TRONG KỲ CHƯA ────────
    public Payroll findExistRecord(int userId, String payPeriod) {
        String sql =
                "SELECT payroll_id " +
                "FROM PAYROLL " +
                "WHERE user_id = ? " +
                "AND pay_period = ? " +
                "AND NVL(is_deleted, 0) = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, payPeriod);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payroll p = new Payroll();
                    p.setPayrollId(rs.getInt("payroll_id"));
                    return p;
                }
            }

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] findExistRecord error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ── 4. SAVE UPSERT: CÓ RỒI THÌ UPDATE, CHƯA CÓ THÌ INSERT ─────
    public boolean save(Payroll p) {
        Payroll existPayroll = findExistRecord(p.getUserId(), p.getPayPeriod());

        if (existPayroll != null) {
            p.setPayrollId(existPayroll.getPayrollId());
            System.out.println(
                    "[PayrollRepo] Đã tồn tại bảng lương. Chuyển sang UPDATE. payroll_id="
                            + p.getPayrollId()
            );
            return update(p);
        }

        String sql =
                "INSERT INTO PAYROLL " +
                "   (user_id, staff_type, pay_period, total_teaching_fee, " +
                "    basic_salary, bonus_amount, total_net, created_at, updated_at, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getUserId());
            ps.setString(2, p.getStaffType());
            ps.setString(3, p.getPayPeriod());
            ps.setDouble(4, p.getTotalTeachingFee());
            ps.setDouble(5, p.getBasicSalary());
            ps.setDouble(6, p.getBonusAmount());
            ps.setDouble(7, p.getTotalNet());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] save error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── 5. UPDATE BẢNG LƯƠNG ──────────────────────────────────────
    public boolean update(Payroll p) {
        String sql =
                "UPDATE PAYROLL " +
                "SET staff_type = ?, " +
                "    total_teaching_fee = ?, " +
                "    basic_salary = ?, " +
                "    bonus_amount = ?, " +
                "    total_net = ?, " +
                "    updated_at = SYSDATE " +
                "WHERE payroll_id = ? " +
                "AND NVL(is_deleted, 0) = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getStaffType());
            ps.setDouble(2, p.getTotalTeachingFee());
            ps.setDouble(3, p.getBasicSalary());
            ps.setDouble(4, p.getBonusAmount());
            ps.setDouble(5, p.getTotalNet());
            ps.setInt(6, p.getPayrollId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] update error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── 6. XÓA MỀM THEO USER + KỲ LƯƠNG ───────────────────────────
    public boolean softDelete(int userId, String period) {
        String sql =
                "UPDATE PAYROLL " +
                "SET is_deleted = 1, updated_at = SYSDATE " +
                "WHERE user_id = ? " +
                "AND pay_period = ? " +
                "AND NVL(is_deleted, 0) = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, period);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] softDelete error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── 7. XÓA MỀM THEO PAYROLL ID ────────────────────────────────
    public boolean deleteById(int payrollId) {
        String sql =
                "UPDATE PAYROLL " +
                "SET is_deleted = 1, updated_at = SYSDATE " +
                "WHERE payroll_id = ? " +
                "AND NVL(is_deleted, 0) = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payrollId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] deleteById error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── 8. XÓA MỀM TOÀN BỘ KỲ LƯƠNG ───────────────────────────────
    public boolean deleteByPeriod(String payPeriod, String staffType) {
        StringBuilder sql = new StringBuilder(
                "UPDATE PAYROLL " +
                "SET is_deleted = 1, updated_at = SYSDATE " +
                "WHERE pay_period = ? " +
                "AND NVL(is_deleted, 0) = 0"
        );

        boolean hasStaffType = !isAllStaffType(staffType);

        if (hasStaffType) {
            sql.append(" AND staff_type = ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, payPeriod);

            if (hasStaffType) {
                ps.setString(2, staffType.trim());
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[PayrollRepo] deleteByPeriod error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── 9. LẤY DANH SÁCH NHÂN SỰ CHƯA CÓ LƯƠNG TRONG KỲ ───────────
    public List<Payroll> findWithoutPayroll(String payPeriod, String staffType) {
        List<Payroll> list = new ArrayList<>();

        boolean filterTeacher = isAllStaffType(staffType) || "TEACHER".equalsIgnoreCase(staffType.trim());
        boolean filterOffice = isAllStaffType(staffType) || "OFFICE".equalsIgnoreCase(staffType.trim());

        if (filterTeacher) {
            String sql =
                    "SELECT u.user_id, u.full_name, 'TEACHER' AS staff_type " +
                    "FROM USERS u " +
                    "JOIN TEACHER_PROFILE tp " +
                    "     ON u.user_id = tp.teacher_id " +
                    "WHERE NVL(u.is_deleted, 0) = 0 " +
                    "  AND NVL(tp.is_deleted, 0) = 0 " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 " +
                    "      FROM PAYROLL p " +
                    "      WHERE p.user_id = u.user_id " +
                    "        AND p.pay_period = ? " +
                    "        AND NVL(p.is_deleted, 0) = 0 " +
                    "  ) " +
                    "ORDER BY u.full_name";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, payPeriod);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMissing(rs));
                    }
                }

            } catch (SQLException e) {
                System.err.println("[PayrollRepo] findWithoutPayroll(TEACHER) error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (filterOffice) {
            String sql =
                    "SELECT u.user_id, u.full_name, 'OFFICE' AS staff_type " +
                    "FROM USERS u " +
                    "JOIN OFFICE_STAFF_PROFILE osp " +
                    "     ON u.user_id = osp.staff_id " +
                    "WHERE NVL(u.is_deleted, 0) = 0 " +
                    "  AND NVL(osp.is_deleted, 0) = 0 " +
                    "  AND NOT EXISTS ( " +
                    "      SELECT 1 " +
                    "      FROM PAYROLL p " +
                    "      WHERE p.user_id = u.user_id " +
                    "        AND p.pay_period = ? " +
                    "        AND NVL(p.is_deleted, 0) = 0 " +
                    "  ) " +
                    "ORDER BY u.full_name";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, payPeriod);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapMissing(rs));
                    }
                }

            } catch (SQLException e) {
                System.err.println("[PayrollRepo] findWithoutPayroll(OFFICE) error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return list;
    }

    // ── 10. HELPER KIỂM TRA LOẠI NHÂN SỰ ──────────────────────────
    private boolean isAllStaffType(String staffType) {
        return staffType == null
                || staffType.trim().isEmpty()
                || staffType.equalsIgnoreCase("Tất cả")
                || staffType.equalsIgnoreCase("Tat ca")
                || staffType.equalsIgnoreCase("ALL");
    }

    // ── 11. MAP DỮ LIỆU BẢNG LƯƠNG ────────────────────────────────
    private Payroll mapRow(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();

        p.setPayrollId(rs.getInt("payroll_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setFullName(rs.getNString("full_name"));
        p.setStaffType(rs.getString("staff_type"));
        p.setPayPeriod(rs.getString("pay_period"));
        p.setTotalTeachingFee(rs.getDouble("total_teaching_fee"));
        p.setBasicSalary(rs.getDouble("basic_salary"));
        p.setBonusAmount(rs.getDouble("bonus_amount"));
        p.setTotalNet(rs.getDouble("total_net"));

        return p;
    }

    // ── 12. MAP NHÂN SỰ CHƯA CÓ LƯƠNG ─────────────────────────────
    private Payroll mapMissing(ResultSet rs) throws SQLException {
        Payroll p = new Payroll();

        p.setUserId(rs.getInt("user_id"));
        p.setFullName(rs.getNString("full_name"));
        p.setStaffType(rs.getString("staff_type"));

        return p;
    }
}