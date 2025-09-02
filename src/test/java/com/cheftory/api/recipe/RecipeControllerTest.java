package com.cheftory.api.recipe;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.recipe.category.RecipeCategory;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.model.CountRecipeCategory;
import com.cheftory.api.recipe.model.IngredientsInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecipeHistoryOverview;
import com.cheftory.api.recipe.model.RecipeOverview;
import com.cheftory.api.recipe.model.RecipeStepInfo;
import com.cheftory.api.recipe.model.RecipeViewStatusInfo;
import com.cheftory.api.utils.RestDocsTest;

import java.time.LocalDateTime;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import com.cheftory.api.exception.GlobalExceptionHandler;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.cheftory.api.utils.RestDocsUtils.enumFields;
import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

@DisplayName("Recipe Controller")
public class RecipeControllerTest extends RestDocsTest {

  private RecipeService recipeService;
  private RecipeController controller;
  private GlobalExceptionHandler exceptionHandler;
  private UserArgumentResolver userArgumentResolver;

  @BeforeEach
  void setUp() {
    recipeService = mock(RecipeService.class);
    controller = new RecipeController(recipeService);
    exceptionHandler = new GlobalExceptionHandler();
    userArgumentResolver = new UserArgumentResolver();

    mockMvc = mockMvcBuilder(controller)
        .withAdvice(exceptionHandler)
        .withArgumentResolver(userArgumentResolver)
        .build();
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
        private RecipeOverview recipe;
        private VideoInfo video;
        private RecipeViewStatusInfo viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recentRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(RecipeOverview.class);
          video = mock(VideoInfo.class);
          viewStatus = mock(RecipeViewStatusInfo.class);
          page = 0;
          pageable = Pageable.ofSize(10);

          doReturn(recipe).when(recentRecipe).getRecipeOverview();
          doReturn(viewStatus).when(recentRecipe).getRecipeViewStatusInfo();
          doReturn(video).when(recipe).getVideoInfo();

          doReturn(recipeId).when(recipe).getId();
          doReturn("Sample Recipe Title").when(video).getTitle();
          doReturn("sample_video_id").when(video).getVideoId();
          doReturn(URI.create("https://example.com/thumbnail.jpg")).when(video).getThumbnailUrl();
          doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(120).when(viewStatus).getLastPlaySeconds();
          doReturn(120).when(video).getVideoSeconds();

          recentRecipes = new PageImpl<>(List.of(recentRecipe), pageable, 1);
          doReturn(recentRecipes).when(recipeService).findRecents(any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 레시피 최근 기록을 성공적으로 반환해야 한다")
        void thenShouldReturnRecentRecipes() {
          var response = given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/recent")
              .then()
              .status(HttpStatus.OK)
              .body("recent_recipes", hasSize(recentRecipes.getContent().size()))
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  queryParameters(
                      parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional()
                  ),
                  responseFields(
                      fieldWithPath("recent_recipes").description("사용자의 최근 레시피 접근 기록"),
                      fieldWithPath("recent_recipes[].viewed_at").description("레시피 접근 시간"),
                      fieldWithPath("recent_recipes[].last_play_seconds").description("레시피 마지막 재생 시간"),
                      fieldWithPath("recent_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("recent_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("recent_recipes[].recipe_title").description("레시피 비디오 제목"),
                      fieldWithPath("recent_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("recent_recipes[].video_seconds").description("레시피 비디오 재생 시간"),
                      fieldWithPath("current_page").description("현재 페이지 번호"),
                      fieldWithPath("total_pages").description("전체 페이지 수"),
                      fieldWithPath("total_elements").description("전체 요소 수"),
                      fieldWithPath("has_next").description("다음 페이지 존재 여부")
                  )
              ));

          verify(recipeService).findRecents(userId, page);

          var responseBody = response.extract().jsonPath();
          var recentRecipesList = responseBody.getList("recent_recipes");

          assertThat(recentRecipesList).hasSize(1);
          assertThat(responseBody.getUUID("recent_recipes[0].recipe_id")).isEqualTo(recipeId);
          assertThat(responseBody.getString("recent_recipes[0].video_id")).isEqualTo("sample_video_id");
          assertThat(responseBody.getString("recent_recipes[0].recipe_title")).isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("recent_recipes[0].video_thumbnail_url")).isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getString("recent_recipes[0].viewed_at")).isEqualTo("2024-01-15T10:30:00");
          assertThat(responseBody.getInt("recent_recipes[0].last_play_seconds")).isEqualTo(120);
          assertThat(responseBody.getInt("recent_recipes[0].video_seconds")).isEqualTo(120);
          assertThat(responseBody.getString("current_page")).isEqualTo("0");
          assertThat(responseBody.getString("total_pages")).isEqualTo("1");
          assertThat(responseBody.getString("total_elements")).isEqualTo("1");
          assertThat(responseBody.getBoolean("has_next")).isEqualTo(false);
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
          doReturn(new PageImpl<>(List.of(), pageable, 0)).when(recipeService).findRecents(userId, page);
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

          verify(recipeService).findRecents(userId, page);
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
        private VideoInfo video;
        private IngredientsInfo ingredients;
        private RecipeViewStatusInfo viewStatus;
        private RecipeStepInfo recipeStep;
        private UUID ingredientsId;
        private UUID viewStatusId;
        private UUID stepId;

        @BeforeEach
        void setUp() {
          fullRecipe = mock(FullRecipeInfo.class);
          video = mock(VideoInfo.class);
          ingredients = mock(IngredientsInfo.class);
          viewStatus = mock(RecipeViewStatusInfo.class);
          recipeStep = mock(RecipeStepInfo.class);

          doReturn(RecipeStatus.COMPLETED).when(fullRecipe).getRecipeStatus();
          doReturn(video).when(fullRecipe).getVideoInfo();
          doReturn(ingredients).when(fullRecipe).getIngredientsInfo();
          doReturn(List.of(recipeStep)).when(fullRecipe).getRecipeStepInfos();
          doReturn(viewStatus).when(fullRecipe).getRecipeViewStatusInfo();

          doReturn("sample_video_id").when(video).getVideoId();
          doReturn("Sample Recipe Title").when(video).getTitle();
          doReturn(URI.create("https://example.com/thumbnail.jpg")).when(video).getThumbnailUrl();
          doReturn(120).when(video).getVideoSeconds();

          ingredientsId = UUID.randomUUID();
          var ingredient = mock(Ingredient.class);
          doReturn("토마토").when(ingredient).getName();
          doReturn(2).when(ingredient).getAmount();
          doReturn("개").when(ingredient).getUnit();

          doReturn(ingredientsId).when(ingredients).getIngredientsId();
          doReturn(List.of(ingredient)).when(ingredients).getIngredients();

          stepId = UUID.randomUUID();
          doReturn(stepId).when(recipeStep).getId();
          doReturn(1).when(recipeStep).getStepOrder();
          doReturn("Step 1 Title").when(recipeStep).getSubtitle();
          doReturn(List.of("Step 1 detail")).when(recipeStep).getDetails();
          doReturn(0.0).when(recipeStep).getStart();
          doReturn(30.0).when(recipeStep).getEnd();

          viewStatusId = UUID.randomUUID();
          doReturn(viewStatusId).when(viewStatus).getId();
          doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(60).when(viewStatus).getLastPlaySeconds();
          doReturn(LocalDateTime.of(2024, 1, 14, 10, 30, 0)).when(viewStatus).getCreatedAt();

          doReturn(fullRecipe).when(recipeService).findFullRecipe(any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("Then - 레시피 전체 정보를 성공적으로 반환해야 한다")
        void thenShouldReturnRecipeDetail() {
          var response = given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/recipes/{recipe_id}", recipeId)
              .then()
              .status(HttpStatus.OK)
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  pathParameters(
                      parameterWithName("recipe_id").description("조회할 레시피 ID")
                  ),
                  responseFields(
                      enumFields("recipe_status", "레시피의 현재 상태: ", RecipeStatus.class),
                      fieldWithPath("video_info").description("레시피 비디오 정보"),
                      fieldWithPath("ingredients_info").description("레시피 재료 정보"),
                      fieldWithPath("recipe_steps").description("레시피 단계 정보"),
                      fieldWithPath("view_status").description("레시피 시청 상태 정보"),
                      fieldWithPath("video_info.video_id").description("레시피 비디오 ID"),
                      fieldWithPath("video_info.video_title").description("레시피 비디오 제목"),
                      fieldWithPath("video_info.video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("video_info.video_seconds").description("레시피 비디오 재생 시간"),
                      fieldWithPath("ingredients_info.id").description("레시피 재료 ID"),
                      fieldWithPath("ingredients_info.ingredients").description("레시피 재료 정보"),
                      fieldWithPath("ingredients_info.ingredients[].name").description("레시피 재료 이름"),
                      fieldWithPath("ingredients_info.ingredients[].amount").description("레시피 재료 양"),
                      fieldWithPath("ingredients_info.ingredients[].unit").description("레시피 재료 단위"),
                      fieldWithPath("recipe_steps[].id").description("레시피 단계 ID"),
                      fieldWithPath("recipe_steps[].step_order").description("레시피 단계 순서"),
                      fieldWithPath("recipe_steps[].subtitle").description("레시피 단계 제목"),
                      fieldWithPath("recipe_steps[].details").description("레시피 단계 설명"),
                      fieldWithPath("recipe_steps[].start_time").description("레시피 단계 시작 시간"),
                      fieldWithPath("recipe_steps[].end_time").description("레시피 단계 종료 시간"),
                      fieldWithPath("view_status.id").description("레시피 시청 상태 ID"),
                      fieldWithPath("view_status.viewed_at").description("레시피 마지막 시청 시간"),
                      fieldWithPath("view_status.last_play_seconds").description("레시피 마지막 재생 시간"),
                      fieldWithPath("view_status.created_at").description("레시피 시청 상태 생성 시간")
                  )
              ));

          verify(recipeService).findFullRecipe(recipeId, userId);

          var responseBody = response.extract().jsonPath();

          assertThat(responseBody.getString("recipe_status")).isEqualTo(RecipeStatus.COMPLETED.name());
          assertThat(responseBody.getString("video_info.video_id")).isEqualTo("sample_video_id");
          assertThat(responseBody.getString("video_info.video_title")).isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("video_info.video_thumbnail_url")).isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getInt("video_info.video_seconds")).isEqualTo(120);
          assertThat(responseBody.getUUID("ingredients_info.id")).isEqualTo(ingredientsId);
          assertThat(responseBody.getString("ingredients_info.ingredients[0].name")).isEqualTo("토마토");
          assertThat(responseBody.getInt("ingredients_info.ingredients[0].amount")).isEqualTo(2);
          assertThat(responseBody.getString("ingredients_info.ingredients[0].unit")).isEqualTo("개");
          assertThat(responseBody.getUUID("view_status.id")).isEqualTo(viewStatusId);
          assertThat(responseBody.getString("view_status.viewed_at")).isEqualTo("2024-01-15T10:30:00");
          assertThat(responseBody.getInt("view_status.last_play_seconds")).isEqualTo(60);
          assertThat(responseBody.getString("view_status.created_at")).isEqualTo("2024-01-14T10:30:00");
          assertThat(responseBody.getUUID("recipe_steps[0].id")).isEqualTo(stepId);
          assertThat(responseBody.getInt("recipe_steps[0].step_order")).isEqualTo(1);
          assertThat(responseBody.getString("recipe_steps[0].subtitle")).isEqualTo("Step 1 Title");
          assertThat(responseBody.getList("recipe_steps[0].details")).isEqualTo(List.of("Step 1 detail"));
          assertThat(responseBody.getDouble("recipe_steps[0].start_time")).isEqualTo(0.0);
          assertThat(responseBody.getDouble("recipe_steps[0].end_time")).isEqualTo(30.0);
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
          doReturn(null).when(recipeService).findFullRecipe(recipeId, userId);
        }

