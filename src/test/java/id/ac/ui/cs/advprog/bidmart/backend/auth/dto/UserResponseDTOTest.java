package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserResponseDTO Test")
class UserResponseDTOTest {

    @Test
    @DisplayName("Should create UserResponseDTO with valid parameters")
    void testUserResponseDTOCreation() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String displayName = "John Doe";
        boolean emailVerified = true;
        Instant createdAt = Instant.now();
        List<String> roles = Arrays.asList("USER", "SELLER");

        UserResponseDTO dto = new UserResponseDTO(userId, email, displayName, emailVerified, createdAt, roles);

        assertNotNull(dto);
        assertEquals(userId, dto.id);
        assertEquals(email, dto.email);
        assertEquals(displayName, dto.displayName);
        assertTrue(dto.emailVerified);
        assertEquals(createdAt, dto.createdAt);
        assertEquals(roles, dto.roles);
    }

    @Test
    @DisplayName("Should get user id")
    void testGetId() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", true, Instant.now(), Arrays.asList());

        assertEquals(userId, dto.id);
    }

    @Test
    @DisplayName("Should get email")
    void testGetEmail() {
        UUID userId = UUID.randomUUID();
        String email = "contact@bidmart.id";
        UserResponseDTO dto = new UserResponseDTO(userId, email, "Test", false, Instant.now(), Arrays.asList());

        assertEquals(email, dto.email);
    }

    @Test
    @DisplayName("Should get display name")
    void testGetDisplayName() {
        UUID userId = UUID.randomUUID();
        String displayName = "Jane Smith";
        UserResponseDTO dto = new UserResponseDTO(userId, "jane@example.com", displayName, true, Instant.now(), Arrays.asList());

        assertEquals(displayName, dto.displayName);
    }

    @Test
    @DisplayName("Should get email verified status")
    void testGetEmailVerified() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto1 = new UserResponseDTO(userId, "test@example.com", "Test", true, Instant.now(), Arrays.asList());
        UserResponseDTO dto2 = new UserResponseDTO(userId, "test@example.com", "Test", false, Instant.now(), Arrays.asList());

        assertTrue(dto1.emailVerified);
        assertFalse(dto2.emailVerified);
    }

    @Test
    @DisplayName("Should get created at timestamp")
    void testGetCreatedAt() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.ofEpochSecond(1000000);
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", true, createdAt, Arrays.asList());

        assertEquals(createdAt, dto.createdAt);
    }

    @Test
    @DisplayName("Should get roles list")
    void testGetRoles() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("USER", "SELLER", "ADMIN");
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", true, Instant.now(), roles);

        assertEquals(roles, dto.roles);
        assertEquals(3, dto.roles.size());
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void testEmptyRoles() {
        UUID userId = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", false, Instant.now(), Arrays.asList());

        assertTrue(dto.roles.isEmpty());
    }

    @Test
    @DisplayName("Should handle single role")
    void testSingleRole() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("USER");
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", true, Instant.now(), roles);

        assertEquals(1, dto.roles.size());
        assertEquals("USER", dto.roles.get(0));
    }

    @Test
    @DisplayName("Should set and modify properties after creation")
    void testModifyAfterCreation() {
        UUID userId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        UserResponseDTO dto = new UserResponseDTO(userId, "old@example.com", "Old Name", false, Instant.now(), Arrays.asList());

        dto.id = newUserId;
        dto.email = "new@example.com";
        dto.displayName = "New Name";
        dto.emailVerified = true;
        dto.roles = Arrays.asList("ADMIN");

        assertEquals(newUserId, dto.id);
        assertEquals("new@example.com", dto.email);
        assertEquals("New Name", dto.displayName);
        assertTrue(dto.emailVerified);
        assertEquals(1, dto.roles.size());
    }

    @Test
    @DisplayName("Should preserve role order")
    void testPreserveRoleOrder() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("SELLER", "USER", "ADMIN");
        UserResponseDTO dto = new UserResponseDTO(userId, "test@example.com", "Test", true, Instant.now(), roles);

        assertEquals("SELLER", dto.roles.get(0));
        assertEquals("USER", dto.roles.get(1));
        assertEquals("ADMIN", dto.roles.get(2));
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        UserResponseDTO dto = new UserResponseDTO(null, null, null, false, null, null);

        assertNull(dto.id);
        assertNull(dto.email);
        assertNull(dto.displayName);
        assertFalse(dto.emailVerified);
        assertNull(dto.createdAt);
        assertNull(dto.roles);
    }

    @Test
    @DisplayName("Should handle different timestamps")
    void testDifferentTimestamps() {
        UUID userId = UUID.randomUUID();
        Instant past = Instant.ofEpochSecond(1000000);
        Instant now = Instant.now();

        UserResponseDTO dtoPast = new UserResponseDTO(userId, "test@example.com", "Test", true, past, Arrays.asList());
        UserResponseDTO dtoNow = new UserResponseDTO(userId, "test@example.com", "Test", true, now, Arrays.asList());

        assertNotEquals(dtoPast.createdAt, dtoNow.createdAt);
    }
}
