package com.cheftory.api.recipeinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.cheftory.api.recipeinfo.identify.RecipeIdentifyService;
import com.cheftory.api.recipeinfo.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredientService;
import com.cheftory.api.recipeinfo.model.CountRecipeCategory;
import com.cheftory.api.recipeinfo.model.FullRecipeInfo;
import com.cheftory.api.recipeinfo.model.RecipeHistory;
import com.cheftory.api.recipeinfo.model.RecipeOverview;
import com.cheftory.api.recipeinfo.model.RecipeProgressStatus;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressService;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipeinfo.search.RecipeSearchService;
import com.cheftory.api.recipeinfo.step.RecipeStepService;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.tag.RecipeTagService;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatusCount;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatusService;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaService;
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
  private RecipeViewStatusService recipeViewStatusService;
  private RecipeCategoryService recipeCategoryService;
  private RecipeYoutubeMetaService recipeYoutubeMetaService;
  private RecipeStepService recipeStepService;
  private RecipeIngredientService recipeIngredientService;
  private RecipeDetailMetaService recipeDetailMetaService;
  private RecipeProgressService recipeProgressService;
  private RecipeTagService recipeTagService;
  private RecipeIdentifyService recipeIdentifyService;
  private RecipeBriefingService recipeBriefingService;
  private AsyncRecipeInfoCreationService asyncRecipeCreationService;
  private RecipeInfoService recipeInfoService;
  private RecipeSearchService recipeSearchService;

  @BeforeEach
  void setUp() {
    recipeService = mock(RecipeService.class);
    recipeViewStatusService = mock(RecipeViewStatusService.class);
    recipeCategoryService = mock(RecipeCategoryService.class);
    recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
    recipeStepService = mock(RecipeStepService.class);
    recipeIngredientService = mock(RecipeIngredientService.class);
    recipeDetailMetaService = mock(RecipeDetailMetaService.class);
    recipeProgressService = mock(RecipeProgressService.class);
    recipeTagService = mock(RecipeTagService.class);
    recipeIdentifyService = mock(RecipeIdentifyService.class);
    recipeBriefingService = mock(RecipeBriefingService.class);
    asyncRecipeCreationService = mock(AsyncRecipeInfoCreationService.class);
    recipeSearchService = mock(RecipeSearchService.class);

    recipeInfoService =
        new RecipeInfoService(
            asyncRecipeCreationService,
            recipeStepService,
            recipeViewStatusService,
            recipeCategoryService,
            recipeYoutubeMetaService,
            recipeIngredientService,
            recipeDetailMetaService,
            recipeProgressService,
            recipeTagService,
            recipeIdentifyService,
            recipeBriefingService,
            recipeService,
            recipeSearchService);
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

      UUID result = recipeInfoService.create(uri, userId);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeViewStatusService).create(userId, recipeId);
      verify(asyncRecipeCreationService, never()).create(any(), any(), any());
    }

    @Test
    @DisplayName("알 수 없는 예외가 발생하면 RECIPE_CREATE_FAIL 예외를 던진다")
    void shouldThrowRecipeCreateFailForUnknownException() {
      URI uri = URI.create("https://youtube.com/watch?v=unknown");
      UUID userId = UUID.randomUUID();

      doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_CREATE_FAIL))
          .when(recipeYoutubeMetaService)
          .getByUrl(uri);

      assertThatThrownBy(() -> recipeInfoService.create(uri, userId))
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

      assertThatThrownBy(() -> recipeInfoService.create(uri, userId))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_BANNED);
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

      UUID result = recipeInfoService.createNewRecipe(uri, userId);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeIdentifyService).create(uri);
      verify(recipeYoutubeMetaService).create(videoInfo, recipeId);
      verify(asyncRecipeCreationService).create(recipeId, videoInfo.getVideoId(), uri);
      verify(recipeViewStatusService).create(userId, recipeId);
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

      UUID result = recipeInfoService.createNewRecipe(uri, userId);

      assertThat(result).isEqualTo(recipeId);
      verify(recipeViewStatusService).create(userId, recipeId);
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

      assertThatThrownBy(() -> recipeInfoService.createNewRecipe(uri, userId))
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

      assertThatThrownBy(() -> recipeInfoService.createNewRecipe(uri, userId))
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

      FullRecipeInfo result = recipeInfoService.getFullRecipe(recipeId, userId);

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

      assertThatThrownBy(() -> recipeInfoService.getFullRecipe(recipeId, userId))
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

      assertThatThrownBy(() -> recipeInfoService.getFullRecipe(recipeId, userId))
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

      assertThatThrownBy(() -> recipeInfoService.getFullRecipe(recipeId, userId))
          .isInstanceOf(RecipeInfoException.class)
          .hasFieldOrPropertyWithValue("errorMessage", RecipeInfoErrorCode.RECIPE_CREATE_FAIL);
    }
  }

  @Nested
  @DisplayName("인기 레시피 조회")
  class FindPopulars {

    @Test
    @DisplayName("성공한 레시피들만 인기 목록에 포함된다")
    void shouldReturnOnlySuccessfulRecipesInPopularList() {
      Integer page = 0;
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();

      Recipe recipe1 = createMockRecipe(recipeId1, RecipeStatus.SUCCESS);
      Recipe recipe2 = createMockRecipe(recipeId2, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2));

      setupPopularMocks(List.of(recipeId1, recipeId2));
      doReturn(recipePage).when(recipeService).getPopulars(page);

      Page<RecipeOverview> result = recipeInfoService.getPopulars(page);

      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent()).allMatch(overview -> overview.getRecipe() != null);
      verify(recipeService).getPopulars(page);
    }

    @Test
    @DisplayName("성공한 레시피가 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoSuccessfulRecipes() {
      Integer page = 0;
      Page<Recipe> emptyRecipePage = new PageImpl<>(List.of());

      doReturn(emptyRecipePage).when(recipeService).getPopulars(page);

      Page<RecipeOverview> result = recipeInfoService.getPopulars(page);

      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
      verify(recipeService).getPopulars(page);
    }

    @Test
    @DisplayName("유튜브 메타데이터가 누락된 레시피는 제외된다")
    void shouldExcludeRecipesWithMissingYoutubeMeta() {
      Integer page = 0;
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();

      Recipe recipe1 = createMockRecipe(recipeId1, RecipeStatus.SUCCESS);
      Recipe recipe2 = createMockRecipe(recipeId2, RecipeStatus.SUCCESS);
      Page<Recipe> recipePage = new PageImpl<>(List.of(recipe1, recipe2));

      List<RecipeYoutubeMeta> youtubeMetas =
          List.of(createMockRecipeYoutubeMeta(UUID.randomUUID(), "정상 영상", recipeId1));

      doReturn(recipePage).when(recipeService).getPopulars(page);
      doReturn(youtubeMetas)
          .when(recipeYoutubeMetaService)
          .getByRecipes(List.of(recipeId1, recipeId2));
      doReturn(List.of()).when(recipeDetailMetaService).getIn(List.of(recipeId1, recipeId2));
      doReturn(List.of()).when(recipeTagService).getIn(List.of(recipeId1, recipeId2));

      Page<RecipeOverview> result = recipeInfoService.getPopulars(page);

      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getRecipe().getId()).isEqualTo(recipeId1);
    }
  }

  @Nested
  @DisplayName("최근 레시피 히스토리 조회")
  class FindRecents {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private Page<RecipeViewStatus> viewStatuses;
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
                    createMockRecipeViewStatus(recipeId1, userId),
                    createMockRecipeViewStatus(recipeId2, userId)));

        recipes =
            List.of(
                createMockRecipe(recipeId1, RecipeStatus.SUCCESS),
                createMockRecipe(recipeId2, RecipeStatus.IN_PROGRESS));

        youtubeMetas =
            List.of(
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "김치찌개 만들기", recipeId1),
                createMockRecipeYoutubeMeta(UUID.randomUUID(), "된장찌개 만들기", recipeId2));

        doReturn(viewStatuses).when(recipeViewStatusService).getRecentUsers(userId, page);
        doReturn(recipes).when(recipeService).getsNotFailed(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 최근 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnRecentRecipeHistories() {
          Page<RecipeHistory> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).hasSize(2);
          assertThat(result.getContent())
              .allMatch(
                  history ->
                      history.getRecipe() != null
                          && history.getRecipeViewStatus() != null
                          && history.getYoutubeMeta() != null);
          verify(recipeViewStatusService).getRecentUsers(userId, page);
          verify(recipeService).getsNotFailed(anyList());
          verify(recipeYoutubeMetaService).getByRecipes(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자에게 뷰 상태가 없을 때")
    class GivenUserWithNoViewStatuses {

      private UUID userId;
      private Page<RecipeViewStatus> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses).when(recipeViewStatusService).getRecentUsers(userId, page);
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistory() {
          Page<RecipeHistory> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeViewStatusService).getRecentUsers(userId, page);
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패한 레시피만 있을 때")
    class GivenOnlyFailedRecipes {

      private UUID userId;
      private Page<RecipeViewStatus> viewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        UUID recipeId = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeViewStatus(recipeId, userId)));

        doReturn(viewStatuses).when(recipeViewStatusService).getRecentUsers(userId, page);
        doReturn(List.of()).when(recipeService).getsNotFailed(anyList()); // 실패한 레시피는 제외
        doReturn(List.of()).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForFailedRecipes() {
          Page<RecipeHistory> result = recipeInfoService.getRecents(userId, page);

          assertThat(result.getContent()).isEmpty();
          verify(recipeViewStatusService).getRecentUsers(userId, page);
          verify(recipeService).getsNotFailed(anyList());
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
      private Page<RecipeViewStatus> viewStatuses;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        page = 0;
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeViewStatus(recipeId1, userId)));
        recipes = List.of(createMockRecipe(recipeId1, RecipeStatus.SUCCESS));
        youtubeMetas = List.of(createMockRecipeYoutubeMeta(UUID.randomUUID(), "한식 요리", recipeId1));

        doReturn(viewStatuses)
            .when(recipeViewStatusService)
            .getCategories(userId, recipeCategoryId, page);
        doReturn(recipes).when(recipeService).getsNotFailed(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 히스토리를 조회한다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 해당 카테고리의 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnCategorizedRecipeHistories() {
          Page<RecipeHistory> result =
              recipeInfoService.getCategorized(userId, recipeCategoryId, page);

          assertThat(result.getContent()).hasSize(1);
          assertThat(result.getContent().get(0).getRecipe()).isNotNull();
          assertThat(result.getContent().get(0).getRecipeViewStatus()).isNotNull();
          assertThat(result.getContent().get(0).getYoutubeMeta()).isNotNull();
          verify(recipeViewStatusService).getCategories(userId, recipeCategoryId, page);
          verify(recipeService).getsNotFailed(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 해당 카테고리에 레시피가 없을 때")
    class GivenCategoryWithNoRecipes {

      private UUID userId;
      private UUID recipeCategoryId;
      private Page<RecipeViewStatus> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses)
            .when(recipeViewStatusService)
            .getCategories(userId, recipeCategoryId, page);
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 히스토리를 조회한다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForCategory() {
          Page<RecipeHistory> result =
              recipeInfoService.getCategorized(userId, recipeCategoryId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeViewStatusService).getCategories(userId, recipeCategoryId, page);
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
      private Page<RecipeViewStatus> viewStatuses;
      private List<Recipe> recipes;
      private List<RecipeYoutubeMeta> youtubeMetas;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = new PageImpl<>(List.of(createMockRecipeViewStatus(recipeId1, userId)));
        recipes = List.of(createMockRecipe(recipeId1, RecipeStatus.SUCCESS));
        youtubeMetas = List.of(createMockRecipeYoutubeMeta(UUID.randomUUID(), "미분류 요리", recipeId1));

        doReturn(viewStatuses).when(recipeViewStatusService).getUnCategories(userId, page);
        doReturn(recipes).when(recipeService).getsNotFailed(anyList());
        doReturn(youtubeMetas).when(recipeYoutubeMetaService).getByRecipes(anyList());
      }

      @Nested
      @DisplayName("When - 미분류 레시피 히스토리를 조회한다면")
      class WhenFindingUnCategorized {

        @Test
        @DisplayName("Then - 미분류 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnUnCategorizedRecipeHistories() {
          Page<RecipeHistory> result = recipeInfoService.getUnCategorized(userId, page);

          assertThat(result.getContent()).hasSize(1);
          assertThat(result.getContent().get(0).getRecipe()).isNotNull();
          assertThat(result.getContent().get(0).getRecipeViewStatus()).isNotNull();
          assertThat(result.getContent().get(0).getYoutubeMeta()).isNotNull();
          verify(recipeViewStatusService).getUnCategories(userId, page);
          verify(recipeService).getsNotFailed(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피가 없을 때")
    class GivenNoUnCategorizedRecipes {

      private UUID userId;
      private Page<RecipeViewStatus> emptyViewStatuses;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        emptyViewStatuses = new PageImpl<>(List.of());

        doReturn(emptyViewStatuses).when(recipeViewStatusService).getUnCategories(userId, page);
      }

      @Nested
      @DisplayName("When - 미분류 레시피 히스토리를 조회한다면")
      class WhenFindingUnCategorized {

        @Test
        @DisplayName("Then - 빈 히스토리 목록을 반환해야 한다")
        void thenShouldReturnEmptyHistoryForUnCategorized() {
          Page<RecipeHistory> result = recipeInfoService.getUnCategorized(userId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeViewStatusService).getUnCategories(userId, page);
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 개수 조회")
  class FindCategories {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private List<RecipeCategory> categories;
      private List<RecipeViewStatusCount> counts;

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
                createMockRecipeViewStatusCount(categoryId1, 5),
                createMockRecipeViewStatusCount(categoryId2, 3));

        doReturn(categories).when(recipeCategoryService).getUsers(userId);
        doReturn(counts).when(recipeViewStatusService).countByCategories(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리별 레시피 개수 목록을 반환해야 한다")
        void thenShouldReturnCategoriesWithCount() {
          List<CountRecipeCategory> result = recipeInfoService.getCategories(userId);

          assertThat(result).hasSize(2);
          assertThat(result).allMatch(category -> category.getCategory() != null);
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeViewStatusService).countByCategories(anyList());
        }
      }
    }

    @Nested
    @DisplayName("Given - 카테고리에 레시피가 없을 때")
    class GivenCategoriesWithNoRecipes {

      private UUID userId;
      private List<RecipeCategory> categories;
      private List<RecipeViewStatusCount> counts;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        UUID categoryId1 = UUID.randomUUID();

        categories = List.of(createMockRecipeCategory(categoryId1, "빈 카테고리"));
        counts = List.of();

        doReturn(categories).when(recipeCategoryService).getUsers(userId);
        doReturn(counts).when(recipeViewStatusService).countByCategories(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리 개수가 0으로 반환되어야 한다")
        void thenShouldReturnCategoriesWithZeroCount() {
          List<CountRecipeCategory> result = recipeInfoService.getCategories(userId);

          assertThat(result).hasSize(1);
          assertThat(result.get(0).getCategory()).isNotNull();
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeViewStatusService).countByCategories(anyList());
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

          verify(recipeViewStatusService).deleteCategories(categoryId);
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
        doReturn(List.of()).when(recipeViewStatusService).countByCategories(List.of());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 빈 카테고리 목록을 반환해야 한다")
        void thenShouldReturnEmptyCategories() {
          List<CountRecipeCategory> result = recipeInfoService.getCategories(userId);

          assertThat(result).isEmpty();
          verify(recipeCategoryService).getUsers(userId);
          verify(recipeViewStatusService).countByCategories(List.of());
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
  private RecipeViewStatus createMockRecipeViewStatus(UUID recipeId, UUID userId) {
    RecipeViewStatus viewStatus = mock(RecipeViewStatus.class);
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

  private RecipeViewStatusCount createMockRecipeViewStatusCount(UUID categoryId, int count) {
    RecipeViewStatusCount statusCount = mock(RecipeViewStatusCount.class);
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
        300);
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

    RecipeViewStatus viewStatus = createMockRecipeViewStatus(recipeId, userId);
    doReturn(viewStatus).when(recipeViewStatusService).get(userId, recipeId);
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
}
