package com.cheftory.api._common.region;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cheftory.api.auth.jwt.TokenProvider;
import com.cheftory.api.exception.GlobalErrorCode;
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
public class RegionControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private TokenProvider tokenProvider;

  @Test
  void 지역_없으면_400_예외처리() throws Exception {
    UUID userId = UUID.randomUUID();
    String validJwt = tokenProvider.createAccessToken(userId);

    mockMvc
        .perform(
            get("/api/security/failed").header(HttpHeaders.AUTHORIZATION, "Bearer " + validJwt))
        .andExpect(status().is4xxClientError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value(GlobalErrorCode.UNKNOWN_REGION.getMessage()))
        .andExpect(jsonPath("$.errorCode").value(GlobalErrorCode.UNKNOWN_REGION.getErrorCode()));
  }
}
