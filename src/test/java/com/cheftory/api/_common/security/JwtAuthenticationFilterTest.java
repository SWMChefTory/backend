package com.cheftory.api._common.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void 유효한_Bearer_토큰이_주어지면_인증_컨텍스트를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        String validJwt = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validJwt);

        when(tokenProvider.getUserIdFromToken(validJwt)).thenReturn(userId);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserIdFromToken(validJwt);
        verify(filterChain, times(1)).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        assert authentication.getPrincipal().equals(userId);
        assert authentication.getCredentials() == null;
        assert authentication.getAuthorities().isEmpty();
    }

    @Test
    void 토큰이_없으면_요청_속성에_예외를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(tokenProvider, never()).getUserIdFromToken(any());

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof AuthException;
    }

    @Test
    void 유효하지_않은_토큰이_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Arrange
        String invalidJwt = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + invalidJwt);

        when(tokenProvider.getUserIdFromToken(invalidJwt))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserIdFromToken(invalidJwt);
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof IllegalArgumentException;
        assert exception.getMessage().equals("Invalid token");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication == null;
    }

    @Test
    void Bearer_접두사가_없는_토큰이_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Arrange
        String malformedJwt = "some.jwt.token";
        request.addHeader("Authorization", malformedJwt);
        // BearerAuthorizationUtils.removePrefix will throw AuthException before tokenProvider is called

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).getUserIdFromToken(any()); // never called due to prefix check
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof AuthException;
    }

    @Test
    void 토큰_검증_중_예외가_발생하면_요청_속성에_예외를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Arrange
        String jwt = "jwt.token";
        request.addHeader("Authorization", "Bearer " + jwt);

        when(tokenProvider.getUserIdFromToken(jwt))
                .thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, times(1)).getUserIdFromToken(jwt);
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof RuntimeException;
        assert exception.getMessage().equals("Token validation failed");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication == null;
    }

    @Test
    void 빈_Authorization_헤더가_주어지면_요청_속성에_예외를_설정하고_filterChain을_호출한다()
            throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "");
        // BearerAuthorizationUtils.removePrefix will throw AuthException before tokenProvider is called

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).getUserIdFromToken(any()); // never called due to prefix check
        verify(filterChain, times(1)).doFilter(request, response);

        var exception = (Exception) request.getAttribute("Exception");
        assert exception != null;
        assert exception instanceof AuthException;
    }
}
