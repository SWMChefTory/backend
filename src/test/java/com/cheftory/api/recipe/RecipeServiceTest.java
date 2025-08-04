package com.cheftory.api.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.category.RecipeCategory;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.model.CountRecipeCategory;
import com.cheftory.api.recipe.model.RecipeHistoryOverview;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.util.YoutubeUrlNormalizer;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusCount;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeService Tests")
public class RecipeServiceTest {

  private RecipeRepository recipeRepository;
  private RecipeViewStatusService recipeViewStatusService;
  private RecipeCategoryService recipeCategoryService;
  private RecipeService recipeService;
  private VideoInfoClient videoInfoClient;
  private RecipeStepService recipeStepService;
  private RecipeIngredientsService recipeIngredientsService;
  private YoutubeUrlNormalizer youtubeUrlNormalizer;
  private AsyncRecipeCreationService asyncRecipeCreationService;

  @BeforeEach
  void setUp() {
    recipeRepository = mock(RecipeRepository.class);
    recipeViewStatusService = mock(RecipeViewStatusService.class);
    recipeCategoryService = mock(RecipeCategoryService.class);
    videoInfoClient = mock(VideoInfoClient.class);
    youtubeUrlNormalizer = mock(YoutubeUrlNormalizer.class);
    recipeStepService = mock(RecipeStepService.class);
    recipeIngredientsService = mock(RecipeIngredientsService.class);
    asyncRecipeCreationService = mock(AsyncRecipeCreationService.class);
    recipeService = new RecipeService(
        videoInfoClient,
        asyncRecipeCreationService,
        recipeRepository,
        recipeStepService,
        recipeIngredientsService,
        youtubeUrlNormalizer,
        recipeViewStatusService,
        recipeCategoryService
    );
  }

  @Nested
  @DisplayName("최근 레시피 히스토리 조회")
  class FindRecents {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private List<RecipeViewStatus> viewStatuses;
      private List<Recipe> recipes;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();

        viewStatuses = List.of(
            createMockRecipeViewStatus(recipeId1),
            createMockRecipeViewStatus(recipeId2)
        );

        recipes = List.of(
            createMockRecipe(recipeId1, "김치찌개"),
            createMockRecipe(recipeId2, "된장찌개")
        );

        doReturn(viewStatuses).when(recipeViewStatusService).findRecentUsers(userId);
        doReturn(recipes).when(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
      }

      @Nested
      @DisplayName("When - 최근 레시피 히스토리를 조회한다면")
      class WhenFindingRecents {

