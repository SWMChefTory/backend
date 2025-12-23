package com.cheftory.api.recipe.content.caption;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.caption.RecipeCaptionController;
import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.entity.LangCodeType;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeCaptionController")
public class RecipeCaptionControllerTest extends RestDocsTest {

  private RecipeCaptionService recipeCaptionService;
  private RecipeInfoService recipeInfoService;
  private RecipeCaptionController controller;
  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    recipeCaptionService = mock(RecipeCaptionService.class);
    controller = new RecipeCaptionController(recipeCaptionService);
    exceptionHandler = new GlobalExceptionHandler();
    recipeInfoService = mock(RecipeInfoService.class);
    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(exceptionHandler)
            .withValidator(RecipeInfoService.class, recipeInfoService)
            .build();
  }

  @Nested
  @DisplayName("레시피 자막 조회")
  class GetRecipeCaption {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 한국어 자막을 조회한다면")
      class WhenRequestingKoreanCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = mock(RecipeCaption.class);

          List<RecipeCaption.Segment> segments =
              List.of(
                  new RecipeCaption.Segment("안녕하세요, 오늘은 김치찌개를 만들어보겠습니다", 0.0, 3.5),
                  new RecipeCaption.Segment("먼저 김치 200그램을 준비해주세요", 3.5, 7.0),
                  new RecipeCaption.Segment("돼지고기 150그램도 함께 준비합니다", 7.0, 10.5),
                  new RecipeCaption.Segment("팬에 기름을 두르고 김치를 볶아주세요", 10.5, 14.0));

          doReturn(segments).when(recipeCaption).getSegments();
          doReturn(LangCodeType.ko).when(recipeCaption).getLangCode();
          doReturn(recipeCaption).when(recipeCaptionService).findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 한국어 자막을 성공적으로 반환해야 한다")
        void thenShouldReturnKoreanCaption() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/caption", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("captions", hasSize(4))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              fieldWithPath("lang_code").description("자막 언어 코드 (ko: 한국어, en: 영어)"),
                              fieldWithPath("captions").description("자막 세그먼트 목록"),
                              fieldWithPath("captions[].start").description("자막 시작 시간(초)"),
                              fieldWithPath("captions[].end").description("자막 종료 시간(초)"),
                              fieldWithPath("captions[].text").description("자막 텍스트"))));

          verify(recipeCaptionService).findByRecipeId(recipeId);

          var responseBody = response.extract().jsonPath();

          // 언어 코드 검증
          assertThat(responseBody.getString("lang_code")).isEqualTo("ko");

          // 자막 세그먼트 수 검증
          assertThat(responseBody.getList("captions")).hasSize(4);

          // 첫 번째 세그먼트 검증
          assertThat(responseBody.getDouble("captions[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("captions[0].end")).isEqualTo(3.5);
          assertThat(responseBody.getString("captions[0].text"))
              .isEqualTo("안녕하세요, 오늘은 김치찌개를 만들어보겠습니다");

          // 두 번째 세그먼트 검증
          assertThat(responseBody.getDouble("captions[1].start")).isEqualTo(3.5);
          assertThat(responseBody.getDouble("captions[1].end")).isEqualTo(7.0);
          assertThat(responseBody.getString("captions[1].text")).isEqualTo("먼저 김치 200그램을 준비해주세요");

          // 세 번째 세그먼트 검증
          assertThat(responseBody.getDouble("captions[2].start")).isEqualTo(7.0);
          assertThat(responseBody.getDouble("captions[2].end")).isEqualTo(10.5);
          assertThat(responseBody.getString("captions[2].text")).isEqualTo("돼지고기 150그램도 함께 준비합니다");

          // 네 번째 세그먼트 검증
          assertThat(responseBody.getDouble("captions[3].start")).isEqualTo(10.5);
          assertThat(responseBody.getDouble("captions[3].end")).isEqualTo(14.0);
          assertThat(responseBody.getString("captions[3].text")).isEqualTo("팬에 기름을 두르고 김치를 볶아주세요");
        }
      }

      @Nested
      @DisplayName("When - 영어 자막을 조회한다면")
      class WhenRequestingEnglishCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = mock(RecipeCaption.class);

          List<RecipeCaption.Segment> segments =
              List.of(
                  new RecipeCaption.Segment(
                      "Hello, today we're going to make kimchi stew", 0.0, 4.0),
                  new RecipeCaption.Segment("First, prepare 200 grams of kimchi", 4.0, 7.5),
                  new RecipeCaption.Segment("Also prepare 150 grams of pork", 7.5, 10.0));

          doReturn(segments).when(recipeCaption).getSegments();
          doReturn(LangCodeType.en).when(recipeCaption).getLangCode();
          doReturn(recipeCaption).when(recipeCaptionService).findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 영어 자막을 성공적으로 반환해야 한다")
        void thenShouldReturnEnglishCaption() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/caption", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("captions", hasSize(3));

          verify(recipeCaptionService).findByRecipeId(recipeId);

          var responseBody = response.extract().jsonPath();

          // 언어 코드 검증
          assertThat(responseBody.getString("lang_code")).isEqualTo("en");

          // 자막 세그먼트 수 검증
          assertThat(responseBody.getList("captions")).hasSize(3);

          // 영어 텍스트 검증
          assertThat(responseBody.getString("captions[0].text"))
              .isEqualTo("Hello, today we're going to make kimchi stew");
          assertThat(responseBody.getString("captions[1].text"))
              .isEqualTo("First, prepare 200 grams of kimchi");
          assertThat(responseBody.getString("captions[2].text"))
              .isEqualTo("Also prepare 150 grams of pork");
        }
      }

      @Nested
      @DisplayName("When - 단일 자막 세그먼트를 조회한다면")
      class WhenRequestingSingleSegmentCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = mock(RecipeCaption.class);

          List<RecipeCaption.Segment> segments =
              List.of(new RecipeCaption.Segment("간단한 요리 설명입니다", 0.0, 5.0));

          doReturn(segments).when(recipeCaption).getSegments();
          doReturn(LangCodeType.ko).when(recipeCaption).getLangCode();
          doReturn(recipeCaption).when(recipeCaptionService).findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 단일 자막 세그먼트를 성공적으로 반환해야 한다")
        void thenShouldReturnSingleSegmentCaption() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/caption", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("captions", hasSize(1));

          verify(recipeCaptionService).findByRecipeId(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("lang_code")).isEqualTo("ko");
          assertThat(responseBody.getList("captions")).hasSize(1);
          assertThat(responseBody.getDouble("captions[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("captions[0].end")).isEqualTo(5.0);
          assertThat(responseBody.getString("captions[0].text")).isEqualTo("간단한 요리 설명입니다");
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
      @DisplayName("When - 자막을 조회한다면")
      class WhenRequestingCaption {

        @BeforeEach
        void setUp() {
          doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_NOT_FOUND))
              .when(recipeCaptionService)
              .findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - RecipeCaptionException이 발생해야 한다")
        void thenShouldThrowRecipeCaptionException() {
          given()
              .contentType(ContentType.JSON)
              .get("/papi/v1/recipes/{recipeId}/caption", nonExistentRecipeId)
              .then()
              .status(HttpStatus.BAD_REQUEST);

          verify(recipeCaptionService).findByRecipeId(nonExistentRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 복잡한 다중 세그먼트 자막이 있는 레시피 ID가 주어졌을 때")
    class GivenComplexMultiSegmentRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 복잡한 자막을 조회한다면")
      class WhenRequestingComplexCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = mock(RecipeCaption.class);

          List<RecipeCaption.Segment> segments =
              List.of(
                  new RecipeCaption.Segment("오늘은 전통 한식인 불고기를 만들어보겠습니다", 0.0, 4.0),
                  new RecipeCaption.Segment("소고기 400그램, 양파 1개, 당근 반개를 준비해주세요", 4.0, 8.5),
                  new RecipeCaption.Segment("간장 3큰술, 설탕 1큰술, 참기름 1큰술도 필요합니다", 8.5, 13.0),
                  new RecipeCaption.Segment("먼저 소고기를 얇게 썰어서 양념에 재워주세요", 13.0, 17.5),
                  new RecipeCaption.Segment("양파와 당근도 적당한 크기로 썰어줍니다", 17.5, 21.0),
                  new RecipeCaption.Segment("팬에 고기를 넣고 중불에서 볶아주세요", 21.0, 25.0),
                  new RecipeCaption.Segment("고기가 익으면 야채를 넣고 함께 볶습니다", 25.0, 29.0),
                  new RecipeCaption.Segment("마지막으로 참기름을 뿌리고 접시에 담아내면 완성입니다", 29.0, 33.5));

          doReturn(segments).when(recipeCaption).getSegments();
          doReturn(LangCodeType.ko).when(recipeCaption).getLangCode();
          doReturn(recipeCaption).when(recipeCaptionService).findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 복잡한 자막 세그먼트들을 성공적으로 반환해야 한다")
        void thenShouldReturnAllComplexCaptionSegments() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/caption", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("captions", hasSize(8));

          verify(recipeCaptionService).findByRecipeId(recipeId);

          var responseBody = response.extract().jsonPath();

          // 전체 세그먼트 수 검증
          assertThat(responseBody.getList("captions")).hasSize(8);
          assertThat(responseBody.getString("lang_code")).isEqualTo("ko");

          // 시간 순서 검증 (각 세그먼트가 이전 세그먼트 종료 시간과 연결되는지)
          assertThat(responseBody.getDouble("captions[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("captions[0].end")).isEqualTo(4.0);
          assertThat(responseBody.getDouble("captions[1].start")).isEqualTo(4.0);
          assertThat(responseBody.getDouble("captions[1].end")).isEqualTo(8.5);

          // 특정 단계별 텍스트 검증
          assertThat(responseBody.getString("captions[0].text")).contains("불고기를 만들어보겠습니다");
          assertThat(responseBody.getString("captions[1].text")).contains("소고기 400그램");
          assertThat(responseBody.getString("captions[3].text")).contains("소고기를 얇게 썰어서");
          assertThat(responseBody.getString("captions[7].text")).contains("완성입니다");

          // 마지막 세그먼트 시간 검증
          assertThat(responseBody.getDouble("captions[7].start")).isEqualTo(29.0);
          assertThat(responseBody.getDouble("captions[7].end")).isEqualTo(33.5);
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 자막이 있는 레시피 ID가 주어졌을 때")
    class GivenEmptyCaptionRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 빈 자막을 조회한다면")
      class WhenRequestingEmptyCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption = mock(RecipeCaption.class);

          // 빈 세그먼트 목록
          List<RecipeCaption.Segment> emptySegments = List.of();

          doReturn(emptySegments).when(recipeCaption).getSegments();
          doReturn(LangCodeType.ko).when(recipeCaption).getLangCode();
          doReturn(recipeCaption).when(recipeCaptionService).findByRecipeId(any(UUID.class));
          doReturn(true).when(recipeInfoService).exists(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 자막 목록을 반환해야 한다")
        void thenShouldReturnEmptyCaptionList() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/{recipeId}/caption", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("captions", hasSize(0));

          verify(recipeCaptionService).findByRecipeId(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("lang_code")).isEqualTo("ko");
          assertThat(responseBody.getList("captions")).isEmpty();
        }
      }
    }
  }
}
