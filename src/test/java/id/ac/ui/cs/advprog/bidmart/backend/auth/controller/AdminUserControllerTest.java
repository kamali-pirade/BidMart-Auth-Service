package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.AdminUserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UpdateUserRolesRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UpdateUserStatusRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.UserStatus;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminUserController adminUserController;

    private UUID testUserId;
    private User testUser;
    private UserResponseDTO testUserResponse;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setDisplayName("Test User");
        testUser.setEmailVerified(true);
        testUser.setRolesList(Arrays.asList("ROLE_USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());

        testUserResponse = new UserResponseDTO(
                testUserId,
                "test@example.com",
                "Test User",
                true,
                Instant.now(),
                Arrays.asList("ROLE_USER")
        );
    }

    @Test
    void testListUsersWithoutFilters() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities(null, null, null, 0, 20))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(0, 20, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(authService).adminListUserEntities(null, null, null, 0, 20);
    }

    @Test
    void testListUsersWithSearchFilter() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities("test", null, null, 0, 20))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(0, 20, "test", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(authService).adminListUserEntities("test", null, null, 0, 20);
    }

    @Test
    void testListUsersWithRoleFilter() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities(null, "ROLE_USER", null, 0, 20))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(0, 20, null, "ROLE_USER", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(authService).adminListUserEntities(null, "ROLE_USER", null, 0, 20);
    }

    @Test
    void testListUsersWithStatusFilter() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities(null, null, "ACTIVE", 0, 20))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(0, 20, null, null, "ACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(authService).adminListUserEntities(null, null, "ACTIVE", 0, 20);
    }

    @Test
    void testListUsersWithPagination() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities(null, null, null, 2, 50))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(2, 50, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).adminListUserEntities(null, null, null, 2, 50);
    }

    @Test
    void testListUsersEmpty() {
        when(authService.adminListUserEntities(null, null, null, 0, 20))
                .thenReturn(Arrays.asList());

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(0, 20, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testGetUserSuccess() {
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.getUser(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserResponse.email, response.getBody().email);
        assertEquals("ACTIVE", response.getBody().status);
        verify(authService).getUserById(testUserId);
    }

    @Test
    void testGetUserNotFound() {
        when(authService.getUserById(testUserId))
                .thenThrow(new IllegalArgumentException("User not found"));

        try {
            adminUserController.getUser(testUserId);
        } catch (IllegalArgumentException e) {
            assertEquals("User not found", e.getMessage());
        }

        verify(authService).getUserById(testUserId);
    }

    @Test
    void testUpdateUserStatusSuccess() {
        testUser.setStatus(UserStatus.SUSPENDED);
        when(authService.adminUpdateUserStatus(testUserId, "SUSPENDED", "Violation"))
                .thenReturn(testUserResponse);
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        UpdateUserStatusRequestDTO request = new UpdateUserStatusRequestDTO();
        request.status = "SUSPENDED";
        request.reason = "Violation";

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.updateStatus(testUserId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUSPENDED", response.getBody().status);
        assertEquals(true, response.getBody().suspended);
        verify(authService).adminUpdateUserStatus(testUserId, "SUSPENDED", "Violation");
    }

    @Test
    void testUpdateUserStatusWithoutReason() {
        when(authService.adminUpdateUserStatus(testUserId, "SUSPENDED", null))
                .thenReturn(testUserResponse);
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        UpdateUserStatusRequestDTO request = new UpdateUserStatusRequestDTO();
        request.status = "SUSPENDED";
        request.reason = null;

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.updateStatus(testUserId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).adminUpdateUserStatus(testUserId, "SUSPENDED", null);
    }

    @Test
    void testUpdateUserRolesSuccess() {
        List<String> newRoles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
        testUser.setRolesList(newRoles);
        when(authService.adminUpdateUserRoles(testUserId, newRoles))
                .thenReturn(testUserResponse);
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO();
        request.roles = newRoles;

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.updateRoles(testUserId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().roles.size());
        verify(authService).adminUpdateUserRoles(testUserId, newRoles);
    }

    @Test
    void testUpdateUserRolesSingleRole() {
        List<String> newRoles = Arrays.asList("ROLE_ADMIN");
        testUser.setRolesList(newRoles);
        when(authService.adminUpdateUserRoles(testUserId, newRoles))
                .thenReturn(testUserResponse);
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO();
        request.roles = newRoles;

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.updateRoles(testUserId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().roles.size());
        verify(authService).adminUpdateUserRoles(testUserId, newRoles);
    }

    @Test
    void testListUsersWithAllFilters() {
        List<User> users = Arrays.asList(testUser);
        when(authService.adminListUserEntities("search", "ROLE_USER", "ACTIVE", 1, 10))
                .thenReturn(users);

        ResponseEntity<List<AdminUserResponseDTO>> response = adminUserController.listUsers(1, 10, "search", "ROLE_USER", "ACTIVE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).adminListUserEntities("search", "ROLE_USER", "ACTIVE", 1, 10);
    }

    @Test
    void testGetUserReturnsCorrectData() {
        testUser.setEmail("user@domain.com");
        testUser.setDisplayName("John Doe");
        testUser.setEmailVerified(false);
        when(authService.getUserById(testUserId)).thenReturn(testUser);

        ResponseEntity<AdminUserResponseDTO> response = adminUserController.getUser(testUserId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user@domain.com", response.getBody().email);
        assertEquals("John Doe", response.getBody().displayName);
        assertEquals(false, response.getBody().emailVerified);
    }
}
