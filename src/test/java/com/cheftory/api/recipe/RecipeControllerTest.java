package com.cheftory.api.recipe;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.dto.CategorizedRecipesResponse;
import com.cheftory.api.recipe.dto.CuisineRecipesResponse;
import com.cheftory.api.recipe.dto.RecentRecipesResponse;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeHistoryOverview;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecommendRecipesResponse;
import com.cheftory.api.recipe.dto.UnCategorizedRecipesResponse;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.creation.RecipeCreationFacade;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    @DisplayName("커서로 최근 레시피를 조회하면 커서 기반 응답을 반환한다")
    void shouldReturnRecentRecipesWithCursor() {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor-1";
        String nextCursor = "cursor-2";
        RecipeHistoryOverview overview = stubHistoryOverview();

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
        RecipeHistoryOverview overview = stubHistoryOverview();

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
        RecipeHistoryOverview overview = stubHistoryOverview();

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
                .get("/api/v1/recipes/cuisine/{type}", RecipeCuisineType.KOREAN.name().toLowerCase())
                .then()
                .status(HttpStatus.OK)
                .body("cuisine_recipes", hasSize(1))
                .body("has_next", equalTo(true))
                .body("next_cursor", equalTo(nextCursor));

        verify(recipeFacade).getCuisineRecipes(RecipeCuisineType.KOREAN, userId, cursor);
    }

    private void setUser(UUID userId) {
        var authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private RecipeHistoryOverview stubHistoryOverview() {
        RecipeHistoryOverview overview = mock(RecipeHistoryOverview.class);
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
}
