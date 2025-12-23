package com.cheftory.api.recipe.content.caption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.client.CaptionClient;
import com.cheftory.api.recipe.content.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.content.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipe.content.caption.client.exception.CaptionClientException;
import com.cheftory.api.recipe.content.caption.entity.LangCodeType;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipe.content.caption.repository.RecipeCaptionRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCaptionService")
public class RecipeCaptionServiceTest {

  private CaptionClient captionClient;
  private RecipeCaptionRepository recipeCaptionRepository;
  private RecipeCaptionService recipeCaptionService;
  private Clock clock;

  @BeforeEach
  void setUp() {
    captionClient = mock(CaptionClient.class);
    clock = mock(Clock.class);
    recipeCaptionRepository = mock(RecipeCaptionRepository.class);
    recipeCaptionService = new RecipeCaptionService(captionClient, recipeCaptionRepository, clock);
  }

  @DisplayName("레시피 자막 생성")
  @Nested
  class CreateRecipeCaption {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {
      private String videoId;
      private UUID recipeId;
      private ClientCaptionResponse clientCaptionResponse;
      private RecipeCaption savedRecipeCaption;

      @BeforeEach
      void setUp() {
        videoId = "sample-video-id";
        recipeId = UUID.randomUUID();

        clientCaptionResponse = mock(ClientCaptionResponse.class);

        savedRecipeCaption = mock(RecipeCaption.class);
        UUID savedCaptionId = UUID.randomUUID();
        doReturn(savedCaptionId).when(savedRecipeCaption).getId();

        doReturn(clientCaptionResponse).when(captionClient).fetchCaption(videoId);

        doReturn(savedRecipeCaption).when(clientCaptionResponse).toRecipeCaption(recipeId, clock);

        doReturn(savedRecipeCaption).when(recipeCaptionRepository).save(any(RecipeCaption.class));
      }

      @DisplayName("When - 레시피 자막을 생성하면")
      @Nested
      class WhenCreateRecipeCaption {

        @Test
        @DisplayName("Then - 자막이 성공적으로 생성되고 ID가 반환된다")
        void thenReturnsCaptionId() {
          UUID result = recipeCaptionService.create(videoId, recipeId);
          assertThat(result).isEqualTo(savedRecipeCaption.getId());
          verify(captionClient).fetchCaption(videoId);
          verify(clientCaptionResponse).toRecipeCaption(recipeId, clock);
          verify(recipeCaptionRepository).save(any(RecipeCaption.class));
        }
      }
    }

    @Nested
    @DisplayName("Given - CaptionClient에서 NOT_COOK_VIDEO 예외가 발생할 때")
    class GivenNotCookVideoException {

      private String videoId;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        videoId = "not-cook-video-id";
        recipeId = UUID.randomUUID();

        doThrow(new CaptionClientException(CaptionClientErrorCode.NOT_COOK_VIDEO))
            .when(captionClient)
            .fetchCaption(videoId);
      }

      @Nested
      @DisplayName("When - 레시피 자막을 생성하면")
      class WhenCreateRecipeCaption {

        @Test
        @DisplayName("Then - NOT_COOK_RECIPE 예외가 발생한다")
        void thenThrowsNotCookRecipeException() {
          assertThatThrownBy(() -> recipeCaptionService.create(videoId, recipeId))
              .isInstanceOf(RecipeCaptionException.class)
              .hasFieldOrPropertyWithValue("errorMessage", RecipeCaptionErrorCode.NOT_COOK_RECIPE);

          verify(captionClient).fetchCaption(videoId);
        }
      }
    }

    @Nested
    @DisplayName("Given - CaptionClient에서 기타 예외가 발생할 때")
    class GivenOtherCaptionClientException {

      private String videoId;
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        videoId = "error-video-id";
        recipeId = UUID.randomUUID();

        doThrow(new CaptionClientException(CaptionClientErrorCode.SERVER_ERROR))
            .when(captionClient)
            .fetchCaption(videoId);
      }

      @Nested
      @DisplayName("When - 레시피 자막을 생성하면")
      class WhenCreateRecipeCaption {

