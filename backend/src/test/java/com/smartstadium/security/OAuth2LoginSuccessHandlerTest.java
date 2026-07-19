package com.smartstadium.security;

import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.service.AuthService;
import com.smartstadium.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2 Login Success Handler")
class OAuth2LoginSuccessHandlerTest {

    @Mock private AuthService authService;
    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;
    @Mock private OAuth2User oAuth2User;

    @InjectMocks private OAuth2LoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://example.com");
    }

    private AuthResponseDto mockLogin() {
        AuthResponseDto authResponse = AuthResponseDto.builder()
            .accessToken("access-token-123")
            .email("user@gmail.com")
            .name("Test User")
            .role("USER")
            .build();
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authService.processGoogleLogin(oAuth2User)).thenReturn(authResponse);
        when(jwtService.generateRefreshToken("user@gmail.com")).thenReturn("refresh-token-456");
        return authResponse;
    }

    @Test
    @DisplayName("redirects to frontend with access token")
    void redirectsWithToken() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect("https://example.com/auth/callback?token=access-token-123");
    }

    @Test
    @DisplayName("sets refresh token as HttpOnly cookie")
    void setsHttpOnlyCookie() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        Cookie cookie = captor.getValue();

        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token-456", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }

    @Test
    @DisplayName("cookie path is root")
    void cookiePathIsRoot() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        assertEquals("/", captor.getValue().getPath());
    }

    @Test
    @DisplayName("cookie expires in 7 days")
    void cookieExpires7Days() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(captor.capture());
        assertEquals(7 * 24 * 60 * 60, captor.getValue().getMaxAge());
    }

    @Test
    @DisplayName("calls processGoogleLogin with OAuth2User")
    void callsProcessGoogleLogin() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(authService).processGoogleLogin(oAuth2User);
    }

    @Test
    @DisplayName("generates refresh token with correct email")
    void generatesRefreshToken() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(jwtService).generateRefreshToken("user@gmail.com");
    }

    @Test
    @DisplayName("redirect URL uses configured frontend URL")
    void usesConfiguredFrontendUrl() throws Exception {
        ReflectionTestUtils.setField(handler, "frontendUrl", "https://custom-domain.com");
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);
        verify(response).sendRedirect(contains("https://custom-domain.com"));
    }

    @Test
    @DisplayName("redirect URL contains /auth/callback path")
    void redirectContainsCallbackPath() throws Exception {
        mockLogin();
        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());
        assertTrue(captor.getValue().contains("/auth/callback?token="));
    }
}
