package com.cheftory.api.security;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.auth.jwt.TokenProvider;
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
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
      UUID userId = tokenProvider.getUserIdFromToken(jwtToken);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      request.setAttribute("userId", userId);
    } catch (Exception e) {
      request.setAttribute("Exception", e);
    }
    filterChain.doFilter(request, response);
  }
}
