package com.cheftory.api.recipeinfo.progress;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.cheftory.api._common.Clock;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeProgressController")
public class RecipeProgressControllerTest extends RestDocsTest {

  private RecipeProgressService recipeProgressService;
  private RecipeService recipeService;
  private RecipeProgressController controller;
  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    recipeProgressService = mock(RecipeProgressService.class);
    recipeService = mock(RecipeService.class);
    controller = new RecipeProgressController(recipeProgressService);
    exceptionHandler = new GlobalExceptionHandler();
    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(exceptionHandler)
            .withValidator(RecipeService.class, recipeService)
            .build();
  }

  @Nested
  @DisplayName("레시피 진행 상황 조회")
  class GetRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenRequestingRecipeProgress {

        private List<RecipeProgress> recipeProgresses;
        private Clock mockClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
          mockClock = mock(Clock.class);
          fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
          doReturn(fixedTime).when(mockClock).now();

          recipeProgresses =
              List.of(
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.CAPTION,
                      RecipeProgressDetail.CAPTION),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.DETAIL,
                      RecipeProgressDetail.INGREDIENT),
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG),
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.STEP, RecipeProgressDetail.STEP),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.FINISHED,
                      RecipeProgressDetail.FINISHED));

          doReturn(recipeProgresses).when(recipeProgressService).finds(any(UUID.class));
          doReturn(true).when(recipeService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 레시피 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnRecipeProgress() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("recipe_progress_statuses", hasSize(recipeProgresses.size()))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              fieldWithPath("recipe_progress_statuses").description("레시피 진행 상황 목록"),
                              enumFields(
                                  "recipe_progress_statuses[].progress_step",
                                  "현재 진행 상태",
                                  RecipeProgressStep.class),
                              enumFields(
                                  "recipe_progress_statuses[].progress_detail",
                                  "현재 진행 상세 내용",
                                  RecipeProgressDetail.class))));

          verify(recipeProgressService).finds(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(6);

          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_step"))
              .isEqualTo("READY");
          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_detail"))
              .isEqualTo("READY");

          assertThat(responseBody.getString("recipe_progress_statuses[1].progress_step"))
              .isEqualTo("CAPTION");
          assertThat(responseBody.getString("recipe_progress_statuses[1].progress_detail"))
              .isEqualTo("CAPTION");

          assertThat(responseBody.getString("recipe_progress_statuses[2].progress_step"))
              .isEqualTo("DETAIL");
          assertThat(responseBody.getString("recipe_progress_statuses[2].progress_detail"))
              .isEqualTo("INGREDIENT");

          assertThat(responseBody.getString("recipe_progress_statuses[3].progress_step"))
              .isEqualTo("DETAIL");
          assertThat(responseBody.getString("recipe_progress_statuses[3].progress_detail"))
              .isEqualTo("TAG");

          assertThat(responseBody.getString("recipe_progress_statuses[4].progress_step"))
              .isEqualTo("STEP");
          assertThat(responseBody.getString("recipe_progress_statuses[4].progress_detail"))
              .isEqualTo("STEP");

          assertThat(responseBody.getString("recipe_progress_statuses[5].progress_step"))
              .isEqualTo("FINISHED");
          assertThat(responseBody.getString("recipe_progress_statuses[5].progress_detail"))
              .isEqualTo("FINISHED");
        }
      }
    }

    @Nested
    @DisplayName("Given - 진행 중인 레시피 ID가 주어졌을 때")
    class GivenInProgressRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenRequestingRecipeProgress {

        private List<RecipeProgress> partialProgresses;
        private Clock mockClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
          mockClock = mock(Clock.class);
          fixedTime = LocalDateTime.of(2024, 2, 1, 14, 30, 0);
          doReturn(fixedTime).when(mockClock).now();

          partialProgresses =
              List.of(
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.CAPTION,
                      RecipeProgressDetail.CAPTION),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.DETAIL,
                      RecipeProgressDetail.INGREDIENT));

          doReturn(partialProgresses).when(recipeProgressService).finds(any(UUID.class));
          doReturn(true).when(recipeService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 부분적인 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnPartialProgress() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("recipe_progress_statuses", hasSize(3));

          verify(recipeProgressService).finds(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(3);
          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_step"))
              .isEqualTo("READY");
          assertThat(responseBody.getString("recipe_progress_statuses[1].progress_step"))
              .isEqualTo("CAPTION");
          assertThat(responseBody.getString("recipe_progress_statuses[2].progress_step"))
              .isEqualTo("DETAIL");
          assertThat(responseBody.getString("recipe_progress_statuses[2].progress_detail"))
              .isEqualTo("INGREDIENT");
        }
      }
    }

    @Nested
    @DisplayName("Given - 시작되지 않은 레시피 ID가 주어졌을 때")
    class GivenNotStartedRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenRequestingRecipeProgress {

        @BeforeEach
        void setUp() {
          doReturn(Collections.emptyList()).when(recipeProgressService).finds(any(UUID.class));
          doReturn(true).when(recipeService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 진행 상황 목록을 반환해야 한다")
        void thenShouldReturnEmptyProgress() {
          given()
              .contentType(ContentType.JSON)
              .get("/api/v1/recipes/progress/{recipeId}", recipeId)
              .then()
              .status(HttpStatus.OK)
              .body("recipe_progress_statuses", hasSize(0));

          verify(recipeProgressService).finds(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 브리핑 포함된 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithBriefing {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenRequestingRecipeProgress {

        private List<RecipeProgress> progressesWithBriefing;
        private Clock mockClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
          mockClock = mock(Clock.class);
          fixedTime = LocalDateTime.of(2024, 3, 1, 16, 45, 0);
          doReturn(fixedTime).when(mockClock).now();

          progressesWithBriefing =
              List.of(
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.CAPTION,
                      RecipeProgressDetail.CAPTION),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.BRIEFING,
                      RecipeProgressDetail.BRIEFING),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.DETAIL,
                      RecipeProgressDetail.INGREDIENT),
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.STEP, RecipeProgressDetail.STEP));

          doReturn(progressesWithBriefing).when(recipeProgressService).finds(any(UUID.class));
          doReturn(true).when(recipeService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 브리핑을 포함한 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnProgressWithBriefing() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("recipe_progress_statuses", hasSize(5));

          verify(recipeProgressService).finds(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(5);

          boolean hasBriefingStep = false;
          for (int i = 0; i < 5; i++) {
            String step =
                responseBody.getString("recipe_progress_statuses[" + i + "].progress_step");
            String detail =
                responseBody.getString("recipe_progress_statuses[" + i + "].progress_detail");
            if ("BRIEFING".equals(step) && "BRIEFING".equals(detail)) {
              hasBriefingStep = true;
              break;
            }
          }
          assertThat(hasBriefingStep).isTrue();
        }
      }
    }

    @Nested
    @DisplayName("Given - 복잡한 다단계 진행을 가진 레시피 ID가 주어졌을 때")
    class GivenComplexMultiStepProgressRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenRequestingRecipeProgress {

        private List<RecipeProgress> complexProgresses;
        private Clock mockClock;
        private LocalDateTime fixedTime;

        @BeforeEach
        void setUp() {
          mockClock = mock(Clock.class);
          fixedTime = LocalDateTime.of(2024, 4, 1, 18, 20, 0);
          doReturn(fixedTime).when(mockClock).now();

          complexProgresses =
              List.of(
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.CAPTION,
                      RecipeProgressDetail.CAPTION),
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.DETAIL,
                      RecipeProgressDetail.DETAIL_META),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.DETAIL,
                      RecipeProgressDetail.INGREDIENT),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.BRIEFING,
                      RecipeProgressDetail.BRIEFING),
                  RecipeProgress.create(
                      recipeId, mockClock, RecipeProgressStep.STEP, RecipeProgressDetail.STEP),
                  RecipeProgress.create(
                      recipeId,
                      mockClock,
                      RecipeProgressStep.FINISHED,
                      RecipeProgressDetail.FINISHED));

          doReturn(complexProgresses).when(recipeProgressService).finds(any(UUID.class));
          doReturn(true).when(recipeService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 복잡한 진행 단계들을 성공적으로 반환해야 한다")
        void thenShouldReturnAllComplexProgressSteps() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("recipe_progress_statuses", hasSize(8));

          verify(recipeProgressService).finds(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(8);

          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_step"))
              .isEqualTo("READY");
          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_detail"))
              .isEqualTo("READY");

          assertThat(responseBody.getString("recipe_progress_statuses[7].progress_step"))
              .isEqualTo("FINISHED");
          assertThat(responseBody.getString("recipe_progress_statuses[7].progress_detail"))
              .isEqualTo("FINISHED");

          List<String> detailSteps = List.of("TAG", "DETAIL_META", "INGREDIENT");
          int detailCount = 0;
          for (int i = 0; i < 8; i++) {
            String step =
                responseBody.getString("recipe_progress_statuses[" + i + "].progress_step");
            String detail =
                responseBody.getString("recipe_progress_statuses[" + i + "].progress_detail");
            if ("DETAIL".equals(step) && detailSteps.contains(detail)) {
              detailCount++;
            }
          }
          assertThat(detailCount).isEqualTo(3);
        }
      }
    }
  }
}
