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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DisplayName("RecipeCategoryRepository Tests")
@DataJpaTest
@Import({RecipeCategoryRepositoryImpl.class})
public class RecipeCategoryRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeCategoryRepository recipeCategoryRepository;

    @Mock
    private Clock clock;

    private LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("카테고리 생성")
    class CreateRecipeCategory {

        @Test
        @DisplayName("카테고리를 생성한다")
        void shouldCreateRecipeCategory() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name = "테스트 카테고리";

            RecipeCategory category = RecipeCategory.create(clock, name, userId);
            UUID categoryId = recipeCategoryRepository.create(category);

            assertThat(categoryId).isNotNull();
        }

        @Test
        @DisplayName("이름이 비어있으면 예외를 던진다")
        void shouldThrowExceptionWhenNameEmpty() {
            UUID userId = UUID.randomUUID();

            assertThatThrownBy(() -> RecipeCategory.create(clock, "", userId))
                    .isInstanceOf(RecipeCategoryException.class)
                    .extracting("error")
                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
        }

        @Test
        @DisplayName("이름이 공백이면 예외를 던진다")
        void shouldThrowExceptionWhenNameBlank() {
            UUID userId = UUID.randomUUID();

            assertThatThrownBy(() -> RecipeCategory.create(clock, "   ", userId))
                    .isInstanceOf(RecipeCategoryException.class)
                    .extracting("error")
                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteRecipeCategory {

        @Test
        @DisplayName("카테고리를 삭제한다")
        void shouldDeleteRecipeCategory() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name = "테스트 카테고리";

            RecipeCategory category = RecipeCategory.create(clock, name, userId);
            UUID categoryId = recipeCategoryRepository.create(category);

            recipeCategoryRepository.delete(userId, categoryId);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 삭제 시 예외를 던진다")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UUID userId = UUID.randomUUID();
            UUID categoryId = UUID.randomUUID();

            assertThatThrownBy(() -> recipeCategoryRepository.delete(userId, categoryId))
                    .isInstanceOf(RecipeCategoryException.class)
                    .extracting("error")
                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("다른 사용자의 카테고리 삭제 시 예외를 던진다")
        void shouldThrowExceptionWhenNotOwner() throws RecipeCategoryException {
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            RecipeCategory category = RecipeCategory.create(clock, "테스트 카테고리", userId1);
            UUID categoryId = recipeCategoryRepository.create(category);

            assertThatThrownBy(() -> recipeCategoryRepository.delete(userId2, categoryId))
                    .isInstanceOf(RecipeCategoryException.class)
                    .extracting("error")
                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 삭제된 카테고리 삭제 시 예외를 던진다")
        void shouldThrowExceptionWhenAlreadyDeleted() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name = "테스트 카테고리";

            RecipeCategory category = RecipeCategory.create(clock, name, userId);
            UUID categoryId = recipeCategoryRepository.create(category);

            recipeCategoryRepository.delete(userId, categoryId);

            assertThatThrownBy(() -> recipeCategoryRepository.delete(userId, categoryId))
                    .isInstanceOf(RecipeCategoryException.class)
                    .extracting("error")
                    .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("카테고리 조회")
    class GetsRecipeCategory {

        @Test
        @DisplayName("사용자의 활성 카테고리 목록을 조회한다")
        void shouldGetUserCategories() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name1 = "카테고리1";
            String name2 = "카테고리2";
            String name3 = "카테고리3";

            RecipeCategory category1 = RecipeCategory.create(clock, name1, userId);
            recipeCategoryRepository.create(category1);
            RecipeCategory category2 = RecipeCategory.create(clock, name2, userId);
            recipeCategoryRepository.create(category2);
            RecipeCategory category3 = RecipeCategory.create(clock, name3, userId);
            recipeCategoryRepository.create(category3);

            var result = recipeCategoryRepository.gets(userId);

            assertThat(result).hasSize(3);
            assertThat(result).extracting("name").contains(name1, name2, name3);
            assertThat(result).allMatch(c -> c.getStatus().equals(RecipeCategoryStatus.ACTIVE));
        }

        @Test
        @DisplayName("삭제된 카테고리는 목록에서 제외된다")
        void shouldExcludeDeletedCategories() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name1 = "카테고리1";
            String name2 = "카테고리2";

            RecipeCategory category1 = RecipeCategory.create(clock, name1, userId);
            RecipeCategory category2 = RecipeCategory.create(clock, name2, userId);

            recipeCategoryRepository.create(category1);
            UUID categoryId2 = recipeCategoryRepository.create(category2);

            recipeCategoryRepository.delete(userId, categoryId2);

            var result = recipeCategoryRepository.gets(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo(name1);
        }
    }

    @Nested
    @DisplayName("카테고리 존재 여부 확인")
    class ExistsRecipeCategory {

        @Test
        @DisplayName("카테고리가 존재하면 true를 반환한다")
        void shouldReturnTrueWhenCategoryExists() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name = "테스트 카테고리";

            RecipeCategory category = RecipeCategory.create(clock, name, userId);
            UUID categoryId = recipeCategoryRepository.create(category);

            boolean result = recipeCategoryRepository.exists(categoryId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("삭제된 카테고리는 존재하지 않는 것으로 간주한다")
        void shouldReturnFalseWhenCategoryDeleted() throws RecipeCategoryException {
            UUID userId = UUID.randomUUID();
            String name = "테스트 카테고리";

            RecipeCategory category = RecipeCategory.create(clock, name, userId);
            UUID categoryId = recipeCategoryRepository.create(category);

            recipeCategoryRepository.delete(userId, categoryId);

            boolean result = recipeCategoryRepository.exists(categoryId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("카테고리가 존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenCategoryNotExists() {
            UUID categoryId = UUID.randomUUID();

            boolean result = recipeCategoryRepository.exists(categoryId);

            assertThat(result).isFalse();
        }
    }
}
