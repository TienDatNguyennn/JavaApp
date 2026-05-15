package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.repository.InvoiceRepository;
import java.util.List;

public class InvoiceService {

    private final InvoiceRepository repo = new InvoiceRepository();

    // ── READ ───────────────────────────────────────────────────────
    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return repo.findByFilter(keyword, status);
    }

    public Invoice getById(int id) {
        return repo.findById(id);
    }

    // ── INSERT ─────────────────────────────────────────────────────
    public String addInvoice(Invoice inv) {
        if (inv.getStudentId() <= 0)
            return "Ma hoc vien khong hop le.";
        if (inv.getTotalAmount() <= 0)
            return "Hoc phi phai lon hon 0.";
        if (inv.getDiscountAmt() < 0)
            return "Giam gia khong duoc am.";
        if (inv.getDiscountAmt() > inv.getTotalAmount())
            return "Giam gia khong the lon hon hoc phi.";
        if (inv.getAmountPaid() < 0)
            return "So tien da nop khong duoc am.";
        if (inv.getAmountPaid() > inv.getFinalAmount())
            return "So tien da nop vuot qua hoc phi thuc thu.";

        // Tu tinh final_amount
        inv.setFinalAmount(inv.getTotalAmount() - inv.getDiscountAmt());

        return repo.insert(inv) ? "SUCCESS" : "Loi them hoa don. Vui long thu lai.";
    }

    // ── UPDATE ─────────────────────────────────────────────────────
    public String updateInvoice(Invoice inv) {
        if (inv.getInvoiceId() <= 0)
            return "Hoa don khong hop le.";
        if (inv.getTotalAmount() <= 0)
            return "Hoc phi phai lon hon 0.";
        if (inv.getDiscountAmt() < 0)
            return "Giam gia khong duoc am.";
        if (inv.getDiscountAmt() > inv.getTotalAmount())
            return "Giam gia khong the lon hon hoc phi.";
        if (inv.getAmountPaid() < 0)
            return "So tien da nop khong duoc am.";

        // Tu tinh lai final_amount
        inv.setFinalAmount(inv.getTotalAmount() - inv.getDiscountAmt());

        if (inv.getAmountPaid() > inv.getFinalAmount())
            return "So tien da nop vuot qua hoc phi thuc thu ("
                   + String.format("%,.0f", inv.getFinalAmount()) + "d).";

        return repo.update(inv) ? "SUCCESS" : "Loi cap nhat hoa don. Vui long thu lai.";
    }

    // ── DELETE ─────────────────────────────────────────────────────
    public String deleteInvoice(int invoiceId) {
        Invoice inv = repo.findById(invoiceId);
        if (inv == null)
            return "Khong tim thay hoa don.";
        if ("PAID".equals(inv.getStatus()))
            return "Khong the xoa hoa don da thanh toan day du.";
        if ("SENT".equals(inv.getApiStatus()) || "ADJUSTED".equals(inv.getApiStatus()))
            return "Khong the xoa hoa don da phat hanh len he thong dien tu.";

        return repo.softDelete(invoiceId) ? "SUCCESS" : "Loi xoa hoa don. Vui long thu lai.";
    }

    // ── PAYMENT ────────────────────────────────────────────────────
    public String recordPayment(int invoiceId, double amountPaid,
                                double finalAmount, String method) {
        if (amountPaid <= 0)
            return "So tien phai lon hon 0.";
        if (amountPaid > finalAmount)
            return "So tien vuot qua hoc phi can dong ("
                   + String.format("%,.0f", finalAmount) + "d).";
        if (method == null || method.isEmpty())
            return "Vui long chon phuong thuc thanh toan.";
        return repo.updatePayment(invoiceId, amountPaid, method)
               ? "SUCCESS" : "Loi cap nhat DB. Vui long thu lai.";
    }

    // ── INVOICE ELECTRONIC ────────────────────────────────────────
    public String issueElectronicInvoice(int invoiceId) {
        return repo.updateApiStatus(invoiceId, "SENT")
               ? "SUCCESS" : "Loi phat hanh hoa don dien tu.";
    }

    public String adjustInvoice(int invoiceId, String reason) {
        if (reason == null || reason.trim().isEmpty())
            return "Vui long nhap ly do dieu chinh.";
        return repo.updateApiStatus(invoiceId, "ADJUSTED")
               ? "SUCCESS" : "Loi dieu chinh hoa don.";
    }
}