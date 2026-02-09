package com.cheftory.api.recipe.rank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import java.util.List;
import java.util.UUID;

import com.cheftory.api.exception.CheftoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeRankController Tests")
public class RecipeRankControllerTest {

    private RecipeRankService recipeRankService;
    private RecipeRankController recipeRankController;

    @BeforeEach
    void setUp() {
        recipeRankService = mock(RecipeRankService.class);
        recipeRankController = new RecipeRankController(recipeRankService);
    }

    @Nested
    @DisplayName("트렌드 레시피 업데이트")
    class UpdateTrendingRecipes {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID 목록이 주어졌을 때")
        class GivenValidRecipeIds {

            private List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();
                recipeIds = List.of(recipeId1, recipeId2);
            }

            @Nested
            @DisplayName("When - 트렌드 레시피를 업데이트한다면")
            class WhenUpdatingTrendingRecipes {

                @Test
                @DisplayName("Then - 성공 응답을 반환하고 RecipeRankService를 호출해야 한다")
                void thenShouldReturnSuccessResponseAndCallService() throws CheftoryException {
                    SuccessOnlyResponse result = recipeRankController.updateTrendingRecipes(recipeIds);

                    assertThat(result).isNotNull();
                    verify(recipeRankService).updateRecipes(RankingType.TRENDING, recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
        class GivenEmptyRecipeIds {

            private List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                recipeIds = List.of();
            }

            @Nested
            @DisplayName("When - 트렌드 레시피를 업데이트한다면")
            class WhenUpdatingTrendingRecipes {

                @Test
                @DisplayName("Then - 성공 응답을 반환하고 RecipeRankService를 호출해야 한다")
                void thenShouldReturnSuccessResponseAndCallService() throws CheftoryException {
                    SuccessOnlyResponse result = recipeRankController.updateTrendingRecipes(recipeIds);

                    assertThat(result).isNotNull();
                    verify(recipeRankService).updateRecipes(RankingType.TRENDING, recipeIds);
                }
            }
        }
    }

    @Nested
    @DisplayName("셰프 레시피 업데이트")
    class UpdateChefRecipes {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID 목록이 주어졌을 때")
        class GivenValidRecipeIds {

            private List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();
                UUID recipeId3 = UUID.randomUUID();
                recipeIds = List.of(recipeId1, recipeId2, recipeId3);
            }

            @Nested
            @DisplayName("When - 셰프 레시피를 업데이트한다면")
            class WhenUpdatingChefRecipes {

                @Test
                @DisplayName("Then - 성공 응답을 반환하고 RecipeRankService를 호출해야 한다")
                void thenShouldReturnSuccessResponseAndCallService() throws CheftoryException {
                    SuccessOnlyResponse result = recipeRankController.updateChefRecipes(recipeIds);

                    assertThat(result).isNotNull();
                    verify(recipeRankService).updateRecipes(RankingType.CHEF, recipeIds);
                }
            }
        }

        @Nested
        @DisplayName("Given - 단일 레시피 ID가 주어졌을 때")
        class GivenSingleRecipeId {

            private List<UUID> recipeIds;

            @BeforeEach
            void setUp() {
                UUID recipeId = UUID.randomUUID();
                recipeIds = List.of(recipeId);
            }

            @Nested
            @DisplayName("When - 셰프 레시피를 업데이트한다면")
            class WhenUpdatingChefRecipes {

                @Test
                @DisplayName("Then - 성공 응답을 반환하고 RecipeRankService를 호출해야 한다")
                void thenShouldReturnSuccessResponseAndCallService() throws CheftoryException {
                    SuccessOnlyResponse result = recipeRankController.updateChefRecipes(recipeIds);

                    assertThat(result).isNotNull();
                    verify(recipeRankService).updateRecipes(RankingType.CHEF, recipeIds);
                }
            }
        }
    }
}
