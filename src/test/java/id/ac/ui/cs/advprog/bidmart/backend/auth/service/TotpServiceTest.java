package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        totpService = new TotpService();
    }

    @Test
    void testGenerateBase32Secret() {
        String secret1 = totpService.generateBase32Secret();
        String secret2 = totpService.generateBase32Secret();

        assertNotNull(secret1);
        assertNotNull(secret2);
        assertTrue(secret1.length() > 0);
        assertTrue(secret2.length() > 0);
        // Two random secrets should be different
        assertFalse(secret1.equals(secret2));
    }

    @Test
    void testGenerateCodeForStep() {
        String secret = totpService.generateBase32Secret();
        String code = totpService.generateCodeForStep(secret, 0);

        assertNotNull(code);
        assertEquals(6, code.length());
        // Code should be all digits
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void testGenerateCodeForStepVariousSteps() {
        String secret = totpService.generateBase32Secret();

        String code1 = totpService.generateCodeForStep(secret, 1);
        String code2 = totpService.generateCodeForStep(secret, 2);
        String code3 = totpService.generateCodeForStep(secret, 100);

        assertEquals(6, code1.length());
        assertEquals(6, code2.length());
        assertEquals(6, code3.length());
    }

    @Test
    void testVerifyCodeValid() {
        String secret = totpService.generateBase32Secret();
        long currentStep = System.currentTimeMillis() / 1000 / 30;

        // Get code for current step
        String currentCode = totpService.generateCodeForStep(secret, currentStep);

        // Verify current code should be valid (with time window)
        assertTrue(totpService.verifyCode(secret, currentCode));
    }

    @Test
    void testVerifyCodeInvalid() {
        String secret = totpService.generateBase32Secret();
        String invalidCode = "000000";

        assertFalse(totpService.verifyCode(secret, invalidCode));
    }

    @Test
    void testVerifyCodeNullSecret() {
        assertFalse(totpService.verifyCode(null, "123456"));
    }

    @Test
    void testVerifyCodeBlankSecret() {
        assertFalse(totpService.verifyCode("", "123456"));
    }

    @Test
    void testVerifyCodeWhitespaceSecret() {
        assertFalse(totpService.verifyCode("   ", "123456"));
    }

    @Test
    void testVerifyCodeNullCode() {
        String secret = totpService.generateBase32Secret();
        assertFalse(totpService.verifyCode(secret, null));
    }

    @Test
    void testVerifyCodeBlankCode() {
        String secret = totpService.generateBase32Secret();
        assertFalse(totpService.verifyCode(secret, ""));
    }

    @Test
    void testVerifyCodeWhitespaceCode() {
        String secret = totpService.generateBase32Secret();
        assertFalse(totpService.verifyCode(secret, "   "));
    }

    @Test
    void testVerifyCodeWithTrimming() {
        String secret = totpService.generateBase32Secret();
        long currentStep = System.currentTimeMillis() / 1000 / 30;
        String currentCode = totpService.generateCodeForStep(secret, currentStep);

        // Verify code with spaces should be trimmed
        assertTrue(totpService.verifyCode(secret, "  " + currentCode + "  "));
    }

    @Test
    void testVerifyCodeTimeWindow() {
        String secret = totpService.generateBase32Secret();
        long currentStep = System.currentTimeMillis() / 1000 / 30;

        // Get code for current step, -1 step, and +1 step
        String prevCode = totpService.generateCodeForStep(secret, currentStep - 1);
        String currentCode = totpService.generateCodeForStep(secret, currentStep);
        String nextCode = totpService.generateCodeForStep(secret, currentStep + 1);

        // All should be valid within the time window
        assertTrue(totpService.verifyCode(secret, prevCode));
        assertTrue(totpService.verifyCode(secret, currentCode));
        assertTrue(totpService.verifyCode(secret, nextCode));
    }

    @Test
    void testVerifyCodeOutOfTimeWindow() {
        String secret = totpService.generateBase32Secret();
        long currentStep = System.currentTimeMillis() / 1000 / 30;

        // Get code for -2 step (outside of time window)
        String oldCode = totpService.generateCodeForStep(secret, currentStep - 2);

        // This code might be invalid due to being outside the time window
        assertFalse(totpService.verifyCode(secret, oldCode));
    }

    @Test
    void testGenerateCodeForStepInvalidSecret() {
        // Invalid secret should return empty string or valid result
        String result = totpService.generateCodeForStep("INVALID", 0);
        // The method catches exceptions and returns empty string
        assertTrue(result.isEmpty() || result.matches("\\d{6}"));
    }

    @Test
    void testGenerateCodeForStepWithNoDecodedKeyReturnsEmptyString() {
        assertEquals("", totpService.generateCodeForStep("!", 0));
    }

    @Test
    void testPrivateBase32HelpersCoverRemainderAndResizeBranches() {
        String encodedOneByte = ReflectionTestUtils.invokeMethod(totpService, "encodeBase32", new byte[]{(byte) 0xff});
        assertEquals("74", encodedOneByte);

        byte[] decodedWithInvalidCharacters = ReflectionTestUtils.invokeMethod(totpService, "decodeBase32", "!!!!");
        assertArrayEquals(new byte[0], decodedWithInvalidCharacters);
    }

    @Test
    void testBase32Encoding() {
        // Test that the service can handle generated secrets
        for (int i = 0; i < 5; i++) {
            String secret = totpService.generateBase32Secret();
            String code = totpService.generateCodeForStep(secret, i);
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"));
        }
    }

    @Test
    void testMultipleCodesConsistency() {
        String secret = totpService.generateBase32Secret();
        long step = 12345L;

        String code1 = totpService.generateCodeForStep(secret, step);
        String code2 = totpService.generateCodeForStep(secret, step);

        // Same secret and step should generate same code
        assertEquals(code1, code2);
    }

    @Test
    void testDifferentSecretsGenerateDifferentCodes() {
        String secret1 = totpService.generateBase32Secret();
        String secret2 = totpService.generateBase32Secret();
        long step = 12345L;

        String code1 = totpService.generateCodeForStep(secret1, step);
        String code2 = totpService.generateCodeForStep(secret2, step);

        // Different secrets should (likely) generate different codes
        // Note: This could theoretically fail due to collision, but extremely unlikely
        assertFalse(code1.equals(code2));
    }
}
