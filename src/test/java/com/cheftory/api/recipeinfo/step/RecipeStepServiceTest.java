package com.cheftory.api.recipeinfo.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.step.client.RecipeStepClient;
import com.cheftory.api.recipeinfo.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.step.entity.RecipeStepSort;
import com.cheftory.api.recipeinfo.step.repository.RecipeStepRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeStepService")
public class RecipeStepServiceTest {

  private RecipeStepService recipeStepService;
  private RecipeStepRepository recipeStepRepository;
  private RecipeStepClient recipeStepClient;
  private Clock clock;

  @BeforeEach
  void setUp() {
    recipeStepRepository = mock(RecipeStepRepository.class);
    recipeStepClient = mock(RecipeStepClient.class);
    clock = mock(Clock.class);
    recipeStepService = new RecipeStepService(recipeStepClient, recipeStepRepository, clock);
  }

  @Nested
  @DisplayName("레시피 단계 생성")
  class CreateRecipeSteps {

    private UUID recipeId;
    private RecipeCaption recipeCaption;

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        recipeCaption = mock(RecipeCaption.class);
      }

      @Nested
      @DisplayName("When - 레시피 단계 생성 요청을 하면")
      class WhenCreateRecipeSteps {

        private String subtitle;
        private Double start;
        private String descriptionText;
        private Double descriptionStart;
        private ClientRecipeStepsResponse clientResponse;

        @BeforeEach
        void setUp() {
          subtitle = "Step 1";
          start = 1.0;
          descriptionText = "Chop the onions.";
          descriptionStart = 2.0;
          clientResponse = mock(ClientRecipeStepsResponse.class);

          RecipeStep step1 =
              RecipeStep.create(
                  1,
                  subtitle,
                  List.of(
                      RecipeStep.Detail.builder()
                          .text(descriptionText)
                          .start(descriptionStart)
                          .build()),
                  start,
                  recipeId,
                  clock);
          List<RecipeStep> converted = List.of(step1);
          doReturn(LocalDateTime.now()).when(clock).now();

          when(recipeStepClient.fetchRecipeSteps(recipeCaption)).thenReturn(clientResponse);
          when(clientResponse.toRecipeSteps(recipeId, clock)).thenReturn(converted);
          when(recipeStepRepository.saveAll(converted)).thenReturn(converted);
        }

        @DisplayName("Then - 레시피 단계들이 생성된다")
        @Test
        void thenRecipeStepsCreated() {
          recipeStepService.create(recipeId, recipeCaption);
          verify(recipeStepClient).fetchRecipeSteps(recipeCaption);
          verify(clientResponse).toRecipeSteps(recipeId, clock);
          verify(recipeStepRepository).saveAll(clientResponse.toRecipeSteps(recipeId, clock));
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 단계 조회")
  class FindRecipeSteps {
    private UUID recipeId;
    private RecipeStep recipeStep;

    @BeforeEach
    void setUp() {
      recipeId = UUID.randomUUID();
      recipeStep = mock(RecipeStep.class);
      when(recipeStepRepository.findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC))
          .thenReturn(List.of(recipeStep));
    }

    @DisplayName("레시피 단계들을 조회한다")
    @Test
    void thenRecipeStepsFetched() {
      recipeStepService.finds(recipeId);
      verify(recipeStepRepository).findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC);
    }
  }

  @DisplayName("ClientRecipeStepsResponse DTO 변환 테스트")
  @Nested
  class ClientRecipeStepsResponseTest {

    @Test
    @DisplayName("ClientRecipeStepsResponse.toRecipeSteps() - 정상 변환")
    void shouldConvertToRecipeStepsCorrectly() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);
      
      String jsonResponse = """
          {
            "steps": [
              {
                "subtitle": "첫 번째 단계",
                "start": 10.0,
                "descriptions": [
                  {
                    "text": "재료를 준비합니다",
                    "start": 10.5
                  },
                  {
                    "text": "도구를 정리합니다",
                    "start": 11.0
                  }
                ]
              },
              {
                "subtitle": "두 번째 단계",
                "start": 30.0,
                "descriptions": [
                  {
                    "text": "요리를 시작합니다",
                    "start": 30.5
                  }
                ]
              }
            ]
          }
          """;
      
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      ClientRecipeStepsResponse clientResponse = objectMapper.readValue(jsonResponse, ClientRecipeStepsResponse.class);

