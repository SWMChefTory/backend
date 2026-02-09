package com.cheftory.api._common.security;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void 유효한_Bearer_토큰이_주어지면_인증_컨텍스트를_설정하고_filterChain을_호출한다() throws ServletException, IOException, AuthException {
        // Arrange
        UUID userId = UUID.randomUUID();
        String validJwt = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validJwt);

        when(tokenProvider.getUserId(validJwt, AuthTokenType.ACCESS)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserId(validJwt, AuthTokenType.ACCESS);
        verify(filterChain, times(1)).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.getPrincipal().equals(userId);
        assert authentication.getCredentials() == null;
        assert authentication.getAuthorities().isEmpty();
    }

    @Test
    void 토큰이_없으면_요청_속성에_예외를_설정하고_filterChain을_호출한다() throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        // verify(tokenProvider, never()).getUserId(anyString(), any(AuthTokenType.class));
        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof AuthException;
    }

    @Test
    void 유효하지_않은_토큰이_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다() throws ServletException, IOException, AuthException {
        // Arrange
        String invalidJwt = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + invalidJwt);

        when(tokenProvider.getUserId(invalidJwt, AuthTokenType.ACCESS))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserId(eq(invalidJwt), eq(AuthTokenType.ACCESS));
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof IllegalArgumentException;
        assert exception.getMessage().equals("Invalid token");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication == null;
    }

    @Test
    void Bearer_접두사가_없는_토큰이_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다() throws ServletException, IOException {
        // Arrange
        String malformedJwt = "some.jwt.token";
        request.addHeader("Authorization", malformedJwt);
        // BearerAuthorizationUtils.removePrefix will throw AuthException before tokenProvider is called

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // verify(tokenProvider, never()).getUserId(any()); // never called due to prefix check
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void 토큰_검증_중_예외가_발생하면_요청_속성에_예외를_설정하고_filterChain을_호출한다() throws ServletException, IOException, AuthException {
        // Arrange
        String jwt = "jwt.token";
        request.addHeader("Authorization", "Bearer " + jwt);

        when(tokenProvider.getUserId(jwt, AuthTokenType.ACCESS))
                .thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserId(eq(jwt), eq(AuthTokenType.ACCESS));
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof RuntimeException;
        assert exception.getMessage().equals("Token validation failed");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication == null;
    }

    @Test
    void 빈_Authorization_헤더가_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "");
        // BearerAuthorizationUtils.removePrefix will throw AuthException before tokenProvider is called

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // verify(tokenProvider, never()).getUserId(any()); // never called due to prefix check
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof AuthException;
    }
}
