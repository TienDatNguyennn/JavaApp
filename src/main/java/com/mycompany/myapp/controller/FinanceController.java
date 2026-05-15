package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.service.InvoiceService;
import com.mycompany.myapp.service.PayrollService;
import java.util.List;

public class FinanceController {

    private final InvoiceService invoiceSvc = new InvoiceService();
    private final PayrollService payrollSvc = new PayrollService();

    // ── HỌC PHÍ — READ ────────────────────────────────────────────
    public List<Invoice> getAllInvoices() {
        return invoiceSvc.getAllInvoices();
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return invoiceSvc.searchInvoices(keyword, status);
    }

    public Invoice getInvoiceById(int id) {
        return invoiceSvc.getById(id);
    }

    // ── HỌC PHÍ — CRUD ────────────────────────────────────────────
    public String addInvoice(Invoice inv) {
        return invoiceSvc.addInvoice(inv);
    }

    public String updateInvoice(Invoice inv) {
        return invoiceSvc.updateInvoice(inv);
    }

    public String deleteInvoice(int invoiceId) {
        return invoiceSvc.deleteInvoice(invoiceId);
    }

    // ── PAYMENT ───────────────────────────────────────────────────
    public String recordPayment(int invoiceId, double paid,
                                double finalAmt, String method) {
        return invoiceSvc.recordPayment(invoiceId, paid, finalAmt, method);
    }

    // ── HÓA ĐƠN ĐIỆN TỬ ───────────────────────────────────────────
    public String issueInvoice(int invoiceId) {
        return invoiceSvc.issueElectronicInvoice(invoiceId);
    }

    public String adjustInvoice(int invoiceId, String reason) {
        return invoiceSvc.adjustInvoice(invoiceId, reason);
    }

    // ── LƯƠNG — READ ──────────────────────────────────────────────
    public List<Payroll> getPayroll(String period, String staffType) {
        return payrollSvc.getPayrollByPeriod(period, staffType);
    }

    public double getTotalSalary(String period, String staffType) {
        return payrollSvc.calcTotal(getPayroll(period, staffType));
    }

    // ── LƯƠNG — CRUD ──────────────────────────────────────────────
    public String savePayroll(Payroll p) {
        return payrollSvc.savePayroll(p);
    }

    public String updatePayroll(Payroll p) {
        return payrollSvc.updatePayroll(p);
    }

    /**
     * Xóa bản ghi lương (Xóa mềm) theo userId và kỳ lương.
     * Giải quyết lỗi Trigger bằng cách gọi qua Service để thực hiện lệnh UPDATE.
     * * @param userId Mã nhân viên (dạng String từ giao diện)
     * @param period Kỳ lương dạng MM/yyyy
     * @return "SUCCESS" hoặc thông báo lỗi
     */
    public String deletePayroll(String userId, String period) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return "Mã nhân viên không được để trống.";
            }
            
            // Chuyển đổi ID sang kiểu số
            int uId = Integer.parseInt(userId.trim());
            
            // GỌI QUA SERVICE (Không gọi trực tiếp Repository)
            return payrollSvc.deleteByUserAndPeriod(uId, period);
            
        } catch (NumberFormatException e) {
            return "Lỗi: Mã nhân viên phải là định dạng số.";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
}