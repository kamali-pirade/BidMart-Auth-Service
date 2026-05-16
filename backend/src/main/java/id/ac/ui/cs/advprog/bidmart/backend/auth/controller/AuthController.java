package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RefreshRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RegisterRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorConfirmRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorDisableRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorSetupRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorVerifyRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.VerifyEmailRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO req) {
        UserResponseDTO response = auth.registerAndReturn(req);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        auth.verifyEmail(token);
        return ResponseEntity.ok("Email berhasil diverifikasi. Silakan login.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDTO req) {
        auth.verifyEmail(req.token);
        return ResponseEntity.ok(Map.of("message", "Email berhasil diverifikasi."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> req) {
        auth.forgotPassword(req.get("email"));
        return ResponseEntity.ok(Map.of("message", "Tautan reset kata sandi telah dikirim jika email terdaftar."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> req) {
        auth.resetPassword(req.get("token"), req.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Kata sandi berhasil diperbarui."));
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<Map<String, Boolean>> validateResetPasswordToken(@RequestParam("token") String token) {
        auth.validateResetToken(token);
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO req, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(auth.loginWithDesign(req, servletRequest));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyRequestDTO req,
                                             HttpServletRequest servletRequest) {
        return ResponseEntity.ok(auth.verifyTwoFactor(req, servletRequest));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactor(@Valid @RequestBody TwoFactorSetupRequestDTO req, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(auth.setupTwoFactor(user, req.method));
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<Map<String, String>> confirmTwoFactor(@Valid @RequestBody TwoFactorConfirmRequestDTO req,
                                                                 Authentication authentication) {
        User user = getCurrentUser(authentication);
        auth.confirmTwoFactor(user, req.code);
        return ResponseEntity.ok(Map.of("message", "Autentikasi dua faktor berhasil diaktifkan."));
    }

    @DeleteMapping("/2fa")
    public ResponseEntity<Map<String, String>> disableTwoFactor(@Valid @RequestBody TwoFactorDisableRequestDTO req,
                                                                 Authentication authentication) {
        User user = getCurrentUser(authentication);
        auth.disableTwoFactor(user, req.password);
        return ResponseEntity.ok(Map.of("message", "Autentikasi dua faktor berhasil dinonaktifkan."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(auth.refresh(req.refreshToken));
    }

    @PostMapping("/refresh-v2")
    public ResponseEntity<?> refreshV2(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(auth.refreshWithDesign(req.refreshToken));
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest req) {
        if (req != null && req.refreshToken != null && !req.refreshToken.isBlank()) {
            auth.logout(req.refreshToken);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        @SuppressWarnings("unchecked")
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();
        UUID userId = (UUID) principal.get("userId");
        Object sid = principal.get("sessionId");
        UUID sessionId = sid == null || sid.toString().isBlank() ? null : UUID.fromString(sid.toString());
        User user = auth.validateCurrentSession(userId, sessionId);

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "userId", user.getId(),
                "email", user.getEmail(),
                "roles", user.getRolesList(),
                "status", user.getStatus().name()
        ));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("Tidak terautentikasi.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> principal = (Map<String, Object>) authentication.getPrincipal();

        Object email = principal.get("email");
        if (email == null) {
            throw new IllegalArgumentException("Tidak terautentikasi.");
        }

        return auth.getUserByEmail(email.toString());
    }

    // Backward-compatible methods for existing tests and legacy call sites.
    public ResponseEntity<Void> register(RegisterRequest req) {
        auth.register(req.email, req.password, req.displayName);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<AuthResponse> login(LoginRequest req) {
        return ResponseEntity.ok(auth.login(req.email, req.password));
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Void> validateUser(@PathVariable("userId") java.util.UUID userId) {
        auth.validateUser(userId);
        return ResponseEntity.ok().build();
    }
}
