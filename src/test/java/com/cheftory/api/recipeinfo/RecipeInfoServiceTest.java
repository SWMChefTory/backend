package com.cheftory.api.recipeinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefingService;
import com.cheftory.api.recipeinfo.category.RecipeCategory;
import com.cheftory.api.recipeinfo.category.RecipeCategoryService;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipeinfo.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.history.RecipeHistoryCategorizedCount;
import com.cheftory.api.recipeinfo.history.RecipeHistoryService;
import com.cheftory.api.recipeinfo.history.RecipeHistoryUnCategorizedCount;
import com.cheftory.api.recipeinfo.identify.RecipeIdentifyService;
import com.cheftory.api.recipeinfo.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredientService;
import com.cheftory.api.recipeinfo.model.FullRecipe;
import com.cheftory.api.recipeinfo.model.RecipeCreationTarget;
import com.cheftory.api.recipeinfo.model.RecipeHistoryOverview;
import com.cheftory.api.recipeinfo.model.RecipeInfoCuisineType;
import com.cheftory.api.recipeinfo.model.RecipeInfoRecommendType;
import com.cheftory.api.recipeinfo.model.RecipeInfoVideoQuery;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressService;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.rank.RankingType;
import com.cheftory.api.recipeinfo.rank.RecipeRankService;
import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipeinfo.search.RecipeSearch;
import com.cheftory.api.recipeinfo.search.RecipeSearchPageRequest;
import com.cheftory.api.recipeinfo.search.RecipeSearchService;
import com.cheftory.api.recipeinfo.step.RecipeStepService;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.tag.RecipeTagService;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaType;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeUri;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeVideoInfo;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeInfoService Tests")
public class RecipeInfoServiceTest {

  private RecipeService recipeService;
  private RecipeHistoryService recipeHistoryService;
  private RecipeCategoryService recipeCategoryService;
  private RecipeYoutubeMetaService recipeYoutubeMetaService;
  private RecipeStepService recipeStepService;
  private RecipeIngredientService recipeIngredientService;
  private RecipeDetailMetaService recipeDetailMetaService;
  private RecipeProgressService recipeProgressService;
  private RecipeTagService recipeTagService;
  private RecipeIdentifyService recipeIdentifyService;
  private RecipeBriefingService recipeBriefingService;
  private AsyncRecipeInfoCreationService asyncRecipeInfoCreationService;
  private RecipeInfoService recipeInfoService;
  private RecipeSearchService recipeSearchService;
  private RecipeRankService recipeRankService;

  @BeforeEach
  void setUp() {
    recipeService = mock(RecipeService.class);
    recipeHistoryService = mock(RecipeHistoryService.class);
    recipeCategoryService = mock(RecipeCategoryService.class);
    recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
    recipeStepService = mock(RecipeStepService.class);
    recipeIngredientService = mock(RecipeIngredientService.class);
    recipeDetailMetaService = mock(RecipeDetailMetaService.class);
    recipeProgressService = mock(RecipeProgressService.class);
    recipeTagService = mock(RecipeTagService.class);
    recipeIdentifyService = mock(RecipeIdentifyService.class);
    recipeBriefingService = mock(RecipeBriefingService.class);
    asyncRecipeInfoCreationService = mock(AsyncRecipeInfoCreationService.class);
    recipeSearchService = mock(RecipeSearchService.class);
    recipeRankService = mock(RecipeRankService.class);

    recipeInfoService =
        new RecipeInfoService(
            asyncRecipeInfoCreationService,
            recipeStepService,
            recipeHistoryService,
            recipeCategoryService,
            recipeYoutubeMetaService,
            recipeIngredientService,
            recipeDetailMetaService,
            recipeProgressService,
            recipeTagService,
            recipeIdentifyService,
            recipeBriefingService,
            recipeService,
            recipeSearchService,
            recipeRankService);
  }

  @Nested
  @DisplayName("레시피 차단")
  class BlockRecipeFeature {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 차단을 요청하면")
      class WhenBlockingRecipe {

        @Test
        @DisplayName("Then - 메타 차단, 레시피 차단, 히스토리 차단이 순서대로 호출된다")
        void thenCallsBlockServices() {
          recipeInfoService.blockRecipe(recipeId);

          verify(recipeYoutubeMetaService).block(recipeId);
          verify(recipeService).block(recipeId);
          verify(recipeHistoryService).blockByRecipe(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 차단되지 않은 영상일 때")
    class GivenNotBlockedVideo {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        doThrow(new RecipeInfoException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO))
            .when(recipeYoutubeMetaService)
            .block(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 차단을 요청하면")
      class WhenBlockingRecipe {

        @Test
        @DisplayName("Then - RECIPE_NOT_BLOCKED_VIDEO 예외가 발생하고 이후 서비스는 호출되지 않는다")
        void thenThrowsNotBlockedAndStop() {
          assertThatThrownBy(() -> recipeInfoService.blockRecipe(recipeId))
              .isInstanceOf(RecipeInfoException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeInfoErrorCode.RECIPE_NOT_BLOCKED_VIDEO);

          verify(recipeYoutubeMetaService).block(recipeId);
          verify(recipeService, never()).block(any());
          verify(recipeHistoryService, never()).blockByRecipe(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("기존 레시피 사용")
  class UseExistingRecipe {

    @Test
    @DisplayName("기존 유튜브 메타데이터가 있으면 기존 레시피를 사용한다")
    void shouldUseExistingRecipeWhenYoutubeMetaExists() {
      URI uri = URI.create("https://youtube.com/watch?v=test");
      UUID userId = UUID.randomUUID();
      UUID metaId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();

      RecipeYoutubeMeta meta = createMockRecipeYoutubeMeta(metaId, "테스트 영상", recipeId);
      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipe).when(recipeService).getNotFailed(List.of(recipeId));

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      UUID result = recipeInfoService.create(target);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService).create(userId, recipeId);
      verify(asyncRecipeInfoCreationService, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("알 수 없는 예외가 발생하면 RECIPE_CREATE_FAIL 예외를 던진다")
    void shouldThrowRecipeCreateFailForUnknownException() {
      URI uri = URI.create("https://youtube.com/watch?v=unknown");
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL))
          .when(recipeYoutubeMetaService)
          .getByUrl(uri);

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      assertThatThrownBy(() -> recipeInfoService.create(target))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }

    @Test
    @DisplayName("밴된 유튜브 영상이면 예외를 던진다")
    void shouldThrowExceptionWhenYoutubeVideoIsBanned() {
      URI uri = URI.create("https://youtube.com/watch?v=banned");
      UUID userId = UUID.randomUUID();
      UUID metaId = UUID.randomUUID();

      RecipeYoutubeMeta bannedMeta =
          createMockRecipeYoutubeMeta(metaId, "밴된 영상", UUID.randomUUID());
      doReturn(true).when(bannedMeta).isBanned();

      doReturn(List.of(bannedMeta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doThrow(new RecipeInfoException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED))
          .when(recipeService)
          .getNotFailed(anyList());

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      assertThatThrownBy(() -> recipeInfoService.create(target))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_BANNED);
    }

    @Test
    @DisplayName("블락된 유튜브 영상이면 예외를 던진다")
    void shouldThrowExceptionWhenYoutubeVideoIsBlocked() {
      URI uri = URI.create("https://youtube.com/watch?v=blocked");
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED))
          .when(recipeYoutubeMetaService)
          .getByUrl(uri);

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      assertThatThrownBy(() -> recipeInfoService.create(target))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }

    @Test
    @DisplayName("Crawler 소스로 레시피 생성 시 히스토리를 생성하지 않는다")
    void shouldNotCreateHistoryWhenCreatingWithCrawlerSource() {
      URI uri = URI.create("https://youtube.com/watch?v=crawler");
      UUID metaId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();

      RecipeYoutubeMeta meta = createMockRecipeYoutubeMeta(metaId, "크롤러 영상", recipeId);
      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipe).when(recipeService).getNotFailed(List.of(recipeId));

      RecipeCreationTarget target = new RecipeCreationTarget.Crawler(uri);
      UUID result = recipeInfoService.create(target);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService, never()).create(any(), any());
      verify(asyncRecipeInfoCreationService, never()).create(any(), any(), any());
    }
  }

