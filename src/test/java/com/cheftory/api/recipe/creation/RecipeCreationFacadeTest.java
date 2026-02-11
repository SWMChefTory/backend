package com.cheftory.api.recipe.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;

@DisplayName("RecipeCreationFacade 테스트")
class RecipeCreationFacadeTest {

    private AsyncRecipeCreationService asyncRecipeCreationService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeProgressService recipeProgressService;
    private RecipeIdentifyService recipeIdentifyService;
    private RecipeInfoService recipeInfoService;
    private RecipeCreditPort creditPort;
    private RecipeCreationTxService recipeCreationTxService;

    private RecipeCreationFacade sut;

    @BeforeEach
    void setUp() {
        asyncRecipeCreationService = mock(AsyncRecipeCreationService.class);
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        recipeIdentifyService = mock(RecipeIdentifyService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        creditPort = mock(RecipeCreditPort.class);
        recipeCreationTxService = mock(RecipeCreationTxService.class);

        sut = new RecipeCreationFacade(
                asyncRecipeCreationService,
                recipeBookmarkService,
                recipeYoutubeMetaService,
                recipeProgressService,
                recipeIdentifyService,
                recipeInfoService,
                creditPort,
                recipeCreationTxService);
    }

    @Nested
    @DisplayName("북마크 생성 (createBookmark)")
    class CreateBookmark {

        @Nested
        @DisplayName("Given - 기존 레시피가 존재할 때")
        class GivenExistingRecipe {
            URI uri;
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeYoutubeMeta meta;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws YoutubeMetaException, RecipeInfoException {
                uri = URI.create("https://youtube.com/watch?v=test");
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 100L;

                meta = mockYoutubeMeta(recipeId);
                recipeInfo = mockRecipeInfo(recipeId, creditCost);

                doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
                doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
            }

            @Nested
            @DisplayName("When - 사용자 요청이고 북마크가 생성되면")
            class WhenUserRequestAndBookmarkCreated {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 크레딧을 차감하고 레시피 ID를 반환한다")
                void thenSpendsCreditAndReturnsId() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

                    assertThat(result).isEqualTo(recipeId);
                    verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
                    verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any(), any());
                }
            }

            @Nested
            @DisplayName("When - 사용자 요청이고 북마크가 생성되지 않으면")
            class WhenUserRequestAndBookmarkNotCreated {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    doReturn(false).when(recipeBookmarkService).create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 크레딧을 차감하지 않고 레시피 ID를 반환한다")
                void thenDoesNotSpendCreditAndReturnsId() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

                    assertThat(result).isEqualTo(recipeId);
                    verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
                    verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any(), any());
                }
            }

            @Nested
            @DisplayName("When - 크롤러 요청이면")
            class WhenCrawlerRequest {

                @Test
                @DisplayName("Then - 북마크/크레딧 처리 없이 레시피 ID를 반환한다")
                void thenReturnsIdWithoutSideEffects() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.Crawler(uri));

                    assertThat(result).isEqualTo(recipeId);
                    verify(recipeBookmarkService, never()).create(any(), any());
                    verify(creditPort, never()).spendRecipeCreate(any(), any(), anyLong());
                    verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any(), any());
                }
            }
        }

        @Nested
        @DisplayName("Given - 유튜브 메타가 차단된 경우")
        class GivenBannedMeta {
            URI uri;
            UUID userId;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                uri = URI.create("https://youtube.com/watch?v=banned");
                userId = UUID.randomUUID();
                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED))
                        .when(recipeYoutubeMetaService)
                        .getByUrl(uri);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - RECIPE_BANNED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.createBookmark(new RecipeCreationTarget.User(uri, userId)))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_BANNED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 유튜브 메타가 블락된 경우")
        class GivenBlockedMeta {
            URI uri;
            UUID userId;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                uri = URI.create("https://youtube.com/watch?v=blocked");
                userId = UUID.randomUUID();
                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED))
                        .when(recipeYoutubeMetaService)
                        .getByUrl(uri);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - RECIPE_CREATE_FAIL 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.createBookmark(new RecipeCreationTarget.User(uri, userId)))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);
                }
            }
        }

        @Nested
        @DisplayName("Given - 새 레시피를 생성해야 할 때")
        class GivenNewRecipe {
            URI uri;
            UUID userId;
            YoutubeVideoInfo videoInfo;
            UUID recipeId;
            long creditCost;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws YoutubeMetaException {
                uri = URI.create("https://youtube.com/watch?v=new");
                userId = UUID.randomUUID();
                videoInfo = mockVideoInfo("test_video_id");
                recipeId = UUID.randomUUID();
                creditCost = 77L;
                recipeInfo = mockRecipeInfo(recipeId, creditCost);

                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
                        .when(recipeYoutubeMetaService)
                        .getByUrl(uri);
                doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeIdentifyException, RecipeBookmarkException {
                    doReturn(recipeInfo).when(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
                    doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 새 레시피를 생성하고 비동기 작업을 시작한다")
                void thenCreatesAndStartsAsync() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

                    assertThat(result).isEqualTo(recipeId);
                    verify(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
                    verify(recipeBookmarkService).create(userId, recipeId);
                    verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
                    verify(asyncRecipeCreationService)
                            .create(recipeId, creditCost, videoInfo.getVideoId(), uri, videoInfo.getTitle());
                }
            }
        }

        @Nested
        @DisplayName("Given - 식별자 생성 중복 시")
        class GivenIdentifyProgressing {
            URI uri;
            UUID userId;
            YoutubeVideoInfo videoInfo;
            UUID recipeId;
            long creditCost;
            RecipeYoutubeMeta meta;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws YoutubeMetaException, RecipeIdentifyException, RecipeInfoException {
                uri = URI.create("https://youtube.com/watch?v=concurrent");
                userId = UUID.randomUUID();
                videoInfo = mockVideoInfo("test_video_id");
                recipeId = UUID.randomUUID();
                creditCost = 50L;
                meta = mockYoutubeMeta(recipeId);
                recipeInfo = mockRecipeInfo(recipeId, creditCost);

                doThrow(new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND))
                        .when(recipeYoutubeMetaService)
                        .getByUrl(uri);
                doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
                doThrow(new RecipeIdentifyException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
                        .when(recipeCreationTxService)
                        .createWithIdentifyWithVideoInfo(videoInfo);

                doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
                doReturn(recipeInfo).when(recipeInfoService).getSuccess(recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeBookmarkException {
                    doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 기존 레시피를 사용한다")
                void thenUsesExistingRecipe() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

                    assertThat(result).isEqualTo(recipeId);
                    verify(asyncRecipeCreationService, never()).create(any(), anyLong(), any(), any(), any());
                    verify(recipeBookmarkService).create(userId, recipeId);
                    verify(creditPort).spendRecipeCreate(userId, recipeId, creditCost);
                }
            }
        }

        @Nested
        @DisplayName("Given - 기존 레시피가 실패 상태일 때")
        class GivenFailedRecipe {
            URI uri;
            UUID userId;
            UUID recipeId;
            long creditCost;
            RecipeYoutubeMeta meta;
            YoutubeVideoInfo videoInfo;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws YoutubeMetaException, RecipeInfoException {
                uri = URI.create("https://youtube.com/watch?v=failed");
                userId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                creditCost = 77L;
                meta = mockYoutubeMeta(recipeId);
                videoInfo = mockVideoInfo("new_video_id");
                recipeInfo = mockRecipeInfo(recipeId, creditCost);

                doReturn(meta).when(recipeYoutubeMetaService).getByUrl(uri);
                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_FAILED))
                        .when(recipeInfoService)
                        .getSuccess(recipeId);
                doReturn(videoInfo).when(recipeYoutubeMetaService).getVideoInfo(uri);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeIdentifyException, RecipeBookmarkException {
                    doReturn(recipeInfo).when(recipeCreationTxService).createWithIdentifyWithVideoInfo(videoInfo);
                    doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                }

                @Test
                @DisplayName("Then - 새 레시피 생성을 진행한다")
                void thenCreatesNewRecipe() throws RecipeException, CreditException {
                    UUID result = sut.createBookmark(new RecipeCreationTarget.User(uri, userId));

                    assertThat(result).isEqualTo(recipeId);
                    verify(asyncRecipeCreationService)
                            .create(recipeId, creditCost, videoInfo.getVideoId(), uri, videoInfo.getTitle());
                }
            }
        }
    }

    private RecipeYoutubeMeta mockYoutubeMeta(UUID recipeId) {
        RecipeYoutubeMeta meta = mock(RecipeYoutubeMeta.class);
        doReturn(recipeId).when(meta).getRecipeId();
        return meta;
    }

    private RecipeInfo mockRecipeInfo(UUID recipeId, long creditCost) {
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(recipeId).when(recipeInfo).getId();
        doReturn(creditCost).when(recipeInfo).getCreditCost();
        return recipeInfo;
    }

    private YoutubeVideoInfo mockVideoInfo(String videoId) throws YoutubeMetaException {
        URI uri = UriComponentsBuilder.fromUriString("https://www.youtube.com/watch?v=" + videoId)
                .build()
                .toUri();
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        return YoutubeVideoInfo.from(
                youtubeUri,
                "테스트 요리 영상",
                "테스트 채널",
                URI.create("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg"),
                300,
                YoutubeMetaType.NORMAL);
    }
}
