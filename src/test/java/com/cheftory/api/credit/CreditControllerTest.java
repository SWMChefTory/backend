package com.cheftory.api.credit;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Credit Controller")
class CreditControllerTest extends RestDocsTest {

  private CreditService creditService;
  private CreditController controller;
  private GlobalExceptionHandler exceptionHandler;
  private UserArgumentResolver userArgumentResolver;

  @BeforeEach
  void setUp() {
    creditService = mock(CreditService.class);
    controller = new CreditController(creditService);
    exceptionHandler = new GlobalExceptionHandler();
    userArgumentResolver = new UserArgumentResolver();

    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(exceptionHandler)
            .withArgumentResolver(userArgumentResolver)
            .build();
  }

  @Nested
  @DisplayName("크레딧 잔액 조회")
  class GetBalance {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Given - user_id가 주어졌을 때 Then - balance를 반환한다")
    void shouldReturnBalance() {
      doReturn(123L).when(creditService).getBalance(any(UUID.class));

      var response =
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/credit/balance")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      responseFields(fieldWithPath("balance").description("현재 크레딧 잔액"))));

      var responseBody = response.extract().jsonPath();
      assertThat(responseBody.getLong("balance")).isEqualTo(123L);
    }
  }
}
