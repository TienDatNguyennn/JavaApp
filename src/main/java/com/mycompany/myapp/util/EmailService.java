package com.mycompany.myapp.util;

import com.mycompany.myapp.model.Invoice;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Gửi hóa đơn điện tử qua Gmail SMTP.
 *
 * Cấu hình lưu tại: ~/.trungtam/email.properties
 *   sender.email    = your@gmail.com
 *   sender.password = xxxx xxxx xxxx xxxx   ← App Password 16 ký tự
 *
 * Cách lấy App Password:
 *   1. Bật 2-Step Verification tại https://myaccount.google.com/security
 *   2. Tìm "App passwords" → chọn "Mail" / "Other" → Generate
 *   3. Copy 16 ký tự vào ô mật khẩu trong cài đặt
 */
public class EmailService {

    private static final File CONFIG_FILE = new File(
            System.getProperty("user.home") + File.separator + ".trungtam"
            + File.separator + "email.properties"
    );

    // ── Config helpers ────────────────────────────────────────────────

    public static Properties loadConfig() throws IOException {
        Properties p = new Properties();
        if (CONFIG_FILE.exists()) {
            try (InputStream is = new FileInputStream(CONFIG_FILE)) {
                p.load(is);
            }
        }
        return p;
    }

    public static void saveConfig(String senderEmail, String appPassword) throws IOException {
        CONFIG_FILE.getParentFile().mkdirs();
        Properties p = new Properties();
        p.setProperty("sender.email",    senderEmail.trim());
        p.setProperty("sender.password", appPassword.trim());
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            p.store(os, "Cau hinh email — Trung tam dao tao");
        }
    }

    public static boolean isConfigured() {
        try {
            Properties p = loadConfig();
            return !p.getProperty("sender.email",    "").isBlank()
                && !p.getProperty("sender.password", "").isBlank();
        } catch (IOException e) {
            return false;
        }
    }

    // ── Send invoice ──────────────────────────────────────────────────

    /**
     * Gửi hóa đơn PDF qua Gmail.
     *
     * @param toEmail       địa chỉ nhận
     * @param recipientName tên hiển thị trong nội dung email
     * @param inv           đối tượng hóa đơn
     * @param pdfBytes      nội dung file PDF đã được sinh sẵn
     */
    public static void sendInvoice(String toEmail,
                                   String recipientName,
                                   Invoice inv,
                                   byte[] pdfBytes) throws Exception {
        Properties cfg = loadConfig();
        String senderEmail = cfg.getProperty("sender.email", "");
        String appPassword  = cfg.getProperty("sender.password", "");

        if (senderEmail.isBlank()) {
            throw new Exception("Chưa cấu hình email gửi. Vui lòng cài đặt Gmail trong phần ⚙.");
        }

        // ── SMTP properties ───────────────────────────────────────────
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host",            "smtp.gmail.com");
        mailProps.put("mail.smtp.port",            "587");
        mailProps.put("mail.smtp.auth",            "true");
        mailProps.put("mail.smtp.starttls.enable", "true");
        mailProps.put("mail.smtp.ssl.trust",       "smtp.gmail.com");
        mailProps.put("mail.smtp.connectiontimeout", "10000");
        mailProps.put("mail.smtp.timeout",           "10000");

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // ── Build message ─────────────────────────────────────────────
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderEmail,
                MimeUtility.encodeText("Trung tâm đào tạo", "UTF-8", "B")));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(
                MimeUtility.encodeText(
                        "Hóa đơn điện tử INV-" + String.format("%03d", inv.getInvoiceId()),
                        "UTF-8", "B"));
        msg.setSentDate(new Date());

        // ── Multipart: body HTML + PDF attachment ─────────────────────
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

    // ── HTML email body ───────────────────────────────────────────────

    private static String buildHtmlBody(String name, Invoice inv) {
        NumberFormat nf  = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                inv.getCreatedAt() != null ? inv.getCreatedAt() : new Date());

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;'>"
             + "<div style='max-width:600px;margin:30px auto;background:#fff;border-radius:10px;overflow:hidden;"
             +      "box-shadow:0 2px 12px rgba(0,0,0,.1);'>"

             // Header
             + "<div style='background:#6c5ce7;padding:28px 32px;'>"
             + "<h2 style='color:#fff;margin:0;font-size:20px;'>TRUNG TÂM ĐÀO TẠO</h2>"
             + "<p style='color:rgba(255,255,255,.8);margin:6px 0 0;font-size:13px;'>"
             +    "Hóa đơn điện tử — " + date + "</p>"
             + "</div>"

             // Body
             + "<div style='padding:32px;'>"
             + "<p style='font-size:15px;'>Xin chào <b>" + esc(name) + "</b>,</p>"
             + "<p style='color:#555;font-size:14px;line-height:1.6;'>"
             +    "Trung tâm trân trọng gửi đến bạn <b>hóa đơn điện tử</b> cho khoản thanh toán học phí. "
             +    "Vui lòng xem file PDF đính kèm để lưu trữ.</p>"

             // Invoice info box
             + "<div style='background:#f8f9fa;border-radius:8px;padding:20px;margin:20px 0;"
             +      "border-left:4px solid #6c5ce7;'>"
             + "<table style='width:100%;border-collapse:collapse;font-size:14px;'>"
             + row("Mã hóa đơn",     "INV-" + String.format("%03d", inv.getInvoiceId()))
             + row("Học viên",        esc(inv.getStudentName()))
             + row("Tổng thanh toán",
                   "<b style='color:#6c5ce7;font-size:16px;'>" + nf.format(inv.getFinalAmount()) + "đ</b>")
             + row("Trạng thái",
                   "<span style='color:#19875f;font-weight:bold;'>✔ ĐÃ THANH TOÁN</span>")
             + "</table>"
             + "</div>"

             + "<p style='color:#555;font-size:13px;'>File hóa đơn PDF đính kèm ngay trong email này. "
             +    "Nếu cần hỗ trợ, vui lòng liên hệ <b>028.1234.5678</b>.</p>"
             + "</div>"

             // Footer
             + "<div style='background:#f0effe;padding:16px 32px;text-align:center;"
             +      "font-size:12px;color:#888;'>"
             + "© Trung tâm đào tạo — Email này được gửi tự động, vui lòng không phản hồi."
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
