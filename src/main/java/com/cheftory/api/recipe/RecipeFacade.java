package com.cheftory.api.recipe;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.challenge.RecipeChallengeService;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.RecipeCategoryCount;
import com.cheftory.api.recipe.dto.RecipeCategoryCounts;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeHistoryOverview;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.history.RecipeHistoryService;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCount;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCount;
import com.cheftory.api.recipe.rank.RankingType;
import com.cheftory.api.recipe.rank.RecipeRankService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeFacade {

    private final RecipeStepService recipeStepService;
    private final RecipeHistoryService recipeHistoryService;
    private final RecipeCategoryService recipeCategoryService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;
    private final RecipeIngredientService recipeIngredientService;
    private final RecipeDetailMetaService recipeDetailMetaService;
    private final RecipeProgressService recipeProgressService;
    private final RecipeTagService recipeTagService;
    private final RecipeBriefingService recipeBriefingService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeRankService recipeRankService;
    private final RecipeChallengeService recipeChallengeService;

    /**
     * 레시피 상세 정보를 조회합니다. * 레시피가 존재하지 않으면 예외를 던집니다. * 레시피가 실패 상태이면 예외를 던집니다. 레시피가 성공 상태이면 조회수를 증가시킵니다.
     */
    public FullRecipe viewFullRecipe(UUID recipeId, UUID userId) {
        try {
            RecipeInfo recipe = recipeInfoService.getSuccess(recipeId);
            List<RecipeStep> steps = recipeStepService.gets(recipeId);
            List<RecipeIngredient> ingredients = recipeIngredientService.gets(recipeId);
            RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
            List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
            List<RecipeTag> tags = recipeTagService.gets(recipeId);
            List<RecipeBriefing> briefings = recipeBriefingService.gets(recipeId);
            RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
            RecipeHistory history = recipeHistoryService.getWithView(userId, recipeId);

            return FullRecipe.of(
                    steps, ingredients, detailMeta, progresses, tags, youtubeMeta, history, recipe, briefings);
        } catch (RecipeException e) {
            if (e.getErrorMessage() == RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND
                    || e.getErrorMessage() == RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND) {
                throw new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND);
            }
            if (e.getErrorMessage() == RecipeInfoErrorCode.RECIPE_FAILED) {
                throw new RecipeException(RecipeErrorCode.RECIPE_FAILED);
            }
            throw e;
        }
    }

    public RecipeOverview getRecipeOverview(UUID recipeId, UUID userId) {
        RecipeInfo recipe = recipeInfoService.getSuccess(recipeId);
        RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
        RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
        List<RecipeTag> tags = recipeTagService.gets(recipeId);
        Boolean isViewed = recipeHistoryService.exist(userId, recipeId);

        return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags, isViewed);
    }

    @Deprecated(forRemoval = true)
    public Page<RecipeHistoryOverview> getCategorized(UUID userId, UUID recipeCategoryId, int page) {
        Page<RecipeHistory> histories = recipeHistoryService.getCategorized(userId, recipeCategoryId, page);

        List<RecipeHistoryOverview> content = makeHistoryOverviews(histories.getContent());
        return new PageImpl<>(content, histories.getPageable(), histories.getTotalElements());
    }

    public CursorPage<RecipeHistoryOverview> getCategorized(UUID userId, UUID recipeCategoryId, String cursor) {
        CursorPage<RecipeHistory> histories = recipeHistoryService.getCategorized(userId, recipeCategoryId, cursor);

        List<RecipeHistoryOverview> items = makeHistoryOverviews(histories.items());
        return CursorPage.of(items, histories.nextCursor());
    }

    @Deprecated(forRemoval = true)
    public Page<RecipeHistoryOverview> getUnCategorized(UUID userId, int page) {
        Page<RecipeHistory> histories = recipeHistoryService.getUnCategorized(userId, page);

        List<RecipeHistoryOverview> content = makeHistoryOverviews(histories.getContent());
        return new PageImpl<>(content, histories.getPageable(), histories.getTotalElements());
    }

    public CursorPage<RecipeHistoryOverview> getUnCategorized(UUID userId, String cursor) {
        CursorPage<RecipeHistory> histories = recipeHistoryService.getUnCategorized(userId, cursor);

        List<RecipeHistoryOverview> items = makeHistoryOverviews(histories.items());
        return CursorPage.of(items, histories.nextCursor());
    }

    @Deprecated(forRemoval = true)
    public Page<RecipeHistoryOverview> getRecents(UUID userId, int page) {
        Page<RecipeHistory> histories = recipeHistoryService.getRecents(userId, page);

        List<RecipeHistoryOverview> content = makeHistoryOverviews(histories.getContent());
        return new PageImpl<>(content, histories.getPageable(), histories.getTotalElements());
    }

    public CursorPage<RecipeHistoryOverview> getRecents(UUID userId, String cursor) {
        CursorPage<RecipeHistory> histories = recipeHistoryService.getRecents(userId, cursor);

        List<RecipeHistoryOverview> items = makeHistoryOverviews(histories.items());
        return CursorPage.of(items, histories.nextCursor());
    }

    /**
     * 히스토리 목록을 만듭니다. 1) viewStatus에서 recipeId 수집 2) recipeId로 실패하지 않은 Recipe 불러오기 (in_progress,
     * success) 3) recipeId로 RecipeYoutube 불러오기 (metaId 수집) 3-2) metaId 목록 수집 4) metaId로
     * RecipeYoutubeMeta 불러오기 5) 매핑: 누락은 스킵하며 로그
     *
     * @param histories 히스토리 페이지
     * @return 리코드 페이지
     */
    private List<RecipeHistoryOverview> makeHistoryOverviews(List<RecipeHistory> histories) {
        List<UUID> recipeIds =
                histories.stream().map(RecipeHistory::getRecipeId).toList();

        Map<UUID, RecipeInfo> recipeMap = recipeInfoService.getValidRecipes(recipeIds).stream()
                .collect(Collectors.toMap(RecipeInfo::getId, Function.identity()));

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity(), (a, b) -> a));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.getIn(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        return histories.stream()
                .map(history -> {
                    UUID recipeId = history.getRecipeId();

                    RecipeInfo recipe = recipeMap.get(recipeId);
                    if (recipe == null) {
                        log.warn("히스토리: 존재하지 않는 레시피 recipeId={}, userId={}", recipeId, history.getUserId());
                        return null;
                    }

                    RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                    if (youtubeMeta == null) {
                        log.warn("히스토리: 유튜브 메타 엔티티 누락 recipeId={}", recipeId);
                        return null;
                    }

                    RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);
                    List<RecipeTag> tags = tagsMap.getOrDefault(recipeId, Collections.emptyList());

                    return RecipeHistoryOverview.of(recipe, history, youtubeMeta, detailMeta, tags);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<RecipeOverview> makeOverviews(List<RecipeInfo> recipes, UUID userId) {
        List<UUID> recipeIds = recipes.stream().map(RecipeInfo::getId).toList();

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.getByRecipes(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.getIn(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        Map<UUID, RecipeHistory> recipeViewStatusMap = recipeHistoryService.getByRecipes(recipeIds, userId).stream()
                .collect(Collectors.toMap(RecipeHistory::getRecipeId, Function.identity()));

        return recipes.stream()
                .map(recipe -> {
                    UUID recipeId = recipe.getId();
                    RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                    if (youtubeMeta == null) {
                        log.error("완료된 레시피의 유튜브 메타데이터 누락: recipeId={}", recipeId);
                        return null;
                    }

                    RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);
                    if (detailMeta == null) {
                        log.warn("레시피 상세 메타데이터 누락: recipeId={}", recipeId);
                    }

                    List<RecipeTag> tags = tagsMap.getOrDefault(recipeId, Collections.emptyList());
                    if (tags.isEmpty()) {
                        log.error("레시피의 태그 누락: recipeId={}", recipeId);
                    }

                    RecipeHistory history = recipeViewStatusMap.get(recipeId);
                    Boolean isViewed = history != null;

                    return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags, isViewed);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public RecipeCategoryCounts getUserCategoryCounts(UUID userId) {
        List<RecipeCategory> categories = recipeCategoryService.getUsers(userId);
        List<UUID> categoryIds = categories.stream().map(RecipeCategory::getId).toList();

        List<RecipeHistoryCategorizedCount> categorizedCounts = recipeHistoryService.countByCategories(categoryIds);
        Map<UUID, Integer> countMap = categorizedCounts.stream()
                .collect(Collectors.toMap(
                        RecipeHistoryCategorizedCount::getCategoryId, RecipeHistoryCategorizedCount::getCount));

        RecipeHistoryUnCategorizedCount uncategorizedCount = recipeHistoryService.countUncategorized(userId);

        List<RecipeCategoryCount> categoriesWithCount = categories.stream()
                .map(category -> RecipeCategoryCount.of(category, countMap.getOrDefault(category.getId(), 0)))
                .toList();

        return RecipeCategoryCounts.of(uncategorizedCount.getCount(), categoriesWithCount);
    }

    public void deleteCategory(UUID categoryId) {
        recipeHistoryService.unCategorize(categoryId);
        recipeCategoryService.delete(categoryId);
    }

    public RecipeProgressStatus getRecipeProgress(UUID recipeId) {
        List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
        RecipeInfo recipe = recipeInfoService.get(recipeId);
        return RecipeProgressStatus.of(recipe, progresses);
    }

    @Transactional
    public void blockRecipe(UUID recipeId) {
        try {
            recipeYoutubeMetaService.block(recipeId);
            recipeInfoService.block(recipeId);
            recipeHistoryService.blockByRecipe(recipeId);
        } catch (RecipeException e) {
            if (e.getErrorMessage() == YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO) {
                log.warn("차단되지 않은 영상에 대해 레시피 차단 시도 recipeId={}", recipeId);
                throw new RecipeException(RecipeErrorCode.RECIPE_NOT_BLOCKED_VIDEO);
            }
            throw e;
        }
    }

    @Deprecated(forRemoval = true)
    public Page<RecipeOverview> getCuisineRecipes(RecipeCuisineType type, UUID userId, int page) {
        Page<RecipeInfo> recipesPage = recipeInfoService.getCuisines(type, page);

        List<RecipeOverview> content = makeOverviews(recipesPage.getContent(), userId);
        return new PageImpl<>(content, recipesPage.getPageable(), recipesPage.getTotalElements());
    }

    public CursorPage<RecipeOverview> getCuisineRecipes(RecipeCuisineType type, UUID userId, String cursor) {
        CursorPage<UUID> recipeIds = recipeRankService.getCuisineRecipes(userId, type, cursor);
        List<RecipeInfo> recipes = recipeInfoService.gets(recipeIds.items());

        List<RecipeOverview> items = makeOverviews(recipes, userId);
        return CursorPage.of(items, recipeIds.nextCursor());
    }

    @Deprecated(forRemoval = true)
    public Page<RecipeOverview> getRecommendRecipes(
            RecipeInfoRecommendType type, UUID userId, int page, RecipeInfoVideoQuery query) {

        Page<RecipeInfo> recipesPage =
                switch (type) {
                    case POPULAR -> recipeInfoService.getPopulars(page, query);
                    case CHEF -> getRankingRecipes(RankingType.CHEF, page);
                    case TRENDING -> getRankingRecipes(RankingType.TRENDING, page);
                };

        List<RecipeOverview> content = makeOverviews(recipesPage.getContent(), userId);
        return new PageImpl<>(content, recipesPage.getPageable(), recipesPage.getTotalElements());
    }

    public CursorPage<RecipeOverview> getRecommendRecipes(
            RecipeInfoRecommendType type, UUID userId, String cursor, RecipeInfoVideoQuery query) {
        CursorPage<RecipeInfo> recipesPage =
                switch (type) {
                    case POPULAR -> recipeInfoService.getPopulars(cursor, query);
                    case CHEF -> getRankingRecipes(RankingType.CHEF, cursor);
                    case TRENDING -> getRankingRecipes(RankingType.TRENDING, cursor);
                };

        List<RecipeOverview> items = makeOverviews(recipesPage.items(), userId);
        return CursorPage.of(items, recipesPage.nextCursor());
    }

    @PocOnly(until = "2025-12-31")
    public Pair<List<RecipeCompleteChallenge>, Page<RecipeOverview>> getChallengeRecipes(
            UUID challengeId, UUID userId, int page) {
        Page<RecipeCompleteChallenge> overviews = recipeChallengeService.getChallengeRecipes(userId, challengeId, page);

        List<RecipeCompleteChallenge> challengeOverviews = overviews.getContent();
        List<UUID> recipeIds = challengeOverviews.stream()
                .map(RecipeCompleteChallenge::getRecipeId)
                .toList();

        List<RecipeOverview> recipeOverviews = makeOverviews(recipeInfoService.getValidRecipes(recipeIds), userId);

        return Pair.of(
                challengeOverviews,
                new PageImpl<>(recipeOverviews, overviews.getPageable(), overviews.getTotalElements()));
    }

    @PocOnly(until = "2025-12-31")
    public Pair<List<RecipeCompleteChallenge>, CursorPage<RecipeOverview>> getChallengeRecipes(
            UUID challengeId, UUID userId, String cursor) {
        CursorPage<RecipeCompleteChallenge> overviews =
                recipeChallengeService.getChallengeRecipes(userId, challengeId, cursor);

        List<RecipeCompleteChallenge> challengeOverviews = overviews.items();
        List<UUID> recipeIds = challengeOverviews.stream()
                .map(RecipeCompleteChallenge::getRecipeId)
                .toList();

        List<RecipeOverview> fetched = makeOverviews(recipeInfoService.getValidRecipes(recipeIds), userId);

        Map<UUID, RecipeOverview> map =
                fetched.stream().collect(Collectors.toMap(RecipeOverview::getRecipeId, Function.identity()));

        List<RecipeOverview> ordered =
                recipeIds.stream().map(map::get).filter(Objects::nonNull).toList();

        return Pair.of(challengeOverviews, CursorPage.of(ordered, overviews.nextCursor()));
    }

    @Deprecated(forRemoval = true)
    private Page<RecipeInfo> getRankingRecipes(RankingType rankingType, int page) {
        Page<UUID> recipeIds = recipeRankService.getRecipeIds(rankingType, page);
        List<RecipeInfo> recipes =
                recipeInfoService.getValidRecipes(recipeIds.stream().toList());

        return new PageImpl<>(recipes, recipeIds.getPageable(), recipeIds.getTotalElements());
    }

    private CursorPage<RecipeInfo> getRankingRecipes(RankingType rankingType, String cursor) {
        CursorPage<UUID> rankedIdsPage = recipeRankService.getRecipeIds(rankingType, cursor);

        List<UUID> rankedIds = rankedIdsPage.items();

        List<RecipeInfo> fetched = recipeInfoService.getValidRecipes(rankedIds);

        Map<UUID, RecipeInfo> map = fetched.stream().collect(Collectors.toMap(RecipeInfo::getId, Function.identity()));

        List<RecipeInfo> ordered =
                rankedIds.stream().map(map::get).filter(Objects::nonNull).toList();

        return CursorPage.of(ordered, rankedIdsPage.nextCursor());
    }
}
