package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalCreateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRolesRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserStatusRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.UserStatus;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final AuthService authService;

    public InternalUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<InternalUserResponseDTO>> getAllUsers() {
        List<InternalUserResponseDTO> users = authService.getAllUsers().stream()
                .map(this::toInternalUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<InternalUserResponseDTO> createUser(@RequestBody InternalCreateUserRequestDTO req) {
        User user = authService.createInternalUser(req);
        return ResponseEntity.status(201).body(toInternalUserResponse(user));
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Map<String, Object>> validateUser(@PathVariable UUID userId) {
        User user = authService.getUserById(userId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("valid", user.getStatus() != UserStatus.SUSPENDED);
        body.put("userId", user.getId());
        body.put("status", safeStatus(user));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            body.put("message", "User ini telah di-suspend.");
            return ResponseEntity.status(403).body(body);
        }

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<InternalUserResponseDTO> getUserById(@PathVariable UUID userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(toInternalUserResponse(user));
    }

    @GetMapping("/by-email")
    public ResponseEntity<InternalUserResponseDTO> getUserByEmail(@RequestParam String email) {
        User user = authService.getUserByEmail(email);
        return ResponseEntity.ok(toInternalUserResponse(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<InternalUserResponseDTO> updateUser(@PathVariable UUID userId,
                                                              @RequestBody InternalUpdateUserRequestDTO req) {
        User user = authService.updateInternalUser(userId, req);
        return ResponseEntity.ok(toInternalUserResponse(user));
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<Map<String, Object>> getUserRoles(@PathVariable UUID userId) {
        User user = authService.getUserById(userId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", user.getId());
        body.put("roles", safeRoles(user));
        body.put("permissions", authService.getEffectivePermissions(user));

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{userId}/roles")
    public ResponseEntity<Map<String, Object>> updateUserRoles(@PathVariable UUID userId,
                                                               @RequestBody InternalUpdateUserRolesRequestDTO req) {
        User user = authService.updateInternalUserRoles(userId, req);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "roles", safeRoles(user),
                "permissions", authService.getEffectivePermissions(user)
        ));
    }

    @GetMapping("/{userId}/permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@PathVariable UUID userId) {
        User user = authService.getUserById(userId);
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "directPermissions", user.getPermissionsList(),
                "effectivePermissions", authService.getEffectivePermissions(user)
        ));
    }

    @GetMapping("/{userId}/permissions/{permission}/validate")
    public ResponseEntity<Map<String, Object>> validateUserPermission(@PathVariable UUID userId,
                                                                      @PathVariable String permission) {
        boolean allowed = authService.userHasPermission(userId, permission);
        return ResponseEntity.status(allowed ? 200 : 403).body(Map.of(
                "valid", allowed,
                "userId", userId,
                "permission", permission
        ));
    }

    @PostMapping("/{userId}/permissions/validate")
    public ResponseEntity<Map<String, Object>> validateUserPermissionPost(@PathVariable UUID userId,
                                                                          @RequestBody Map<String, String> req) {
        String permission = req.get("permission");
        boolean allowed = authService.userHasPermission(userId, permission);
        return ResponseEntity.status(allowed ? 200 : 403).body(Map.of(
                "valid", allowed,
                "userId", userId,
                "permission", permission
        ));
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable UUID userId) {
        User user = authService.getUserById(userId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", user.getId());
        body.put("status", safeStatus(user));
        body.put("valid", user.getStatus() != UserStatus.SUSPENDED);

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(@PathVariable UUID userId,
                                                                @RequestBody InternalUpdateUserStatusRequestDTO req) {
        User user = authService.updateInternalUserStatus(userId, req);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "status", safeStatus(user)
        ));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId) {
        authService.deleteInternalUser(userId);
        return ResponseEntity.ok(Map.of("message", "User berhasil dihapus."));
    }

    private InternalUserResponseDTO toInternalUserResponse(User user) {
        return new InternalUserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName() != null ? user.getDisplayName() : "Pengguna Baru",
                user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
                user.isEmailVerified(),
                safeRoles(user),
                authService.getEffectivePermissions(user),
                safeStatus(user),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private List<String> safeRoles(User user) {
        return user.getRolesList() != null ? user.getRolesList() : List.of();
    }

    private String safeStatus(User user) {
        return user.getStatus() != null ? user.getStatus().name() : "ACTIVE";
    }
}