  @Nested
  @DisplayName("새 레시피 생성")
  class CreateNewRecipe {

    @Test
    @DisplayName("새로운 유튜브 URL이면 새 레시피를 생성한다")
    void shouldCreateNewRecipeForNewYoutubeUrl() {
      URI uri = URI.create("https://youtube.com/watch?v=new");
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();

      YoutubeVideoInfo videoInfo = createMockYoutubeVideoInfo();

      doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
          .when(recipeService)
          .getNotFailed(anyList());
      doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
      doReturn(recipeId).when(recipeService).create();

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      UUID result = recipeInfoService.create(target);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeIdentifyService).create(uri);
      verify(recipeYoutubeMetaService).create(videoInfo, recipeId);
      verify(asyncRecipeInfoCreationService).create(recipeId, videoInfo.getVideoId(), uri);
      verify(recipeHistoryService).create(userId, recipeId);
    }

    @Test
    @DisplayName("동시성 문제로 이미 진행 중인 레시피가 있으면 기존 레시피를 사용한다")
    void shouldUseExistingRecipeWhenConcurrencyIssueOccurs() {
      URI uri = URI.create("https://youtube.com/watch?v=concurrent");
      UUID userId = UUID.randomUUID();
      UUID metaId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();

      YoutubeVideoInfo videoInfo = createMockYoutubeVideoInfo();
      RecipeYoutubeMeta meta = createMockRecipeYoutubeMeta(metaId, "동시성 테스트", recipeId);
      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.IN_PROGRESS);

      doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
      doThrow(new RecipeInfoException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
          .when(recipeIdentifyService)
          .create(uri);
      doReturn(List.of(meta)).when(recipeYoutubeMetaService).getByUrl(uri);
      doReturn(recipe).when(recipeService).getNotFailed(List.of(recipeId));

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      UUID result = recipeInfoService.create(target);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeHistoryService).create(userId, recipeId);
    }

