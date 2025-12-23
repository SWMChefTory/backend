package com.cheftory.api.recipe.content.step;

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

import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeStepController")
public class RecipeStepControllerTest extends RestDocsTest {

  private RecipeStepService recipeStepService;
  private RecipeInfoService recipeInfoService;
  private RecipeStepController controller;
  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    recipeStepService = mock(RecipeStepService.class);
    recipeInfoService = mock(RecipeInfoService.class);
    controller = new RecipeStepController(recipeStepService);
    exceptionHandler = new GlobalExceptionHandler();
    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(exceptionHandler)
            .withValidator(RecipeInfoService.class, recipeInfoService)
            .build();
  }

  @Nested
  @DisplayName("레시피 단계 조회")
  class GetRecipeSteps {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 조회한다면")
      class WhenRequestingRecipeSteps {

        private List<RecipeStep> recipeSteps;
        private RecipeStep step1;
        private RecipeStep step2;
        private UUID step1Id;
        private UUID step2Id;

        @BeforeEach
        void setUp() {
          step1Id = UUID.randomUUID();
          step2Id = UUID.randomUUID();

          step1 = mock(RecipeStep.class);
          step2 = mock(RecipeStep.class);

          // Step 1 모킹
          doReturn(step1Id).when(step1).getId();
          doReturn(1).when(step1).getStepOrder();
          doReturn("재료 준비").when(step1).getSubtitle();
          doReturn(0.0).when(step1).getStart();

          RecipeStep.Detail step1Detail1 = RecipeStep.Detail.of("김치 200g을 준비합니다", 0.0);
          RecipeStep.Detail step1Detail2 = RecipeStep.Detail.of("돼지고기 150g을 준비합니다", 5.0);
          doReturn(List.of(step1Detail1, step1Detail2)).when(step1).getDetails();

          // Step 2 모킹
          doReturn(step2Id).when(step2).getId();
          doReturn(2).when(step2).getStepOrder();
          doReturn("조리하기").when(step2).getSubtitle();
          doReturn(30.0).when(step2).getStart();

          RecipeStep.Detail step2Detail1 = RecipeStep.Detail.of("팬에 기름을 두르고 가열합니다", 30.0);
          RecipeStep.Detail step2Detail2 = RecipeStep.Detail.of("김치와 돼지고기를 넣고 볶습니다", 45.0);
          doReturn(List.of(step2Detail1, step2Detail2)).when(step2).getDetails();

          recipeSteps = List.of(step1, step2);
          doReturn(recipeSteps).when(recipeStepService).gets(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 레시피 단계를 성공적으로 반환해야 한다")
        void thenShouldReturnRecipeSteps() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/steps", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("steps", hasSize(recipeSteps.size()))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              fieldWithPath("steps").description("레시피 단계 목록"),
                              fieldWithPath("steps[].id").description("레시피 단계 ID"),
                              fieldWithPath("steps[].step_order").description("레시피 단계 순서"),
                              fieldWithPath("steps[].subtitle").description("레시피 단계 제목"),
                              fieldWithPath("steps[].details").description("레시피 단계 상세 설명 목록"),
                              fieldWithPath("steps[].details[].text").description("단계 상세 설명 텍스트"),
                              fieldWithPath("steps[].details[].start")
                                  .description("단계 상세 설명 시작 시간(초)"),
                              fieldWithPath("steps[].start").description("레시피 단계 시작 시간(초)"))));

          verify(recipeStepService).gets(recipeId);

          var responseBody = response.extract().jsonPath();

          // 전체 응답 검증
          assertThat(responseBody.getList("steps")).hasSize(2);

          // Step 1 검증
          assertThat(responseBody.getUUID("steps[0].id")).isEqualTo(step1Id);
          assertThat(responseBody.getInt("steps[0].step_order")).isEqualTo(1);
          assertThat(responseBody.getString("steps[0].subtitle")).isEqualTo("재료 준비");
          assertThat(responseBody.getDouble("steps[0].start")).isEqualTo(0.0);

          // Step 1 Details 검증
          assertThat(responseBody.getList("steps[0].details")).hasSize(2);
          assertThat(responseBody.getString("steps[0].details[0].text"))
              .isEqualTo("김치 200g을 준비합니다");
          assertThat(responseBody.getDouble("steps[0].details[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getString("steps[0].details[1].text"))
              .isEqualTo("돼지고기 150g을 준비합니다");
          assertThat(responseBody.getDouble("steps[0].details[1].start")).isEqualTo(5.0);

          // Step 2 검증
          assertThat(responseBody.getUUID("steps[1].id")).isEqualTo(step2Id);
          assertThat(responseBody.getInt("steps[1].step_order")).isEqualTo(2);
          assertThat(responseBody.getString("steps[1].subtitle")).isEqualTo("조리하기");
          assertThat(responseBody.getDouble("steps[1].start")).isEqualTo(30.0);

          // Step 2 Details 검증
          assertThat(responseBody.getList("steps[1].details")).hasSize(2);
          assertThat(responseBody.getString("steps[1].details[0].text"))
              .isEqualTo("팬에 기름을 두르고 가열합니다");
          assertThat(responseBody.getDouble("steps[1].details[0].start")).isEqualTo(30.0);
          assertThat(responseBody.getString("steps[1].details[1].text"))
              .isEqualTo("김치와 돼지고기를 넣고 볶습니다");
          assertThat(responseBody.getDouble("steps[1].details[1].start")).isEqualTo(45.0);
        }
      }
    }

    @Nested
    @DisplayName("Given - 단일 단계만 있는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithSingleStep {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 조회한다면")
      class WhenRequestingRecipeSteps {

        private List<RecipeStep> recipeSteps;
        private RecipeStep singleStep;
        private UUID stepId;

        @BeforeEach
        void setUp() {
          stepId = UUID.randomUUID();
          singleStep = mock(RecipeStep.class);

          doReturn(stepId).when(singleStep).getId();
          doReturn(1).when(singleStep).getStepOrder();
          doReturn("간단한 조리").when(singleStep).getSubtitle();
          doReturn(0.0).when(singleStep).getStart();

          RecipeStep.Detail detail = RecipeStep.Detail.of("계란을 풀어서 프라이팬에 넣고 익혀주세요", 0.0);
          doReturn(List.of(detail)).when(singleStep).getDetails();

          recipeSteps = List.of(singleStep);
          doReturn(recipeSteps).when(recipeStepService).gets(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 단일 레시피 단계를 성공적으로 반환해야 한다")
        void thenShouldReturnSingleRecipeStep() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/steps", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("steps", hasSize(1));

          verify(recipeStepService).gets(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getUUID("steps[0].id")).isEqualTo(stepId);
          assertThat(responseBody.getInt("steps[0].step_order")).isEqualTo(1);
          assertThat(responseBody.getString("steps[0].subtitle")).isEqualTo("간단한 조리");
          assertThat(responseBody.getDouble("steps[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getList("steps[0].details")).hasSize(1);
          assertThat(responseBody.getString("steps[0].details[0].text"))
              .isEqualTo("계란을 풀어서 프라이팬에 넣고 익혀주세요");
        }
      }
    }

    @Nested
    @DisplayName("Given - 단계가 없는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithNoSteps {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 조회한다면")
      class WhenRequestingRecipeSteps {

        @BeforeEach
        void setUp() {
          doReturn(Collections.emptyList()).when(recipeStepService).gets(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 단계 목록을 반환해야 한다")
        void thenShouldReturnEmptySteps() {
          given()
              .contentType(ContentType.JSON)
              .get("/papi/v1/recipes/{recipeId}/steps", recipeId)
              .then()
              .status(HttpStatus.OK)
              .body("steps", hasSize(0));

          verify(recipeStepService).gets(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID nonExistentRecipeId;

      @BeforeEach
      void setUp() {
        nonExistentRecipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 조회한다면")
      class WhenRequestingRecipeSteps {

        @BeforeEach
        void setUp() {
          doReturn(Collections.emptyList()).when(recipeStepService).gets(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 단계 목록을 반환해야 한다")
        void thenShouldReturnEmptyStepsForNonExistentRecipe() {
          given()
              .contentType(ContentType.JSON)
              .get("/papi/v1/recipes/{recipeId}/steps", nonExistentRecipeId)
              .then()
              .status(HttpStatus.OK)
              .body("steps", hasSize(0));

          verify(recipeStepService).gets(nonExistentRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 복잡한 다단계 레시피 ID가 주어졌을 때")
    class GivenComplexMultiStepRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 조회한다면")
      class WhenRequestingRecipeSteps {

        private List<RecipeStep> complexRecipeSteps;

        @BeforeEach
        void setUp() {
          // 3단계 복잡한 레시피 생성
          RecipeStep step1 = mock(RecipeStep.class);
          RecipeStep step2 = mock(RecipeStep.class);
          RecipeStep step3 = mock(RecipeStep.class);

          UUID step1Id = UUID.randomUUID();
          UUID step2Id = UUID.randomUUID();
          UUID step3Id = UUID.randomUUID();

          // Step 1
          doReturn(step1Id).when(step1).getId();
          doReturn(1).when(step1).getStepOrder();
          doReturn("재료 손질").when(step1).getSubtitle();
          doReturn(0.0).when(step1).getStart();
          doReturn(
                  List.of(
                      RecipeStep.Detail.of("야채를 씻습니다", 0.0),
                      RecipeStep.Detail.of("고기를 손질합니다", 10.0),
                      RecipeStep.Detail.of("양념을 준비합니다", 20.0)))
              .when(step1)
              .getDetails();

          // Step 2
          doReturn(step2Id).when(step2).getId();
          doReturn(2).when(step2).getStepOrder();
          doReturn("조리 시작").when(step2).getSubtitle();
          doReturn(60.0).when(step2).getStart();
          doReturn(
                  List.of(
                      RecipeStep.Detail.of("팬을 예열합니다", 60.0),
                      RecipeStep.Detail.of("고기를 볶습니다", 75.0)))
              .when(step2)
              .getDetails();

          // Step 3
          doReturn(step3Id).when(step3).getId();
          doReturn(3).when(step3).getStepOrder();
          doReturn("마무리").when(step3).getSubtitle();
          doReturn(120.0).when(step3).getStart();
          doReturn(
                  List.of(
                      RecipeStep.Detail.of("야채를 추가합니다", 120.0),
                      RecipeStep.Detail.of("양념을 넣고 볶습니다", 135.0),
                      RecipeStep.Detail.of("완성된 요리를 그릇에 담습니다", 150.0)))
              .when(step3)
              .getDetails();

          complexRecipeSteps = List.of(step1, step2, step3);
          doReturn(complexRecipeSteps).when(recipeStepService).gets(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 복잡한 단계들을 성공적으로 반환해야 한다")
        void thenShouldReturnAllComplexSteps() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/steps", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("steps", hasSize(3));

          verify(recipeStepService).gets(recipeId);

          var responseBody = response.extract().jsonPath();

          // 전체 단계 수 검증
          assertThat(responseBody.getList("steps")).hasSize(3);

          // 각 단계의 기본 정보 검증
          assertThat(responseBody.getInt("steps[0].step_order")).isEqualTo(1);
          assertThat(responseBody.getString("steps[0].subtitle")).isEqualTo("재료 손질");
          assertThat(responseBody.getList("steps[0].details")).hasSize(3);

          assertThat(responseBody.getInt("steps[1].step_order")).isEqualTo(2);
          assertThat(responseBody.getString("steps[1].subtitle")).isEqualTo("조리 시작");
          assertThat(responseBody.getList("steps[1].details")).hasSize(2);

          assertThat(responseBody.getInt("steps[2].step_order")).isEqualTo(3);
          assertThat(responseBody.getString("steps[2].subtitle")).isEqualTo("마무리");
          assertThat(responseBody.getList("steps[2].details")).hasSize(3);

          // 시작 시간 순서 검증
          assertThat(responseBody.getDouble("steps[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("steps[1].start")).isEqualTo(60.0);
          assertThat(responseBody.getDouble("steps[2].start")).isEqualTo(120.0);
        }
      }
    }
  }
}
