package com.smartstadium.security;

import com.smartstadium.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Authentication Filter")
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("sets authentication for valid Bearer token")
    void validBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.validateToken("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("user@test.com");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("skips authentication for invalid token")
    void invalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtService.validateToken("bad-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("skips authentication when no auth header")
    void noAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("falls back to refresh_token cookie")
    void fallbackToCookie() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie cookie = new Cookie("refresh_token", "cookie-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtService.validateToken("cookie-token")).thenReturn(true);
        when(jwtService.extractEmail("cookie-token")).thenReturn("cookie@test.com");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("cookie@test.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("ignores non-refresh cookies")
    void ignoresOtherCookies() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie cookie = new Cookie("session_id", "some-value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("always calls filterChain.doFilter")
    void alwaysContinuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("prefers header over cookie")
    void prefersHeaderOverCookie() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer header-token");
        when(jwtService.validateToken("header-token")).thenReturn(true);
        when(jwtService.extractEmail("header-token")).thenReturn("header@test.com");

        filter.doFilterInternal(request, response, filterChain);

        assertEquals("header@test.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("handles non-Bearer auth header gracefully")
    void nonBearerHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("handles empty cookies array")
    void emptyCookiesArray() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("authentication has no authorities")
    void noAuthorities() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.validateToken("token")).thenReturn(true);
        when(jwtService.extractEmail("token")).thenReturn("user@test.com");

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }
}
