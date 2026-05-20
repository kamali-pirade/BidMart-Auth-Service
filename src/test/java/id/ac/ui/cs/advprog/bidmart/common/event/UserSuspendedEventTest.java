package id.ac.ui.cs.advprog.bidmart.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserSuspendedEvent Test")
class UserSuspendedEventTest {

    @Test
    @DisplayName("Should create UserSuspendedEvent with valid parameters")
    void testUserSuspendedEventCreation() {
        UUID userId = UUID.randomUUID();
        String reason = "Suspicious activity detected";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertNotNull(event);
        assertEquals(userId, event.getUserId());
        assertEquals(reason, event.getReason());
        assertEquals(happenedAt, event.getHappenedAt());
    }

    @Test
    @DisplayName("Should get userId from event")
    void testGetUserId() {
        UUID userId = UUID.randomUUID();
        String reason = "Account suspended";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertEquals(userId, event.getUserId());
        assertNotNull(event.getUserId());
    }

    @Test
    @DisplayName("Should get reason from event")
    void testGetReason() {
        UUID userId = UUID.randomUUID();
        String reason = "Violation of terms of service";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertEquals(reason, event.getReason());
    }

    @Test
    @DisplayName("Should get happenedAt from event")
    void testGetHappenedAt() {
        UUID userId = UUID.randomUUID();
        String reason = "Banned";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertEquals(happenedAt, event.getHappenedAt());
    }

    @Test
    @DisplayName("Should handle empty reason string")
    void testEmptyReason() {
        UUID userId = UUID.randomUUID();
        String reason = "";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertEquals("", event.getReason());
        assertTrue(event.getReason().isEmpty());
    }

    @Test
    @DisplayName("Should handle null reason")
    void testNullReason() {
        UUID userId = UUID.randomUUID();
        String reason = null;
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertNull(event.getReason());
    }

    @Test
    @DisplayName("Should handle long reason string")
    void testLongReason() {
        UUID userId = UUID.randomUUID();
        String reason = "This is a very long reason explaining why the user account has been suspended due to repeated violations of the platform's terms of service and community guidelines";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event = new UserSuspendedEvent(userId, reason, happenedAt);

        assertEquals(reason, event.getReason());
        assertEquals(164, event.getReason().length());
    }

    @Test
    @DisplayName("Should handle different timestamps")
    void testDifferentTimestamps() {
        UUID userId = UUID.randomUUID();
        String reason = "Suspended";
        Instant past = Instant.ofEpochSecond(1000000);
        Instant now = Instant.now();

        UserSuspendedEvent eventPast = new UserSuspendedEvent(userId, reason, past);
        UserSuspendedEvent eventNow = new UserSuspendedEvent(userId, reason, now);

        assertEquals(past, eventPast.getHappenedAt());
        assertEquals(now, eventNow.getHappenedAt());
        assertNotEquals(eventPast.getHappenedAt(), eventNow.getHappenedAt());
    }

    @Test
    @DisplayName("Should differentiate events by userId")
    void testDifferentUserIds() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        String reason = "Suspended";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event1 = new UserSuspendedEvent(userId1, reason, happenedAt);
        UserSuspendedEvent event2 = new UserSuspendedEvent(userId2, reason, happenedAt);

        assertNotEquals(event1.getUserId(), event2.getUserId());
    }

    @Test
    @DisplayName("Should differentiate events by reason")
    void testDifferentReasons() {
        UUID userId = UUID.randomUUID();
        String reason1 = "Reason 1";
        String reason2 = "Reason 2";
        Instant happenedAt = Instant.now();

        UserSuspendedEvent event1 = new UserSuspendedEvent(userId, reason1, happenedAt);
        UserSuspendedEvent event2 = new UserSuspendedEvent(userId, reason2, happenedAt);

        assertNotEquals(event1.getReason(), event2.getReason());
    }
}
