package com.cheftory.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.jwt.TokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityControllerTest {

  private static final String COUNTRY_HEADER = "X-Country-Code";

  @Autowired private MockMvc mockMvc;
  @Autowired private TokenProvider tokenProvider;

  private RequestPostProcessor countryHeader() {
    return request -> {
      request.addHeader(COUNTRY_HEADER, "KR");
      return request;
    };
  }

  @Test
  void 인증_없으면_400_예외처리() throws Exception {
    mockMvc
        .perform(get("/api/security/failed").with(countryHeader()))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_TOKEN.getMessage()))
        .andExpect(jsonPath("$.errorCode").value(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
  }

  @Test
  void 인증_있으면_200() throws Exception {
    UUID userId = UUID.randomUUID();
    String validJwt = tokenProvider.createAccessToken(userId);

    mockMvc
        .perform(
            get("/api/security/success")
                .with(countryHeader())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwt))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(userId.toString()));
  }

  @Test
  void 프라이빗_URL_항상_200() throws Exception {
    mockMvc
        .perform(get("/papi/v1/security/success").with(countryHeader()))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }

  @Test
  void 계정_생성_항상_200() throws Exception {
    UUID userId = UUID.randomUUID();
    String validJwt = tokenProvider.createAccessToken(userId);

    mockMvc
        .perform(
            get("/api/v1/account/security/success")
                .with(countryHeader())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwt))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }
}
