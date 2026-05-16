package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.AdminUserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UpdateUserRolesRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UpdateUserStatusRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/admin/users", "/api/admin/users"})
public class AdminUserController {

    private final AuthService authService;

    public AdminUserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponseDTO>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size,
                                                                @RequestParam(required = false) String search,
                                                                @RequestParam(required = false) String role,
                                                                @RequestParam(required = false) String status) {
        return ResponseEntity.ok(authService.adminListUserEntities(search, role, status, page, size)
                .stream()
                .map(AdminUserResponseDTO::from)
                .toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponseDTO> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(AdminUserResponseDTO.from(authService.getUserById(userId)));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponseDTO> updateStatus(@PathVariable UUID userId,
                                                             @Valid @RequestBody UpdateUserStatusRequestDTO req) {
        authService.adminUpdateUserStatus(userId, req.status, req.reason);
        return ResponseEntity.ok(AdminUserResponseDTO.from(authService.getUserById(userId)));
    }

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<AdminUserResponseDTO> suspendUser(@PathVariable UUID userId,
                                                            @RequestBody(required = false) UpdateUserStatusRequestDTO req) {
        String reason = req != null ? req.reason : null;
        authService.adminUpdateUserStatus(userId, "SUSPENDED", reason);
        return ResponseEntity.ok(AdminUserResponseDTO.from(authService.getUserById(userId)));
    }

    @PatchMapping("/{userId}/unsuspend")
    public ResponseEntity<AdminUserResponseDTO> unsuspendUser(@PathVariable UUID userId) {
        authService.adminUpdateUserStatus(userId, "ACTIVE", null);
        return ResponseEntity.ok(AdminUserResponseDTO.from(authService.getUserById(userId)));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<AdminUserResponseDTO> updateRoles(@PathVariable UUID userId,
                                                            @Valid @RequestBody UpdateUserRolesRequestDTO req) {
        authService.adminUpdateUserRoles(userId, req.roles);
        return ResponseEntity.ok(AdminUserResponseDTO.from(authService.getUserById(userId)));
    }
}
