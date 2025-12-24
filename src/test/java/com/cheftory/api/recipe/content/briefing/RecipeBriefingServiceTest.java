package com.cheftory.api.recipe.content.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.client.BriefingClient;
import com.cheftory.api.recipe.content.briefing.client.dto.BriefingClientResponse;
import com.cheftory.api.recipe.content.briefing.client.exception.BriefingClientErrorCode;
import com.cheftory.api.recipe.content.briefing.client.exception.BriefingClientException;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBriefingService 테스트")
public class RecipeBriefingServiceTest {

  private BriefingClient briefingClient;
  private RecipeBriefingService recipeBriefingService;
  private RecipeBriefingRepository recipeBriefingRepository;
  private Clock clock;

  @BeforeEach
  void setUp() {
    briefingClient = mock(BriefingClient.class);
    recipeBriefingRepository = mock(RecipeBriefingRepository.class);
    clock = mock(Clock.class);
    recipeBriefingService =
        new RecipeBriefingService(briefingClient, recipeBriefingRepository, clock);
  }

  @DisplayName("레시피 브리핑 생성")
  @Nested
  class CreateBriefing {

    @Nested
    @DisplayName("Given - 유효한 비디오 ID와 레시피 ID가 주어졌을 때")
    class GivenValidVideoAndRecipeId {
      private String videoId;
      private UUID recipeId;
      private BriefingClientResponse briefingClientResponse;
      private List<RecipeBriefing> recipeBriefings;

      @BeforeEach
      void setUp() {
        videoId = "valid-video-id";
        recipeId = UUID.randomUUID();

        briefingClientResponse = mock(BriefingClientResponse.class);
        recipeBriefings = List.of(mock(RecipeBriefing.class), mock(RecipeBriefing.class));

        doReturn(briefingClientResponse).when(briefingClient).fetchBriefing(videoId);
        doReturn(recipeBriefings).when(briefingClientResponse).toRecipeBriefing(recipeId, clock);
        doReturn(recipeBriefings).when(recipeBriefingRepository).saveAll(anyList());
      }

      @Nested
      @DisplayName("When - 레시피 브리핑을 생성하면")
      class WhenCreateRecipeBriefing {

        @Test
        @DisplayName("Then - 브리핑이 성공적으로 생성된다")
        void thenCreatesBriefingSuccessfully() {
          recipeBriefingService.create(videoId, recipeId);

          verify(briefingClient).fetchBriefing(videoId);
          verify(briefingClientResponse).toRecipeBriefing(recipeId, clock);
          verify(recipeBriefingRepository).saveAll(recipeBriefings);
        }
      }
    }

    @Nested
    @DisplayName("Given - BriefingClient에서 예외가 발생할 때")
    class GivenBriefingClientException {

      private String videoId;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        videoId = "error-video-id";
        recipeId = UUID.randomUUID();

        doThrow(new BriefingClientException(BriefingClientErrorCode.SERVER_ERROR))
            .when(briefingClient)
            .fetchBriefing(videoId);
      }

      @Nested
      @DisplayName("When - 레시피 브리핑을 생성하면")
      class WhenCreateRecipeBriefing {

        @Test
        @DisplayName("Then - BRIEFING_CREATE_FAIL 예외가 발생한다")
        void thenThrowsBriefingCreateFailException() {
          assertThatThrownBy(() -> recipeBriefingService.create(videoId, recipeId))
              .isInstanceOf(RecipeBriefingException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);

          verify(briefingClient).fetchBriefing(videoId);
        }
      }
    }

    @Nested
    @DisplayName("Given - Repository에서 예외가 발생할 때")
    class GivenRepositoryException {

      private String videoId;
      private UUID recipeId;
      private BriefingClientResponse briefingClientResponse;
      private List<RecipeBriefing> recipeBriefings;

      @BeforeEach
      void setUp() {
        videoId = "valid-video-id";
        recipeId = UUID.randomUUID();

        briefingClientResponse = mock(BriefingClientResponse.class);
        recipeBriefings = List.of(mock(RecipeBriefing.class));

        doReturn(briefingClientResponse).when(briefingClient).fetchBriefing(videoId);
        doReturn(recipeBriefings).when(briefingClientResponse).toRecipeBriefing(recipeId, clock);
        doThrow(new RuntimeException("Database error"))
            .when(recipeBriefingRepository)
            .saveAll(anyList());
      }

