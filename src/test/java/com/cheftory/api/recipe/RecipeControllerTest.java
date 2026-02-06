package com.cheftory.api.recipe;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.creation.RecipeCreationFacade;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.RecipeBookmarkOverview;
import com.cheftory.api.recipe.dto.RecipeCategoryCounts;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("Recipe Controller")
public class RecipeControllerTest extends RestDocsTest {

    private RecipeFacade recipeFacade;
    private RecipeCreationFacade recipeCreationFacade;
    private RecipeController controller;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        recipeFacade = mock(RecipeFacade.class);
        recipeCreationFacade = mock(RecipeCreationFacade.class);
        controller = new RecipeController(recipeFacade, recipeCreationFacade);

        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Test
    @DisplayName("레시피를 생성한다")
    void shouldCreateRecipe() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        String videoUrl = "https://youtu.be/video-id";
        RecipeCreateRequest request = new RecipeCreateRequest(URI.create(videoUrl));

        setUser(userId);

        doReturn(recipeId).when(recipeCreationFacade).createBookmark(any(RecipeCreationTarget.User.class));

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .body(request)
                .post("/api/v1/recipes")
                .then()
                .status(HttpStatus.OK)
                .body("recipe_id", equalTo(recipeId.toString()));
    }

    @Test
    @DisplayName("레시피 상세 정보를 조회한다")
    void shouldReturnFullRecipe() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        FullRecipe fullRecipe = stubFullRecipe();

        setUser(userId);

        doReturn(fullRecipe).when(recipeFacade).getFullRecipe(recipeId, userId);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/{recipeId}", recipeId)
                .then()
                .status(HttpStatus.OK)
                .body(
                        "recipe_status",
                        equalTo(fullRecipe.getRecipe().getRecipeStatus().name()));
    }

    @Test
    @DisplayName("레시피 개요 정보를 조회한다")
    void shouldReturnRecipeOverview() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();
        RecipeOverview overview = stubRecipeOverview();

        setUser(userId);

        doReturn(overview).when(recipeFacade).getRecipeOverview(recipeId, userId);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/overview/{recipeId}", recipeId)
                .then()
                .status(HttpStatus.OK)
                .body("recipe_id", equalTo(overview.getRecipeId().toString()));
    }

    @Test
    @DisplayName("커서로 최근 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnRecentRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeBookmarkOverview overview = stubBookmarkOverview();

        setUser(userId);

        doReturn(CursorPage.of(List.of(overview), nextCursor))
                .when(recipeFacade)
                .getRecents(userId, cursor);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get("/api/v1/recipes/recent")
                .then()
                .status(HttpStatus.OK)
                .body("recent_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade).getRecents(userId, cursor);
    }

    @Test
    @DisplayName("커서로 추천 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnRecommendRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeOverview overview = stubRecipeOverview();

        setUser(userId);

        doReturn(CursorPage.of(List.of(overview), nextCursor))
                .when(recipeFacade)
                .getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, cursor, RecipeInfoVideoQuery.ALL);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get("/api/v1/recipes/recommend")
                .then()
                .status(HttpStatus.OK)
                .body("recommend_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade)
                .getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, cursor, RecipeInfoVideoQuery.ALL);
    }

    @Test
    @DisplayName("커서로 카테고리별 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnCategorizedRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeBookmarkOverview overview = stubBookmarkOverview();

        setUser(userId);

        doReturn(CursorPage.of(List.of(overview), nextCursor))
                .when(recipeFacade)
                .getCategorized(userId, categoryId, cursor);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get("/api/v1/recipes/categorized/{recipe_category_id}", categoryId)
                .then()
                .status(HttpStatus.OK)
                .body("categorized_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade).getCategorized(userId, categoryId, cursor);
    }

    @Test
    @DisplayName("커서로 미분류 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnUncategorizedRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeBookmarkOverview overview = stubBookmarkOverview();

        setUser(userId);

        doReturn(CursorPage.of(List.of(overview), nextCursor))
                .when(recipeFacade)
                .getUnCategorized(userId, cursor);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get("/api/v1/recipes/uncategorized")
                .then()
                .status(HttpStatus.OK)
                .body("unCategorized_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade).getUnCategorized(userId, cursor);
    }

    @Test
    @DisplayName("레시피 카테고리 목록을 조회한다")
    void shouldReturnRecipeCategories() {
        UUID userId = UUID.randomUUID();
        RecipeCategoryCounts counts = stubRecipeCategoryCounts();

        setUser(userId);

        doReturn(counts).when(recipeFacade).getUserCategoryCounts(userId);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .get("/api/v1/recipes/categories")
                .then()
                .status(HttpStatus.OK)
                .body("total_count", equalTo(counts.getTotalCount()));
    }

    @Test
    @DisplayName("레시피 카테고리를 삭제한다")
    void shouldDeleteRecipeCategory() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        setUser(userId);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .delete("/api/v1/recipes/categories/{recipeCategoryId}", categoryId)
                .then()
                .status(HttpStatus.OK);

        verify(recipeFacade).deleteCategory(categoryId);
    }

    @Test
    @DisplayName("레시피를 차단한다")
    void shouldBlockRecipe() {
        UUID userId = UUID.randomUUID();
        UUID recipeId = UUID.randomUUID();

        setUser(userId);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .post("/api/v1/recipes/block/{recipeId}", recipeId)
                .then()
                .status(HttpStatus.OK);

        verify(recipeFacade).blockRecipe(recipeId);
    }

    @Test
    @DisplayName("레시피 생성 진행 상태를 조회한다")
    void shouldReturnRecipeProgress() {
        UUID recipeId = UUID.randomUUID();
        RecipeProgressStatus status = stubRecipeProgressStatus();

        doReturn(status).when(recipeFacade).getRecipeProgress(recipeId);

        given().contentType(ContentType.JSON)
                .get("/api/v1/recipes/progress/{recipeId}", recipeId)
                .then()
                .status(HttpStatus.OK)
                .body(
                        "recipe_status",
                        equalTo(status.getRecipe().getRecipeStatus().name()));
    }

    @Test
    @DisplayName("커서로 요리 카테고리별 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnCuisineRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeOverview overview = stubRecipeOverview();

        setUser(userId);

        doReturn(CursorPage.of(List.of(overview), nextCursor))
                .when(recipeFacade)
                .getCuisineRecipes(RecipeCuisineType.KOREAN, userId, cursor);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get(
                        "/api/v1/recipes/cuisine/{type}",
                        RecipeCuisineType.KOREAN.name().toLowerCase())
                .then()
                .status(HttpStatus.OK)
                .body("cuisine_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade).getCuisineRecipes(RecipeCuisineType.KOREAN, userId, cursor);
    }

    @Test
    @DisplayName("챌린지 레시피를 조회한다")
    void shouldReturnChallengeRecipes() {
        UUID userId = UUID.randomUUID();
        UUID challengeId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeOverview overview = stubRecipeOverview();
        RecipeCompleteChallenge completeChallenge = RecipeCompleteChallenge.of(UUID.randomUUID(), true);

        setUser(userId);

        doReturn(Pair.of(List.of(completeChallenge), CursorPage.of(List.of(overview), nextCursor)))
                .when(recipeFacade)
                .getChallengeRecipes(challengeId, userId, cursor);

        given().contentType(ContentType.JSON)
                .header("Authorization", "Bearer accessToken")
                .param("cursor", cursor)
                .get("/api/v1/recipes/challenge/{challengeId}", challengeId)
                .then()
                .status(HttpStatus.OK)
                .body("challenge_recipes", hasSize(1))
                .body("complete_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));
    }

    private void setUser(UUID userId) {
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private RecipeBookmarkOverview stubBookmarkOverview() {
        RecipeBookmarkOverview overview = mock(RecipeBookmarkOverview.class);
        doReturn(UUID.randomUUID()).when(overview).getRecipeId();
        doReturn(RecipeStatus.SUCCESS).when(overview).getRecipeStatus();
        doReturn(0).when(overview).getViewCount();
        doReturn(LocalDateTime.now()).when(overview).getRecipeCreatedAt();
        doReturn(LocalDateTime.now()).when(overview).getRecipeUpdatedAt();
        doReturn(LocalDateTime.now()).when(overview).getViewedAt();
        doReturn(0).when(overview).getLastPlaySeconds();
        doReturn(UUID.randomUUID()).when(overview).getRecipeCategoryId();
        doReturn("recipe").when(overview).getVideoTitle();
        doReturn("channel").when(overview).getChannelTitle();
        doReturn("video-id").when(overview).getVideoId();
        doReturn(URI.create("https://example.com/video")).when(overview).getVideoUri();
        doReturn(URI.create("https://example.com/thumb")).when(overview).getThumbnailUrl();
        doReturn(120).when(overview).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(overview).getVideoType();
        doReturn("desc").when(overview).getDescription();
        doReturn(2).when(overview).getServings();
        doReturn(10).when(overview).getCookTime();
        doReturn(List.of("tag")).when(overview).getTags();
        doReturn(1L).when(overview).getCreditCost();
        return overview;
    }

    private RecipeOverview stubRecipeOverview() {
        RecipeOverview overview = mock(RecipeOverview.class);
        doReturn(UUID.randomUUID()).when(overview).getRecipeId();
        doReturn(RecipeStatus.SUCCESS).when(overview).getRecipeStatus();
        doReturn(0).when(overview).getViewCount();
        doReturn(LocalDateTime.now()).when(overview).getRecipeCreatedAt();
        doReturn(LocalDateTime.now()).when(overview).getRecipeUpdatedAt();
        doReturn("recipe").when(overview).getVideoTitle();
        doReturn("channel").when(overview).getChannelTitle();
        doReturn("video-id").when(overview).getVideoId();
        doReturn(URI.create("https://example.com/video")).when(overview).getVideoUri();
        doReturn(URI.create("https://example.com/thumb")).when(overview).getThumbnailUrl();
        doReturn(120).when(overview).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(overview).getVideoType();
        doReturn("desc").when(overview).getDescription();
        doReturn(2).when(overview).getServings();
        doReturn(10).when(overview).getCookTime();
        doReturn(List.of("tag")).when(overview).getTags();
        doReturn(true).when(overview).getIsViewed();
        doReturn(1L).when(overview).getCreditCost();
        return overview;
    }

    private FullRecipe stubFullRecipe() {
        FullRecipe fullRecipe = mock(FullRecipe.class);
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(RecipeStatus.SUCCESS).when(recipeInfo).getRecipeStatus();
        doReturn(recipeInfo).when(fullRecipe).getRecipe();

        RecipeYoutubeMeta youtubeMeta = mock(RecipeYoutubeMeta.class);
        doReturn("video-id").when(youtubeMeta).getVideoId();
        doReturn("title").when(youtubeMeta).getTitle();
        doReturn("channel").when(youtubeMeta).getChannelTitle();
        doReturn(URI.create("http://thumb")).when(youtubeMeta).getThumbnailUrl();
        doReturn(100).when(youtubeMeta).getVideoSeconds();
        doReturn(YoutubeMetaType.NORMAL).when(youtubeMeta).getType();
        doReturn(youtubeMeta).when(fullRecipe).getRecipeYoutubeMeta();

        doReturn(List.of()).when(fullRecipe).getRecipeIngredients();
        doReturn(List.of()).when(fullRecipe).getRecipeProgresses();
        doReturn(List.of()).when(fullRecipe).getRecipeSteps();
        doReturn(List.of()).when(fullRecipe).getRecipeTags();
        doReturn(List.of()).when(fullRecipe).getRecipeBriefings();

        return fullRecipe;
    }

    private RecipeCategoryCounts stubRecipeCategoryCounts() {
        RecipeCategoryCounts counts = mock(RecipeCategoryCounts.class);
        doReturn(10).when(counts).getTotalCount();
        doReturn(1).when(counts).getUncategorizedCount();
        doReturn(List.of()).when(counts).getCategorizedCounts();
        return counts;
    }

    private RecipeProgressStatus stubRecipeProgressStatus() {
        RecipeProgressStatus status = mock(RecipeProgressStatus.class);
        RecipeInfo recipe = mock(RecipeInfo.class);
        doReturn(RecipeStatus.IN_PROGRESS).when(recipe).getRecipeStatus();
        doReturn(recipe).when(status).getRecipe();
        doReturn(List.of()).when(status).getProgresses();
        return status;
    }
}
