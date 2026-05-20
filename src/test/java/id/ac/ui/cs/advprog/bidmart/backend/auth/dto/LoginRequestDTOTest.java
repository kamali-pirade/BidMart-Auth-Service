package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginRequestDTO Test")
class LoginRequestDTOTest {

    @Test
    @DisplayName("Should create LoginRequestDTO with valid parameters")
    void testLoginRequestDTOCreation() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.email = "user@example.com";
        dto.password = "password123";

        assertNotNull(dto);
        assertEquals("user@example.com", dto.email);
        assertEquals("password123", dto.password);
    }

    @Test
    @DisplayName("Should set and get email")
    void testSetAndGetEmail() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.email = "test@example.com";

        assertEquals("test@example.com", dto.email);
    }

    @Test
    @DisplayName("Should set and get password")
    void testSetAndGetPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.password = "secretPassword";

        assertEquals("secretPassword", dto.password);
    }

    @Test
    @DisplayName("Should handle different email formats")
    void testDifferentEmailFormats() {
        LoginRequestDTO dto1 = new LoginRequestDTO();
        dto1.email = "user@example.com";

        LoginRequestDTO dto2 = new LoginRequestDTO();
        dto2.email = "admin@bidmart.id";

        assertNotEquals(dto1.email, dto2.email);
    }

    @Test
    @DisplayName("Should handle complex passwords")
    void testComplexPasswords() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.password = "P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?";

        assertEquals("P@ssw0rd!#$%^&*()_+-=[]{}|;':\",./<>?", dto.password);
    }

    @Test
    @DisplayName("Should allow null email")
    void testNullEmail() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.email = null;

        assertNull(dto.email);
    }

    @Test
    @DisplayName("Should allow null password")
    void testNullPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.password = null;

        assertNull(dto.password);
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.email = "";
        dto.password = "";

        assertEquals("", dto.email);
        assertEquals("", dto.password);
    }
}
