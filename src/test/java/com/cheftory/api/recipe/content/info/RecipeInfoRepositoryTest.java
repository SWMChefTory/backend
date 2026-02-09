package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.tag.RecipeTagRepository;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeRepositoryTest")
public class RecipeInfoRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeInfoRepository recipeInfoRepository;

    @Autowired
    private RecipeYoutubeMetaRepository youtubeMetaRepository;

    @Autowired
    private RecipeTagRepository recipeTagRepository;

    @MockitoBean
    private Clock clock;

    @Nested
    @DisplayName("레시피 저장")
    class SaveRecipeInfo {

        @Nested
        @DisplayName("Given - 새로운 레시피가 주어졌을 때")
        class GivenNewRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                recipeInfo = RecipeInfo.create(clock);
            }

            @Nested
            @DisplayName("When - 레시피를 저장하면")
            class WhenSaveRecipeInfo {

                private RecipeInfo savedRecipeInfo;

                @BeforeEach
                void setUp() {
                    savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
                }

                @DisplayName("Then - 레시피가 저장된다")
                @Test
                void thenRecipeIsSaved() {
                    Optional<RecipeInfo> foundRecipe =
                            recipeInfoRepository.findById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(foundRecipe).isPresent();
                    assertThat(getField(foundRecipe.get(), "id")).isEqualTo(getField(savedRecipeInfo, "id"));
                    assertThat(getField(foundRecipe.get(), "recipeStatus")).isEqualTo(RecipeStatus.IN_PROGRESS);
                    assertThat(getField(foundRecipe.get(), "viewCount")).isEqualTo(0);
                    assertThat(getField(foundRecipe.get(), "createdAt")).isNotNull();
                }
            }
        }

        @Nested
        @DisplayName("Given - 성공 상태로 변경된 레시피가 주어졌을 때")
        class GivenSuccessRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.success(clock);
            }

            @Nested
            @DisplayName("When - 성공 상태 레시피를 저장하면")
            class WhenSaveSuccessRecipeInfo {

                private RecipeInfo savedRecipeInfo;

                @BeforeEach
                void setUp() {
                    savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
                }

                @DisplayName("Then - 성공 상태로 저장된다")
                @Test
                void thenSuccessRecipeIsSaved() {
                    Optional<RecipeInfo> foundRecipe =
                            recipeInfoRepository.findById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(foundRecipe).isPresent();
                    assertThat(getField(foundRecipe.get(), "recipeStatus")).isEqualTo(RecipeStatus.SUCCESS);
                    assertThat(foundRecipe.get().isSuccess()).isTrue();
                    assertThat(foundRecipe.get().isFailed()).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("Given - 실패 상태로 변경된 레시피가 주어졌을 때")
        class GivenFailedRecipeInfo {

            private RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                recipeInfo = RecipeInfo.create(clock);
                recipeInfo.failed(clock);
            }

            @Nested
            @DisplayName("When - 실패 상태 레시피를 저장하면")
            class WhenSaveFailedRecipeInfo {

                private RecipeInfo savedRecipeInfo;

                @BeforeEach
                void setUp() {
                    savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
                }

                @DisplayName("Then - 실패 상태로 저장된다")
                @Test
                void thenFailedRecipeIsSaved() {
                    Optional<RecipeInfo> foundRecipe =
                            recipeInfoRepository.findById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(foundRecipe).isPresent();
                    assertThat(getField(foundRecipe.get(), "recipeStatus")).isEqualTo(RecipeStatus.FAILED);
                    assertThat(foundRecipe.get().isSuccess()).isFalse();
                    assertThat(foundRecipe.get().isFailed()).isTrue();
                }
            }
        }
    }

    @Nested
    @DisplayName("조회수 증가")
    class IncreaseViewCount {

        @Nested
        @DisplayName("Given - 저장된 레시피가 있을 때")
        class GivenSavedRecipeInfo {

            private RecipeInfo savedRecipeInfo;

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                RecipeInfo recipeInfo = RecipeInfo.create(clock);
                savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
            }

            @Nested
            @DisplayName("When - 조회수를 증가시키면")
            class WhenIncreaseViewCount {

                @BeforeEach
                void setUp() {
                    recipeInfoRepository.increaseCount((UUID) getField(savedRecipeInfo, "id"));
                }

                @DisplayName("Then - 조회수가 1 증가한다")
                @Test
                void thenViewCountIsIncreased() {
                    Optional<RecipeInfo> updatedRecipe =
                            recipeInfoRepository.findById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(updatedRecipe).isPresent();
                    assertThat(getField(updatedRecipe.get(), "viewCount")).isEqualTo(1);
                }
            }

            @Nested
            @DisplayName("When - 조회수를 여러 번 증가시키면")
            class WhenIncreaseViewCountMultipleTimes {

                @BeforeEach
                void setUp() {
                    recipeInfoRepository.increaseCount((UUID) getField(savedRecipeInfo, "id"));
                    recipeInfoRepository.increaseCount((UUID) getField(savedRecipeInfo, "id"));
                    recipeInfoRepository.increaseCount((UUID) getField(savedRecipeInfo, "id"));
                }

                @DisplayName("Then - 조회수가 3 증가한다")
                @Test
                void thenViewCountIsIncreasedByThree() {
                    Optional<RecipeInfo> updatedRecipe =
                            recipeInfoRepository.findById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(updatedRecipe).isPresent();
                    assertThat(getField(updatedRecipe.get(), "viewCount")).isEqualTo(3);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentRecipeInfoId {

            private UUID nonExistentId;

            @BeforeEach
            void setUp() {
                nonExistentId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 존재하지 않는 ID로 조회수를 증가시키면")
            class WhenIncreaseViewCountWithNonExistentId {

                @DisplayName("Then - 아무 변화가 없다")
                @Test
                void thenNothingHappens() {
                    // 예외가 발생하지 않고 정상적으로 처리되어야 함
                    recipeInfoRepository.increaseCount(nonExistentId);

                    Optional<RecipeInfo> recipe = recipeInfoRepository.findById(nonExistentId);
                    assertThat(recipe).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("특정 상태가 아닌 레시피 조회")
    class FindRecipesByIdInAndRecipeInfoStatusNot {

        private List<RecipeInfo> savedRecipeInfos;
        private List<UUID> recipeIds;

        @BeforeEach
        void setUp() {
            doReturn(LocalDateTime.now()).when(clock).now();
            RecipeInfo successRecipeInfo1 = RecipeInfo.create(clock);
            successRecipeInfo1.success(clock);
            RecipeInfo successRecipeInfo2 = RecipeInfo.create(clock);
            successRecipeInfo2.success(clock);

            RecipeInfo inProgressRecipeInfo = RecipeInfo.create(clock);

            RecipeInfo failedRecipeInfo = RecipeInfo.create(clock);
            failedRecipeInfo.failed(clock);

            savedRecipeInfos = recipeInfoRepository.saveAll(
                    List.of(successRecipeInfo1, successRecipeInfo2, inProgressRecipeInfo, failedRecipeInfo));

            recipeIds = savedRecipeInfos.stream()
                    .map(recipeInfo -> (UUID) getField(recipeInfo, "id"))
                    .toList();
        }

        @Nested
        @DisplayName("Given - 다양한 상태의 레시피들이 저장되어 있을 때")
        class GivenVariousStatusRecipes {

            @Nested
            @DisplayName("When - 실패 상태가 아닌 레시피들을 조회하면")
            class WhenFindRecipesNotFailed {

                private List<RecipeInfo> notFailedRecipeInfos;

                @BeforeEach
                void setUp() {
                    notFailedRecipeInfos = recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                            recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
                }

                @DisplayName("Then - 실패 상태가 아닌 레시피들만 반환된다")
                @Test
                void thenReturnsRecipesNotFailed() {
                    assertThat(notFailedRecipeInfos).hasSize(3);

                    notFailedRecipeInfos.forEach(recipe -> {
                        assertThat((RecipeStatus) getField(recipe, "recipeStatus"))
                                .isNotEqualTo(RecipeStatus.FAILED);
                        assertThat(recipe.isFailed()).isFalse();
                    });

                    // 성공 상태와 진행 중 상태만 포함되어야 함
                    long successCount = notFailedRecipeInfos.stream()
                            .filter(recipe -> (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.SUCCESS)
                            .count();
                    long inProgressCount = notFailedRecipeInfos.stream()
                            .filter(recipe ->
                                    (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.IN_PROGRESS)
                            .count();

                    assertThat(successCount).isEqualTo(2);
                    assertThat(inProgressCount).isEqualTo(1);
                }
            }

            @Nested
            @DisplayName("When - 진행 중 상태가 아닌 레시피들을 조회하면")
            class WhenFindRecipesNotInProgress {

                private List<RecipeInfo> notInProgressRecipeInfos;

                @BeforeEach
                void setUp() {
                    notInProgressRecipeInfos = recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                            recipeIds, List.of(RecipeStatus.SUCCESS, RecipeStatus.FAILED));
                }

                @DisplayName("Then - 진행 중 상태가 아닌 레시피들만 반환된다")
                @Test
                void thenReturnsRecipesNotInProgress() {
                    assertThat(notInProgressRecipeInfos).hasSize(3);

                    notInProgressRecipeInfos.forEach(recipe -> {
                        assertThat((RecipeStatus) getField(recipe, "recipeStatus"))
                                .isNotEqualTo(RecipeStatus.IN_PROGRESS);
                    });

                    // 성공 상태와 실패 상태만 포함되어야 함
                    long successCount = notInProgressRecipeInfos.stream()
                            .filter(recipe -> (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.SUCCESS)
                            .count();
                    long failedCount = notInProgressRecipeInfos.stream()
                            .filter(recipe -> (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.FAILED)
                            .count();

                    assertThat(successCount).isEqualTo(2);
                    assertThat(failedCount).isEqualTo(1);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 ID 목록이 주어졌을 때")
        class GivenEmptyIdList {

            @Nested
            @DisplayName("When - 빈 목록으로 조회하면")
            class WhenFindWithEmptyIdList {

                private List<RecipeInfo> recipeInfos;

                @BeforeEach
                void setUp() {
                    recipeInfos = recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                            List.of(), List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
                }

                @Test
                @DisplayName("Then - 빈 목록이 반환된다")
                void thenReturnsEmptyList() throws Exception {
                    assertThat(recipeInfos).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("ID 목록으로 레시피 조회")
    class FindAllByIdIn {

        private List<RecipeInfo> savedRecipeInfos;
        private List<UUID> savedRecipeIds;

        @BeforeEach
        void setUp() {
            doReturn(LocalDateTime.now()).when(clock).now();
            RecipeInfo recipeInfo1 = RecipeInfo.create(clock);
            RecipeInfo recipeInfo2 = RecipeInfo.create(clock);
            recipeInfo2.success(clock);
            RecipeInfo recipeInfo3 = RecipeInfo.create(clock);
            recipeInfo3.failed(clock);

            savedRecipeInfos = recipeInfoRepository.saveAll(List.of(recipeInfo1, recipeInfo2, recipeInfo3));
            savedRecipeIds =
                    savedRecipeInfos.stream().map(r -> (UUID) getField(r, "id")).toList();
        }

        @Nested
        @DisplayName("Given - 저장된 레시피 ID들이 주어졌을 때")
        class GivenSavedRecipeInfoIds {

            @Nested
            @DisplayName("When - ID 목록으로 레시피들을 조회하면")
            class WhenFindAllByIdIn {

                private List<RecipeInfo> foundRecipeInfos;

                @BeforeEach
                void setUp() {
                    foundRecipeInfos = recipeInfoRepository.findAllByIdIn(savedRecipeIds);
                }

                @DisplayName("Then - 해당 ID들의 모든 레시피가 반환된다")
                @Test
                void thenReturnsAllRecipesWithGivenIds() {
                    assertThat(foundRecipeInfos).hasSize(3);

                    List<UUID> foundRecipeIds = foundRecipeInfos.stream()
                            .map(r -> (UUID) getField(r, "id"))
                            .toList();
                    assertThat(foundRecipeIds).containsExactlyInAnyOrderElementsOf(savedRecipeIds);

                    // 각 상태가 모두 포함되는지 확인
                    boolean hasInProgress = foundRecipeInfos.stream()
                            .anyMatch(recipe ->
                                    (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.IN_PROGRESS);
                    boolean hasSuccess = foundRecipeInfos.stream()
                            .anyMatch(
                                    recipe -> (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.SUCCESS);
                    boolean hasFailed = foundRecipeInfos.stream()
                            .anyMatch(recipe -> (RecipeStatus) getField(recipe, "recipeStatus") == RecipeStatus.FAILED);

                    assertThat(hasInProgress).isTrue();
                    assertThat(hasSuccess).isTrue();
                    assertThat(hasFailed).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Given - 일부 존재하고 일부 존재하지 않는 ID들이 주어졌을 때")
        class GivenPartiallyExistingIds {

            private List<UUID> mixedIds;

            @BeforeEach
            void setUp() {
                // 저장된 ID 2개 + 존재하지 않는 ID 2개
                mixedIds = List.of(savedRecipeIds.get(0), savedRecipeIds.get(1), UUID.randomUUID(), UUID.randomUUID());
            }

            @Nested
            @DisplayName("When - 혼합된 ID 목록으로 조회하면")
            class WhenFindAllByMixedIds {

                private List<RecipeInfo> foundRecipeInfos;

                @BeforeEach
                void setUp() {
                    foundRecipeInfos = recipeInfoRepository.findAllByIdIn(mixedIds);
                }

                @DisplayName("Then - 존재하는 레시피들만 반환된다")
                @Test
                void thenReturnsOnlyExistingRecipes() {
                    assertThat(foundRecipeInfos).hasSize(2);

                    List<UUID> foundRecipeIds = foundRecipeInfos.stream()
                            .map(r -> (UUID) getField(r, "id"))
                            .toList();
                    assertThat(foundRecipeIds).containsExactlyInAnyOrder(savedRecipeIds.get(0), savedRecipeIds.get(1));
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 ID 목록이 주어졌을 때")
        class GivenEmptyIdList {

            @Nested
            @DisplayName("When - 빈 목록으로 조회하면")
            class WhenFindAllByEmptyIdList {

                private List<RecipeInfo> foundRecipeInfos;

                @BeforeEach
                void setUp() {
                    foundRecipeInfos = recipeInfoRepository.findAllByIdIn(List.of());
                }

                @Test
                @DisplayName("Then - 빈 목록이 반환된다")
                void thenReturnsEmptyList() throws Exception {
                    assertThat(foundRecipeInfos).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 존재 여부 확인")
    class ExistsById {

        @Nested
        @DisplayName("Given - 저장된 레시피가 있을 때")
        class GivenSavedRecipeInfo {

            private RecipeInfo savedRecipeInfo;

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                RecipeInfo recipeInfo = RecipeInfo.create(clock);
                savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
            }

            @Nested
            @DisplayName("When - 저장된 레시피 ID로 존재 여부를 확인하면")
            class WhenCheckingExistenceWithSavedId {

                @Test
                @DisplayName("Then - true가 반환된다")
                void thenReturnTrue() {
                    boolean exists = recipeInfoRepository.existsById((UUID) getField(savedRecipeInfo, "id"));

                    assertThat(exists).isTrue();
                }
            }

            @Nested
            @DisplayName("When - 다른 상태의 레시피들이 존재할 때")
            class WhenDifferentStatusRecipesExist {

                private RecipeInfo successRecipeInfo;
                private RecipeInfo failedRecipeInfo;

                @BeforeEach
                void setUp() {
                    doReturn(LocalDateTime.now()).when(clock).now();
                    RecipeInfo recipeInfo1 = RecipeInfo.create(clock);
                    recipeInfo1.success(clock);
                    successRecipeInfo = recipeInfoRepository.save(recipeInfo1);

                    RecipeInfo recipeInfo2 = RecipeInfo.create(clock);
                    recipeInfo2.failed(clock);
                    failedRecipeInfo = recipeInfoRepository.save(recipeInfo2);
                }

                @Test
                @DisplayName("Then - 모든 상태의 레시피가 존재하는 것으로 확인된다")
                void thenAllStatusRecipesExist() {
                    assertThat(recipeInfoRepository.existsById((UUID) getField(savedRecipeInfo, "id")))
                            .isTrue(); // IN_PROGRESS
                    assertThat(recipeInfoRepository.existsById((UUID) getField(successRecipeInfo, "id")))
                            .isTrue(); // SUCCESS
                    assertThat(recipeInfoRepository.existsById((UUID) getField(failedRecipeInfo, "id")))
                            .isTrue(); // FAILED
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentRecipeInfoId {

            private UUID nonExistentId;

            @BeforeEach
            void setUp() {
                nonExistentId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 존재하지 않는 ID로 존재 여부를 확인하면")
            class WhenCheckingExistenceWithNonExistentId {

                @Test
                @DisplayName("Then - false가 반환된다")
                void thenReturnFalse() {
                    boolean exists = recipeInfoRepository.existsById(nonExistentId);

                    assertThat(exists).isFalse();
                }
            }
        }
    }

    @Nested
    @DisplayName("Repository 성능 및 엣지 케이스")
    class PerformanceAndEdgeCases {

        @Nested
        @DisplayName("Given - 대량의 레시피가 저장되어 있을 때")
        class GivenLargeNumberOfRecipes {

            @BeforeEach
            void setUp() {
                doReturn(LocalDateTime.now()).when(clock).now();
                List<RecipeInfo> recipeInfos = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    RecipeInfo recipeInfo = RecipeInfo.create(clock);
                    if (i % 3 == 0) {
                        recipeInfo.success(clock);
                    } else if (i % 5 == 0) {
                        recipeInfo.failed(clock);
                    }
                    recipeInfos.add(recipeInfo);
                }
                recipeInfoRepository.saveAll(recipeInfos);
            }
        }
    }

    @Nested
    @DisplayName("커서 기반 조회 쿼리")
    class CursorQueries {

        @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();
            doReturn(now).when(clock).now();
            recipeInfoRepository.deleteAllInBatch();
        }

        @Test
        @DisplayName("인기 레시피 첫 페이지를 조회한다")
        void shouldFindPopularFirst() {
            RecipeInfo recipe1 = RecipeInfo.create(clock);
            recipe1.success(clock);
            RecipeInfo recipe2 = RecipeInfo.create(clock);
            recipe2.success(clock);
            RecipeInfo recipe3 = RecipeInfo.create(clock);
            recipe3.success(clock);

            recipeInfoRepository.saveAll(List.of(recipe1, recipe2, recipe3));
            recipeInfoRepository.increaseCount((UUID) getField(recipe1, "id"));
            recipeInfoRepository.increaseCount((UUID) getField(recipe1, "id"));
            recipeInfoRepository.increaseCount((UUID) getField(recipe2, "id"));

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeInfo> result = recipeInfoRepository.findPopularFirst(RecipeStatus.SUCCESS, pageable);

            assertThat(result).hasSize(2);
            assertThat(getField(result.getFirst(), "id")).isEqualTo(getField(recipe1, "id"));
            assertThat(getField(result.get(1), "id")).isEqualTo(getField(recipe2, "id"));
        }

        @Test
        @DisplayName("인기 레시피 keyset을 조회한다")
        void shouldFindPopularKeyset() {
            RecipeInfo recipe1 = RecipeInfo.create(clock);
            recipe1.success(clock);
            RecipeInfo recipe2 = RecipeInfo.create(clock);
            recipe2.success(clock);
            RecipeInfo recipe3 = RecipeInfo.create(clock);
            recipe3.success(clock);

            recipeInfoRepository.saveAll(List.of(recipe1, recipe2, recipe3));
            recipeInfoRepository.increaseCount((UUID) getField(recipe1, "id"));
            recipeInfoRepository.increaseCount((UUID) getField(recipe1, "id"));
            recipeInfoRepository.increaseCount((UUID) getField(recipe2, "id"));

            RecipeInfo latest = recipeInfoRepository
                    .findById((UUID) getField(recipe2, "id"))
                    .orElseThrow();

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeInfo> result = recipeInfoRepository.findPopularKeyset(
                    RecipeStatus.SUCCESS,
                    ((Integer) getField(latest, "viewCount")).longValue(),
                    (UUID) getField(latest, "id"),
                    pageable);

            assertThat(result).hasSize(1);
            assertThat(getField(result.getFirst(), "id")).isEqualTo(getField(recipe3, "id"));
        }

        @Test
        @DisplayName("요리 카테고리 첫 페이지를 조회한다")
        void shouldFindCuisineFirst() {
            RecipeInfo recipe1 = RecipeInfo.create(clock);
            recipe1.success(clock);
            RecipeInfo recipe2 = RecipeInfo.create(clock);
            recipe2.success(clock);

            RecipeInfo savedRecipe1 = recipeInfoRepository.save(recipe1);
            RecipeInfo savedRecipe2 = recipeInfoRepository.save(recipe2);
            UUID recipeId1 = (UUID) getField(savedRecipe1, "id");
            UUID recipeId2 = (UUID) getField(savedRecipe2, "id");
            createRecipeTag(recipeId1, "한식");
            createRecipeTag(recipeId2, "한식");

            recipeInfoRepository.increaseCount(recipeId1);

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeInfo> result = recipeInfoRepository.findCuisineFirst("한식", RecipeStatus.SUCCESS, pageable);

            assertThat(result).hasSize(2);
            assertThat(getField(result.getFirst(), "id")).isEqualTo(recipeId1);
        }

        @Test
        @DisplayName("요리 카테고리 keyset을 조회한다")
        void shouldFindCuisineKeyset() {
            RecipeInfo recipe1 = RecipeInfo.create(clock);
            recipe1.success(clock);
            RecipeInfo recipe2 = RecipeInfo.create(clock);
            recipe2.success(clock);

            RecipeInfo savedRecipe1 = recipeInfoRepository.save(recipe1);
            RecipeInfo savedRecipe2 = recipeInfoRepository.save(recipe2);
            UUID recipeId1 = (UUID) getField(savedRecipe1, "id");
            UUID recipeId2 = (UUID) getField(savedRecipe2, "id");
            createRecipeTag(recipeId1, "한식");
            createRecipeTag(recipeId2, "한식");

            recipeInfoRepository.increaseCount(recipeId1);
            RecipeInfo last = recipeInfoRepository.findById(recipeId1).orElseThrow();

            Pageable pageable = PageRequest.of(0, 2);
            List<RecipeInfo> result = recipeInfoRepository.findCuisineKeyset(
                    "한식",
                    RecipeStatus.SUCCESS,
                    ((Integer) getField(last, "viewCount")).longValue(),
                    (UUID) getField(last, "id"),
                    pageable);

            assertThat(result).hasSize(1);
            assertThat(getField(result.getFirst(), "id")).isEqualTo(recipeId2);
        }
    }

    private void createYoutubeMeta(UUID recipeId, YoutubeMetaType type) {
        String videoId = "test_" + UUID.randomUUID().toString().substring(0, 8);
        YoutubeVideoInfo videoInfo = new YoutubeVideoInfo();
        setField(videoInfo, "videoUri", URI.create("https://www.youtube.com/watch?v=" + videoId));
        setField(videoInfo, "videoId", videoId);
        setField(videoInfo, "title", "Test Video");
        setField(videoInfo, "channelTitle", "Test Channel");
        setField(videoInfo, "thumbnailUrl", URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"));
        setField(videoInfo, "videoSeconds", 180);
        setField(videoInfo, "videoType", type);

        RecipeYoutubeMeta meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
        youtubeMetaRepository.save(meta);
    }

    private void createRecipeTag(UUID recipeId, String tag) {
        RecipeTag recipeTag = RecipeTag.create(tag, recipeId, clock);
        recipeTagRepository.save(recipeTag);
    }
}
