package com.mycompany.myapp.service;

import com.mycompany.myapp.model.Invoice;
import com.mycompany.myapp.repository.InvoiceRepository;
import java.util.List;

public class InvoiceService {
    private final InvoiceRepository repo = new InvoiceRepository();

    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }

    public List<Invoice> searchInvoices(String keyword, String status) {
        return repo.findByFilter(keyword, status);
    }

    public Invoice getById(int id) {
        return repo.findById(id);
    }

    /** Ghi nhận thanh toán — validate rồi mới lưu */
    public String recordPayment(int invoiceId, double amountPaid,
                                double finalAmount, String method) {
        if (amountPaid <= 0)
            return "Số tiền phải lớn hơn 0.";
        if (amountPaid > finalAmount)
            return "Số tiền vượt quá học phí cần đóng ("
                   + String.format("%,.0f", finalAmount) + "đ).";
        if (method == null || method.isEmpty())
            return "Vui lòng chọn phương thức thanh toán.";
        return repo.updatePayment(invoiceId, amountPaid, method)
               ? "SUCCESS" : "Lỗi cập nhật DB. Vui lòng thử lại.";
    }

    /** Phát hành hóa đơn điện tử */
    public String issueElectronicInvoice(int invoiceId) {
        // TODO: gọi REST API hóa đơn điện tử thực tế ở đây
        return repo.updateApiStatus(invoiceId, "SENT")
               ? "SUCCESS" : "Lỗi phát hành hóa đơn điện tử.";
    }

    /** Điều chỉnh hóa đơn đã phát hành */
    public String adjustInvoice(int invoiceId, String reason) {
        if (reason == null || reason.trim().isEmpty())
            return "Vui lòng nhập lý do điều chỉnh.";
        return repo.updateApiStatus(invoiceId, "ADJUSTED")
               ? "SUCCESS" : "Lỗi điều chỉnh hóa đơn.";
    }
}