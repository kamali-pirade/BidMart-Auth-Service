package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalCreateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRolesRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserStatusRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.UserStatus;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternalUserControllerTest {

    private AuthService authService;
    private InternalUserController controller;

    private UUID userId;
    private User activeUser;
    private User suspendedUser;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        controller = new InternalUserController(authService);

        userId = UUID.randomUUID();

        activeUser = new User();
        activeUser.setId(userId);
        activeUser.setEmail("test@mail.com");
        activeUser.setDisplayName("Test User");
        activeUser.setAvatarUrl("avatar.png");
        activeUser.setEmailVerified(true);
        activeUser.setRolesList(List.of("BUYER", "SELLER"));
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setCreatedAt(Instant.now());
        activeUser.setUpdatedAt(Instant.now());

        suspendedUser = new User();
        suspendedUser.setId(userId);
        suspendedUser.setEmail("suspended@mail.com");
        suspendedUser.setDisplayName("Suspended User");
        suspendedUser.setAvatarUrl(null);
        suspendedUser.setEmailVerified(false);
        suspendedUser.setRolesList(List.of("BUYER"));
        suspendedUser.setStatus(UserStatus.SUSPENDED);
        suspendedUser.setCreatedAt(Instant.now());
        suspendedUser.setUpdatedAt(Instant.now());
    }

    @Test
    void validateUserShouldReturnOkWhenUserIsActive() {
        when(authService.getUserById(userId)).thenReturn(activeUser);

        ResponseEntity<Map<String, Object>> response = controller.validateUser(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("valid"));
        assertEquals(userId, response.getBody().get("userId"));
        assertEquals("ACTIVE", response.getBody().get("status"));
    }

    @Test
    void validateUserShouldReturnForbiddenWhenUserIsSuspended() {
        when(authService.getUserById(userId)).thenReturn(suspendedUser);

        ResponseEntity<Map<String, Object>> response = controller.validateUser(userId);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("valid"));
        assertEquals(userId, response.getBody().get("userId"));
        assertEquals("SUSPENDED", response.getBody().get("status"));
        assertEquals("User ini telah di-suspend.", response.getBody().get("message"));
    }

    @Test
    void getUserByIdShouldReturnInternalUserResponse() {
        when(authService.getUserById(userId)).thenReturn(activeUser);

        ResponseEntity<InternalUserResponseDTO> response = controller.getUserById(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id);
        assertEquals("test@mail.com", response.getBody().email);
        assertEquals("Test User", response.getBody().displayName);
        assertEquals("avatar.png", response.getBody().avatarUrl);
        assertTrue(response.getBody().emailVerified);
        assertEquals(List.of("BUYER", "SELLER"), response.getBody().roles);
        assertEquals("ACTIVE", response.getBody().status);
    }

    @Test
    void getUserByEmailShouldReturnInternalUserResponse() {
        when(authService.getUserByEmail("test@mail.com")).thenReturn(activeUser);

        ResponseEntity<InternalUserResponseDTO> response = controller.getUserByEmail("test@mail.com");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id);
        assertEquals("test@mail.com", response.getBody().email);
        assertEquals("Test User", response.getBody().displayName);
    }

    @Test
    void getUserRolesShouldReturnRoles() {
        when(authService.getUserById(userId)).thenReturn(activeUser);

        ResponseEntity<Map<String, Object>> response = controller.getUserRoles(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().get("userId"));
        assertEquals(List.of("BUYER", "SELLER"), response.getBody().get("roles"));
    }

    @Test
    void getUserStatusShouldReturnStatus() {
        when(authService.getUserById(userId)).thenReturn(activeUser);

        ResponseEntity<Map<String, Object>> response = controller.getUserStatus(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().get("userId"));
        assertEquals("ACTIVE", response.getBody().get("status"));
        assertEquals(true, response.getBody().get("valid"));
    }

    @Test
    void getUserStatusShouldReturnInvalidWhenSuspended() {
        when(authService.getUserById(userId)).thenReturn(suspendedUser);

        ResponseEntity<Map<String, Object>> response = controller.getUserStatus(userId);

        assertEquals(false, response.getBody().get("valid"));
        assertEquals("SUSPENDED", response.getBody().get("status"));
    }

    @Test
    void getAllUsersShouldReturnListOfUsers() {
        when(authService.getAllUsers()).thenReturn(List.of(activeUser, suspendedUser));

        ResponseEntity<?> response = controller.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        List<?> body = (List<?>) response.getBody();
        assertEquals(2, body.size());
    }

    @Test
    void responseMappingUsesDefaultsForNullProfileFieldsRolesAndStatus() {
        User user = mock(User.class);
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn("default@mail.com");
        when(user.getDisplayName()).thenReturn(null);
        when(user.getAvatarUrl()).thenReturn(null);
        when(user.isEmailVerified()).thenReturn(false);
        when(user.getRolesList()).thenReturn(null);
        when(user.getStatus()).thenReturn(null);
        when(user.getCreatedAt()).thenReturn(now);
        when(user.getUpdatedAt()).thenReturn(now);
        when(authService.getUserById(id)).thenReturn(user);

        InternalUserResponseDTO body = controller.getUserById(id).getBody();

        assertEquals("Pengguna Baru", body.displayName);
        assertEquals("", body.avatarUrl);
        assertEquals(List.of(), body.roles);
        assertEquals("ACTIVE", body.status);
    }

    @Test
    void createUserShouldReturnCreatedUser() {
        InternalCreateUserRequestDTO request = new InternalCreateUserRequestDTO();
        request.email = "new@mail.com";
        request.password = "password123";
        request.displayName = "New User";
        request.avatarUrl = "";
        request.emailVerified = true;
        request.roles = List.of("BUYER");
        request.status = "ACTIVE";

        when(authService.createInternalUser(request)).thenReturn(activeUser);

        ResponseEntity<?> response = controller.createUser(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        InternalUserResponseDTO body = (InternalUserResponseDTO) response.getBody();
        assertEquals(userId, body.id);
        assertEquals("test@mail.com", body.email);
    }

    @Test
    void updateUserShouldReturnUpdatedUser() {
        InternalUpdateUserRequestDTO request = new InternalUpdateUserRequestDTO();
        request.displayName = "Updated User";
        request.avatarUrl = "updated.png";
        request.emailVerified = true;

        when(authService.updateInternalUser(userId, request)).thenReturn(activeUser);

        ResponseEntity<?> response = controller.updateUser(userId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        InternalUserResponseDTO body = (InternalUserResponseDTO) response.getBody();
        assertEquals(userId, body.id);
        assertEquals("Test User", body.displayName);
    }

    @Test
    void updateUserStatusShouldReturnUpdatedStatus() {
        InternalUpdateUserStatusRequestDTO request = new InternalUpdateUserStatusRequestDTO();
        request.status = "SUSPENDED";

        when(authService.updateInternalUserStatus(userId, request)).thenReturn(suspendedUser);

        ResponseEntity<?> response = controller.updateUserStatus(userId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(userId, body.get("userId"));
        assertEquals("SUSPENDED", body.get("status"));
    }

    @Test
    void updateUserRolesShouldReturnUpdatedRoles() {
        InternalUpdateUserRolesRequestDTO request = new InternalUpdateUserRolesRequestDTO();
        request.roles = List.of("ADMIN");

        User adminUser = new User();
        adminUser.setId(userId);
        adminUser.setEmail("admin@mail.com");
        adminUser.setDisplayName("Admin User");
        adminUser.setRolesList(List.of("ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);

        when(authService.updateInternalUserRoles(userId, request)).thenReturn(adminUser);

        ResponseEntity<?> response = controller.updateUserRoles(userId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(userId, body.get("userId"));
        assertEquals(List.of("ADMIN"), body.get("roles"));
    }

    @Test
    void deleteUserShouldReturnSuccessMessage() {
        doNothing().when(authService).deleteInternalUser(userId);

        ResponseEntity<?> response = controller.deleteUser(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("User berhasil dihapus.", body.get("message"));

        verify(authService).deleteInternalUser(userId);
    }
}
