package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Payroll;
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

    public double calcTotal(List<Payroll> list) {
        if (list == null) return 0;
        return list.stream().mapToDouble(Payroll::getTotalNet).sum();
    }

    public List<Payroll> getWithoutPayroll(String period, String staffType) {
        return repo.findWithoutPayroll(period, staffType);
    }

    // ── INSERT ────────────────────────────────────────────────────
    public String savePayroll(Payroll p) {
        if (p == null) return "Dữ liệu trống.";
        if (p.getBasicSalary() < 0) return "Lương cơ bản không hợp lệ.";
        
        // Tính toán lại tổng trước khi lưu (tương ứng logic Java)
        p.recalcTotalNet(); 
        
        return repo.save(p) ? "SUCCESS" : "Lỗi lưu bảng lương.";
    }

    // ── UPDATE ────────────────────────────────────────────────────
    public String updatePayroll(Payroll p) {
        if (p == null || p.getPayrollId() <= 0) return "Dữ liệu không hợp lệ.";
        
        p.recalcTotalNet();
        
        return repo.update(p) ? "SUCCESS" : "Lỗi cập nhật bảng lương.";
    }

    // ── DELETE (SOFT DELETE) THEO USER VÀ KỲ ──────────────────────
    /**
     * Hàm này được gọi từ FinanceController.deletePayroll(String userId, String period)
     * Giải quyết lỗi Trigger bằng cách gọi hàm UPDATE trong Repository.
     */
    public String deleteByUserAndPeriod(int userId, String period) {
        if (userId <= 0) return "Mã nhân viên không hợp lệ.";
        if (period == null || period.trim().isEmpty()) return "Kỳ lương không hợp lệ.";

        // Gọi hàm softDelete trong repo (Sử dụng UPDATE thay vì DELETE)
        boolean ok = repo.softDelete(userId, period);
        
        return ok ? "SUCCESS" : "Không tìm thấy bản ghi để xóa hoặc bản ghi đã bị xóa trước đó.";
    }

    // ── DELETE (SOFT DELETE) THEO ID ──────────────────────────────
    public String deleteById(int payrollId) {
        if (payrollId <= 0) return "Mã bản lương không hợp lệ.";
        
        // repo.deleteById phải dùng lệnh UPDATE PAYROLL SET is_deleted = 1...
        return repo.deleteById(payrollId) ? "SUCCESS" : "Lỗi xóa bản lương (Bản ghi không tồn tại).";
    }

    // ── DELETE (SOFT DELETE) TOÀN BỘ KỲ ───────────────────────────
    public String deleteByPeriod(String period, String staffType) {
        if (period == null || period.trim().isEmpty())
            return "Kỳ lương không hợp lệ.";
            
        // repo.deleteByPeriod phải dùng lệnh UPDATE
        boolean ok = repo.deleteByPeriod(period, staffType);
        
        return ok ? "SUCCESS" : "Không có bản ghi nào được cập nhật trạng thái xóa.";
    }
}