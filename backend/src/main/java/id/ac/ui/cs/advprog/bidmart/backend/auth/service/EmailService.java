package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.from-email:}")
    private String mailFromEmail;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Async
    public void sendVerificationEmail(String to, String link) {
        sendEmail(
                to,
                "Verify Your BidMart Account",
                "Welcome to BidMart! Please click the link below to verify your email:\n\n" + link,
                "Verification link",
                link
        );
    }

    @Async
    public void sendResetPasswordEmail(String to, String link) {
        sendEmail(
                to,
                "Reset Your BidMart Password",
                "You requested a password reset. Click the link below to set a new password:\n\n"
                        + link + "\n\nIf you didn't request this, please ignore this email.",
                "Password reset link",
                link
        );
    }

    @Async
    public void sendTwoFactorEmail(String to, String code) {
        sendEmail(
                to,
                "Your BidMart 2FA Code",
                "Use this code to continue signing in to BidMart:\n\n" + code,
                "Email 2FA code",
                code
        );
    }

    private void sendEmail(String to, String subject, String text, String fallbackLabel, String fallbackValue) {
        if (!mailEnabled) {
            LOGGER.warn("Email sending disabled. {} for {}: {}", fallbackLabel, to, fallbackValue);
            return;
        }

        if (isSmtpConfigured()) {
            try {
                sendWithSmtp(to, subject, text);
                LOGGER.info("Email sent to {} via SMTP", to);
                return;
            } catch (MailException e) {
                LOGGER.error("Failed to send email to {} via SMTP: {}", to, e.getMessage());
            }
        } else {
            LOGGER.warn("SMTP is not configured. Using fallback log for {}", to);
        }

        LOGGER.info("Fallback {} for {}: {}", fallbackLabel, to, fallbackValue);
    }

    private boolean isSmtpConfigured() {
        return mailSenderProvider.getIfAvailable() != null
                && smtpUsername != null && !smtpUsername.isBlank()
                && smtpPassword != null && !smtpPassword.isBlank()
                && mailFromEmail != null && !mailFromEmail.isBlank();
    }

    private void sendWithSmtp(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSenderProvider.getObject().send(message);
    }
}
