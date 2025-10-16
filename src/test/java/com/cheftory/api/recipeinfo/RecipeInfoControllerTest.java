package com.cheftory.api.recipeinfo;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
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
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredient;
import com.cheftory.api.recipeinfo.model.CountRecipeCategory;
import com.cheftory.api.recipeinfo.model.FullRecipeInfo;
import com.cheftory.api.recipeinfo.model.RecipeHistory;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
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

      doReturn(recipeId).when(recipeInfoService).create(any(URI.class), any(UUID.class));

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

      verify(recipeInfoService)
          .create(URI.create("https://www.youtube.com/watch?v=example"), userId);

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

        private Page<RecipeHistory> recentRecipes;
        private RecipeHistory recentRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta meta;
        private RecipeViewStatus viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recentRecipe = mock(RecipeHistory.class);
          recipe = mock(Recipe.class);
          meta = mock(RecipeYoutubeMeta.class);
          viewStatus = mock(RecipeViewStatus.class);
          page = 0;
          pageable = Pageable.ofSize(10);

          doReturn(recipe).when(recentRecipe).getRecipe();
          doReturn(viewStatus).when(recentRecipe).getRecipeViewStatus();
          doReturn(meta).when(recentRecipe).getYoutubeMeta();

          doReturn(recipeId).when(recipe).getId();
          doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();
          doReturn("Sample Recipe Title").when(meta).getTitle();
          doReturn("sample_video_id").when(meta).getVideoId();
          doReturn(URI.create("https://example.com/thumbnail.jpg")).when(meta).getThumbnailUrl();
          doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(120).when(viewStatus).getLastPlaySeconds();
          doReturn(120).when(meta).getVideoSeconds();

          recentRecipes = new PageImpl<>(List.of(recentRecipe), pageable, 1);
          doReturn(recentRecipes)
              .when(recipeInfoService)
              .findRecents(any(UUID.class), any(Integer.class));
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
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              enumFields(
                                  "recent_recipes[].recipe_status",
                                  "레시피의 현재 상태: ",
                                  RecipeStatus.class),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findRecents(userId, page);

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
              .findRecents(userId, page);
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

          verify(recipeInfoService).findRecents(userId, page);
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

        private FullRecipeInfo fullRecipe;
        private RecipeYoutubeMeta youtubeMeta;
        private List<RecipeIngredient> ingredients;
        private RecipeViewStatus viewStatus;
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
          fullRecipe = mock(FullRecipeInfo.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          viewStatus = mock(RecipeViewStatus.class);

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

          RecipeTag tag = mock(RecipeTag.class);
          doReturn("한식").when(tag).getTag();
          tags = List.of(tag);

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
          doReturn(viewStatus).when(fullRecipe).getRecipeViewStatus();
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
              .findFullRecipe(any(UUID.class), any(UUID.class));
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

          verify(recipeInfoService).findFullRecipe(recipeId, userId);

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
          doReturn(null).when(recipeInfoService).findFullRecipe(recipeId, userId);
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

          verify(recipeInfoService).findFullRecipe(recipeId, userId);
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

          doReturn(recipeId).when(recipe).getId();
          doReturn(100).when(recipe).getViewCount(); // getCount() -> getViewCount()로 변경

          doReturn("Sample Recipe Title").when(youtubeMeta).getTitle();
          doReturn("sample_video_id").when(youtubeMeta).getVideoId();
          doReturn(URI.create("https://example.com/thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(URI.create("https://example.com/video")).when(youtubeMeta).getVideoUri();

          doReturn(recipe).when(recipeOverview).getRecipe();
          doReturn(youtubeMeta).when(recipeOverview).getYoutubeMeta();

          recipes = new PageImpl<>(List.of(recipeOverview), pageable, 1);
          doReturn(recipes).when(recipeInfoService).findPopulars(any(Integer.class));
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
                              fieldWithPath("recommend_recipes[].video_thumbnail_url")
                                  .description("레시피 비디오 썸네일 URL"),
                              fieldWithPath("recommend_recipes[].video_id")
                                  .description("레시피 비디오 ID"),
                              fieldWithPath("recommend_recipes[].count").description("레시피 조회 수"),
                              fieldWithPath("recommend_recipes[].video_url")
                                  .description("레시피 비디오 URL"),
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findPopulars(page);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("recommend_recipes")).hasSize(1);
          assertThat(responseBody.getString("recommend_recipes[0].recipe_id"))
              .isEqualTo(recipeId.toString());
          assertThat(responseBody.getString("recommend_recipes[0].recipe_title"))
              .isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("recommend_recipes[0].video_thumbnail_url"))
              .isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getString("recommend_recipes[0].video_id"))
              .isEqualTo("sample_video_id");
          assertThat(responseBody.getInt("recommend_recipes[0].count")).isEqualTo(100);
          assertThat(responseBody.getString("recommend_recipes[0].video_url"))
              .isEqualTo("https://example.com/video");
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
              .findPopulars(any(Integer.class));
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

          verify(recipeInfoService).findPopulars(page);
        }
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

        private Page<RecipeHistory> categorizedRecipes;
        private RecipeHistory categorizedRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta recipeDetailMeta;
        private RecipeViewStatus viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          categorizedRecipe = mock(RecipeHistory.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          recipeDetailMeta = mock(RecipeDetailMeta.class);
          viewStatus = mock(RecipeViewStatus.class);

          // RecipeHistory 구조에 맞춰 수정
          doReturn(recipe).when(categorizedRecipe).getRecipe();
          doReturn(viewStatus).when(categorizedRecipe).getRecipeViewStatus();
          doReturn(youtubeMeta).when(categorizedRecipe).getYoutubeMeta();
          doReturn(recipeDetailMeta).when(categorizedRecipe).getRecipeDetailMeta();

          doReturn(recipeId).when(recipe).getId();
          doReturn("Categorized Recipe Title").when(youtubeMeta).getTitle();
          doReturn("categorized_video_id").when(youtubeMeta).getVideoId();
          doReturn(URI.create("https://example.com/categorized_thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(180).when(youtubeMeta).getVideoSeconds();

          doReturn(30).when(recipeDetailMeta).getCookTime();
          doReturn(2).when(recipeDetailMeta).getServings();
          doReturn("Categorized Recipe Description").when(recipeDetailMeta).getDescription();
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0)).when(recipeDetailMeta).getCreatedAt();

          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(90).when(viewStatus).getLastPlaySeconds();
          doReturn(categoryId).when(viewStatus).getRecipeCategoryId();

          categorizedRecipes = new PageImpl<>(List.of(categorizedRecipe), pageable, 1);
          doReturn(categorizedRecipes)
              .when(recipeInfoService)
              .findCategorized(any(UUID.class), any(UUID.class), any(Integer.class));
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
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findCategorized(userId, categoryId, page);

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
        }

        @Test
        @DisplayName("Then - DetailMeta가 null이면 해당 필드는 null로 내려간다")
        void thenDetailMetaNullShouldReturnNullFields() {
          // DetailMeta를 null로 응답
          doReturn(null).when(categorizedRecipe).getRecipeDetailMeta();

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
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findCategorized(userId, categoryId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("categorized_recipes[0].description")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].servings")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].cook_time")).isNull();
          assertThatObject(responseBody.get("categorized_recipes[0].created_at")).isNull();
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
          doReturn(new PageImpl<RecipeHistory>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .findCategorized(userId, categoryId, page);
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

          verify(recipeInfoService).findCategorized(userId, categoryId, page);
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

        private Page<RecipeHistory> unCategorizedRecipes;
        private RecipeHistory unCategorizedRecipe;
        private Recipe recipe;
        private RecipeYoutubeMeta youtubeMeta;
        private RecipeDetailMeta recipeDetailMeta;
        private RecipeViewStatus viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          unCategorizedRecipe = mock(RecipeHistory.class);
          recipe = mock(Recipe.class);
          youtubeMeta = mock(RecipeYoutubeMeta.class);
          recipeDetailMeta = mock(RecipeDetailMeta.class);
          viewStatus = mock(RecipeViewStatus.class);

          doReturn(recipe).when(unCategorizedRecipe).getRecipe();
          doReturn(viewStatus).when(unCategorizedRecipe).getRecipeViewStatus();
          doReturn(youtubeMeta).when(unCategorizedRecipe).getYoutubeMeta();
          doReturn(recipeDetailMeta).when(unCategorizedRecipe).getRecipeDetailMeta();

          doReturn(recipeId).when(recipe).getId();
          doReturn("Uncategorized Recipe Title").when(youtubeMeta).getTitle();
          doReturn("uncategorized_video_id").when(youtubeMeta).getVideoId();
          doReturn(URI.create("https://example.com/uncategorized_thumbnail.jpg"))
              .when(youtubeMeta)
              .getThumbnailUrl();
          doReturn(240).when(youtubeMeta).getVideoSeconds();

          doReturn(30).when(recipeDetailMeta).getCookTime();
          doReturn(2).when(recipeDetailMeta).getServings();
          doReturn("Uncategorized Recipe Description").when(recipeDetailMeta).getDescription();
          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0)).when(recipeDetailMeta).getCreatedAt();

          doReturn(LocalDateTime.of(2024, 1, 25, 16, 45, 0)).when(viewStatus).getViewedAt();
          doReturn(150).when(viewStatus).getLastPlaySeconds();

          unCategorizedRecipes = new PageImpl<>(List.of(unCategorizedRecipe), pageable, 1);
          doReturn(unCategorizedRecipes)
              .when(recipeInfoService)
              .findUnCategorized(any(UUID.class), any(Integer.class));
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
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findUnCategorized(userId, page);

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
        }

        @Test
        @DisplayName("Then - DetailMeta가 null이면 해당 필드는 null로 내려간다")
        void thenDetailMetaNullShouldReturnNullFields() {
          // DetailMeta를 null로 응답
          doReturn(null).when(unCategorizedRecipe).getRecipeDetailMeta();

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
                              fieldWithPath("current_page").description("현재 페이지 번호"),
                              fieldWithPath("total_pages").description("전체 페이지 수"),
                              fieldWithPath("total_elements").description("전체 요소 수"),
                              fieldWithPath("has_next").description("다음 페이지 존재 여부"))));

          verify(recipeInfoService).findUnCategorized(userId, page);

          var responseBody = response.extract().jsonPath();
          assertThatObject(responseBody.get("unCategorized_recipes[0].description")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].servings")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].cook_time")).isNull();
          assertThatObject(responseBody.get("unCategorized_recipes[0].created_at")).isNull();
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
          doReturn(new PageImpl<RecipeHistory>(List.of(), pageable, 0))
              .when(recipeInfoService)
              .findUnCategorized(userId, page);
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

          verify(recipeInfoService).findUnCategorized(userId, page);
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

          private List<CountRecipeCategory> categories;
          private CountRecipeCategory categoryWithCount;
          private RecipeCategory category;
          private UUID categoryId;

          @BeforeEach
          void setUp() {
            categoryId = UUID.randomUUID();
            category = mock(RecipeCategory.class);
            categoryWithCount = mock(CountRecipeCategory.class);

            doReturn(categoryId).when(category).getId();
            doReturn("한식").when(category).getName();
            doReturn(category).when(categoryWithCount).getCategory();
            doReturn(5).when(categoryWithCount).getCount();

            categories = List.of(categoryWithCount);
            doReturn(categories).when(recipeInfoService).findCategories(any(UUID.class));
          }

          @Test
          @DisplayName("Then - 레시피 카테고리 목록을 성공적으로 반환해야 한다")
          void thenShouldReturnRecipeCategories() {
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
                                fieldWithPath("categories[].name").description("카테고리 이름"))));

            verify(recipeInfoService).findCategories(userId);

            var responseBody = response.extract().jsonPath();
            var categoriesList = responseBody.getList("categories");

            assertThat(categoriesList).hasSize(1);
            assertThat(responseBody.getUUID("categories[0].category_id")).isEqualTo(categoryId);
            assertThat(responseBody.getInt("categories[0].count")).isEqualTo(5);
            assertThat(responseBody.getString("categories[0].name")).isEqualTo("한식");
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
            doReturn(List.of()).when(recipeInfoService).findCategories(userId);
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
                .body("categories", hasSize(0));

            verify(recipeInfoService).findCategories(userId);
          }
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

          doReturn(progressStatus).when(recipeInfoService).findRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).findRecipeProgress(recipeId);

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

          doReturn(progressStatus).when(recipeInfoService).findRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).findRecipeProgress(recipeId);

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

          doReturn(progressStatus).when(recipeInfoService).findRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).findRecipeProgress(recipeId);
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

          doReturn(progressStatus).when(recipeInfoService).findRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).findRecipeProgress(recipeId);

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

          doReturn(progressStatus).when(recipeInfoService).findRecipeProgress(any(UUID.class));
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

          verify(recipeInfoService).findRecipeProgress(recipeId);

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
}
