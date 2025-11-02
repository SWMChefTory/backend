package com.cheftory.api.recipeinfo;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipeinfo.briefing.RecipeBriefing;
import com.cheftory.api.recipeinfo.category.RecipeCategory;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredient;
import com.cheftory.api.recipeinfo.model.FullRecipe;
import com.cheftory.api.recipeinfo.model.RecipeCategoryCount;
import com.cheftory.api.recipeinfo.model.RecipeCategoryCounts;
import com.cheftory.api.recipeinfo.model.RecipeCreationTarget;
import com.cheftory.api.recipeinfo.model.RecipeHistoryOverview;
import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.model.RecipeInfoCuisineType;
import com.cheftory.api.recipeinfo.model.RecipeInfoRecommendType;
import com.cheftory.api.recipeinfo.model.RecipeInfoVideoQuery;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaType;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Recipe Controller")
public class RecipeInfoControllerTest extends RestDocsTest {

  private RecipeInfoService recipeInfoService;
  private RecipeInfoController controller;
  private GlobalExceptionHandler exceptionHandler;
  private UserArgumentResolver userArgumentResolver;

  @BeforeEach
  void setUp() {
    recipeInfoService = mock(RecipeInfoService.class);
    controller = new RecipeInfoController(recipeInfoService);
    exceptionHandler = new GlobalExceptionHandler();
    userArgumentResolver = new UserArgumentResolver();

    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(exceptionHandler)
            .withArgumentResolver(userArgumentResolver)
            .build();
  }

  @Nested
  @DisplayName("레시피 생성")
  class CreateRecipe {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Then - 레시피 생성 요청을 성공적으로 처리해야 한다")
    void thenShouldCreateRecipeSuccessfully() {
      var recipeId = UUID.randomUUID();
      var requestBody =
          """
          {
            "video_url": "https://www.youtube.com/watch?v=example"
          }
          """;

      doReturn(recipeId).when(recipeInfoService).create(any(RecipeCreationTarget.class));

      var response =
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .body(requestBody)
              .post("/api/v1/recipes")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      requestAccessTokenFields(),
                      requestFields(fieldWithPath("video_url").description("레시피 비디오 URL")),
                      responseFields(fieldWithPath("recipe_id").description("생성된 레시피 ID"))));

      verify(recipeInfoService).create(any(RecipeCreationTarget.User.class));

