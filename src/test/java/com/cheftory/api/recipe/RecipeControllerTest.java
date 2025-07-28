package com.cheftory.api.recipe;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.model.IngredientsInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.model.FullRecipeInfo;
import com.cheftory.api.recipe.model.RecentRecipeOverview;
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

@DisplayName("Recipe Controller")
public class RecipeControllerTest extends RestDocsTest {

  private RecipeService recipeService;
  private RecipeController controller;

  @BeforeEach
  void setUp() {
    recipeService = mock(RecipeService.class);
    controller = new RecipeController(recipeService);

    mockMvc = mockMvcBuilder(controller)
        .withAdvice(new GlobalExceptionHandler())
        .withArgumentResolver(new UserArgumentResolver())
        .build();
  }

  @Nested
  @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
  class GivenValidUserId {

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
    @DisplayName("When - 레시피 최근 기록을 조회할 때")
    class WhenRequestingRecentRecipes {

      private List<RecentRecipeOverview> recentRecipes;
      private RecentRecipeOverview recentRecipe;
      private RecipeOverview recipe;
      private VideoInfo video;
      private RecipeViewStatusInfo viewStatus;

      @BeforeEach
      void setUp() {
        recentRecipe = mock(RecentRecipeOverview.class);
        recipe = mock(RecipeOverview.class);
        video = mock(VideoInfo.class);
        viewStatus = mock(RecipeViewStatusInfo.class);

        doReturn(recipe).when(recentRecipe).getRecipeOverview();
        doReturn(viewStatus).when(recentRecipe).getRecipeViewStatusInfo();
        doReturn(video).when(recipe).getVideoInfo();

        doReturn(recipeId).when(recipe).getId();
        doReturn("Sample Recipe Title").when(video).getTitle();
        doReturn("sample_video_id").when(video).getVideoId();
        doReturn(URI.create("https://example.com/thumbnail.jpg")).when(video).getThumbnailUrl();
        doReturn(LocalDateTime.of(2024, 1, 15, 10, 30, 0)).when(viewStatus).getViewedAt();
        doReturn(120).when(viewStatus).getLastPlaySeconds();

        recentRecipes = List.of(recentRecipe);
        doReturn(recentRecipes).when(recipeService).findRecents(any(UUID.class));
      }

      @Test
      @DisplayName("Then - 레시피 최근 기록을 성공적으로 반환한다")
      void thenShouldReturnRecentRecipes() {
        var response = given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .get("/api/v1/recipes/recent")
            .then()
            .status(HttpStatus.OK)
            .body("recent_recipes", hasSize(recentRecipes.size()))
            .apply(document(
                getNestedClassPath(this.getClass()) + "/{method-name}",
                requestPreprocessor(),
                responsePreprocessor(),
                requestAccessTokenFields(),
                responseFields(
                    fieldWithPath("recent_recipes").description("사용자의 최근 레시피 접근 기록"),
                    fieldWithPath("recent_recipes[].viewed_at").description("레시피 접근 시간"),
                    fieldWithPath("recent_recipes[].last_play_seconds").description(
                        "레시피 마지막 재생 시간"),
                    fieldWithPath("recent_recipes[].recipe_id").description("레시피 ID"),
                    fieldWithPath("recent_recipes[].video_id").description("레시피 비디오 ID"),
                    fieldWithPath("recent_recipes[].recipe_title").description("레시피 비디오 제목"),
                    fieldWithPath("recent_recipes[].video_thumbnail_url").description(
                        "레시피 비디오 썸네일 URL")
                )
            ));
        verify(recipeService).findRecents(userId);

        var responseBody = response.extract().jsonPath();
        var recentRecipes = responseBody.getList("recent_recipes");

        assertThat(recentRecipes).hasSize(1);

        assertThat(responseBody.getUUID("recent_recipes[0].recipe_id"))
            .isEqualTo(recipeId);
        assertThat(responseBody.getString("recent_recipes[0].video_id"))
            .isEqualTo("sample_video_id");
        assertThat(responseBody.getString("recent_recipes[0].recipe_title"))
            .isEqualTo("Sample Recipe Title");
        assertThat(responseBody.getString("recent_recipes[0].video_thumbnail_url"))
            .isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(responseBody.getString("recent_recipes[0].viewed_at"))
            .isEqualTo("2024-01-15T10:30:00");
        assertThat(responseBody.getInt("recent_recipes[0].last_play_seconds"))
            .isEqualTo(120);
      }
    }

