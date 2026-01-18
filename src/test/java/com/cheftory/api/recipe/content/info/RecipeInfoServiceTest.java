package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CountIdCursor;
import com.cheftory.api._common.cursor.CountIdCursorCodec;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import com.cheftory.api.recipe.dto.RecipeSort;
import com.cheftory.api.recipe.util.RecipePageRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeInfoService")
class RecipeInfoServiceTest {

  private RecipeInfoService service;
  private RecipeInfoRepository recipeInfoRepository;
  private Clock clock;
  private I18nTranslator i18nTranslator;
  private CountIdCursorCodec countIdCursorCodec;

  @BeforeEach
  void setUp() {
    recipeInfoRepository = mock(RecipeInfoRepository.class);
    clock = mock(Clock.class);
    i18nTranslator = mock(I18nTranslator.class);
    countIdCursorCodec = mock(CountIdCursorCodec.class);
    service =
        new RecipeInfoService(recipeInfoRepository, clock, i18nTranslator, countIdCursorCodec);
  }

  @Nested
  @DisplayName("block(recipeId)")
  class BlockRecipeInfo {

    private UUID recipeId;
    private RecipeInfo recipeInfo;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipeInfo = mock(RecipeInfo.class);
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(java.util.Optional.of(recipeInfo));
      }

      @Nested
      @DisplayName("When - 레시피 차단 요청을 하면")
      class WhenBlockingRecipeInfo {

        @Test
        @DisplayName("Then - 레시피가 BLOCKED 상태로 변경되고 저장된다")
        void thenMarkBlockedAndSave() {
          service.block(recipeId);

          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfo).block(clock);
          verify(recipeInfoRepository).save(recipeInfo);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeInfoNotExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(java.util.Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 차단 요청을 하면")
      class WhenBlockingRecipeInfo {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.block(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("findSuccess(recipeId)")
  class FindSuccess {

    private UUID recipeId;
    private RecipeInfo recipeInfo;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipeInfo = mock(RecipeInfo.class);
    }

    @Nested
    @DisplayName("Given - 성공 상태의 레시피가 존재할 때")
    class GivenSuccessRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.of(recipeInfo));
        when(recipeInfo.isFailed()).thenReturn(false);
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - 레시피가 반환되고 조회수가 증가한다")
        void thenReturnRecipeAndIncreaseCount() {
          RecipeInfo result = service.getSuccess(recipeId);

          assertThat(result).isEqualTo(recipeInfo);
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository).increaseCount(recipeId);
          verify(recipeInfo).isFailed();
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패 상태의 레시피가 존재할 때")
    class GivenFailedRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.of(recipeInfo));
        when(recipeInfo.isFailed()).thenReturn(true);
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - RECIPE_FAILED 예외가 발생한다")
        void thenThrowsRecipeFailedException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.getSuccess(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_FAILED.getErrorCode());
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository, never()).increaseCount(any());
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeInfoNotExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.getSuccess(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository, never()).increaseCount(any());
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
    class GivenValidRecipeInfoExists {

      private RecipeInfo validRecipeInfo;
      private RecipeInfo failedRecipeInfo;

      @BeforeEach
      void setUp() {
        validRecipeInfo = mock(RecipeInfo.class);
        failedRecipeInfo = mock(RecipeInfo.class);
        when(validRecipeInfo.isFailed()).thenReturn(false);
        when(failedRecipeInfo.isFailed()).thenReturn(true);
        when(recipeInfoRepository.findAllByIdIn(recipeIds))
            .thenReturn(List.of(validRecipeInfo, failedRecipeInfo));
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - 유효한 레시피가 반환된다")
        void thenReturnValidRecipe() {
          RecipeInfo result = service.getNotFailed(recipeIds);

          assertThat(result).isEqualTo(validRecipeInfo);
          verify(recipeInfoRepository).findAllByIdIn(recipeIds);
        }
      }
    }

    @Nested
    @DisplayName("Given - 모든 레시피가 실패 상태일 때")
    class GivenAllRecipesFailed {

