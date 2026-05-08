package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginSuccessResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RefreshRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RegisterRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorConfirmRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorDisableRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorSetupRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorSetupResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorVerifyRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.VerifyEmailRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register() {
        RegisterRequest req = new RegisterRequest();
        req.email = "test@example.com";
        req.password = "pass";
        req.displayName = "Test";

        doNothing().when(authService).register(req.email, req.password, req.displayName);

        ResponseEntity<Void> res = authController.register(req);
        assertEquals(200, res.getStatusCode().value());
        verify(authService).register("test@example.com", "pass", "Test");
    }

    @Test
    void verify_email() {
        doNothing().when(authService).verifyEmail("token");

        ResponseEntity<String> res = authController.verify("token");
        assertEquals(200, res.getStatusCode().value());
        assertNotNull(res.getBody());
        verify(authService).verifyEmail("token");
    }

    @Test
    void login() {
        LoginRequest req = new LoginRequest();
        req.email = "test@example.com";
        req.password = "pass";

        AuthResponse authRes = new AuthResponse("access", "refresh");
        when(authService.login(req.email, req.password)).thenReturn(authRes);

        ResponseEntity<AuthResponse> res = authController.login(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("access", res.getBody().accessToken);
        verify(authService).login("test@example.com", "pass");
    }

    @Test
    void refresh() {
        RefreshRequest req = new RefreshRequest();
        req.refreshToken = "token";

        AuthResponse authRes = new AuthResponse("access", "refresh");
        when(authService.refresh("token")).thenReturn(authRes);

        ResponseEntity<AuthResponse> res = authController.refresh(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("access", res.getBody().accessToken);
        verify(authService).refresh("token");
    }

    @Test
    void logout() {
        RefreshRequest req = new RefreshRequest();
        req.refreshToken = "token";

        doNothing().when(authService).logout("token");

        ResponseEntity<Void> res = authController.logout(req);
        assertEquals(200, res.getStatusCode().value());
        verify(authService).logout("token");
    }

    @Test
    void registerWithDesign_verifyEmail_loginV2_refreshV2AndValidateUser() {
        RegisterRequestDTO register = new RegisterRequestDTO();
        register.email = "test@example.com";
        register.password = "password123";
        register.displayName = "Test";
        UserResponseDTO user = new UserResponseDTO(UUID.randomUUID(), register.email, "Test", false, Instant.now(), List.of("BUYER"));
        when(authService.registerAndReturn(register)).thenReturn(user);

        ResponseEntity<UserResponseDTO> registerRes = authController.register(register);
        assertEquals(201, registerRes.getStatusCode().value());
        assertEquals(user, registerRes.getBody());

        VerifyEmailRequestDTO verifyEmail = new VerifyEmailRequestDTO();
        verifyEmail.token = "token";
        ResponseEntity<Map<String, String>> verifyRes = authController.verifyEmail(verifyEmail);
        assertEquals("Email berhasil diverifikasi.", verifyRes.getBody().get("message"));

        LoginRequestDTO login = new LoginRequestDTO();
        login.email = "test@example.com";
        login.password = "password123";
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(authService.loginWithDesign(login, servletRequest)).thenReturn(Map.of("ok", true));
        assertEquals(200, authController.login(login, servletRequest).getStatusCode().value());

        RefreshRequest refresh = new RefreshRequest();
        refresh.refreshToken = "refresh";
        when(authService.refreshWithDesign("refresh")).thenReturn(new LoginSuccessResponseDTO("access", "refresh", 3600, user));
        assertEquals(200, authController.refreshV2(refresh).getStatusCode().value());

        UUID userId = UUID.randomUUID();
        assertEquals(200, authController.validateUser(userId).getStatusCode().value());
        verify(authService).validateUser(userId);
    }

    @Test
    void twoFactorEndpointsUseAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        when(authentication.getPrincipal()).thenReturn(Map.of("email", "test@example.com"));
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        TwoFactorSetupRequestDTO setup = new TwoFactorSetupRequestDTO();
        setup.method = "TOTP";
        when(authService.setupTwoFactor(user, "TOTP")).thenReturn(new TwoFactorSetupResponseDTO("secret", null, List.of("code")));
        assertEquals(200, authController.setupTwoFactor(setup, authentication).getStatusCode().value());

        TwoFactorConfirmRequestDTO confirm = new TwoFactorConfirmRequestDTO();
        confirm.code = "123456";
        assertEquals("Autentikasi dua faktor berhasil diaktifkan.", authController.confirmTwoFactor(confirm, authentication).getBody().get("message"));

        TwoFactorDisableRequestDTO disable = new TwoFactorDisableRequestDTO();
        disable.password = "password";
        assertEquals("Autentikasi dua faktor berhasil dinonaktifkan.", authController.disableTwoFactor(disable, authentication).getBody().get("message"));
    }

    @Test
    void twoFactorVerifyAndLogoutOptionalBranches() {
        TwoFactorVerifyRequestDTO verify = new TwoFactorVerifyRequestDTO();
        verify.partialToken = "partial";
        verify.method = "TOTP";
        verify.code = "123456";
        HttpServletRequest request = mock(HttpServletRequest.class);
        UserResponseDTO user = new UserResponseDTO(UUID.randomUUID(), "test@example.com", "Test", true, Instant.now(), List.of("BUYER"));
        when(authService.verifyTwoFactor(verify, request)).thenReturn(new LoginSuccessResponseDTO("access", "refresh", 3600, user));
        assertEquals(200, authController.verifyTwoFactor(verify, request).getStatusCode().value());

        assertEquals(200, authController.logout(null).getStatusCode().value());
        RefreshRequest nullToken = new RefreshRequest();
        assertEquals(200, authController.logout(nullToken).getStatusCode().value());
        RefreshRequest blank = new RefreshRequest();
        blank.refreshToken = " ";
        assertEquals(200, authController.logout(blank).getStatusCode().value());
    }

    @Test
    void forgotPasswordAndResetPasswordReturnMessages() {
        ResponseEntity<Map<String, String>> forgot = authController.forgotPassword(Map.of("email", "test@example.com"));
        assertEquals(200, forgot.getStatusCode().value());
        assertEquals("Tautan reset kata sandi telah dikirim jika email terdaftar.", forgot.getBody().get("message"));
        verify(authService).forgotPassword("test@example.com");

        ResponseEntity<Map<String, String>> reset = authController.resetPassword(Map.of(
                "token", "token",
                "newPassword", "new-password"
        ));
        assertEquals(200, reset.getStatusCode().value());
        assertEquals("Kata sandi berhasil diperbarui.", reset.getBody().get("message"));
        verify(authService).resetPassword("token", "new-password");
    }

    @Test
    void currentUserRejectsMissingAuthenticationOrEmail() {
        TwoFactorSetupRequestDTO setup = new TwoFactorSetupRequestDTO();
        setup.method = "TOTP";
        assertEquals("Tidak terautentikasi.", org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authController.setupTwoFactor(setup, null)
        ).getMessage());

        Authentication noPrincipal = mock(Authentication.class);
        when(noPrincipal.getPrincipal()).thenReturn(null);
        assertEquals("Tidak terautentikasi.", org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authController.setupTwoFactor(setup, noPrincipal)
        ).getMessage());

        Authentication noEmail = mock(Authentication.class);
        when(noEmail.getPrincipal()).thenReturn(Map.of("userId", "1"));
        assertEquals("Tidak terautentikasi.", org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authController.setupTwoFactor(setup, noEmail)
        ).getMessage());
    }
}
