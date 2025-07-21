package com.cheftory.api.security;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SampleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private TokenProvider tokenProvider;

  @Test
  void 인증_없으면_400_예외처리() throws Exception {
    mockMvc.perform(get("/api/sample/public"))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value(AuthErrorCode.INVALID_TOKEN.getMessage()))
        .andExpect(jsonPath("$.errorCode").value(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
  }

  @Test
  void 인증_있으면_200() throws Exception {
    UUID userId = UUID.randomUUID();
    String validToken = tokenProvider.createAccessToken(userId);
    String validJwt = "Bearer " + validToken;

    mockMvc.perform(get("/your-secured-url")
            .header("Authorization", validJwt))
        .andExpect(status().isOk());
  }
}