package com.cheftory.api.recipe.rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.RankCursor;
import com.cheftory.api._common.cursor.RankCursorCodec;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.ranking.RankingItemType;
import com.cheftory.api.ranking.RankingSurfaceType;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeRankService Tests")
public class RecipeRankServiceTest {

    private RecipeRankRepository recipeRankRepository;
    private RankingKeyGenerator rankingKeyGenerator;
    private RankCursorCodec rankCursorCodec;
    private RecipeRankingPort recipeRankingPort;
    private RecipeRankService recipeRankService;

    @BeforeEach
    void setUp() {
        recipeRankRepository = mock(RecipeRankRepository.class);
        rankingKeyGenerator = mock(RankingKeyGenerator.class);
        rankCursorCodec = mock(RankCursorCodec.class);
        recipeRankingPort = mock(RecipeRankingPort.class);
        recipeRankService =
                new RecipeRankService(recipeRankRepository, rankingKeyGenerator, rankCursorCodec, recipeRankingPort);
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

        @Test
        @DisplayName("커서 기반 첫 페이지를 조회한다")
        void shouldGetRecipeIdsWithCursorFirst() throws CheftoryException {
            RankingType rankingType = RankingType.TRENDING;
            String latestKey = "trendRecipe:latest";
            String rankingKey = "trendRecipe:ranking:20240101120000";
            List<String> recipeIds = Stream.of(
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            UUID.randomUUID())
                    .map(UUID::toString)
                    .toList();

            doReturn(latestKey).when(rankingKeyGenerator).getLatestKey(rankingType);
            doReturn(Optional.of(rankingKey)).when(recipeRankRepository).findLatest(latestKey);
            doReturn(recipeIds).when(recipeRankRepository).findRecipeIdsByRank(rankingKey, 1, 11);
            doReturn("next-cursor").when(rankCursorCodec).encode(any(RankCursor.class));

            CursorPage<UUID> result = recipeRankService.getRecipeIds(rankingType, null);

            assertThat(result.items()).hasSize(10);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");
            verify(recipeRankRepository).findRecipeIdsByRank(rankingKey, 1, 11);
        }

        @Test
        @DisplayName("커서 기반 다음 페이지를 조회한다")
        void shouldGetRecipeIdsWithCursorNext() throws CheftoryException {
            RankingType rankingType = RankingType.CHEF;
            RankCursor decoded = new RankCursor("chefRecipe:ranking:20240101120000", 10);

            doReturn(decoded).when(rankCursorCodec).decode("cursor");
            doReturn(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                    .when(recipeRankRepository)
                    .findRecipeIdsByRank(decoded.rankingKey(), 11, 11);

            CursorPage<UUID> result = recipeRankService.getRecipeIds(rankingType, "cursor");

            assertThat(result.items()).hasSize(2);
            assertThat(result.nextCursor()).isNull();
            verify(recipeRankRepository).findRecipeIdsByRank(decoded.rankingKey(), 11, 11);
        }
    }

    @Test
    @DisplayName("cuisine 추천은 추천 포트를 호출한다")
    void shouldRecommendCuisineRecipes() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor";
        CursorPage<UUID> expected = CursorPage.of(List.of(UUID.randomUUID()), "next-cursor");

        doReturn(expected)
                .when(recipeRankingPort)
                .recommend(userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, cursor, 10);

        CursorPage<UUID> result = recipeRankService.getCuisineRecipes(userId, RecipeCuisineType.KOREAN, cursor);

        assertThat(result).isEqualTo(expected);
        verify(recipeRankingPort)
                .recommend(userId, RankingSurfaceType.CUISINE_KOREAN, RankingItemType.RECIPE, cursor, 10);
    }
}
