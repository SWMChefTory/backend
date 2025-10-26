package com.cheftory.api.recipeinfo.rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipeinfo.rank.exception.RecipeRankException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeRankService Tests")
public class RecipeRankServiceTest {

  private RecipeRankRepository recipeRankRepository;
  private RankingKeyGenerator rankingKeyGenerator;
  private RecipeRankService recipeRankService;

  @BeforeEach
  void setUp() {
    recipeRankRepository = mock(RecipeRankRepository.class);
    rankingKeyGenerator = mock(RankingKeyGenerator.class);
    recipeRankService = new RecipeRankService(recipeRankRepository, rankingKeyGenerator);
  }

  @Nested
  @DisplayName("레시피 순위 업데이트")
  class UpdateRecipes {

    @Nested
    @DisplayName("Given - 유효한 랭킹 타입과 레시피 ID 목록이 주어졌을 때")
    class GivenValidRankingTypeAndRecipeIds {

      private RankingType rankingType;
      private List<UUID> recipeIds;
      private String newKey;
      private String latestKey;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        recipeIds = List.of(recipeId1, recipeId2);
        newKey = "trendRecipe:ranking:20240101120000";
        latestKey = "trendRecipe:latest";

        doReturn(newKey).when(rankingKeyGenerator).generateKey(rankingType);
        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
      }

      @Nested
      @DisplayName("When - 레시피 순위를 업데이트한다면")
      class WhenUpdatingRecipes {

        @Test
        @DisplayName("Then - 올바른 순서로 레시피 순위가 저장되어야 한다")
        void thenShouldSaveRecipeRankingsInCorrectOrder() {
          recipeRankService.updateRecipes(rankingType, recipeIds);

          // 첫 번째 레시피는 순위 1
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(0), 1);
          // 두 번째 레시피는 순위 2
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(1), 2);

