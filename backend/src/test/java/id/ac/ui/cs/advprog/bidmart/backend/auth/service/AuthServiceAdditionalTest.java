package id.ac.ui.cs.advprog.bidmart.backend.auth.service;

import id.ac.ui.cs.advprog.bidmart.backend.auth.config.AppProperties;
import id.ac.ui.cs.advprog.bidmart.backend.auth.config.AuthProperties;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.ChangePasswordRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalCreateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserRolesRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.InternalUpdateUserStatusRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.LoginSuccessResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.PartialLoginResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RegisterRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.RoleRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.SessionResponseDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.dto.TwoFactorVerifyRequestDTO;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.PartialAuthSession;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.RefreshToken;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.Role;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.UserStatus;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.EmailVerificationTokenRepository;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.PartialAuthSessionRepository;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.PasswordResetTokenRepository;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.RefreshTokenRepository;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.RoleRepository;
import id.ac.ui.cs.advprog.bidmart.backend.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceAdditionalTest {

    @Mock
    private UserRepository users;
    @Mock
    private RefreshTokenRepository refreshTokens;
    @Mock
    private RoleRepository roles;
    @Mock
    private EmailVerificationTokenRepository verificationTokens;
    @Mock
    private PasswordResetTokenRepository resetTokens;
    @Mock
    private PartialAuthSessionRepository partialAuthSessions;
    @Mock
    private EmailService emailService;
    @Mock
    private TotpService totpService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AuthService authService;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        AuthProperties authProps = new AuthProperties();
        authProps.setSecret("my-super-secret-key-that-is-at-least-32-bytes");
        authProps.setAccessTokenExpiration(3_600_000L);
        authProps.setRefreshTokenExpiration(7_200_000L);

        AppProperties appProps = new AppProperties();
        appProps.setBaseUrl("http://localhost:8080");
        appProps.setFrontendUrl("   ");

        authService = new AuthService(
                users,
                refreshTokens,
                roles,
                verificationTokens,
                resetTokens,
                partialAuthSessions,
                authProps,
                appProps,
                emailService,
                totpService,
                eventPublisher
        );
        encoder = new BCryptPasswordEncoder();

        doAnswer(invocation -> invocation.getArgument(0)).when(users).save(any(User.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(refreshTokens).save(any(RefreshToken.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(roles).save(any(Role.class));
        doAnswer(invocation -> invocation.getArgument(0)).when(partialAuthSessions).save(any(PartialAuthSession.class));
        when(refreshTokens.findByUserAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtAsc(any(User.class), any(Instant.class)))
                .thenReturn(List.of());
    }

    @Test
    void registerAndReturn_NormalizesEmailAndUsesFallbackFrontendUrl() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.email = "  New@Example.COM ";
        req.password = "password123";
        req.displayName = "New User";
        when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());

        var response = authService.registerAndReturn(req);

        assertEquals("new@example.com", response.email);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(users).save(userCaptor.capture());
        assertEquals(List.of("BUYER", "SELLER"), userCaptor.getValue().getRolesList());
        verify(emailService).sendVerificationEmail(eq("new@example.com"), org.mockito.ArgumentMatchers.startsWith("http://localhost:3000/auth/verify?token="));
    }

    @Test
    void loginWithDesign_CoversFailuresPartialSessionAndRequestMetadata() {
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.loginWithDesign(login("missing@example.com", "secret"), null));

        User suspended = user("suspended@example.com", "secret", true);
        suspended.setStatus(UserStatus.SUSPENDED);
        when(users.findByEmail("suspended@example.com")).thenReturn(Optional.of(suspended));
        LoginRequestDTO suspendedReq = login("suspended@example.com", "secret");
        assertThrows(IllegalStateException.class, () -> authService.loginWithDesign(suspendedReq, null));

        User twoFactorUser = user("two@example.com", "secret", true);
        twoFactorUser.setTwoFactorEnabled(true);
        when(users.findByEmail("two@example.com")).thenReturn(Optional.of(twoFactorUser));
        Object partial = authService.loginWithDesign(login("two@example.com", "secret"), null);
        assertInstanceOf(PartialLoginResponseDTO.class, partial);
        assertThrows(IllegalStateException.class, () -> authService.login("two@example.com", "secret"));

        User normal = user("normal@example.com", "secret", true);
        when(users.findByEmail("normal@example.com")).thenReturn(Optional.of(normal));
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("A".repeat(181));
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.1");
        LoginSuccessResponseDTO success = (LoginSuccessResponseDTO) authService.loginWithDesign(login("normal@example.com", "secret"), request);
        assertEquals(3_600L, success.expiresIn);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokens, org.mockito.Mockito.atLeastOnce()).save(tokenCaptor.capture());
        RefreshToken saved = tokenCaptor.getAllValues().get(tokenCaptor.getAllValues().size() - 1);
        assertEquals(180, saved.getDevice().length());
        assertEquals("203.0.113.7", saved.getIpAddress());

        HttpServletRequest fallbackRequest = mock(HttpServletRequest.class);
        when(fallbackRequest.getHeader("User-Agent")).thenReturn(" ");
        when(fallbackRequest.getHeader("X-Forwarded-For")).thenReturn(" ");
        when(fallbackRequest.getRemoteAddr()).thenReturn("198.51.100.9");
        authService.loginWithDesign(login("normal@example.com", "secret"), fallbackRequest);
        verify(refreshTokens, org.mockito.Mockito.atLeast(2)).save(tokenCaptor.capture());
        RefreshToken fallback = tokenCaptor.getAllValues().get(tokenCaptor.getAllValues().size() - 1);
        assertEquals("Unknown device", fallback.getDevice());
        assertEquals("198.51.100.9", fallback.getIpAddress());

        HttpServletRequest shortRequest = mock(HttpServletRequest.class);
        when(shortRequest.getHeader("User-Agent")).thenReturn("Short UA");
        when(shortRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(shortRequest.getRemoteAddr()).thenReturn("198.51.100.10");
        authService.loginWithDesign(login("normal@example.com", "secret"), shortRequest);
        verify(refreshTokens, org.mockito.Mockito.atLeast(3)).save(tokenCaptor.capture());
        RefreshToken shortToken = tokenCaptor.getAllValues().get(tokenCaptor.getAllValues().size() - 1);
        assertEquals("Short UA", shortToken.getDevice());
        assertEquals("198.51.100.10", shortToken.getIpAddress());

        HttpServletRequest nullUserAgentRequest = mock(HttpServletRequest.class);
        when(nullUserAgentRequest.getHeader("User-Agent")).thenReturn(null);
        when(nullUserAgentRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(nullUserAgentRequest.getRemoteAddr()).thenReturn("198.51.100.11");
        authService.loginWithDesign(login("normal@example.com", "secret"), nullUserAgentRequest);
    }

    @Test
    void verifyTwoFactor_CoversEveryValidationBranchAndSuccess() {
        when(partialAuthSessions.findByPartialToken("missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.verifyTwoFactor(twoFactorRequest("missing", "TOTP", "123456"), null));

        PartialAuthSession used = partialSession(user("used@example.com", "secret", true));
        used.setUsed(true);
        when(partialAuthSessions.findByPartialToken("used")).thenReturn(Optional.of(used));
        assertThrows(IllegalArgumentException.class, () -> authService.verifyTwoFactor(twoFactorRequest("used", "TOTP", "123456"), null));

        PartialAuthSession expired = partialSession(user("expired@example.com", "secret", true));
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        when(partialAuthSessions.findByPartialToken("expired")).thenReturn(Optional.of(expired));
        assertThrows(IllegalArgumentException.class, () -> authService.verifyTwoFactor(twoFactorRequest("expired", "TOTP", "123456"), null));

        PartialAuthSession unsupported = partialSession(user("unsupported@example.com", "secret", true));
        when(partialAuthSessions.findByPartialToken("unsupported")).thenReturn(Optional.of(unsupported));
        assertThrows(IllegalArgumentException.class, () -> authService.verifyTwoFactor(twoFactorRequest("unsupported", "email", "123456"), null));

        PartialAuthSession invalid = partialSession(user("invalid@example.com", "secret", true));
        when(partialAuthSessions.findByPartialToken("invalid")).thenReturn(Optional.of(invalid));
        when(totpService.verifyCode("SECRET", "000000")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.verifyTwoFactor(twoFactorRequest("invalid", " totp ", "000000"), null));

        PartialAuthSession valid = partialSession(user("valid@example.com", "secret", true));
        when(partialAuthSessions.findByPartialToken("valid")).thenReturn(Optional.of(valid));
        when(totpService.verifyCode("SECRET", "123456")).thenReturn(true);
        LoginSuccessResponseDTO response = authService.verifyTwoFactor(twoFactorRequest("valid", "totp", "123456"), null);
        assertNotNull(response.accessToken);
        assertTrue(valid.isUsed());
    }

    @Test
    void twoFactorSetupConfirmDisableAndPasswordChangeBranches() {
        User user = user("twofactor@example.com", "old-password", true);
        when(totpService.generateBase32Secret()).thenReturn("TEMPSECRET");
        assertThrows(IllegalArgumentException.class, () -> authService.setupTwoFactor(user, "sms"));

        var setup = authService.setupTwoFactor(user, "totp");
        assertEquals("TEMPSECRET", setup.secret);
        assertEquals(5, setup.backupCodes.size());
        assertEquals("TOTP", user.getTwoFactorMethod());

        User noSetup = user("nosetup@example.com", "old-password", true);
        assertThrows(IllegalArgumentException.class, () -> authService.confirmTwoFactor(noSetup, "123456"));
        noSetup.setTwoFactorTempSecret(" ");
        assertThrows(IllegalArgumentException.class, () -> authService.confirmTwoFactor(noSetup, "123456"));
        when(totpService.verifyCode("TEMPSECRET", "bad")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> authService.confirmTwoFactor(user, "bad"));
        when(totpService.verifyCode("TEMPSECRET", "123456")).thenReturn(true);
        authService.confirmTwoFactor(user, "123456");
        assertTrue(user.isTwoFactorEnabled());
        assertNull(user.getTwoFactorTempSecret());

        assertThrows(IllegalArgumentException.class, () -> authService.disableTwoFactor(user, "wrong"));
        authService.disableTwoFactor(user, "old-password");
        assertFalse(user.isTwoFactorEnabled());
        assertNull(user.getTwoFactorSecret());

        ChangePasswordRequestDTO bad = new ChangePasswordRequestDTO();
        bad.currentPassword = "wrong";
        bad.newPassword = "new-password";
        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(user, bad));

        ChangePasswordRequestDTO good = new ChangePasswordRequestDTO();
        good.currentPassword = "old-password";
        good.newPassword = "new-password";
        authService.changePassword(user, good);
        assertTrue(encoder.matches("new-password", user.getPasswordHash()));
    }

    @Test
    void sessionsProfileAdminAndRoleManagementBranches() {
        User user = user("admin@example.com", "secret", true);
        UUID sessionId = UUID.randomUUID();
        RefreshToken session = refreshToken(user, sessionId);
        when(refreshTokens.findByUserAndRevokedFalseOrderByCreatedAtDesc(user)).thenReturn(List.of(session));

        List<SessionResponseDTO> sessions = authService.getActiveSessions(user, sessionId.toString());
        assertTrue(sessions.get(0).current);
        assertFalse(authService.getActiveSessions(user, "other").get(0).current);
        assertFalse(authService.getActiveSessions(user, null).get(0).current);

        when(refreshTokens.findByIdAndUser(sessionId, user)).thenReturn(Optional.of(session));
        authService.revokeSession(user, sessionId);
        assertTrue(session.isRevoked());
        when(refreshTokens.findByIdAndUser(UUID.nameUUIDFromBytes("missing".getBytes()), user)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.revokeSession(user, UUID.nameUUIDFromBytes("missing".getBytes())));

        User buyer = user("buyer@example.com", "secret", true);
        buyer.setRolesList(List.of("BUYER"));
        ReflectionTestUtils.setField(buyer, "createdAt", Instant.now().minusSeconds(10));
        User seller = user("seller@example.com", "secret", true);
        seller.setRolesList(List.of("SELLER"));
        ReflectionTestUtils.setField(seller, "createdAt", Instant.now());
        when(users.searchUsers("e", UserStatus.ACTIVE)).thenReturn(List.of(buyer, seller));
        when(users.searchUsers("e", null)).thenReturn(List.of(buyer, seller));
        assertEquals(List.of("seller@example.com"), authService.adminListUsers("e", "seller", "active", 0, 1).stream().map(dto -> dto.email).toList());
        assertEquals(List.of(), authService.adminListUsers("e", "buyer", "active", 5, 10));
        assertEquals(2, authService.adminListUsers("e", null, null, 0, 10).size());
        assertEquals(2, authService.adminListUsers("e", " ", "", 0, 10).size());

        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        assertEquals(user.getEmail(), authService.adminGetUser(user.getId()).email);
        assertThrows(IllegalArgumentException.class, () -> authService.getUserById(UUID.randomUUID()));

        authService.adminUpdateUserStatus(user.getId(), "suspended", "reason");
        verify(refreshTokens).revokeAllByUser(user);
        authService.adminUpdateUserStatus(user.getId(), "active", "reason");
        assertEquals(List.of("ADMIN"), authService.adminUpdateUserRoles(user.getId(), List.of("ADMIN")).roles);

        var roleReq = new RoleRequestDTO();
        roleReq.name = " manager ";
        roleReq.permissions = List.of("READ", "WRITE");
        when(roles.findAll()).thenReturn(List.of(role("ADMIN", "ALL"), role("EMPTY", null), role("BLANK", " ")));
        assertEquals(3, authService.adminListRoles().size());
        when(roles.findByNameIgnoreCase(roleReq.name)).thenReturn(Optional.empty());
        assertEquals("MANAGER", authService.adminCreateRole(roleReq).name);
        when(roles.findByNameIgnoreCase(roleReq.name)).thenReturn(Optional.of(role("MANAGER", "READ")));
        assertThrows(IllegalArgumentException.class, () -> authService.adminCreateRole(roleReq));

        Role existingRole = role("OLD", "READ");
        UUID roleId = UUID.randomUUID();
        when(roles.findById(roleId)).thenReturn(Optional.of(existingRole));
        assertEquals("MANAGER", authService.adminUpdateRole(roleId, roleReq).name);
        when(roles.findById(UUID.nameUUIDFromBytes("role".getBytes()))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.adminUpdateRole(UUID.nameUUIDFromBytes("role".getBytes()), roleReq));
    }

    @Test
    void loginRevokesOldestSessionsWhenConcurrentSessionLimitWouldBeExceeded() {
        User user = user("limit@example.com", "secret", true);
        RefreshToken oldest = refreshToken(user, UUID.randomUUID());
        RefreshToken middle = refreshToken(user, UUID.randomUUID());
        RefreshToken newest = refreshToken(user, UUID.randomUUID());
        when(users.findByEmail("limit@example.com")).thenReturn(Optional.of(user));
        when(refreshTokens.findByUserAndRevokedFalseAndExpiresAtAfterOrderByCreatedAtAsc(eq(user), any(Instant.class)))
                .thenReturn(new java.util.ArrayList<>(List.of(oldest, middle, newest)));

        authService.loginWithDesign(login("limit@example.com", "secret"), null);

        assertTrue(oldest.isRevoked());
        assertFalse(middle.isRevoked());
        assertFalse(newest.isRevoked());
        verify(refreshTokens).save(oldest);
    }

    @Test
    void legacyConstructorRoleRepositoryUnavailableBranches() {
        AuthProperties authProps = new AuthProperties();
        authProps.setSecret("my-super-secret-key-that-is-at-least-32-bytes");
        authProps.setAccessTokenExpiration(1_000L);
        authProps.setRefreshTokenExpiration(1_000L);
        AuthService legacy = new AuthService(users, refreshTokens, verificationTokens, resetTokens, authProps, new AppProperties(), emailService);

        assertEquals(List.of(), legacy.adminListRoles());
        RoleRequestDTO req = new RoleRequestDTO();
        req.name = "role";
        req.permissions = List.of("READ");
        assertThrows(IllegalStateException.class, () -> legacy.adminCreateRole(req));
        assertThrows(IllegalStateException.class, () -> legacy.adminUpdateRole(UUID.randomUUID(), req));
    }

    @Test
    void internalUserManagementBranches() {
        InternalCreateUserRequestDTO create = new InternalCreateUserRequestDTO();
        create.email = " New@Example.COM ";
        create.password = "password123";
        create.displayName = "New";
        create.avatarUrl = "avatar";
        create.emailVerified = true;
        create.roles = List.of("ADMIN");
        create.status = "suspended";
        when(users.existsByEmail("new@example.com")).thenReturn(false);
        User created = authService.createInternalUser(create);
        assertEquals(UserStatus.SUSPENDED, created.getStatus());
        assertEquals(List.of("ADMIN"), created.getRolesList());

        when(users.existsByEmail("new@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.createInternalUser(create));

        InternalCreateUserRequestDTO defaults = new InternalCreateUserRequestDTO();
        defaults.email = " default@example.com ";
        defaults.password = "password123";
        defaults.displayName = "Default";
        defaults.roles = List.of();
        defaults.status = "";
        when(users.existsByEmail("default@example.com")).thenReturn(false);
        User defaultUser = authService.createInternalUser(defaults);
        assertEquals(UserStatus.ACTIVE, defaultUser.getStatus());
        assertEquals(List.of("BUYER", "SELLER"), defaultUser.getRolesList());
        assertFalse(defaultUser.isEmailVerified());

        InternalCreateUserRequestDTO nullDefaults = new InternalCreateUserRequestDTO();
        nullDefaults.email = " null-default@example.com ";
        nullDefaults.password = "password123";
        nullDefaults.displayName = "Null Default";
        when(users.existsByEmail("null-default@example.com")).thenReturn(false);
        User nullDefaultUser = authService.createInternalUser(nullDefaults);
        assertEquals(UserStatus.ACTIVE, nullDefaultUser.getStatus());
        assertEquals(List.of("BUYER", "SELLER"), nullDefaultUser.getRolesList());

        User user = user("old@example.com", "secret", true);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        InternalUpdateUserRequestDTO update = new InternalUpdateUserRequestDTO();
        update.email = " NewEmail@Example.COM ";
        update.displayName = "Updated";
        update.avatarUrl = "new-avatar";
        update.emailVerified = false;
        when(users.existsByEmail("newemail@example.com")).thenReturn(false);
        assertEquals("newemail@example.com", authService.updateInternalUser(user.getId(), update).getEmail());

        InternalUpdateUserRequestDTO duplicate = new InternalUpdateUserRequestDTO();
        duplicate.email = "taken@example.com";
        when(users.existsByEmail("taken@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.updateInternalUser(user.getId(), duplicate));

        InternalUpdateUserRequestDTO noChange = new InternalUpdateUserRequestDTO();
        noChange.email = "";
        assertDoesNotThrow(() -> authService.updateInternalUser(user.getId(), noChange));

        InternalUpdateUserRequestDTO sameEmail = new InternalUpdateUserRequestDTO();
        sameEmail.email = user.getEmail();
        assertDoesNotThrow(() -> authService.updateInternalUser(user.getId(), sameEmail));

        InternalUpdateUserRequestDTO nullFields = new InternalUpdateUserRequestDTO();
        assertDoesNotThrow(() -> authService.updateInternalUser(user.getId(), nullFields));

        InternalUpdateUserStatusRequestDTO suspended = new InternalUpdateUserStatusRequestDTO();
        suspended.status = "suspended";
        assertEquals(UserStatus.SUSPENDED, authService.updateInternalUserStatus(user.getId(), suspended).getStatus());

        InternalUpdateUserStatusRequestDTO active = new InternalUpdateUserStatusRequestDTO();
        active.status = "active";
        assertEquals(UserStatus.ACTIVE, authService.updateInternalUserStatus(user.getId(), active).getStatus());

        InternalUpdateUserRolesRequestDTO emptyRoles = new InternalUpdateUserRolesRequestDTO();
        emptyRoles.roles = List.of();
        assertThrows(IllegalArgumentException.class, () -> authService.updateInternalUserRoles(user.getId(), emptyRoles));

        InternalUpdateUserRolesRequestDTO nullRoles = new InternalUpdateUserRolesRequestDTO();
        assertThrows(IllegalArgumentException.class, () -> authService.updateInternalUserRoles(user.getId(), nullRoles));

        InternalUpdateUserRolesRequestDTO roleUpdate = new InternalUpdateUserRolesRequestDTO();
        roleUpdate.roles = List.of("ADMIN", "BUYER");
        assertEquals(List.of("ADMIN", "BUYER"), authService.updateInternalUserRoles(user.getId(), roleUpdate).getRolesList());

        authService.deleteInternalUser(user.getId());
        verify(users).delete(user);
    }

    @Test
    void updateProfileAndGetUserByEmailAndFrontendUrlBranches() {
        User user = user("profile@example.com", "secret", true);
        User updated = authService.updateProfile(user, "Updated", "avatar");
        assertEquals("Updated", updated.getDisplayName());
        assertEquals("avatar", updated.getAvatarUrl());

        when(users.findByEmail("profile@example.com")).thenReturn(Optional.of(user));
        assertEquals(user, authService.getUserByEmail(" PROFILE@example.com "));
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> authService.getUserByEmail("missing@example.com"));

        AppProperties props = (AppProperties) ReflectionTestUtils.getField(authService, "appProps");
        props.setFrontendUrl("https://frontend.example");
        when(users.findByEmail("profile@example.com")).thenReturn(Optional.of(user));
        authService.forgotPassword("profile@example.com");
        verify(emailService).sendResetPasswordEmail(eq("profile@example.com"), org.mockito.ArgumentMatchers.startsWith("https://frontend.example/auth/reset?token="));
    }

    @Test
    void userLookupAndValidationBranches() {
        User active = user("active@example.com", "secret", true);
        User suspended = user("blocked@example.com", "secret", true);
        suspended.setStatus(UserStatus.SUSPENDED);
        when(users.findById(active.getId())).thenReturn(Optional.of(active));
        when(users.findById(suspended.getId())).thenReturn(Optional.of(suspended));
        when(users.findById(UUID.nameUUIDFromBytes("missing".getBytes()))).thenReturn(Optional.empty());
        when(users.findAll()).thenReturn(List.of(active, suspended));

        assertDoesNotThrow(() -> authService.validateUser(active.getId()));
        assertThrows(IllegalStateException.class, () -> authService.validateUser(suspended.getId()));
        assertTrue(authService.isUserValid(active.getId()));
        assertFalse(authService.isUserValid(suspended.getId()));
        assertFalse(authService.isUserValid(UUID.nameUUIDFromBytes("missing".getBytes())));
        assertEquals(2, authService.getAllUsers().size());
    }

    @SuppressWarnings("deprecation")
    @Test
    void legacyPublisherMethodsAreCallable() {
        AuthProperties authProps = new AuthProperties();
        authProps.setSecret("my-super-secret-key-that-is-at-least-32-bytes");
        authProps.setAccessTokenExpiration(1_000L);
        authProps.setRefreshTokenExpiration(1_000L);
        AuthService legacy = new AuthService(users, refreshTokens, verificationTokens, resetTokens, authProps, new AppProperties(), emailService);

        Object publisher = ReflectionTestUtils.getField(legacy, "eventPublisher");
        assertNotNull(publisher);
        ((ApplicationEventPublisher) publisher).publishEvent(new Object());
        ((ApplicationEventPublisher) publisher).publishEvent(mock(ApplicationEvent.class));
    }

    private User user(String email, String password, boolean verified) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setDisplayName("User");
        user.setEmailVerified(verified);
        user.setStatus(UserStatus.ACTIVE);
        user.setRolesList(List.of("BUYER", "SELLER"));
        user.setTwoFactorSecret("SECRET");
        return user;
    }

    private LoginRequestDTO login(String email, String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.email = email;
        request.password = password;
        return request;
    }

    private TwoFactorVerifyRequestDTO twoFactorRequest(String token, String method, String code) {
        TwoFactorVerifyRequestDTO request = new TwoFactorVerifyRequestDTO();
        request.partialToken = token;
        request.method = method;
        request.code = code;
        return request;
    }

    private PartialAuthSession partialSession(User user) {
        PartialAuthSession partial = new PartialAuthSession();
        partial.setUser(user);
        partial.setPartialToken("partial");
        partial.setExpiresAt(Instant.now().plusSeconds(60));
        partial.setMethods("TOTP");
        return partial;
    }

    private RefreshToken refreshToken(User user, UUID id) {
        RefreshToken token = new RefreshToken();
        ReflectionTestUtils.setField(token, "id", id);
        token.setUser(user);
        token.setToken("refresh");
        token.setDevice("device");
        token.setIpAddress("127.0.0.1");
        token.setExpiresAt(Instant.now().plusSeconds(60));
        token.setLastActive(Instant.now());
        return token;
    }

    private Role role(String name, String permissions) {
        Role role = new Role();
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        role.setName(name);
        role.setPermissions(permissions);
        return role;
    }
}
