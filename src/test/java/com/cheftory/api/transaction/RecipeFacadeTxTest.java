package com.cheftory.api.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.RecipeFacade;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
@DisplayName("RecipeFacade 트랜잭션 테스트")
class RecipeFacadeTxTest {

    @Autowired
    private RecipeFacade recipeFacade;

    @MockitoBean
    private RecipeInfoService recipeInfoService;

    @MockitoBean
    private RecipeYoutubeMetaService recipeYoutubeMetaService;

    @MockitoBean
    private RecipeBookmarkService recipeBookmarkService;

    @MockitoSpyBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        reset(recipeInfoService, recipeYoutubeMetaService, recipeBookmarkService, transactionManager);
    }

    @Test
    @DisplayName("blockRecipe: checked 예외 발생 시 rollback")
    void rollbacksOnCheckedException() throws RecipeInfoException {
        UUID recipeId = UUID.randomUUID();
        doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                .when(recipeInfoService)
                .block(recipeId);

        assertThatThrownBy(() -> recipeFacade.blockRecipe(recipeId)).isInstanceOf(RecipeException.class);
        verify(transactionManager).rollback(org.mockito.ArgumentMatchers.any());
        verify(transactionManager, never()).commit(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("blockRecipe: 예외 없으면 commit")
    void commitsOnSuccess() throws RecipeException {
        recipeFacade.blockRecipe(UUID.randomUUID());

        verify(transactionManager).commit(org.mockito.ArgumentMatchers.any());
        verify(transactionManager, never()).rollback(org.mockito.ArgumentMatchers.any());
    }
}