        @Test
        @DisplayName("Then - 400 Bad Request를 반환해야 한다")
        void thenShouldReturnNotFound() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .get("/api/v1/recipes/{recipe_id}", recipeId)
              .then()
              .status(HttpStatus.BAD_REQUEST);

          verify(recipeService).findFullRecipe(recipeId, userId);
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
        private VideoInfo videoInfo;
        private RecipeOverview recipe;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          recipe = mock(RecipeOverview.class);
          videoInfo = mock(VideoInfo.class);
          pageable = Pageable.ofSize(10);

          doReturn(videoInfo).when(recipe).getVideoInfo();
          doReturn(recipeId).when(recipe).getId();
          doReturn(100).when(recipe).getCount();

          doReturn("Sample Recipe Title").when(videoInfo).getTitle();
          doReturn("sample_video_id").when(videoInfo).getVideoId();
          doReturn(URI.create("https://example.com/thumbnail.jpg")).when(videoInfo).getThumbnailUrl();
          doReturn(URI.create("https://example.com/video")).when(videoInfo).getVideoUri();

          recipes = new PageImpl<>(List.of(recipe), pageable, 1);
          doReturn(recipes).when(recipeService).findRecommends(any(Integer.class));
        }

        @Test
        @DisplayName("Then - 추천 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnRecommendRecipes() {
          var response = given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/recommend")
              .then()
              .status(HttpStatus.OK)
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  queryParameters(
                      parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional()
                  ),
                  responseFields(
                      fieldWithPath("recommend_recipes").description("추천 레시피 목록"),
                      fieldWithPath("recommend_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("recommend_recipes[].recipe_title").description("레시피 제목"),
                      fieldWithPath("recommend_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("recommend_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("recommend_recipes[].count").description("레시피 조회 수"),
                      fieldWithPath("recommend_recipes[].video_url").description("레시피 비디오 URL"),
                      fieldWithPath("current_page").description("현재 페이지 번호"),
                      fieldWithPath("total_pages").description("전체 페이지 수"),
                      fieldWithPath("total_elements").description("전체 요소 수"),
                      fieldWithPath("has_next").description("다음 페이지 존재 여부")
                  )
              ));

          verify(recipeService).findRecommends(page);

          var responseBody = response.extract().jsonPath();
          assertThat(responseBody.getList("recommend_recipes")).hasSize(1);
          assertThat(responseBody.getString("recommend_recipes[0].recipe_id")).isEqualTo(recipeId.toString());
          assertThat(responseBody.getString("recommend_recipes[0].recipe_title")).isEqualTo("Sample Recipe Title");
          assertThat(responseBody.getString("recommend_recipes[0].video_thumbnail_url")).isEqualTo("https://example.com/thumbnail.jpg");
          assertThat(responseBody.getString("recommend_recipes[0].video_id")).isEqualTo("sample_video_id");
          assertThat(responseBody.getInt("recommend_recipes[0].count")).isEqualTo(100);
          assertThat(responseBody.getString("recommend_recipes[0].video_url")).isEqualTo("https://example.com/video");
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
          doReturn(new PageImpl<>(List.of(), pageable, 0)).when(recipeService).findRecommends(any(Integer.class));
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

          verify(recipeService).findRecommends(page);
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

        private Page<RecipeHistoryOverview> categorizedRecipes;
        private RecipeHistoryOverview categorizedRecipe;
        private RecipeOverview recipe;
        private VideoInfo video;
        private RecipeViewStatusInfo viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          categorizedRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(RecipeOverview.class);
          video = mock(VideoInfo.class);
          viewStatus = mock(RecipeViewStatusInfo.class);

          doReturn(recipe).when(categorizedRecipe).getRecipeOverview();
          doReturn(viewStatus).when(categorizedRecipe).getRecipeViewStatusInfo();
          doReturn(video).when(recipe).getVideoInfo();

          doReturn(recipeId).when(recipe).getId();
          doReturn("Categorized Recipe Title").when(video).getTitle();
          doReturn("categorized_video_id").when(video).getVideoId();
          doReturn(URI.create("https://example.com/categorized_thumbnail.jpg")).when(video).getThumbnailUrl();
          doReturn(180).when(video).getVideoSeconds();

          doReturn(LocalDateTime.of(2024, 1, 20, 14, 30, 0)).when(viewStatus).getViewedAt();
          doReturn(90).when(viewStatus).getLastPlaySeconds();
          doReturn(categoryId).when(viewStatus).getCategoryId();

          categorizedRecipes = new PageImpl<>(List.of(categorizedRecipe), pageable, 1);
          doReturn(categorizedRecipes).when(recipeService).findCategorized(any(UUID.class), any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 카테고리별 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnCategorizedRecipes() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
              .then()
              .status(HttpStatus.OK)
              .body("categorized_recipes", hasSize(categorizedRecipes.getContent().size()))
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  pathParameters(
                      parameterWithName("recipe_category_id").description("조회할 레시피 카테고리 ID")
                  ),
                  queryParameters(
                      parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional()
                  ),
                  responseFields(
                      fieldWithPath("categorized_recipes").description("카테고리별 레시피 목록"),
                      fieldWithPath("categorized_recipes[].viewed_at").description("레시피 접근 시간"),
                      fieldWithPath("categorized_recipes[].last_play_seconds").description("레시피 마지막 재생 시간"),
                      fieldWithPath("categorized_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("categorized_recipes[].recipe_title").description("레시피 제목"),
                      fieldWithPath("categorized_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("categorized_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("categorized_recipes[].video_seconds").description("레시피 비디오 재생 시간"),
                      fieldWithPath("categorized_recipes[].category_id").description("레시피 카테고리 ID"),
                      fieldWithPath("current_page").description("현재 페이지 번호"),
                      fieldWithPath("total_pages").description("전체 페이지 수"),
                      fieldWithPath("total_elements").description("전체 요소 수"),
                      fieldWithPath("has_next").description("다음 페이지 존재 여부")
                  )
                            ));

          verify(recipeService).findCategorized(userId, categoryId, page);
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
          doReturn(new PageImpl<>(List.of(), pageable, 0)).when(recipeService).findCategorized(userId, categoryId, page);
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

          verify(recipeService).findCategorized(userId, categoryId, page);
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
        private RecipeOverview recipe;
        private VideoInfo video;
        private RecipeViewStatusInfo viewStatus;
        private UUID recipeId;
        private Integer page;
        private Pageable pageable;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          page = 0;
          pageable = Pageable.ofSize(10);
          unCategorizedRecipe = mock(RecipeHistoryOverview.class);
          recipe = mock(RecipeOverview.class);
          video = mock(VideoInfo.class);
          viewStatus = mock(RecipeViewStatusInfo.class);

          doReturn(recipe).when(unCategorizedRecipe).getRecipeOverview();
          doReturn(viewStatus).when(unCategorizedRecipe).getRecipeViewStatusInfo();
          doReturn(video).when(recipe).getVideoInfo();

          doReturn(recipeId).when(recipe).getId();
          doReturn("Uncategorized Recipe Title").when(video).getTitle();
          doReturn("uncategorized_video_id").when(video).getVideoId();
          doReturn(URI.create("https://example.com/uncategorized_thumbnail.jpg")).when(video).getThumbnailUrl();
          doReturn(240).when(video).getVideoSeconds();

          doReturn(LocalDateTime.of(2024, 1, 25, 16, 45, 0)).when(viewStatus).getViewedAt();
          doReturn(150).when(viewStatus).getLastPlaySeconds();

          unCategorizedRecipes = new PageImpl<>(List.of(unCategorizedRecipe), pageable, 1);
          doReturn(unCategorizedRecipes).when(recipeService).findUnCategorized(any(UUID.class), any(Integer.class));
        }

        @Test
        @DisplayName("Then - 미분류 레시피를 성공적으로 반환해야 한다")
        void thenShouldReturnUnCategorizedRecipes() {
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", userId.toString())
              .header("Authorization", "Bearer accessToken")
              .param("page", page)
              .get("/api/v1/recipes/uncategorized")
              .then()
              .status(HttpStatus.OK)
              .body("unCategorized_recipes", hasSize(unCategorizedRecipes.getContent().size()))
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  queryParameters(
                      parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional()
                  ),
                  responseFields(
                      fieldWithPath("unCategorized_recipes").description("미분류 레시피 목록"),
                      fieldWithPath("unCategorized_recipes[].viewed_at").description("레시피 접근 시간"),
                      fieldWithPath("unCategorized_recipes[].last_play_seconds").description("레시피 마지막 재생 시간"),
                      fieldWithPath("unCategorized_recipes[].recipe_id").description("레시피 ID"),
                      fieldWithPath("unCategorized_recipes[].recipe_title").description("레시피 제목"),
                      fieldWithPath("unCategorized_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                      fieldWithPath("unCategorized_recipes[].video_id").description("레시피 비디오 ID"),
                      fieldWithPath("unCategorized_recipes[].video_seconds").description("레시피 비디오 재생 시간"),
                      fieldWithPath("current_page").description("현재 페이지 번호"),
                      fieldWithPath("total_pages").description("전체 페이지 수"),
                      fieldWithPath("total_elements").description("전체 요소 수"),
                      fieldWithPath("has_next").description("다음 페이지 존재 여부")
                  )
              ));

          verify(recipeService).findUnCategorized(userId, page);
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
          doReturn(new PageImpl<>(List.of(), pageable, 0)).when(recipeService).findUnCategorized(userId, page);
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

          verify(recipeService).findUnCategorized(userId, page);
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
          var response = given()
              .contentType(ContentType.JSON)
              .header("Authorization", "Bearer accessToken")
              .delete("/api/v1/recipes/categories/{recipe_category_id}", categoryId)
              .then()
              .status(HttpStatus.OK)
              .apply(document(
                  getNestedClassPath(this.getClass()) + "/{method-name}",
                  requestPreprocessor(),
                  responsePreprocessor(),
                  requestAccessTokenFields(),
                  pathParameters(
                      parameterWithName("recipe_category_id").description("삭제할 레시피 카테고리 ID")
                  ),
                  responseFields(
                      fieldWithPath("message").description("성공 메시지")
                  )
              ));
          assertSuccessResponse(response);
          verify(recipeService).deleteCategory(categoryId);
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

          verify(recipeService).deleteCategory(categoryId);
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
            doReturn(categories).when(recipeService).findCategories(any(UUID.class));
          }

          @Test
          @DisplayName("Then - 레시피 카테고리 목록을 성공적으로 반환해야 한다")
          void thenShouldReturnRecipeCategories() {
            var response = given()
                .contentType(ContentType.JSON)
                .attribute("userId", userId.toString())
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/categories")
                .then()
                .status(HttpStatus.OK)
                .body("categories", hasSize(categories.size()))
                .apply(document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestAccessTokenFields(),
                    responseFields(
                        fieldWithPath("categories").description("레시피 카테고리 목록"),
                        fieldWithPath("categories[].category_id").description("카테고리 ID"),
                        fieldWithPath("categories[].count").description("해당 카테고리의 레시피 수"),
                        fieldWithPath("categories[].name").description("카테고리 이름")
                    )
                ));

            verify(recipeService).findCategories(userId);

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
            doReturn(List.of()).when(recipeService).findCategories(userId);
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

            verify(recipeService).findCategories(userId);
          }
        }
      }
    }
  }
}