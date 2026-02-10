package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CountIdCursor;
import com.cheftory.api._common.cursor.CountIdCursorCodec;
import com.cheftory.api._common.cursor.CursorErrorCode;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepository;
import com.cheftory.api.recipe.content.info.repository.RecipeInfoRepositoryImpl;
import com.cheftory.api.recipe.dto.RecipeInfoVideoQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RecipeInfoRepositoryImpl.class, CountIdCursorCodec.class})
@DisplayName("RecipeInfoRepository 테스트")
class RecipeInfoRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeInfoRepository recipeInfoRepository;

    @Autowired
    private CountIdCursorCodec countIdCursorCodec;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - 레시피가 존재할 때")
        class GivenExists {
            RecipeInfo created;

            @BeforeEach
            void setUp() {
                created = RecipeInfo.create(clock);
                recipeInfoRepository.create(created);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                RecipeInfo result;

                @BeforeEach
                void setUp() throws RecipeInfoException {
                    result = recipeInfoRepository.get(idOf(created));
                }

                @Test
                @DisplayName("Then - 레시피 정보를 반환한다")
                void thenReturnsInfo() {
                    assertThat(result).isNotNull();
                    assertThat(idOf(result)).isEqualTo(idOf(created));
                    assertThat(statusOf(result)).isEqualTo(statusOf(created));
                }
            }
        }

        @Nested
        @DisplayName("Given - 레시피가 존재하지 않을 때")
        class GivenNotExists {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - RECIPE_INFO_NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeInfoRepository.get(recipeId))
                            .isInstanceOf(RecipeInfoException.class)
                            .extracting("error")
                            .isEqualTo(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("목록 조회 (gets, getProgressRecipes)")
    class ListQueries {

        @Nested
        @DisplayName("Given - 다양한 상태의 레시피들이 존재할 때")
        class GivenVariousStatusRecipes {
            RecipeInfo success;
            RecipeInfo failed;
            RecipeInfo progress;
            List<UUID> ids;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                success = recipeInfoRepository.create(RecipeInfo.create(clock));
                failed = recipeInfoRepository.create(RecipeInfo.create(clock));
                progress = recipeInfoRepository.create(RecipeInfo.create(clock));

                success.success(clock);
                recipeInfoRepository.create(success);

                failed.failed(clock);
                recipeInfoRepository.create(failed);

                ids = List.of(idOf(success), idOf(failed), idOf(progress));
            }

            @Nested
            @DisplayName("When - gets를 호출하면")
            class WhenGets {
                List<RecipeInfo> result;

                @BeforeEach
                void setUp() {
                    result = recipeInfoRepository.gets(ids);
                }

                @Test
                @DisplayName("Then - SUCCESS 상태의 레시피만 반환한다")
                void thenReturnsSuccessOnly() {
                    assertThat(result).hasSize(1);
                    assertThat(idOf(result.getFirst())).isEqualTo(idOf(success));
                }
            }

            @Nested
            @DisplayName("When - getProgressRecipes를 호출하면")
            class WhenGetProgressRecipes {
                List<RecipeInfo> result;

                @BeforeEach
                void setUp() {
                    result = recipeInfoRepository.getProgressRecipes(ids);
                }

                @Test
                @DisplayName("Then - IN_PROGRESS 상태의 레시피만 반환한다")
                void thenReturnsProgressOnly() {
                    assertThat(result).hasSize(1);
                    assertThat(idOf(result.getFirst())).isEqualTo(idOf(progress));
                }
            }
        }
    }

    @Nested
    @DisplayName("상태 변경 (success, failed, block)")
    class StateTransitions {

        @Nested
        @DisplayName("Given - 레시피가 존재할 때")
        class GivenRecipe {
            RecipeInfo created;

            @BeforeEach
            void setUp() {
                created = recipeInfoRepository.create(RecipeInfo.create(clock));
            }

            @Nested
            @DisplayName("When - success를 호출하면")
            class WhenSuccess {
                RecipeInfo updated;

                @BeforeEach
                void setUp() throws RecipeInfoException {
                    updated = recipeInfoRepository.success(idOf(created), clock);
                }

                @Test
                @DisplayName("Then - 상태가 SUCCESS로 변경된다")
                void thenStatusIsSuccess() {
                    assertThat(updated.isSuccess()).isTrue();
                }
            }

            @Nested
            @DisplayName("When - failed를 호출하면")
            class WhenFailed {
                RecipeInfo updated;

                @BeforeEach
                void setUp() throws RecipeInfoException {
                    updated = recipeInfoRepository.failed(idOf(created), clock);
                }

                @Test
                @DisplayName("Then - 상태가 FAILED로 변경된다")
                void thenStatusIsFailed() {
                    assertThat(updated.isFailed()).isTrue();
                }
            }

            @Nested
            @DisplayName("When - block을 호출하면")
            class WhenBlock {

                @BeforeEach
                void setUp() throws RecipeInfoException {
                    recipeInfoRepository.block(idOf(created), clock);
                }

                @Test
                @DisplayName("Then - 상태가 BLOCKED로 변경된다")
                void thenStatusIsBlocked() throws RecipeInfoException {
                    RecipeInfo blocked = recipeInfoRepository.get(idOf(created));
                    assertThat(blocked.isBlocked()).isTrue();
                }
            }
        }
    }

    @Nested
    @DisplayName("기본 조작 (create, increaseCount, exists)")
    class BasicOperations {

        @Nested
        @DisplayName("Given - 신규 레시피가 주어졌을 때")
        class GivenNewRecipe {
            RecipeInfo created;

            @BeforeEach
            void setUp() {
                created = RecipeInfo.create(clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {
                RecipeInfo saved;

                @BeforeEach
                void setUp() {
                    saved = recipeInfoRepository.create(created);
                }

                @Test
                @DisplayName("Then - 레시피가 저장된다")
                void thenSaved() throws RecipeInfoException {
                    RecipeInfo found = recipeInfoRepository.get(idOf(saved));
                    assertThat(idOf(found)).isEqualTo(idOf(saved));
                    assertThat(statusOf(found)).isEqualTo(RecipeStatus.IN_PROGRESS);
                }
            }
        }

        @Nested
        @DisplayName("Given - 저장된 레시피가 있을 때")
        class GivenSavedRecipe {
            RecipeInfo created;

            @BeforeEach
            void setUp() {
                created = recipeInfoRepository.create(RecipeInfo.create(clock));
            }

            @Nested
            @DisplayName("When - 조회수 증가를 요청하면")
            class WhenIncreasingCount {

                @BeforeEach
                void setUp() {
                    recipeInfoRepository.increaseCount(created.getId());
                }

                @Test
                @DisplayName("Then - 조회수가 증가한다")
                void thenCountIncreased() throws RecipeInfoException {
                    RecipeInfo updated = recipeInfoRepository.get(created.getId());
                    assertThat(viewCountOf(updated)).isEqualTo(1);
                }
            }

            @Nested
            @DisplayName("When - 존재 여부를 확인하면")
            class WhenCheckingExistence {
                boolean exists;

                @BeforeEach
                void setUp() {
                    exists = recipeInfoRepository.exists(idOf(created));
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() {
                    assertThat(exists).isTrue();
                }
            }
        }
    }

    @Nested
    @DisplayName("인기 레시피 조회 (popularFirst, popularKeyset)")
    class PopularCursorQueries {

        @Nested
        @DisplayName("Given - 조회수가 다른 레시피들이 존재할 때")
        class GivenRecipesWithCounts {
            RecipeInfo high;
            RecipeInfo low;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                high = recipeInfoRepository.create(RecipeInfo.create(clock));
                recipeInfoRepository.success(idOf(high), clock);
                recipeInfoRepository.increaseCount(idOf(high));
                recipeInfoRepository.increaseCount(idOf(high));

                low = recipeInfoRepository.create(RecipeInfo.create(clock));
                recipeInfoRepository.success(idOf(low), clock);
                recipeInfoRepository.increaseCount(idOf(low));
            }

            @Nested
            @DisplayName("When - popularFirst(ALL)을 호출하면")
            class WhenPopularFirst {
                CursorPage<RecipeInfo> result;

                @BeforeEach
                void setUp() {
                    result = recipeInfoRepository.popularFirst(RecipeInfoVideoQuery.ALL);
                }

                @Test
                @DisplayName("Then - 조회수 내림차순으로 반환한다")
                void thenReturnsSorted() {
                    List<UUID> ids = result.items().stream()
                            .map(RecipeInfoRepositoryTest.this::idOf)
                            .toList();
                    assertThat(ids).contains(idOf(high), idOf(low));
                    assertThat(ids.indexOf(idOf(high))).isLessThan(ids.indexOf(idOf(low)));
                }
            }

            @Nested
            @DisplayName("When - 유효한 커서로 popularKeyset을 호출하면")
            class WhenPopularKeyset {
                CursorPage<RecipeInfo> result;

                @BeforeEach
                void setUp() throws CursorException {
                    String cursor = countIdCursorCodec.encode(new CountIdCursor(2, idOf(high)));
                    result = recipeInfoRepository.popularKeyset(RecipeInfoVideoQuery.ALL, cursor);
                }

                @Test
                @DisplayName("Then - 다음 페이지를 반환한다")
                void thenReturnsNextPage() {
                    List<UUID> ids = result.items().stream()
                            .map(RecipeInfoRepositoryTest.this::idOf)
                            .toList();
                    assertThat(ids).contains(idOf(low));
                    assertThat(ids).doesNotContain(idOf(high));
                }
            }

            @Nested
            @DisplayName("When - 잘못된 커서로 popularKeyset을 호출하면")
            class WhenInvalidCursor {

                @Test
                @DisplayName("Then - INVALID_CURSOR 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() ->
                                    recipeInfoRepository.popularKeyset(RecipeInfoVideoQuery.ALL, "invalid-cursor"))
                            .isInstanceOf(CursorException.class)
                            .extracting("error")
                            .isEqualTo(CursorErrorCode.INVALID_CURSOR);
                }
            }
        }
    }

    private UUID idOf(RecipeInfo recipeInfo) {
        return (UUID) getField(recipeInfo, "id");
    }

    private long viewCountOf(RecipeInfo recipeInfo) {
        return (Integer) getField(recipeInfo, "viewCount");
    }

    private RecipeStatus statusOf(RecipeInfo recipeInfo) {
        return (RecipeStatus) getField(recipeInfo, "recipeStatus");
    }
}
