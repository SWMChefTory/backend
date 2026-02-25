package com.cheftory.api.recipe.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeSourceType;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.creation.credit.RecipeCreditException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.dto.RecipeCreationTarget;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationFacade 테스트")
class RecipeCreationFacadeTest {

    private AsyncRecipeCreationService asyncRecipeCreationService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeInfoService recipeInfoService;
    private RecipeCreditPort creditPort;
    private RecipeCreationFacade sut;

    @BeforeEach
    void setUp() {
        asyncRecipeCreationService = mock(AsyncRecipeCreationService.class);
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        creditPort = mock(RecipeCreditPort.class);
        sut = new RecipeCreationFacade(
                asyncRecipeCreationService,
                recipeBookmarkService,
                recipeYoutubeMetaService,
                recipeInfoService,
                creditPort);
    }

    @Nested
    @DisplayName("create()")
    class Create {
        URI uri;
        String videoId;
        UUID userId;
        RecipeCreationTarget.User userTarget;

        @BeforeEach
        void setUp() throws Exception {
            uri = URI.create("https://www.youtube.com/watch?v=abc123");
            videoId = "abc123";
            userId = UUID.randomUUID();
            userTarget = new RecipeCreationTarget.User(uri, userId);
            doReturn(videoId).when(recipeYoutubeMetaService).getVideoId(uri);
        }

        @Nested
        @DisplayName("Given - 기존 SUCCESS 레시피가 있으면")
        class GivenExistingSuccess {
            UUID recipeId;
            RecipeInfo recipeInfo;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                recipeInfo = mockRecipe(recipeId, RecipeStatus.SUCCESS, 3L, UUID.randomUUID());
                doReturn(recipeInfo).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - 기존 레시피에 합류하고 북마크/과금을 수행한다")
            void thenJoinAndBookmark() throws Exception {
                UUID result = sut.create(userTarget);

                assertThat(result).isEqualTo(recipeId);
                verify(asyncRecipeCreationService, never())
                        .create(eq(recipeId), anyLong(), eq(videoId), org.mockito.ArgumentMatchers.any());
                verify(creditPort).spendRecipeCreate(userId, recipeId, 3L);
            }
        }

        @Nested
        @DisplayName("Given - 레시피가 없으면")
        class GivenNotFound {
            UUID recipeId;
            UUID jobId;
            RecipeInfo created;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                created = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 5L, jobId);

                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                        .when(recipeInfoService)
                        .getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(created).when(recipeInfoService).create(RecipeSourceType.YOUTUBE, videoId);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - 신규 생성 후 async 작업을 시작한다")
            void thenCreateNewAndSubmitAsync() throws Exception {
                UUID result = sut.create(userTarget);

                assertThat(result).isEqualTo(recipeId);
                verify(asyncRecipeCreationService).create(recipeId, 5L, videoId, jobId);
                verify(creditPort).spendRecipeCreate(userId, recipeId, 5L);
            }
        }

        @Nested
        @DisplayName("Given - 기존 FAILED 레시피가 있고 retry 선점에 성공하면")
        class GivenFailedAndRetrySuccess {
            UUID recipeId;
            UUID oldJobId;
            UUID newJobId;
            RecipeInfo failedRecipe;
            RecipeInfo retriedRecipe;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                oldJobId = UUID.randomUUID();
                newJobId = UUID.randomUUID();
                failedRecipe = mockRecipe(recipeId, RecipeStatus.FAILED, 7L, oldJobId);
                retriedRecipe = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 7L, newJobId);