    @Nested
    @DisplayName("When - 레시피 전체 정보를 조회할 때")
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
      @DisplayName("Then - 레시피 전체 정보를 성공적으로 반환한다")
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
                    enumFields("recipe_status", "레시피의 현재 상태 :", RecipeStatus.class),
                    fieldWithPath("video_info").description("레시피 비디오 정보"),
                    fieldWithPath("ingredients_info").description("레시피 재료 정보"),
                    fieldWithPath("recipe_steps").description("레시피 단계 정보"),
                    fieldWithPath("view_status").description("레시피 시청 상태 정보"),
                    fieldWithPath("video_info.video_id").description("레시피 비디오 ID"),
                    fieldWithPath("video_info.video_title").description("레시피 비디오 제목"),
                    fieldWithPath("video_info.video_thumbnail_url").description(
                        "레시피 비디오 썸네일 URL"),
                    fieldWithPath("video_info.video_seconds").description("레시피 비디오 재생 시간"),
                    fieldWithPath("ingredients_info.id").description("레시피 재료 ID"),
                    fieldWithPath("ingredients_info.ingredients").description("레시피 재료 정보"),
                    fieldWithPath("recipe_steps[].id").description("레시피 단계 ID"),
                    fieldWithPath("recipe_steps[].step_order").description("레시피 단계 순서"),
                    fieldWithPath("recipe_steps[].subtitle").description("레시피 단계 제목"),
                    fieldWithPath("recipe_steps[].details").description("레시피 단계 설명"),
                    fieldWithPath("recipe_steps[].start_time").description("레시피 단계 시작 시간"),
                    fieldWithPath("recipe_steps[].end_time").description("레시피 단계 종료 시간"),
                    fieldWithPath("view_status.id").description("레시피 시청 상태 ID"),
                    fieldWithPath("view_status.viewed_at").description("레시피 마지막 시청 시간"),
                    fieldWithPath("view_status.last_play_seconds").description("레시피 마지막 재생 시간"),
                    fieldWithPath("view_status.created_at").description("레시피 시청 상태 생성 시간"),
                    fieldWithPath("ingredients_info.ingredients").description("레시피 재료 정보"),
                    fieldWithPath("ingredients_info.id").description("레시피 재료 정보 ID"),
                    fieldWithPath("ingredients_info.ingredients[].name").description(
                        "레시피 재료 이름"),
                    fieldWithPath("ingredients_info.ingredients[].amount").description(
                        "레시피 재료 양"),
                    fieldWithPath("ingredients_info.ingredients[].unit").description(
                        "레시피 재료 단위")
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

    @Nested
    @DisplayName("When - 추천 레시피를 조회할 때")
    class WhenRequestingRecommendRecipes {

      private List<RecipeOverview> recipes;
      private UUID recipeId;
      private VideoInfo videoInfo;
      private RecipeOverview recipe;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipe = mock(RecipeOverview.class);
        videoInfo = mock(VideoInfo.class);

        doReturn(videoInfo).when(recipe).getVideoInfo();
        doReturn(recipeId).when(recipe).getId();
        doReturn(100).when(recipe).getCount();

        doReturn("Sample Recipe Title").when(videoInfo).getTitle();
        doReturn("sample_video_id").when(videoInfo).getVideoId();
        doReturn(URI.create("https://example.com/thumbnail.jpg")).when(videoInfo).getThumbnailUrl();

        recipes = List.of(recipe);
        doReturn(recipes).when(recipeService).findRecommends();
      }

      @Test
      @DisplayName("Then - 추천 레시피를 성공적으로 반환한다")
      void thenShouldReturnRecommendRecipes() {
        var response = given()
            .contentType(ContentType.JSON)
            .attribute("userId", userId.toString())
            .header("Authorization", "Bearer accessToken")
            .get("/api/v1/recipes/recommend")
            .then()
            .status(HttpStatus.OK)
            .apply(document(
                getNestedClassPath(this.getClass()) + "/{method-name}",
                requestPreprocessor(),
                responsePreprocessor(),
                requestAccessTokenFields(),
                responseFields(
                    fieldWithPath("recommend_recipes").description("추천 레시피 목록"),
                    fieldWithPath("recommend_recipes[].recipe_id").description("레시피 ID"),
                    fieldWithPath("recommend_recipes[].recipe_title").description("레시피 제목"),
                    fieldWithPath("recommend_recipes[].video_thumbnail_url").description("레시피 비디오 썸네일 URL"),
                    fieldWithPath("recommend_recipes[].video_id").description("레시피 비디오 ID"),
                    fieldWithPath("recommend_recipes[].count").description("레시피 조회 수")
                )
            ));

        var responseBody = response.extract().jsonPath();
        assertThat(responseBody.getList("recommend_recipes")).hasSize(1);

        verify(recipeService).findRecommends();

        assertThat(responseBody.getUUID("recommend_recipes[0].recipe_id")).isEqualTo(recipeId);
        assertThat(responseBody.getString("recommend_recipes[0].recipe_title")).isEqualTo("Sample Recipe Title");
        assertThat(responseBody.getString("recommend_recipes[0].video_thumbnail_url")).isEqualTo("https://example.com/thumbnail.jpg");
        assertThat(responseBody.getString("recommend_recipes[0].video_id")).isEqualTo("sample_video_id");
        assertThat(responseBody.getInt("recommend_recipes[0].count")).isEqualTo(100);
      }
    }
  }
}
