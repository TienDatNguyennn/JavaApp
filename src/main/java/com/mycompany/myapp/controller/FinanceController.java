package com.mycompany.myapp.controller;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.model.Payroll;
import com.mycompany.myapp.service.InvoiceService;
import com.mycompany.myapp.service.PayrollService;
import java.util.List;

public class FinanceController {
    private final InvoiceService invoiceSvc = new InvoiceService();
    private final PayrollService payrollSvc = new PayrollService();

    // ── HỌC PHÍ ───────────────────────────────────────────────────
    public List<Invoice> getAllInvoices() {
        return invoiceSvc.getAllInvoices();
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return invoiceSvc.searchInvoices(keyword, status);
    }

    public Invoice getInvoiceById(int id) {
        return invoiceSvc.getById(id);
    }

    public String recordPayment(int invoiceId, double paid,
                                double finalAmt, String method) {
        return invoiceSvc.recordPayment(invoiceId, paid, finalAmt, method);
    }

    // ── HÓA ĐƠN ĐIỆN TỬ ──────────────────────────────────────────
    public String issueInvoice(int invoiceId) {
        return invoiceSvc.issueElectronicInvoice(invoiceId);
    }

    public String adjustInvoice(int invoiceId, String reason) {
        return invoiceSvc.adjustInvoice(invoiceId, reason);
    }

    // ── LƯƠNG ─────────────────────────────────────────────────────
    public List<Payroll> getPayroll(String period, String staffType) {
        return payrollSvc.getPayrollByPeriod(period, staffType);
    }

    public double getTotalSalary(String period, String staffType) {
        return payrollSvc.calcTotal(getPayroll(period, staffType));
    }

    public String savePayroll(Payroll p)   { return payrollSvc.savePayroll(p); }
    public String updatePayroll(Payroll p) { return payrollSvc.updatePayroll(p); }
}