        @Test
        @DisplayName("Then - 최근 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnRecentRecipeHistories() {
          List<RecipeHistoryOverview> result = recipeService.findRecents(userId);

          assertThat(result).hasSize(2);
          assertThat(result).allMatch(overview ->
              overview.getRecipeOverview() != null &&
                  overview.getRecipeViewStatusInfo() != null
          );
          verify(recipeViewStatusService).findRecentUsers(userId);
          verify(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
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
      private List<RecipeViewStatus> viewStatuses;
      private List<Recipe> recipes;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = List.of(createMockRecipeViewStatus(recipeId1));
        recipes = List.of(createMockRecipe(recipeId1, "한식 요리"));

        doReturn(viewStatuses).when(recipeViewStatusService).findCategories(userId, recipeCategoryId);
        doReturn(recipes).when(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 히스토리를 조회한다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 해당 카테고리의 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnCategorizedRecipeHistories() {
          List<RecipeHistoryOverview> result = recipeService.findCategorized(userId, recipeCategoryId);

          assertThat(result).hasSize(1);
          assertThat(result.get(0).getRecipeOverview()).isNotNull();
          assertThat(result.get(0).getRecipeViewStatusInfo()).isNotNull();
          verify(recipeViewStatusService).findCategories(userId, recipeCategoryId);
          verify(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
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
      private List<RecipeViewStatus> viewStatuses;
      private List<Recipe> recipes;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        UUID recipeId1 = UUID.randomUUID();

        viewStatuses = List.of(createMockRecipeViewStatus(recipeId1));
        recipes = List.of(createMockRecipe(recipeId1, "미분류 요리"));

        doReturn(viewStatuses).when(recipeViewStatusService).findUnCategories(userId);
        doReturn(recipes).when(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
      }

      @Nested
      @DisplayName("When - 미분류 레시피 히스토리를 조회한다면")
      class WhenFindingUnCategorized {

        @Test
        @DisplayName("Then - 미분류 레시피 히스토리 목록을 반환해야 한다")
        void thenShouldReturnUnCategorizedRecipeHistories() {
          List<RecipeHistoryOverview> result = recipeService.findUnCategorized(userId);

          assertThat(result).hasSize(1);
          assertThat(result.get(0).getRecipeOverview()).isNotNull();
          assertThat(result.get(0).getRecipeViewStatusInfo()).isNotNull();
          verify(recipeViewStatusService).findUnCategories(userId);
          verify(recipeRepository).findRecipesByIdInAndStatus(anyList(), eq(RecipeStatus.COMPLETED));
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

        categories = List.of(
            createMockRecipeCategory(categoryId1, "한식"),
            createMockRecipeCategory(categoryId2, "양식")
        );

        counts = List.of(
            createMockRecipeViewStatusCount(categoryId1, 5),
            createMockRecipeViewStatusCount(categoryId2, 3)
        );

        doReturn(categories).when(recipeCategoryService).findUsers(userId);
        doReturn(counts).when(recipeViewStatusService).countByCategories(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리별 레시피 개수 목록을 반환해야 한다")
        void thenShouldReturnCategoriesWithCount() {
          List<CountRecipeCategory> result = recipeService.findCategories(userId);

          assertThat(result).hasSize(2);
          assertThat(result).allMatch(category -> category.getCategory() != null);
          verify(recipeCategoryService).findUsers(userId);
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
        counts = List.of(); // 빈 목록

        doReturn(categories).when(recipeCategoryService).findUsers(userId);
        doReturn(counts).when(recipeViewStatusService).countByCategories(anyList());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenFindingCategories {

        @Test
        @DisplayName("Then - 카테고리 개수가 0으로 반환되어야 한다")
        void thenShouldReturnCategoriesWithZeroCount() {
          List<CountRecipeCategory> result = recipeService.findCategories(userId);

          assertThat(result).hasSize(1);
          assertThat(result.get(0).getCategory()).isNotNull();
          // CountRecipeCategory.of() 메서드가 기본값으로 처리할 것으로 예상
          verify(recipeCategoryService).findUsers(userId);
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
          recipeService.deleteCategory(categoryId);

          verify(recipeViewStatusService).deleteCategories(categoryId);
          verify(recipeCategoryService).delete(categoryId);
        }
      }
    }
  }

  // Helper methods for creating mock objects
  private RecipeViewStatus createMockRecipeViewStatus(UUID recipeId) {
    RecipeViewStatus viewStatus = mock(RecipeViewStatus.class);
    doReturn(recipeId).when(viewStatus).getRecipeId();
    return viewStatus;
  }

  private Recipe createMockRecipe(UUID recipeId, String title) {
    Recipe recipe = mock(Recipe.class);
    VideoInfo videoInfo = createMockVideoInfo(title);

    doReturn(recipeId).when(recipe).getId();
    doReturn(videoInfo).when(recipe).getVideoInfo();
    doReturn(RecipeStatus.COMPLETED).when(recipe).getStatus();
    doReturn("테스트 레시피 설명").when(recipe).getDescription();
    doReturn(10).when(recipe).getCount();
    doReturn(LocalDateTime.now()).when(recipe).getCreatedAt();

    return recipe;
  }

  private VideoInfo createMockVideoInfo(String title) {
    VideoInfo videoInfo = mock(VideoInfo.class);
    doReturn(URI.create("https://www.youtube.com/watch?v=test123")).when(videoInfo).getVideoUri();
    doReturn(title).when(videoInfo).getTitle();
    doReturn(URI.create("https://img.youtube.com/vi/test123/maxresdefault.jpg")).when(videoInfo).getThumbnailUrl();
    doReturn(300).when(videoInfo).getVideoSeconds();
    return videoInfo;
  }

  private RecipeCategory createMockRecipeCategory(UUID categoryId, String name) {
    RecipeCategory category = mock(RecipeCategory.class);
    doReturn(categoryId).when(category).getId();
    doReturn(name).when(category).getName();
    return category;
  }

  private RecipeViewStatusCount createMockRecipeViewStatusCount(UUID categoryId, int count) {
    RecipeViewStatusCount statusCount = mock(RecipeViewStatusCount.class);
    doReturn(categoryId).when(statusCount).getCategoryId();
    doReturn(count).when(statusCount).getCount();
    return statusCount;
  }
}