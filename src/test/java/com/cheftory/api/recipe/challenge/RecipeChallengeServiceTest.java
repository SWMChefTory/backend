package com.cheftory.api.recipe.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ViewedAtCursor;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeErrorCode;
import com.cheftory.api.recipe.challenge.exception.RecipeChallengeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeChallengeService 테스트")
class RecipeChallengeServiceTest {

    private RecipeUserChallengeRepository recipeUserChallengeRepository;
    private RecipeChallengeRepository recipeChallengeRepository;
    private ChallengeRepository challengeRepository;
    private RecipeUserChallengeCompletionRepository recipeUserChallengeCompletionRepository;
    private Clock clock;
    private ViewedAtCursorCodec viewedAtCursorCodec;
    private RecipeChallengeService service;

    @BeforeEach
    void setUp() {
        recipeUserChallengeRepository = mock(RecipeUserChallengeRepository.class);
        recipeChallengeRepository = mock(RecipeChallengeRepository.class);
        challengeRepository = mock(ChallengeRepository.class);
        recipeUserChallengeCompletionRepository = mock(RecipeUserChallengeCompletionRepository.class);
        clock = mock(Clock.class);
        viewedAtCursorCodec = mock(ViewedAtCursorCodec.class);
        service = new RecipeChallengeService(
                recipeUserChallengeRepository,
                recipeChallengeRepository,
                challengeRepository,
                recipeUserChallengeCompletionRepository,
                clock,
                viewedAtCursorCodec);
    }

    @Nested
    @DisplayName("사용자 챌린지 조회 (getUser)")
    class GetUser {

        @Nested
        @DisplayName("Given - 사용자가 참여 중인 챌린지가 있을 때")
        class GivenUserHasChallenge {
            UUID userId;
            UUID challengeId;
            Challenge challenge;
            RecipeUserChallenge recipeUserChallenge;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                challenge = mock(Challenge.class);
                recipeUserChallenge = mock(RecipeUserChallenge.class);

                doReturn(LocalDateTime.of(2024, 1, 1, 12, 0)).when(clock).now();
                doReturn(List.of(challenge)).when(challengeRepository).findOngoing(any(LocalDateTime.class));
                doReturn(challengeId).when(challenge).getId();
                doReturn(List.of(recipeUserChallenge))
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengesByUserIdAndChallengeIdIn(any(UUID.class), anyList());
                doReturn(challengeId).when(recipeUserChallenge).getChallengeId();
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                Challenge result;

                @BeforeEach
                void setUp() throws RecipeChallengeException {
                    result = service.getUser(userId);
                }

                @Test
                @DisplayName("Then - 참여 중인 챌린지를 반환한다")
                void thenReturnsChallenge() {
                    assertThat(result).isEqualTo(challenge);
                    verify(challengeRepository).findOngoing(any(LocalDateTime.class));
                    verify(recipeUserChallengeRepository)
                            .findRecipeUserChallengesByUserIdAndChallengeIdIn(any(UUID.class), anyList());
                }
            }
        }

        @Nested
        @DisplayName("Given - 진행 중인 챌린지가 없을 때")
        class GivenNoOngoingChallenge {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                doReturn(LocalDateTime.of(2024, 1, 1, 12, 0)).when(clock).now();
                doReturn(List.of()).when(challengeRepository).findOngoing(any(LocalDateTime.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    RecipeChallengeException thrown =
                            assertThrows(RecipeChallengeException.class, () -> service.getUser(userId));
                    assertThat(thrown.getError()).isEqualTo(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
                }
            }
        }

        @Nested
        @DisplayName("Given - 사용자가 참여한 챌린지가 없을 때")
        class GivenUserHasNoChallenge {
            UUID userId;
            Challenge challenge;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                challenge = mock(Challenge.class);

