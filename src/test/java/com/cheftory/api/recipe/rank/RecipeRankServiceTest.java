package com.cheftory.api.recipe.rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.region.Market;
import com.cheftory.api._common.region.MarketContext;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.recipe.dto.RecipeCuisineType;
import com.cheftory.api.recipe.rank.port.RecipeRankingPort;
import com.cheftory.api.recipe.rank.repository.RecipeRankRepository;
import com.cheftory.api.recipe.rank.repository.RecipeRankRepositoryImpl;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@DisplayName("RecipeRankService Tests")
public class RecipeRankServiceTest {

    private RecipeRankRepository repository;
    private RecipeRankingPort port;
    private Clock clock;
    private RecipeRankService service;
    private MockedStatic<MarketContext> marketContextMock;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeRankRepositoryImpl.class);
        port = mock(RecipeRankingPort.class);
        clock = mock(Clock.class);
        service = new RecipeRankService(repository, port, clock);

        marketContextMock = Mockito.mockStatic(MarketContext.class);
        MarketContext.Info info = new MarketContext.Info(Market.KOREA, "KR");
        marketContextMock.when(MarketContext::required).thenReturn(info);
    }

    @AfterEach
    void tearDown() {
        marketContextMock.close();
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
                LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                newKey = "korea:trendRecipe:ranking:20240101120000";
                latestKey = "korea:trendRecipe:latest";

                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 레시피 순위를 업데이트한다면")
            class WhenUpdatingRecipes {

                @Test
                @DisplayName("Then - 올바른 순서로 레시피 순위가 저장되어야 한다")
                void thenShouldSaveRecipeRankingsInCorrectOrder() {
                    service.updateRecipes(rankingType, recipeIds);

                    verify(repository).saveRanking(newKey, recipeIds.get(0), 1);
                    verify(repository).saveRanking(newKey, recipeIds.get(1), 2);
                    verify(repository).setExpire(eq(newKey), any(Duration.class));
                    verify(repository).saveLatest(latestKey, newKey);
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
                LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                newKey = "korea:chefRecipe:ranking:20240101120000";
                latestKey = "korea:chefRecipe:latest";

                doReturn(now).when(clock).now();
            }

            @Nested
            @DisplayName("When - 셰프 레시피 순위를 업데이트한다면")
            class WhenUpdatingChefRecipes {

                @Test
                @DisplayName("Then - 올바른 순서로 셰프 레시피 순위가 저장되어야 한다")
                void thenShouldSaveChefRecipeRankingsInCorrectOrder() {
                    service.updateRecipes(rankingType, recipeIds);

                    verify(repository).saveRanking(newKey, recipeIds.get(0), 1);
                    verify(repository).saveRanking(newKey, recipeIds.get(1), 2);
                    verify(repository).saveRanking(newKey, recipeIds.get(2), 3);
                    verify(repository).setExpire(eq(newKey), any(Duration.class));
                    verify(repository).saveLatest(latestKey, newKey);
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
            List<UUID> recipeIds = Stream.generate(UUID::randomUUID).limit(11).toList();

            CursorPage<UUID> expectedPage = CursorPage.of(recipeIds.subList(0, 10), "next-cursor");
            doReturn(expectedPage).when(repository).getRecipeIdsFirst(rankingType);

            CursorPage<UUID> result = service.getRecipeIds(rankingType, null);

            assertThat(result.items()).hasSize(10);
            assertThat(result.nextCursor()).isEqualTo("next-cursor");
            verify(repository).getRecipeIdsFirst(rankingType);
        }

        @Test
        @DisplayName("커서 기반 다음 페이지를 조회한다")
        void shouldGetRecipeIdsWithCursorNext() throws CheftoryException {
            RankingType rankingType = RankingType.CHEF;
            List<UUID> recipeIds = Stream.generate(UUID::randomUUID).limit(2).toList();

            CursorPage<UUID> expectedPage = CursorPage.of(recipeIds, null);
            doReturn(expectedPage).when(repository).getRecipeIds(rankingType, "cursor");

            CursorPage<UUID> result = service.getRecipeIds(rankingType, "cursor");

            assertThat(result.items()).hasSize(2);
            assertThat(result.nextCursor()).isNull();
            verify(repository).getRecipeIds(rankingType, "cursor");
        }
    }

    @Test
    @DisplayName("cuisine 추천은 추천 포트를 호출한다")
    void shouldRecommendCuisineRecipes() throws CheftoryException {
        UUID userId = UUID.randomUUID();
        String cursor = "cursor";
        CursorPage<UUID> expected = CursorPage.of(List.of(UUID.randomUUID()), "next-cursor");

        doReturn(expected).when(port).recommend(userId, RecipeCuisineType.KOREAN, cursor, 10);

        CursorPage<UUID> result = service.getCuisineRecipes(userId, RecipeCuisineType.KOREAN, cursor);

        assertThat(result).isEqualTo(expected);
        verify(port).recommend(userId, RecipeCuisineType.KOREAN, cursor, 10);
    }
}
