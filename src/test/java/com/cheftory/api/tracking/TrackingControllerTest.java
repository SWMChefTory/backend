package com.cheftory.api.tracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.tracking.dto.TrackingClickRequest;
import com.cheftory.api.tracking.dto.TrackingImpressionRequest;
import com.cheftory.api.tracking.entity.SurfaceType;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("TrackingController 테스트")
class TrackingControllerTest extends RestDocsTest {

    private TrackingService trackingService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        trackingService = mock(TrackingService.class);
        TrackingController controller = new TrackingController(trackingService);

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(new GlobalExceptionHandler())
                .withArgumentResolver(new UserArgumentResolver())
                .build();

        userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("노출 기록 API")
    class TrackImpressions {

        @Test
        @DisplayName("유효한 요청이면 노출 저장을 호출하고 성공 응답을 반환한다")
        void tracksImpressions() {
            UUID requestId = UUID.randomUUID();
            UUID recipeId1 = UUID.randomUUID();
            UUID recipeId2 = UUID.randomUUID();

            Map<String, Object> request = Map.of(
                    "request_id",
                    requestId.toString(),
                    "surface_type",
                    "SEARCH_RESULTS",
                    "impressions",
                    List.of(
                            Map.of("recipe_id", recipeId1.toString(), "position", 0, "timestamp", 1_700_000_000_000L),
                            Map.of("recipe_id", recipeId2.toString(), "position", 1, "timestamp", 1_700_000_010_000L)));

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .body(request)
                    .when()
                    .post("/api/v1/tracking/impressions")
                    .then();

            assertSuccessResponse(response);

            ArgumentCaptor<TrackingImpressionRequest> captor = ArgumentCaptor.forClass(TrackingImpressionRequest.class);
            verify(trackingService).saveImpressions(eq(userId), captor.capture());
            TrackingImpressionRequest captured = captor.getValue();
            assertThat(captured.requestId()).isEqualTo(requestId);
            assertThat(captured.surfaceType()).isEqualTo(SurfaceType.SEARCH_RESULTS);
            assertThat(captured.impressions()).hasSize(2);
        }

        @Test
        @DisplayName("노출 목록이 비어 있으면 400을 반환한다")
        void returnsBadRequestWhenImpressionsEmpty() {
            Map<String, Object> request = Map.of(
                    "request_id",
                    UUID.randomUUID().toString(),
                    "surface_type",
                    "SEARCH_RESULTS",
                    "impressions",
                    List.of());

            given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .body(request)
                    .when()
                    .post("/api/v1/tracking/impressions")
                    .then()
                    .status(HttpStatus.BAD_REQUEST);

            verifyNoInteractions(trackingService);
        }
    }

    @Nested
    @DisplayName("클릭 기록 API")
    class TrackClick {

        @Test
        @DisplayName("유효한 요청이면 클릭 저장을 호출하고 성공 응답을 반환한다")
        void tracksClick() {
            UUID requestId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            Map<String, Object> request = Map.of(
                    "request_id",
                    requestId.toString(),
                    "surface_type",
                    "HOME_POPULAR_RECIPES",
                    "recipe_id",
                    recipeId.toString(),
                    "position",
                    4,
                    "timestamp",
                    1_700_000_020_000L);

            var response = given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .body(request)
                    .when()
                    .post("/api/v1/tracking/clicks")
                    .then();

            assertSuccessResponse(response);

            ArgumentCaptor<TrackingClickRequest> captor = ArgumentCaptor.forClass(TrackingClickRequest.class);
            verify(trackingService).saveClick(eq(userId), captor.capture());
            TrackingClickRequest captured = captor.getValue();
            assertThat(captured.requestId()).isEqualTo(requestId);
            assertThat(captured.surfaceType()).isEqualTo(SurfaceType.HOME_POPULAR_RECIPES);
            assertThat(captured.recipeId()).isEqualTo(recipeId);
            assertThat(captured.position()).isEqualTo(4);
        }

        @Test
        @DisplayName("필수 필드가 없으면 400을 반환한다")
        void returnsBadRequestWhenMissingField() {
            Map<String, Object> request = Map.of(
                    "request_id",
                    UUID.randomUUID().toString(),
                    "surface_type",
                    "HOME_POPULAR_RECIPES",
                    "position",
                    4,
                    "timestamp",
                    1_700_000_020_000L);

            given().contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .body(request)
                    .when()
                    .post("/api/v1/tracking/clicks")
                    .then()
                    .status(HttpStatus.BAD_REQUEST);

            verifyNoInteractions(trackingService);
        }
    }
}
