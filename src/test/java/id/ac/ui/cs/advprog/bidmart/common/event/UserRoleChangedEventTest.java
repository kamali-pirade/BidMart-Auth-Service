package id.ac.ui.cs.advprog.bidmart.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserRoleChangedEvent Test")
class UserRoleChangedEventTest {

    @Test
    @DisplayName("Should create UserRoleChangedEvent with valid parameters")
    void testUserRoleChangedEventCreation() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ADMIN", "USER");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertNotNull(event);
        assertEquals(userId, event.getUserId());
        assertEquals(roles, event.getRoles());
        assertEquals(happenedAt, event.getHappenedAt());
    }

    @Test
    @DisplayName("Should get userId from event")
    void testGetUserId() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ADMIN", "USER");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertEquals(userId, event.getUserId());
    }

    @Test
    @DisplayName("Should get roles from event")
    void testGetRoles() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ADMIN", "USER", "SELLER");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertEquals(roles, event.getRoles());
        assertEquals(3, event.getRoles().size());
        assertTrue(event.getRoles().contains("ADMIN"));
        assertTrue(event.getRoles().contains("USER"));
        assertTrue(event.getRoles().contains("SELLER"));
    }

    @Test
    @DisplayName("Should get happenedAt from event")
    void testGetHappenedAt() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("USER");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertEquals(happenedAt, event.getHappenedAt());
    }

    @Test
    @DisplayName("Should handle empty roles list")
    void testEmptyRolesList() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList();
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertNotNull(event.getRoles());
        assertTrue(event.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should handle single role")
    void testSingleRole() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("ADMIN");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertEquals(1, event.getRoles().size());
        assertEquals("ADMIN", event.getRoles().get(0));
    }

    @Test
    @DisplayName("Should preserve role order")
    void testPreserveRoleOrder() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("SELLER", "ADMIN", "USER");
        Instant happenedAt = Instant.now();

        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, roles, happenedAt);

        assertEquals("SELLER", event.getRoles().get(0));
        assertEquals("ADMIN", event.getRoles().get(1));
        assertEquals("USER", event.getRoles().get(2));
    }

    @Test
    @DisplayName("Should handle different timestamps")
    void testDifferentTimestamps() {
        UUID userId = UUID.randomUUID();
        List<String> roles = Arrays.asList("USER");
        Instant past = Instant.ofEpochSecond(1000000);
        Instant now = Instant.now();

        UserRoleChangedEvent eventPast = new UserRoleChangedEvent(userId, roles, past);
        UserRoleChangedEvent eventNow = new UserRoleChangedEvent(userId, roles, now);

        assertEquals(past, eventPast.getHappenedAt());
        assertEquals(now, eventNow.getHappenedAt());
        assertNotEquals(eventPast.getHappenedAt(), eventNow.getHappenedAt());
    }
}
