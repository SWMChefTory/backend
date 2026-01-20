package com.cheftory.api.category;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.RecipeCategoryRepository;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("RecipeCategory Repository")
public class RecipeInfoRecipeCategoryRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeCategoryRepository repository;

    @Nested
    @DisplayName("레시피 카테고리 저장")
    class SaveRecipeInfoRecipeCategory {

        @Nested
        @DisplayName("Given - 유효한 레시피 카테고리가 주어졌을 때")
        class GivenValidRecipeInfoRecipeCategory {

            private String categoryName;
            private UUID userId;
            private Clock clock;
            private RecipeCategory recipeCategory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                clock = new Clock();
                categoryName = "양식";
                recipeCategory = RecipeCategory.create(clock, categoryName, userId);
            }

            @Nested
            @DisplayName("When - 레시피 카테고리를 저장한다면")
            class WhenSavingRecipeInfoRecipeCategory {

                @BeforeEach
                void beforeEach() {
                    repository.save(recipeCategory);
                }

                @Test
                @DisplayName("Then - 레시피 카테고리가 저장되어야 한다")
                public void thenShouldPersistRecipeCategory() {
                    RecipeCategory recipeCategory = repository
                            .findById(GivenValidRecipeInfoRecipeCategory.this.recipeCategory.getId())
                            .orElseThrow();

                    assertThat(recipeCategory.getName()).isEqualTo("양식");
                    assertThat(recipeCategory.getUserId()).isEqualTo(userId);
                    assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.ACTIVE);
                    assertThat(recipeCategory.getCreatedAt()).isNotNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("사용자별 레시피 카테고리 조회")
    class FindRecipeInfoCategoriesByUser {

        @Nested
        @DisplayName("Given - 사용자의 활성 카테고리들이 존재할 때")
        class GivenUserActiveCategories {

            private UUID userId;
            private Clock clock;
            private List<RecipeCategory> recipeCategories;
            private List<String> categoryNames;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                clock = new Clock();
                categoryNames = List.of("양식", "한식", "디저트");
                recipeCategories = categoryNames.stream()
                        .map(name -> RecipeCategory.create(clock, name, userId))
                        .toList();
                repository.saveAll(recipeCategories);
            }

            @Nested
            @DisplayName("When - 사용자의 활성 카테고리를 조회한다면")
            class WhenFindingUserActiveCategories {

                @Test
                @DisplayName("Then - 해당 사용자의 활성 카테고리 목록을 반환해야 한다")
                public void thenShouldReturnUserActiveCategories() {
                    var categories = repository.findAllByUserIdAndStatus(userId, RecipeCategoryStatus.ACTIVE);

                    assertThat(categories)
                            .hasSize(3)
                            .extracting(RecipeCategory::getName)
                            .containsExactlyInAnyOrder("양식", "한식", "디저트");
                }
            }
        }

        @Nested
        @DisplayName("Given - 활성 카테고리와 삭제된 카테고리가 모두 존재할 때")
        class GivenMixedRecipeRecipeCategoryStatuses {

            private UUID userId;
            private Clock clock;
            private RecipeCategory deletedRecipeCategory;
            private RecipeCategory activeRecipeCategory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                clock = new Clock();

                activeRecipeCategory = RecipeCategory.create(clock, "활성카테고리", userId);
                deletedRecipeCategory = RecipeCategory.create(clock, "삭제카테고리", userId);
                deletedRecipeCategory.delete(); // 삭제 처리

                repository.saveAll(List.of(activeRecipeCategory, deletedRecipeCategory));
            }

            @Nested
            @DisplayName("When - 사용자의 활성 카테고리만 조회한다면")
            class WhenFindingOnlyActiveCategories {

                @Test
                @DisplayName("Then - 삭제된 카테고리는 제외하고 활성 카테고리만 반환해야 한다")
                public void thenShouldReturnOnlyActiveCategories() {
                    var categories = repository.findAllByUserIdAndStatus(userId, RecipeCategoryStatus.ACTIVE);

                    assertThat(categories)
                            .hasSize(1)
                            .extracting(RecipeCategory::getName)
                            .containsExactly("활성카테고리");
                }
            }
        }

        @Nested
        @DisplayName("Given - 다른 사용자의 카테고리들이 존재할 때")
        class GivenOtherUsersCategories {

            private UUID currentUserId;
            private UUID otherUserId;
            private Clock clock;

            @BeforeEach
            void setUp() {
                currentUserId = UUID.randomUUID();
                otherUserId = UUID.randomUUID();
                clock = new Clock();

                RecipeCategory currentUserRecipeCategory = RecipeCategory.create(clock, "내카테고리", currentUserId);

                RecipeCategory otherUserRecipeCategory = RecipeCategory.create(clock, "남의카테고리", otherUserId);

                repository.saveAll(List.of(currentUserRecipeCategory, otherUserRecipeCategory));
            }

            @Nested
            @DisplayName("When - 특정 사용자의 카테고리를 조회한다면")
            class WhenFindingSpecificUserCategories {

                @Test
                @DisplayName("Then - 해당 사용자의 카테고리만 반환해야 한다")
                public void thenShouldReturnOnlySpecificUserCategories() {
                    var categories = repository.findAllByUserIdAndStatus(currentUserId, RecipeCategoryStatus.ACTIVE);

                    assertThat(categories)
                            .hasSize(1)
                            .extracting(RecipeCategory::getName)
                            .containsExactly("내카테고리");
                }
            }
        }
    }
}