    @Test
    @DisplayName("비디오 정보 조회 실패시 예외를 던진다")
    void shouldThrowExceptionWhenVideoInfoFetchFails() {
      URI uri = URI.create("https://youtube.com/watch?v=fail");
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
          .when(recipeService)
          .getNotFailed(anyList());
      doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL))
          .when(recipeYoutubeMetaService)
          .getVideoInfo(uri);

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      assertThatThrownBy(() -> recipeInfoService.create(target))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }

    @Test
    @DisplayName("레시피 생성 실패시 예외를 던진다")
    void shouldThrowExceptionWhenRecipeCreationFails() {
      URI uri = URI.create("https://youtube.com/watch?v=creation_fail");
      UUID userId = UUID.randomUUID();

      YoutubeVideoInfo videoInfo = createMockYoutubeVideoInfo();

      doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
          .when(recipeService)
          .getNotFailed(anyList());
      doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
      doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL))
          .when(recipeService)
          .create();

      RecipeCreationTarget target = new RecipeCreationTarget.User(uri, userId);
      assertThatThrownBy(() -> recipeInfoService.create(target))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }
  }

  @Nested
  @DisplayName("레시피 상세 조회")
  class FindFullRecipe {

    @Test
    @DisplayName("성공한 레시피를 조회하면 상세 정보를 반환한다")
    void shouldReturnFullRecipeInfoWhenViewingSuccessfulRecipe() {
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

      setupFullRecipeInfoMocks(recipeId, userId, recipe);
      doReturn(recipe).when(recipeService).getSuccess(recipeId);

      FullRecipe result = recipeInfoService.viewFullRecipe(recipeId, userId);

      assertThat(result).isNotNull();
      verify(recipeService).getSuccess(recipeId);
    }

    @Test
    @DisplayName("존재하지 않는 레시피 조회시 예외를 던진다")
    void shouldThrowExceptionWhenRecipeNotFound() {
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
          .when(recipeService)
          .getSuccess(recipeId);

      assertThatThrownBy(() -> recipeInfoService.viewFullRecipe(recipeId, userId))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
    }

    @Test
    @DisplayName("실패한 레시피 조회시 예외를 던진다")
    void shouldThrowExceptionWhenRecipeFailed() {
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_FAILED))
          .when(recipeService)
          .getSuccess(recipeId);

      assertThatThrownBy(() -> recipeInfoService.viewFullRecipe(recipeId, userId))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_FAILED);
    }

    @Test
    @DisplayName("기타 예외 발생시 그대로 전파한다")
    void shouldPropagateOtherExceptions() {
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL))
          .when(recipeService)
          .getSuccess(recipeId);

      assertThatThrownBy(() -> recipeInfoService.viewFullRecipe(recipeId, userId))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }
  }

  @Nested
  @DisplayName("레시피 개요 조회")
  class GetRecipeOverview {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeIdAndUserId {

      private UUID recipeId;
      private UUID userId;
      private Recipe recipe;
      private RecipeYoutubeMeta youtubeMeta;
      private RecipeDetailMeta detailMeta;
      private List<RecipeTag> tags;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        youtubeMeta = createMockRecipeYoutubeMeta(UUID.randomUUID(), "Test Recipe", recipeId);
        detailMeta = createMockRecipeDetailMeta(recipeId, "Test Description");
        tags = List.of(createMockRecipeTag(recipeId, "한식"));
      }

      @Nested
      @DisplayName("When - 레시피 개요를 조회한다면")
      class WhenGettingRecipeOverview {

        @Test
        @DisplayName("Then - 레시피 개요를 반환하고 isViewed가 true여야 한다")
        void thenShouldReturnRecipeOverviewWithIsViewedTrue() {
          doReturn(recipe).when(recipeService).getSuccess(recipeId);
          doReturn(youtubeMeta).when(recipeYoutubeMetaService).get(recipeId);
          doReturn(detailMeta).when(recipeDetailMetaService).get(recipeId);
          doReturn(tags).when(recipeTagService).gets(recipeId);
          doReturn(true).when(recipeHistoryService).exist(userId, recipeId);

          RecipeOverview result = recipeInfoService.getRecipeOverview(recipeId, userId);

          assertThat(result).isNotNull();
          assertThat(result.getRecipeId()).isEqualTo(recipeId);
          assertThat(result.getVideoTitle()).isEqualTo("Test Recipe");
          assertThat(result.getDescription()).isEqualTo("Test Description");
          assertThat(result.getIsViewed()).isTrue();
          assertThat(result.getTags()).hasSize(1);
          assertThat(result.getTags().get(0)).isEqualTo("한식");

          verify(recipeService).getSuccess(recipeId);
          verify(recipeYoutubeMetaService).get(recipeId);
          verify(recipeDetailMetaService).get(recipeId);
          verify(recipeTagService).gets(recipeId);
          verify(recipeHistoryService).exist(userId, recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자가 레시피를 본 적이 없을 때")
    class GivenUserNotViewedRecipe {

      private UUID recipeId;
      private UUID userId;
      private Recipe recipe;
      private RecipeYoutubeMeta youtubeMeta;
      private RecipeDetailMeta detailMeta;
      private List<RecipeTag> tags;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        youtubeMeta = createMockRecipeYoutubeMeta(UUID.randomUUID(), "Test Recipe", recipeId);
        detailMeta = createMockRecipeDetailMeta(recipeId, "Test Description");
        tags = List.of(createMockRecipeTag(recipeId, "한식"));
      }

      @Nested
      @DisplayName("When - 레시피 개요를 조회한다면")
      class WhenGettingRecipeOverview {

        @Test
        @DisplayName("Then - 레시피 개요를 반환하고 isViewed가 false여야 한다")
        void thenShouldReturnRecipeOverviewWithIsViewedFalse() {
          doReturn(recipe).when(recipeService).getSuccess(recipeId);
          doReturn(youtubeMeta).when(recipeYoutubeMetaService).get(recipeId);
          doReturn(detailMeta).when(recipeDetailMetaService).get(recipeId);
          doReturn(tags).when(recipeTagService).gets(recipeId);
          doReturn(false).when(recipeHistoryService).exist(userId, recipeId);

          RecipeOverview result = recipeInfoService.getRecipeOverview(recipeId, userId);

          assertThat(result).isNotNull();
          assertThat(result.getRecipeId()).isEqualTo(recipeId);
          assertThat(result.getVideoTitle()).isEqualTo("Test Recipe");
          assertThat(result.getDescription()).isEqualTo("Test Description");
          assertThat(result.getIsViewed()).isFalse();

          verify(recipeService).getSuccess(recipeId);
          verify(recipeYoutubeMetaService).get(recipeId);
          verify(recipeDetailMetaService).get(recipeId);
          verify(recipeTagService).gets(recipeId);
          verify(recipeHistoryService).exist(userId, recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - DetailMeta가 null일 때")
    class GivenDetailMetaIsNull {

      private UUID recipeId;
      private UUID userId;
      private Recipe recipe;
      private RecipeYoutubeMeta youtubeMeta;
      private List<RecipeTag> tags;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        youtubeMeta = createMockRecipeYoutubeMeta(UUID.randomUUID(), "Test Recipe", recipeId);
        tags = List.of(createMockRecipeTag(recipeId, "한식"));
      }

      @Nested
      @DisplayName("When - 레시피 개요를 조회한다면")
      class WhenGettingRecipeOverview {

        @Test
        @DisplayName("Then - description, servings, cookTime이 null이어야 한다")
        void thenShouldReturnRecipeOverviewWithNullDetailMetaFields() {
          doReturn(recipe).when(recipeService).getSuccess(recipeId);
          doReturn(youtubeMeta).when(recipeYoutubeMetaService).get(recipeId);
          doReturn(null).when(recipeDetailMetaService).get(recipeId);
          doReturn(tags).when(recipeTagService).gets(recipeId);
          doReturn(false).when(recipeHistoryService).exist(userId, recipeId);

          RecipeOverview result = recipeInfoService.getRecipeOverview(recipeId, userId);

          assertThat(result).isNotNull();
          assertThat(result.getRecipeId()).isEqualTo(recipeId);
          assertThat(result.getDescription()).isNull();
          assertThat(result.getServings()).isNull();
          assertThat(result.getCookTime()).isNull();

          verify(recipeService).getSuccess(recipeId);
          verify(recipeYoutubeMetaService).get(recipeId);
          verify(recipeDetailMetaService).get(recipeId);
          verify(recipeTagService).gets(recipeId);
          verify(recipeHistoryService).exist(userId, recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 개요를 조회한다면")
      class WhenGettingRecipeOverview {

        @Test
        @DisplayName("Then - 예외를 던진다")
        void thenShouldThrowException() {
          doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
              .when(recipeService)
              .getSuccess(recipeId);

          assertThatThrownBy(() -> recipeInfoService.getRecipeOverview(recipeId, userId))
              .isInstanceOf(RecipeInfoException.class)
              .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_NOT_FOUND);

          verify(recipeService).getSuccess(recipeId);
          verify(recipeYoutubeMetaService, never()).get(recipeId);
        }
      }
    }
  }

  @Nested
  @DisplayName("추천 레시피 조회")
  class GetRecommendRecipes {

    @Nested
    @DisplayName("인기 레시피 타입")
    class PopularRecipeType {

      @Test
      @DisplayName("성공한 레시피들만 인기 목록에 포함된다")
      void shouldReturnOnlySuccessfulRecipesInPopularList() {
        Integer page = 0;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Recipe recipe1 = createMockRecipe(recipeId1, RecipeStatus.SUCCESS);
        Recipe recipe2 = createMockRecipe(recipeId2, RecipeStatus.SUCCESS);
        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2));

        setupPopularMocks(List.of(recipeId1, recipeId2));
        doReturn(recipePage).when(recipeService).getPopulars(page, RecipeInfoVideoQuery.ALL);

        Page<RecipeOverview> result =
            recipeInfoService.getRecommendRecipes(
                RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.ALL);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(overview -> overview.getRecipeId() != null);
        verify(recipeService).getPopulars(page, RecipeInfoVideoQuery.ALL);
      }

      @Test
      @DisplayName("Query가 NORMAL이면 getPopulars with NORMAL을 호출한다")
      void shouldCallGetPopularsWithNormalWhenQueryIsNormal() {
        Integer page = 0;
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

        setupPopularMocks(List.of(recipeId));
        doReturn(recipePage).when(recipeService).getPopulars(page, RecipeInfoVideoQuery.NORMAL);
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

        recipeInfoService.getRecommendRecipes(
            RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.NORMAL);

        verify(recipeService).getPopulars(page, RecipeInfoVideoQuery.NORMAL);
      }

      @Test
      @DisplayName("Query가 SHORTS이면 getPopulars with SHORTS를 호출한다")
      void shouldCallGetPopularsWithShortsWhenQueryIsShorts() {
        Integer page = 0;
        UUID recipeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

        setupPopularMocks(List.of(recipeId));
        doReturn(recipePage).when(recipeService).getPopulars(page, RecipeInfoVideoQuery.SHORTS);
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

        recipeInfoService.getRecommendRecipes(
            RecipeInfoRecommendType.POPULAR, userId, page, RecipeInfoVideoQuery.SHORTS);

        verify(recipeService).getPopulars(page, RecipeInfoVideoQuery.SHORTS);
      }
    }

    @Nested
    @DisplayName("트렌드 레시피 타입")
    class TrendingRecipeType {

      @Test
      @DisplayName("트렌드 레시피를 조회한다")
      void shouldReturnTrendingRecipes() {
        Integer page = 0;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<UUID> recipeIds = List.of(recipeId1, recipeId2);
        Page<UUID> recipeIdsPage = new PageImpl<>(recipeIds);
        List<Recipe> recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.SUCCESS));

        doReturn(recipeIdsPage).when(recipeRankService).getRecipeIds(RankingType.TRENDING, page);
        doReturn(recipes).when(recipeService).getValidRecipes(recipeIds);
        setupPopularMocks(recipeIds);
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

        Page<RecipeOverview> result =
            recipeInfoService.getRecommendRecipes(
                RecipeInfoRecommendType.TRENDING, userId, page, RecipeInfoVideoQuery.ALL);

        assertThat(result.getContent()).hasSize(2);
        verify(recipeRankService).getRecipeIds(RankingType.TRENDING, page);
        verify(recipeService).getValidRecipes(recipeIds);
      }
    }

    @Nested
    @DisplayName("셰프 레시피 타입")
    class ChefRecipeType {

      @Test
      @DisplayName("셰프 레시피를 조회한다")
      void shouldReturnChefRecipes() {
        Integer page = 0;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        List<UUID> recipeIds = List.of(recipeId1, recipeId2);
        Page<UUID> recipeIdsPage = new PageImpl<>(recipeIds);
        List<Recipe> recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.SUCCESS));

        doReturn(recipeIdsPage).when(recipeRankService).getRecipeIds(RankingType.CHEF, page);
        doReturn(recipes).when(recipeService).getValidRecipes(recipeIds);
        setupPopularMocks(recipeIds);
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

        Page<RecipeOverview> result =
            recipeInfoService.getRecommendRecipes(
                RecipeInfoRecommendType.CHEF, userId, page, RecipeInfoVideoQuery.ALL);

        assertThat(result.getContent()).hasSize(2);
        verify(recipeRankService).getRecipeIds(RankingType.CHEF, page);
        verify(recipeService).getValidRecipes(recipeIds);
      }
    }
  }

  @Nested
  @DisplayName("최근 레시피 히스토리 조회")
  class FindRecents {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private Page<RecipeHistory> viewStatuses;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();

        viewStatuses =
            new PageImpl<>(
                List.of(
                    createMockRecipeHistory(recipeId1, userId),
                    createMockRecipeHistory(recipeId2, userId)));

        recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.IN_PROGRESS));

        youtubeMetas =
            List.of(
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 만들기", recipeId1),
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "된장찌개 만들기", recipeId2));

        doReturn(viewStatuses).when(recipeHistoryService).getRecents(userId, page);
        doReturn(recipes).when(recipeService).getValidRecipes(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 최근 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnRecentRecipeRecords() {
          Page<RecipeHistoryOverview> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).hasSize(2);
          assertThat(result.getContent())
              .allMatch(
                  history ->
                      history.getRecipeId() != null
                          && history.getViewedAt() != null
                          && history.getVideoTitle() != null);
          verify(recipeHistoryService).getRecents(userId, page);
          verify(recipeService).getValidRecipes(anyList());
          verify(recipeYoutubeMetaService).getByRecipes(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자에게 뷰 상태가 없을 때")
    class GivenUserWithNoViewStatuses {

      private UUID userId;
      private Page<RecipeHistory> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses).when(recipeHistoryService).getRecents(userId, page);
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistory() {
          Page<RecipeHistoryOverview> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeHistoryService).getRecents(userId, page);
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패한 레시피만 있을 때")
    class GivenOnlyFailedRecipes {

      private UUID userId;
      private Page<RecipeHistory> viewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        UUID recipeId = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeHistory(recipeId, userId)));

        doReturn(viewStatuses).when(recipeHistoryService).getRecents(userId, page);
        doReturn(List.of()).when(recipeService).getValidRecipes(anyList()); // 실패한 레시피는 제외
        doReturn(List.of()).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForFailedRecipes() {
          Page<RecipeHistoryOverview> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).isEmpty();
          verify(recipeHistoryService).getRecents(userId, page);
          verify(recipeService).getValidRecipes(anyList());
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 히스토리 조회")
  class FindCategorized {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID와 카테고리 ID가 주어졌을 때")
    class GivenValidUserIdAndCategoryId {

      private UUID userId;
      private UUID recipeCategoryId;
      private Page<RecipeHistory> viewStatuses;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        page = 0;
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeHistory(recipeId1, userId)));
        recipes = List.of(createMockRecipe(recipeId1, RecipeStatus.SUCCESS));
        youtubeMetas = List.of(createMockRecipeYoutubeMeta(UUID.randomUUID(), "한식 요리", recipeId1));

        doReturn(viewStatuses)
            .when(recipeHistoryService)
            .getCategorized(userId, recipeCategoryId, page);
        doReturn(recipes).when(recipeService).getValidRecipes(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 히스토리를 조회한다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 해당 카테고리의 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnCategorizedRecipeRecords() {
          Page<RecipeHistoryOverview> result =
              recipeInfoService.getCategorized(userId, recipeCategoryId, page);

          assertThat(result.getContent()).hasSize(1);
          assertThat(result.getContent().get(0).getRecipeId()).isNotNull();
          assertThat(result.getContent().get(0).getViewedAt()).isNotNull();
          assertThat(result.getContent().get(0).getVideoTitle()).isNotNull();
          verify(recipeHistoryService).getCategorized(userId, recipeCategoryId, page);
          verify(recipeService).getValidRecipes(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 해당 카테고리에 레시피가 없을 때")
    class GivenCategoryWithNoRecipes {

      private UUID userId;
      private UUID recipeCategoryId;
      private Page<RecipeHistory> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses)
            .when(recipeHistoryService)
            .getCategorized(userId, recipeCategoryId, page);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 히스토리를 조회한다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForCategory() {
          Page<RecipeHistoryOverview> result =
              recipeInfoService.getCategorized(userId, recipeCategoryId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeHistoryService).getCategorized(userId, recipeCategoryId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("미분류 레시피 히스토리 조회")
  class FindUnCategorized {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private Page<RecipeHistory> viewStatuses;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeHistory(recipeId1, userId)));
        recipes = List.of(createMockRecipe(recipeId1, RecipeStatus.SUCCESS));
        youtubeMetas = List.of(createMockRecipeYoutubeMeta(UUID.randomUUID(), "미분류 요리", recipeId1));

        doReturn(viewStatuses).when(recipeHistoryService).getUnCategorized(userId, page);
        doReturn(recipes).when(recipeService).getValidRecipes(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 미분류 레시피 히스토리를 조회한다면")
      class WhenFindingUnCategorized {

        @Test
        @DisplayName("Then - 미분류 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnUnCategorizedRecipeRecords() {
          Page<RecipeHistoryOverview> result = recipeInfoService.getUnCategorized(userId, page);

          assertThat(result.getContent()).hasSize(1);
          assertThat(result.getContent().get(0).getRecipeId()).isNotNull();
          assertThat(result.getContent().get(0).getViewedAt()).isNotNull();
          assertThat(result.getContent().get(0).getVideoTitle()).isNotNull();
          verify(recipeHistoryService).getUnCategorized(userId, page);
          verify(recipeService).getValidRecipes(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피가 없을 때")
    class GivenNoUnCategorizedRecipes {

      private UUID userId;
      private Page<RecipeHistory> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses).when(recipeHistoryService).getUnCategorized(userId, page);
      }

      @Nested
      @DisplayName("When - 미분류 레시피 히스토리를 조회한다면")
      class WhenFindingUnCategorized {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForUnCategorized() {
          Page<RecipeHistoryOverview> result = recipeInfoService.getUnCategorized(userId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeHistoryService).getUnCategorized(userId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 개수 조회")
  class FindCategoryCounts {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private List<RecipeCategory> categories;
      private List<RecipeHistoryCategorizedCount> counts;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        categories =
            List.of(
                createMockRecipeCategory(categoryId1, "한식"),
                createMockRecipeCategory(categoryId2, "양식"));

        counts =
            List.of(
                createMockRecipeHistoryCount(categoryId1, 5),
                createMockRecipeHistoryCount(categoryId2, 3));

        doReturn(categories).when(recipeCategoryService).getUsers(userId);
        doReturn(counts).when(recipeHistoryService).countByCategories(anyList());
        doReturn(RecipeHistoryUnCategorizedCount.of(2))
            .when(recipeHistoryService)
            .countUncategorized(userId);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리별 레시피 개수와 totalCount가 올바르게 반환되어야 한다")
        void thenShouldReturnCategoriesWithCountAndTotal() {
          var result = recipeInfoService.getCategoryCounts(userId);

          assertThat(result.getCategorizedCounts()).hasSize(2);
          assertThat(result.getCategorizedCounts())
              .allMatch(category -> category.getCategory() != null);
          assertThat(result.getUncategorizedCount()).isEqualTo(2);
          assertThat(result.getTotalCount()).isEqualTo(10); // 5 + 3 + 2
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeHistoryService).countByCategories(anyList());
          verify(recipeHistoryService).countUncategorized(userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 카테고리에 레시피가 없을 때")
    class GivenCategoriesWithNoRecipes {

      private UUID userId;
      private List<RecipeCategory> categories;
      private List<RecipeHistoryCategorizedCount> counts;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        UUID categoryId1 = UUID.randomUUID();

        categories = List.of(createMockRecipeCategory(categoryId1, "빈 카테고리"));
        counts = List.of();

        doReturn(categories).when(recipeCategoryService).getUsers(userId);
        doReturn(counts).when(recipeHistoryService).countByCategories(anyList());
        doReturn(RecipeHistoryUnCategorizedCount.of(0))
            .when(recipeHistoryService)
            .countUncategorized(userId);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리 개수가 0으로 반환되어야 한다")
        void thenShouldReturnCategoriesWithZeroCount() {
          var result = recipeInfoService.getCategoryCounts(userId);

          assertThat(result.getCategorizedCounts()).hasSize(1);
          assertThat(result.getCategorizedCounts().get(0).getCategory()).isNotNull();
          assertThat(result.getUncategorizedCount()).isEqualTo(0);
          assertThat(result.getTotalCount()).isEqualTo(0);
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeHistoryService).countByCategories(anyList());
          verify(recipeHistoryService).countUncategorized(userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피만 있을 때")
    class GivenOnlyUncategorizedRecipes {

      private UUID userId;
      private List<RecipeCategory> categories;
      private List<RecipeHistoryCategorizedCount> counts;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();

        categories = List.of();
        counts = List.of();

        doReturn(categories).when(recipeCategoryService).getUsers(userId);
        doReturn(counts).when(recipeHistoryService).countByCategories(anyList());
        doReturn(RecipeHistoryUnCategorizedCount.of(7))
            .when(recipeHistoryService)
            .countUncategorized(userId);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 미분류 개수만 반환되고 totalCount가 올바르게 계산되어야 한다")
        void thenShouldReturnOnlyUncategorizedCount() {
          var result = recipeInfoService.getCategoryCounts(userId);

          assertThat(result.getCategorizedCounts()).isEmpty();
          assertThat(result.getUncategorizedCount()).isEqualTo(7);
          assertThat(result.getTotalCount()).isEqualTo(7);
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeHistoryService).countByCategories(List.of());
          verify(recipeHistoryService).countUncategorized(userId);
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리 삭제")
  class DeleteCategory {

    @Nested
    @DisplayName("Given - 유효한 카테고리 ID가 주어졌을 때")
    class GivenValidCategoryId {

      private UUID categoryId;

      @BeforeEach
      void setUp() {
        categoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 카테고리를 삭제한다면")
      class WhenDeletingCategory {

        @Test
        @DisplayName("Then - 카테고리와 관련된 뷰 상태가 모두 삭제되어야 한다")
        void thenShouldDeleteCategoryAndRelatedViewStatus() {
          recipeInfoService.deleteCategory(categoryId);

          verify(recipeHistoryService).unCategorize(categoryId);
          verify(recipeCategoryService).delete(categoryId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자에게 카테고리가 없을 때")
    class GivenUserWithNoCategories {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();

        doReturn(List.of()).when(recipeCategoryService).getUsers(userId);
        doReturn(List.of()).when(recipeHistoryService).countByCategories(List.of());
        doReturn(RecipeHistoryUnCategorizedCount.of(0))
            .when(recipeHistoryService)
            .countUncategorized(userId);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 빈 카테고리 목록을 반환해야 한다")
        void thenShouldReturnEmptyCategories() {
          var result = recipeInfoService.getCategoryCounts(userId);

          assertThat(result.getCategorizedCounts()).isEmpty();
          assertThat(result.getUncategorizedCount()).isEqualTo(0);
          assertThat(result.getTotalCount()).isEqualTo(0);
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeHistoryService).countByCategories(List.of());
          verify(recipeHistoryService).countUncategorized(userId);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 검색")
  class SearchRecipes {

    @Nested
    @DisplayName("Given - 유효한 검색어가 주어졌을 때")
    class GivenValidSearchQuery {

      private String query;
      private Integer page;
      private Page<RecipeSearch> searchResults;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private List<RecipeDetailMeta> detailMetas;
      private List<RecipeTag> tags;
      private UUID userId;

      @BeforeEach
      void setUp() {
        query = "김치찌개";
        page = 0;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        userId = UUID.randomUUID();

        RecipeSearch search1 = mock(RecipeSearch.class);
        RecipeSearch search2 = mock(RecipeSearch.class);
        doReturn(recipeId1.toString()).when(search1).getId();
        doReturn(recipeId2.toString()).when(search2).getId();

        searchResults = new PageImpl<>(List.of(search1, search2));

        recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.SUCCESS));

        youtubeMetas =
            List.of(
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 만들기", recipeId1),
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 레시피", recipeId2));

        detailMetas =
            List.of(
                createMockRecipeDetailMeta(recipeId1, "맛있는 김치찌개"),
                createMockRecipeDetailMeta(recipeId2, "전통 김치찌개"));

        tags =
            List.of(
                createMockRecipeTag(recipeId1, "한식"),
                createMockRecipeTag(recipeId1, "찌개"),
                createMockRecipeTag(recipeId2, "한식"));

        doReturn(searchResults).when(recipeSearchService).search(eq(userId), eq(query), eq(page));
        doReturn(recipes).when(recipeService).gets(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
        doReturn(detailMetas).when(recipeDetailMetaService).getIn(anyList());
        doReturn(tags).when(recipeTagService).getIn(anyList());
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        @Test
        @DisplayName("Then - 검색 결과를 반환해야 한다")
        void thenShouldReturnSearchResults() {
          Page<RecipeOverview> result = recipeInfoService.searchRecipes(page, query, userId);

          assertThat(result.getContent()).hasSize(2);
          assertThat(result.getContent())
              .allMatch(
                  overview ->
                      overview.getRecipeId() != null
                          && overview.getVideoTitle() != null
                          && overview.getDescription() != null
                          && overview.getTags() != null);
          verify(recipeSearchService).search(eq(userId), eq(query), eq(page));
          verify(recipeService).gets(anyList());
          verify(recipeYoutubeMetaService).getByRecipes(anyList());
          verify(recipeDetailMetaService).getIn(anyList());
          verify(recipeTagService).getIn(anyList());
          verify(recipeHistoryService).getByRecipes(anyList(), eq(userId));
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자가 일부 레시피를 본 경우")
    class GivenUserViewedSomeRecipes {

      private String query;
      private Integer page;
      private UUID recipeId1;
      private UUID recipeId2;
      private UUID userId;
      private Page<RecipeSearch> searchResults;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private List<RecipeDetailMeta> detailMetas;
      private List<RecipeTag> tags;

      @BeforeEach
      void setUp() {
        query = "김치찌개";
        page = 0;
        recipeId1 = UUID.randomUUID();
        recipeId2 = UUID.randomUUID();
        userId = UUID.randomUUID();

        RecipeSearch search1 = mock(RecipeSearch.class);
        RecipeSearch search2 = mock(RecipeSearch.class);
        doReturn(recipeId1.toString()).when(search1).getId();
        doReturn(recipeId2.toString()).when(search2).getId();

        searchResults = new PageImpl<>(List.of(search1, search2));

        recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.SUCCESS));

        youtubeMetas =
            List.of(
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 만들기", recipeId1),
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 레시피", recipeId2));

        detailMetas =
            List.of(
                createMockRecipeDetailMeta(recipeId1, "맛있는 김치찌개"),
                createMockRecipeDetailMeta(recipeId2, "전통 김치찌개"));

        tags = List.of(createMockRecipeTag(recipeId1, "한식"), createMockRecipeTag(recipeId2, "한식"));

        doReturn(searchResults).when(recipeSearchService).search(eq(userId), eq(query), eq(page));
        doReturn(recipes).when(recipeService).gets(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
        doReturn(detailMetas).when(recipeDetailMetaService).getIn(anyList());
        doReturn(tags).when(recipeTagService).getIn(anyList());

        // recipeId1만 본 상태로 설정
        RecipeHistory viewStatus1 = createMockRecipeHistory(recipeId1, userId);
        doReturn(List.of(viewStatus1))
            .when(recipeHistoryService)
            .getByRecipes(anyList(), eq(userId));
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        @Test
        @DisplayName("Then - 사용자가 본 레시피와 안 본 레시피가 올바르게 구분되어야 한다")
        void thenShouldDistinguishViewedAndNotViewedRecipes() {
          Page<RecipeOverview> result = recipeInfoService.searchRecipes(page, query, userId);

          assertThat(result.getContent()).hasSize(2);

          RecipeOverview overview1 =
              result.getContent().stream()
                  .filter(o -> o.getRecipeId().equals(recipeId1))
                  .findFirst()
                  .orElse(null);
          RecipeOverview overview2 =
              result.getContent().stream()
                  .filter(o -> o.getRecipeId().equals(recipeId2))
                  .findFirst()
                  .orElse(null);

          assertThat(overview1).isNotNull();
          assertThat(overview1.getIsViewed()).isTrue();
          assertThat(overview2).isNotNull();
          assertThat(overview2.getIsViewed()).isFalse();
        }
      }
    }

    @Nested
    @DisplayName("Given - 검색 결과가 없을 때")
    class GivenNoSearchResults {

      private String query;
      private Integer page;
      private Page<RecipeSearch> emptySearchResults;
      private UUID userId;

      @BeforeEach
      void setUp() {
        query = "존재하지않는레시피";
        page = 0;
        emptySearchResults = new PageImpl<>(List.of());
        userId = UUID.randomUUID();

        doReturn(emptySearchResults)
            .when(recipeSearchService)
            .search(eq(userId), eq(query), eq(page));
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmptyResults() {
          Page<RecipeOverview> result = recipeInfoService.searchRecipes(page, query, userId);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeSearchService).search(eq(userId), eq(query), eq(page));
        }
      }
    }

    @Nested
    @DisplayName("Given - 검색된 레시피의 유튜브 메타가 누락되었을 때")
    class GivenSearchResultWithMissingYoutubeMeta {

      private String query;
      private Integer page;
      private Page<RecipeSearch> searchResults;
      private List<Recipe> recipes;
      private UUID userId;

      @BeforeEach
      void setUp() {
        query = "파스타";
        page = 0;
        UUID recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        RecipeSearch search = mock(RecipeSearch.class);
        doReturn(recipeId.toString()).when(search).getId();

        searchResults = new PageImpl<>(List.of(search));
        recipes = List.of(createMockRecipe(recipeId, RecipeStatus.SUCCESS));

        doReturn(searchResults).when(recipeSearchService).search(eq(userId), eq(query), eq(page));
        doReturn(recipes).when(recipeService).gets(anyList());
        doReturn(List.of()).when(recipeYoutubeMetaService).getByRecipes(anyList()); // 메타 누락
        doReturn(List.of()).when(recipeDetailMetaService).getIn(anyList());
        doReturn(List.of()).when(recipeTagService).getIn(anyList());
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));
      }

      @Nested
      @DisplayName("When - 레시피를 검색한다면")
      class WhenSearchingRecipes {

        @Test
        @DisplayName("Then - 유튜브 메타가 없는 레시피는 제외되어야 한다")
        void thenShouldExcludeRecipesWithoutYoutubeMeta() {
          Page<RecipeOverview> result = recipeInfoService.searchRecipes(page, query, userId);

          assertThat(result.getContent()).isEmpty();
          verify(recipeSearchService).search(eq(userId), eq(query), eq(page));
          verify(recipeService).gets(anyList());
          verify(recipeYoutubeMetaService).getByRecipes(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 페이지의 검색 결과가 있을 때")
    class GivenMultiplePages {

      private String query;
      private Integer page;
      private Page<RecipeSearch> searchResults;
      private Recipe recipe;
      private RecipeYoutubeMeta youtubeMeta;
      private RecipeDetailMeta detailMeta;
      private List<RecipeTag> tags;
      private UUID userId;

      @BeforeEach
      void setUp() {
        query = "찌개";
        page = 1;
        UUID recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();

        RecipeSearch search = mock(RecipeSearch.class);
        doReturn(recipeId.toString()).when(search).getId();

        // 전체 25개 결과 중 두 번째 페이지
        searchResults = new PageImpl<>(List.of(search), RecipeSearchPageRequest.create(1), 25);
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
        youtubeMeta = createMockRecipeYoutubeMeta(UUID.randomUUID(), "된장찌개", recipeId);
        detailMeta = createMockRecipeDetailMeta(recipeId, "구수한 된장찌개");
        tags = List.of(createMockRecipeTag(recipeId, "한식"));

        doReturn(searchResults).when(recipeSearchService).search(eq(userId), eq(query), eq(page));
        doReturn(List.of(recipe)).when(recipeService).gets(anyList());
        doReturn(List.of(youtubeMeta)).when(recipeYoutubeMetaService).getByRecipes(anyList());
        doReturn(List.of(detailMeta)).when(recipeDetailMetaService).getIn(anyList());
        doReturn(tags).when(recipeTagService).getIn(anyList());
        doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));
      }

      @Nested
      @DisplayName("When - 두 번째 페이지를 검색한다면")
      class WhenSearchingSecondPage {

        @Test
        @DisplayName("Then - 올바른 페이지 정보와 함께 결과를 반환해야 한다")
        void thenShouldReturnCorrectPageInfo() {
          Page<RecipeOverview> result = recipeInfoService.searchRecipes(page, query, userId);

          assertThat(result.getContent()).hasSize(1);
          assertThat(result.getTotalElements()).isEqualTo(25);
          assertThat(result.getNumber()).isEqualTo(1);
          verify(recipeSearchService).search(eq(userId), eq(query), eq(page));
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 진행 상황 조회")
  class FindRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;
      private Recipe recipe;
      private List<RecipeProgress> progresses;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

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

        doReturn(progresses).when(recipeProgressService).gets(recipeId);
        doReturn(recipe).when(recipeService).get(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenFindingRecipeProgress {

        @Test
        @DisplayName("Then - 레시피 진행 상황을 반환해야 한다")
        void thenShouldReturnRecipeProgress() {
          RecipeProgressStatus result = recipeInfoService.getRecipeProgress(recipeId);

          assertThat(result).isNotNull();
          verify(recipeProgressService).gets(recipeId);
          verify(recipeService).get(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 진행 상황이 없는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithNoProgress {

      private UUID recipeId;
      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.IN_PROGRESS);

        doReturn(List.of()).when(recipeProgressService).gets(recipeId);
        doReturn(recipe).when(recipeService).get(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenFindingRecipeProgress {

        @Test
        @DisplayName("Then - 빈 진행 상황과 레시피를 반환해야 한다")
        void thenShouldReturnEmptyProgressWithRecipe() {
          RecipeProgressStatus result = recipeInfoService.getRecipeProgress(recipeId);

          assertThat(result).isNotNull();
          verify(recipeProgressService).gets(recipeId);
          verify(recipeService).get(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();

        doThrow(new RecipeInfoException(RecipeErrorCode.RECIPE_NOT_FOUND))
            .when(recipeService)
            .get(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenFindingRecipeProgress {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생해야 한다")
        void thenShouldThrowRecipeNotFoundException() {
          assertThatThrownBy(() -> recipeInfoService.getRecipeProgress(recipeId))
              .isInstanceOf(RecipeInfoException.class)
              .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_NOT_FOUND);

          verify(recipeService).get(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 브리핑을 포함한 진행 상황이 있는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithBriefingProgress {

      private UUID recipeId;
      private Recipe recipe;
      private List<RecipeProgress> progresses;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

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

        doReturn(progresses).when(recipeProgressService).gets(recipeId);
        doReturn(recipe).when(recipeService).get(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenFindingRecipeProgress {

        @Test
        @DisplayName("Then - 브리핑을 포함한 진행 상황을 반환해야 한다")
        void thenShouldReturnProgressWithBriefing() {
          RecipeProgressStatus result = recipeInfoService.getRecipeProgress(recipeId);

          assertThat(result).isNotNull();
          verify(recipeProgressService).gets(recipeId);
          verify(recipeService).get(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 복잡한 다단계 진행 상황이 있는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithComplexProgress {

      private UUID recipeId;
      private Recipe recipe;
      private List<RecipeProgress> progresses;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);

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

        doReturn(progresses).when(recipeProgressService).gets(recipeId);
        doReturn(recipe).when(recipeService).get(recipeId);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회한다면")
      class WhenFindingRecipeProgress {

        @Test
        @DisplayName("Then - 모든 복잡한 진행 단계를 포함한 상황을 반환해야 한다")
        void thenShouldReturnComplexProgress() {
          RecipeProgressStatus result = recipeInfoService.getRecipeProgress(recipeId);

          assertThat(result).isNotNull();
          verify(recipeProgressService).gets(recipeId);
          verify(recipeService).get(recipeId);
        }
      }
    }
  }

  // Helper Methods
  private RecipeHistory createMockRecipeHistory(UUID recipeId, UUID userId) {
    RecipeHistory viewStatus = mock(RecipeHistory.class);
    doReturn(recipeId).when(viewStatus).getRecipeId();
    doReturn(userId).when(viewStatus).getUserId();
    doReturn(LocalDateTime.now()).when(viewStatus).getViewedAt();
    return viewStatus;
  }

  private Recipe createMockRecipe(UUID recipeId, RecipeStatus status) {
    Recipe recipe = mock(Recipe.class);
    doReturn(recipeId).when(recipe).getId();
    doReturn(status).when(recipe).getRecipeStatus();
    doReturn(10).when(recipe).getViewCount();
    doReturn(LocalDateTime.now()).when(recipe).getCreatedAt();
    doReturn(LocalDateTime.now()).when(recipe).getUpdatedAt();

    doReturn(status == RecipeStatus.FAILED).when(recipe).isFailed();
    doReturn(status == RecipeStatus.SUCCESS).when(recipe).isSuccess();

    return recipe;
  }

  private RecipeYoutubeMeta createMockRecipeYoutubeMeta(UUID metaId, String title, UUID recipeId) {
    RecipeYoutubeMeta youtubeMeta = mock(RecipeYoutubeMeta.class);
    doReturn(metaId).when(youtubeMeta).getId();
    doReturn(recipeId).when(youtubeMeta).getRecipeId();
    doReturn(title).when(youtubeMeta).getTitle();
    doReturn("test_video_id").when(youtubeMeta).getVideoId();
    doReturn(URI.create("https://www.youtube.com/watch?v=test123")).when(youtubeMeta).getVideoUri();
    doReturn(URI.create("https://img.youtube.com/vi/test123/maxresdefault.jpg"))
        .when(youtubeMeta)
        .getThumbnailUrl();
    doReturn(300).when(youtubeMeta).getVideoSeconds();
    doReturn(false).when(youtubeMeta).isBanned();
    doReturn(false).when(youtubeMeta).isBlocked();
    doReturn(LocalDateTime.now()).when(youtubeMeta).getCreatedAt();
    return youtubeMeta;
  }

  private RecipeCategory createMockRecipeCategory(UUID categoryId, String name) {
    RecipeCategory category = mock(RecipeCategory.class);
    doReturn(categoryId).when(category).getId();
    doReturn(name).when(category).getName();
    doReturn(LocalDateTime.now()).when(category).getCreatedAt();
    return category;
  }

  private RecipeHistoryCategorizedCount createMockRecipeHistoryCount(UUID categoryId, int count) {
    RecipeHistoryCategorizedCount statusCount = mock(RecipeHistoryCategorizedCount.class);
    doReturn(categoryId).when(statusCount).getCategoryId();
    doReturn(count).when(statusCount).getCount();
    return statusCount;
  }

  private YoutubeVideoInfo createMockYoutubeVideoInfo() {
    UriComponents uriComponents =
        UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=test_video_id").build();
    URI uri = uriComponents.toUri();
    YoutubeUri youtubeUri = YoutubeUri.from(uri);
    return YoutubeVideoInfo.from(
        youtubeUri,
        "테스트 요리 영상",
        URI.create("https://img.youtube.com/vi/test_video_id/maxresdefault.jpg"),
        300,
        YoutubeMetaType.NORMAL);
  }

  private void setupFullRecipeInfoMocks(UUID recipeId, UUID userId, Recipe recipe) {
    doReturn(Collections.emptyList()).when(recipeStepService).gets(recipeId);
    doReturn(Collections.emptyList()).when(recipeIngredientService).gets(recipeId);
    doReturn(mock(RecipeDetailMeta.class)).when(recipeDetailMetaService).get(recipeId);
    doReturn(Collections.emptyList()).when(recipeProgressService).gets(recipeId);
    doReturn(Collections.emptyList()).when(recipeTagService).gets(recipeId);
    doReturn(Collections.emptyList()).when(recipeBriefingService).gets(recipeId);

    RecipeYoutubeMeta youtubeMeta =
        createMockRecipeYoutubeMeta(UUID.randomUUID(), "테스트 영상", recipeId);
    doReturn(youtubeMeta).when(recipeYoutubeMetaService).get(recipeId);

    RecipeHistory viewStatus = createMockRecipeHistory(recipeId, userId);
    doReturn(viewStatus).when(recipeHistoryService).getWithView(userId, recipeId);
  }

  private void setupPopularMocks(List<UUID> recipeIds) {
    List<RecipeYoutubeMeta> youtubeMetas =
        recipeIds.stream()
            .map(recipeId -> createMockRecipeYoutubeMeta(UUID.randomUUID(), "인기 영상", recipeId))
            .toList();

    List<RecipeDetailMeta> detailMetas =
        recipeIds.stream()
            .map(
                recipeId -> {
                  RecipeDetailMeta detailMeta = mock(RecipeDetailMeta.class);
                  doReturn(recipeId).when(detailMeta).getRecipeId();
                  doReturn("상세 설명").when(detailMeta).getDescription();
                  return detailMeta;
                })
            .toList();

    List<RecipeTag> tags =
        recipeIds.stream()
            .flatMap(
                recipeId ->
                    List.of(
                        createMockRecipeTag(recipeId, "태그1"), createMockRecipeTag(recipeId, "태그2"))
                        .stream())
            .toList();

    doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(recipeIds);
    doReturn(detailMetas).when(recipeDetailMetaService).getIn(recipeIds);
    doReturn(tags).when(recipeTagService).getIn(recipeIds);
  }

  private RecipeTag createMockRecipeTag(UUID recipeId, String tagName) {
    RecipeTag tag = mock(RecipeTag.class);
    doReturn(recipeId).when(tag).getRecipeId();
    doReturn(tagName).when(tag).getTag();
    return tag;
  }

  private RecipeDetailMeta createMockRecipeDetailMeta(UUID recipeId, String description) {
    RecipeDetailMeta detailMeta = mock(RecipeDetailMeta.class);
    doReturn(recipeId).when(detailMeta).getRecipeId();
    doReturn(description).when(detailMeta).getDescription();
    doReturn(2).when(detailMeta).getServings();
    doReturn(30).when(detailMeta).getCookTime();
    doReturn(LocalDateTime.now()).when(detailMeta).getCreatedAt();
    return detailMeta;
  }

  @Nested
  @DisplayName("음식 종류별 레시피 조회")
  class GetCuisineRecipes {

    @Test
    @DisplayName("한식 레시피를 조회한다")
    void shouldReturnKoreanRecipes() {
      Integer page = 0;
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe1 = createMockRecipe(recipeId1, RecipeStatus.SUCCESS);
      Recipe recipe2 = createMockRecipe(recipeId2, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2));

      setupPopularMocks(List.of(recipeId1, recipeId2));
      doReturn(recipePage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.KOREAN.getKoreanName(), page);
      doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.KOREAN, userId, page);

      assertThat(result.getContent()).hasSize(2);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.KOREAN.getKoreanName(), page);
    }

    @Test
    @DisplayName("중식 레시피를 조회한다")
    void shouldReturnChineseRecipes() {
      Integer page = 0;
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

      setupPopularMocks(List.of(recipeId));
      doReturn(recipePage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.CHINESE.getKoreanName(), page);
      doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.CHINESE, userId, page);

      assertThat(result.getContent()).hasSize(1);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.CHINESE.getKoreanName(), page);
    }

    @Test
    @DisplayName("일식 레시피를 조회한다")
    void shouldReturnJapaneseRecipes() {
      Integer page = 0;
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

      setupPopularMocks(List.of(recipeId));
      doReturn(recipePage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.JAPANESE.getKoreanName(), page);
      doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.JAPANESE, userId, page);

      assertThat(result.getContent()).hasSize(1);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.JAPANESE.getKoreanName(), page);
    }

    @Test
    @DisplayName("양식 레시피를 조회한다")
    void shouldReturnWesternRecipes() {
      Integer page = 0;
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

      setupPopularMocks(List.of(recipeId));
      doReturn(recipePage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.WESTERN.getKoreanName(), page);
      doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.WESTERN, userId, page);

      assertThat(result.getContent()).hasSize(1);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.WESTERN.getKoreanName(), page);
    }

    @Test
    @DisplayName("분식 레시피를 조회한다")
    void shouldReturnSnackRecipes() {
      Integer page = 0;
      UUID recipeId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      Recipe recipe = createMockRecipe(recipeId, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe));

      setupPopularMocks(List.of(recipeId));
      doReturn(recipePage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.SNACK.getKoreanName(), page);
      doReturn(List.of()).when(recipeHistoryService).getByRecipes(anyList(), any(UUID.class));

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.SNACK, userId, page);

      assertThat(result.getContent()).hasSize(1);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.SNACK.getKoreanName(), page);
    }

    @Test
    @DisplayName("레시피가 없을 때 빈 페이지를 반환한다")
    void shouldReturnEmptyPageWhenNoRecipes() {
      Integer page = 0;
      UUID userId = UUID.randomUUID();

      Page<Recipe> emptyPage = new PageImpl<>(List.of());

      doReturn(emptyPage)
          .when(recipeService)
          .getCuisines(RecipeInfoCuisineType.KOREAN.getKoreanName(), page);

      Page<RecipeOverview> result =
          recipeInfoService.getCuisineRecipes(RecipeInfoCuisineType.KOREAN, userId, page);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
      verify(recipeService).getCuisines(RecipeInfoCuisineType.KOREAN.getKoreanName(), page);
    }
  }
}
