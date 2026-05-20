package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RegisterRequestDTO Test")
class RegisterRequestDTOTest {

    @Test
    @DisplayName("Should create RegisterRequestDTO with valid parameters")
    void testRegisterRequestDTOCreation() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.email = "newuser@example.com";
        dto.password = "SecurePass123";
        dto.displayName = "New User";

        assertNotNull(dto);
        assertEquals("newuser@example.com", dto.email);
        assertEquals("SecurePass123", dto.password);
        assertEquals("New User", dto.displayName);
    }

    @Test
    @DisplayName("Should set and get email")
    void testSetAndGetEmail() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.email = "test@example.com";

        assertEquals("test@example.com", dto.email);
    }

    @Test
    @DisplayName("Should set and get password")
    void testSetAndGetPassword() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.password = "MyPassword123";

        assertEquals("MyPassword123", dto.password);
    }

    @Test
    @DisplayName("Should set and get display name")
    void testSetAndGetDisplayName() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.displayName = "John Doe";

        assertEquals("John Doe", dto.displayName);
    }

    @Test
    @DisplayName("Should handle different email formats")
    void testDifferentEmailFormats() {
        RegisterRequestDTO dto1 = new RegisterRequestDTO();
        dto1.email = "user+tag@example.com";

        RegisterRequestDTO dto2 = new RegisterRequestDTO();
        dto2.email = "admin.user@example.co.id";

        assertNotEquals(dto1.email, dto2.email);
    }

    @Test
    @DisplayName("Should handle different password formats")
    void testDifferentPasswordFormats() {
        RegisterRequestDTO dto1 = new RegisterRequestDTO();
        dto1.password = "SimplePassword123";

        RegisterRequestDTO dto2 = new RegisterRequestDTO();
        dto2.password = "C0mpl3x!@#$%^&*()";

        assertNotEquals(dto1.password, dto2.password);
    }

    @Test
    @DisplayName("Should handle long display names")
    void testLongDisplayName() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.displayName = "This Is A Very Long Display Name For Testing Purposes";

        assertEquals("This Is A Very Long Display Name For Testing Purposes", dto.displayName);
    }

    @Test
    @DisplayName("Should allow null values")
    void testNullValues() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.email = null;
        dto.password = null;
        dto.displayName = null;

        assertNull(dto.email);
        assertNull(dto.password);
        assertNull(dto.displayName);
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.email = "";
        dto.password = "";
        dto.displayName = "";

        assertEquals("", dto.email);
        assertEquals("", dto.password);
        assertEquals("", dto.displayName);
    }

    @Test
    @DisplayName("Should handle special characters in display name")
    void testSpecialCharactersInDisplayName() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.displayName = "John O'Brien-Smith";

        assertEquals("John O'Brien-Smith", dto.displayName);
    }
}