        @Test
        @DisplayName("Then - CAPTION_CREATE_FAIL 예외가 발생한다")
        void thenThrowsCaptionCreateFailException() {
          assertThatThrownBy(() -> recipeCaptionService.create(videoId, recipeId))
              .isInstanceOf(RecipeCaptionException.class)
              .hasFieldOrPropertyWithValue(
                  "errorMessage", RecipeCaptionErrorCode.CAPTION_CREATE_FAIL);

          verify(captionClient).fetchCaption(videoId);
        }
      }
    }
  }

  @DisplayName("레시피 ID로 자막 조회")
  @Nested
  class FindByRecipeId {

    @Nested
    @DisplayName("Given - 존재하는 레시피 ID가 주어졌을 때")
    class GivenExistingRecipeId {
      private UUID recipeId;
      private RecipeCaption expectedCaption;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        expectedCaption = mock(RecipeCaption.class);
        doReturn(Optional.of(expectedCaption))
            .when(recipeCaptionRepository)
            .findByRecipeId(recipeId);
      }

      @Test
      @DisplayName("When - 자막을 조회하면 Then - 해당 자막이 반환된다")
      void whenFindByRecipeId_thenReturnsCaption() {
        RecipeCaption result = recipeCaptionService.findByRecipeId(recipeId);
        assertThat(result).isEqualTo(expectedCaption);
        verify(recipeCaptionRepository).findByRecipeId(recipeId);
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistingRecipeId {
      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        doReturn(Optional.empty()).when(recipeCaptionRepository).findByRecipeId(recipeId);
      }

      @Test
      @DisplayName("When - 자막을 조회하면 Then - RecipeCaptionException이 발생한다")
      void whenFindByRecipeId_thenThrowsException() {
        assertThatThrownBy(() -> recipeCaptionService.findByRecipeId(recipeId))
            .isInstanceOf(RecipeCaptionException.class)
            .hasFieldOrPropertyWithValue("errorMessage", RecipeCaptionErrorCode.CAPTION_NOT_FOUND);
        verify(recipeCaptionRepository).findByRecipeId(recipeId);
      }
    }
  }

  @DisplayName("자막 ID로 자막 조회")
  @Nested
  class FindById {

    @Nested
    @DisplayName("Given - 존재하는 자막 ID가 주어졌을 때")
    class GivenExistingCaptionId {
      private UUID captionId;
      private RecipeCaption expectedCaption;

      @BeforeEach
      void setUp() {
        captionId = UUID.randomUUID();
        expectedCaption = mock(RecipeCaption.class);
        doReturn(Optional.of(expectedCaption)).when(recipeCaptionRepository).findById(captionId);
      }

      @Test
      @DisplayName("When - 자막을 조회하면 Then - 해당 자막이 반환된다")
      void whenFindById_thenReturnsCaption() {
        RecipeCaption result = recipeCaptionService.get(captionId);
        assertThat(result).isEqualTo(expectedCaption);
        verify(recipeCaptionRepository).findById(captionId);
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 자막 ID가 주어졌을 때")
    class GivenNonExistingCaptionId {
      private UUID captionId;

      @BeforeEach
      void setUp() {
        captionId = UUID.randomUUID();
        doReturn(Optional.empty()).when(recipeCaptionRepository).findById(captionId);
      }

      @Test
      @DisplayName("When - 자막을 조회하면 Then - RecipeCaptionException이 발생한다")
      void whenFindById_thenThrowsException() {
        assertThatThrownBy(() -> recipeCaptionService.get(captionId))
            .isInstanceOf(RecipeCaptionException.class)
            .hasFieldOrPropertyWithValue("errorMessage", RecipeCaptionErrorCode.CAPTION_NOT_FOUND);
        verify(recipeCaptionRepository).findById(captionId);
      }
    }
  }

  @DisplayName("ClientCaptionResponse DTO 변환 테스트")
  @Nested
  class ClientCaptionResponseTest {

    @Test
    @DisplayName("ClientCaptionResponse.toRecipeCaption() - 정상 변환")
    void shouldConvertToRecipeCaptionCorrectly() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      // JSON으로부터 ClientCaptionResponse 생성
      String jsonResponse =
          """
          {
            "lang_code": "ko",
            "captions": [
              {
                "start": 0.0,
                "end": 2.0,
                "text": "안녕하세요"
              },
              {
                "start": 2.0,
                "end": 4.0,
                "text": "요리를 시작하겠습니다"
              }
            ]
          }
          """;

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      ClientCaptionResponse clientResponse =
          objectMapper.readValue(jsonResponse, ClientCaptionResponse.class);

      // when
      RecipeCaption result = clientResponse.toRecipeCaption(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getLangCode()).isEqualTo(LangCodeType.ko);
      assertThat(result.getRecipeId()).isEqualTo(recipeId);
      assertThat(result.getSegments()).hasSize(2);
      assertThat(result.getSegments().get(0).getText()).isEqualTo("안녕하세요");
      assertThat(result.getSegments().get(0).getStart()).isEqualTo(0.0);
      assertThat(result.getSegments().get(0).getEnd()).isEqualTo(2.0);
      assertThat(result.getSegments().get(1).getText()).isEqualTo("요리를 시작하겠습니다");
      assertThat(result.getSegments().get(1).getStart()).isEqualTo(2.0);
      assertThat(result.getSegments().get(1).getEnd()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("ClientCaptionResponse.toRecipeCaption() - 빈 세그먼트")
    void shouldConvertWithEmptySegments() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      String jsonResponse =
          """
          {
            "lang_code": "en",
            "captions": []
          }
          """;

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      ClientCaptionResponse clientResponse =
          objectMapper.readValue(jsonResponse, ClientCaptionResponse.class);

      // when
      RecipeCaption result = clientResponse.toRecipeCaption(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getLangCode()).isEqualTo(LangCodeType.en);
      assertThat(result.getRecipeId()).isEqualTo(recipeId);
      assertThat(result.getSegments()).isEmpty();
    }

    @Test
    @DisplayName("ClientCaptionResponse.toRecipeCaption() - 다양한 언어 코드")
    void shouldConvertWithDifferentLangCodes() throws Exception {
      // given
      UUID recipeId = UUID.randomUUID();
      Clock mockClock = mock(Clock.class);

      String jsonResponse =
          """
          {
            "lang_code": "en",
            "captions": [
              {
                "start": 0.0,
                "end": 1.5,
                "text": "Hello world"
              }
            ]
          }
          """;

      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      ClientCaptionResponse clientResponse =
          objectMapper.readValue(jsonResponse, ClientCaptionResponse.class);

      // when
      RecipeCaption result = clientResponse.toRecipeCaption(recipeId, mockClock);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getLangCode()).isEqualTo(LangCodeType.en);
      assertThat(result.getRecipeId()).isEqualTo(recipeId);
      assertThat(result.getSegments()).hasSize(1);
      assertThat(result.getSegments().get(0).getText()).isEqualTo("Hello world");
    }
  }
}
