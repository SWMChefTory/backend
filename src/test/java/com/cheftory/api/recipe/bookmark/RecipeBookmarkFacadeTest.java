package com.cheftory.api.recipe.bookmark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBookmarkFacade 테스트")
class RecipeBookmarkFacadeTest {

    private RecipeBookmarkService recipeBookmarkService;
    private RecipeInfoService recipeInfoService;
    private RecipeCreditPort creditPort;

    private RecipeBookmarkFacade sut;

    @BeforeEach
    void setUp() {
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        creditPort = mock(RecipeCreditPort.class);

        sut = new RecipeBookmarkFacade(recipeBookmarkService, recipeInfoService, creditPort);
    }

    @Nested
    @DisplayName("createAndCharge")
    class CreateAndCharge {

        @Test
        @DisplayName("북마크 생성 성공 시 credit을 차감한다")
        void shouldChargeWhenCreated() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            long creditCost = 100L;

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            doReturn(creditCost).when(recipeInfo).getCreditCost();
            doReturn(recipeInfo).when(recipeInfoService).get(recipeId);
            doReturn(true).when(recipeBookmarkService).create(userId, recipeId);

            boolean result = sut.createAndCharge(userId, recipeId);

            assertThat(result).isTrue();
            verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
        }

        @Test
        @DisplayName("북마크 생성 실패 시 credit을 차감하지 않는다")
        void shouldNotChargeWhenNotCreated() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            RecipeInfo recipeInfo = mock(RecipeInfo.class);
            doReturn(recipeInfo).when(recipeInfoService).get(recipeId);
            doReturn(false).when(recipeBookmarkService).create(userId, recipeId);

            boolean result = sut.createAndCharge(userId, recipeId);

            assertThat(result).isFalse();
            verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
        }

        @Test
        @DisplayName("레시피가 없으면 RECIPE_NOT_FOUND로 변환한다")
        void shouldMapRecipeNotFound() {
            UUID userId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();

            doThrow(new RecipeException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                    .when(recipeInfoService)
                    .get(recipeId);

            assertThatThrownBy(() -> sut.createAndCharge(userId, recipeId))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_NOT_FOUND);
        }
    }
}
