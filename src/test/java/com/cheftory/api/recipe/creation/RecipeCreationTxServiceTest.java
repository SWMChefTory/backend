package com.cheftory.api.recipe.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationTxService 테스트")
class RecipeCreationTxServiceTest {

    private RecipeInfoService recipeInfoService;
    private RecipeIdentifyService recipeIdentifyService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeCreationTxService service;

    @BeforeEach
    void setUp() {
        recipeInfoService = mock(RecipeInfoService.class);
        recipeIdentifyService = mock(RecipeIdentifyService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        service = new RecipeCreationTxService(recipeInfoService, recipeIdentifyService, recipeYoutubeMetaService);
    }

    @Nested
    @DisplayName("YouTube 비디오 정보로 레시피 생성 (createWithIdentifyWithVideoInfo)")
    class CreateWithIdentifyWithVideoInfo {

        @Nested
        @DisplayName("Given - 유효한 YouTube 비디오 정보가 주어졌을 때")
        class GivenValidVideoInfo {
            YoutubeVideoInfo videoInfo;
            UUID recipeId;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                videoInfo = mock(YoutubeVideoInfo.class);
                recipeId = UUID.randomUUID();
                recipeInfo = mock(RecipeInfo.class);

                doReturn(recipeId).when(recipeInfo).getId();
                doReturn(recipeInfo).when(recipeInfoService).create();
                doReturn(URI.create("https://youtube.com/watch?v=test"))
                        .when(videoInfo)
                        .getVideoUri();
            }

            @Nested
            @DisplayName("When - 레시피 생성을 요청하면")
            class WhenCreating {
                RecipeInfo result;

                @BeforeEach
                void setUp() throws RecipeIdentifyException {
                    result = service.createWithIdentifyWithVideoInfo(videoInfo);
                }

                @Test
                @DisplayName("Then - 레시피 정보, 식별자, YouTube 메타데이터를 생성한다")
                void thenCreatesAllComponents() throws RecipeIdentifyException {
                    assertThat(result).isEqualTo(recipeInfo);
                    verify(recipeInfoService).create();
                    verify(recipeIdentifyService).create(videoInfo.getVideoUri());
                    verify(recipeYoutubeMetaService).create(videoInfo, recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 식별자 생성 실패 시")
        class GivenIdentifyCreationFails {
            YoutubeVideoInfo videoInfo;
            RecipeIdentifyException exception;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                videoInfo = mock(YoutubeVideoInfo.class);
                exception = mock(RecipeIdentifyException.class);

                doReturn(mock(RecipeInfo.class)).when(recipeInfoService).create();
                doReturn(URI.create("https://youtube.com/watch?v=test"))
                        .when(videoInfo)
                        .getVideoUri();
                doThrow(exception).when(recipeIdentifyService).create(any(URI.class));
            }

            @Nested
            @DisplayName("When - 레시피 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() {
                    RecipeIdentifyException thrown = assertThrows(
                            RecipeIdentifyException.class, () -> service.createWithIdentifyWithVideoInfo(videoInfo));
                    assertSame(exception, thrown);
                }
            }
        }
    }
}
