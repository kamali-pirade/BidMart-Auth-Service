package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleResponseDTO Test")
class RoleResponseDTOTest {

    @Test
    @DisplayName("Should create RoleResponseDTO with valid parameters")
    void testRoleResponseDTOCreation() {
        UUID roleId = UUID.randomUUID();
        List<String> permissions = Arrays.asList("READ", "WRITE");

        RoleResponseDTO dto = new RoleResponseDTO(roleId, "ADMIN", permissions);

        assertNotNull(dto);
        assertEquals(roleId, dto.id);
        assertEquals("ADMIN", dto.name);
        assertEquals(permissions, dto.permissions);
    }

    @Test
    @DisplayName("Should get roleId")
    void testGetId() {
        UUID roleId = UUID.randomUUID();
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "USER", Arrays.asList("READ"));

        assertEquals(roleId, dto.id);
    }

    @Test
    @DisplayName("Should get role name")
    void testGetName() {
        UUID roleId = UUID.randomUUID();
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "SELLER", Arrays.asList());

        assertEquals("SELLER", dto.name);
    }

    @Test
    @DisplayName("Should get permissions list")
    void testGetPermissions() {
        UUID roleId = UUID.randomUUID();
        List<String> permissions = Arrays.asList("CREATE", "UPDATE", "DELETE");
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "ADMIN", permissions);

        assertEquals(permissions, dto.permissions);
        assertEquals(3, dto.permissions.size());
    }

    @Test
    @DisplayName("Should handle empty permissions list")
    void testEmptyPermissions() {
        UUID roleId = UUID.randomUUID();
        List<String> permissions = Arrays.asList();
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "GUEST", permissions);

        assertTrue(dto.permissions.isEmpty());
    }

    @Test
    @DisplayName("Should handle single permission")
    void testSinglePermission() {
        UUID roleId = UUID.randomUUID();
        List<String> permissions = Arrays.asList("READ");
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "VIEWER", permissions);

        assertEquals(1, dto.permissions.size());
        assertEquals("READ", dto.permissions.get(0));
    }

    @Test
    @DisplayName("Should set and modify properties after creation")
    void testModifyAfterCreation() {
        UUID roleId = UUID.randomUUID();
        UUID newRoleId = UUID.randomUUID();
        RoleResponseDTO dto = new RoleResponseDTO(roleId, "ADMIN", Arrays.asList("READ"));

        dto.id = newRoleId;
        dto.name = "SUPER_ADMIN";
        dto.permissions = Arrays.asList("READ", "WRITE", "DELETE");

        assertEquals(newRoleId, dto.id);
        assertEquals("SUPER_ADMIN", dto.name);
        assertEquals(3, dto.permissions.size());
    }

    @Test
    @DisplayName("Should handle different role names")
    void testDifferentRoleNames() {
        UUID id = UUID.randomUUID();
        RoleResponseDTO adminRole = new RoleResponseDTO(id, "ADMIN", Arrays.asList());
        RoleResponseDTO userRole = new RoleResponseDTO(id, "USER", Arrays.asList());
        RoleResponseDTO sellerRole = new RoleResponseDTO(id, "SELLER", Arrays.asList());

        assertEquals("ADMIN", adminRole.name);
        assertEquals("USER", userRole.name);
        assertEquals("SELLER", sellerRole.name);
    }

    @Test
    @DisplayName("Should preserve permission order")
    void testPreservePermissionOrder() {
        UUID id = UUID.randomUUID();
        List<String> permissions = Arrays.asList("WRITE", "DELETE", "READ", "CREATE");
        RoleResponseDTO dto = new RoleResponseDTO(id, "ADMIN", permissions);

        assertEquals("WRITE", dto.permissions.get(0));
        assertEquals("DELETE", dto.permissions.get(1));
        assertEquals("READ", dto.permissions.get(2));
        assertEquals("CREATE", dto.permissions.get(3));
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        RoleResponseDTO dto = new RoleResponseDTO(null, null, null);

        assertNull(dto.id);
        assertNull(dto.name);
        assertNull(dto.permissions);
    }
}
