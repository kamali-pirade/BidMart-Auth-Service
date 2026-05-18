package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient resendClient;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:}")
    private String resendFromEmail;

    public EmailService(RestClient.Builder restClientBuilder) {
        this.resendClient = restClientBuilder.baseUrl("https://api.resend.com").build();
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
            log.warn("Email sending disabled. {} for {}: {}", fallbackLabel, to, fallbackValue);
            return;
        }

        if (resendApiKey == null || resendApiKey.isBlank()
                || resendFromEmail == null || resendFromEmail.isBlank()) {
            log.warn("Resend is not configured. {} for {}: {}", fallbackLabel, to, fallbackValue);
            return;
        }

        try {
            resendClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .body(Map.of(
                            "from", resendFromEmail,
                            "to", new String[]{to},
                            "subject", subject,
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent to {} via Resend", to);
        } catch (RestClientException e) {
            log.error("Failed to send email to {} via Resend: {}", to, e.getMessage());
            log.info("Fallback {} for {}: {}", fallbackLabel, to, fallbackValue);
        }
    }
}
