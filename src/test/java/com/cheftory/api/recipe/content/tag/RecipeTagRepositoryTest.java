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

@DisplayName("RecipeTag Repository")
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
    @DisplayName("레시피 태그 저장")
    class SaveRecipeTag {

        @Nested
        @DisplayName("Given - 유효한 레시피 태그가 주어졌을 때")
        class GivenValidRecipeTag {
            private String tag;
            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                tag = "한식";
                doReturn(FIXED_TIME).when(clock).now();
            }

            @Nested
            @DisplayName("When - 레시피 태그를 저장한다면")
            class WhenSavingRecipeTag {

                private RecipeTag recipeTag;

                @BeforeEach
                void beforeEach() {
                    recipeTag = RecipeTag.create(tag, recipeId, clock);
                    recipeTagRepository.create(List.of(recipeTag));
                }

                @DisplayName("Then - 레시피 태그가 저장되어야 한다")
                @Test
                public void thenShouldPersistRecipeTag() {
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
        class GivenEmptyTagList {
            @Test
            @DisplayName("When - 저장을 시도하면 예외 없이 처리되어야 한다")
            void shouldHandleEmptyListGracefully() {
                recipeTagRepository.create(Collections.emptyList());
                // No exception should be thrown
            }
        }
    }

    @Nested
    @DisplayName("특정 레시피의 태그들 조회")
    class FindRecipeTags {
        @Nested
        @DisplayName("Given - 특정 레시피에 태그들이 존재할 때")
        class GivenRecipeWithTags {
            private UUID recipeId;
            private RecipeTag tag1;
            private RecipeTag tag2;
            private RecipeTag tag3;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                tag1 = RecipeTag.create("한식", recipeId, clock);
                tag2 = RecipeTag.create("매운맛", recipeId, clock);
                tag3 = RecipeTag.create("간단요리", recipeId, clock);
                recipeTagRepository.create(List.of(tag1, tag2, tag3));
            }

            @Nested
            @DisplayName("When - 해당 레시피의 태그들을 조회하면")
            class WhenFindingTagsByRecipeId {

                @DisplayName("Then - 모든 태그들이 조회되어야 한다")
                @Test
                public void thenShouldReturnAllTagsForRecipe() {
                    var tags = recipeTagRepository.finds(recipeId);
                    assertThat(tags).hasSize(3);
                    assertThat(tags).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
                }
            }

            @Nested
            @DisplayName("When - 레시피 IDs로 태그를 조회하면")
            class WhenFindingTagsByRecipeIds {

                @DisplayName("Then - 모든 태그들이 조회되어야 한다")
                @Test
                public void thenShouldReturnAllTagsForRecipe() {
                    var tags = recipeTagRepository.finds(List.of(recipeId));
                    assertThat(tags).hasSize(3);
                    assertThat(tags).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "간단요리");
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentRecipeId {
            @Test
            @DisplayName("When - 태그를 조회하면 빈 목록을 반환해야 한다")
            void shouldReturnEmptyList() {
                UUID nonExistentId = UUID.randomUUID();
                List<RecipeTag> tags = recipeTagRepository.finds(nonExistentId);
                assertThat(tags).isEmpty();
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
        class GivenEmptyRecipeIdList {
            @Test
            @DisplayName("When - 태그를 조회하면 빈 목록을 반환해야 한다")
            void shouldReturnEmptyList() {
                List<RecipeTag> tags = recipeTagRepository.finds(Collections.emptyList());
                assertThat(tags).isEmpty();
            }
        }
    }
}
