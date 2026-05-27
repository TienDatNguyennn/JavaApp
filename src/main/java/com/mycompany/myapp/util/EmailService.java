package com.mycompany.myapp.util;

import com.mycompany.myapp.model.Invoice;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties; // dùng cho SMTP config

/**
 * Gửi hóa đơn điện tử qua Gmail SMTP.
 *
 * ⚠ App Password chỉ dùng để gửi mail, KHÔNG thể đăng nhập Gmail.
 *   Revoke tại https://myaccount.google.com/apppasswords sau khi bảo vệ xong.
 */
public class EmailService {

    // ── Tài khoản gửi hóa đơn ────────────────────────────────────────
    private static final String SENDER_EMAIL    = "laq1502@gmail.com";
    private static final String SENDER_PASSWORD = "jlhrpnipxtztcmf";
    private static final String SENDER_NAME     = "Trung tâm đào tạo";

    // ── Gửi hóa đơn ──────────────────────────────────────────────────

    /**
     * Gửi hóa đơn PDF qua Gmail đến địa chỉ toEmail.
     *
     * @param toEmail       địa chỉ nhận
     * @param recipientName tên người nhận (hiển thị trong thân email)
     * @param inv           đối tượng hóa đơn
     * @param pdfBytes      nội dung PDF đã sinh sẵn
     */
    public static void sendInvoice(String toEmail,
                                   String recipientName,
                                   Invoice inv,
                                   byte[] pdfBytes) throws Exception {
        if (SENDER_EMAIL == null || SENDER_EMAIL.isBlank()) {
            throw new Exception("Chưa cấu hình tài khoản gửi email.");
        }

        // ── SMTP (port 465 / SSL) ─────────────────────────────────────
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host",              "smtp.gmail.com");
        mailProps.put("mail.smtp.port",              "465");
        mailProps.put("mail.smtp.auth",              "true");
        mailProps.put("mail.smtp.ssl.enable",        "true");
        mailProps.put("mail.smtp.ssl.trust",         "smtp.gmail.com");
        mailProps.put("mail.smtp.connectiontimeout", "15000");
        mailProps.put("mail.smtp.timeout",           "15000");

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        // ── Tạo message ───────────────────────────────────────────────
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL,
                MimeUtility.encodeText(SENDER_NAME, "UTF-8", "B")));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(MimeUtility.encodeText(
                "Hóa đơn điện tử INV-" + String.format("%03d", inv.getInvoiceId()),
                "UTF-8", "B"));
        msg.setSentDate(new Date());

        // ── Multipart: HTML body + PDF đính kèm ──────────────────────
        MimeMultipart multipart = new MimeMultipart("mixed");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(buildHtmlBody(recipientName, inv), "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        MimeBodyPart pdfPart = new MimeBodyPart();
        pdfPart.setDataHandler(new javax.activation.DataHandler(
                new ByteArrayDataSource(pdfBytes, "application/pdf")));
        pdfPart.setFileName(MimeUtility.encodeText(
                "HoaDon_INV-" + String.format("%03d", inv.getInvoiceId()) + ".pdf",
                "UTF-8", "B"));
        multipart.addBodyPart(pdfPart);

        msg.setContent(multipart);
        Transport.send(msg);
    }

    // ── Nội dung HTML email ───────────────────────────────────────────

    private static String buildHtmlBody(String name, Invoice inv) {
        NumberFormat nf  = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                inv.getCreatedAt() != null ? inv.getCreatedAt() : new Date());

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;"
             + "font-family:Arial,sans-serif;background:#f4f4f4;'>"
             + "<div style='max-width:600px;margin:30px auto;background:#fff;"
             +      "border-radius:10px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.1);'>"

             // Header tím
             + "<div style='background:#6c5ce7;padding:28px 32px;'>"
             + "<h2 style='color:#fff;margin:0;font-size:20px;'>TRUNG TÂM ĐÀO TẠO</h2>"
             + "<p style='color:rgba(255,255,255,.8);margin:6px 0 0;font-size:13px;'>"
             +    "Hóa đơn điện tử — " + date + "</p>"
             + "</div>"

             // Thân
             + "<div style='padding:32px;'>"
             + "<p style='font-size:15px;'>Xin chào <b>" + esc(name) + "</b>,</p>"
             + "<p style='color:#555;font-size:14px;line-height:1.6;'>"
             +    "Trung tâm trân trọng gửi đến bạn <b>hóa đơn điện tử</b> "
             +    "cho khoản thanh toán học phí. "
             +    "Vui lòng xem file PDF đính kèm để lưu trữ.</p>"

             // Bảng thông tin
             + "<div style='background:#f8f9fa;border-radius:8px;padding:20px;"
             +      "margin:20px 0;border-left:4px solid #6c5ce7;'>"
             + "<table style='width:100%;border-collapse:collapse;font-size:14px;'>"
             + row("Mã hóa đơn",      "INV-" + String.format("%03d", inv.getInvoiceId()))
             + row("Học viên",         esc(inv.getStudentName()))
             + row("Tổng thanh toán",
                   "<b style='color:#6c5ce7;font-size:16px;'>"
                   + nf.format(inv.getFinalAmount()) + "đ</b>")
             + row("Trạng thái",
                   "<span style='color:#19875f;font-weight:bold;'>✔ ĐÃ THANH TOÁN</span>")
             + "</table></div>"

             + "<p style='color:#555;font-size:13px;'>File hóa đơn PDF đính kèm ngay "
             +    "trong email này. Nếu cần hỗ trợ: <b>028.1234.5678</b>.</p>"
             + "</div>"

             // Footer
             + "<div style='background:#f0effe;padding:16px 32px;text-align:center;"
             +      "font-size:12px;color:#888;'>"
             + "© Trung tâm đào tạo — Email tự động, vui lòng không phản hồi."
             + "</div>"
             + "</div></body></html>";
    }

    private static String row(String label, String value) {
        return "<tr>"
             + "<td style='padding:6px 0;color:#666;width:45%;'>" + label + ":</td>"
             + "<td style='padding:6px 0;'>" + value + "</td>"
             + "</tr>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
