package com.cheftory.api.recipe;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.creation.RecipeCreationFacade;
import com.cheftory.api.recipe.dto.CategorizedRecipesResponse;
import com.cheftory.api.recipe.dto.ChallengeRecipesResponse;
import com.cheftory.api.recipe.dto.CuisineRecipesResponse;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.FullRecipeResponse;
import com.cheftory.api.recipe.dto.RecentRecipesResponse;
import com.cheftory.api.recipe.dto.RecipeCategoryCounts;
import com.cheftory.api.recipe.dto.RecipeCategoryCountsResponse;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.dto.RecipeCreateResponse;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeOverviewResponse;
import com.cheftory.api.recipe.dto.RecipeProgressResponse;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.recipe.dto.RecommendRecipesResponse;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class RecipeController {
    private final RecipeFacade recipeFacade;
    private final RecipeCreationFacade recipeCreationFacade;

    @PostMapping("/api/v1/recipes")
    public RecipeCreateResponse create(@RequestBody RecipeCreateRequest request, @UserPrincipal UUID userId)
            throws RecipeException, CreditException {
        UUID recipeId = recipeCreationFacade.createBookmark(request.toUserTarget(userId));
        return RecipeCreateResponse.from(recipeId);
    }

    @GetMapping("/api/v1/recipes/{recipeId}")
    public FullRecipeResponse getFullRecipe(@PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId)
            throws CheftoryException {
        FullRecipe info = recipeFacade.getFullRecipe(recipeId, userId);
        return FullRecipeResponse.of(info);
    }

    @GetMapping("/api/v1/recipes/overview/{recipeId}")
    public RecipeOverviewResponse getOverviewRecipe(@PathVariable("recipeId") UUID recipeId, @UserPrincipal UUID userId)
            throws RecipeInfoException, RecipeDetailMetaException, YoutubeMetaException {
        RecipeOverview overview = recipeFacade.getRecipeOverview(recipeId, userId);
        return RecipeOverviewResponse.of(overview);
    }

    @GetMapping("/api/v1/recipes/recent")
    public RecentRecipesResponse getRecentInfos(
            @UserPrincipal UUID userId, @RequestParam(required = false) String cursor) throws CursorException {
        return RecentRecipesResponse.from(recipeFacade.getRecents(userId, cursor));
    }

    @GetMapping("/api/v1/recipes/recommend")
    @Deprecated
    public RecommendRecipesResponse getRecommendedRecipesDefault(
            @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
            @UserPrincipal UUID userId,
            @RequestParam(required = false) String cursor)
            throws CheftoryException {
        return RecommendRecipesResponse.from(
                recipeFacade.getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, cursor, query));
    }

    @GetMapping("/api/v1/recipes/recommend/{type}")
    public RecommendRecipesResponse getRecommendedRecipes(
            @PathVariable String type,
            @RequestParam(defaultValue = "ALL") RecipeInfoVideoQuery query,
            @UserPrincipal UUID userId,
            @RequestParam(required = false) String cursor)
            throws CheftoryException {
        RecipeInfoRecommendType recommendType = RecipeInfoRecommendType.fromString(type);
        return RecommendRecipesResponse.from(recipeFacade.getRecommendRecipes(recommendType, userId, cursor, query));
    }

    @GetMapping("/api/v1/recipes/categorized/{recipeCategoryId}")
    public CategorizedRecipesResponse getCategorizedRecipes(
            @PathVariable("recipeCategoryId") UUID categoryId,
            @UserPrincipal UUID userId,
            @RequestParam(required = false) String cursor)
            throws CursorException {
        return CategorizedRecipesResponse.from(recipeFacade.getCategorized(userId, categoryId, cursor));
    }

    @DeleteMapping("/api/v1/recipes/categories/{recipeCategoryId}")
    public SuccessOnlyResponse deleteRecipeCategory(@UserPrincipal UUID userId, @PathVariable UUID recipeCategoryId)
            throws RecipeCategoryException, RecipeBookmarkException {
        recipeFacade.deleteCategory(userId, recipeCategoryId);
        return SuccessOnlyResponse.create();
    }

    @GetMapping("/api/v1/recipes/categories")
    public RecipeCategoryCountsResponse getRecipeCategories(@UserPrincipal UUID userId) {
        RecipeCategoryCounts categories = recipeFacade.getUserCategoryCounts(userId);
        return RecipeCategoryCountsResponse.from(categories);
    }

    @GetMapping("/api/v1/recipes/progress/{recipeId}")
    public RecipeProgressResponse getRecipeProgress(@PathVariable("recipeId") UUID recipeId)
            throws RecipeInfoException {
        RecipeProgressStatus progressStatus = recipeFacade.getRecipeProgress(recipeId);
        return RecipeProgressResponse.of(progressStatus);
    }

    @PostMapping("/api/v1/recipes/block/{recipeId}")
    public SuccessOnlyResponse blockRecipe(@PathVariable UUID recipeId) throws RecipeException {
        recipeFacade.blockRecipe(recipeId);
        return SuccessOnlyResponse.create();
    }

    @PostMapping("/papi/v1/recipes")
    public RecipeCreateResponse createCrawledRecipe(@RequestBody RecipeCreateRequest request)
            throws RecipeException, CreditException {
        UUID recipeId = recipeCreationFacade.createBookmark(request.toCrawlerTarget());
        return RecipeCreateResponse.from(recipeId);
    }

    @GetMapping("/papi/v1/recipes/progress/{recipeId}")
    public RecipeProgressResponse getCrawledRecipeProgress(@PathVariable("recipeId") UUID recipeId)
            throws RecipeInfoException {
        RecipeProgressStatus progressStatus = recipeFacade.getRecipeProgress(recipeId);
        return RecipeProgressResponse.of(progressStatus);
    }

    @GetMapping("/api/v1/recipes/cuisine/{type}")
    public CuisineRecipesResponse getBrowseRecipes(
            @PathVariable String type, @RequestParam(required = false) String cursor, @UserPrincipal UUID userId)
            throws CheftoryException {
        RecipeCuisineType cuisineType = RecipeCuisineType.fromString(type);
        CursorPage<RecipeOverview> recipes = recipeFacade.getCuisineRecipes(cuisineType, userId, cursor);
        return CuisineRecipesResponse.from(recipes);
    }

    @PocOnly(until = "2025-12-31")
    @GetMapping("/api/v1/recipes/challenge/{challengeId}")
    public ChallengeRecipesResponse getChallengeRecipes(
            @PathVariable UUID challengeId, @RequestParam(required = false) String cursor, @UserPrincipal UUID userId)
            throws RecipeChallengeException, CursorException {
        Pair<List<RecipeCompleteChallenge>, CursorPage<RecipeOverview>> result =
                recipeFacade.getChallengeRecipes(challengeId, userId, cursor);
        return ChallengeRecipesResponse.from(result.getFirst(), result.getSecond());
    }
}
