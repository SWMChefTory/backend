package com.cheftory.api.recipe.content.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeDetailMetaService 테스트")
public class RecipeDetailMetaServiceTest {

    private RecipeDetailMetaRepository recipeDetailMetaRepository;
    private Clock clock;
    private RecipeDetailMetaService recipeDetailMetaService;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        recipeDetailMetaRepository = mock(RecipeDetailMetaRepository.class);
        clock = mock(Clock.class);
        fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

        when(clock.now()).thenReturn(fixedTime);

        recipeDetailMetaService = new RecipeDetailMetaService(recipeDetailMetaRepository, clock);
    }

    @Nested
    @DisplayName("상세 메타 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID recipeId;
            Integer cookTime;
            Integer servings;
            String description;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                cookTime = 30;
                servings = 2;
                description = "맛있는 김치찌개 만들기";
                doNothing().when(recipeDetailMetaRepository).create(any(RecipeDetailMeta.class));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeDetailMetaService.create(recipeId, cookTime, servings, description);
                }

                @Test
                @DisplayName("Then - 상세 메타가 생성되어 저장된다")
                void thenCreatedAndSaved() {
                    ArgumentCaptor<RecipeDetailMeta> captor = ArgumentCaptor.forClass(RecipeDetailMeta.class);
                    verify(recipeDetailMetaRepository).create(captor.capture());

                    RecipeDetailMeta capturedMeta = captor.getValue();
                    assertThat(capturedMeta).isNotNull();
                    assertThat(capturedMeta.getId()).isNotNull();
                    assertThat(capturedMeta.getRecipeId()).isEqualTo(recipeId);
                    assertThat(capturedMeta.getCookTime()).isEqualTo(30);
                    assertThat(capturedMeta.getServings()).isEqualTo(2);
                    assertThat(capturedMeta.getDescription()).isEqualTo("맛있는 김치찌개 만들기");
                    assertThat(capturedMeta.getCreatedAt()).isEqualTo(fixedTime);
                }
            }
        }

        @Nested
        @DisplayName("Given - 간단한 파라미터가 주어졌을 때")
        class GivenSimpleParameters {
            UUID recipeId;
            Integer cookTime;
            Integer servings;
            String description;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                cookTime = 5;
                servings = 1;
                description = "간단한 계란찜";
                doNothing().when(recipeDetailMetaRepository).create(any(RecipeDetailMeta.class));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeDetailMetaService.create(recipeId, cookTime, servings, description);
                }

                @Test
                @DisplayName("Then - 올바른 값으로 생성된다")
                void thenCreatedCorrectly() {
                    ArgumentCaptor<RecipeDetailMeta> captor = ArgumentCaptor.forClass(RecipeDetailMeta.class);
                    verify(recipeDetailMetaRepository).create(captor.capture());

                    RecipeDetailMeta capturedMeta = captor.getValue();
                    assertThat(capturedMeta.getCookTime()).isEqualTo(5);
                    assertThat(capturedMeta.getServings()).isEqualTo(1);
                    assertThat(capturedMeta.getDescription()).isEqualTo("간단한 계란찜");
                }
            }
        }
    }

    @Nested
    @DisplayName("상세 메타 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
        class GivenExistingId {
            UUID recipeId;
            RecipeDetailMeta expectedMeta;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId = UUID.randomUUID();
                expectedMeta = createMockRecipeDetailMeta(recipeId, 30, 2, "김치찌개");
                doReturn(expectedMeta).when(recipeDetailMetaRepository).get(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                RecipeDetailMeta result;

                @BeforeEach
                void setUp() throws RecipeDetailMetaException {
                    result = recipeDetailMetaService.get(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 메타를 반환한다")
                void thenReturnsMeta() throws RecipeDetailMetaException {
                    assertThat(result).isEqualTo(expectedMeta);
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getCookTime()).isEqualTo(30);
                    assertThat(result.getServings()).isEqualTo(2);
                    assertThat(result.getDescription()).isEqualTo("김치찌개");
                    verify(recipeDetailMetaRepository).get(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistingId {
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeDetailMetaException {
                recipeId = UUID.randomUUID();
                doThrow(new RecipeDetailMetaException(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND))
                        .when(recipeDetailMetaRepository)
                        .get(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeDetailMetaService.get(recipeId))
                            .isInstanceOf(RecipeDetailMetaException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("다중 상세 메타 조회 (getIn)")
    class GetIn {

        @Nested
        @DisplayName("Given - 여러 레시피 ID가 주어졌을 때")
        class GivenMultipleIds {
            List<UUID> recipeIds;
            List<RecipeDetailMeta> expectedMetas;

            @BeforeEach
            void setUp() {
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();
                UUID recipeId3 = UUID.randomUUID();
                recipeIds = List.of(recipeId1, recipeId2, recipeId3);

                expectedMetas = List.of(
                        createMockRecipeDetailMeta(recipeId1, 30, 2, "김치찌개"),
                        createMockRecipeDetailMeta(recipeId2, 15, 1, "계란찜"),
                        createMockRecipeDetailMeta(recipeId3, 45, 4, "불고기"));

                when(recipeDetailMetaRepository.gets(recipeIds)).thenReturn(expectedMetas);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaService.getIn(recipeIds);
                }

                @Test
                @DisplayName("Then - 모든 메타를 반환한다")
                void thenReturnsAll() {
                    assertThat(result).hasSize(3);
                    assertThat(result).containsExactlyElementsOf(expectedMetas);
                    assertThat(result)
                            .extracting(RecipeDetailMeta::getDescription)
                            .containsExactlyInAnyOrder("김치찌개", "계란찜", "불고기");
                    assertThat(result).extracting(RecipeDetailMeta::getCookTime).containsExactlyInAnyOrder(30, 15, 45);
                    assertThat(result).extracting(RecipeDetailMeta::getServings).containsExactlyInAnyOrder(2, 1, 4);

                    verify(recipeDetailMetaRepository).gets(recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID들이 주어졌을 때")
        class GivenNonExistingIds {
            List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
                when(recipeDetailMetaRepository.gets(recipeIds)).thenReturn(Collections.emptyList());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaService.getIn(recipeIds);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result).isEmpty();
                    verify(recipeDetailMetaRepository).gets(recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 ID 목록이 주어졌을 때")
        class GivenEmptyIds {
            List<UUID> emptyRecipeIds;

            @BeforeEach
            void setUp() {
                emptyRecipeIds = Collections.emptyList();
                when(recipeDetailMetaRepository.gets(emptyRecipeIds)).thenReturn(Collections.emptyList());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeDetailMeta> result;

                @BeforeEach
                void setUp() {
                    result = recipeDetailMetaService.getIn(emptyRecipeIds);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result).isEmpty();
                    verify(recipeDetailMetaRepository).gets(emptyRecipeIds);
                }
            }
        }
    }

    private RecipeDetailMeta createMockRecipeDetailMeta(
            UUID recipeId, Integer cookTime, Integer servings, String description) {
        RecipeDetailMeta meta = mock(RecipeDetailMeta.class);
        UUID metaId = UUID.randomUUID();

        doReturn(metaId).when(meta).getId();
        doReturn(recipeId).when(meta).getRecipeId();
        doReturn(cookTime).when(meta).getCookTime();
        doReturn(servings).when(meta).getServings();
        doReturn(description).when(meta).getDescription();
        doReturn(fixedTime).when(meta).getCreatedAt();

        return meta;
    }
}
