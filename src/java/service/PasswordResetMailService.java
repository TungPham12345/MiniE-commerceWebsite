package service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class PasswordResetMailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    private PasswordResetMailService() {
    }

    public static boolean isConfigured() {
        return hasText(System.getenv("GMAIL_SMTP_USERNAME"))
                && hasText(System.getenv("GMAIL_SMTP_APP_PASSWORD"));
    }

    public static void sendResetCode(String recipient, String code) throws jakarta.mail.MessagingException {
        String configuredUsername = System.getenv("GMAIL_SMTP_USERNAME");
        String configuredPassword = System.getenv("GMAIL_SMTP_APP_PASSWORD");
        if (!hasText(configuredUsername) || !hasText(configuredPassword)) {
            throw new IllegalStateException("Chưa cấu hình tài khoản gửi email.");
        }
        final String username = configuredUsername.trim();
        final String appPassword = configuredPassword.replaceAll("\\s+", "");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session mailSession = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
        message.setSubject("Mã xác nhận đặt lại mật khẩu - HighFashion", StandardCharsets.UTF_8.name());
        message.setText(
                "Xin chào,\n\nMã xác nhận đặt lại mật khẩu HighFashion của bạn là: " + code
                + "\nMã có hiệu lực trong 10 phút. Không chia sẻ mã này với bất kỳ ai.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.",
                StandardCharsets.UTF_8.name());
        Transport.send(message);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