      @BeforeEach
      void setUp() {
        RecipeInfo failedRecipeInfo1 = mock(RecipeInfo.class);
        RecipeInfo failedRecipeInfo2 = mock(RecipeInfo.class);
        when(failedRecipeInfo1.isFailed()).thenReturn(true);
        when(failedRecipeInfo2.isFailed()).thenReturn(true);
        when(recipeInfoRepository.findAllByIdIn(recipeIds))
            .thenReturn(List.of(failedRecipeInfo1, failedRecipeInfo2));
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - RECIPE_FAILED 예외가 발생한다")
        void thenThrowsRecipeFailedException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.getNotFailed(recipeIds));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_FAILED.getErrorCode());
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenNoRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findAllByIdIn(recipeIds)).thenReturn(List.of());
      }

      @Nested
      @DisplayName("When - 레시피 조회 요청을 하면")
      class WhenFindingRecipeInfo {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.getNotFailed(recipeIds));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
        }
      }
    }
  }

  @Nested
  @DisplayName("create()")
  class Create {

    @Nested
    @DisplayName("Given - 레시피 생성이 가능할 때")
    class GivenCanCreateRecipeInfo {

      @Nested
      @DisplayName("When - 레시피 생성 요청을 하면")
      class WhenCreatingRecipeInfo {

        @Test
        @DisplayName("Then - 레시피가 생성되고 ID가 반환된다")
        void thenCreateRecipeAndReturnId() {
          UUID expectedId = UUID.randomUUID();
          RecipeInfo recipeInfo = mock(RecipeInfo.class);
          when(recipeInfo.getId()).thenReturn(expectedId);

          when(recipeInfoRepository.save(any(RecipeInfo.class))).thenReturn(recipeInfo);

          RecipeInfo result = service.create();

          verify(recipeInfoRepository).save(any(RecipeInfo.class));
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
    @DisplayName("Given - 유효한 레시피들(IN_PROGRESS, SUCCESS)이 존재할 때")
    class GivenValidRecipesExist {

      private List<RecipeInfo> validRecipeInfos;

      @BeforeEach
      void setUp() {
        validRecipeInfos = List.of(mock(RecipeInfo.class), mock(RecipeInfo.class));
        when(recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS)))
            .thenReturn(validRecipeInfos);
      }

      @Nested
      @DisplayName("When - 레시피 목록 조회 요청을 하면")
      class WhenFindingRecipes {

        @Test
        @DisplayName("Then - 유효한 레시피 목록이 반환된다")
        void thenReturnValidRecipes() {
          List<RecipeInfo> result = service.getValidRecipes(recipeIds);

          assertThat(result).isEqualTo(validRecipeInfos);
          verify(recipeInfoRepository)
              .findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
        }
      }
    }

    @Nested
    @DisplayName("Given - BLOCKED 상태의 레시피가 포함되어 있을 때")
    class GivenBlockedRecipesIncluded {

      @BeforeEach
      void setUp() {
        List<RecipeInfo> validRecipeInfos = List.of(mock(RecipeInfo.class));
        when(recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS)))
            .thenReturn(validRecipeInfos);
      }

      @Nested
      @DisplayName("When - 레시피 목록 조회 요청을 하면")
      class WhenFindingRecipes {

        @Test
        @DisplayName("Then - BLOCKED 상태는 제외되고 유효한 레시피만 반환된다")
        void thenReturnOnlyValidRecipesExcludingBlocked() {
          List<RecipeInfo> result = service.getValidRecipes(recipeIds);

          assertThat(result).hasSize(1);
          verify(recipeInfoRepository)
              .findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
        }
      }
    }
  }

  @Nested
  @DisplayName("getPopulars(page, query)")
  class GetPopulars {

    private Integer page;

    @BeforeEach
    void init() {
      page = 0;
    }

    @Nested
    @DisplayName("Given - Query가 ALL일 때")
    class GivenQueryIsAll {

      private Page<RecipeInfo> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        List<RecipeInfo> recipeInfos = List.of(mock(RecipeInfo.class), mock(RecipeInfo.class));
        expectedPage = new PageImpl<>(recipeInfos);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 인기 레시피 페이지 조회 요청을 하면")
      class WhenFindingPopularRecipes {

        @Test
        @DisplayName("Then - 모든 레시피 페이지가 반환된다")
        void thenReturnAllRecipePage() {
          Page<RecipeInfo> result = service.getPopulars(page, RecipeInfoVideoQuery.ALL);

          assertThat(result).isEqualTo(expectedPage);
          verify(recipeInfoRepository)
              .findByRecipeStatus(eq(RecipeStatus.SUCCESS), any(Pageable.class));
        }
      }
    }

    @Nested
    @DisplayName("Given - Query가 NORMAL일 때")
    class GivenQueryIsNormal {

      private Page<RecipeInfo> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        List<RecipeInfo> recipeInfos = List.of(mock(RecipeInfo.class), mock(RecipeInfo.class));
        expectedPage = new PageImpl<>(recipeInfos);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(recipeInfoRepository.findRecipes(RecipeStatus.SUCCESS, pageable, "NORMAL"))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 인기 레시피 페이지 조회 요청을 하면")
      class WhenFindingPopularRecipes {

        @Test
        @DisplayName("Then - NORMAL 레시피 페이지가 반환된다")
        void thenReturnNormalRecipePage() {
          Page<RecipeInfo> result = service.getPopulars(page, RecipeInfoVideoQuery.NORMAL);

          assertThat(result).isEqualTo(expectedPage);
          verify(recipeInfoRepository)
              .findRecipes(eq(RecipeStatus.SUCCESS), any(Pageable.class), eq("NORMAL"));
        }
      }
    }

    @Nested
    @DisplayName("Given - Query가 SHORTS일 때")
    class GivenQueryIsShorts {

      private Page<RecipeInfo> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        List<RecipeInfo> recipeInfos = List.of(mock(RecipeInfo.class), mock(RecipeInfo.class));
        expectedPage = new PageImpl<>(recipeInfos);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(recipeInfoRepository.findRecipes(RecipeStatus.SUCCESS, pageable, "SHORTS"))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 인기 레시피 페이지 조회 요청을 하면")
      class WhenFindingPopularRecipes {

        @Test
        @DisplayName("Then - SHORTS 레시피 페이지가 반환된다")
        void thenReturnShortsRecipePage() {
          Page<RecipeInfo> result = service.getPopulars(page, RecipeInfoVideoQuery.SHORTS);

          assertThat(result).isEqualTo(expectedPage);
          verify(recipeInfoRepository)
              .findRecipes(eq(RecipeStatus.SUCCESS), any(Pageable.class), eq("SHORTS"));
        }
      }
    }
  }

  @Nested
  @DisplayName("success(recipeId)")
  class Success {

    private UUID recipeId;
    private RecipeInfo recipeInfo;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipeInfo = mock(RecipeInfo.class);
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.of(recipeInfo));
        when(recipeInfoRepository.save(recipeInfo)).thenReturn(recipeInfo);
      }

      @Nested
      @DisplayName("When - 레시피 성공 처리 요청을 하면")
      class WhenMarkingSuccess {

        @Test
        @DisplayName("Then - 레시피가 성공 상태로 변경되고 저장된다")
        void thenMarkSuccessAndSave() {
          RecipeInfo result = service.success(recipeId);

          assertThat(result).isEqualTo(recipeInfo);
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfo).success(clock);
          verify(recipeInfoRepository).save(recipeInfo);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeInfoNotExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 성공 처리 요청을 하면")
      class WhenMarkingSuccess {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.success(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("failed(recipeId)")
  class Failed {

    private UUID recipeId;
    private RecipeInfo recipeInfo;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
      recipeInfo = mock(RecipeInfo.class);
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.of(recipeInfo));
        when(recipeInfoRepository.save(recipeInfo)).thenReturn(recipeInfo);
      }

      @Nested
      @DisplayName("When - 레시피 실패 처리 요청을 하면")
      class WhenMarkingFailed {

        @Test
        @DisplayName("Then - 레시피가 실패 상태로 변경되고 저장된다")
        void thenMarkFailedAndSave() {
          RecipeInfo result = service.failed(recipeId);

          assertThat(result).isEqualTo(recipeInfo);
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfo).failed(clock);
          verify(recipeInfoRepository).save(recipeInfo);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeInfoNotExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.findById(recipeId)).thenReturn(Optional.empty());
      }

      @Nested
      @DisplayName("When - 레시피 실패 처리 요청을 하면")
      class WhenMarkingFailed {

        @Test
        @DisplayName("Then - RECIPE_NOT_FOUND 예외가 발생한다")
        void thenThrowsRecipeNotFoundException() {
          RecipeInfoException ex =
              assertThrows(RecipeInfoException.class, () -> service.failed(recipeId));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
          verify(recipeInfoRepository).findById(recipeId);
          verify(recipeInfoRepository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("exists(recipeId)")
  class Exists {

    private UUID recipeId;

    @BeforeEach
    void init() {
      recipeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Given - 레시피가 존재할 때")
    class GivenRecipeInfoExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.existsById(recipeId)).thenReturn(true);
      }

      @Nested
      @DisplayName("When - 레시피 존재 여부를 확인하면")
      class WhenCheckingExistence {

        @Test
        @DisplayName("Then - true가 반환된다")
        void thenReturnTrue() {
          boolean result = service.exists(recipeId);

          assertThat(result).isTrue();
          verify(recipeInfoRepository).existsById(recipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 레시피가 존재하지 않을 때")
    class GivenRecipeInfoNotExists {

      @BeforeEach
      void setUp() {
        when(recipeInfoRepository.existsById(recipeId)).thenReturn(false);
      }

      @Nested
      @DisplayName("When - 레시피 존재 여부를 확인하면")
      class WhenCheckingExistence {

        @Test
        @DisplayName("Then - false가 반환된다")
        void thenReturnFalse() {
          boolean result = service.exists(recipeId);

          assertThat(result).isFalse();
          verify(recipeInfoRepository).existsById(recipeId);
        }
      }
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Scenarios")
  class EdgeCasesAndErrorScenarios {

    @Nested
    @DisplayName("Given - findNotFailed에서 multiple valid recipes warning 상황")
    class GivenMultipleValidRecipesWarning {

      private List<UUID> recipeIds;
      private RecipeInfo validRecipeInfo1;
      private RecipeInfo validRecipeInfo2;

      @BeforeEach
      void setUp() {
        recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        validRecipeInfo1 = mock(RecipeInfo.class);
        validRecipeInfo2 = mock(RecipeInfo.class);

        when(validRecipeInfo1.isFailed()).thenReturn(false);
        when(validRecipeInfo2.isFailed()).thenReturn(false);
        when(recipeInfoRepository.findAllByIdIn(recipeIds))
            .thenReturn(List.of(validRecipeInfo1, validRecipeInfo2));
      }

      @Nested
      @DisplayName("When - 여러 유효한 레시피가 조회될 때")
      class WhenMultipleValidRecipesFound {

        @Test
        @DisplayName("Then - 첫 번째 레시피가 반환되고 경고 로그가 출력된다")
        void thenReturnFirstRecipeAndLogWarning() {
          RecipeInfo result = service.getNotFailed(recipeIds);

          assertThat(result).isEqualTo(validRecipeInfo1);
          verify(recipeInfoRepository).findAllByIdIn(recipeIds);
        }
      }
    }
  }

  @Nested
  @DisplayName("getCuisines(type, page)")
  class GetCuisines {

    private Integer page;
    private RecipeCuisineType cuisineType;
    private String cuisineName;

    @BeforeEach
    void init() {
      page = 0;
      cuisineType = RecipeCuisineType.KOREAN;
      cuisineName = "Korean";
    }

    @Nested
    @DisplayName("Given - 성공한 특정 음식 종류 레시피들이 존재할 때")
    class GivenCuisineRecipesExist {

      private Page<RecipeInfo> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        List<RecipeInfo> recipeInfos = List.of(mock(RecipeInfo.class), mock(RecipeInfo.class));
        expectedPage = new PageImpl<>(recipeInfos);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(i18nTranslator.translate(cuisineType.messageKey())).thenReturn(cuisineName);
        when(recipeInfoRepository.findCuisineRecipes(cuisineName, RecipeStatus.SUCCESS, pageable))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 특정 음식 종류 레시피 페이지 조회 요청을 하면")
      class WhenFindingCuisineRecipes {

        @Test
        @DisplayName("Then - 해당 음식 종류 레시피 페이지가 반환된다")
        void thenReturnCuisineRecipePage() {
          Page<RecipeInfo> result = service.getCuisines(cuisineType, page);

          assertThat(result).isEqualTo(expectedPage);
          verify(recipeInfoRepository)
              .findCuisineRecipes(eq(cuisineName), eq(RecipeStatus.SUCCESS), any(Pageable.class));
        }
      }
    }

    @Nested
    @DisplayName("Given - 중식 레시피가 존재할 때")
    class GivenChineseRecipesExist {

      private Page<RecipeInfo> expectedPage;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        cuisineType = RecipeCuisineType.CHINESE;
        cuisineName = "Chinese";
        List<RecipeInfo> recipeInfos = List.of(mock(RecipeInfo.class));
        expectedPage = new PageImpl<>(recipeInfos);
        pageable = RecipePageRequest.create(page, RecipeSort.COUNT_DESC);

        when(i18nTranslator.translate(cuisineType.messageKey())).thenReturn(cuisineName);
        when(recipeInfoRepository.findCuisineRecipes(cuisineName, RecipeStatus.SUCCESS, pageable))
            .thenReturn(expectedPage);
      }

      @Nested
      @DisplayName("When - 중식 레시피 페이지 조회 요청을 하면")
      class WhenFindingChineseRecipes {

        @Test
        @DisplayName("Then - 중식 레시피 페이지가 반환된다")
        void thenReturnChineseRecipePage() {
          Page<RecipeInfo> result = service.getCuisines(cuisineType, page);

          assertThat(result).isEqualTo(expectedPage);
          assertThat(result.getContent()).hasSize(1);
          verify(recipeInfoRepository)
              .findCuisineRecipes(eq(cuisineName), eq(RecipeStatus.SUCCESS), any(Pageable.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("커서 기반 조회")
  class CursorQueries {

    @Test
    @DisplayName("커서가 없으면 인기 레시피 첫 페이지를 조회한다")
    void shouldGetPopularsFirstPageWithCursor() {
      RecipeInfo recipeInfo = mock(RecipeInfo.class);
      UUID recipeId = UUID.randomUUID();

      doReturn(recipeId).when(recipeInfo).getId();
      doReturn(10).when(recipeInfo).getViewCount();

      List<RecipeInfo> rows = Collections.nCopies(22, recipeInfo);
      doReturn(rows)
          .when(recipeInfoRepository)
          .findPopularFirst(eq(RecipeStatus.SUCCESS), any(Pageable.class));
      doReturn("next-cursor").when(countIdCursorCodec).encode(any(CountIdCursor.class));

      CursorPage<RecipeInfo> result = service.getPopulars(null, RecipeInfoVideoQuery.ALL);

      assertThat(result.items()).hasSize(21);
      assertThat(result.nextCursor()).isEqualTo("next-cursor");
      verify(recipeInfoRepository).findPopularFirst(eq(RecipeStatus.SUCCESS), any(Pageable.class));
    }

    @Test
    @DisplayName("커서가 있으면 인기 레시피 keyset을 조회한다")
    void shouldGetPopularsKeysetWithCursor() {
      RecipeInfo recipeInfo = mock(RecipeInfo.class);

      doReturn(new CountIdCursor(10L, UUID.randomUUID())).when(countIdCursorCodec).decode("cursor");
      doReturn(List.of(recipeInfo))
          .when(recipeInfoRepository)
          .findPopularKeyset(
              eq(RecipeStatus.SUCCESS), eq(10L), any(UUID.class), any(Pageable.class));

      CursorPage<RecipeInfo> result = service.getPopulars("cursor", RecipeInfoVideoQuery.ALL);

      assertThat(result.items()).hasSize(1);
      verify(recipeInfoRepository)
          .findPopularKeyset(
              eq(RecipeStatus.SUCCESS), eq(10L), any(UUID.class), any(Pageable.class));
    }

    @Test
    @DisplayName("커서 기반 cuisine 조회는 keyset을 사용한다")
    void shouldGetCuisineKeysetWithCursor() {
      RecipeInfo recipeInfo = mock(RecipeInfo.class);

      doReturn(new CountIdCursor(6L, UUID.randomUUID())).when(countIdCursorCodec).decode("cursor");
      doReturn("한식").when(i18nTranslator).translate(RecipeCuisineType.KOREAN.messageKey());
      doReturn(List.of(recipeInfo))
          .when(recipeInfoRepository)
          .findCuisineKeyset(
              eq("한식"), eq(RecipeStatus.SUCCESS), eq(6L), any(UUID.class), any(Pageable.class));

      CursorPage<RecipeInfo> result = service.getCuisines(RecipeCuisineType.KOREAN, "cursor");

      assertThat(result.items()).hasSize(1);
      verify(recipeInfoRepository)
          .findCuisineKeyset(
              eq("한식"), eq(RecipeStatus.SUCCESS), eq(6L), any(UUID.class), any(Pageable.class));
    }
  }
}
