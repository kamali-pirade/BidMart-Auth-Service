package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    private EmailService emailService;
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    private JavaMailSender mailSender;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mailSenderProvider = mock(ObjectProvider.class);
        emailService = new EmailService(mailSenderProvider);
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
    }

    @Test
    void sendVerificationEmailWithSmtp() {
        configureSmtp();

        emailService.sendVerificationEmail("test@test.com", "http://link");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void missingSmtpFallsBackToLogWithoutThrowing() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "http://link"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void missingProvidersDoNotThrow() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "http://link"));
        assertDoesNotThrow(() -> emailService.sendResetPasswordEmail("test@test.com", "http://link"));
        assertDoesNotThrow(() -> emailService.sendTwoFactorEmail("test@test.com", "123456"));
    }

    @Test
    void disabledEmailDoesNotThrow() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "http://link"));
    }

    @Test
    void smtpFailureFallsBackToLogWithoutThrowing() {
        configureSmtp();
        doThrow(new MailSendException("smtp down"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendResetPasswordEmail("test@test.com", "http://link"));
    }

    private void configureSmtp() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSenderProvider.getObject()).thenReturn(mailSender);
        ReflectionTestUtils.setField(emailService, "smtpUsername", "bidmartb02@gmail.com");
        ReflectionTestUtils.setField(emailService, "smtpPassword", "app-password");
        ReflectionTestUtils.setField(emailService, "mailFromEmail", "bidmartb02@gmail.com");
    }
}