      @Nested
      @DisplayName("When - 레시피 브리핑을 생성하면")
      class WhenCreateRecipeBriefing {

        @Test
        @DisplayName("Then - BRIEFING_CREATE_FAIL 예외가 발생한다")
        void thenThrowsBriefingCreateFailException() {
          assertThatThrownBy(() -> recipeBriefingService.create(videoId, recipeId))
              .isInstanceOf(RecipeBriefingException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);

          verify(briefingClient).fetchBriefing(videoId);
          verify(briefingClientResponse).toRecipeBriefing(recipeId, clock);
          verify(recipeBriefingRepository).saveAll(recipeBriefings);
        }
      }
    }
  }

  @DisplayName("레시피 ID로 브리핑 목록 조회")
  @Nested
  class FindBriefingsByRecipeId {

    @Nested
    @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
    class GivenExistingRecipeId {
      private UUID recipeId;
      private List<RecipeBriefing> expectedBriefings;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        expectedBriefings = List.of(mock(RecipeBriefing.class), mock(RecipeBriefing.class));
        doReturn(expectedBriefings).when(recipeBriefingRepository).findAllByRecipeId(recipeId);
      }

      @Test
      @DisplayName("When - 브리핑 목록을 조회하면 Then - 해당 브리핑 목록이 반환된다")
      void whenFindsByRecipeId_thenReturnsBriefings() {
        List<RecipeBriefing> result = recipeBriefingService.gets(recipeId);

        assertThat(result).isEqualTo(expectedBriefings);
        assertThat(result).hasSize(2);
        verify(recipeBriefingRepository).findAllByRecipeId(recipeId);
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistingRecipeId {
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        doReturn(List.of()).when(recipeBriefingRepository).findAllByRecipeId(recipeId);
      }

      @Test
      @DisplayName("When - 브리핑 목록을 조회하면 Then - 빈 목록이 반환된다")
      void whenFindsByRecipeId_thenReturnsEmptyList() {
        List<RecipeBriefing> result = recipeBriefingService.gets(recipeId);

        assertThat(result).isEmpty();
        verify(recipeBriefingRepository).findAllByRecipeId(recipeId);
      }
    }
  }

  @DisplayName("BriefingClientResponse DTO 변환 테스트")
  @Nested
  class BriefingClientResponseTest {

    @Test
    @DisplayName("BriefingClientResponse.toRecipeBriefing() - 정상 변환")
    void shouldConvertToRecipeBriefingCorrectly() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      // JSON으로부터 BriefingClientResponse 생성
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

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      BriefingClientResponse clientResponse =
          objectMapper.readValue(jsonResponse, BriefingClientResponse.class);

      // when
      List<RecipeBriefing> result = clientResponse.toRecipeBriefing(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(3);
      assertThat(result.get(0).getContent()).isEqualTo("이 요리는 매우 맛있습니다");
      assertThat(result.get(1).getContent()).isEqualTo("조리 시간이 30분 정도 걸립니다");
      assertThat(result.get(2).getContent()).isEqualTo("초보자도 쉽게 따라할 수 있어요");

      // 모든 RecipeBriefing이 같은 recipeId를 가지는지 확인
      result.forEach(
          briefing -> {
            assertThat(briefing.getRecipeId()).isEqualTo(recipeId);
          });
    }

    @Test
    @DisplayName("BriefingClientResponse.toRecipeBriefing() - 빈 브리핑 목록")
    void shouldConvertWithEmptyBriefings() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      String jsonResponse =
          """
          {
            "briefings": []
          }
          """;

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      BriefingClientResponse clientResponse =
          objectMapper.readValue(jsonResponse, BriefingClientResponse.class);

      // when
      List<RecipeBriefing> result = clientResponse.toRecipeBriefing(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("BriefingClientResponse.toRecipeBriefing() - 단일 브리핑")
    void shouldConvertWithSingleBriefing() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      String jsonResponse =
          """
          {
            "briefings": [
              "정말 맛있는 요리입니다!"
            ]
          }
          """;

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      BriefingClientResponse clientResponse =
          objectMapper.readValue(jsonResponse, BriefingClientResponse.class);

      // when
      List<RecipeBriefing> result = clientResponse.toRecipeBriefing(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getContent()).isEqualTo("정말 맛있는 요리입니다!");
      assertThat(result.get(0).getRecipeId()).isEqualTo(recipeId);
    }
  }
}
