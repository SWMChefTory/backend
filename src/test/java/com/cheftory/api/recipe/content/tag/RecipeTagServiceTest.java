package com.cheftory.api.recipe.content.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.tag.repository.RecipeTagRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeTagService 테스트")
public class RecipeTagServiceTest {

    private RecipeTagRepository recipeTagRepository;
    private Clock clock;
    private RecipeTagService recipeTagService;
    private LocalDateTime fixedTime;

    @BeforeEach
    public void setUp() {
        recipeTagRepository = mock(RecipeTagRepository.class);
        clock = mock(Clock.class);
        fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

        when(clock.now()).thenReturn(fixedTime);

        recipeTagService = new RecipeTagService(recipeTagRepository, clock);
    }

    @Nested
    @DisplayName("레시피 태그 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 태그 목록이 주어졌을 때")
        class GivenValidTags {
            UUID recipeId;
            List<String> tags;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                tags = List.of("한식", "매운맛", "간단요리");
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeTagService.create(recipeId, tags);
                }

                @Test
                @DisplayName("Then - 모든 태그를 저장한다")
                void thenSavesAllTags() {
                    @SuppressWarnings("unchecked")
                    ArgumentCaptor<List<RecipeTag>> captor = ArgumentCaptor.forClass(List.class);
                    verify(recipeTagRepository).create(captor.capture());

                    List<RecipeTag> capturedTags = captor.getValue();
                    assertThat(capturedTags).hasSize(3);

                    for (RecipeTag tag : capturedTags) {
                        assertThat(tag.getRecipeId()).isEqualTo(recipeId);
                        assertThat(tag.getTag()).isIn("한식", "매운맛", "간단요리");
                        assertThat(tag.getCreatedAt()).isEqualTo(fixedTime);
                    }
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 태그 목록이 주어졌을 때")
        class GivenEmptyTags {
            UUID recipeId;
            List<String> emptyTags;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                emptyTags = Collections.emptyList();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() {
                    recipeTagService.create(recipeId, emptyTags);
                }

                @Test
                @DisplayName("Then - 빈 목록을 저장한다")
                void thenSavesEmptyList() {
                    @SuppressWarnings("unchecked")
                    ArgumentCaptor<List<RecipeTag>> captor = ArgumentCaptor.forClass(List.class);
                    verify(recipeTagRepository).create(captor.capture());

                    List<RecipeTag> capturedTags = captor.getValue();
                    assertThat(capturedTags).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 태그 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID가 주어졌을 때")
        class GivenRecipeId {
            UUID recipeId;
            List<RecipeTag> mockTags;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                mockTags = List.of(createMockRecipeTag("한식", recipeId), createMockRecipeTag("매운맛", recipeId));
                doReturn(mockTags).when(recipeTagRepository).finds(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeTag> result;

                @BeforeEach
                void setUp() {
                    result = recipeTagService.gets(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 태그 목록을 반환한다")
                void thenReturnsTags() {
                    assertThat(result).hasSize(2);
                    assertThat(result).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛");
                    assertThat(result).allMatch(tag -> tag.getRecipeId().equals(recipeId));

                    verify(recipeTagRepository).finds(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentId {
            UUID nonExistentRecipeId;

            @BeforeEach
            void setUp() {
                nonExistentRecipeId = UUID.randomUUID();
                doReturn(Collections.emptyList()).when(recipeTagRepository).finds(nonExistentRecipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeTag> result;

                @BeforeEach
                void setUp() {
                    result = recipeTagService.gets(nonExistentRecipeId);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isEmpty();
                    verify(recipeTagRepository).finds(nonExistentRecipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 여러 레시피 ID가 주어졌을 때")
        class GivenMultipleIds {
            UUID recipeId1;
            UUID recipeId2;
            List<UUID> recipeIds;
            List<RecipeTag> mockTags;

            @BeforeEach
            void setUp() {
                recipeId1 = UUID.randomUUID();
                recipeId2 = UUID.randomUUID();
                recipeIds = List.of(recipeId1, recipeId2);

                mockTags = List.of(
                        createMockRecipeTag("한식", recipeId1),
                        createMockRecipeTag("매운맛", recipeId1),
                        createMockRecipeTag("양식", recipeId2));

                doReturn(mockTags).when(recipeTagRepository).finds(recipeIds);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeTag> result;

                @BeforeEach
                void setUp() {
                    result = recipeTagService.gets(recipeIds);
                }

                @Test
                @DisplayName("Then - 모든 레시피의 태그를 반환한다")
                void thenReturnsAllTags() {
                    assertThat(result).hasSize(3);
                    assertThat(result).extracting(RecipeTag::getRecipeId).containsOnly(recipeId1, recipeId2);
                    assertThat(result).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "양식");

                    verify(recipeTagRepository).finds(recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
        class GivenEmptyIds {
            List<UUID> emptyIds;

            @BeforeEach
            void setUp() {
                emptyIds = Collections.emptyList();
                doReturn(Collections.emptyList()).when(recipeTagRepository).finds(emptyIds);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeTag> result;

                @BeforeEach
                void setUp() {
                    result = recipeTagService.gets(emptyIds);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isEmpty();
                    verify(recipeTagRepository).finds(emptyIds);
                }
            }
        }
    }

    private RecipeTag createMockRecipeTag(String tagName, UUID recipeId) {
        RecipeTag tag = mock(RecipeTag.class);
        UUID tagId = UUID.randomUUID();

        doReturn(tagId).when(tag).getId();
        doReturn(tagName).when(tag).getTag();
        doReturn(recipeId).when(tag).getRecipeId();
        doReturn(fixedTime).when(tag).getCreatedAt();

        return tag;
    }
}
