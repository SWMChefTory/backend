package com.cheftory.api._common.security;

import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.jwt.TokenProvider;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            UUID userId =
                    tokenProvider.getUserId(BearerAuthorizationUtils.removePrefix(jwtToken), AuthTokenType.ACCESS);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            request.setAttribute("Exception", e);
        }
        filterChain.doFilter(request, response);
    }
}
