package com.cheftory.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.jwt.TokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TokenProvider tokenProvider;

  @Test
  void 인증_없으면_400_예외처리() throws Exception {
    mockMvc.perform(get("/api/security/failed"))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_TOKEN.getMessage()))
        .andExpect(jsonPath("$.errorCode").value(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
  }

  @Test
  void 인증_있으면_200() throws Exception {
    UUID userId = UUID.randomUUID();
    String validJwt = tokenProvider.createAccessToken(userId);
    String header =  "Bearer " + validJwt;
    mockMvc.perform(get("/api/security/success")
            .header(HttpHeaders.AUTHORIZATION, header))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(userId.toString()));
  }

  @Test
  void 프라이빗_URL_항상_200() throws Exception {
    mockMvc.perform(get("/papi/v1/security/success"))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }

  @Test
  void 계정_생성_항상_200() throws Exception {
    UUID userId = UUID.randomUUID();
    String validJwt = tokenProvider.createAccessToken(userId);
    mockMvc.perform(get("/api/v1/account/security/success")
            .header(HttpHeaders.AUTHORIZATION, validJwt))
        .andExpect(status().isOk())
        .andExpect(content().string("success"));
  }
}