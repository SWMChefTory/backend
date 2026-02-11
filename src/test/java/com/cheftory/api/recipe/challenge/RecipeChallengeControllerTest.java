package com.cheftory.api.recipe.challenge;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeErrorCode;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("RecipeChallenge Controller")
class RecipeChallengeControllerTest extends RestDocsTest {

    private RecipeChallengeService recipeChallengeService;
    private RecipeChallengeController controller;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        recipeChallengeService = mock(RecipeChallengeService.class);
        controller = new RecipeChallengeController(recipeChallengeService);
        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Nested
    @DisplayName("챌린지 조회")
    class GetChallenge {

        private UUID userId;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            var authentication = new UsernamePasswordAuthenticationToken(userId, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("Given - user_id가 주어졌을 때 Then - 챌린지 정보를 반환한다")
        void shouldReturnChallenge() throws RecipeChallengeException {
            Challenge challenge = mock(Challenge.class);
            UUID challengeId = UUID.randomUUID();
            LocalDateTime startAt = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endAt = LocalDateTime.of(2024, 1, 31, 23, 59);

            doReturn(challengeId).when(challenge).getId();
            doReturn(startAt).when(challenge).getStartAt();
            doReturn(endAt).when(challenge).getEndAt();
            doReturn(ChallengeType.SINGLE).when(challenge).getType();
            doReturn(challenge).when(recipeChallengeService).getUser(any(UUID.class));

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .get("/api/v1/recipes/challenge")
                    .then()
                    .status(HttpStatus.OK)
                    .apply(document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            responseFields(
                                    fieldWithPath("challenge_id").description("챌린지 ID"),
                                    fieldWithPath("start_at").description("챌린지 시작 시간"),
                                    fieldWithPath("end_at").description("챌린지 종료 시간"),
                                    fieldWithPath("type").description("챌린지 타입 (SINGLE, HOUSEWIFE)"))));

            var responseBody = response.extract().jsonPath();
            assertThat(responseBody.getString("challenge_id")).isEqualTo(challengeId.toString());
            assertThat(responseBody.getString("type")).isEqualTo("SINGLE");
        }

        @Test
        @DisplayName("Given - 참여 중인 챌린지가 없을 때 Then - 400 에러를 반환한다")
        void shouldReturnErrorWhenChallengeNotFound() throws RecipeChallengeException {
            doThrow(new RecipeChallengeException(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND))
                    .when(recipeChallengeService)
                    .getUser(any(UUID.class));

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .get("/api/v1/recipes/challenge")
                    .then()
                    .status(HttpStatus.BAD_REQUEST);

            var responseBody = response.extract().jsonPath();
            assertThat(responseBody.getString("errorCode"))
                    .isEqualTo(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND.getErrorCode());
        }
    }
}
