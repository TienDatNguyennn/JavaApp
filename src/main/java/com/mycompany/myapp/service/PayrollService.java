package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.model.StaffOptionDTO;
import com.mycompany.myapp.repository.PayrollRepository;
import java.util.List;

public class PayrollService {

    private final PayrollRepository repo = new PayrollRepository();

    // ── READ ──────────────────────────────────────────────────────
    /**
     * Lấy danh sách bảng lương.
     * Repository sẽ chỉ lấy các bản ghi có is_deleted = 0.
     */
    public List<Payroll> getPayrollByPeriod(String period, String staffType) {
        return repo.findByPeriod(period, staffType);
    }

    /**
     * Lấy danh sách giáo viên / nhân viên để đổ vào ComboBox.
     */
    public List<StaffOptionDTO> getStaffOptions() {
        return repo.getStaffOptions();
    }

    public double calcTotal(List<Payroll> list) {
        if (list == null) return 0;
        return list.stream().mapToDouble(Payroll::getTotalNet).sum();
    }

    /**
     * Lấy danh sách nhân sự chưa nhập lương trong kỳ.
     */
    public List<Payroll> getWithoutPayroll(String period, String staffType) {
        return repo.findWithoutPayroll(period, staffType);
    }

    // ── INSERT / UPSERT ───────────────────────────────────────────
    public String savePayroll(Payroll p) {
        if (p == null) return "Dữ liệu trống.";

        if (p.getUserId() <= 0) {
            return "Vui lòng chọn nhân viên hợp lệ.";
        }

        if (p.getPayPeriod() == null || p.getPayPeriod().trim().isEmpty()) {
            return "Kỳ lương không hợp lệ.";
        }

        if (p.getStaffType() == null || p.getStaffType().trim().isEmpty()) {
            return "Loại nhân viên không hợp lệ.";
        }

        if (p.getBasicSalary() < 0) return "Lương cơ bản không hợp lệ.";
        if (p.getTotalTeachingFee() < 0) return "Phí giảng dạy không hợp lệ.";
        if (p.getBonusAmount() < 0) return "Thưởng không hợp lệ.";

        // Tính toán lại tổng trước khi lưu
        p.recalcTotalNet();

        return repo.save(p) ? "SUCCESS" : "Lỗi lưu bảng lương.";
    }

    // ── UPDATE ────────────────────────────────────────────────────
    public String updatePayroll(Payroll p) {
        if (p == null || p.getPayrollId() <= 0) {
            return "Dữ liệu không hợp lệ.";
        }

        if (p.getBasicSalary() < 0) return "Lương cơ bản không hợp lệ.";
        if (p.getTotalTeachingFee() < 0) return "Phí giảng dạy không hợp lệ.";
        if (p.getBonusAmount() < 0) return "Thưởng không hợp lệ.";

        p.recalcTotalNet();

        return repo.update(p) ? "SUCCESS" : "Lỗi cập nhật bảng lương.";
    }

    // ── DELETE SOFT DELETE THEO USER VÀ KỲ ────────────────────────
    /**
     * Hàm này được gọi từ FinanceController.deletePayroll(String userId, String period)
     * Dùng UPDATE is_deleted = 1 thay vì DELETE thật.
     */
    public String deleteByUserAndPeriod(int userId, String period) {
        if (userId <= 0) return "Mã nhân viên không hợp lệ.";
        if (period == null || period.trim().isEmpty()) return "Kỳ lương không hợp lệ.";

        boolean ok = repo.softDelete(userId, period.trim());

        return ok ? "SUCCESS" : "Không tìm thấy bản ghi để xóa hoặc bản ghi đã bị xóa trước đó.";
    }

    // ── DELETE SOFT DELETE THEO ID ────────────────────────────────
    public String deleteById(int payrollId) {
        if (payrollId <= 0) return "Mã bản lương không hợp lệ.";

        return repo.deleteById(payrollId)
                ? "SUCCESS"
                : "Lỗi xóa bản lương hoặc bản ghi không tồn tại.";
    }

    // ── DELETE SOFT DELETE TOÀN BỘ KỲ ─────────────────────────────
    public String deleteByPeriod(String period, String staffType) {
        if (period == null || period.trim().isEmpty()) {
            return "Kỳ lương không hợp lệ.";
        }

        boolean ok = repo.deleteByPeriod(period.trim(), staffType);

        return ok ? "SUCCESS" : "Không có bản ghi nào được cập nhật trạng thái xóa.";
    }
}