package com.mycompany.myapp.service;

import com.mycompany.myapp.model.InvoiceEmailDTO;
import com.mycompany.myapp.model.InvoiceEmailItemDTO;
import com.mycompany.myapp.repository.InvoiceRepository;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class InvoiceEmailService {

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final EmailService emailService = new EmailService();

    public void sendInvoiceToEmail(int invoiceId, String toEmail) throws Exception {
        InvoiceEmailDTO invoice = invoiceRepo.getInvoiceEmailData(invoiceId);

        String subject = "ALPHA LOGIC CENTER - Hóa đơn học phí #" + invoice.invoiceId;
        String htmlContent = buildInvoiceHtml(invoice);

        try {
            invoiceRepo.updateInvoiceApiStatus(invoiceId, "SENDING");

            emailService.sendHtmlEmail(toEmail, subject, htmlContent);

            invoiceRepo.updateInvoiceApiStatus(invoiceId, "SENT");

        } catch (Exception e) {
            try {
                invoiceRepo.updateInvoiceApiStatus(invoiceId, "FAILED");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            throw e;
        }
    }

    private String buildInvoiceHtml(InvoiceEmailDTO invoice) {
        DecimalFormat moneyFormat = new DecimalFormat("#,###");

        StringBuilder itemRows = new StringBuilder();

        for (InvoiceEmailItemDTO item : invoice.items) {
            itemRows.append("<tr>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>")
                    .append(escapeHtml(item.className))
                    .append("</td>")
                    .append("<td style='padding:8px;border:1px solid #ddd;'>")
                    .append(escapeHtml(item.subjectName))
                    .append("</td>")
                    .append("<td style='padding:8px;border:1px solid #ddd;text-align:right;'>")
                    .append(moneyFormat.format(item.amount))
                    .append(" VNĐ</td>")
                    .append("</tr>");
        }

        return ""
                + "<div style='font-family:Arial,sans-serif;background:#f6f7fb;padding:24px;'>"
                + "  <div style='max-width:720px;margin:auto;background:white;border-radius:12px;padding:24px;border:1px solid #e5e7eb;'>"
                + "    <h2 style='color:#6c3df4;margin-bottom:4px;'>ALPHA LOGIC CENTER</h2>"
                + "    <p style='color:#666;margin-top:0;'>Hệ thống quản lý đào tạo</p>"
                + "    <hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>"

                + "    <h3 style='color:#222;'>THÔNG BÁO HÓA ĐƠN HỌC PHÍ</h3>"
                + "    <p>Kính gửi phụ huynh/học viên: <b>" + escapeHtml(invoice.studentName) + "</b></p>"
                + "    <p>Trung tâm gửi thông tin hóa đơn học phí như sau:</p>"

                + "    <table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Mã hóa đơn</td><td style='padding:8px;border:1px solid #ddd;'><b>#" + invoice.invoiceId + "</b></td></tr>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Học viên</td><td style='padding:8px;border:1px solid #ddd;'>" + escapeHtml(invoice.studentName) + "</td></tr>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Phụ huynh</td><td style='padding:8px;border:1px solid #ddd;'>" + escapeHtml(invoice.parentName) + "</td></tr>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>SĐT phụ huynh</td><td style='padding:8px;border:1px solid #ddd;'>" + escapeHtml(invoice.parentPhone) + "</td></tr>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Trạng thái thanh toán</td><td style='padding:8px;border:1px solid #ddd;'><b>" + escapeHtml(invoice.status) + "</b></td></tr>"
                + "    </table>"

                + "    <h4>Chi tiết lớp học</h4>"
                + "    <table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
                + "      <tr style='background:#6c3df4;color:white;'>"
                + "        <th style='padding:8px;border:1px solid #ddd;text-align:left;'>Lớp học</th>"
                + "        <th style='padding:8px;border:1px solid #ddd;text-align:left;'>Môn học</th>"
                + "        <th style='padding:8px;border:1px solid #ddd;text-align:right;'>Học phí</th>"
                + "      </tr>"
                + itemRows
                + "    </table>"

                + "    <table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Tổng tiền</td><td style='padding:8px;border:1px solid #ddd;text-align:right;'>" + moneyFormat.format(invoice.totalAmount) + " VNĐ</td></tr>"
                + "      <tr><td style='padding:8px;border:1px solid #ddd;'>Giảm giá</td><td style='padding:8px;border:1px solid #ddd;text-align:right;'>" + moneyFormat.format(invoice.discountAmount) + " VNĐ</td></tr>"
                + "      <tr style='background:#f2efff;'><td style='padding:8px;border:1px solid #ddd;'><b>Số tiền cần thanh toán</b></td><td style='padding:8px;border:1px solid #ddd;text-align:right;'><b>" + moneyFormat.format(invoice.finalAmount) + " VNĐ</b></td></tr>"
                + "    </table>"

                + "    <p>Vui lòng kiểm tra thông tin và liên hệ trung tâm nếu cần hỗ trợ.</p>"
                + "    <p style='margin-top:24px;'>Trân trọng,<br><b>ALPHA LOGIC CENTER</b></p>"
                + "  </div>"
                + "</div>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}