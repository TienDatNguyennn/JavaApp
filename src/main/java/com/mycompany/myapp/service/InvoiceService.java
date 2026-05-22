package com.mycompany.myapp.service;

import com.mycompany.myapp.config.DBConnection;
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
        if (inv.getTotalAmount() < 0)
            return "Hoc phi khong duoc am.";
        if (inv.getDiscountAmt() < 0)
            return "Giam gia khong duoc am.";
        if (inv.getDiscountAmt() > inv.getTotalAmount())
            return "Giam gia khong the lon hon hoc phi.";

        // Tinh finalAmount TRUOC khi validate amountPaid
        double finalAmt = inv.getTotalAmount() - inv.getDiscountAmt();
        inv.setFinalAmount(finalAmt);

        if (inv.getAmountPaid() < 0)
            return "So tien da nop khong duoc am.";
        if (inv.getAmountPaid() > finalAmt)
            return "So tien da nop vuot qua hoc phi thuc thu (" + String.format("%,.0f", finalAmt) + "d).";

        try {
            boolean ok = repo.insert(inv);
            if (ok) {
                DBConnection.commitTransaction();
                return "SUCCESS";
            } else {
                DBConnection.rollbackTransaction();
                return "Loi them hoa don. Vui long thu lai.";
            }
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            System.err.println("[InvoiceService] addInvoice: " + e.getMessage());
            return "Loi them hoa don: " + e.getMessage();
        }
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

        // Tinh finalAmount TRUOC khi validate amountPaid
        double finalAmt = inv.getTotalAmount() - inv.getDiscountAmt();
        inv.setFinalAmount(finalAmt);

        if (inv.getAmountPaid() < 0)
            return "So tien da nop khong duoc am.";
        if (inv.getAmountPaid() > finalAmt)
            return "So tien da nop vuot qua hoc phi thuc thu (" + String.format("%,.0f", finalAmt) + "d).";

        try {
            boolean ok = repo.update(inv);
            if (ok) { DBConnection.commitTransaction(); return "SUCCESS"; }
            else    { DBConnection.rollbackTransaction(); return "Loi cap nhat hoa don. Vui long thu lai."; }
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            return "Loi cap nhat hoa don: " + e.getMessage();
        }
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

        try {
            boolean ok = repo.softDelete(invoiceId);
            if (ok) { DBConnection.commitTransaction(); return "SUCCESS"; }
            else    { DBConnection.rollbackTransaction(); return "Loi xoa hoa don. Vui long thu lai."; }
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            return "Loi xoa hoa don: " + e.getMessage();
        }
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
        try {
            boolean ok = repo.updatePayment(invoiceId, amountPaid, method);
            if (ok) { DBConnection.commitTransaction(); return "SUCCESS"; }
            else    { DBConnection.rollbackTransaction(); return "Loi cap nhat DB. Vui long thu lai."; }
        } catch (Exception e) {
            DBConnection.rollbackTransaction();
            return "Loi cap nhat thanh toan: " + e.getMessage();
        }
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