      // when
      List<RecipeStep> result = clientResponse.toRecipeSteps(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      
      // 첫 번째 단계 검증
      RecipeStep firstStep = result.get(0);
      assertThat(firstStep.getStepOrder()).isEqualTo(1);
      assertThat(firstStep.getSubtitle()).isEqualTo("첫 번째 단계");
      assertThat(firstStep.getStart()).isEqualTo(10.0);
      assertThat(firstStep.getRecipeId()).isEqualTo(recipeId);
      assertThat(firstStep.getDetails()).hasSize(2);
      assertThat(firstStep.getDetails().get(0).getText()).isEqualTo("재료를 준비합니다");
      assertThat(firstStep.getDetails().get(0).getStart()).isEqualTo(10.5);
      assertThat(firstStep.getDetails().get(1).getText()).isEqualTo("도구를 정리합니다");
      assertThat(firstStep.getDetails().get(1).getStart()).isEqualTo(11.0);
      
      // 두 번째 단계 검증
      RecipeStep secondStep = result.get(1);
      assertThat(secondStep.getStepOrder()).isEqualTo(2);
      assertThat(secondStep.getSubtitle()).isEqualTo("두 번째 단계");
      assertThat(secondStep.getStart()).isEqualTo(30.0);
      assertThat(secondStep.getRecipeId()).isEqualTo(recipeId);
      assertThat(secondStep.getDetails()).hasSize(1);
      assertThat(secondStep.getDetails().get(0).getText()).isEqualTo("요리를 시작합니다");
      assertThat(secondStep.getDetails().get(0).getStart()).isEqualTo(30.5);
    }

    @Test
    @DisplayName("ClientRecipeStepsResponse.toRecipeSteps() - 빈 단계 목록")
    void shouldConvertWithEmptySteps() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);
      
      String jsonResponse = """
          {
            "steps": []
          }
          """;
      
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      ClientRecipeStepsResponse clientResponse = objectMapper.readValue(jsonResponse, ClientRecipeStepsResponse.class);

      // when
      List<RecipeStep> result = clientResponse.toRecipeSteps(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ClientRecipeStepsResponse.toRecipeSteps() - 단일 단계")
    void shouldConvertSingleStep() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);
      
      String jsonResponse = """
          {
            "steps": [
              {
                "subtitle": "유일한 단계",
                "start": 5.0,
                "descriptions": [
                  {
                    "text": "설명1",
                    "start": 5.1
                  },
                  {
                    "text": "설명2",
                    "start": 5.2
                  },
                  {
                    "text": "설명3",
                    "start": 5.3
                  }
                ]
              }
            ]
          }
          """;
      
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      ClientRecipeStepsResponse clientResponse = objectMapper.readValue(jsonResponse, ClientRecipeStepsResponse.class);

      // when
      List<RecipeStep> result = clientResponse.toRecipeSteps(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      
      RecipeStep step = result.get(0);
      assertThat(step.getStepOrder()).isEqualTo(1);
      assertThat(step.getSubtitle()).isEqualTo("유일한 단계");
      assertThat(step.getStart()).isEqualTo(5.0);
      assertThat(step.getRecipeId()).isEqualTo(recipeId);
      assertThat(step.getDetails()).hasSize(3);
      assertThat(step.getDetails().get(0).getText()).isEqualTo("설명1");
      assertThat(step.getDetails().get(1).getText()).isEqualTo("설명2");
      assertThat(step.getDetails().get(2).getText()).isEqualTo("설명3");
    }

    @Test
    @DisplayName("ClientRecipeStepsResponse.toRecipeSteps() - 빈 descriptions를 가진 단계")
    void shouldConvertStepWithEmptyDescriptions() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);
      
      String jsonResponse = """
          {
            "steps": [
              {
                "subtitle": "설명 없는 단계",
                "start": 0.0,
                "descriptions": []
              }
            ]
          }
          """;
      
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      ClientRecipeStepsResponse clientResponse = objectMapper.readValue(jsonResponse, ClientRecipeStepsResponse.class);

      // when
      List<RecipeStep> result = clientResponse.toRecipeSteps(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(1);
      
      RecipeStep step = result.get(0);
      assertThat(step.getStepOrder()).isEqualTo(1);
      assertThat(step.getSubtitle()).isEqualTo("설명 없는 단계");
      assertThat(step.getStart()).isEqualTo(0.0);
      assertThat(step.getRecipeId()).isEqualTo(recipeId);
      assertThat(step.getDetails()).isEmpty();
    }
  }
}
