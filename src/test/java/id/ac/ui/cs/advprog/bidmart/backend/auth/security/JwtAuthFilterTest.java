package id.ac.ui.cs.advprog.bidmart.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import id.ac.ui.cs.advprog.bidmart.backend.auth.config.AuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private JwtAuthFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        AuthProperties props = new AuthProperties();
        props.setSecret("my-super-secret-key-that-is-at-least-32-bytes");
        props.setAccessTokenExpiration(3600000L);
        filter = new JwtAuthFilter(props);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/register");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/verify");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/verify-email");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/refresh");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/refresh-v2");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/auth/2fa/verify");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/health");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/internal/users/1");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/ws/chat");
        assertTrue(filter.shouldNotFilter(request));

        when(request.getRequestURI()).thenReturn("/api/orders");
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_NoHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidJwt() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidJwt() throws Exception {
        AuthProperties props = new AuthProperties();
        props.setSecret("my-super-secret-key-that-is-at-least-32-bytes");
        props.setAccessTokenExpiration(3600000L);
        JwtService svc = new JwtService(props);
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = svc.generateAccessToken(userId, "email@email.com", sessionId, Arrays.asList("ADMIN", null, " "));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(SecurityContextHolder.getContext().getAuthentication() != null);
        assertEquals(1, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
        @SuppressWarnings("unchecked")
        Map<String, Object> principal = (Map<String, Object>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals(sessionId.toString(), principal.get("sessionId"));
    }

    @Test
    void doFilterInternal_ValidJwtWithNonListRolesAndMissingSession() throws Exception {
        String secret = "my-super-secret-key-that-is-at-least-32-bytes";
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("email", "email@email.com")
                .claim("roles", "ADMIN")
                .issuedAt(new Date())
                .expiration(Date.from(java.time.Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        @SuppressWarnings("unchecked")
        Map<String, Object> principal = (Map<String, Object>) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("", principal.get("sessionId"));
        assertEquals(List.of(), principal.get("roles"));
    }
}
