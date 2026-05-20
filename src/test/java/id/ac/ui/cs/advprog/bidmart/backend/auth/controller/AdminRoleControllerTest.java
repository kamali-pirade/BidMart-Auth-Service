package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RoleRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RoleResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRoleControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminRoleController adminRoleController;

    private UUID testRoleId;
    private RoleResponseDTO testRoleResponse;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testRoleResponse = new RoleResponseDTO(
                testRoleId,
                "ROLE_USER",
                Arrays.asList("read:profile", "write:profile")
        );
    }

    @Test
    void testListRolesSuccess() {
        List<RoleResponseDTO> roles = Arrays.asList(
                testRoleResponse,
                new RoleResponseDTO(
                        UUID.randomUUID(),
                        "ROLE_ADMIN",
                        Arrays.asList("read:users", "write:users", "read:roles", "write:roles")
                )
        );
        when(authService.adminListRoles()).thenReturn(roles);

        ResponseEntity<List<RoleResponseDTO>> response = adminRoleController.listRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(authService).adminListRoles();
    }

    @Test
    void testListRolesEmpty() {
        when(authService.adminListRoles()).thenReturn(Arrays.asList());

        ResponseEntity<List<RoleResponseDTO>> response = adminRoleController.listRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void testListRolesSingleRole() {
        List<RoleResponseDTO> roles = Arrays.asList(testRoleResponse);
        when(authService.adminListRoles()).thenReturn(roles);

        ResponseEntity<List<RoleResponseDTO>> response = adminRoleController.listRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("ROLE_USER", response.getBody().get(0).name);
    }

    @Test
    void testCreateRoleSuccess() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_MODERATOR";
        request.permissions = Arrays.asList("read:users", "moderate:content");

        RoleResponseDTO createdRole = new RoleResponseDTO(
                UUID.randomUUID(),
                "ROLE_MODERATOR",
                Arrays.asList("read:users", "moderate:content")
        );
        when(authService.adminCreateRole(any(RoleRequestDTO.class))).thenReturn(createdRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.createRole(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ROLE_MODERATOR", response.getBody().name);
        assertEquals(2, response.getBody().permissions.size());
    }

    @Test
    void testCreateRoleWithSinglePermission() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_GUEST";
        request.permissions = Arrays.asList("read:public");

        RoleResponseDTO createdRole = new RoleResponseDTO(
                UUID.randomUUID(),
                "ROLE_GUEST",
                Arrays.asList("read:public")
        );
        when(authService.adminCreateRole(any(RoleRequestDTO.class))).thenReturn(createdRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.createRole(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().permissions.size());
    }

    @Test
    void testCreateRoleWithManyPermissions() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_SUPERADMIN";
        request.permissions = Arrays.asList(
                "read:all",
                "write:all",
                "delete:all",
                "manage:users",
                "manage:roles",
                "manage:system"
        );

        RoleResponseDTO createdRole = new RoleResponseDTO(
                UUID.randomUUID(),
                "ROLE_SUPERADMIN",
                request.permissions
        );
        when(authService.adminCreateRole(any(RoleRequestDTO.class))).thenReturn(createdRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.createRole(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(6, response.getBody().permissions.size());
    }

    @Test
    void testUpdateRoleSuccess() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_USER_UPDATED";
        request.permissions = Arrays.asList("read:profile", "write:profile", "delete:profile");

        RoleResponseDTO updatedRole = new RoleResponseDTO(
                testRoleId,
                "ROLE_USER_UPDATED",
                request.permissions
        );
        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenReturn(updatedRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.updateRole(testRoleId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROLE_USER_UPDATED", response.getBody().name);
        assertEquals(3, response.getBody().permissions.size());
        verify(authService).adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class));
    }

    @Test
    void testUpdateRoleWithReducedPermissions() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_USER";
        request.permissions = Arrays.asList("read:profile");

        RoleResponseDTO updatedRole = new RoleResponseDTO(
                testRoleId,
                "ROLE_USER",
                request.permissions
        );
        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenReturn(updatedRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.updateRole(testRoleId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().permissions.size());
    }

    @Test
    void testUpdateRoleNameOnly() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_MEMBER";
        request.permissions = Arrays.asList("read:profile", "write:profile");

        RoleResponseDTO updatedRole = new RoleResponseDTO(
                testRoleId,
                "ROLE_MEMBER",
                request.permissions
        );
        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenReturn(updatedRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.updateRole(testRoleId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ROLE_MEMBER", response.getBody().name);
    }

    @Test
    void testUpdateRolePermissionsOnly() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_USER";
        request.permissions = Arrays.asList("read:profile", "write:profile", "manage:profile");

        RoleResponseDTO updatedRole = new RoleResponseDTO(
                testRoleId,
                "ROLE_USER",
                request.permissions
        );
        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenReturn(updatedRole);

        ResponseEntity<RoleResponseDTO> response = adminRoleController.updateRole(testRoleId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().permissions.size());
    }

    @Test
    void testListRolesMultipleRoles() {
        List<RoleResponseDTO> roles = Arrays.asList(
                new RoleResponseDTO(UUID.randomUUID(), "ROLE_USER", Arrays.asList("read:profile")),
                new RoleResponseDTO(UUID.randomUUID(), "ROLE_ADMIN", Arrays.asList("read:all", "write:all")),
                new RoleResponseDTO(UUID.randomUUID(), "ROLE_GUEST", Arrays.asList("read:public"))
        );
        when(authService.adminListRoles()).thenReturn(roles);

        ResponseEntity<List<RoleResponseDTO>> response = adminRoleController.listRoles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        assertEquals("ROLE_USER", response.getBody().get(0).name);
        assertEquals("ROLE_ADMIN", response.getBody().get(1).name);
        assertEquals("ROLE_GUEST", response.getBody().get(2).name);
    }

    @Test
    void testCreateRoleVerifiesServiceCall() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_TEST";
        request.permissions = Arrays.asList("test:permission");

        RoleResponseDTO createdRole = new RoleResponseDTO(
                UUID.randomUUID(),
                "ROLE_TEST",
                request.permissions
        );
        when(authService.adminCreateRole(any(RoleRequestDTO.class))).thenReturn(createdRole);

        adminRoleController.createRole(request);

        verify(authService).adminCreateRole(any(RoleRequestDTO.class));
    }

    @Test
    void testUpdateRoleVerifiesServiceCall() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_USER";
        request.permissions = Arrays.asList("read:profile");

        RoleResponseDTO updatedRole = new RoleResponseDTO(
                testRoleId,
                "ROLE_USER",
                request.permissions
        );
        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenReturn(updatedRole);

        adminRoleController.updateRole(testRoleId, request);

        verify(authService).adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class));
    }

    @Test
    void testCreateRoleNotFound() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_TEST";
        request.permissions = Arrays.asList("test:permission");

        when(authService.adminCreateRole(any(RoleRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Role already exists"));

        try {
            adminRoleController.createRole(request);
        } catch (IllegalArgumentException e) {
            assertEquals("Role already exists", e.getMessage());
        }
    }

    @Test
    void testUpdateRoleNotFound() {
        RoleRequestDTO request = new RoleRequestDTO();
        request.name = "ROLE_TEST";
        request.permissions = Arrays.asList("test:permission");

        when(authService.adminUpdateRole(eq(testRoleId), any(RoleRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Role not found"));

        try {
            adminRoleController.updateRole(testRoleId, request);
        } catch (IllegalArgumentException e) {
            assertEquals("Role not found", e.getMessage());
        }
    }
}
