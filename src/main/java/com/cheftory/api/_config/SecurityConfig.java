package com.cheftory.api._config;

import com.cheftory.api.security.JwtAuthenticationEntryPoint;
import com.cheftory.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final JwtAuthenticationEntryPoint entryPoint;
  private final JwtAuthenticationFilter filter;


  public SecurityConfig(JwtAuthenticationEntryPoint entryPoint, JwtAuthenticationFilter filter) {
    this.entryPoint = entryPoint;
    this.filter = filter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(Customizer.withDefaults())
        .sessionManagement(sm ->
            sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/papi/v1/**").permitAll()
            .requestMatchers("/api/v1/account/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(filter, BasicAuthenticationFilter.class)
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(entryPoint)
        );

    return http.build();
  }
}