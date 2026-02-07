package com.cheftory.api.recipe.content.detailmeta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaRepository;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeDetailMetaService")
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

    @DisplayName("레시피 상세 메타 생성")
    @Nested
    class CreateRecipeDetailMeta {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            private UUID recipeId;
            private Integer cookTime;
            private Integer servings;
            private String description;
            private RecipeDetailMeta savedMeta;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                cookTime = 30;
                servings = 2;
                description = "맛있는 김치찌개 만들기";
                savedMeta = mock(RecipeDetailMeta.class);

                when(recipeDetailMetaRepository.save(any(RecipeDetailMeta.class)))
                        .thenReturn(savedMeta);
            }

            @DisplayName("When - 레시피 상세 메타를 생성하면")
            @Nested
            class WhenCreateRecipeDetailMeta {

                @Test
                @DisplayName("Then - 상세 메타가 성공적으로 생성된다")
                void thenRecipeDetailMetaIsCreated() {
                    // when
                    recipeDetailMetaService.create(recipeId, cookTime, servings, description);

                    // then
                    ArgumentCaptor<RecipeDetailMeta> captor = ArgumentCaptor.forClass(RecipeDetailMeta.class);
                    verify(recipeDetailMetaRepository).save(captor.capture());

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
        @DisplayName("Given - 간단한 레시피 파라미터가 주어졌을 때")
        class GivenSimpleRecipeParameters {
            private UUID recipeId;
            private Integer cookTime;
            private Integer servings;
            private String description;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                cookTime = 5;
                servings = 1;
                description = "간단한 계란찜";

                when(recipeDetailMetaRepository.save(any(RecipeDetailMeta.class)))
                        .thenReturn(mock(RecipeDetailMeta.class));
            }

            @Test
            @DisplayName("When - 간단한 레시피 메타를 생성하면 Then - 올바른 값으로 생성된다")
            void whenCreateSimpleRecipeMeta_thenCreatedWithCorrectValues() {
                // when
                recipeDetailMetaService.create(recipeId, cookTime, servings, description);

                // then
                ArgumentCaptor<RecipeDetailMeta> captor = ArgumentCaptor.forClass(RecipeDetailMeta.class);
                verify(recipeDetailMetaRepository).save(captor.capture());

                RecipeDetailMeta capturedMeta = captor.getValue();
                assertThat(capturedMeta.getCookTime()).isEqualTo(5);
                assertThat(capturedMeta.getServings()).isEqualTo(1);
                assertThat(capturedMeta.getDescription()).isEqualTo("간단한 계란찜");
            }
        }
    }

    @DisplayName("레시피 ID로 상세 메타 조회")
    @Nested
    class FindByRecipeId {

        @Nested
        @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
        class GivenExistingRecipeId {
            private UUID recipeId;
            private RecipeDetailMeta expectedMeta;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                expectedMeta = createMockRecipeDetailMeta(recipeId, 30, 2, "김치찌개");

                when(recipeDetailMetaRepository.findByRecipeId(recipeId)).thenReturn(Optional.of(expectedMeta));
            }

            @Test
            @DisplayName("When - 상세 메타를 조회하면 Then - 해당 메타가 반환된다")
            void whenFindByRecipeId_thenReturnsMetaData() {
                // when
                RecipeDetailMeta result = recipeDetailMetaService.get(recipeId);

                // then
                assertThat(result).isEqualTo(expectedMeta);
                assertThat(result.getRecipeId()).isEqualTo(recipeId);
                assertThat(result.getCookTime()).isEqualTo(30);
                assertThat(result.getServings()).isEqualTo(2);
                assertThat(result.getDescription()).isEqualTo("김치찌개");

                verify(recipeDetailMetaRepository).findByRecipeId(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistingRecipeId {
            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                when(recipeDetailMetaRepository.findByRecipeId(recipeId)).thenReturn(Optional.empty());
            }

            @Test
            @DisplayName("When - 상세 메타를 조회하면 Then - 예외가 발생한다")
            void whenFindByRecipeId_thenReturnsEmptyOptional() {
                assertThatThrownBy(() -> recipeDetailMetaService.get(recipeId))
                        .isInstanceOf(RecipeDetailMetaException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND);
            }
        }
    }

    @DisplayName("여러 레시피 ID로 상세 메타 조회")
    @Nested
    class FindByMultipleRecipeIds {

        @Nested
        @DisplayName("Given - 여러 레시피 ID가 주어졌을 때")
        class GivenMultipleRecipeIds {
            private List<UUID> recipeIds;
            private List<RecipeDetailMeta> expectedMetas;

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

                when(recipeDetailMetaRepository.findAllByRecipeIdIn(recipeIds)).thenReturn(expectedMetas);
            }

            @Test
            @DisplayName("When - 여러 상세 메타를 조회하면 Then - 모든 메타가 반환된다")
            void whenFindByMultipleRecipeIds_thenReturnsAllMetas() {
                // when
                List<RecipeDetailMeta> result = recipeDetailMetaService.getIn(recipeIds);

                // then
                assertThat(result).hasSize(3);
                assertThat(result).containsExactlyElementsOf(expectedMetas);
                assertThat(result)
                        .extracting(RecipeDetailMeta::getDescription)
                        .containsExactlyInAnyOrder("김치찌개", "계란찜", "불고기");
                assertThat(result).extracting(RecipeDetailMeta::getCookTime).containsExactlyInAnyOrder(30, 15, 45);
                assertThat(result).extracting(RecipeDetailMeta::getServings).containsExactlyInAnyOrder(2, 1, 4);

                verify(recipeDetailMetaRepository).findAllByRecipeIdIn(recipeIds);
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID들이 주어졌을 때")
        class GivenNonExistingRecipeIds {
            private List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
                when(recipeDetailMetaRepository.findAllByRecipeIdIn(recipeIds)).thenReturn(Collections.emptyList());
            }

            @Test
            @DisplayName("When - 여러 상세 메타를 조회하면 Then - 빈 목록이 반환된다")
            void whenFindByNonExistingIds_thenReturnsEmptyList() {
                // when
                List<RecipeDetailMeta> result = recipeDetailMetaService.getIn(recipeIds);

                // then
                assertThat(result).isEmpty();
                verify(recipeDetailMetaRepository).findAllByRecipeIdIn(recipeIds);
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
        class GivenEmptyRecipeIds {
            private List<UUID> emptyRecipeIds;

            @BeforeEach
            void setUp() {
                emptyRecipeIds = Collections.emptyList();
                when(recipeDetailMetaRepository.findAllByRecipeIdIn(emptyRecipeIds))
                        .thenReturn(Collections.emptyList());
            }

            @Test
            @DisplayName("When - 빈 목록으로 조회하면 Then - 빈 목록이 반환된다")
            void whenFindByEmptyList_thenReturnsEmptyList() {
                // when
                List<RecipeDetailMeta> result = recipeDetailMetaService.getIn(emptyRecipeIds);

                // then
                assertThat(result).isEmpty();
                verify(recipeDetailMetaRepository).findAllByRecipeIdIn(emptyRecipeIds);
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
