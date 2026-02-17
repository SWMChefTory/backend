package com.cheftory.api.recipe;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.recipe.challenge.RecipeChallengeService;
import com.cheftory.api.recipe.challenge.RecipeCompleteChallenge;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.RecipeBookmarkOverview;
import com.cheftory.api.recipe.dto.RecipeCategoryCount;
import com.cheftory.api.recipe.dto.RecipeCategoryCounts;
import com.cheftory.api.recipe.dto.PublicRecipeDetail;
import com.cheftory.api.recipe.dto.PublicRecipeOverview;
import com.cheftory.api.recipe.dto.PublicRecipeSitemapResponse;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.SitemapEntry;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.rank.RankingType;
import com.cheftory.api.recipe.rank.RecipeRankService;
import com.cheftory.api.recipe.rank.port.RecipeRankEventType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 레시피 퍼사드.
 *
 * <p>레시피 관련 복합 작업을 조율합니다. 레시피 조회, 북마크, 카테고리, 랭킹 등
 * 다양한 서비스를 조합하여 레시피 도메인의 비즈니스 로직을 처리합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeFacade {

    private final RecipeStepService recipeStepService;
    private final RecipeBookmarkService recipeBookmarkService;
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
     * 레시피 전체 상세 정보를 조회합니다.
     *
     * <p>레시피가 존재하지 않거나 실패 상태이면 예외를 던집니다.
     * 레시피가 성공 상태이면 조회수를 증가시키고 조회 이벤트를 로깅합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 레시피 전체 정보 (스텝, 재료, 메타데이터, 태그, 브리핑 등)
     * @throws CheftoryException 레시피를 찾을 수 없거나 실패 상태인 경우
     */
    public FullRecipe getFullRecipe(UUID recipeId, UUID userId) throws CheftoryException {
        try {
            RecipeInfo recipe = recipeInfoService.getSuccess(recipeId);
            recipeInfoService.increaseCount(recipeId);
            List<RecipeStep> steps = recipeStepService.gets(recipeId);
            List<RecipeIngredient> ingredients = recipeIngredientService.gets(recipeId);
            RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
            List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
            List<RecipeTag> tags = recipeTagService.gets(recipeId);
            List<RecipeBriefing> briefings = recipeBriefingService.gets(recipeId);
            RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
            recipeRankService.logEvent(userId, recipeId, RecipeRankEventType.VIEW);
            if (recipeBookmarkService.exist(userId, recipeId)) {
                RecipeBookmark bookmark = recipeBookmarkService.get(userId, recipeId);
                return FullRecipe.owned(
                        steps, ingredients, detailMeta, progresses, tags, youtubeMeta, bookmark, recipe, briefings);
            }
            return FullRecipe.notOwned(
                    steps, ingredients, detailMeta, progresses, tags, youtubeMeta, recipe, briefings);

        } catch (CheftoryException e) {
            if (e.getError() == RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND
                    || e.getError() == RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND) {
                throw new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND, e);
            }
            if (e.getError() == RecipeInfoErrorCode.RECIPE_FAILED) {
                throw new RecipeException(RecipeErrorCode.RECIPE_FAILED, e);
            }
            throw e;
        }
    }

    /**
     * 레시피 개요 정보를 조회합니다.
     *
     * <p>레시피 기본 정보, YouTube 메타데이터, 상세 메타데이터, 태그를 포함합니다.
     * 조회 시 조회수를 증가시킵니다.</p>
     *
     * @param recipeId 레시피 ID
     * @param userId 사용자 ID
     * @return 레시피 개요 정보
     * @throws RecipeInfoException 레시피 정보 조회 실패 시
     * @throws YoutubeMetaException YouTube 메타데이터 조회 실패 시
     * @throws RecipeDetailMetaException 상세 메타데이터 조회 실패 시
     */
    public RecipeOverview getRecipeOverview(UUID recipeId, UUID userId)
            throws RecipeInfoException, YoutubeMetaException, RecipeDetailMetaException {
        RecipeInfo recipe = recipeInfoService.getSuccess(recipeId);
        recipeInfoService.increaseCount(recipeId);
        RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
        RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
        List<RecipeTag> tags = recipeTagService.gets(recipeId);
        boolean isViewed = recipeBookmarkService.exist(userId, recipeId);

        return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags, isViewed);
    }

    /**
     * 특정 카테고리의 북마크된 레시피 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param recipeCategoryId 레시피 카테고리 ID
     * @param cursor 페이징 커서
     * @return 북마크된 레시피 개요 목록
     * @throws CursorException 커서 디코딩 실패 시
     */
    public CursorPage<RecipeBookmarkOverview> getCategorized(UUID userId, UUID recipeCategoryId, String cursor)
            throws CursorException {
        CursorPage<RecipeBookmark> bookmarks = recipeBookmarkService.getCategorized(userId, recipeCategoryId, cursor);

        List<RecipeBookmarkOverview> items = makeBookmarkOverviews(bookmarks.items());
        return CursorPage.of(items, bookmarks.nextCursor());
    }

    /**
     * 최근에 북마크한 레시피 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @return 최근 북마크한 레시피 개요 목록
     * @throws CursorException 커서 디코딩 실패 시
     */
    public CursorPage<RecipeBookmarkOverview> getRecents(UUID userId, String cursor) throws CursorException {
        CursorPage<RecipeBookmark> bookmarks = recipeBookmarkService.getRecents(userId, cursor);

        List<RecipeBookmarkOverview> items = makeBookmarkOverviews(bookmarks.items());
        return CursorPage.of(items, bookmarks.nextCursor());
    }

    /**
     * 북마크 목록을 만듭니다. 1) viewStatus에서 recipeId 수집 2) recipeId로 실패하지 않은 Recipe 불러오기 (in_progress,
     * success) 3) recipeId로 RecipeYoutube 불러오기 (metaId 수집) 3-2) metaId 목록 수집 4) metaId로
     * RecipeYoutubeMeta 불러오기 5) 매핑: 누락은 스킵하며 로그
     *
     * @param bookmarks 북마크 페이지
     * @return 리코드 페이지
     */
    private List<RecipeBookmarkOverview> makeBookmarkOverviews(List<RecipeBookmark> bookmarks) {
        List<UUID> recipeIds =
                bookmarks.stream().map(RecipeBookmark::getRecipeId).toList();

        Map<UUID, RecipeInfo> recipeMap = Stream.concat(
                        recipeInfoService.gets(recipeIds).stream(), recipeInfoService.getProgresses(recipeIds).stream())
                .collect(Collectors.toMap(RecipeInfo::getId, Function.identity()));

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.gets(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity(), (a, b) -> a));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.gets(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        return bookmarks.stream()
                .map(bookmark -> {
                    UUID recipeId = bookmark.getRecipeId();

                    RecipeInfo recipe = recipeMap.get(recipeId);
                    if (recipe == null) {
                        log.warn("북마크: 존재하지 않는 레시피 recipeId={}, userId={}", recipeId, bookmark.getUserId());
                        return null;
                    }

                    RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                    if (youtubeMeta == null) {
                        log.warn("북마크: 유튜브 메타 엔티티 누락 recipeId={}", recipeId);
                        return null;
                    }

                    RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);
                    List<RecipeTag> tags = tagsMap.getOrDefault(recipeId, Collections.emptyList());

                    return RecipeBookmarkOverview.of(recipe, bookmark, youtubeMeta, detailMeta, tags);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<RecipeOverview> makeOverviews(List<RecipeInfo> recipes, UUID userId) {
        List<UUID> recipeIds = recipes.stream().map(RecipeInfo::getId).toList();

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.gets(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity()));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.gets(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        Map<UUID, RecipeBookmark> recipeViewStatusMap = recipeBookmarkService.gets(recipeIds, userId).stream()
                .collect(Collectors.toMap(RecipeBookmark::getRecipeId, Function.identity()));

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

                    RecipeBookmark bookmark = recipeViewStatusMap.get(recipeId);
                    Boolean isViewed = bookmark != null;

                    return RecipeOverview.of(recipe, youtubeMeta, detailMeta, tags, isViewed);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 사용자의 카테고리별 북마크 수를 조회합니다.
     *
     * <p>카테고리별 북마크 수와 미분류 북마크 수를 모두 포함합니다.</p>
     *
     * @param userId 사용자 ID
     * @return 카테고리별 북마크 수 집계
     */
    public RecipeCategoryCounts getUserCategoryCounts(UUID userId) {
        List<RecipeCategory> categories = recipeCategoryService.getUsers(userId);
        List<UUID> categoryIds = categories.stream().map(RecipeCategory::getId).toList();

        List<RecipeBookmarkCategorizedCount> categorizedCounts = recipeBookmarkService.countByCategories(categoryIds);
        Map<UUID, Integer> countMap = categorizedCounts.stream()
                .collect(Collectors.toMap(
                        RecipeBookmarkCategorizedCount::getCategoryId, RecipeBookmarkCategorizedCount::getCount));

        RecipeBookmarkUnCategorizedCount uncategorizedCount = recipeBookmarkService.countUncategorized(userId);

        List<RecipeCategoryCount> categoriesWithCount = categories.stream()
                .map(category -> RecipeCategoryCount.of(category, countMap.getOrDefault(category.getId(), 0)))
                .toList();

        return RecipeCategoryCounts.of(uncategorizedCount.getCount(), categoriesWithCount);
    }

    /**
     * 사용자 카테고리를 삭제하고 관련 북마크의 카테고리를 해제합니다.
     *
     * @param userId 사용자 ID
     * @param categoryId 삭제할 카테고리 ID
     * @throws RecipeCategoryException 카테고리 삭제 실패 시
     * @throws RecipeBookmarkException 북마크 카테고리 해제 실패 시
     */
    public void deleteCategory(UUID userId, UUID categoryId) throws RecipeCategoryException, RecipeBookmarkException {
        recipeCategoryService.delete(userId, categoryId);
        recipeBookmarkService.unCategorize(categoryId);
    }

    /**
     * 레시피 생성 진행 상태를 조회합니다.
     *
     * @param recipeId 레시피 ID
     * @return 레시피 생성 진행 상태
     * @throws RecipeInfoException 레시피 정보 조회 실패 시
     */
    public RecipeProgressStatus getRecipeProgress(UUID recipeId) throws RecipeInfoException {
        List<RecipeProgress> progresses = recipeProgressService.gets(recipeId);
        RecipeInfo recipe = recipeInfoService.get(recipeId);
        return RecipeProgressStatus.of(recipe, progresses);
    }

    /**
     * 레시피를 차단합니다.
     *
     * <p>YouTube 메타데이터, 레시피 정보, 북마크를 차단 상태로 변경합니다.</p>
     *
     * @param recipeId 차단할 레시피 ID
     * @throws RecipeException 레시피 차단 실패 시
     */
    @Transactional(rollbackFor = RecipeException.class)
    public void blockRecipe(UUID recipeId) throws RecipeException {
        try {
            recipeYoutubeMetaService.block(recipeId);
            recipeInfoService.block(recipeId);
            recipeBookmarkService.block(recipeId);
        } catch (RecipeException e) {
            if (e.getError() == YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO) {
                log.warn("차단되지 않은 영상에 대해 레시피 차단 시도 recipeId={}", recipeId);
                throw new RecipeException(RecipeErrorCode.RECIPE_NOT_BLOCKED_VIDEO, e);
            }
            throw e;
        }
    }

    /**
     * 요리 타입별 추천 레시피 목록을 조회합니다.
     *
     * @param type 요리 타입
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @return 추천 레시피 개요 목록
     * @throws CheftoryException 추천 조회 실패 시
     */
    public CursorPage<RecipeOverview> getCuisineRecipes(RecipeCuisineType type, UUID userId, String cursor)
            throws CheftoryException {
        CursorPage<UUID> recipeIds = recipeRankService.getCuisineRecipes(userId, type, cursor);
        List<RecipeInfo> recipes = recipeInfoService.gets(recipeIds.items());

        List<RecipeOverview> items = makeOverviews(recipes, userId);
        return CursorPage.of(items, recipeIds.nextCursor());
    }

    /**
     * 추천 타입별 레시피 목록을 조회합니다.
     *
     * <p>인기, 셰프 추천, 트렌딩 중 하나의 타입으로 레시피를 추천합니다.</p>
     *
     * @param type 추천 타입 (POPULAR, CHEF, TRENDING)
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @param query 비디오 조회 쿼리 옵션
     * @return 추천 레시피 개요 목록
     * @throws CheftoryException 추천 조회 실패 시
     */
    public CursorPage<RecipeOverview> getRecommendRecipes(
            RecipeInfoRecommendType type, UUID userId, String cursor, RecipeInfoVideoQuery query)
            throws CheftoryException {
        CursorPage<RecipeInfo> recipesPage =
                switch (type) {
                    case POPULAR -> recipeInfoService.getPopulars(cursor, query);
                    case CHEF -> getRankingRecipes(RankingType.CHEF, cursor);
                    case TRENDING -> getRankingRecipes(RankingType.TRENDING, cursor);
                };

        List<RecipeOverview> items = makeOverviews(recipesPage.items(), userId);
        return CursorPage.of(items, recipesPage.nextCursor());
    }

    /**
     * 챌린지에 포함된 레시피 목록과 완료 상태를 조회합니다.
     *
     * @param challengeId 챌린지 ID
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @return 챌린지 완료 상태 목록과 레시피 개요 목록의 쌍
     * @throws RecipeChallengeException 챌린지 조회 실패 시
     * @throws CursorException 커서 디코딩 실패 시
     */
    @PocOnly(until = "2025-12-31")
    public Pair<List<RecipeCompleteChallenge>, CursorPage<RecipeOverview>> getChallengeRecipes(
            UUID challengeId, UUID userId, String cursor) throws RecipeChallengeException, CursorException {
        CursorPage<RecipeCompleteChallenge> overviews =
                recipeChallengeService.getChallengeRecipes(userId, challengeId, cursor);

        List<RecipeCompleteChallenge> challengeOverviews = overviews.items();
        List<UUID> recipeIds = challengeOverviews.stream()
                .map(RecipeCompleteChallenge::getRecipeId)
                .toList();

        List<RecipeOverview> fetched = makeOverviews(recipeInfoService.gets(recipeIds), userId);

        Map<UUID, RecipeOverview> map =
                fetched.stream().collect(Collectors.toMap(RecipeOverview::getRecipeId, Function.identity()));

        List<RecipeOverview> ordered =
                recipeIds.stream().map(map::get).filter(Objects::nonNull).toList();

        return Pair.of(challengeOverviews, CursorPage.of(ordered, overviews.nextCursor()));
    }

    private CursorPage<RecipeInfo> getRankingRecipes(RankingType rankingType, String cursor) throws CheftoryException {
        CursorPage<UUID> rankedIdsPage = recipeRankService.getRecipeIds(rankingType, cursor);

        List<UUID> rankedIds = rankedIdsPage.items();

        List<RecipeInfo> fetched = recipeInfoService.gets(rankedIds);

        Map<UUID, RecipeInfo> map = fetched.stream().collect(Collectors.toMap(RecipeInfo::getId, Function.identity()));

        List<RecipeInfo> ordered =
                rankedIds.stream().map(map::get).filter(Objects::nonNull).toList();

        return CursorPage.of(ordered, rankedIdsPage.nextCursor());
    }

    // ── 공개 레시피 API용 ──

    /**
     * 공개 레시피 목록 조회 (cuisine 필터 옵션)
     *
     * @param cuisine 요리 종류 (null이면 전체)
     * @param cursor 페이징 커서
     * @return 공개 레시피 개요 커서 페이지
     * @throws CheftoryException 조회 실패 시
     */
    public CursorPage<PublicRecipeOverview> getPublicRecipes(String cuisine, String cursor) throws CheftoryException {
        CursorPage<RecipeInfo> page;
        if (cuisine != null && !cuisine.isBlank()) {
            RecipeCuisineType type = RecipeCuisineType.fromString(cuisine);
            page = recipeInfoService.getPublicCuisineRecipes(type, cursor);
        } else {
            page = recipeInfoService.getPublicRecipes(cursor);
        }

        List<PublicRecipeOverview> overviews = makePublicOverviews(page.items());
        return CursorPage.of(overviews, page.nextCursor());
    }

    /**
     * 공개 레시피 상세 조회 (viewCount 증가 안함 — SEO 봇 트래픽 오염 방지)
     *
     * @param recipeId 레시피 ID
     * @return 공개 레시피 상세 정보
     * @throws CheftoryException 레시피를 찾을 수 없을 때
     */
    public PublicRecipeDetail getPublicRecipeById(UUID recipeId) throws CheftoryException {
        RecipeInfo recipe = recipeInfoService.getByIdPublic(recipeId)
                .orElseThrow(() -> new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND));

        RecipeDetailMeta detailMeta = recipeDetailMetaService.get(recipeId);
        RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaService.get(recipeId);
        List<RecipeIngredient> ingredients = recipeIngredientService.gets(recipeId);
        List<RecipeStep> steps = recipeStepService.gets(recipeId);
        List<RecipeTag> tags = recipeTagService.gets(recipeId);
        List<RecipeBriefing> briefings = recipeBriefingService.gets(recipeId);

        return PublicRecipeDetail.of(recipe, detailMeta, youtubeMeta, ingredients, steps, tags, briefings);
    }

    /**
     * 사이트맵 데이터 조회
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사이트맵 응답
     */
    public PublicRecipeSitemapResponse getSitemapEntries(int page, int size) {
        List<SitemapEntry> entries = recipeInfoService.getPublicForSitemap(page, size).stream()
                .map(r -> new SitemapEntry(r.getId(), r.getUpdatedAt()))
                .toList();
        long total = recipeInfoService.countPublicRecipes();
        return new PublicRecipeSitemapResponse(entries, total);
    }

    /**
     * 공개 레시피 목록용 배치 조회 헬퍼 (N+1 방지)
     */
    private List<PublicRecipeOverview> makePublicOverviews(List<RecipeInfo> recipes) {
        List<UUID> recipeIds = recipes.stream().map(RecipeInfo::getId).toList();

        Map<UUID, RecipeYoutubeMeta> youtubeMetaMap = recipeYoutubeMetaService.gets(recipeIds).stream()
                .collect(Collectors.toMap(RecipeYoutubeMeta::getRecipeId, Function.identity(), (a, b) -> a));

        Map<UUID, RecipeDetailMeta> detailMetaMap = recipeDetailMetaService.getIn(recipeIds).stream()
                .collect(Collectors.toMap(RecipeDetailMeta::getRecipeId, Function.identity()));

        Map<UUID, List<RecipeTag>> tagsMap =
                recipeTagService.gets(recipeIds).stream().collect(Collectors.groupingBy(RecipeTag::getRecipeId));

        return recipes.stream()
                .map(recipe -> {
                    UUID recipeId = recipe.getId();
                    RecipeYoutubeMeta youtubeMeta = youtubeMetaMap.get(recipeId);
                    if (youtubeMeta == null) {
                        log.warn("공개 레시피 유튜브 메타 누락: recipeId={}", recipeId);
                        return null;
                    }
                    RecipeDetailMeta detailMeta = detailMetaMap.get(recipeId);
                    List<RecipeTag> tags = tagsMap.getOrDefault(recipeId, Collections.emptyList());
                    return PublicRecipeOverview.of(recipe, youtubeMeta, detailMeta, tags);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
