package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.I18nTranslator;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeInfoService 테스트")
class RecipeInfoServiceTest {

    private RecipeInfoRepository repository;
    private Clock clock;
    private I18nTranslator translator;
    private RecipeInfoService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeInfoRepository.class);
        clock = mock(Clock.class);
        translator = mock(I18nTranslator.class);
        service = new RecipeInfoService(repository, clock, translator);
    }

    @Nested
    @DisplayName("성공 레시피 조회 (getSuccess)")
    class GetSuccess {

        @Nested
        @DisplayName("Given - 성공 상태의 레시피가 있을 때")
        class GivenSuccessRecipe {
            UUID recipeId;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                recipeId = UUID.randomUUID();
                recipeInfo = mock(RecipeInfo.class);
                doReturn(false).when(recipeInfo).isFailed();
                doReturn(false).when(recipeInfo).isBlocked();
                doReturn(recipeInfo).when(repository).get(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                RecipeInfo result;

                @BeforeEach
                void setUp() throws RecipeInfoException {
                    result = service.getSuccess(recipeId);
                }

                @Test
                @DisplayName("Then - 레시피를 반환한다")
                void thenReturnsRecipe() throws RecipeInfoException {
                    assertThat(result).isEqualTo(recipeInfo);
                    verify(repository).get(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 실패 상태의 레시피가 있을 때")
        class GivenFailedRecipe {
            UUID recipeId;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                recipeId = UUID.randomUUID();
                recipeInfo = mock(RecipeInfo.class);
                doReturn(true).when(recipeInfo).isFailed();
                doReturn(recipeInfo).when(repository).get(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - RECIPE_FAILED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.getSuccess(recipeId))
                            .isInstanceOf(RecipeInfoException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeInfoErrorCode.RECIPE_FAILED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 차단 상태의 레시피가 있을 때")
        class GivenBlockedRecipe {
            UUID recipeId;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                recipeId = UUID.randomUUID();
                recipeInfo = mock(RecipeInfo.class);
                doReturn(false).when(recipeInfo).isFailed();
                doReturn(true).when(recipeInfo).isBlocked();
                doReturn(recipeInfo).when(repository).get(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - RECIPE_BANNED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.getSuccess(recipeId))
                            .isInstanceOf(RecipeInfoException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeInfoErrorCode.RECIPE_BANNED);
                }
            }
        }
    }

    @Nested
    @DisplayName("위임 메서드 (Delegate Methods)")
    class DelegateMethods {

        @Nested
        @DisplayName("increaseCount")
        class IncreaseCount {
            @Test
            @DisplayName("repository로 위임한다")
            void delegates() {
                UUID recipeId = UUID.randomUUID();
                service.increaseCount(recipeId);
                verify(repository).increaseCount(recipeId);
            }
        }

        @Nested
        @DisplayName("create")
        class Create {
            @Test
            @DisplayName("신규 레시피를 생성해 repository로 위임한다")
            void delegates() {
                RecipeInfo created = service.create();
                assertThat(created).isNotNull();
                verify(repository).create(created);
            }
        }

        @Nested
        @DisplayName("getProgresses")
        class GetProgresses {
            @Test
            @DisplayName("repository.getProgressRecipes로 위임한다")
            void delegates() {
                List<UUID> recipeIds = List.of(UUID.randomUUID());
                List<RecipeInfo> expected = List.of(mock(RecipeInfo.class));
                doReturn(expected).when(repository).getProgressRecipes(recipeIds);

                List<RecipeInfo> result = service.getProgresses(recipeIds);

                assertThat(result).isEqualTo(expected);
                verify(repository).getProgressRecipes(recipeIds);
            }
        }

        @Nested
        @DisplayName("gets")
        class Gets {
            @Test
            @DisplayName("repository.gets로 위임한다")
            void delegates() {
                List<UUID> recipeIds = List.of(UUID.randomUUID());
                List<RecipeInfo> expected = List.of(mock(RecipeInfo.class));
                doReturn(expected).when(repository).gets(recipeIds);

                List<RecipeInfo> result = service.gets(recipeIds);

                assertThat(result).isEqualTo(expected);
                verify(repository).gets(recipeIds);
            }
        }

        @Nested
        @DisplayName("success")
        class Success {
            @Test
            @DisplayName("repository.success로 위임한다")
            void delegates() throws RecipeInfoException {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo expected = mock(RecipeInfo.class);
                doReturn(expected).when(repository).success(recipeId, clock);

                RecipeInfo result = service.success(recipeId);

                assertThat(result).isEqualTo(expected);
                verify(repository).success(recipeId, clock);
            }
        }

        @Nested
        @DisplayName("failed")
        class Failed {
            @Test
            @DisplayName("repository.failed로 위임한다")
            void delegates() throws RecipeInfoException {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo expected = mock(RecipeInfo.class);
                doReturn(expected).when(repository).failed(recipeId, clock);

                RecipeInfo result = service.failed(recipeId);

                assertThat(result).isEqualTo(expected);
                verify(repository).failed(recipeId, clock);
            }
        }

        @Nested
        @DisplayName("block")
        class Block {
            @Test
            @DisplayName("repository.block으로 위임한다")
            void delegates() throws RecipeInfoException {
                UUID recipeId = UUID.randomUUID();
                service.block(recipeId);
                verify(repository).block(recipeId, clock);
            }
        }

        @Nested
        @DisplayName("exists")
        class Exists {
            @Test
            @DisplayName("repository.exists로 위임한다")
            void delegates() {
                UUID recipeId = UUID.randomUUID();
                doReturn(true).when(repository).exists(recipeId);
                boolean result = service.exists(recipeId);
                assertThat(result).isTrue();
                verify(repository).exists(recipeId);
            }
        }

        @Nested
        @DisplayName("get")
        class Get {
            @Test
            @DisplayName("repository.get으로 위임한다")
            void delegates() throws RecipeInfoException {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo expected = mock(RecipeInfo.class);
                doReturn(expected).when(repository).get(recipeId);

                RecipeInfo result = service.get(recipeId);

                assertThat(result).isEqualTo(expected);
                verify(repository).get(recipeId);
            }
        }
    }

    @Nested
    @DisplayName("커서 조회 (Cursor Queries)")
    class CursorQueries {

        @Nested
        @DisplayName("인기 레시피 조회 (getPopulars)")
        class GetPopulars {

            @Nested
            @DisplayName("Given - 커서가 없을 때")
            class GivenNoCursor {
                CursorPage<RecipeInfo> expected;

                @BeforeEach
                void setUp() {
                    expected = CursorPage.of(List.of(), "next");
                    doReturn(expected).when(repository).popularFirst(RecipeInfoVideoQuery.ALL);
                }

                @Nested
                @DisplayName("When - 조회를 요청하면")
                class WhenGetting {
                    CursorPage<RecipeInfo> result;

                    @BeforeEach
                    void setUp() throws CursorException {
                        result = service.getPopulars("", RecipeInfoVideoQuery.ALL);
                    }

                    @Test
                    @DisplayName("Then - popularFirst를 호출한다")
                    void thenCallsPopularFirst() {
                        assertThat(result).isEqualTo(expected);
                        verify(repository).popularFirst(RecipeInfoVideoQuery.ALL);
                    }
                }
            }

            @Nested
            @DisplayName("Given - 커서가 있을 때")
            class GivenCursor {
                CursorPage<RecipeInfo> expected;

                @BeforeEach
                void setUp() throws CursorException {
                    expected = CursorPage.of(List.of(), null);
                    doReturn(expected).when(repository).popularKeyset(RecipeInfoVideoQuery.SHORTS, "cursor");
                }

                @Nested
                @DisplayName("When - 조회를 요청하면")
                class WhenGetting {
                    CursorPage<RecipeInfo> result;

                    @BeforeEach
                    void setUp() throws CursorException {
                        result = service.getPopulars("cursor", RecipeInfoVideoQuery.SHORTS);
                    }

                    @Test
                    @DisplayName("Then - popularKeyset을 호출한다")
                    void thenCallsPopularKeyset() throws CursorException {
                        assertThat(result).isEqualTo(expected);
                        verify(repository).popularKeyset(RecipeInfoVideoQuery.SHORTS, "cursor");
                    }
                }
            }
        }

        @Nested
        @DisplayName("음식 종류별 조회 (getCuisines)")
        class GetCuisines {

            @Nested
            @DisplayName("Given - 커서가 없을 때")
            class GivenNoCursor {
                CursorPage<RecipeInfo> expected;

                @BeforeEach
                void setUp() {
                    expected = CursorPage.of(List.of(), "next");
                    doReturn("한식").when(translator).translate(RecipeCuisineType.KOREAN.messageKey());
                    doReturn(expected).when(repository).cusineFirst("한식");
                }

                @Nested
                @DisplayName("When - 조회를 요청하면")
                class WhenGetting {
                    CursorPage<RecipeInfo> result;

                    @BeforeEach
                    void setUp() throws CursorException {
                        result = service.getCuisines(RecipeCuisineType.KOREAN, null);
                    }

                    @Test
                    @DisplayName("Then - cusineFirst를 호출한다")
                    void thenCallsCusineFirst() {
                        assertThat(result).isEqualTo(expected);
                        verify(translator).translate(RecipeCuisineType.KOREAN.messageKey());
                        verify(repository).cusineFirst("한식");
                    }
                }
            }

            @Nested
            @DisplayName("Given - 커서가 있을 때")
            class GivenCursor {
                CursorPage<RecipeInfo> expected;

                @BeforeEach
                void setUp() throws CursorException {
                    expected = CursorPage.of(List.of(), null);
                    doReturn("한식").when(translator).translate(RecipeCuisineType.KOREAN.messageKey());
                    doReturn(expected).when(repository).cuisineKeyset("한식", "cursor");
                }

                @Nested
                @DisplayName("When - 조회를 요청하면")
                class WhenGetting {
                    CursorPage<RecipeInfo> result;

                    @BeforeEach
                    void setUp() throws CursorException {
                        result = service.getCuisines(RecipeCuisineType.KOREAN, "cursor");
                    }

                    @Test
                    @DisplayName("Then - cuisineKeyset을 호출한다")
                    void thenCallsCuisineKeyset() throws CursorException {
                        assertThat(result).isEqualTo(expected);
                        verify(translator).translate(RecipeCuisineType.KOREAN.messageKey());
                        verify(repository).cuisineKeyset("한식", "cursor");
                    }
                }
            }
        }
    }
}
