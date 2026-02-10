package com.cheftory.api.recipe.content.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.tag.repository.RecipeTagRepository;
import com.cheftory.api.recipe.content.tag.repository.RecipeTagRepositoryImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeTagRepository 테스트")
@Import(RecipeTagRepositoryImpl.class)
public class RecipeTagRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeTagRepository recipeTagRepository;

    @MockitoBean
    private Clock clock;

    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        doReturn(FIXED_TIME).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 태그 저장 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 레시피 태그가 주어졌을 때")
        class GivenValidTag {
            String tag;
            UUID recipeId;
            RecipeTag recipeTag;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                tag = "한식";
                recipeTag = RecipeTag.create(tag, recipeId, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {

                @BeforeEach
                void setUp() {
                    recipeTagRepository.create(List.of(recipeTag));
                }

                @Test
                @DisplayName("Then - 레시피 태그를 저장한다")
                void thenSavesTag() {
                    List<RecipeTag> savedTags = recipeTagRepository.finds(recipeId);

                    assertThat(savedTags).hasSize(1);
                    RecipeTag savedTag = savedTags.getFirst();
                    assertThat(savedTag.getTag()).isEqualTo("한식");
                    assertThat(savedTag.getRecipeId()).isEqualTo(recipeId);
                    assertThat(savedTag.getCreatedAt()).isEqualTo(FIXED_TIME);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 태그 목록이 주어졌을 때")
        class GivenEmptyList {

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {

                @Test
                @DisplayName("Then - 예외 없이 처리된다")
                void thenHandlesGracefully() {
                    recipeTagRepository.create(Collections.emptyList());
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 태그 조회 (finds)")
    class Finds {

        @Nested
        @DisplayName("Given - 태그가 저장되어 있을 때")
        class GivenSavedTags {
            UUID recipeId;
            RecipeTag tag1;
            RecipeTag tag2;
            RecipeTag tag3;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                tag1 = RecipeTag.create("한식", recipeId, clock);
                tag2 = RecipeTag.create("매운맛", recipeId, clock);
                tag3 = RecipeTag.create("간단요리", recipeId, clock);
                recipeTagRepository.create(List.of(tag1, tag2, tag3));
            }

            @Nested
            @DisplayName("When - 레시피 ID로 조회하면")
            class WhenFindingByRecipeId {
                List<RecipeTag> tags;

                @BeforeEach
                void setUp() {
                    tags = recipeTagRepository.finds(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 모든 태그를 반환한다")
                void thenReturnsAllTags() {
                    assertThat(tags).hasSize(3);
                    assertThat(tags).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
                }
            }

            @Nested
            @DisplayName("When - 레시피 ID 목록으로 조회하면")
            class WhenFindingByRecipeIds {
                List<RecipeTag> tags;

                @BeforeEach
                void setUp() {
                    tags = recipeTagRepository.finds(List.of(recipeId));
                }

                @Test
                @DisplayName("Then - 해당 레시피들의 모든 태그를 반환한다")
                void thenReturnsAllTags() {
                    assertThat(tags).hasSize(3);
                    assertThat(tags).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentId {
            UUID nonExistentId;

            @BeforeEach
            void setUp() {
                nonExistentId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFinding {
                List<RecipeTag> tags;

                @BeforeEach
                void setUp() {
                    tags = recipeTagRepository.finds(nonExistentId);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(tags).isEmpty();
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
        class GivenEmptyIdList {

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenFinding {
                List<RecipeTag> tags;

                @BeforeEach
                void setUp() {
                    tags = recipeTagRepository.finds(Collections.emptyList());
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(tags).isEmpty();
                }
            }
        }
    }
}
