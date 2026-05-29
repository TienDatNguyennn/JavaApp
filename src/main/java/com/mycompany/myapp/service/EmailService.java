package com.mycompany.myapp.service;

import java.util.Properties;
import java.util.regex.Pattern;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private final String senderEmail = "tiendat17126@gmail.com";
    private final String appPassword = "ggtt fodi cdqb algx";

    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) throws Exception {
        validateEmail(toEmail);

        if (subject == null || subject.trim().isEmpty()) {
            subject = "Thông báo từ trung tâm";
        }

        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new Exception("Nội dung email không được để trống.");
        }

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(senderEmail, "ALPHA LOGIC CENTER", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail.trim()));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private void validateEmail(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email người nhận không được để trống.");
        }

        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (!Pattern.matches(regex, email.trim())) {
            throw new Exception("Email người nhận không đúng định dạng.");
        }
    }
}