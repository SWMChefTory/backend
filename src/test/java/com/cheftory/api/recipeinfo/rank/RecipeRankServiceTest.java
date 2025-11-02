package com.cheftory.api.recipeinfo.rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipeinfo.rank.exception.RecipeRankErrorCode;
import com.cheftory.api.recipeinfo.rank.exception.RecipeRankException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

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

          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(0), 1);
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(1), 2);
          verify(recipeRankRepository).setExpire(newKey, Duration.ofDays(2));
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

          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(0), 1);
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(1), 2);
          verify(recipeRankRepository).saveRanking(newKey, recipeIds.get(2), 3);
          verify(recipeRankRepository).setExpire(newKey, Duration.ofDays(2));
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
      private String actualRankingKey;
      private Set<String> recipeIdStrings;
      private List<UUID> expectedRecipeIds;
      private Long totalCount;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        page = 0;
        latestKey = "trendRecipe:latest";
        actualRankingKey = "trendRecipe:ranking:20240101120000";
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        expectedRecipeIds = List.of(recipeId1, recipeId2);
        recipeIdStrings = Set.of(recipeId1.toString(), recipeId2.toString());
        totalCount = 25L;

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(recipeIdStrings)
            .when(recipeRankRepository)
            .findRecipeIds(actualRankingKey, 0L, 9L);
        doReturn(totalCount).when(recipeRankRepository).count(actualRankingKey);
      }

      @Nested
      @DisplayName("When - 레시피 ID 목록을 조회한다면")
      class WhenGettingRecipeIds {

        @Test
        @DisplayName("Then - 해당 페이지의 레시피 ID 페이지를 반환해야 한다")
        void thenShouldReturnRecipeIdsPage() {
          Page<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result.getContent()).hasSize(2);
          assertThat(result.getContent()).containsExactlyInAnyOrderElementsOf(expectedRecipeIds);
          assertThat(result.getTotalElements()).isEqualTo(totalCount);
          assertThat(result.getNumber()).isEqualTo(0);
          assertThat(result.getSize()).isEqualTo(10);
          verify(recipeRankRepository, times(2)).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds(actualRankingKey, 0L, 9L);
          verify(recipeRankRepository).count(actualRankingKey);
        }
      }
    }

    @Nested
    @DisplayName("Given - 두 번째 페이지가 주어졌을 때")
    class GivenSecondPage {

      private RankingType rankingType;
      private Integer page;
      private String latestKey;
      private String actualRankingKey;
      private Set<String> recipeIdStrings;
      private Long totalCount;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.CHEF;
        page = 1;
        latestKey = "chefRecipe:latest";
        actualRankingKey = "chefRecipe:ranking:20240101120000";
        UUID recipeId1 = UUID.randomUUID();
        UUID recipeId2 = UUID.randomUUID();
        recipeIdStrings = Set.of(recipeId1.toString(), recipeId2.toString());
        totalCount = 12L;

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(recipeIdStrings)
            .when(recipeRankRepository)
            .findRecipeIds(actualRankingKey, 10L, 19L);
        doReturn(totalCount).when(recipeRankRepository).count(actualRankingKey);
      }

      @Nested
      @DisplayName("When - 두 번째 페이지의 레시피 ID를 조회한다면")
      class WhenGettingSecondPageRecipeIds {

        @Test
        @DisplayName("Then - 올바른 오프셋으로 레시피 ID 페이지를 조회해야 한다")
        void thenShouldQueryWithCorrectOffset() {
          Page<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result.getContent()).hasSize(2);
          assertThat(result.getNumber()).isEqualTo(1);
          assertThat(result.getTotalElements()).isEqualTo(totalCount);
          verify(recipeRankRepository, times(2)).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds(actualRankingKey, 10L, 19L);
          verify(recipeRankRepository).count(actualRankingKey);
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
      private Long totalCount;

      @BeforeEach
      void setUp() {
        rankingType = RankingType.TRENDING;
        page = 0;
        latestKey = "trendRecipe:latest";
        actualRankingKey = "trendRecipe:ranking:20240101120000";
        totalCount = 0L;

        doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
        doReturn(Optional.of(actualRankingKey)).when(recipeRankRepository).findLatest(latestKey);
        doReturn(Set.of()).when(recipeRankRepository).findRecipeIds(actualRankingKey, 0L, 9L);
        doReturn(totalCount).when(recipeRankRepository).count(actualRankingKey);
      }

      @Nested
      @DisplayName("When - 레시피 ID 목록을 조회한다면")
      class WhenGettingRecipeIds {

        @Test
        @DisplayName("Then - 빈 페이지를 반환해야 한다")
        void thenShouldReturnEmptyPage() {
          Page<UUID> result = recipeRankService.getRecipeIds(rankingType, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(recipeRankRepository, times(2)).findLatest(latestKey);
          verify(recipeRankRepository).findRecipeIds(actualRankingKey, 0L, 9L);
          verify(recipeRankRepository).count(actualRankingKey);
        }
      }
    }
  }
}
