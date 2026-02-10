package com.cheftory.api.recipe.content.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepository;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({RecipeDetailMetaRepositoryImpl.class})
@DisplayName("RecipeDetailMetaRepository 테스트")
class RecipeDetailMetaRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeDetailMetaRepository recipeDetailMetaRepository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("상세 메타 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - 데이터가 존재할 때")
        class GivenExists {
            UUID recipeId;
            RecipeDetailMeta created;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId = UUID.randomUUID();
                created = RecipeDetailMeta.create(30, 4, "Test description", "Test title", clock, recipeId);
                recipeDetailMetaRepository.create(created);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                RecipeDetailMeta result;

                @BeforeEach
                void setUp() throws RecipeDetailMetaException {
                    result = recipeDetailMetaRepository.get(recipeId);
                }

                @Test
                @DisplayName("Then - 상세 메타를 반환한다")
                void thenReturnsMeta() {
                    assertThat(result).isNotNull();
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getCookTime()).isEqualTo(30);
                    assertThat(result.getServings()).isEqualTo(4);
                    assertThat(result.getDescription()).isEqualTo("Test description");
                }
            }
        }

        @Nested
        @DisplayName("Given - 데이터가 존재하지 않을 때")
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
                @DisplayName("Then - DETAIL_META_NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeDetailMetaRepository.get(recipeId))
                            .isInstanceOf(RecipeDetailMetaException.class)
                            .extracting("error")
                            .isEqualTo(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("다중 상세 메타 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 여러 데이터가 존재할 때")
        class GivenMultipleData {
            UUID recipeId1;
            UUID recipeId2;
            UUID recipeId3;
            List<UUID> recipeIds;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId1 = UUID.randomUUID();
                recipeId2 = UUID.randomUUID();
                recipeId3 = UUID.randomUUID();

                RecipeDetailMeta meta1 = RecipeDetailMeta.create(30, 4, "Description 1", "Title 1", clock, recipeId1);
                RecipeDetailMeta meta2 = RecipeDetailMeta.create(45, 2, "Description 2", "Title 2", clock, recipeId2);
                RecipeDetailMeta meta3 = RecipeDetailMeta.create(60, 6, "Description 3", "Title 3", clock, recipeId3);

                recipeDetailMetaRepository.create(meta1);
                recipeDetailMetaRepository.create(meta2);
                recipeDetailMetaRepository.create(meta3);

                recipeIds = List.of(recipeId1, recipeId2, recipeId3);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaRepository.gets(recipeIds);
                }

                @Test
                @DisplayName("Then - 모든 메타 데이터를 반환한다")
                void thenReturnsAll() {
                    assertThat(result).hasSize(3);
                    assertThat(result.stream().map(RecipeDetailMeta::getRecipeId))
                            .containsExactlyInAnyOrder(recipeId1, recipeId2, recipeId3);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 리스트가 주어졌을 때")
        class GivenEmptyList {
            List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                recipeIds = List.of();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaRepository.gets(recipeIds);
                }

                @Test
                @DisplayName("Then - 빈 리스트를 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 일부 데이터만 존재할 때")
        class GivenPartialData {
            UUID recipeId1;
            UUID recipeId2;
            UUID recipeId3;
            List<UUID> recipeIds;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId1 = UUID.randomUUID();
                recipeId2 = UUID.randomUUID();
                recipeId3 = UUID.randomUUID();

                RecipeDetailMeta meta1 = RecipeDetailMeta.create(30, 4, "Description 1", "Title 1", clock, recipeId1);
                RecipeDetailMeta meta3 = RecipeDetailMeta.create(60, 6, "Description 3", "Title 3", clock, recipeId3);

                recipeDetailMetaRepository.create(meta1);
                recipeDetailMetaRepository.create(meta3);

                recipeIds = List.of(recipeId1, recipeId2, recipeId3);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaRepository.gets(recipeIds);
                }

                @Test
                @DisplayName("Then - 존재하는 데이터만 반환한다")
                void thenReturnsExisting() {
                    assertThat(result).hasSize(2);
                    assertThat(result.stream().map(RecipeDetailMeta::getRecipeId))
                            .containsExactlyInAnyOrder(recipeId1, recipeId3);
                }
            }
        }
    }

    @Nested
    @DisplayName("상세 메타 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 데이터가 주어졌을 때")
        class GivenValidData {
            UUID recipeId;
            RecipeDetailMeta recipeDetailMeta;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId = UUID.randomUUID();
                recipeDetailMeta = RecipeDetailMeta.create(45, 2, "Test description", "Test title", clock, recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeDetailMetaRepository.create(recipeDetailMeta);
                }

                @Test
                @DisplayName("Then - 데이터가 저장된다")
                void thenSaved() throws RecipeDetailMetaException {
                    RecipeDetailMeta result = recipeDetailMetaRepository.get(recipeId);
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getCookTime()).isEqualTo(45);
                    assertThat(result.getServings()).isEqualTo(2);
                    assertThat(result.getDescription()).isEqualTo("Test description");
                }
            }
        }
    }
}
