package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UpdateProfileRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.ChangePasswordRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MeControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private MeController meController;

    private Authentication authenticated(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(Map.of("email", email));
        return auth;
    }

    @Test
    void me_NoAuth() {
        try {
            meController.me(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Unauthorized", e.getMessage());
        }
    }

    @Test
    void me_Unauthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        try {
            meController.me(auth);
        } catch (IllegalArgumentException e) {
            assertEquals("Unauthorized", e.getMessage());
        }
    }

    @Test
    void me_NotFound() {
        Authentication auth = authenticated("e");
        when(authService.getUserByEmail("e")).thenThrow(new IllegalArgumentException("User not found"));

        try {
            meController.me(auth);
        } catch (IllegalArgumentException e) {
            assertEquals("User not found", e.getMessage());
        }
    }

    @Test
    void me_Success() {
        Authentication auth = authenticated("e");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("e");
        user.setDisplayName("D");
        user.setAvatarUrl("A");

        when(authService.getUserByEmail("e")).thenReturn(user);

        ResponseEntity<?> res = meController.me(auth);

        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void meV2_Success() {
        Authentication auth = authenticated("e");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("e");
        user.setDisplayName("D");

        when(authService.getUserByEmail("e")).thenReturn(user);

        assertEquals(200, meController.meV2(auth).getStatusCode().value());
    }

    @Test
    void me_Success_DefaultValues() {
        Authentication auth = authenticated("e");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail("e");
        user.setDisplayName(null);
        user.setAvatarUrl(null);

        when(authService.getUserByEmail("e")).thenReturn(user);

        ResponseEntity<?> res = meController.me(auth);

        assertEquals(200, res.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("Pengguna Baru", body.get("displayName"));
        assertEquals("", body.get("avatarUrl"));
    }

    @Test
    void updateProfile_Success() {
        Authentication auth = authenticated("e");

        User user = new User();
        user.setEmail("e");

        User updated = new User();
        updated.setEmail("e");
        updated.setDisplayName("N");
        updated.setAvatarUrl("U");

        when(authService.getUserByEmail("e")).thenReturn(user);
        when(authService.updateProfile(user, "N", "U")).thenReturn(updated);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.displayName = "N";
        req.avatarUrl = "U";

        ResponseEntity<?> res = meController.updateProfile(auth, req);

        assertEquals(200, res.getStatusCode().value());
        verify(authService).updateProfile(user, "N", "U");
    }

    @Test
    void updateProfile_NoAuth() {
        try {
            meController.updateProfile(null, new UpdateProfileRequest());
        } catch (IllegalArgumentException e) {
            assertEquals("Unauthorized", e.getMessage());
        }
    }

    @Test
    void updateProfile_NotFound() {
        Authentication auth = authenticated("e");
        when(authService.getUserByEmail("e")).thenThrow(new IllegalArgumentException("User not found"));

        try {
            meController.updateProfile(auth, new UpdateProfileRequest());
        } catch (IllegalArgumentException e) {
            assertEquals("User not found", e.getMessage());
        }
    }

    @Test
    void updateProfile_AvatarUrlNull() {
        Authentication auth = authenticated("e");

        User user = new User();
        user.setEmail("e");

        User updated = new User();
        updated.setEmail("e");
        updated.setDisplayName("N");
        updated.setAvatarUrl(null);

        when(authService.getUserByEmail("e")).thenReturn(user);
        when(authService.updateProfile(user, "N", null)).thenReturn(updated);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.displayName = "N";
        req.avatarUrl = null;

        ResponseEntity<?> res = meController.updateProfile(auth, req);

        assertEquals(200, res.getStatusCode().value());

        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("", body.get("avatarUrl"));
    }

    @Test
    void updateProfileV2_SuccessAndChangePassword() {
        Authentication auth = authenticated("e");

        User user = new User();
        user.setEmail("e");

        User updated = new User();
        updated.setEmail("e");
        updated.setDisplayName("N");
        updated.setAvatarUrl("U");

        when(authService.getUserByEmail("e")).thenReturn(user);
        when(authService.updateProfile(user, "N", "U")).thenReturn(updated);

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.displayName = "N";
        req.avatarUrl = "U";

        assertEquals(200, meController.updateProfileV2(auth, req).getStatusCode().value());

        User updatedNoAvatar = new User();
        updatedNoAvatar.setEmail("e");
        updatedNoAvatar.setDisplayName("N");
        updatedNoAvatar.setAvatarUrl(null);
        when(authService.updateProfile(user, "N", null)).thenReturn(updatedNoAvatar);
        UpdateProfileRequest noAvatar = new UpdateProfileRequest();
        noAvatar.displayName = "N";
        noAvatar.avatarUrl = null;
        Map<String, Object> noAvatarBody = (Map<String, Object>) meController.updateProfileV2(auth, noAvatar).getBody();
        assertEquals("", noAvatarBody.get("avatarUrl"));

        ChangePasswordRequestDTO password = new ChangePasswordRequestDTO();
        password.currentPassword = "old-password";
        password.newPassword = "new-password";
        assertEquals("Password updated", meController.changePassword(auth, password).getBody().get("message"));
        verify(authService).changePassword(user, password);
    }

    @Test
    void currentUserRejectsMissingEmail() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(Map.of("userId", "1"));

        try {
            meController.me(auth);
        } catch (IllegalArgumentException e) {
            assertEquals("Unauthorized", e.getMessage());
        }
    }
}
