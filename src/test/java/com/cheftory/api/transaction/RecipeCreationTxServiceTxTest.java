package com.cheftory.api.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.RecipeCreationTxService;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import java.net.URI;
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
@DisplayName("RecipeCreationTxService 트랜잭션 테스트")
class RecipeCreationTxServiceTxTest {

    @Autowired
    private RecipeCreationTxService recipeCreationTxService;

    @MockitoBean
    private RecipeInfoService recipeInfoService;

    @MockitoBean
    private RecipeIdentifyService recipeIdentifyService;

    @MockitoBean
    private RecipeYoutubeMetaService recipeYoutubeMetaService;

    @MockitoSpyBean(name = "transactionManager")
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        reset(recipeInfoService, recipeIdentifyService, recipeYoutubeMetaService, transactionManager);
    }

    @Test
    @DisplayName("createWithIdentifyWithVideoInfo: checked 예외 발생 시 rollback")
    void rollbacksOnCheckedException() throws RecipeIdentifyException {
        YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(recipeInfo).when(recipeInfoService).create();
        doReturn(UUID.randomUUID()).when(recipeInfo).getId();
        doReturn(URI.create("https://youtube.com/watch?v=test")).when(videoInfo).getVideoUri();
        doThrow(new RecipeIdentifyException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
                .when(recipeIdentifyService)
                .create(any(URI.class));

        assertThatThrownBy(() -> recipeCreationTxService.createWithIdentifyWithVideoInfo(videoInfo))
                .isInstanceOf(RecipeIdentifyException.class);
        verify(transactionManager).rollback(any());
        verify(transactionManager, never()).commit(any());
    }

    @Test
    @DisplayName("createWithIdentifyWithVideoInfo: 예외 없으면 commit")
    void commitsOnSuccess() throws RecipeIdentifyException {
        YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(recipeInfo).when(recipeInfoService).create();
        doReturn(UUID.randomUUID()).when(recipeInfo).getId();
        doReturn(URI.create("https://youtube.com/watch?v=test")).when(videoInfo).getVideoUri();

        RecipeInfo result = recipeCreationTxService.createWithIdentifyWithVideoInfo(videoInfo);

        assertThat(result).isEqualTo(recipeInfo);
        verify(transactionManager).commit(any());
        verify(transactionManager, never()).rollback(any());
    }
}
