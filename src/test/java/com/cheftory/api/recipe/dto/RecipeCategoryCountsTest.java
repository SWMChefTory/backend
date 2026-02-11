package com.cheftory.api.recipe.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCategoryCounts 테스트")
class RecipeCategoryCountsTest {

    @Nested
    @DisplayName("of 팩토리 메서드")
    class Of {

        @Test
        @DisplayName("미분류 개수와 카테고리별 개수로 생성한다")
        void createsWithCounts() {
            Integer uncategorizedCount = 5;
            RecipeCategoryCount count1 = mockRecipeCategoryCount(3);
            RecipeCategoryCount count2 = mockRecipeCategoryCount(7);
            List<RecipeCategoryCount> categorizedCounts = List.of(count1, count2);

            RecipeCategoryCounts result = RecipeCategoryCounts.of(uncategorizedCount, categorizedCounts);

            assertThat(result.getUncategorizedCount()).isEqualTo(5);
            assertThat(result.getCategorizedCounts()).hasSize(2);
            assertThat(result.getTotalCount()).isEqualTo(15); // 5 + 3 + 7
        }

        @Test
        @DisplayName("빈 카테고리 목록으로 생성한다")
        void createsWithEmptyCategories() {
            Integer uncategorizedCount = 10;
            List<RecipeCategoryCount> categorizedCounts = List.of();

            RecipeCategoryCounts result = RecipeCategoryCounts.of(uncategorizedCount, categorizedCounts);

            assertThat(result.getUncategorizedCount()).isEqualTo(10);
            assertThat(result.getCategorizedCounts()).isEmpty();
            assertThat(result.getTotalCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("미분류가 0이고 카테고리만 있는 경우")
        void createsWithZeroUncategorized() {
            Integer uncategorizedCount = 0;
            RecipeCategoryCount count1 = mockRecipeCategoryCount(4);
            RecipeCategoryCount count2 = mockRecipeCategoryCount(6);
            List<RecipeCategoryCount> categorizedCounts = List.of(count1, count2);

            RecipeCategoryCounts result = RecipeCategoryCounts.of(uncategorizedCount, categorizedCounts);

            assertThat(result.getTotalCount()).isEqualTo(10);
        }

        private RecipeCategoryCount mockRecipeCategoryCount(int recipeCount) {
            RecipeCategoryCount count = mock(RecipeCategoryCount.class);
            doReturn(recipeCount).when(count).getRecipeCount();
            return count;
        }
    }
}
