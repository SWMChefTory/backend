package com.cheftory.api.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.challenge.RecipeChallengeService;
import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.dto.FullRecipe;
import com.cheftory.api.recipe.dto.RecipeBookmarkOverview;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoRecommendType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeOverview;
import com.cheftory.api.recipe.dto.RecipeProgressStatus;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.rank.RankingType;
import com.cheftory.api.recipe.rank.RecipeRankService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeFacade 테스트")
class RecipeFacadeTest {

    private RecipeInfoService recipeInfoService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeCategoryService recipeCategoryService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeStepService recipeStepService;
    private RecipeIngredientService recipeIngredientService;
    private RecipeDetailMetaService recipeDetailMetaService;
    private RecipeProgressService recipeProgressService;
    private RecipeTagService recipeTagService;
    private RecipeBriefingService recipeBriefingService;
    private RecipeRankService recipeRankService;
    private RecipeChallengeService recipeChallengeService;

    private RecipeFacade sut;

    @BeforeEach
    void setUp() {
        recipeInfoService = mock(RecipeInfoService.class);
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeCategoryService = mock(RecipeCategoryService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        recipeStepService = mock(RecipeStepService.class);
        recipeIngredientService = mock(RecipeIngredientService.class);
        recipeDetailMetaService = mock(RecipeDetailMetaService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        recipeTagService = mock(RecipeTagService.class);
        recipeBriefingService = mock(RecipeBriefingService.class);
        recipeRankService = mock(RecipeRankService.class);
        recipeChallengeService = mock(RecipeChallengeService.class);

        sut = new RecipeFacade(
                recipeStepService,
                recipeBookmarkService,
                recipeCategoryService,
                recipeYoutubeMetaService,
                recipeIngredientService,
                recipeDetailMetaService,
                recipeProgressService,
                recipeTagService,
                recipeBriefingService,
                recipeInfoService,
                recipeRankService,
                recipeChallengeService);
    }

    @Nested
    @DisplayName("레시피 차단")
    class BlockRecipe {

        @Test
        @DisplayName("정상 차단 흐름")
        void shouldBlockRecipe() {
            UUID recipeId = UUID.randomUUID();

            sut.blockRecipe(recipeId);

            verify(recipeYoutubeMetaService).block(recipeId);
            verify(recipeInfoService).block(recipeId);
            verify(recipeBookmarkService).block(recipeId);
        }

        @Test
        @DisplayName("차단되지 않은 영상이면 RECIPE_NOT_BLOCKED_VIDEO로 변환된다")
        void shouldThrowNotBlockedVideo() {
            UUID recipeId = UUID.randomUUID();

            doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO))
                    .when(recipeYoutubeMetaService)
                    .block(recipeId);

            assertThatThrownBy(() -> sut.blockRecipe(recipeId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_NOT_BLOCKED_VIDEO);

            verify(recipeYoutubeMetaService).block(recipeId);
            verify(recipeInfoService, never()).block(any());
            verify(recipeBookmarkService, never()).block(any());
        }
    }

    @Nested
    @DisplayName("레시피 상세 조회")
    class ViewFullRecipe {

        @Test
        @DisplayName("성공 레시피면 FullRecipe를 반환한다")
        void shouldReturnFullRecipe() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            RecipeInfo recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS);

            doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
            doReturn(Collections.emptyList()).when(recipeStepService).gets(recipeId);
            doReturn(Collections.emptyList()).when(recipeIngredientService).gets(recipeId);
            doReturn(mock(RecipeDetailMeta.class)).when(recipeDetailMetaService).get(recipeId);
            doReturn(Collections.emptyList()).when(recipeProgressService).gets(recipeId);
            doReturn(Collections.emptyList()).when(recipeTagService).gets(recipeId);
            doReturn(Collections.emptyList()).when(recipeBriefingService).gets(recipeId);
            doReturn(mockYoutubeMeta(recipeId)).when(recipeYoutubeMetaService).get(recipeId);
            doReturn(mockBookmark(recipeId, userId)).when(recipeBookmarkService).get(userId, recipeId);

            FullRecipe result = sut.getFullRecipe(recipeId, userId);

            assertThat(result).isNotNull();
            verify(recipeInfoService).getSuccess(recipeId);
        }

