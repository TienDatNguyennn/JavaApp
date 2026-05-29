package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.model.StaffOptionDTO;
import com.mycompany.myapp.service.InvoiceEmailService;
import com.mycompany.myapp.service.InvoiceService;
import com.mycompany.myapp.service.PayrollService;
import java.util.List;

public class FinanceController {

    private final InvoiceService invoiceSvc = new InvoiceService();
    private final PayrollService payrollSvc = new PayrollService();
    private final InvoiceEmailService invoiceEmailService = new InvoiceEmailService();

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
    public String recordPayment(int invoiceId, double paid, double finalAmt, String method) {
        return invoiceSvc.recordPayment(invoiceId, paid, finalAmt, method);
    }

    // ── HÓA ĐƠN ĐIỆN TỬ ───────────────────────────────────────────
    public String issueInvoice(int invoiceId) {
        return invoiceSvc.issueElectronicInvoice(invoiceId);
    }

    public String adjustInvoice(int invoiceId, String reason) {
        return invoiceSvc.adjustInvoice(invoiceId, reason);
    }

    public String sendInvoiceEmail(int invoiceId, String toEmail) {
        try {
            invoiceEmailService.sendInvoiceToEmail(invoiceId, toEmail);
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "Gửi hóa đơn thất bại: " + e.getMessage();
        }
    }

    // ── LƯƠNG — READ ──────────────────────────────────────────────
    public List<Payroll> getPayroll(String period, String staffType) {
        return payrollSvc.getPayrollByPeriod(period, staffType);
    }

    public List<StaffOptionDTO> getStaffOptions() {
        return payrollSvc.getStaffOptions();
    }

    public List<Payroll> getWithoutPayroll(String period, String staffType) {
        return payrollSvc.getWithoutPayroll(period, staffType);
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

    public String deletePayroll(String userId, String period) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return "Mã nhân viên không được để trống.";
            }

            int uId = Integer.parseInt(userId.trim());

            return payrollSvc.deleteByUserAndPeriod(uId, period);

        } catch (NumberFormatException e) {
            return "Lỗi: Mã nhân viên phải là định dạng số.";
        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }

    public String deletePayrollById(int payrollId) {
        return payrollSvc.deleteById(payrollId);
    }

    public String deletePayrollByPeriod(String period, String staffType) {
        return payrollSvc.deleteByPeriod(period, staffType);
    }
}