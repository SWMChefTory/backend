package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
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
        service = mock(RecipeInfoService.class, CALLS_REAL_METHODS);
        setField(service, "recipeInfoRepository", recipeInfoRepository);
        setField(service, "clock", clock);
        setField(service, "i18nTranslator", i18nTranslator);
        setField(service, "countIdCursorCodec", countIdCursorCodec);
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
                void thenMarkBlockedAndSave() throws RecipeInfoException {
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
                    RecipeInfoException ex = assertThrows(RecipeInfoException.class, () -> service.block(recipeId));

                    assertThat(ex.getError().getErrorCode())
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
                void thenReturnRecipeAndIncreaseCount() throws RecipeInfoException {
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

                    assertThat(ex.getError().getErrorCode())
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

                    assertThat(ex.getError().getErrorCode())
                            .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND.getErrorCode());
                    verify(recipeInfoRepository).findById(recipeId);
                    verify(recipeInfoRepository, never()).increaseCount(any());
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
                    RecipeInfo recipeInfo = RecipeInfo.create(clock);
                    setField(recipeInfo, "id", expectedId);

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
                void thenMarkSuccessAndSave() throws RecipeInfoException {
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
                    RecipeInfoException ex = assertThrows(RecipeInfoException.class, () -> service.success(recipeId));

                    assertThat(ex.getError().getErrorCode())
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
                void thenMarkFailedAndSave() throws RecipeInfoException {
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
                    RecipeInfoException ex = assertThrows(RecipeInfoException.class, () -> service.failed(recipeId));

                    assertThat(ex.getError().getErrorCode())
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
    @DisplayName("커서 기반 조회")
    class CursorQueries {

        @Test
        @DisplayName("커서가 없으면 인기 레시피 첫 페이지를 조회한다")
        void shouldGetPopularsFirstPageWithCursor() {
            RecipeInfo recipeInfo = RecipeInfo.create(clock);
            UUID recipeId = UUID.randomUUID();
            setField(recipeInfo, "id", recipeId);
            setField(recipeInfo, "viewCount", 10);

            List<RecipeInfo> rows = Collections.nCopies(21, recipeInfo);

            doReturn(rows).when(recipeInfoRepository).findPopularFirst(eq(RecipeStatus.SUCCESS), any(Pageable.class));

            doReturn("next-cursor").when(countIdCursorCodec).encode(any(CountIdCursor.class));

            CursorPage<RecipeInfo> result = service.getPopulars(null, RecipeInfoVideoQuery.ALL);

            assertThat(result.items()).hasSize(20);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");

            verify(recipeInfoRepository).findPopularFirst(eq(RecipeStatus.SUCCESS), any(Pageable.class));
        }

        @Test
        @DisplayName("커서가 있으면 인기 레시피 keyset을 조회한다")
        void shouldGetPopularsKeysetWithCursor() {
            RecipeInfo recipeInfo = mock(RecipeInfo.class);

            doReturn(new CountIdCursor(10L, UUID.randomUUID()))
                    .when(countIdCursorCodec)
                    .decode("cursor");
            doReturn(List.of(recipeInfo))
                    .when(recipeInfoRepository)
                    .findPopularKeyset(eq(RecipeStatus.SUCCESS), eq(10L), any(UUID.class), any(Pageable.class));

            CursorPage<RecipeInfo> result = service.getPopulars("cursor", RecipeInfoVideoQuery.ALL);

            assertThat(result.items()).hasSize(1);
            verify(recipeInfoRepository)
                    .findPopularKeyset(eq(RecipeStatus.SUCCESS), eq(10L), any(UUID.class), any(Pageable.class));
        }

        @Test
        @DisplayName("커서 기반 cuisine 조회는 keyset을 사용한다")
        void shouldGetCuisineKeysetWithCursor() {
            RecipeInfo recipeInfo = mock(RecipeInfo.class);

            doReturn(new CountIdCursor(6L, UUID.randomUUID()))
                    .when(countIdCursorCodec)
                    .decode("cursor");
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
