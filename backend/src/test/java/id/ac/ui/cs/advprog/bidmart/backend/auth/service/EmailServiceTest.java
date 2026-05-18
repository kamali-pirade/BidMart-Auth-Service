package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EmailServiceTest {

    private EmailService emailService;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        emailService = new EmailService(builder);
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
    }

    @Test
    void sendVerificationEmailWithResend() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "resend-key");
        ReflectionTestUtils.setField(emailService, "resendFromEmail", "BidMart <noreply@example.com>");

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer resend-key"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendVerificationEmail("test@test.com", "http://link");

        server.verify();
    }

    @Test
    void sendResetPasswordEmailWithResend() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "resend-key");
        ReflectionTestUtils.setField(emailService, "resendFromEmail", "BidMart <noreply@example.com>");

        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendResetPasswordEmail("test@test.com", "http://link");

        server.verify();
    }

    @Test
    void missingResendApiKeyDoesNotThrow() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");
        ReflectionTestUtils.setField(emailService, "resendFromEmail", "BidMart <noreply@example.com>");

        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "http://link"));
        assertDoesNotThrow(() -> emailService.sendResetPasswordEmail("test@test.com", "http://link"));
        assertDoesNotThrow(() -> emailService.sendTwoFactorEmail("test@test.com", "123456"));
    }

    @Test
    void disabledEmailAndResendFailureDoNotThrow() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);
        assertDoesNotThrow(() -> emailService.sendVerificationEmail("test@test.com", "http://link"));

        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "resendApiKey", "resend-key");
        ReflectionTestUtils.setField(emailService, "resendFromEmail", "BidMart <noreply@example.com>");
        server.expect(once(), requestTo("https://api.resend.com/emails"))
                .andRespond(withServerError());

        assertDoesNotThrow(() -> emailService.sendResetPasswordEmail("test@test.com", "http://link"));
    }
}
