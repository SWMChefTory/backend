package com.cheftory.api.user.share;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
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

@DisplayName("UserShareController 테스트")
public class UserShareControllerTest extends RestDocsTest {

    private UserShareController controller;
    private UserShareService userShareService;
    private GlobalExceptionHandler globalExceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    private UUID fixedUserId;

    @BeforeEach
    void setUp() {
        fixedUserId = UUID.randomUUID();
        userShareService = mock(UserShareService.class);
        controller = new UserShareController(userShareService);
        globalExceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(globalExceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(fixedUserId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/v1/users/share - 공유하기")
    class shareResponse {

        @Test
        @DisplayName("성공 - 공유 횟수를 반환한다")
        void shouldReturnShareCount() throws CreditException, UserShareException {

            doReturn(1).when(userShareService).share(fixedUserId);

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", fixedUserId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .when()
                    .post("/api/v1/users/share")
                    .then()
                    .status(HttpStatus.OK)
                    .apply(document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseFields(fieldWithPath("share_count").description("오늘의 공유 횟수"))));

            response.body("share_count", equalTo(1));
        }

        @Test
        @DisplayName("실패 - 일일 공유 제한을 초과하면 에러를 반환한다")
        void shouldReturnLimitExceededError() throws CreditException, UserShareException {
            doThrow(new UserShareException(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED))
                    .when(userShareService)
                    .share(fixedUserId);

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", fixedUserId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .when()
                    .post("/api/v1/users/share")
                    .then();

            assertErrorResponse(response, UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }
    }
}