        @Test
        @DisplayName("RecipeInfo not found면 RECIPE_INFO_NOT_FOUND로 변환된다")
        void shouldMapNotFoundToRecipeInfoNotFound() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                    .when(recipeInfoService)
                    .getSuccess(recipeId);

            assertThatThrownBy(() -> sut.getFullRecipe(recipeId, userId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_NOT_FOUND);
        }

        @Test
        @DisplayName("DetailMeta not found면 RECIPE_INFO_NOT_FOUND로 변환된다")
        void shouldMapDetailMetaNotFoundToRecipeInfoNotFound() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doReturn(mockRecipe(recipeId, RecipeStatus.SUCCESS))
                    .when(recipeInfoService)
                    .getSuccess(recipeId);
            doReturn(Collections.emptyList()).when(recipeStepService).gets(recipeId);
            doReturn(Collections.emptyList()).when(recipeIngredientService).gets(recipeId);
            doThrow(new RecipeException(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND))
                    .when(recipeDetailMetaService)
                    .get(recipeId);

            assertThatThrownBy(() -> sut.getFullRecipe(recipeId, userId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_NOT_FOUND);
        }

        @Test
        @DisplayName("Recipe failed면 RECIPE_FAILED로 변환된다")
        void shouldMapFailedToRecipeFailed() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_FAILED))
                    .when(recipeInfoService)
                    .getSuccess(recipeId);

            assertThatThrownBy(() -> sut.getFullRecipe(recipeId, userId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_FAILED);
        }

        @Test
        @DisplayName("그 외 예외는 그대로 전파한다")
        void shouldPropagateOtherExceptions() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL))
                    .when(recipeInfoService)
                    .getSuccess(recipeId);

            assertThatThrownBy(() -> sut.getFullRecipe(recipeId, userId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
    }

    @Nested
    @DisplayName("레시피 개요 조회")
    class GetRecipeOverview {

        @Test
        @DisplayName("detailMeta가 null이어도 overview를 반환한다")
        void shouldReturnOverviewWithNullDetailMeta() {
            UUID recipeId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doReturn(mockRecipe(recipeId, RecipeStatus.SUCCESS))
                    .when(recipeInfoService)
                    .getSuccess(recipeId);
            doReturn(mockYoutubeMeta(recipeId)).when(recipeYoutubeMetaService).get(recipeId);
            doReturn(null).when(recipeDetailMetaService).get(recipeId);
            doReturn(List.of(mockTag(recipeId, "한식"))).when(recipeTagService).gets(recipeId);
            doReturn(false).when(recipeBookmarkService).exist(userId, recipeId);

            RecipeOverview result = sut.getRecipeOverview(recipeId, userId);

            assertThat(result).isNotNull();
            assertThat(result.getRecipeId()).isEqualTo(recipeId);
            assertThat(result.getIsViewed()).isFalse();
        }
    }

    @Nested
    @DisplayName("최근/카테고리/미분류 북마크 조회")
    class BookmarkOverviews {

        @Test
        @DisplayName("커서 기반 최근 북마크를 반환한다")
        void shouldReturnRecentBookmarksWithCursor() {
            UUID userId = UUID.randomUUID();
            String cursor = "cursor-1";
            UUID recipeId = UUID.randomUUID();
            String nextCursor = "cursor-2";

            CursorPage<RecipeBookmark> bookmarks = CursorPage.of(List.of(mockBookmark(recipeId, userId)), nextCursor);

            doReturn(bookmarks).when(recipeBookmarkService).getRecents(userId, cursor);
            doReturn(List.of(mockRecipe(recipeId, RecipeStatus.SUCCESS)))
                    .when(recipeInfoService)
                    .getValidRecipes(anyList());
            doReturn(List.of(mockYoutubeMeta(recipeId)))
                    .when(recipeYoutubeMetaService)
                    .getByRecipes(anyList());
            doReturn(List.of(mockDetailMeta(recipeId)))
                    .when(recipeDetailMetaService)
                    .getIn(anyList());
            doReturn(List.of(mockTag(recipeId, "한식"))).when(recipeTagService).getIn(anyList());

            CursorPage<RecipeBookmarkOverview> result = sut.getRecents(userId, cursor);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(recipeBookmarkService).getRecents(userId, cursor);
        }

        @Test
        @DisplayName("커서 기반 카테고리 북마크를 반환한다")
        void shouldReturnCategorizedBookmarksWithCursor() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();
            String cursor = "cursor-1";
            UUID recipeId = UUID.randomUUID();
            String nextCursor = "cursor-2";

            CursorPage<RecipeBookmark> bookmarks = CursorPage.of(List.of(mockBookmark(recipeId, userId)), nextCursor);

            doReturn(bookmarks).when(recipeBookmarkService).getCategorized(userId, categoryId, cursor);
            doReturn(List.of(mockRecipe(recipeId, RecipeStatus.SUCCESS)))
                    .when(recipeInfoService)
                    .getValidRecipes(anyList());
            doReturn(List.of(mockYoutubeMeta(recipeId)))
                    .when(recipeYoutubeMetaService)
                    .getByRecipes(anyList());
            doReturn(List.of(mockDetailMeta(recipeId)))
                    .when(recipeDetailMetaService)
                    .getIn(anyList());
            doReturn(List.of(mockTag(recipeId, "한식"))).when(recipeTagService).getIn(anyList());

            CursorPage<RecipeBookmarkOverview> result = sut.getCategorized(userId, categoryId, cursor);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(recipeBookmarkService).getCategorized(userId, categoryId, cursor);
        }
    }

    @Nested
    @DisplayName("카테고리별 레시피 개수 조회")
    class RecipeCategoryCounts {

        @Test
        @DisplayName("카테고리별/미분류 카운트를 반환한다")
        void shouldReturnCategoryCounts() {
            UUID userId = UUID.randomUUID();
            UUID categoryId1 = UUID.randomUUID();
            UUID categoryId2 = UUID.randomUUID();

            RecipeCategory c1 = mockCategory(categoryId1, "한식");
            RecipeCategory c2 = mockCategory(categoryId2, "양식");

            RecipeBookmarkCategorizedCount cc1 = mockCategorizedCount(categoryId1, 5);
            RecipeBookmarkCategorizedCount cc2 = mockCategorizedCount(categoryId2, 3);

            doReturn(List.of(c1, c2)).when(recipeCategoryService).getUsers(userId);
            doReturn(List.of(cc1, cc2))
                    .when(recipeBookmarkService)
                    .countByCategories(List.of(categoryId1, categoryId2));
            doReturn(RecipeBookmarkUnCategorizedCount.of(2))
                    .when(recipeBookmarkService)
                    .countUncategorized(userId);

            com.cheftory.api.recipe.dto.RecipeCategoryCounts result = sut.getUserCategoryCounts(userId);

            assertThat(result).isNotNull();
            assertThat(result.getCategorizedCounts()).hasSize(2);
            assertThat(result.getUncategorizedCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteRecipeCategory {

        @Test
        @DisplayName("카테고리 삭제 시 unCategorize 후 delete가 호출된다")
        void shouldDeleteCategory() {
            UUID categoryId = UUID.randomUUID();

            sut.deleteCategory(categoryId);

            verify(recipeBookmarkService).unCategorize(categoryId);
            verify(recipeCategoryService).delete(categoryId);
        }
    }

    @Nested
    @DisplayName("레시피 진행 상황 조회")
    class GetRecipeProgress {

        @Test
        @DisplayName("진행 상황과 레시피를 조합해 반환한다")
        void shouldReturnRecipeProgressStatus() {
            UUID recipeId = UUID.randomUUID();
            RecipeInfo recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS);

            RecipeProgress p1 = mock(RecipeProgress.class);
            doReturn(RecipeProgressStep.READY).when(p1).getStep();
            doReturn(RecipeProgressDetail.READY).when(p1).getDetail();

            doReturn(List.of(p1)).when(recipeProgressService).gets(recipeId);
            doReturn(recipeInfo).when(recipeInfoService).get(recipeId);

            RecipeProgressStatus result = sut.getRecipeProgress(recipeId);

            assertThat(result).isNotNull();
            verify(recipeProgressService).gets(recipeId);
            verify(recipeInfoService).get(recipeId);
        }
    }

    @Nested
    @DisplayName("음식 종류별 레시피 조회")
    class GetCuisineRecipes {

        @Test
        @DisplayName("커서 기반 cuisine 조회 시 overview를 만들어 반환한다")
        void shouldReturnCuisineRecipesWithCursor() {
            String cursor = "cursor-1";
            String nextCursor = "cursor-2";
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeInfo recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS);
            CursorPage<UUID> recipeIds = CursorPage.of(List.of(recipeId), nextCursor);

            doReturn(recipeIds).when(recipeRankService).getCuisineRecipes(userId, RecipeCuisineType.KOREAN, cursor);
            doReturn(List.of(recipeInfo)).when(recipeInfoService).gets(List.of(recipeId));

            doReturn(List.of(mockYoutubeMeta(recipeId)))
                    .when(recipeYoutubeMetaService)
                    .getByRecipes(List.of(recipeId));
            doReturn(List.of(mockDetailMeta(recipeId)))
                    .when(recipeDetailMetaService)
                    .getIn(List.of(recipeId));
            doReturn(List.of(mockTag(recipeId, "한식"))).when(recipeTagService).getIn(List.of(recipeId));
            doReturn(List.of()).when(recipeBookmarkService).gets(List.of(recipeId), userId);

            CursorPage<RecipeOverview> result = sut.getCuisineRecipes(RecipeCuisineType.KOREAN, userId, cursor);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(recipeRankService).getCuisineRecipes(userId, RecipeCuisineType.KOREAN, cursor);
            verify(recipeInfoService).gets(List.of(recipeId));
        }
    }

    @Nested
    @DisplayName("추천 레시피 조회")
    class GetRecommendRecipes {

        @Test
        @DisplayName("커서 기반 POPULAR 타입은 getPopulars를 호출한다")
        void shouldReturnPopularRecipesWithCursor() {
            String cursor = "cursor-1";
            String nextCursor = "cursor-2";
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeInfo recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS);
            CursorPage<RecipeInfo> recipesPage = CursorPage.of(List.of(recipeInfo), nextCursor);

            doReturn(recipesPage).when(recipeInfoService).getPopulars(cursor, RecipeInfoVideoQuery.ALL);

            doReturn(List.of(mockYoutubeMeta(recipeId)))
                    .when(recipeYoutubeMetaService)
                    .getByRecipes(List.of(recipeId));
            doReturn(List.of(mockDetailMeta(recipeId)))
                    .when(recipeDetailMetaService)
                    .getIn(List.of(recipeId));
            doReturn(List.of(mockTag(recipeId, "태그1"))).when(recipeTagService).getIn(List.of(recipeId));
            doReturn(List.of()).when(recipeBookmarkService).gets(List.of(recipeId), userId);

            CursorPage<RecipeOverview> result =
                    sut.getRecommendRecipes(RecipeInfoRecommendType.POPULAR, userId, cursor, RecipeInfoVideoQuery.ALL);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(recipeInfoService).getPopulars(cursor, RecipeInfoVideoQuery.ALL);
        }

        @Test
        @DisplayName("커서 기반 CHEF 타입은 랭킹 기반으로 조회한다")
        void shouldReturnChefRecipesWithCursor() {
            String cursor = "cursor-1";
            String nextCursor = "cursor-2";
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            doReturn(CursorPage.of(List.of(recipeId), nextCursor))
                    .when(recipeRankService)
                    .getRecipeIds(RankingType.CHEF, cursor);

            RecipeInfo recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS);
            doReturn(List.of(recipeInfo)).when(recipeInfoService).getValidRecipes(List.of(recipeId));

            doReturn(List.of(mockYoutubeMeta(recipeId)))
                    .when(recipeYoutubeMetaService)
                    .getByRecipes(List.of(recipeId));
            doReturn(List.of(mockDetailMeta(recipeId)))
                    .when(recipeDetailMetaService)
                    .getIn(List.of(recipeId));
            doReturn(List.of(mockTag(recipeId, "태그1"))).when(recipeTagService).getIn(List.of(recipeId));
            doReturn(List.of()).when(recipeBookmarkService).gets(List.of(recipeId), userId);

            CursorPage<RecipeOverview> result =
                    sut.getRecommendRecipes(RecipeInfoRecommendType.CHEF, userId, cursor, RecipeInfoVideoQuery.ALL);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isEqualTo(nextCursor);
            verify(recipeRankService).getRecipeIds(RankingType.CHEF, cursor);
        }
    }

    private RecipeBookmark mockBookmark(UUID recipeId, UUID userId) {
        RecipeBookmark bookmark = mock(RecipeBookmark.class);
        doReturn(recipeId).when(bookmark).getRecipeId();
        doReturn(userId).when(bookmark).getUserId();
        doReturn(LocalDateTime.now()).when(bookmark).getViewedAt();
        return bookmark;
    }

    private RecipeInfo mockRecipe(UUID recipeId, RecipeStatus status) {
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(recipeId).when(recipeInfo).getId();
        doReturn(status).when(recipeInfo).getRecipeStatus();
        doReturn(status == RecipeStatus.FAILED).when(recipeInfo).isFailed();
        doReturn(status == RecipeStatus.SUCCESS).when(recipeInfo).isSuccess();
        doReturn(LocalDateTime.now()).when(recipeInfo).getCreatedAt();
        doReturn(LocalDateTime.now()).when(recipeInfo).getUpdatedAt();
        doReturn(0).when(recipeInfo).getViewCount();
        return recipeInfo;
    }

    private RecipeYoutubeMeta mockYoutubeMeta(UUID recipeId) {
        RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
        doReturn(recipeId).when(meta).getRecipeId();
        doReturn("title").when(meta).getTitle();
        return meta;
    }

    private RecipeDetailMeta mockDetailMeta(UUID recipeId) {
        RecipeDetailMeta meta = mock(RecipeDetailMeta.class);
        doReturn(recipeId).when(meta).getRecipeId();
        doReturn("desc").when(meta).getDescription();
        doReturn(2).when(meta).getServings();
        doReturn(30).when(meta).getCookTime();
        return meta;
    }

    private RecipeTag mockTag(UUID recipeId, String tag) {
        RecipeTag t = mock(RecipeTag.class);
        doReturn(recipeId).when(t).getRecipeId();
        doReturn(tag).when(t).getTag();
        return t;
    }

    private RecipeCategory mockCategory(UUID categoryId, String name) {
        RecipeCategory c = mock(RecipeCategory.class);
        doReturn(categoryId).when(c).getId();
        doReturn(name).when(c).getName();
        return c;
    }

    private RecipeBookmarkCategorizedCount mockCategorizedCount(UUID categoryId, int count) {
        RecipeBookmarkCategorizedCount cc = mock(RecipeBookmarkCategorizedCount.class);
        doReturn(categoryId).when(cc).getCategoryId();
        doReturn(count).when(cc).getCount();
        return cc;
    }
}
