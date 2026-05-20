package id.ac.ui.cs.advprog.bidmart.backend.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthEntityCoverageTest {

    @Test
    void roleAndTokenGettersExposeAssignedValues() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail(" Entity@Example.COM ");
        user.setPasswordHash("hash");
        user.setDisplayName("Entity");
        user.setAvatarUrl("avatar");
        user.setRolesList(List.of(" buyer ", "", "seller", "BUYER"));
        user.setTwoFactorBackupCodes("one,two");
        user.preUpdate();

        assertEquals(userId, user.getId());
        assertEquals("entity@example.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("Entity", user.getDisplayName());
        assertEquals("avatar", user.getAvatarUrl());
        assertEquals("BUYER,SELLER", user.getRoles());
        assertEquals(List.of("BUYER", "SELLER"), user.getRolesList());
        assertEquals("one,two", user.getTwoFactorBackupCodes());
        assertNotNull(user.getUpdatedAt());

        user.setRoles(null);
        assertEquals(List.of(), user.getRolesList());
        user.setRoles(" ");
        assertEquals(List.of(), user.getRolesList());
        user.setRoles("BUYER, ,SELLER");
        assertEquals(List.of("BUYER", "SELLER"), user.getRolesList());

        Role role = new Role();
        UUID roleId = UUID.randomUUID();
        ReflectionTestUtils.setField(role, "id", roleId);
        role.setName("ADMIN");
        role.setPermissions("READ");
        assertEquals(roleId, role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals("READ", role.getPermissions());
        assertNotNull(role.getCreatedAt());

        EmailVerificationToken verification = new EmailVerificationToken();
        UUID verificationId = UUID.randomUUID();
        ReflectionTestUtils.setField(verification, "id", verificationId);
        verification.setUser(user);
        verification.setToken("token");
        verification.setExpiresAt(Instant.now());
        verification.setUsedAt(Instant.now());
        assertEquals(verificationId, verification.getId());
        assertEquals(user, verification.getUser());
        assertEquals("token", verification.getToken());
        assertNotNull(verification.getCreatedAt());
        assertNotNull(verification.getUsedAt());

        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken("refresh");
        refresh.setExpiresAt(Instant.now());
        refresh.setRevoked(true);
        refresh.setDevice("device");
        refresh.setIpAddress("ip");
        refresh.setLastActive(Instant.now());
        assertEquals(user, refresh.getUser());
        assertEquals("refresh", refresh.getToken());
        assertTrue(refresh.isRevoked());
        assertEquals("device", refresh.getDevice());
        assertEquals("ip", refresh.getIpAddress());
        assertNotNull(refresh.getCreatedAt());
    }

    @Test
    void partialAuthSessionGettersExposeAssignedValues() {
        PartialAuthSession session = new PartialAuthSession();
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(session, "id", id);
        User user = new User();
        Instant now = Instant.now();
        session.setUser(user);
        session.setPartialToken("partial");
        session.setMethods("TOTP");
        session.setExpiresAt(now);
        session.setUsed(true);
        session.setEmailOtpHash("hash");
        session.setEmailOtpExpiresAt(now);

        assertEquals(id, session.getId());
        assertEquals(user, session.getUser());
        assertEquals("partial", session.getPartialToken());
        assertEquals("TOTP", session.getMethods());
        assertEquals(now, session.getExpiresAt());
        assertTrue(session.isUsed());
        assertEquals("hash", session.getEmailOtpHash());
        assertEquals(now, session.getEmailOtpExpiresAt());
    }
}