      var responseBody = response.extract().jsonPath();
      assertThat(responseBody.getUUID("recipe_id")).isEqualTo(recipeId);
    }
  }

  @Nested
  @DisplayName("레시피 최근 기록 조회")
  class GetRecentRecipes {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 레시피 최근 기록을 조회한다면")
      class WhenRequestingRecentRecipes {

        private Page<RecipeHistoryOverview> recentRecipes;
        private RecipeHistoryOverview recentRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta meta;
        private RecipeHistory viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recentRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(Recipe.class);
          meta = mock(RecipeYoutubeMeta.class);
          viewStatus = mock(RecipeHistory.class);
          page = 0;
          pageable = Pageable.ofSize(10);

          // RecipeHistoryOverview mock 설정
          doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(recentRecipe).getViewedAt();
          doReturn(120).when(recentRecipe).getLastPlaySeconds();
          doReturn(recipeId).when(recentRecipe).getRecipeId();
          doReturn("Sample Recipe Title").when(recentRecipe).getVideoTitle();
          doReturn(URI.create("https://example.com/thumbnail.jpg"))
              .when(recentRecipe)
              .getThumbnailUrl();
          doReturn("sample_video_id").when(recentRecipe).getVideoId();
          doReturn(120).when(recentRecipe).getVideoSeconds();
          doReturn(RecipeStatus.IN_PROGRESS).when(recentRecipe).getRecipeStatus();
          doReturn(List.of("한식")).when(recentRecipe).getTags();
          doReturn("레시피 설명").when(recentRecipe).getDescription();
          doReturn(2).when(recentRecipe).getServings();
          doReturn(30).when(recentRecipe).getCookTime();
          doReturn(LocalDateTime.of(2024, 1, 14, 10, 30, 0))
              .when(recentRecipe)
              .getRecipeCreatedAt();

          recentRecipes = new PageImpl<>(List.of(recentRecipe), pageable, 1);
          doReturn(recentRecipes)
              .when(recipeInfoService)
              .getRecents(any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 레시피 최근 기록을 성공적으로 반환해야 한다")
        void thenShouldReturnRecentRecipes() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/recent")
                  .then()
                  .status(HttpStatus.OK)
                  .body("recent_recipes", hasSize(recentRecipes.getContent().size()))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("recent_recipes").description("사용자의 최근 레시피 접근 기록"),
                              fieldWithPath("recent_recipes[].viewed_at").description("레시피 접근 시간"),
                              fieldWithPath("recent_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("recent_recipes[].recipe_id").description("레시피 ID"),
                              fieldWithPath("recent_recipes[].video_id").description("레시피 비디오 ID"),
                              fieldWithPath("recent_recipes[].recipe_title")
                                  .description("레시피 비디오 제목"),
                              fieldWithPath("recent_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("recent_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("recent_recipes[].description").description("레시피 설명"),
                              fieldWithPath("recent_recipes[].cook_time").description("조리 시간(분)"),
                              fieldWithPath("recent_recipes[].servings").description("인분"),
                              fieldWithPath("recent_recipes[].created_at").description("레시피 생성 일시"),
                              fieldWithPath("recent_recipes[].tags").description("태그 목록"),
                              fieldWithPath("recent_recipes[].tags[].name").description("태그 이름"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              enumFields(
                                  "recent_recipes[].recipe_status",
                                  "레시피의 현재 상태: ",
                                  RecipeStatus.class),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getRecents(userId, page);

          var responseBody = response.extract().jsonPath();
          var recentRecipesList = responseBody.getList("recent_recipes");

          assertThat(recentRecipesList).hasSize(1);
          assertThat(responseBody.getUUID("recent_recipes[0].recipe_id")).isEqualTo(recipeId);
          assertThat(responseBody.getString("recent_recipes[0].video_id"))
              .isEqualTo("sample_video_id");
          assertThat(responseBody.getString("recent_recipes[0].recipe_title"))
              .isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("recent_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getString("recent_recipes[0].viewed_at"))
              .isEqualTo("2024-01-15T10:30:00");
          assertThat(responseBody.getInt("recent_recipes[0].last_play_seconds")).isEqualTo(120);
          assertThat(responseBody.getInt("recent_recipes[0].video_seconds")).isEqualTo(120);
          assertThat(responseBody.getString("current_page")).isEqualTo("0");
          assertThat(responseBody.getString("total_pages")).isEqualTo("1");
          assertThat(responseBody.getString("total_elements")).isEqualTo("1");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
          assertThat(responseBody.getString("recent_recipes[0].recipe_status"))
              .isEqualTo(RecipeStatus.IN_PROGRESS.name());
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 사용자 ID가 주어졌을 때")
    class GivenNonExistentUserId {

      private UUID userId;
      private Integer page;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        page = 0;
        pageable = Pageable.ofSize(10);
        userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 레시피 최근 기록을 조회한다면")
      class WhenRequestingRecentRecipes {

        @BeforeEach
        void setUp() {
          doReturn(new PageImpl<>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .getRecents(userId, page);
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyList() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/recent")
              .then()
              .status(HttpStatus.OK)
              .body("recent_recipes", hasSize(0));

          verify(recipeInfoService).getRecents(userId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 전체 정보 조회")
  class GetRecipeDetail {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeIdAndUserId {

      private UUID userId;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 레시피 전체 정보를 조회한다면")
      class WhenRequestingRecipeDetail {

        private FullRecipe fullRecipe;
        private RecipeYoutubeMeta youtubeMeta;
        private List<RecipeIngredient> ingredients;
        private RecipeHistory viewStatus;
        private List<RecipeStep> recipeSteps;
        private RecipeDetailMeta detailMeta;
        private List<RecipeTag> tags;
        private List<RecipeProgress> progresses;
        private List<RecipeBriefing> briefings;
        private Recipe recipe;
        private UUID viewStatusId;
        private UUID stepId;

        @BeforeEach
        void setUp() {
          fullRecipe = mock(FullRecipe.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          viewStatus = mock(RecipeHistory.class);

          doReturn(RecipeStatus.SUCCESS).when(recipe).getRecipeStatus();
          doReturn(recipe).when(fullRecipe).getRecipe();

          doReturn("sample_video_id").when(youtubeMeta).getVideoId();
          doReturn("Sample Recipe Title").when(youtubeMeta).getTitle();
          doReturn(URI.create("https://example.com/thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(120).when(youtubeMeta).getVideoSeconds();

          RecipeIngredient ingredient = mock(RecipeIngredient.class);
          doReturn("토마토").when(ingredient).getName();
          doReturn(2).when(ingredient).getAmount();
          doReturn("개").when(ingredient).getUnit();
          ingredients = List.of(ingredient);

          detailMeta = mock(RecipeDetailMeta.class);
          doReturn("맛있는 토마토 요리입니다").when(detailMeta).getDescription();
          doReturn(2).when(detailMeta).getServings();
          doReturn(30).when(detailMeta).getCookTime();

          RecipeStep recipeStep = mock(RecipeStep.class);
          var stepDetail = RecipeStep.Detail.builder().text("Step 1 detail").start(0.0).build();

          stepId = UUID.randomUUID();
          doReturn(stepId).when(recipeStep).getId();
          doReturn(1).when(recipeStep).getStepOrder();
          doReturn(List.of(stepDetail)).when(recipeStep).getDetails();
          doReturn(0.0).when(recipeStep).getStart();
          doReturn("Step 1 Title").when(recipeStep).getSubtitle();
          recipeSteps = List.of(recipeStep);

          RecipeTag recipeTag = mock(RecipeTag.class);
          doReturn("한식").when(recipeTag).getTag();
          tags = List.of(recipeTag);

          RecipeBriefing recipeBriefing = mock(RecipeBriefing.class);
          doReturn("이 요리는 맛있습니다").when(recipeBriefing).getContent();
          briefings = List.of(recipeBriefing);

          RecipeProgress progress = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.FINISHED).when(progress).getStep();
          doReturn(RecipeProgressDetail.FINISHED).when(progress).getDetail();
          progresses = List.of(progress);

          doReturn(youtubeMeta).when(fullRecipe).getRecipeYoutubeMeta();
          doReturn(ingredients).when(fullRecipe).getRecipeIngredients();
          doReturn(recipeSteps).when(fullRecipe).getRecipeSteps();
          doReturn(viewStatus).when(fullRecipe).getRecipeHistory();
          doReturn(detailMeta).when(fullRecipe).getRecipeDetailMeta();
          doReturn(tags).when(fullRecipe).getRecipeTags();
          doReturn(progresses).when(fullRecipe).getRecipeProgresses();
          doReturn(briefings).when(fullRecipe).getRecipeBriefings();

          viewStatusId = UUID.randomUUID();
          doReturn(viewStatusId).when(viewStatus).getId();
          doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(60).when(viewStatus).getLastPlaySeconds();
          doReturn(LocalDateTime.of(2024, 1, 14, 10, 30, 0)).when(viewStatus).getCreatedAt();

          doReturn(fullRecipe)
              .when(recipeInfoService)
              .getFullRecipe(any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 레시피 전체 정보를 성공적으로 반환해야 한다")
        void thenShouldReturnRecipeDetail() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .get("/api/v1/recipes/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              enumFields("recipe_status", "레시피의 현재 상태: ", RecipeStatus.class),
                              fieldWithPath("video_info").description("레시피 비디오 정보"),
                              fieldWithPath("video_info.video_id").description("레시피 비디오 ID"),
                              fieldWithPath("video_info.video_title").description("레시피 비디오 제목"),
                              fieldWithPath("video_info.video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("video_info.video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("recipe_ingredient").description("레시피 재료 목록"),
                              fieldWithPath("recipe_ingredient[].name").description("재료 이름"),
                              fieldWithPath("recipe_ingredient[].amount").description("재료 양"),
                              fieldWithPath("recipe_ingredient[].unit").description("재료 단위"),
                              fieldWithPath("recipe_progresses").description("레시피 진행 상태 목록"),
                              enumFields(
                                      "recipe_progresses[].step",
                                      "레시피 요약 완료 단계: ",
                                      RecipeProgressStep.class)
                                  .description("진행 단계"),
                              enumFields(
                                      "recipe_progresses[].detail",
                                      "레시피 상세 완료 단계: ",
                                      RecipeProgressDetail.class)
                                  .description("진행 상세"),
                              fieldWithPath("recipe_steps").description("레시피 단계 정보"),
                              fieldWithPath("recipe_steps[].id").description("레시피 단계 ID"),
                              fieldWithPath("recipe_steps[].step_order").description("레시피 단계 순서"),
                              fieldWithPath("recipe_steps[].subtitle").description("레시피 단계 제목"),
                              fieldWithPath("recipe_steps[].details").description("레시피 단계 설명 목록"),
                              fieldWithPath("recipe_steps[].details[].text")
                                  .description("레시피 단계 설명 텍스트"),
                              fieldWithPath("recipe_steps[].details[].start")
                                  .description("레시피 단계 설명 시작 시간"),
                              fieldWithPath("recipe_steps[].start_time")
                                  .description("레시피 단계 시작 시간"),
                              fieldWithPath("view_status").description("레시피 시청 상태 정보"),
                              fieldWithPath("view_status.id").description("레시피 시청 상태 ID"),
                              fieldWithPath("view_status.viewed_at").description("레시피 마지막 시청 시간"),
                              fieldWithPath("view_status.last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("view_status.created_at")
                                  .description("레시피 시청 상태 생성 시간"),
                              fieldWithPath("recipe_detail_meta")
                                  .description("레시피 상세 메타데이터")
                                  .optional(),
                              fieldWithPath("recipe_detail_meta.description")
                                  .description("레시피 설명")
                                  .optional(),
                              fieldWithPath("recipe_detail_meta.servings")
                                  .description("인분")
                                  .optional(),
                              fieldWithPath("recipe_detail_meta.cookingTime")
                                  .description("조리 시간(분)")
                                  .optional(),
                              fieldWithPath("recipe_tags").description("레시피 태그 목록"),
                              fieldWithPath("recipe_tags[].name").description("태그 이름"),
                              fieldWithPath("recipe_briefings").description("레시피 브리핑 목록"),
                              fieldWithPath("recipe_briefings[].content").description("브리핑 내용"))));

          verify(recipeInfoService).getFullRecipe(recipeId, userId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status"))
              .isEqualTo(RecipeStatus.SUCCESS.name());
          assertThat(responseBody.getString("video_info.video_id")).isEqualTo("sample_video_id");
          assertThat(responseBody.getString("video_info.video_title"))
              .isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("video_info.video_thumbnail_url"))
              .isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getInt("video_info.video_seconds")).isEqualTo(120);

          assertThat(responseBody.getUUID("view_status.id")).isEqualTo(viewStatusId);
          assertThat(responseBody.getString("view_status.viewed_at"))
              .isEqualTo("2024-01-15T10:30:00");
          assertThat(responseBody.getInt("view_status.last_play_seconds")).isEqualTo(60);
          assertThat(responseBody.getString("view_status.created_at"))
              .isEqualTo("2024-01-14T10:30:00");

          assertThat(responseBody.getUUID("recipe_steps[0].id")).isEqualTo(stepId);
          assertThat(responseBody.getInt("recipe_steps[0].step_order")).isEqualTo(1);
          assertThat(responseBody.getString("recipe_steps[0].subtitle")).isEqualTo("Step 1 Title");
          assertThat(responseBody.getString("recipe_steps[0].details[0].text"))
              .isEqualTo("Step 1 detail");
          assertThat(responseBody.getDouble("recipe_steps[0].details[0].start")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("recipe_steps[0].start_time")).isEqualTo(0.0);

          assertThat(responseBody.getString("recipe_ingredient[0].name")).isEqualTo("토마토");
          assertThat(responseBody.getInt("recipe_ingredient[0].amount")).isEqualTo(2);
          assertThat(responseBody.getString("recipe_ingredient[0].unit")).isEqualTo("개");

          assertThat(responseBody.getString("recipe_detail_meta.description"))
              .isEqualTo("맛있는 토마토 요리입니다");
          assertThat(responseBody.getInt("recipe_detail_meta.servings")).isEqualTo(2);
          assertThat(responseBody.getInt("recipe_detail_meta.cookingTime")).isEqualTo(30);

          assertThat(responseBody.getString("recipe_tags[0].name")).isEqualTo("한식");

          assertThat(responseBody.getString("recipe_progresses[0].step"))
              .isEqualTo(RecipeProgressStep.FINISHED.name());
          assertThat(responseBody.getString("recipe_progresses[0].detail"))
              .isEqualTo(RecipeProgressDetail.FINISHED.name());
          assertThat(responseBody.getString("recipe_briefings[0].content"))
              .isEqualTo("이 요리는 맛있습니다");
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID userId;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 레시피 전체 정보를 조회한다면")
      class WhenRequestingRecipeDetail {

        @BeforeEach
        void setUp() {
          doReturn(null).when(recipeInfoService).getFullRecipe(recipeId, userId);
        }

        @Test
        @DisplayName("Then - 400 Bad Request를 반환해야 한다")
        void thenShouldReturnNotFound() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/recipes/{recipeId}", recipeId)
              .then()
              .status(HttpStatus.BAD_REQUEST);

          verify(recipeInfoService).getFullRecipe(recipeId, userId);
        }
      }
    }
  }

  @Nested
  @DisplayName("추천 레시피 조회")
  class GetRecommendRecipes {

    @Nested
    @DisplayName("Given - 추천 레시피 요청이 주어졌을 때")
    class GivenRecommendRecipesRequest {

      private UUID userId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 추천 레시피를 조회한다면")
      class WhenRequestingRecommendRecipes {

        private Page<RecipeOverview> recipes;
        private UUID recipeId;
        private Recipe recipe;
        private RecipeOverview recipeOverview; // RecipeOverview 객체 추가
        private Pageable pageable;
        private RecipeYoutubeMeta youtubeMeta;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recipe = mock(Recipe.class);
          recipeOverview = mock(RecipeOverview.class); // RecipeOverview 모킹
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          pageable = Pageable.ofSize(10);

          // RecipeOverview mock 설정
          doReturn(recipeId).when(recipeOverview).getRecipeId();
          doReturn("Sample Recipe Title").when(recipeOverview).getVideoTitle();
          doReturn(URI.create("https://example.com/thumbnail.jpg"))
              .when(recipeOverview)
              .getThumbnailUrl();
          doReturn("sample_video_id").when(recipeOverview).getVideoId();
          doReturn(180).when(recipeOverview).getVideoSeconds();
          doReturn(true).when(recipeOverview).getIsViewed();
          doReturn(List.of("한식", "간단요리")).when(recipeOverview).getTags();
          doReturn("맛있는 레시피입니다").when(recipeOverview).getDescription();
          doReturn(2).when(recipeOverview).getServings();
          doReturn(30).when(recipeOverview).getCookTime();

          recipes = new PageImpl<>(List.of(recipeOverview), pageable, 1);
          doReturn(recipes)
              .when(recipeInfoService)
              .getRecommendRecipes(
                  eq(RecipeInfoRecommendType.POPULAR),
                  any(UUID.class),
                  any(Integer.class),
                  any(RecipeInfoVideoQuery.class));
        }

        @Test
        @DisplayName("Then - 추천 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnRecommendRecipes() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/recommend")
                  .then()
                  .status(HttpStatus.OK)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("recommend_recipes").description("추천 레시피 목록"),
                              fieldWithPath("recommend_recipes[].recipe_id").description("레시피 ID"),
                              fieldWithPath("recommend_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("recommend_recipes[].tags").description("태그 목록"),
                              fieldWithPath("recommend_recipes[].tags[].name").description("태그 이름"),
                              fieldWithPath("recommend_recipes[].is_viewed")
                                  .description("사용자가 해당 레시피를 본 적이 있는지 여부"),
                              fieldWithPath("recommend_recipes[].description").description("레시피 설명"),
                              fieldWithPath("recommend_recipes[].servings").description("인분"),
                              fieldWithPath("recommend_recipes[].cooking_time")
                                  .description("조리 시간(분)"),
                              fieldWithPath("recommend_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("recommend_recipes[].video_title")
                                  .description("레시피 비디오 제목"),
                              fieldWithPath("recommend_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("recommend_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService)
              .getRecommendRecipes(
                  eq(RecipeInfoRecommendType.POPULAR),
                  any(UUID.class),
                  any(Integer.class),
                  any(RecipeInfoVideoQuery.class));

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("recommend_recipes")).hasSize(1);
          assertThat(responseBody.getString("recommend_recipes[0].recipe_id"))
              .isEqualTo(recipeId.toString());
          assertThat(responseBody.getString("recommend_recipes[0].recipe_title"))
              .isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getList("recommend_recipes[0].tags")).hasSize(2);
          assertThat(responseBody.getString("recommend_recipes[0].tags[0].name")).isEqualTo("한식");
          assertThat(responseBody.getString("recommend_recipes[0].tags[1].name")).isEqualTo("간단요리");
          assertThat(responseBody.getBoolean("recommend_recipes[0].is_viewed")).isEqualTo(true);
          assertThat(responseBody.getString("recommend_recipes[0].description"))
              .isEqualTo("맛있는 레시피입니다");
          assertThat(responseBody.getInt("recommend_recipes[0].servings")).isEqualTo(2);
          assertThat(responseBody.getInt("recommend_recipes[0].cooking_time")).isEqualTo(30);
          assertThat(responseBody.getString("recommend_recipes[0].video_id"))
              .isEqualTo("sample_video_id");
          assertThat(responseBody.getString("recommend_recipes[0].video_title"))
              .isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("recommend_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getInt("recommend_recipes[0].video_seconds")).isEqualTo(180);
          assertThat(responseBody.getString("current_page")).isEqualTo("0");
          assertThat(responseBody.getString("total_pages")).isEqualTo("1");
          assertThat(responseBody.getString("total_elements")).isEqualTo("1");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
        }
      }
    }

    @Nested
    @DisplayName("Given - 추천 레시피가 없을 때")
    class GivenNoRecommendRecipes {

      private UUID userId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 추천 레시피를 조회한다면")
      class WhenRequestingRecommendRecipes {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
          pageable = Pageable.ofSize(10);
          doReturn(new PageImpl<RecipeOverview>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .getRecommendRecipes(
                  eq(RecipeInfoRecommendType.POPULAR),
                  any(UUID.class),
                  any(Integer.class),
                  any(RecipeInfoVideoQuery.class));
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyList() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/recommend")
              .then()
              .status(HttpStatus.OK)
              .body("recommend_recipes", hasSize(0));

          verify(recipeInfoService)
              .getRecommendRecipes(
                  eq(RecipeInfoRecommendType.POPULAR),
                  any(UUID.class),
                  any(Integer.class),
                  any(RecipeInfoVideoQuery.class));
        }
      }
    }

    @Nested
    @DisplayName("Given - query 파라미터가 주어졌을 때")
    class GivenQueryParameter {

      private UUID userId;
      private Integer page;
      private Page<RecipeOverview> recipes;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        recipes = new PageImpl<>(List.of());
      }

      @Test
      @DisplayName("query=ALL이면 RecipeInfoVideoQuery.ALL로 서비스를 호출한다")
      void callsServiceWithAllQuery() {
        doReturn(recipes)
            .when(recipeInfoService)
            .getRecommendRecipes(
                eq(RecipeInfoRecommendType.POPULAR),
                any(UUID.class),
                any(Integer.class),
                any(RecipeInfoVideoQuery.class));

        given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .param("page", page)
            .param("query", "ALL")
            .get("/api/v1/recipes/recommend")
            .then()
            .status(HttpStatus.OK);

        verify(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.ALL);
      }

      @Test
      @DisplayName("query=NORMAL이면 RecipeInfoVideoQuery.NORMAL로 서비스를 호출한다")
      void callsServiceWithNormalQuery() {
        doReturn(recipes)
            .when(recipeInfoService)
            .getRecommendRecipes(
                eq(RecipeInfoRecommendType.POPULAR),
                any(UUID.class),
                any(Integer.class),
                any(RecipeInfoVideoQuery.class));

        given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .param("page", page)
            .param("query", "NORMAL")
            .get("/api/v1/recipes/recommend")
            .then()
            .status(HttpStatus.OK);

        verify(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.NORMAL);
      }

      @Test
      @DisplayName("query=SHORTS이면 RecipeInfoVideoQuery.SHORTS로 서비스를 호출한다")
      void callsServiceWithShortsQuery() {
        doReturn(recipes)
            .when(recipeInfoService)
            .getRecommendRecipes(
                eq(RecipeInfoRecommendType.POPULAR),
                any(UUID.class),
                any(Integer.class),
                any(RecipeInfoVideoQuery.class));

        given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .param("page", page)
            .param("query", "SHORTS")
            .get("/api/v1/recipes/recommend")
            .then()
            .status(HttpStatus.OK);

        verify(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.SHORTS);
      }

      @Test
      @DisplayName("query 파라미터가 없으면 기본값 ALL로 서비스를 호출한다")
      void callsServiceWithDefaultAllQuery() {
        doReturn(recipes)
            .when(recipeInfoService)
            .getRecommendRecipes(
                eq(RecipeInfoRecommendType.POPULAR),
                any(UUID.class),
                any(Integer.class),
                any(RecipeInfoVideoQuery.class));

        given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .param("page", page)
            .get("/api/v1/recipes/recommend")
            .then()
            .status(HttpStatus.OK);

        verify(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.ALL);
      }

      @Test
      @DisplayName("SHORTS 타입 비디오 레시피가 정상적으로 반환된다")
      void returnsShortsRecipe() {
        UUID recipeId = UUID.randomUUID();
        RecipeOverview recipeOverview = mock(RecipeOverview.class);

        // RecipeOverview mock 설정
        doReturn(recipeId).when(recipeOverview).getRecipeId();
        doReturn("30초 요리 팁").when(recipeOverview).getVideoTitle();
        doReturn(URI.create("https://example.com/shorts/thumbnail.jpg"))
            .when(recipeOverview)
            .getThumbnailUrl();
        doReturn("shorts123").when(recipeOverview).getVideoId();
        doReturn(30).when(recipeOverview).getVideoSeconds();
        doReturn(false).when(recipeOverview).getIsViewed();
        doReturn(List.of("간편식")).when(recipeOverview).getTags();
        doReturn("30초 요리 팁입니다").when(recipeOverview).getDescription();
        doReturn(1).when(recipeOverview).getServings();
        doReturn(5).when(recipeOverview).getCookTime();

        Page<RecipeOverview> shortsRecipes = new PageImpl<>(List.of(recipeOverview));
        doReturn(shortsRecipes)
            .when(recipeInfoService)
            .getRecommendRecipes(
                eq(RecipeInfoRecommendType.POPULAR),
                any(UUID.class),
                any(Integer.class),
                any(RecipeInfoVideoQuery.class));

        var response =
            given()
                .contentType(ContentType.JSON)
                .attribute("userId", userId.toString())
                .header("Authorization", "Bearer accessToken")
                .param("page", page)
                .param("query", "SHORTS")
                .get("/api/v1/recipes/recommend")
                .then()
                .status(HttpStatus.OK);

        var responseBody = response.extract().jsonPath();
        assertThat(responseBody.getString("recommend_recipes[0].recipe_title"))
            .isEqualTo("30초 요리 팁");
        assertThat(responseBody.getString("recommend_recipes[0].video_id")).isEqualTo("shorts123");
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 조회")
  class GetCategorizedRecipes {

    @Nested
    @DisplayName("Given - 유효한 카테고리 ID와 사용자 ID가 주어졌을 때")
    class GivenValidCategoryIdAndUserId {

      private UUID userId;
      private UUID categoryId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피를 조회한다면")
      class WhenRequestingCategorizedRecipes {

        private Page<RecipeHistoryOverview> categorizedRecipes;
        private RecipeHistoryOverview categorizedRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta recipeDetailMeta;
        private List<String> tags;
        private RecipeHistory viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          categorizedRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          recipeDetailMeta = mock(RecipeDetailMeta.class);
          viewStatus = mock(RecipeHistory.class);

          // RecipeHistoryOverview mock 설정
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0)).when(categorizedRecipe).getViewedAt();
          doReturn(90).when(categorizedRecipe).getLastPlaySeconds();
          doReturn(recipeId).when(categorizedRecipe).getRecipeId();
          doReturn("Categorized Recipe Title").when(categorizedRecipe).getVideoTitle();
          doReturn(URI.create("https://example.com/categorized_thumbnail.jpg"))
              .when(categorizedRecipe)
              .getThumbnailUrl();
          doReturn("categorized_video_id").when(categorizedRecipe).getVideoId();
          doReturn(180).when(categorizedRecipe).getVideoSeconds();
          doReturn(categoryId).when(categorizedRecipe).getRecipeCategoryId();
          doReturn("Categorized Recipe Description").when(categorizedRecipe).getDescription();
          doReturn(2).when(categorizedRecipe).getServings();
          doReturn(30).when(categorizedRecipe).getCookTime();
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0))
              .when(categorizedRecipe)
              .getRecipeCreatedAt();

          tags = List.of("한식");
          doReturn(tags).when(categorizedRecipe).getTags();
          categorizedRecipes = new PageImpl<>(List.of(categorizedRecipe), pageable, 1);
          doReturn(categorizedRecipes)
              .when(recipeInfoService)
              .getCategorized(any(UUID.class), any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 카테고리별 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnCategorizedRecipes() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("categorized_recipes", hasSize(categorizedRecipes.getContent().size()))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          pathParameters(
                              parameterWithName("recipe_category_id")
                                  .description("조회할 레시피 카테고리 ID")),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("categorized_recipes").description("카테고리별 레시피 목록"),
                              fieldWithPath("categorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("categorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("categorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("categorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("categorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("categorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("categorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("categorized_recipes[].category_id")
                                  .description("레시피 카테고리 ID"),
                              fieldWithPath("categorized_recipes[].description")
                                  .description("레시피 설명"),
                              fieldWithPath("categorized_recipes[].servings").description("레시피 인분"),
                              fieldWithPath("categorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분)"),
                              fieldWithPath("categorized_recipes[].created_at")
                                  .description("레시피 생성 시간"),
                              fieldWithPath("categorized_recipes[].tags").description("레시피 태그 목록"),
                              fieldWithPath("categorized_recipes[].tags[].name")
                                  .description("태그 이름"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getCategorized(userId, categoryId, page);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getUUID("categorized_recipes[0].recipe_id")).isEqualTo(recipeId);
          assertThat(responseBody.getString("categorized_recipes[0].recipe_title"))
              .isEqualTo("Categorized Recipe Title");
          assertThat(responseBody.getString("categorized_recipes[0].video_id"))
              .isEqualTo("categorized_video_id");
          assertThat(responseBody.getString("categorized_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/categorized_thumbnail.jpg");
          assertThat(responseBody.getInt("categorized_recipes[0].video_seconds")).isEqualTo(180);
          assertThat(responseBody.getString("categorized_recipes[0].viewed_at"))
              .isEqualTo("2024-01-20T14:30:00");
          assertThat(responseBody.getInt("categorized_recipes[0].last_play_seconds")).isEqualTo(90);
          assertThat(responseBody.getUUID("categorized_recipes[0].category_id"))
              .isEqualTo(categoryId);
          assertThat(responseBody.getString("categorized_recipes[0].description"))
              .isEqualTo("Categorized Recipe Description");
          assertThat(responseBody.getInt("categorized_recipes[0].servings")).isEqualTo(2);
          assertThat(responseBody.getInt("categorized_recipes[0].cook_time")).isEqualTo(30);
          assertThat(responseBody.getString("categorized_recipes[0].created_at"))
              .isEqualTo("2024-01-20T14:30:00");
          assertThat(responseBody.getString("categorized_recipes[0].tags[0].name")).isEqualTo("한식");
        }

        @Test
        @DisplayName("Then - DetailMeta가 null이면 해당 필드는 null로 내려간다")
        void thenDetailMetaNullShouldReturnNullFields() {
          // DetailMeta가 null인 경우를 위한 새로운 mock 설정
          RecipeHistoryOverview nullDetailMetaRecipe = mock(RecipeHistoryOverview.class);
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0))
              .when(nullDetailMetaRecipe)
              .getViewedAt();
          doReturn(90).when(nullDetailMetaRecipe).getLastPlaySeconds();
          doReturn(recipeId).when(nullDetailMetaRecipe).getRecipeId();
          doReturn("Categorized Recipe Title").when(nullDetailMetaRecipe).getVideoTitle();
          doReturn(URI.create("https://example.com/categorized_thumbnail.jpg"))
              .when(nullDetailMetaRecipe)
              .getThumbnailUrl();
          doReturn("categorized_video_id").when(nullDetailMetaRecipe).getVideoId();
          doReturn(180).when(nullDetailMetaRecipe).getVideoSeconds();
          doReturn(categoryId).when(nullDetailMetaRecipe).getRecipeCategoryId();
          doReturn(null).when(nullDetailMetaRecipe).getDescription();
          doReturn(null).when(nullDetailMetaRecipe).getServings();
          doReturn(null).when(nullDetailMetaRecipe).getCookTime();
          doReturn(null).when(nullDetailMetaRecipe).getRecipeCreatedAt();
          doReturn(tags).when(nullDetailMetaRecipe).getTags();

          Page<RecipeHistoryOverview> nullDetailMetaRecipes =
              new PageImpl<>(List.of(nullDetailMetaRecipe), pageable, 1);
          doReturn(nullDetailMetaRecipes)
              .when(recipeInfoService)
              .getCategorized(any(UUID.class), any(UUID.class), any(Integer.class));

          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("categorized_recipes", hasSize(1))
                  .body("categorized_recipes[0].description", nullValue())
                  .body("categorized_recipes[0].servings", nullValue())
                  .body("categorized_recipes[0].cook_time", nullValue())
                  .body("categorized_recipes[0].created_at", nullValue())
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("categorized_recipes").description("미분류 레시피 목록"),
                              fieldWithPath("categorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("categorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("categorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("categorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("categorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("categorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("categorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("categorized_recipes[].category_id")
                                  .description("레시피 카테고리 ID"),
                              fieldWithPath("categorized_recipes[].description")
                                  .description("레시피 설명 (nullable)"),
                              fieldWithPath("categorized_recipes[].servings")
                                  .description("레시피 인분 (nullable)"),
                              fieldWithPath("categorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분) (nullable)"),
                              fieldWithPath("categorized_recipes[].created_at")
                                  .description("레시피 생성 시간 (nullable)"),
                              fieldWithPath("categorized_recipes[].tags").description("레시피 태그 목록"),
                              fieldWithPath("categorized_recipes[].tags[].name")
                                  .description("태그 이름"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getCategorized(userId, categoryId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("categorized_recipes[0].description")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].servings")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].cook_time")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].created_at")).isNull();
        }

        @Test
        @DisplayName("Then - Tags가 null이면 해당 필드는 null로 내려간다")
        void thenTagsNullShouldReturnNullFields() {
          // Tags를 null로 응답
          doReturn(null).when(categorizedRecipe).getTags();

          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
                  .then()
                  .status(HttpStatus.OK)
                  .body("categorized_recipes", hasSize(categorizedRecipes.getContent().size()))
                  .body("categorized_recipes[0].tags", nullValue())
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("categorized_recipes").description("미분류 레시피 목록"),
                              fieldWithPath("categorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("categorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("categorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("categorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("categorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("categorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("categorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("categorized_recipes[].category_id")
                                  .description("레시피 카테고리 ID"),
                              fieldWithPath("categorized_recipes[].description")
                                  .description("레시피 설명 (nullable)"),
                              fieldWithPath("categorized_recipes[].servings")
                                  .description("레시피 인분 (nullable)"),
                              fieldWithPath("categorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분) (nullable)"),
                              fieldWithPath("categorized_recipes[].created_at")
                                  .description("레시피 생성 시간 (nullable)"),
                              fieldWithPath("categorized_recipes[].tags").description("레시피 태그 목록"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getCategorized(userId, categoryId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("categorized_recipes[0].tags")).isNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 카테고리 ID가 주어졌을 때")
    class GivenEmptyCategory {

      private UUID userId;
      private UUID categoryId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피를 조회한다면")
      class WhenRequestingCategorizedRecipes {

        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          page = 0;
          pageable = Pageable.ofSize(10);
          doReturn(new PageImpl<RecipeHistoryOverview>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .getCategorized(userId, categoryId, page);
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyList() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
              .then()
              .status(HttpStatus.OK)
              .body("categorized_recipes", hasSize(0));

          verify(recipeInfoService).getCategorized(userId, categoryId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("미분류 레시피 조회")
  class GetUnCategorizedRecipes {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 미분류 레시피를 조회한다면")
      class WhenRequestingUnCategorizedRecipes {

        private Page<RecipeHistoryOverview> unCategorizedRecipes;
        private RecipeHistoryOverview unCategorizedRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta detailMeta;
        private List<String> tags;
        private RecipeHistory viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          unCategorizedRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          detailMeta = mock(RecipeDetailMeta.class);
          tags = List.of("한식");
          viewStatus = mock(RecipeHistory.class);

          // RecipeHistoryOverview mock 설정
          doReturn(LocalDateTime.of(2024, 1, 25, 16, 45, 0))
              .when(unCategorizedRecipe)
              .getViewedAt();
          doReturn(150).when(unCategorizedRecipe).getLastPlaySeconds();
          doReturn(recipeId).when(unCategorizedRecipe).getRecipeId();
          doReturn("Uncategorized Recipe Title").when(unCategorizedRecipe).getVideoTitle();
          doReturn(URI.create("https://example.com/uncategorized_thumbnail.jpg"))
              .when(unCategorizedRecipe)
              .getThumbnailUrl();
          doReturn("uncategorized_video_id").when(unCategorizedRecipe).getVideoId();
          doReturn(240).when(unCategorizedRecipe).getVideoSeconds();
          doReturn("Uncategorized Recipe Description").when(unCategorizedRecipe).getDescription();
          doReturn(2).when(unCategorizedRecipe).getServings();
          doReturn(30).when(unCategorizedRecipe).getCookTime();
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0))
              .when(unCategorizedRecipe)
              .getRecipeCreatedAt();
          doReturn(tags).when(unCategorizedRecipe).getTags();

          unCategorizedRecipes = new PageImpl<>(List.of(unCategorizedRecipe), pageable, 1);
          doReturn(unCategorizedRecipes)
              .when(recipeInfoService)
              .getUnCategorized(any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 미분류 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnUnCategorizedRecipes() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/uncategorized")
                  .then()
                  .status(HttpStatus.OK)
                  .body("unCategorized_recipes", hasSize(unCategorizedRecipes.getContent().size()))
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("unCategorized_recipes").description("미분류 레시피 목록"),
                              fieldWithPath("unCategorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("unCategorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("unCategorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("unCategorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("unCategorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("unCategorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].description")
                                  .description("레시피 설명"),
                              fieldWithPath("unCategorized_recipes[].servings")
                                  .description("레시피 인분"),
                              fieldWithPath("unCategorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분)"),
                              fieldWithPath("unCategorized_recipes[].created_at")
                                  .description("레시피 생성 시간"),
                              fieldWithPath("unCategorized_recipes[].tags")
                                  .description("레시피 태그 목록"),
                              fieldWithPath("unCategorized_recipes[].tags[].name")
                                  .description("태그 이름"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getUnCategorized(userId, page);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getUUID("unCategorized_recipes[0].recipe_id"))
              .isEqualTo(recipeId);
          assertThat(responseBody.getString("unCategorized_recipes[0].recipe_title"))
              .isEqualTo("Uncategorized Recipe Title");
          assertThat(responseBody.getString("unCategorized_recipes[0].video_id"))
              .isEqualTo("uncategorized_video_id");
          assertThat(responseBody.getString("unCategorized_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/uncategorized_thumbnail.jpg");
          assertThat(responseBody.getInt("unCategorized_recipes[0].video_seconds")).isEqualTo(240);
          assertThat(responseBody.getString("unCategorized_recipes[0].viewed_at"))
              .isEqualTo("2024-01-25T16:45:00");
          assertThat(responseBody.getInt("unCategorized_recipes[0].last_play_seconds"))
              .isEqualTo(150);
          assertThat(responseBody.getString("unCategorized_recipes[0].description"))
              .isEqualTo("Uncategorized Recipe Description");
          assertThat(responseBody.getInt("unCategorized_recipes[0].servings")).isEqualTo(2);
          assertThat(responseBody.getInt("unCategorized_recipes[0].cook_time")).isEqualTo(30);
          assertThat(responseBody.getString("unCategorized_recipes[0].created_at"))
              .isEqualTo("2024-01-20T14:30:00");
          assertThat(responseBody.getString("unCategorized_recipes[0].tags[0].name"))
              .isEqualTo("한식");
        }

        @Test
        @DisplayName("Then - DetailMeta가 null이면 해당 필드는 null로 내려간다")
        void thenDetailMetaNullShouldReturnNullFields() {
          // DetailMeta가 null인 경우를 위한 새로운 mock 설정
          RecipeHistoryOverview nullDetailMetaRecipe = mock(RecipeHistoryOverview.class);
          doReturn(LocalDateTime.of(2024, 1, 25, 16, 45, 0))
              .when(nullDetailMetaRecipe)
              .getViewedAt();
          doReturn(150).when(nullDetailMetaRecipe).getLastPlaySeconds();
          doReturn(recipeId).when(nullDetailMetaRecipe).getRecipeId();
          doReturn("Uncategorized Recipe Title").when(nullDetailMetaRecipe).getVideoTitle();
          doReturn(URI.create("https://example.com/uncategorized_thumbnail.jpg"))
              .when(nullDetailMetaRecipe)
              .getThumbnailUrl();
          doReturn("uncategorized_video_id").when(nullDetailMetaRecipe).getVideoId();
          doReturn(240).when(nullDetailMetaRecipe).getVideoSeconds();
          doReturn(null).when(nullDetailMetaRecipe).getDescription();
          doReturn(null).when(nullDetailMetaRecipe).getServings();
          doReturn(null).when(nullDetailMetaRecipe).getCookTime();
          doReturn(null).when(nullDetailMetaRecipe).getRecipeCreatedAt();
          doReturn(tags).when(nullDetailMetaRecipe).getTags();

          Page<RecipeHistoryOverview> nullDetailMetaRecipes =
              new PageImpl<>(List.of(nullDetailMetaRecipe), pageable, 1);
          doReturn(nullDetailMetaRecipes)
              .when(recipeInfoService)
              .getUnCategorized(any(UUID.class), any(Integer.class));

          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/uncategorized")
                  .then()
                  .status(HttpStatus.OK)
                  .body("unCategorized_recipes", hasSize(1))
                  .body("unCategorized_recipes[0].description", nullValue())
                  .body("unCategorized_recipes[0].servings", nullValue())
                  .body("unCategorized_recipes[0].cook_time", nullValue())
                  .body("unCategorized_recipes[0].created_at", nullValue())
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("unCategorized_recipes").description("미분류 레시피 목록"),
                              fieldWithPath("unCategorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("unCategorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("unCategorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("unCategorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("unCategorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("unCategorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].description")
                                  .description("레시피 설명 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].servings")
                                  .description("레시피 인분 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분) (nullable)"),
                              fieldWithPath("unCategorized_recipes[].created_at")
                                  .description("레시피 생성 시간 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].tags")
                                  .description("레시피 태그 목록"),
                              fieldWithPath("unCategorized_recipes[].tags[].name")
                                  .description("태그 이름"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getUnCategorized(userId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("unCategorized_recipes[0].description")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].servings")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].cook_time")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].created_at")).isNull();
        }

        @Test
        @DisplayName("Then - Tags가 null이면 해당 필드는 null로 내려간다")
        void thenTagsNullShouldReturnNullFields() {
          // Tags를 null로 응답
          doReturn(null).when(unCategorizedRecipe).getTags();

          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("page", page)
                  .get("/api/v1/recipes/uncategorized")
                  .then()
                  .status(HttpStatus.OK)
                  .body("unCategorized_recipes", hasSize(unCategorizedRecipes.getContent().size()))
                  .body("unCategorized_recipes[0].tags", nullValue())
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          queryParameters(
                              parameterWithName("page")
                                  .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                  .optional()),
                          responseFields(
                              fieldWithPath("unCategorized_recipes").description("미분류 레시피 목록"),
                              fieldWithPath("unCategorized_recipes[].viewed_at")
                                  .description("레시피 접근 시간"),
                              fieldWithPath("unCategorized_recipes[].last_play_seconds")
                                  .description("레시피 마지막 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].recipe_id")
                                  .description("레시피 ID"),
                              fieldWithPath("unCategorized_recipes[].recipe_title")
                                  .description("레시피 제목"),
                              fieldWithPath("unCategorized_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("unCategorized_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("unCategorized_recipes[].video_seconds")
                                  .description("레시피 비디오 재생 시간"),
                              fieldWithPath("unCategorized_recipes[].description")
                                  .description("레시피 설명 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].servings")
                                  .description("레시피 인분 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].cook_time")
                                  .description("레시피 조리 시간(분) (nullable)"),
                              fieldWithPath("unCategorized_recipes[].created_at")
                                  .description("레시피 생성 시간 (nullable)"),
                              fieldWithPath("unCategorized_recipes[].tags")
                                  .description("레시피 태그 목록"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).getUnCategorized(userId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("unCategorized_recipes[0].tags")).isNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피가 없을 때")
    class GivenNoUnCategorizedRecipes {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      @Nested
      @DisplayName("When - 미분류 레시피를 조회한다면")
      class WhenRequestingUnCategorizedRecipes {

        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          page = 0;
          pageable = Pageable.ofSize(10);
          doReturn(new PageImpl<RecipeHistoryOverview>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .getUnCategorized(userId, page);
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyList() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/uncategorized")
              .then()
              .status(HttpStatus.OK)
              .body("unCategorized_recipes", hasSize(0));

          verify(recipeInfoService).getUnCategorized(userId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 카테고리 삭제")
  class DeleteRecipeCategory {

    @Nested
    @DisplayName("Given - 유효한 카테고리 ID가 주어졌을 때")
    class GivenValidCategoryId {

      private UUID categoryId;

      @BeforeEach
      void setUp() {
        categoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 삭제한다면")
      class WhenDeletingRecipeCategory {

        @Test
        @DisplayName("Then - 카테고리를 성공적으로 삭제해야 한다")
        void thenShouldDeleteCategorySuccessfully() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .header("Authorization", "Bearer accessToken")
                  .delete("/api/v1/recipes/categories/{recipe_category_id}", categoryId)
                  .then()
                  .status(HttpStatus.OK)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          requestAccessTokenFields(),
                          pathParameters(
                              parameterWithName("recipe_category_id")
                                  .description("삭제할 레시피 카테고리 ID")),
                          responseFields(fieldWithPath("message").description("성공 메시지"))));
          assertSuccessResponse(response);
          verify(recipeInfoService).deleteCategory(categoryId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 카테고리 ID가 주어졌을 때")
    class GivenNonExistentCategoryId {

      private UUID categoryId;

      @BeforeEach
      void setUp() {
        categoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 삭제한다면")
      class WhenDeletingRecipeCategory {

        @Test
        @DisplayName("Then - 정상적으로 처리되어야 한다")
        void thenShouldProcessNormally() {
          given()
              .contentType(ContentType.JSON)
              .header("Authorization", "Bearer accessToken")
              .delete("/api/v1/recipes/categories/{recipe_category_id}", categoryId)
              .then()
              .status(HttpStatus.OK);

          verify(recipeInfoService).deleteCategory(categoryId);
        }
      }
    }

    @Nested
    @DisplayName("레시피 카테고리 목록 조회")
    class GetRecipeCategories {

      @Nested
      @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
      class GivenValidUserId {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          var authentication = new UsernamePasswordAuthenticationToken(userId, null);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Nested
        @DisplayName("When - 레시피 카테고리 목록을 조회한다면")
        class WhenRequestingRecipeCategories {

          private RecipeCategoryCounts categoryCounts;
          private List<RecipeCategoryCount> categories;
          private RecipeCategoryCount categoryWithCount;
          private RecipeCategory category;
          private UUID categoryId;

          @BeforeEach
          void setUp() {
            categoryId = UUID.randomUUID();
            category = mock(RecipeCategory.class);
            categoryWithCount = mock(RecipeCategoryCount.class);

            doReturn(categoryId).when(category).getId();
            doReturn("한식").when(category).getName();
            doReturn(category).when(categoryWithCount).getCategory();
            doReturn(5).when(categoryWithCount).getRecipeCount();

            categories = List.of(categoryWithCount);
            categoryCounts = mock(RecipeCategoryCounts.class);
            doReturn(3).when(categoryCounts).getUncategorizedCount();
            doReturn(categories).when(categoryCounts).getCategorizedCounts();
            doReturn(8).when(categoryCounts).getTotalCount(); // 5 + 3

            doReturn(categoryCounts).when(recipeInfoService).getCategoryCounts(any(UUID.class));
          }

          @Test
          @DisplayName("Then - 레시피 카테고리 목록과 개수를 성공적으로 반환해야 한다")
          void thenShouldReturnRecipeCategoryCounts() {
            var response =
                given()
                    .contentType(ContentType.JSON)
                    .attribute("userId", userId.toString())
                    .header("Authorization", "Bearer accessToken")
                    .get("/api/v1/recipes/categories")
                    .then()
                    .status(HttpStatus.OK)
                    .body("categories", hasSize(categories.size()))
                    .apply(
                        document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            requestAccessTokenFields(),
                            responseFields(
                                fieldWithPath("categories").description("레시피 카테고리 목록"),
                                fieldWithPath("categories[].category_id").description("카테고리 ID"),
                                fieldWithPath("categories[].count").description("해당 카테고리의 레시피 수"),
                                fieldWithPath("categories[].name").description("카테고리 이름"),
                                fieldWithPath("unCategorized_count").description("미분류 레시피 수"),
                                fieldWithPath("total_count").description("전체 레시피 수"))));

            verify(recipeInfoService).getCategoryCounts(userId);

            var responseBody = response.extract().jsonPath();
            var categoriesList = responseBody.getList("categories");

            assertThat(categoriesList).hasSize(1);
            assertThat(responseBody.getUUID("categories[0].category_id")).isEqualTo(categoryId);
            assertThat(responseBody.getInt("categories[0].count")).isEqualTo(5);
            assertThat(responseBody.getString("categories[0].name")).isEqualTo("한식");
            assertThat(responseBody.getInt("unCategorized_count")).isEqualTo(3);
            assertThat(responseBody.getInt("total_count")).isEqualTo(8);
          }
        }
      }

      @Nested
      @DisplayName("Given - 카테고리가 없는 사용자 ID가 주어졌을 때")
      class GivenUserWithNoCategories {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          var authentication = new UsernamePasswordAuthenticationToken(userId, null);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Nested
        @DisplayName("When - 레시피 카테고리 목록을 조회한다면")
        class WhenRequestingRecipeCategories {

          @BeforeEach
          void setUp() {
            var categoryCounts = mock(RecipeCategoryCounts.class);
            doReturn(0).when(categoryCounts).getUncategorizedCount();
            doReturn(List.of()).when(categoryCounts).getCategorizedCounts();
            doReturn(0).when(categoryCounts).getTotalCount();
            doReturn(categoryCounts).when(recipeInfoService).getCategoryCounts(userId);
          }

          @Test
          @DisplayName("Then - 빈 결과를 반환해야 한다")
          void thenShouldReturnEmptyList() {
            given()
                .contentType(ContentType.JSON)
                .attribute("userId", userId.toString())
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/categories")
                .then()
                .status(HttpStatus.OK)
                .body("categories", hasSize(0))
                .body("unCategorized_count", equalTo(0))
                .body("total_count", equalTo(0));

            verify(recipeInfoService).getCategoryCounts(userId);
          }
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 검색")
  class SearchRecipes {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("Given - 유효한 검색어가 주어졌을 때")
    class GivenValidSearchQuery {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "김치찌개";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Page<RecipeOverview> searchResults;
        private RecipeOverview recipeOverview;
        private Recipe recipe;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta detailMeta;
        private UUID recipeId;
        private Pageable pageable;
        private List<String> tags;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recipe = mock(Recipe.class);
          recipeOverview = mock(RecipeOverview.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          detailMeta = mock(RecipeDetailMeta.class);
          pageable = Pageable.ofSize(10);

          doReturn(recipeId).when(recipe).getId();

          doReturn("김치찌개 맛있게 끓이는 법").when(youtubeMeta).getTitle();
          doReturn("kimchi_video_id").when(youtubeMeta).getVideoId();
          doReturn(URI.create("https://example.com/kimchi_thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(300).when(youtubeMeta).getVideoSeconds();

          doReturn("얼큰한 김치찌개 레시피").when(detailMeta).getDescription();
          doReturn(2).when(detailMeta).getServings();
          doReturn(30).when(detailMeta).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId).when(recipeOverview).getRecipeId();
          doReturn("김치찌개 맛있게 끓이는 법").when(recipeOverview).getVideoTitle();
          doReturn(URI.create("https://example.com/kimchi_thumbnail.jpg"))
              .when(recipeOverview)
              .getThumbnailUrl();
          doReturn("kimchi_video_id").when(recipeOverview).getVideoId();
          doReturn(300).when(recipeOverview).getVideoSeconds();
          doReturn("얼큰한 김치찌개 레시피").when(recipeOverview).getDescription();
          doReturn(2).when(recipeOverview).getServings();
          doReturn(30).when(recipeOverview).getCookTime();
          doReturn(List.of("한식", "찌개")).when(recipeOverview).getTags();
          doReturn(true).when(recipeOverview).getIsViewed();

          // 디버깅을 위한 mock 설정 확인
          System.out.println("Mock setup - recipeId: " + recipeOverview.getRecipeId());
          System.out.println("Mock setup - videoTitle: " + recipeOverview.getVideoTitle());
          System.out.println("Mock setup - tags: " + recipeOverview.getTags());

          searchResults = new PageImpl<>(List.of(recipeOverview), pageable, 1);
          doReturn(searchResults)
              .when(recipeInfoService)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 검색 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnSearchResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK)
                  .extract()
                  .response();

          // 응답 검증
          response.then().body("searched_recipes", hasSize(1));

          response
              .then()
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      queryParameters(
                          parameterWithName("query").description("검색어"),
                          parameterWithName("page")
                              .description("페이지 번호 (0부터 시작, 기본값: 0)")
                              .optional()),
                      responseFields(
                          fieldWithPath("searched_recipes").description("검색된 레시피 목록"),
                          fieldWithPath("searched_recipes[].recipe_id").description("레시피 ID"),
                          fieldWithPath("searched_recipes[].recipe_title").description("레시피 제목"),
                          fieldWithPath("searched_recipes[].tags").description("레시피 태그 목록"),
                          fieldWithPath("searched_recipes[].tags[].name").description("태그 이름"),
                          fieldWithPath("searched_recipes[].description").description("레시피 설명"),
                          fieldWithPath("searched_recipes[].servings").description("인분"),
                          fieldWithPath("searched_recipes[].cooking_time").description("조리 시간(분)"),
                          fieldWithPath("searched_recipes[].video_id").description("레시피 비디오 ID"),
                          fieldWithPath("searched_recipes[].video_title").description("레시피 비디오 제목"),
                          fieldWithPath("searched_recipes[].video_thumbnail_url")
                              .description("레시피 비디오 썸네일 URL"),
                          fieldWithPath("searched_recipes[].video_seconds")
                              .description("레시피 비디오 재생 시간"),
                          fieldWithPath("searched_recipes[].is_viewed").description("레시피 조회 여부"),
                          fieldWithPath("current_page").description("현재 페이지 번호"),
                          fieldWithPath("total_pages").description("전체 페이지 수"),
                          fieldWithPath("total_elements").description("전체 요소 수"),
                          fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).searchRecipes(page, query, userId);

          var responseBody = response.jsonPath();
          assertThat(responseBody.getList("searched_recipes")).hasSize(1);
          assertThat(responseBody.getString("searched_recipes[0].recipe_id"))
              .isEqualTo(recipeId.toString());
          assertThat(responseBody.getString("searched_recipes[0].recipe_title"))
              .isEqualTo("김치찌개 맛있게 끓이는 법");
          assertThat(responseBody.getList("searched_recipes[0].tags")).hasSize(2);
          assertThat(responseBody.getString("searched_recipes[0].tags[0].name")).isEqualTo("한식");
          assertThat(responseBody.getString("searched_recipes[0].tags[1].name")).isEqualTo("찌개");
          assertThat(responseBody.getString("searched_recipes[0].description"))
              .isEqualTo("얼큰한 김치찌개 레시피");
          assertThat(responseBody.getInt("searched_recipes[0].servings")).isEqualTo(2);
          assertThat(responseBody.getInt("searched_recipes[0].cooking_time")).isEqualTo(30);
          assertThat(responseBody.getString("searched_recipes[0].video_id"))
              .isEqualTo("kimchi_video_id");
          assertThat(responseBody.getString("searched_recipes[0].video_title"))
              .isEqualTo("김치찌개 맛있게 끓이는 법");
          assertThat(responseBody.getString("searched_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/kimchi_thumbnail.jpg");
          assertThat(responseBody.getInt("searched_recipes[0].video_seconds")).isEqualTo(300);
          assertThat(responseBody.getBoolean("searched_recipes[0].is_viewed")).isEqualTo(true);
          assertThat(responseBody.getString("current_page")).isEqualTo("0");
          assertThat(responseBody.getString("total_pages")).isEqualTo("1");
          assertThat(responseBody.getString("total_elements")).isEqualTo("1");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
        }
      }
    }

    @Nested
    @DisplayName("Given - 검색 결과가 없는 검색어가 주어졌을 때")
    class GivenQueryWithNoResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "존재하지않는레시피";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Pageable pageable;

        @BeforeEach
        void setUp() {
          pageable = Pageable.ofSize(10);
          doReturn(new PageImpl<RecipeOverview>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyResults() {
          given()
              .contentType(ContentType.JSON)
              .param("query", query)
              .param("page", page)
              .get("/api/v1/recipes/search")
              .then()
              .status(HttpStatus.OK)
              .body("searched_recipes", hasSize(0));

          verify(recipeInfoService).searchRecipes(page, query, userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 검색 결과가 있는 검색어가 주어졌을 때")
    class GivenQueryWithMultipleResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "찌개";
        page = 0;
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        private Page<RecipeOverview> searchResults;
        private List<RecipeOverview> recipeOverviews;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          pageable = Pageable.ofSize(10);

          // 첫 번째 레시피 - 김치찌개
          RecipeOverview kimchiStew = mock(RecipeOverview.class);
          Recipe recipe1 = mock(Recipe.class);
          RecipeYoutubeMeta meta1 = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detail1 = mock(RecipeDetailMeta.class);
          UUID recipeId1 = UUID.randomUUID();

          doReturn(recipeId1).when(recipe1).getId();
          doReturn("김치찌개").when(meta1).getTitle();
          doReturn("kimchi_video_id").when(meta1).getVideoId();
          doReturn(URI.create("https://example.com/kimchi.jpg")).when(meta1).getThumbnailUrl();
          doReturn(300).when(meta1).getVideoSeconds();
          doReturn("김치찌개 레시피").when(detail1).getDescription();
          doReturn(2).when(detail1).getServings();
          doReturn(30).when(detail1).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId1).when(kimchiStew).getRecipeId();
          doReturn("김치찌개").when(kimchiStew).getVideoTitle();
          doReturn(URI.create("https://example.com/kimchi.jpg")).when(kimchiStew).getThumbnailUrl();
          doReturn("kimchi_video_id").when(kimchiStew).getVideoId();
          doReturn(300).when(kimchiStew).getVideoSeconds();
          doReturn("김치찌개 레시피").when(kimchiStew).getDescription();
          doReturn(2).when(kimchiStew).getServings();
          doReturn(30).when(kimchiStew).getCookTime();
          doReturn(List.of("한식")).when(kimchiStew).getTags();
          doReturn(true).when(kimchiStew).getIsViewed();

          // 두 번째 레시피 - 된장찌개
          RecipeOverview soyBeanStew = mock(RecipeOverview.class);
          Recipe recipe2 = mock(Recipe.class);
          RecipeYoutubeMeta meta2 = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detail2 = mock(RecipeDetailMeta.class);
          UUID recipeId2 = UUID.randomUUID();

          doReturn(recipeId2).when(recipe2).getId();
          doReturn("된장찌개").when(meta2).getTitle();
          doReturn("soybean_video_id").when(meta2).getVideoId();
          doReturn(URI.create("https://example.com/soybean.jpg")).when(meta2).getThumbnailUrl();
          doReturn(240).when(meta2).getVideoSeconds();
          doReturn("된장찌개 레시피").when(detail2).getDescription();
          doReturn(2).when(detail2).getServings();
          doReturn(25).when(detail2).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId2).when(soyBeanStew).getRecipeId();
          doReturn("된장찌개").when(soyBeanStew).getVideoTitle();
          doReturn(URI.create("https://example.com/soybean.jpg"))
              .when(soyBeanStew)
              .getThumbnailUrl();
          doReturn("soybean_video_id").when(soyBeanStew).getVideoId();
          doReturn(240).when(soyBeanStew).getVideoSeconds();
          doReturn("된장찌개 레시피").when(soyBeanStew).getDescription();
          doReturn(2).when(soyBeanStew).getServings();
          doReturn(25).when(soyBeanStew).getCookTime();
          doReturn(List.of("한식")).when(soyBeanStew).getTags();
          doReturn(true).when(soyBeanStew).getIsViewed();

          recipeOverviews = List.of(kimchiStew, soyBeanStew);
          searchResults = new PageImpl<>(recipeOverviews, pageable, 2);
          doReturn(searchResults)
              .when(recipeInfoService)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 검색 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnAllSearchResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK)
                  .body("searched_recipes", hasSize(2));

          verify(recipeInfoService).searchRecipes(page, query, userId);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("searched_recipes")).hasSize(2);
          assertThat(responseBody.getString("searched_recipes[0].recipe_title")).isEqualTo("김치찌개");
          assertThat(responseBody.getString("searched_recipes[1].recipe_title")).isEqualTo("된장찌개");
          assertThat(responseBody.getString("total_elements")).isEqualTo("2");
        }
      }
    }

    @Nested
    @DisplayName("Given - 페이지네이션이 필요한 검색 결과가 주어졌을 때")
    class GivenPaginatedSearchResults {

      private String query;
      private Integer page;

      @BeforeEach
      void setUp() {
        query = "파스타";
        page = 1; // 두 번째 페이지
      }

      @Nested
      @DisplayName("When - 두 번째 페이지를 검색한다면")
      class WhenSearchingSecondPage {

        private Page<RecipeOverview> searchResults;

        @BeforeEach
        void setUp() {
          RecipeOverview recipe = mock(RecipeOverview.class);
          Recipe recipeEntity = mock(Recipe.class);
          RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
          RecipeDetailMeta detailMeta = mock(RecipeDetailMeta.class);
          UUID recipeId = UUID.randomUUID();

          doReturn(recipeId).when(recipeEntity).getId();
          doReturn("까르보나라 파스타").when(meta).getTitle();
          doReturn("pasta_video_id").when(meta).getVideoId();
          doReturn(URI.create("https://example.com/pasta.jpg")).when(meta).getThumbnailUrl();
          doReturn(180).when(meta).getVideoSeconds();
          doReturn("파스타 레시피").when(detailMeta).getDescription();
          doReturn(2).when(detailMeta).getServings();
          doReturn(20).when(detailMeta).getCookTime();

          // RecipeOverview mock 설정
          doReturn(recipeId).when(recipe).getRecipeId();
          doReturn("까르보나라 파스타").when(recipe).getVideoTitle();
          doReturn(URI.create("https://example.com/pasta.jpg")).when(recipe).getThumbnailUrl();
          doReturn("pasta_video_id").when(recipe).getVideoId();
          doReturn(180).when(recipe).getVideoSeconds();
          doReturn("파스타 레시피").when(recipe).getDescription();
          doReturn(2).when(recipe).getServings();
          doReturn(20).when(recipe).getCookTime();
          doReturn(List.of("양식")).when(recipe).getTags();
          doReturn(true).when(recipe).getIsViewed();

          // 전체 12개 결과 중 두 번째 페이지 (10개 이후의 2개)
          searchResults = new PageImpl<>(List.of(recipe), Pageable.ofSize(10).withPage(1), 11);
          doReturn(searchResults)
              .when(recipeInfoService)
              .searchRecipes(any(Integer.class), any(String.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 두 번째 페이지 결과를 성공적으로 반환해야 한다")
        void thenShouldReturnSecondPageResults() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .attribute("userId", userId.toString())
                  .header("Authorization", "Bearer accessToken")
                  .param("query", query)
                  .param("page", page)
                  .get("/api/v1/recipes/search")
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeInfoService).searchRecipes(page, query, userId);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getString("current_page")).isEqualTo("1");
          assertThat(responseBody.getString("total_pages")).isEqualTo("2");
          assertThat(responseBody.getString("total_elements")).isEqualTo("11");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
        }
      }
    }
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

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.SUCCESS).when(recipe).getRecipeStatus();

          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          RecipeProgress progress3 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.FINISHED).when(progress3).getStep();
          doReturn(RecipeProgressDetail.FINISHED).when(progress3).getDetail();

          progresses = List.of(progress1, progress2, progress3);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
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
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              enumFields("recipe_status", "레시피의 현재 상태: ", RecipeStatus.class),
                              fieldWithPath("recipe_progress_statuses").description("레시피 진행 상황 목록"),
                              enumFields(
                                  "recipe_progress_statuses[].progress_step",
                                  "현재 진행 상태",
                                  RecipeProgressStep.class),
                              enumFields(
                                  "recipe_progress_statuses[].progress_detail",
                                  "현재 진행 상세 내용",
                                  RecipeProgressDetail.class))));

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status")).isEqualTo("SUCCESS");
          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(3);
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

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();

          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          progresses = List.of(progress1, progress2);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 부분적인 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnPartialProgress() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status")).isEqualTo("IN_PROGRESS");
          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(2);
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

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(List.of()).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).getRecipeProgress(recipeId);
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

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.SUCCESS).when(recipe).getRecipeStatus();

          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          RecipeProgress progress3 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.BRIEFING).when(progress3).getStep();
          doReturn(RecipeProgressDetail.BRIEFING).when(progress3).getDetail();

          RecipeProgress progress4 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.STEP).when(progress4).getStep();
          doReturn(RecipeProgressDetail.STEP).when(progress4).getDetail();

          progresses = List.of(progress1, progress2, progress3, progress4);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 브리핑을 포함한 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnProgressWithBriefing() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(4);

          boolean hasBriefingStep = false;
          for (int i = 0; i < 4; i++) {
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

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.SUCCESS).when(recipe).getRecipeStatus();

          // 모든 Detail 타입을 포함한 복잡한 진행 상황
          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          RecipeProgress progress3 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.DETAIL).when(progress3).getStep();
          doReturn(RecipeProgressDetail.TAG).when(progress3).getDetail();

          RecipeProgress progress4 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.DETAIL).when(progress4).getStep();
          doReturn(RecipeProgressDetail.DETAIL_META).when(progress4).getDetail();

          RecipeProgress progress5 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.DETAIL).when(progress5).getStep();
          doReturn(RecipeProgressDetail.INGREDIENT).when(progress5).getDetail();

          RecipeProgress progress6 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.BRIEFING).when(progress6).getStep();
          doReturn(RecipeProgressDetail.BRIEFING).when(progress6).getDetail();

          RecipeProgress progress7 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.STEP).when(progress7).getStep();
          doReturn(RecipeProgressDetail.STEP).when(progress7).getDetail();

          RecipeProgress progress8 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.FINISHED).when(progress8).getStep();
          doReturn(RecipeProgressDetail.FINISHED).when(progress8).getDetail();

          progresses =
              List.of(
                  progress1, progress2, progress3, progress4, progress5, progress6, progress7,
                  progress8);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 모든 복잡한 진행 단계들을 성공적으로 반환해야 한다")
        void thenShouldReturnAllComplexProgressSteps() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          // 전체 진행 단계 수 검증
          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(8);

          // 시작과 끝 검증
          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_step"))
              .isEqualTo("READY");
          assertThat(responseBody.getString("recipe_progress_statuses[0].progress_detail"))
              .isEqualTo("READY");

          assertThat(responseBody.getString("recipe_progress_statuses[7].progress_step"))
              .isEqualTo("FINISHED");
          assertThat(responseBody.getString("recipe_progress_statuses[7].progress_detail"))
              .isEqualTo("FINISHED");

          // DETAIL 단계의 다양한 세부 사항들 확인
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
          assertThat(detailCount).isEqualTo(3); // TAG, DETAIL_META, INGREDIENT
        }
      }
    }
  }

  @Nested
  @DisplayName("크롤러 레시피 생성")
  class CreateCrawledRecipe {

    @Nested
    @DisplayName("Given - 유효한 비디오 URL이 주어졌을 때")
    class GivenValidVideoUrl {

      @Test
      @DisplayName("Then - 크롤러 레시피 생성 요청을 성공적으로 처리하고 히스토리를 생성하지 않는다")
      void thenShouldCreateCrawledRecipeWithoutHistory() {
        var recipeId = UUID.randomUUID();
        var requestBody =
            """
            {
              "video_url": "https://www.youtube.com/watch?v=crawled"
            }
            """;

        doReturn(recipeId).when(recipeInfoService).create(any(RecipeCreationTarget.class));

        var response =
            given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/papi/v1/recipes")
                .then()
                .status(HttpStatus.OK);

        verify(recipeInfoService).create(any(RecipeCreationTarget.Crawler.class));

        var responseBody = response.extract().jsonPath();
        assertThat(responseBody.getUUID("recipe_id")).isEqualTo(recipeId);
      }
    }
  }

  @Nested
  @DisplayName("크롤러 레시피 진행 상황 조회")
  class GetCrawledRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 크롤러 레시피 진행 상황을 조회한다면")
      class WhenRequestingCrawledRecipeProgress {

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.SUCCESS).when(recipe).getRecipeStatus();

          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          RecipeProgress progress3 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.FINISHED).when(progress3).getStep();
          doReturn(RecipeProgressDetail.FINISHED).when(progress3).getDetail();

          progresses = List.of(progress1, progress2, progress3);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 크롤러 레시피 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnCrawledRecipeProgress() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK)
                  .apply(
                      document(
                          getNestedClassPath(this.getClass()) + "/{method-name}",
                          requestPreprocessor(),
                          responsePreprocessor(),
                          pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                          responseFields(
                              enumFields("recipe_status", "레시피의 현재 상태: ", RecipeStatus.class),
                              fieldWithPath("recipe_progress_statuses").description("레시피 진행 상황 목록"),
                              enumFields(
                                  "recipe_progress_statuses[].progress_step",
                                  "현재 진행 상태",
                                  RecipeProgressStep.class),
                              enumFields(
                                  "recipe_progress_statuses[].progress_detail",
                                  "현재 진행 상세 내용",
                                  RecipeProgressDetail.class))));

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status")).isEqualTo("SUCCESS");
          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(3);
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
      @DisplayName("When - 크롤러 레시피 진행 상황을 조회한다면")
      class WhenRequestingCrawledRecipeProgress {

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;
        private List<RecipeProgress> progresses;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();

          RecipeProgress progress1 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.READY).when(progress1).getStep();
          doReturn(RecipeProgressDetail.READY).when(progress1).getDetail();

          RecipeProgress progress2 = mock(RecipeProgress.class);
          doReturn(RecipeProgressStep.CAPTION).when(progress2).getStep();
          doReturn(RecipeProgressDetail.CAPTION).when(progress2).getDetail();

          progresses = List.of(progress1, progress2);

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(progresses).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 부분적인 진행 상황을 성공적으로 반환해야 한다")
        void thenShouldReturnPartialProgress() {
          var response =
              given()
                  .contentType(ContentType.JSON)
                  .get("/papi/v1/recipes/progress/{recipeId}", recipeId)
                  .then()
                  .status(HttpStatus.OK);

          verify(recipeInfoService).getRecipeProgress(recipeId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status")).isEqualTo("IN_PROGRESS");
          assertThat(responseBody.getList("recipe_progress_statuses")).hasSize(2);
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
      @DisplayName("When - 크롤러 레시피 진행 상황을 조회한다면")
      class WhenRequestingCrawledRecipeProgress {

        private RecipeProgressStatus progressStatus;
        private Recipe recipe;

        @BeforeEach
        void setUp() {
          recipe = mock(Recipe.class);
          doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();

          progressStatus = mock(RecipeProgressStatus.class);
          doReturn(recipe).when(progressStatus).getRecipe();
          doReturn(List.of()).when(progressStatus).getProgresses();

          doReturn(progressStatus).when(recipeInfoService).getRecipeProgress(any(UUID.class));
        }

        @Test
        @DisplayName("Then - 빈 진행 상황 목록을 반환해야 한다")
        void thenShouldReturnEmptyProgress() {
          given()
              .contentType(ContentType.JSON)
              .get("/papi/v1/recipes/progress/{recipeId}", recipeId)
              .then()
              .status(HttpStatus.OK)
              .body("recipe_progress_statuses", hasSize(0));

          verify(recipeInfoService).getRecipeProgress(recipeId);
        }
      }
    }
  }

  @Nested
  @DisplayName("트렌딩 레시피 조회")
  class GetTrendingRecipes {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      @Test
      @DisplayName("Then - 트렌딩 레시피 목록을 성공적으로 조회한다")
      void thenShouldGetTrendingRecipesSuccessfully() {
        var recipeId1 = UUID.randomUUID();
        var recipeId2 = UUID.randomUUID();
        var recipeOverview1 = mock(RecipeOverview.class);
        var recipeOverview2 = mock(RecipeOverview.class);

        doReturn(recipeId1).when(recipeOverview1).getRecipeId();
        doReturn("트렌딩 레시피 1").when(recipeOverview1).getVideoTitle();
        doReturn(URI.create("https://example.com/thumb1.jpg"))
            .when(recipeOverview1)
            .getThumbnailUrl();
        doReturn("video1").when(recipeOverview1).getVideoId();
        doReturn(180).when(recipeOverview1).getVideoSeconds();
        doReturn(false).when(recipeOverview1).getIsViewed();
        doReturn(List.of("인기")).when(recipeOverview1).getTags();
        doReturn("트렌딩 레시피 설명 1").when(recipeOverview1).getDescription();
        doReturn(2).when(recipeOverview1).getServings();
        doReturn(30).when(recipeOverview1).getCookTime();

        doReturn(recipeId2).when(recipeOverview2).getRecipeId();
        doReturn("트렌딩 레시피 2").when(recipeOverview2).getVideoTitle();
        doReturn(URI.create("https://example.com/thumb2.jpg"))
            .when(recipeOverview2)
            .getThumbnailUrl();
        doReturn("video2").when(recipeOverview2).getVideoId();
        doReturn(240).when(recipeOverview2).getVideoSeconds();
        doReturn(false).when(recipeOverview2).getIsViewed();
        doReturn(List.of("인기")).when(recipeOverview2).getTags();
        doReturn("트렌딩 레시피 설명 2").when(recipeOverview2).getDescription();
        doReturn(3).when(recipeOverview2).getServings();
        doReturn(40).when(recipeOverview2).getCookTime();

        var recipes = List.of(recipeOverview1, recipeOverview2);
        var page = new PageImpl<>(recipes, Pageable.ofSize(20), 2);

        doReturn(page)
            .when(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.TRENDING, userId, 0, RecipeInfoVideoQuery.ALL);

        given()
            .queryParam("page", 0)
            .queryParam("query", "ALL")
            .get("/api/v1/recipes/recommend/trending")
            .then()
            .status(HttpStatus.OK)
            .body("recommend_recipes", hasSize(2))
            .body("recommend_recipes[0].recipe_id", equalTo(recipeId1.toString()))
            .body("recommend_recipes[0].recipe_title", equalTo("트렌딩 레시피 1"))
            .body("recommend_recipes[0].video_title", equalTo("트렌딩 레시피 1"))
            .body(
                "recommend_recipes[0].video_thumbnail_url",
                equalTo("https://example.com/thumb1.jpg"))
            .body("recommend_recipes[0].video_id", equalTo("video1"))
            .body("recommend_recipes[0].video_seconds", equalTo(180))
            .body("recommend_recipes[0].is_viewed", equalTo(false))
            .body("recommend_recipes[1].recipe_id", equalTo(recipeId2.toString()))
            .body("recommend_recipes[1].recipe_title", equalTo("트렌딩 레시피 2"))
            .body("recommend_recipes[1].video_title", equalTo("트렌딩 레시피 2"))
            .body(
                "recommend_recipes[1].video_thumbnail_url",
                equalTo("https://example.com/thumb2.jpg"))
            .body("recommend_recipes[1].video_id", equalTo("video2"))
            .body("recommend_recipes[1].video_seconds", equalTo(240))
            .body("recommend_recipes[1].is_viewed", equalTo(false))
            .body("total_pages", equalTo(1))
            .body("total_elements", equalTo(2))
            .body("current_page", equalTo(0))
            .body("has_next", equalTo(false));

        verify(recipeInfoService)
            .getRecommendRecipes(
                RecipeInfoRecommendType.TRENDING, userId, 0, RecipeInfoVideoQuery.ALL);
      }
    }
  }

  @Nested
  @DisplayName("셰프 레시피 조회")
  class GetChefRecipes {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      @Test
      @DisplayName("Then - 셰프 레시피 목록을 성공적으로 조회한다")
      void thenShouldGetChefRecipesSuccessfully() {
        var recipeId1 = UUID.randomUUID();
        var recipeId2 = UUID.randomUUID();
        var recipeOverview1 = mock(RecipeOverview.class);
        var recipeOverview2 = mock(RecipeOverview.class);

        doReturn(recipeId1).when(recipeOverview1).getRecipeId();
        doReturn("셰프 레시피 1").when(recipeOverview1).getVideoTitle();
        doReturn(URI.create("https://example.com/chef1.jpg"))
            .when(recipeOverview1)
            .getThumbnailUrl();
        doReturn("chef1").when(recipeOverview1).getVideoId();
        doReturn(300).when(recipeOverview1).getVideoSeconds();
        doReturn(false).when(recipeOverview1).getIsViewed();
        doReturn(List.of("고급")).when(recipeOverview1).getTags();
        doReturn("셰프 레시피 설명 1").when(recipeOverview1).getDescription();
        doReturn(4).when(recipeOverview1).getServings();
        doReturn(60).when(recipeOverview1).getCookTime();

        doReturn(recipeId2).when(recipeOverview2).getRecipeId();
        doReturn("셰프 레시피 2").when(recipeOverview2).getVideoTitle();
        doReturn(URI.create("https://example.com/chef2.jpg"))
            .when(recipeOverview2)
            .getThumbnailUrl();
        doReturn("chef2").when(recipeOverview2).getVideoId();
        doReturn(360).when(recipeOverview2).getVideoSeconds();
        doReturn(false).when(recipeOverview2).getIsViewed();
        doReturn(List.of("고급")).when(recipeOverview2).getTags();
        doReturn("셰프 레시피 설명 2").when(recipeOverview2).getDescription();
        doReturn(6).when(recipeOverview2).getServings();
        doReturn(90).when(recipeOverview2).getCookTime();

        var recipes = List.of(recipeOverview1, recipeOverview2);
        var page = new PageImpl<>(recipes, Pageable.ofSize(20), 2);

        doReturn(page)
            .when(recipeInfoService)
            .getRecommendRecipes(RecipeInfoRecommendType.CHEF, userId, 0, RecipeInfoVideoQuery.ALL);

        given()
            .queryParam("page", 0)
            .queryParam("query", "ALL")
            .get("/api/v1/recipes/recommend/chef")
            .then()
            .status(HttpStatus.OK)
            .body("recommend_recipes", hasSize(2))
            .body("recommend_recipes[0].recipe_id", equalTo(recipeId1.toString()))
            .body("recommend_recipes[0].recipe_title", equalTo("셰프 레시피 1"))
            .body("recommend_recipes[0].video_title", equalTo("셰프 레시피 1"))
            .body(
                "recommend_recipes[0].video_thumbnail_url",
                equalTo("https://example.com/chef1.jpg"))
            .body("recommend_recipes[0].video_id", equalTo("chef1"))
            .body("recommend_recipes[0].video_seconds", equalTo(300))
            .body("recommend_recipes[0].is_viewed", equalTo(false))
            .body("recommend_recipes[1].recipe_id", equalTo(recipeId2.toString()))
            .body("recommend_recipes[1].recipe_title", equalTo("셰프 레시피 2"))
            .body("recommend_recipes[1].video_title", equalTo("셰프 레시피 2"))
            .body(
                "recommend_recipes[1].video_thumbnail_url",
                equalTo("https://example.com/chef2.jpg"))
            .body("recommend_recipes[1].video_id", equalTo("chef2"))
            .body("recommend_recipes[1].video_seconds", equalTo(360))
            .body("recommend_recipes[1].is_viewed", equalTo(false))
            .body("total_pages", equalTo(1))
            .body("total_elements", equalTo(2))
            .body("current_page", equalTo(0))
            .body("has_next", equalTo(false));

        verify(recipeInfoService)
            .getRecommendRecipes(RecipeInfoRecommendType.CHEF, userId, 0, RecipeInfoVideoQuery.ALL);
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 추천 타입이 주어졌을 때")
    class GivenInvalidRecommendType {

      @Test
      @DisplayName("Then - 400 Bad Request와 INVALID_RECOMMEND_TYPE 에러를 반환해야 한다")
      void thenShouldReturnInvalidRecommendTypeError() {
        given()
            .queryParam("page", 0)
            .queryParam("query", "ALL")
            .get("/api/v1/recipes/recommend/invalid")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .body("errorCode", equalTo(RecipeInfoErrorCode.INVALID_RECOMMEND_TYPE.getErrorCode()))
            .body("message", equalTo(RecipeInfoErrorCode.INVALID_RECOMMEND_TYPE.getMessage()));
      }
    }
  }

  @Nested
  @DisplayName("요리 카테고리별 레시피 조회")
  class GetCuisineRecipes {

    private UUID userId;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      var authentication = new UsernamePasswordAuthenticationToken(userId, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("Given - 유효한 카테고리 타입과 사용자 ID가 주어졌을 때")
    class GivenValidCuisineType {

      @Test
      @DisplayName("Then - 요리 카테고리별 레시피 목록을 성공적으로 조회한다")
      void thenShouldGetCuisineRecipesSuccessfully() {
        var recipeId1 = UUID.randomUUID();
        var recipeOverview1 = mock(RecipeOverview.class);

        doReturn(recipeId1).when(recipeOverview1).getRecipeId();
        doReturn("한식 레시피 1").when(recipeOverview1).getVideoTitle();
        doReturn(URI.create("https://example.com/korean1.jpg"))
            .when(recipeOverview1)
            .getThumbnailUrl();
        doReturn(URI.create("https://youtube.com/watch?v=korean1"))
            .when(recipeOverview1)
            .getVideoUri();
        doReturn(1000).when(recipeOverview1).getViewCount();
        doReturn("korean1").when(recipeOverview1).getVideoId();
        doReturn(false).when(recipeOverview1).getIsViewed();
        doReturn(YoutubeMetaType.NORMAL).when(recipeOverview1).getVideoType();

        var recipes = List.of(recipeOverview1);
        var page = new PageImpl<>(recipes, Pageable.ofSize(20), 1);

        doReturn(page)
            .when(recipeInfoService)
            .getCuisineRecipes(RecipeInfoCuisineType.KOREAN, userId, 0);

        given()
            .queryParam("page", 0)
            .get("/api/v1/recipes/cuisine/korean")
            .then()
            .status(HttpStatus.OK)
            .body("cuisine_recipes", hasSize(1));

        verify(recipeInfoService).getCuisineRecipes(RecipeInfoCuisineType.KOREAN, userId, 0);
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 카테고리 타입이 주어졌을 때")
    class GivenInvalidCuisineType {

      @Test
      @DisplayName("Then - 400 Bad Request와 INVALID_CUISINE_TYPE 에러를 반환해야 한다")
      void thenShouldReturnInvalidCuisineTypeError() {
        given()
            .queryParam("page", 0)
            .get("/api/v1/recipes/cuisine/invalid")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .body("errorCode", equalTo(RecipeInfoErrorCode.INVALID_CUISINE_TYPE.getErrorCode()))
            .body("message", equalTo(RecipeInfoErrorCode.INVALID_CUISINE_TYPE.getMessage()));
      }
    }
  }
}