                doReturn(LocalDateTime.of(2024, 1, 1, 12, 0)).when(clock).now();
                doReturn(List.of(challenge)).when(challengeRepository).findOngoing(any(LocalDateTime.class));
                doReturn(UUID.randomUUID()).when(challenge).getId();
                doReturn(List.of())
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengesByUserIdAndChallengeIdIn(any(UUID.class), anyList());
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    RecipeChallengeException thrown =
                            assertThrows(RecipeChallengeException.class, () -> service.getUser(userId));
                    assertThat(thrown.getError()).isEqualTo(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("챌린지 레시피 목록 조회 (getChallengeRecipes)")
    class GetChallengeRecipes {

        @Nested
        @DisplayName("Given - 첫 페이지 조회 시")
        class GivenFirstPage {
            UUID userId;
            UUID challengeId;
            RecipeUserChallenge recipeUserChallenge;
            RecipeChallenge recipeChallenge1;
            RecipeChallenge recipeChallenge2;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                recipeUserChallenge = mock(RecipeUserChallenge.class);
                recipeChallenge1 = mock(RecipeChallenge.class);
                recipeChallenge2 = mock(RecipeChallenge.class);

                LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
                UUID recipeId1 = UUID.randomUUID();
                UUID recipeId2 = UUID.randomUUID();
                UUID rcId1 = UUID.randomUUID();
                UUID rcId2 = UUID.randomUUID();

                doReturn(UUID.randomUUID()).when(recipeUserChallenge).getId();
                doReturn(recipeUserChallenge)
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);

                doReturn(List.of(recipeChallenge1, recipeChallenge2))
                        .when(recipeChallengeRepository)
                        .findChallengeFirst(eq(challengeId), any(Pageable.class));

                doReturn(createdAt).when(recipeChallenge1).getCreatedAt();
                doReturn(createdAt).when(recipeChallenge2).getCreatedAt();
                doReturn(rcId1).when(recipeChallenge1).getId();
                doReturn(rcId2).when(recipeChallenge2).getId();
                doReturn(recipeId1).when(recipeChallenge1).getRecipeId();
                doReturn(recipeId2).when(recipeChallenge2).getRecipeId();

                doReturn("next-cursor").when(viewedAtCursorCodec).encode(any(ViewedAtCursor.class));
                doReturn(List.of())
                        .when(recipeUserChallengeCompletionRepository)
                        .findByRecipeChallengeIdInAndRecipeUserChallengeId(anyList(), any(UUID.class));
            }

            @Nested
            @DisplayName("When - 커서 없이 조회를 요청하면")
            class WhenGettingFirstPage {
                CursorPage<RecipeCompleteChallenge> result;

                @BeforeEach
                void setUp() throws RecipeChallengeException, CursorException {
                    result = service.getChallengeRecipes(userId, challengeId, null);
                }

                @Test
                @DisplayName("Then - 첫 페이지 레시피 목록을 반환한다")
                void thenReturnsFirstPage() {
                    assertThat(result.items()).hasSize(2);
                    verify(recipeChallengeRepository).findChallengeFirst(eq(challengeId), any(Pageable.class));
                    verify(recipeChallengeRepository, never()).findChallengeKeyset(any(), any(), any(), any());
                }
            }

            @Nested
            @DisplayName("When - 빈 커서로 조회를 요청하면")
            class WhenGettingWithBlankCursor {
                CursorPage<RecipeCompleteChallenge> result;

                @BeforeEach
                void setUp() throws RecipeChallengeException, CursorException {
                    result = service.getChallengeRecipes(userId, challengeId, "");
                }

                @Test
                @DisplayName("Then - 첫 페이지 레시피 목록을 반환한다")
                void thenReturnsFirstPage() {
                    assertThat(result.items()).hasSize(2);
                    verify(recipeChallengeRepository).findChallengeFirst(eq(challengeId), any(Pageable.class));
                }
            }
        }

        @Nested
        @DisplayName("Given - 커서 페이지 조회 시")
        class GivenCursorPage {
            UUID userId;
            UUID challengeId;
            String cursor;
            RecipeUserChallenge recipeUserChallenge;
            RecipeChallenge recipeChallenge;
            ViewedAtCursor viewedAtCursor;

            @BeforeEach
            void setUp() throws CursorException {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                cursor = "encoded-cursor";
                recipeUserChallenge = mock(RecipeUserChallenge.class);
                recipeChallenge = mock(RecipeChallenge.class);
                viewedAtCursor = mock(ViewedAtCursor.class);

                LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

                doReturn(UUID.randomUUID()).when(recipeUserChallenge).getId();
                doReturn(recipeUserChallenge)
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);

                doReturn(viewedAtCursor).when(viewedAtCursorCodec).decode(cursor);
                doReturn(createdAt).when(viewedAtCursor).lastViewedAt();
                doReturn(UUID.randomUUID()).when(viewedAtCursor).lastId();

                doReturn(List.of(recipeChallenge))
                        .when(recipeChallengeRepository)
                        .findChallengeKeyset(
                                eq(challengeId), any(LocalDateTime.class), any(UUID.class), any(Pageable.class));

                doReturn(createdAt).when(recipeChallenge).getCreatedAt();
                doReturn(UUID.randomUUID()).when(recipeChallenge).getId();
                doReturn(UUID.randomUUID()).when(recipeChallenge).getRecipeId();

                doReturn("next-cursor").when(viewedAtCursorCodec).encode(any(ViewedAtCursor.class));
                doReturn(List.of())
                        .when(recipeUserChallengeCompletionRepository)
                        .findByRecipeChallengeIdInAndRecipeUserChallengeId(anyList(), any(UUID.class));
            }

            @Nested
            @DisplayName("When - 커서로 조회를 요청하면")
            class WhenGettingWithCursor {
                CursorPage<RecipeCompleteChallenge> result;