                when(recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE))
                        .thenReturn(failedRecipe, retriedRecipe, retriedRecipe);
                doReturn(true).when(recipeInfoService).retry(recipeId);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - 재시도 후 갱신된 jobId로 async를 재제출하고 합류한다")
            void thenRetryAndJoin() throws Exception {
                UUID result = sut.create(userTarget);

                assertThat(result).isEqualTo(recipeId);
                verify(asyncRecipeCreationService).create(recipeId, 7L, videoId, newJobId);
                verify(recipeInfoService).retry(recipeId);
                verify(creditPort).spendRecipeCreate(userId, recipeId, 7L);
            }
        }

        @Nested
        @DisplayName("Given - 기존 FAILED 레시피가 있고 retry 경쟁에서 지면")
        class GivenFailedAndRetryLose {
            UUID recipeId;
            UUID jobId;
            RecipeInfo failedRecipe;
            RecipeInfo inProgressRecipe;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                failedRecipe = mockRecipe(recipeId, RecipeStatus.FAILED, 2L, UUID.randomUUID());
                inProgressRecipe = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 2L, jobId);

                when(recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE))
                        .thenReturn(failedRecipe, inProgressRecipe);
                doReturn(false).when(recipeInfoService).retry(recipeId);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - async 재제출 없이 기존 진행중 작업에 합류한다")
            void thenJoinWithoutAsyncResubmit() throws Exception {
                UUID result = sut.create(userTarget);

                assertThat(result).isEqualTo(recipeId);
                verify(asyncRecipeCreationService, never())
                        .create(eq(recipeId), anyLong(), eq(videoId), org.mockito.ArgumentMatchers.any());
                verify(creditPort).spendRecipeCreate(userId, recipeId, 2L);
            }
        }

        @Nested
        @DisplayName("Given - retry 후 재조회 결과가 여전히 FAILED면")
        class GivenRetryResultStillFailed {
            UUID recipeId;
            RecipeInfo failedRecipe;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                failedRecipe = mockRecipe(recipeId, RecipeStatus.FAILED, 2L, UUID.randomUUID());
                when(recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE))
                        .thenReturn(failedRecipe, failedRecipe);
                doReturn(false).when(recipeInfoService).retry(recipeId);
            }

            @Test
            @DisplayName("Then - RECIPE_FAILED 예외를 던진다")
            void thenThrowsRecipeFailed() {
                assertThatThrownBy(() -> sut.create(userTarget))
                        .isInstanceOf(RecipeException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_FAILED);
            }
        }

        @Nested
        @DisplayName("Given - 기존 BLOCKED/BANNED 레시피면")
        class GivenBlocked {
            @Test
            @DisplayName("Then - RECIPE_BANNED 예외를 던진다")
            void thenThrowsBanned() throws Exception {
                RecipeInfo blocked = mockRecipe(UUID.randomUUID(), RecipeStatus.BLOCKED, 1L, UUID.randomUUID());
                doReturn(blocked).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);

                assertThatThrownBy(() -> sut.create(userTarget))
                        .isInstanceOf(RecipeException.class)
                        .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_BANNED);
            }
        }

        @Nested
        @DisplayName("Given - 신규 생성 후 async 제출이 즉시 실패하면")
        class GivenAsyncSubmitFailureOnNewCreate {
            UUID recipeId;
            UUID jobId;
            RecipeInfo created;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
                created = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 4L, jobId);
                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                        .when(recipeInfoService)
                        .getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(created).when(recipeInfoService).create(RecipeSourceType.YOUTUBE, videoId);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                doThrow(new RuntimeException("executor reject"))
                        .when(asyncRecipeCreationService)
                        .create(recipeId, 4L, videoId, jobId);
            }

            @Test
            @DisplayName("Then - 레시피를 FAILED로 되돌리고 북마크/과금을 롤백한다")
            void thenRollbackSideEffects() throws Exception {
                assertThatThrownBy(() -> sut.create(userTarget)).isInstanceOf(RuntimeException.class);

                verify(recipeInfoService).failed(recipeId);
                verify(recipeBookmarkService).delete(userId, recipeId);
                verify(creditPort).refundRecipeCreate(userId, recipeId, 4L);
            }
        }

        @Nested
        @DisplayName("Given - 신규 생성 중 duplicate source가 발생하면")
        class GivenDuplicateOnNewCreate {
            UUID recipeId;
            RecipeInfo existing;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                existing = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 3L, UUID.randomUUID());
                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                        .when(recipeInfoService)
                        .getBySource(videoId, RecipeSourceType.YOUTUBE);
                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_DUPLICATE_SOURCE))
                        .when(recipeInfoService)
                        .create(RecipeSourceType.YOUTUBE, videoId);
                doReturn(existing).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - 기존 레시피로 합류한다")
            void thenJoinExisting() throws Exception {
                UUID result = sut.create(userTarget);
                assertThat(result).isEqualTo(recipeId);
                verify(asyncRecipeCreationService, never())
                        .create(eq(recipeId), anyLong(), eq(videoId), org.mockito.ArgumentMatchers.any());
            }
        }

        @Nested
        @DisplayName("Given - retry 경로에서 async 제출이 즉시 실패하면")
        class GivenAsyncSubmitFailureOnRetry {
            UUID recipeId;
            UUID oldJobId;
            UUID newJobId;
            RecipeInfo failedRecipe;
            RecipeInfo retriedRecipe;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                oldJobId = UUID.randomUUID();
                newJobId = UUID.randomUUID();
                failedRecipe = mockRecipe(recipeId, RecipeStatus.FAILED, 6L, oldJobId);
                retriedRecipe = mockRecipe(recipeId, RecipeStatus.IN_PROGRESS, 6L, newJobId);
                when(recipeInfoService.getBySource(videoId, RecipeSourceType.YOUTUBE))
                        .thenReturn(failedRecipe, retriedRecipe);
                doReturn(true).when(recipeInfoService).retry(recipeId);
                doThrow(new RuntimeException("executor reject"))
                        .when(asyncRecipeCreationService)
                        .create(recipeId, 6L, videoId, newJobId);
            }

            @Test
            @DisplayName("Then - 레시피 상태를 FAILED로 복구하고 예외를 전파한다")
            void thenRestoreFailedAndThrow() throws Exception {
                assertThatThrownBy(() -> sut.create(userTarget)).isInstanceOf(RuntimeException.class);
                verify(recipeInfoService).failed(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - Crawler 요청일 때")
        class GivenCrawler {
            RecipeCreationTarget.Crawler crawlerTarget;

            @BeforeEach
            void setUp() {
                crawlerTarget = new RecipeCreationTarget.Crawler(uri);
            }

            @Test
            @DisplayName("Then - 기존 레시피 합류 시 북마크/과금을 수행하지 않는다")
            void thenNoBookmarkOrCreditOnJoin() throws Exception {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo existing = mockRecipe(recipeId, RecipeStatus.SUCCESS, 1L, UUID.randomUUID());
                doReturn(existing).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);

                UUID result = sut.create(crawlerTarget);

                assertThat(result).isEqualTo(recipeId);
                verify(recipeBookmarkService, never()).create(org.mockito.ArgumentMatchers.any(), eq(recipeId));
                verify(creditPort, never())
                        .spendRecipeCreate(org.mockito.ArgumentMatchers.any(), eq(recipeId), anyLong());
            }
        }

        @Nested
        @DisplayName("Given - 북마크가 이미 존재하면")
        class GivenBookmarkAlreadyExists {
            @BeforeEach
            void setUp() throws Exception {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo existing = mockRecipe(recipeId, RecipeStatus.SUCCESS, 9L, UUID.randomUUID());
                doReturn(existing).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(false).when(recipeBookmarkService).create(userId, recipeId);
            }

            @Test
            @DisplayName("Then - 과금은 수행하지 않는다")
            void thenSkipCredit() throws Exception {
                sut.create(userTarget);
                verify(creditPort, never())
                        .spendRecipeCreate(eq(userId), org.mockito.ArgumentMatchers.any(), anyLong());
            }
        }

        @Nested
        @DisplayName("Given - 과금 부족 예외가 발생하면")
        class GivenCreditInsufficient {
            @BeforeEach
            void setUp() throws Exception {
                UUID recipeId = UUID.randomUUID();
                RecipeInfo existing = mockRecipe(recipeId, RecipeStatus.SUCCESS, 9L, UUID.randomUUID());
                doReturn(existing).when(recipeInfoService).getBySource(videoId, RecipeSourceType.YOUTUBE);
                doReturn(true).when(recipeBookmarkService).create(userId, recipeId);
                doThrow(new RecipeCreditException(CreditErrorCode.CREDIT_INSUFFICIENT))
                        .when(creditPort)
                        .spendRecipeCreate(userId, recipeId, 9L);
            }

            @Test
            @DisplayName("Then - RecipeException으로 매핑하고 북마크를 롤백한다")
            void thenMapException() throws Exception {
                assertThatThrownBy(() -> sut.create(userTarget))
                        .isInstanceOf(RecipeException.class)
                        .hasFieldOrPropertyWithValue("error", CreditErrorCode.CREDIT_INSUFFICIENT);
                verify(recipeBookmarkService).delete(eq(userId), org.mockito.ArgumentMatchers.any());
            }
        }
    }

    private RecipeInfo mockRecipe(UUID id, RecipeStatus status, long creditCost, UUID currentJobId) {
        RecipeInfo recipeInfo = mock(RecipeInfo.class);
        doReturn(id).when(recipeInfo).getId();
        doReturn(status).when(recipeInfo).getRecipeStatus();
        doReturn(creditCost).when(recipeInfo).getCreditCost();
        doReturn(currentJobId).when(recipeInfo).getCurrentJobId();
        return recipeInfo;
    }
}
