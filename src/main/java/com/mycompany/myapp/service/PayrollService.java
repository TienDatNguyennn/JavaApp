package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.repository.PayrollRepository;
import java.util.List;

public class PayrollService {
    private final PayrollRepository repo = new PayrollRepository();

    public List<Payroll> getPayrollByPeriod(String period, String staffType) {
        return repo.findByPeriod(period, staffType);
    }

    public double calcTotal(List<Payroll> list) {
        return list.stream().mapToDouble(Payroll::getTotalNet).sum();
    }

    public String savePayroll(Payroll p) {
        if (p.getBasicSalary() < 0)
            return "Lương cơ bản không hợp lệ.";
        p.recalcTotalNet();
        return repo.save(p) ? "SUCCESS" : "Lỗi lưu bảng lương.";
    }

    public String updatePayroll(Payroll p) {
        p.recalcTotalNet();
        return repo.update(p) ? "SUCCESS" : "Lỗi cập nhật bảng lương.";
    }
}