                @BeforeEach
                void setUp() throws RecipeChallengeException, CursorException {
                    result = service.getChallengeRecipes(userId, challengeId, cursor);
                }

                @Test
                @DisplayName("Then - 해당 커서 이후 레시피 목록을 반환한다")
                void thenReturnsNextPage() {
                    assertThat(result.items()).hasSize(1);
                    verify(recipeChallengeRepository)
                            .findChallengeKeyset(
                                    eq(challengeId), any(LocalDateTime.class), any(UUID.class), any(Pageable.class));
                    verify(recipeChallengeRepository, never()).findChallengeFirst(any(), any());
                }
            }
        }

        @Nested
        @DisplayName("Given - 사용자가 챌린지에 참여하지 않았을 때")
        class GivenUserNotInChallenge {
            UUID userId;
            UUID challengeId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                doReturn(null)
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    RecipeChallengeException thrown = assertThrows(
                            RecipeChallengeException.class,
                            () -> service.getChallengeRecipes(userId, challengeId, null));
                    assertThat(thrown.getError()).isEqualTo(RecipeChallengeErrorCode.RECIPE_CHALLENGE_NOT_FOUND);
                }
            }
        }

        @Nested
        @DisplayName("Given - 완료된 레시피가 있을 때")
        class GivenCompletedRecipes {
            UUID userId;
            UUID challengeId;
            UUID recipeUserChallengeId;
            RecipeUserChallenge recipeUserChallenge;
            RecipeChallenge recipeChallenge1;
            RecipeChallenge recipeChallenge2;
            RecipeUserChallengeCompletion completion;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                recipeUserChallengeId = UUID.randomUUID();
                recipeUserChallenge = mock(RecipeUserChallenge.class);
                recipeChallenge1 = mock(RecipeChallenge.class);
                recipeChallenge2 = mock(RecipeChallenge.class);
                completion = mock(RecipeUserChallengeCompletion.class);

                LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);
                UUID rcId1 = UUID.randomUUID();
                UUID rcId2 = UUID.randomUUID();

                doReturn(recipeUserChallengeId).when(recipeUserChallenge).getId();
                doReturn(recipeUserChallenge)
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);

                doReturn(List.of(recipeChallenge1, recipeChallenge2))
                        .when(recipeChallengeRepository)
                        .findChallengeFirst(eq(challengeId), any(Pageable.class));

                doReturn(createdAt).when(recipeChallenge1).getCreatedAt();
                doReturn(createdAt).when(recipeChallenge2).getCreatedAt();
                doReturn(rcId1).when(recipeChallenge1).getId();
                doReturn(rcId2).when(recipeChallenge2).getId();
                doReturn(UUID.randomUUID()).when(recipeChallenge1).getRecipeId();
                doReturn(UUID.randomUUID()).when(recipeChallenge2).getRecipeId();

                doReturn("next-cursor").when(viewedAtCursorCodec).encode(any(ViewedAtCursor.class));

                doReturn(rcId1).when(completion).getRecipeChallengeId();
                doReturn(List.of(completion))
                        .when(recipeUserChallengeCompletionRepository)
                        .findByRecipeChallengeIdInAndRecipeUserChallengeId(anyList(), eq(recipeUserChallengeId));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                CursorPage<RecipeCompleteChallenge> result;

                @BeforeEach
                void setUp() throws RecipeChallengeException, CursorException {
                    result = service.getChallengeRecipes(userId, challengeId, null);
                }

                @Test
                @DisplayName("Then - 완료 여부가 포함된 목록을 반환한다")
                void thenReturnsWithCompletionStatus() {
                    assertThat(result.items()).hasSize(2);
                    assertThat(result.items().getFirst().isFinished()).isTrue();
                    assertThat(result.items().get(1).isFinished()).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("Given - 커서 디코딩 실패 시")
        class GivenCursorDecodeFails {
            UUID userId;
            UUID challengeId;
            String cursor;
            RecipeUserChallenge recipeUserChallenge;
            CursorException exception;

            @BeforeEach
            void setUp() throws CursorException {
                userId = UUID.randomUUID();
                challengeId = UUID.randomUUID();
                cursor = "invalid-cursor";
                recipeUserChallenge = mock(RecipeUserChallenge.class);
                exception = mock(CursorException.class);

                doReturn(UUID.randomUUID()).when(recipeUserChallenge).getId();
                doReturn(recipeUserChallenge)
                        .when(recipeUserChallengeRepository)
                        .findRecipeUserChallengeByUserIdAndChallengeId(userId, challengeId);
                doThrow(exception).when(viewedAtCursorCodec).decode(cursor);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 예외를 전파한다")
                void thenPropagatesException() {
                    CursorException thrown = assertThrows(
                            CursorException.class, () -> service.getChallengeRecipes(userId, challengeId, cursor));
                    assertSame(exception, thrown);
                }
            }
        }
    }
}
