package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginSuccessResponseDTO Test")
class LoginSuccessResponseDTOTest {

    @Test
    @DisplayName("Should create LoginSuccessResponseDTO with valid parameters")
    void testLoginSuccessResponseDTOCreation() {
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        long expiresIn = 3600;
        UserResponseDTO user = new UserResponseDTO(
            UUID.randomUUID(),
            "user@example.com",
            "Test User",
            true,
            Instant.now(),
            Arrays.asList("USER")
        );

        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO(accessToken, refreshToken, expiresIn, user);

        assertNotNull(dto);
        assertEquals(accessToken, dto.accessToken);
        assertEquals(refreshToken, dto.refreshToken);
        assertEquals(expiresIn, dto.expiresIn);
        assertEquals(user, dto.user);
    }

    @Test
    @DisplayName("Should get access token")
    void testGetAccessToken() {
        String accessToken = "token_abc123";
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO(accessToken, "refresh", 3600, null);

        assertEquals("token_abc123", dto.accessToken);
    }

    @Test
    @DisplayName("Should get refresh token")
    void testGetRefreshToken() {
        String refreshToken = "refresh_xyz789";
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", refreshToken, 3600, null);

        assertEquals("refresh_xyz789", dto.refreshToken);
    }

    @Test
    @DisplayName("Should get expires in")
    void testGetExpiresIn() {
        long expiresIn = 7200;
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", expiresIn, null);

        assertEquals(7200, dto.expiresIn);
    }

    @Test
    @DisplayName("Should get user response")
    void testGetUser() {
        UserResponseDTO user = new UserResponseDTO(
            UUID.randomUUID(),
            "test@example.com",
            "Test",
            true,
            Instant.now(),
            Arrays.asList()
        );
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", 3600, user);

        assertEquals(user, dto.user);
        assertEquals("test@example.com", dto.user.email);
    }

    @Test
    @DisplayName("Should handle different expiry times")
    void testDifferentExpiryTimes() {
        LoginSuccessResponseDTO dto1 = new LoginSuccessResponseDTO("access", "refresh", 1800, null);
        LoginSuccessResponseDTO dto2 = new LoginSuccessResponseDTO("access", "refresh", 3600, null);

        assertNotEquals(dto1.expiresIn, dto2.expiresIn);
    }

    @Test
    @DisplayName("Should set and modify properties after creation")
    void testModifyAfterCreation() {
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("old_access", "old_refresh", 3600, null);

        dto.accessToken = "new_access";
        dto.refreshToken = "new_refresh";
        dto.expiresIn = 7200;

        assertEquals("new_access", dto.accessToken);
        assertEquals("new_refresh", dto.refreshToken);
        assertEquals(7200, dto.expiresIn);
    }

    @Test
    @DisplayName("Should handle null user response")
    void testNullUser() {
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", 3600, null);

        assertNull(dto.user);
    }

    @Test
    @DisplayName("Should handle null tokens")
    void testNullTokens() {
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO(null, null, 3600, null);

        assertNull(dto.accessToken);
        assertNull(dto.refreshToken);
    }

    @Test
    @DisplayName("Should handle zero expires in")
    void testZeroExpiresIn() {
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", 0, null);

        assertEquals(0, dto.expiresIn);
    }

    @Test
    @DisplayName("Should handle large expires in values")
    void testLargeExpiresIn() {
        long largeValue = 86400; // 24 hours
        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", largeValue, null);

        assertEquals(86400, dto.expiresIn);
    }

    @Test
    @DisplayName("Should preserve user information")
    void testPreserveUserInformation() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        UserResponseDTO user = new UserResponseDTO(
            userId,
            "user@example.com",
            "User Name",
            true,
            createdAt,
            Arrays.asList("USER", "SELLER")
        );

        LoginSuccessResponseDTO dto = new LoginSuccessResponseDTO("access", "refresh", 3600, user);

        assertEquals(userId, dto.user.id);
        assertEquals("user@example.com", dto.user.email);
        assertEquals("User Name", dto.user.displayName);
        assertTrue(dto.user.emailVerified);
        assertEquals(2, dto.user.roles.size());
    }
}
