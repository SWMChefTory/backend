package com.cheftory.api.recipe.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.recipe.category.repository.RecipeCategoryRepository;
import com.cheftory.api.recipe.category.repository.RecipeCategoryRepositoryImpl;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@DisplayName("RecipeCategoryRepository 테스트")
@Import({RecipeCategoryRepositoryImpl.class})
public class RecipeCategoryRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeCategoryRepository recipeCategoryRepository;

    @Mock
    private Clock clock;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("카테고리 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 카테고리가 주어졌을 때")
        class GivenValidCategory {
            UUID userId;
            String name;
            RecipeCategory category;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                name = "테스트 카테고리";
                category = RecipeCategory.create(clock, name, userId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                UUID categoryId;

                @BeforeEach
                void setUp() {
                    categoryId = recipeCategoryRepository.create(category);
                }

                @Test
                @DisplayName("Then - 카테고리가 생성된다")
                void thenCreated() {
                    assertThat(categoryId).isNotNull();
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 비어있을 때")
        class GivenEmptyName {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - NAME_EMPTY 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> RecipeCategory.create(clock, "", userId))
                            .isInstanceOf(RecipeCategoryException.class)
                            .extracting("error")
                            .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 공백일 때")
        class GivenBlankName {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - NAME_EMPTY 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> RecipeCategory.create(clock, "   ", userId))
                            .isInstanceOf(RecipeCategoryException.class)
                            .extracting("error")
                            .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 존재하는 카테고리가 있을 때")
        class GivenExistingCategory {
            UUID userId;
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId);
                categoryId = recipeCategoryRepository.create(category);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws RecipeCategoryException {
                    recipeCategoryRepository.delete(userId, categoryId);
                }

                @Test
                @DisplayName("Then - 카테고리가 삭제된다")
                void thenDeleted() {
                    assertThat(recipeCategoryRepository.exists(categoryId)).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 카테고리일 때")
        class GivenNonExistingCategory {
            UUID userId;
            UUID categoryId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeCategoryRepository.delete(userId, categoryId))
                            .isInstanceOf(RecipeCategoryException.class)
                            .extracting("error")
                            .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
                }
            }
        }

        @Nested
        @DisplayName("Given - 다른 사용자의 카테고리일 때")
        class GivenOtherUserCategory {
            UUID userId1;
            UUID userId2;
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId1 = UUID.randomUUID();
                userId2 = UUID.randomUUID();
                RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId1);
                categoryId = recipeCategoryRepository.create(category);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeCategoryRepository.delete(userId2, categoryId))
                            .isInstanceOf(RecipeCategoryException.class)
                            .extracting("error")
                            .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 삭제된 카테고리일 때")
        class GivenDeletedCategory {
            UUID userId;
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId);
                categoryId = recipeCategoryRepository.create(category);
                recipeCategoryRepository.delete(userId, categoryId);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeCategoryRepository.delete(userId, categoryId))
                            .isInstanceOf(RecipeCategoryException.class)
                            .extracting("error")
                            .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 사용자의 카테고리들이 있을 때")
        class GivenUserCategories {
            UUID userId;
            String name1;
            String name2;
            String name3;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                name1 = "카테고리1";
                name2 = "카테고리2";
                name3 = "카테고리3";

                recipeCategoryRepository.create(RecipeCategory.create(clock, name1, userId));
                recipeCategoryRepository.create(RecipeCategory.create(clock, name2, userId));
                recipeCategoryRepository.create(RecipeCategory.create(clock, name3, userId));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                java.util.List<RecipeCategory> result;

                @BeforeEach
                void setUp() {
                    result = recipeCategoryRepository.gets(userId);
                }

                @Test
                @DisplayName("Then - 활성 카테고리 목록을 반환한다")
                void thenReturnsActiveCategories() {
                    assertThat(result).hasSize(3);
                    assertThat(result).extracting("name").contains(name1, name2, name3);
                    assertThat(result).allMatch(c -> c.getStatus().equals(RecipeCategoryStatus.ACTIVE));
                }
            }
        }

        @Nested
        @DisplayName("Given - 삭제된 카테고리가 포함되어 있을 때")
        class GivenDeletedCategoriesIncluded {
            UUID userId;
            String name1;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                name1 = "카테고리1";
                String name2 = "카테고리2";

                recipeCategoryRepository.create(RecipeCategory.create(clock, name1, userId));
                UUID categoryId2 = recipeCategoryRepository.create(RecipeCategory.create(clock, name2, userId));
                recipeCategoryRepository.delete(userId, categoryId2);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                java.util.List<RecipeCategory> result;

                @BeforeEach
                void setUp() {
                    result = recipeCategoryRepository.gets(userId);
                }

                @Test
                @DisplayName("Then - 삭제된 카테고리는 제외된다")
                void thenExcludesDeleted() {
                    assertThat(result).hasSize(1);
                    assertThat(result.getFirst().getName()).isEqualTo(name1);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 존재 여부 확인 (exists)")
    class Exists {

        @Nested
        @DisplayName("Given - 존재하는 카테고리일 때")
        class GivenExistingCategory {
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                UUID userId = UUID.randomUUID();
                RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId);
                categoryId = recipeCategoryRepository.create(category);
            }

            @Nested
            @DisplayName("When - 확인을 요청하면")
            class WhenChecking {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = recipeCategoryRepository.exists(categoryId);
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() {
                    assertThat(result).isTrue();
                }
            }
        }

        @Nested
        @DisplayName("Given - 삭제된 카테고리일 때")
        class GivenDeletedCategory {
            UUID categoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                UUID userId = UUID.randomUUID();
                RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId);
                categoryId = recipeCategoryRepository.create(category);
                recipeCategoryRepository.delete(userId, categoryId);
            }

            @Nested
            @DisplayName("When - 확인을 요청하면")
            class WhenChecking {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = recipeCategoryRepository.exists(categoryId);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 카테고리일 때")
        class GivenNonExistingCategory {
            UUID categoryId;

            @BeforeEach
            void setUp() {
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 확인을 요청하면")
            class WhenChecking {
                boolean result;

                @BeforeEach
                void setUp() {
                    result = recipeCategoryRepository.exists(categoryId);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                }
            }
        }
    }
}
