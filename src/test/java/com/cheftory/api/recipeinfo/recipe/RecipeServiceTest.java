package com.cheftory.api.recipeinfo.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api.recipeinfo.model.RecipeSort;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipeinfo.recipe.exception.RecipeException;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeService")
class RecipeServiceTest {

  private RecipeService service;
  private RecipeRepository recipeRepository;

  @BeforeEach
  void setUp() {
    recipeRepository = mock(RecipeRepository.class);
    service = new RecipeService(recipeRepository);
  }

  @Nested
  @DisplayName("findSuccess(recipeId)")
  class FindSuccess {

    private UUID recipeId;
    private Recipe recipe;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipe = mock(Recipe.class);
    }

    @Nested
    @DisplayName("Given - 성공 상태의 레시피가 존재할 때")
    class GivenSuccessRecipeExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipe.isFailed()).thenReturn(false);
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - 레시피가 반환되고 조회수가 증가한다")
        void thenReturnRecipeAndIncreaseCount() {
          Recipe result = service.findSuccess(recipeId);

          assertThat(result).isEqualTo(recipe);
          verify(recipeRepository).findById(recipeId);
          verify(recipeRepository).increaseCount(recipeId);
          verify(recipe).isFailed();
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패 상태의 레시피가 존재할 때")
    class GivenFailedRecipeExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipe.isFailed()).thenReturn(true);
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - RECIPE_FAILED 예외가 발생한다")
        void thenThrowsRecipeFailedException() {
          RecipeException ex =
              assertThrows(RecipeException.class, () -> service.findSuccess(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_FAILED.getErrorCode());
          verify(recipeRepository).findById(recipeId);
          verify(recipeRepository, never()).increaseCount(any());
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeNotExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeException ex =
              assertThrows(RecipeException.class, () -> service.findSuccess(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_NOT_FOUND.getErrorCode());
          verify(recipeRepository).findById(recipeId);
          verify(recipeRepository, never()).increaseCount(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("findNotFailed(recipeIds)")
  class FindNotFailed {

    private List<UUID> recipeIds;

    @BeforeEach
    void init() {
      recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    @DisplayName("Given - 유효한 레시피가 하나 존재할 때")
    class GivenValidRecipeExists {

      private Recipe validRecipe;
      private Recipe failedRecipe;

      @BeforeEach
      void setUp() {
        validRecipe = mock(Recipe.class);
        failedRecipe = mock(Recipe.class);
        when(validRecipe.isFailed()).thenReturn(false);
        when(failedRecipe.isFailed()).thenReturn(true);
        when(recipeRepository.findAllByIdIn(recipeIds))
            .thenReturn(List.of(validRecipe, failedRecipe));
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - 유효한 레시피가 반환된다")
        void thenReturnValidRecipe() {
          Recipe result = service.findNotFailed(recipeIds);

          assertThat(result).isEqualTo(validRecipe);
          verify(recipeRepository).findAllByIdIn(recipeIds);
        }
      }
    }

    @Nested
    @DisplayName("Given - 모든 레시피가 실패 상태일 때")
    class GivenAllRecipesFailed {

      @BeforeEach
      void setUp() {
        Recipe failedRecipe1 = mock(Recipe.class);
        Recipe failedRecipe2 = mock(Recipe.class);
        when(failedRecipe1.isFailed()).thenReturn(true);
        when(failedRecipe2.isFailed()).thenReturn(true);
        when(recipeRepository.findAllByIdIn(recipeIds))
            .thenReturn(List.of(failedRecipe1, failedRecipe2));
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - RECIPE_FAILED 예외가 발생한다")
        void thenThrowsRecipeFailedException() {
          RecipeException ex =
              assertThrows(RecipeException.class, () -> service.findNotFailed(recipeIds));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_FAILED.getErrorCode());
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenNoRecipeExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findAllByIdIn(recipeIds)).thenReturn(List.of());
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipe {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeException ex =
              assertThrows(RecipeException.class, () -> service.findNotFailed(recipeIds));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_NOT_FOUND.getErrorCode());
        }
      }
    }
  }

  @Nested
  @DisplayName("create()")
  class Create {

    @Nested
    @DisplayName("Given - 레시피 생성이 가능할 때")
    class GivenCanCreateRecipe {

      @Nested
      @DisplayName("When - 레시피 생성 요청을 하면")
      class WhenCreatingRecipe {

        @Test
        @DisplayName("Then - 레시피가 생성되고 ID가 반환된다")
        void thenCreateRecipeAndReturnId() {
          UUID expectedId = UUID.randomUUID();
          Recipe recipe = mock(Recipe.class);
          when(recipe.getId()).thenReturn(expectedId);

          // Recipe.create()는 static 메서드라 모킹하기 어려우므로 실제 생성된다고 가정
          when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

          UUID result = service.create();

          verify(recipeRepository).save(any(Recipe.class));
          // 실제로는 Recipe.create().getId()가 호출되므로 결과가 null이 아님을 확인
          assertThat(result).isNotNull();
        }
      }
    }
  }

  @Nested
  @DisplayName("findsNotFailed(recipeIds)")
  class FindsNotFailed {

    private List<UUID> recipeIds;

    @BeforeEach
    void init() {
      recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    @DisplayName("Given - 실패하지 않은 레시피들이 존재할 때")
    class GivenNotFailedRecipesExist {

      private List<Recipe> notFailedRecipes;

      @BeforeEach
      void setUp() {
        notFailedRecipes = List.of(mock(Recipe.class), mock(Recipe.class));
        when(recipeRepository.findRecipesByIdInAndRecipeStatusNot(recipeIds, RecipeStatus.FAILED))
            .thenReturn(notFailedRecipes);
      }

      @Nested
      @DisplayName("When - 레시피 목록 조회 요청을 하면")
      class WhenFindingRecipes {

        @Test
        @DisplayName("Then - 실패하지 않은 레시피 목록이 반환된다")
        void thenReturnNotFailedRecipes() {
          List<Recipe> result = service.findsNotFailed(recipeIds);

          assertThat(result).isEqualTo(notFailedRecipes);
          verify(recipeRepository)
              .findRecipesByIdInAndRecipeStatusNot(recipeIds, RecipeStatus.FAILED);
        }
      }
    }
  }

  @Nested
  @DisplayName("findsSuccess(page)")
  class FindsSuccess {

    private Integer page;

    @BeforeEach
    void init() {
      page = 0;
    }

    @Nested
    @DisplayName("Given - 성공 상태의 레시피들이 존재할 때")
    class GivenSuccessRecipesExist {

      private Page<Recipe> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        List<Recipe> recipes = List.of(mock(Recipe.class), mock(Recipe.class));
        expectedPage = new PageImpl<>(recipes);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 성공 레시피 페이지 조회 요청을 하면")
      class WhenFindingSuccessRecipes {

        @Test
        @DisplayName("Then - 성공 레시피 페이지가 반환된다")
        void thenReturnSuccessRecipePage() {
          Page<Recipe> result = service.findsSuccess(page);

          assertThat(result).isEqualTo(expectedPage);
          verify(recipeRepository)
              .findByRecipeStatus(eq(RecipeStatus.SUCCESS), any(Pageable.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("success(recipeId)")
  class Success {

    private UUID recipeId;
    private Recipe recipe;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipe = mock(Recipe.class);
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
      }

      @Nested
      @DisplayName("When - 레시피 성공 처리 요청을 하면")
      class WhenMarkingSuccess {

        @Test
        @DisplayName("Then - 레시피가 성공 상태로 변경되고 저장된다")
        void thenMarkSuccessAndSave() {
          Recipe result = service.success(recipeId);

          assertThat(result).isEqualTo(recipe);
          verify(recipeRepository).findById(recipeId);
          verify(recipe).success();
          verify(recipeRepository).save(recipe);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeNotExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 성공 처리 요청을 하면")
      class WhenMarkingSuccess {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeException ex = assertThrows(RecipeException.class, () -> service.success(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_NOT_FOUND.getErrorCode());
          verify(recipeRepository).findById(recipeId);
          verify(recipeRepository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("failed(recipeId)")
  class Failed {

    private UUID recipeId;
    private Recipe recipe;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipe = mock(Recipe.class);
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
      }

      @Nested
      @DisplayName("When - 레시피 실패 처리 요청을 하면")
      class WhenMarkingFailed {

        @Test
        @DisplayName("Then - 레시피가 실패 상태로 변경되고 저장된다")
        void thenMarkFailedAndSave() {
          Recipe result = service.failed(recipeId);

          assertThat(result).isEqualTo(recipe);
          verify(recipeRepository).findById(recipeId);
          verify(recipe).failed();
          verify(recipeRepository).save(recipe);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeNotExists {

      @BeforeEach
      void setUp() {
        when(recipeRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 실패 처리 요청을 하면")
      class WhenMarkingFailed {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeException ex = assertThrows(RecipeException.class, () -> service.failed(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeErrorCode.RECIPE_NOT_FOUND.getErrorCode());
          verify(recipeRepository).findById(recipeId);
          verify(recipeRepository, never()).save(any());
        }
      }
    }
  }
}
