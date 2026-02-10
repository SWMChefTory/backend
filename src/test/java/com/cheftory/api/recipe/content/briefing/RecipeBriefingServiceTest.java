package com.cheftory.api.recipe.content.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.client.BriefingClient;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import com.cheftory.api.recipe.content.briefing.respotiory.RecipeBriefingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBriefingService 테스트")
public class RecipeBriefingServiceTest {

    private BriefingClient client;
    private RecipeBriefingService service;
    private RecipeBriefingRepository repository;
    private Clock clock;

    @BeforeEach
    void setUp() {
        client = mock(BriefingClient.class);
        repository = mock(RecipeBriefingRepository.class);
        clock = mock(Clock.class);
        service = new RecipeBriefingService(client, repository, clock);
    }

    @Nested
    @DisplayName("레시피 브리핑 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID와 레시피 ID가 주어졌을 때")
        class GivenValidIds {
            String videoId;
            UUID recipeId;
            BriefingClientResponse briefingClientResponse;
            List<RecipeBriefing> recipeBriefings;

            @BeforeEach
            void setUp() throws RecipeBriefingException {
                videoId = "valid-video-id";
                recipeId = UUID.randomUUID();
                briefingClientResponse = mock(BriefingClientResponse.class);
                recipeBriefings = List.of(mock(RecipeBriefing.class), mock(RecipeBriefing.class));

                doReturn(briefingClientResponse).when(client).fetchBriefing(videoId);
                doReturn(recipeBriefings).when(briefingClientResponse).toRecipeBriefing(recipeId, clock);
                doNothing().when(repository).saveAll(anyList());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeBriefingException {
                    service.create(videoId, recipeId);
                }

                @Test
                @DisplayName("Then - 브리핑을 생성하고 저장한다")
                void thenCreatesAndSaves() throws RecipeBriefingException {
                    verify(client).fetchBriefing(videoId);
                    verify(briefingClientResponse).toRecipeBriefing(recipeId, clock);
                    verify(repository).saveAll(recipeBriefings);
                }
            }
        }

        @Nested
        @DisplayName("Given - 외부 클라이언트 오류가 발생했을 때")
        class GivenClientError {
            String videoId;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBriefingException {
                videoId = "error-video-id";
                recipeId = UUID.randomUUID();
                doThrow(new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL))
                        .when(client)
                        .fetchBriefing(videoId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - RecipeBriefingException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.create(videoId, recipeId))
                            .isInstanceOf(RecipeBriefingException.class);
                }
            }
        }

        @Nested
        @DisplayName("Given - 저장소 오류가 발생했을 때")
        class GivenRepositoryError {
            String videoId;
            UUID recipeId;
            BriefingClientResponse briefingClientResponse;
            List<RecipeBriefing> recipeBriefings;

            @BeforeEach
            void setUp() throws RecipeBriefingException {
                videoId = "valid-video-id";
                recipeId = UUID.randomUUID();
                briefingClientResponse = mock(BriefingClientResponse.class);
                recipeBriefings = List.of(mock(RecipeBriefing.class));

                doReturn(briefingClientResponse).when(client).fetchBriefing(videoId);
                doReturn(recipeBriefings).when(briefingClientResponse).toRecipeBriefing(recipeId, clock);
                doThrow(new RuntimeException("Database error")).when(repository).saveAll(anyList());
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - RuntimeException을 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.create(videoId, recipeId)).isInstanceOf(RuntimeException.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 브리핑 조회 (gets)")
    class Gets {

        @Nested
        @DisplayName("Given - 레시피 ID가 주어졌을 때")
        class GivenRecipeId {
            UUID recipeId;
            List<RecipeBriefing> expectedBriefings;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                expectedBriefings = List.of(mock(RecipeBriefing.class), mock(RecipeBriefing.class));
                doReturn(expectedBriefings).when(repository).findAllByRecipeId(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeBriefing> result;

                @BeforeEach
                void setUp() {
                    result = service.gets(recipeId);
                }

                @Test
                @DisplayName("Then - 해당 레시피의 브리핑 목록을 반환한다")
                void thenReturnsBriefings() {
                    assertThat(result).isEqualTo(expectedBriefings);
                    verify(repository).findAllByRecipeId(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 브리핑이 없는 레시피 ID가 주어졌을 때")
        class GivenNoBriefings {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                doReturn(List.of()).when(repository).findAllByRecipeId(recipeId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeBriefing> result;

                @BeforeEach
                void setUp() {
                    result = service.gets(recipeId);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isEmpty();
                }
            }
        }
    }

    @Nested
    @DisplayName("BriefingClientResponse 변환 (toRecipeBriefing)")
    class ToRecipeBriefing {

        @Nested
        @DisplayName("Given - 정상적인 응답이 주어졌을 때")
        class GivenValidResponse {
            UUID recipeId;
            Clock mockClock;
            BriefingClientResponse clientResponse;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                mockClock = mock(Clock.class);
                String jsonResponse =
                        """
                        {
                            "briefings": [
                                "이 요리는 매우 맛있습니다",
                                "조리 시간이 30분 정도 걸립니다",
                                "초보자도 쉽게 따라할 수 있어요"
                            ]
                        }
                        """;
                ObjectMapper objectMapper = new ObjectMapper();
                clientResponse = objectMapper.readValue(jsonResponse, BriefingClientResponse.class);
            }

            @Nested
            @DisplayName("When - 변환을 요청하면")
            class WhenConverting {
                List<RecipeBriefing> result;

                @BeforeEach
                void setUp() {
                    result = clientResponse.toRecipeBriefing(recipeId, mockClock);
                }

                @Test
                @DisplayName("Then - RecipeBriefing 리스트로 변환된다")
                void thenConvertsCorrectly() {
                    assertThat(result).hasSize(3);
                    assertThat(result.get(0).getContent()).isEqualTo("이 요리는 매우 맛있습니다");
                    assertThat(result.get(1).getContent()).isEqualTo("조리 시간이 30분 정도 걸립니다");
                    assertThat(result.get(2).getContent()).isEqualTo("초보자도 쉽게 따라할 수 있어요");
                    result.forEach(
                            briefing -> assertThat(briefing.getRecipeId()).isEqualTo(recipeId));
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 브리핑 목록이 주어졌을 때")
        class GivenEmptyBriefings {
            UUID recipeId;
            Clock mockClock;
            BriefingClientResponse clientResponse;

            @BeforeEach
            void setUp() throws Exception {
                recipeId = UUID.randomUUID();
                mockClock = mock(Clock.class);
                String jsonResponse =
                        """
                        {
                            "briefings": []
                        }
                        """;
                ObjectMapper objectMapper = new ObjectMapper();
                clientResponse = objectMapper.readValue(jsonResponse, BriefingClientResponse.class);
            }

            @Nested
            @DisplayName("When - 변환을 요청하면")
            class WhenConverting {
                List<RecipeBriefing> result;

                @BeforeEach
                void setUp() {
                    result = clientResponse.toRecipeBriefing(recipeId, mockClock);
                }

                @Test
                @DisplayName("Then - 빈 리스트를 반환한다")
                void thenReturnsEmptyList() {
                    assertThat(result).isEmpty();
                }
            }
        }
    }
}