          verify(recipeRankRepository).setExpire(newKey, 172800L);
          verify(recipeRankRepository).saveLatest(latestKey, newKey);
        }
      }
    }

    @Nested
    @DisplayName("Given - 셰프 랭킹 타입과 레시피 ID 목록이 주어졌을 때")
    class GivenChefRankingTypeAndRecipeIds {

      private RankingType rankingType;
      private List<UUID> recipeIds;
      private String newKey;
      private String latestKey;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.CHEF;
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        UUID recipeId3 = UUID.randomUUID();
        recipeIds = List.of(recipeId1, recipeId2, recipeId3);
        newKey = "chefRecipe:ranking:20240101120000";
        latestKey = "chefRecipe:latest";

        doReturn(newKey).when(rankingKeyGenerator).generateKey(rankingType);
        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
      }

      @Nested
      @DisplayName("When - 셰프 레시피 순위를 업데이트한다면")
      class WhenUpdatingChefRecipes {

        @Test
        @DisplayName("Then - 올바른 순서로 셰프 레시피 순위가 저장되어야 한다")
        void thenShouldSaveChefRecipeRankingsInCorrectOrder() {
          recipeRankService.updateRecipes(rankingType, recipeIds);

          // 각 레시피가 올바른 순위로 저장되는지 확인
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(0), 1);
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(1), 2);
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(2), 3);

          verify(recipeRankRepository).setExpire(newKey, 172800L);
          verify(recipeRankRepository).saveLatest(latestKey, newKey);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 ID 조회")
  class GetRecipeIds {

    @Nested
    @DisplayName("Given - 유효한 랭킹 타입과 페이지가 주어졌을 때")
    class GivenValidRankingTypeAndPage {

      private RankingType rankingType;
      private Integer page;
      private String latestKey;
      private Set<String> recipeIdStrings;
      private List<UUID> expectedRecipeIds;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        page = 0;
        latestKey = "trendRecipe:latest";
        String actualRankingKey = "trendRecipe:ranking:20240101120000";
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        expectedRecipeIds = List.of(recipeId1, recipeId2);
        recipeIdStrings = Set.of(recipeId1.toString(), recipeId2.toString());

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(recipeIdStrings).when(recipeRankRepository).findRecipeIds(actualRankingKey, 0L, 9L);
      }

      @Nested
      @DisplayName("When - 레시피 ID 목록을 조회한다면")
      class WhenGettingRecipeIds {

        @Test
        @DisplayName("Then - 해당 페이지의 레시피 ID 목록을 반환해야 한다")
        void thenShouldReturnRecipeIdsForPage() {
          List<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result).hasSize(2);
          assertThat(result).containsExactlyInAnyOrderElementsOf(expectedRecipeIds);
          verify(recipeRankRepository).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds("trendRecipe:ranking:20240101120000", 0L, 9L);
        }
      }
    }

    @Nested
    @DisplayName("Given - 두 번째 페이지가 주어졌을 때")
    class GivenSecondPage {

      private RankingType rankingType;
      private Integer page;
      private String latestKey;
      private Set<String> recipeIdStrings;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.CHEF;
        page = 1;
        latestKey = "chefRecipe:latest";
        String actualRankingKey = "chefRecipe:ranking:20240101120000";
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        recipeIdStrings = Set.of(recipeId1.toString(), recipeId2.toString());

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(recipeIdStrings).when(recipeRankRepository).findRecipeIds(actualRankingKey, 10L, 19L);
      }

      @Nested
      @DisplayName("When - 두 번째 페이지의 레시피 ID를 조회한다면")
      class WhenGettingSecondPageRecipeIds {

        @Test
        @DisplayName("Then - 올바른 오프셋으로 레시피 ID를 조회해야 한다")
        void thenShouldQueryWithCorrectOffset() {
          List<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result).hasSize(2);
          verify(recipeRankRepository).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds("chefRecipe:ranking:20240101120000", 10L, 19L);
        }
      }
    }

    @Nested
    @DisplayName("Given - 랭킹이 존재하지 않을 때")
    class GivenNonExistentRanking {

      private RankingType rankingType;
      private Integer page;
      private String latestKey;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        page = 0;
        latestKey = "trendRecipe:latest";

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.empty()).when(recipeRankRepository).findLatest(latestKey);
      }

      @Nested
      @DisplayName("When - 레시피 ID 목록을 조회한다면")
      class WhenGettingRecipeIds {

        @Test
        @DisplayName("Then - RecipeRankException을 던져야 한다")
        void thenShouldThrowRecipeRankException() {
          assertThatThrownBy(() -> recipeRankService.getRecipeIds(rankingType, page))
              .isInstanceOf(RecipeRankException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND);
        }
      }
    }

    @Nested
    @DisplayName("Given - 조회 결과가 빈 Set일 때")
    class GivenEmptyResults {

      private RankingType rankingType;
      private Integer page;
      private String latestKey;
      private String actualRankingKey;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        page = 0;
        latestKey = "trendRecipe:latest";
        actualRankingKey = "trendRecipe:ranking:20240101120000";

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(Set.of())
            .when(recipeRankRepository)
            .findRecipeIds(actualRankingKey, 0L, 9L);
      }

      @Nested
      @DisplayName("When - 레시피 ID 목록을 조회한다면")
      class WhenGettingRecipeIds {

        @Test
        @DisplayName("Then - 빈 목록을 반환해야 한다")
        void thenShouldReturnEmptyList() {
          List<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result).isEmpty();
          verify(recipeRankRepository).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds(actualRankingKey, 0L, 9L);
        }
      }
    }
  }

  @Nested
  @DisplayName("총 개수 조회")
  class GetTotalCount {

    @Nested
    @DisplayName("Given - 유효한 랭킹 타입이 주어졌을 때")
    class GivenValidRankingType {

      private RankingType rankingType;
      private String latestKey;
      private String actualKey;
      private Long expectedCount;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        latestKey = "trendRecipe:latest";
        actualKey = "trendRecipe:ranking:20240101120000";
        expectedCount = 25L;

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(expectedCount).when(recipeRankRepository).count(actualKey);
      }

      @Nested
      @DisplayName("When - 총 개수를 조회한다면")
      class WhenGettingTotalCount {

        @Test
        @DisplayName("Then - 해당 랭킹의 총 개수를 반환해야 한다")
        void thenShouldReturnTotalCount() {
          Long result = recipeRankService.getTotalCount(rankingType);

          assertThat(result).isEqualTo(expectedCount);
          verify(recipeRankRepository).findLatest(latestKey);
          verify(recipeRankRepository).count(actualKey);
        }
      }
    }

    @Nested
    @DisplayName("Given - 셰프 랭킹 타입이 주어졌을 때")
    class GivenChefRankingType {

      private RankingType rankingType;
      private String latestKey;
      private String actualKey;
      private Long expectedCount;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.CHEF;
        latestKey = "chefRecipe:latest";
        actualKey = "chefRecipe:ranking:20240101120000";
        expectedCount = 15L;

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(expectedCount).when(recipeRankRepository).count(actualKey);
      }

      @Nested
      @DisplayName("When - 셰프 랭킹의 총 개수를 조회한다면")
      class WhenGettingChefTotalCount {

        @Test
        @DisplayName("Then - 셰프 랭킹의 총 개수를 반환해야 한다")
        void thenShouldReturnChefTotalCount() {
          Long result = recipeRankService.getTotalCount(rankingType);

          assertThat(result).isEqualTo(expectedCount);
          verify(recipeRankRepository).findLatest(latestKey);
          verify(recipeRankRepository).count(actualKey);
        }
      }
    }

    @Nested
    @DisplayName("Given - 랭킹이 존재하지 않을 때")
    class GivenNonExistentRanking {

      private RankingType rankingType;
      private String latestKey;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        latestKey = "trendRecipe:latest";

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.empty()).when(recipeRankRepository).findLatest(latestKey);
      }

      @Nested
      @DisplayName("When - 총 개수를 조회한다면")
      class WhenGettingTotalCount {

        @Test
        @DisplayName("Then - RecipeRankException을 던져야 한다")
        void thenShouldThrowRecipeRankException() {
          assertThatThrownBy(() -> recipeRankService.getTotalCount(rankingType))
              .isInstanceOf(RecipeRankException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeRankErrorCode.RECIPE_RANK_NOT_FOUND);
        }
      }
    }
  }
}
