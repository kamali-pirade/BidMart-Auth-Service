package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.SessionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @InjectMocks
    private SessionController sessionController;

    @Mock
    private AuthService authService;

    @Test
    void me_NoAuth() {
        ResponseEntity<Map<String, Object>> res = sessionController.me(null);
        assertEquals(401, res.getStatusCode().value());
    }

    @Test
    void me_NoPrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(null);

        ResponseEntity<Map<String, Object>> res = sessionController.me(auth);

        assertEquals(401, res.getStatusCode().value());
    }

    @Test
    void me_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(Map.of("userId", "1", "email", "t@t.com"));

        ResponseEntity<Map<String, Object>> res = sessionController.me(auth);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("1", res.getBody().get("userId"));
    }

    @Test
    void listSessionsAndRevokeSession() {
        Authentication auth = mock(Authentication.class);
        User user = new User();
        when(auth.getPrincipal()).thenReturn(Map.of("email", "t@t.com", "sessionId", "sid"));
        when(authService.getUserByEmail("t@t.com")).thenReturn(user);
        SessionResponseDTO session = new SessionResponseDTO(UUID.randomUUID(), "device", "ip", Instant.now(), true);
        when(authService.getActiveSessions(user, "sid")).thenReturn(List.of(session));

        ResponseEntity<List<SessionResponseDTO>> list = sessionController.listSessions(auth);
        assertEquals(List.of(session), list.getBody());

        UUID sessionId = UUID.randomUUID();
        assertEquals(204, sessionController.revokeSession(sessionId, auth).getStatusCode().value());
        verify(authService).revokeSession(user, sessionId);
    }

    @Test
    void listSessionsAllowsMissingSessionId() {
        Authentication auth = mock(Authentication.class);
        User user = new User();
        when(auth.getPrincipal()).thenReturn(Map.of("email", "t@t.com"));
        when(authService.getUserByEmail("t@t.com")).thenReturn(user);
        when(authService.getActiveSessions(user, null)).thenReturn(List.of());

        assertEquals(List.of(), sessionController.listSessions(auth).getBody());
    }

    @Test
    void currentUserRejectsUnauthorizedRequests() {
        assertEquals("Unauthorized", assertThrows(
                IllegalArgumentException.class,
                () -> sessionController.listSessions(null)
        ).getMessage());

        Authentication noPrincipal = mock(Authentication.class);
        when(noPrincipal.getPrincipal()).thenReturn(null);
        assertEquals("Unauthorized", assertThrows(
                IllegalArgumentException.class,
                () -> sessionController.listSessions(noPrincipal)
        ).getMessage());

        Authentication noEmail = mock(Authentication.class);
        when(noEmail.getPrincipal()).thenReturn(Map.of("userId", "1"));
        assertEquals("Unauthorized", assertThrows(
                IllegalArgumentException.class,
                () -> sessionController.listSessions(noEmail)
        ).getMessage());
    }
}
