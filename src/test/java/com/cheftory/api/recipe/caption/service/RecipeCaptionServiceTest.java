package com.cheftory.api.recipe.caption.service;

import com.cheftory.api.recipe.caption.RecipeCaptionService;
import com.cheftory.api.recipe.caption.client.CaptionClient;
import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.client.exception.CaptionClientErrorCode;
import com.cheftory.api.recipe.caption.client.exception.CaptionClientException;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.exception.CaptionErrorCode;
import com.cheftory.api.recipe.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipe.caption.repository.RecipeCaptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.apache.groovy.json.internal.IO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeCaptionServiceTest {

  @InjectMocks
  private RecipeCaptionService recipeCaptionService;

  @Mock
  private CaptionClient captionClient;

  @Mock
  private RecipeCaptionRepository recipeCaptionRepository;

  private static ObjectMapper objectMapper;

  @BeforeAll
  static void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Nested
  @DisplayName("Given : 유효한 videoId와 recipeId가 주어질 때")
  class GivenValidVideoIdAndRecipeId {

    private String videoId;
    private UUID recipeId;
    private ClientCaptionResponse clientCaptionResponse;
    private RecipeCaption recipeCaption;
    private UUID captionId;


    @BeforeEach
    void setUp() throws IOException {
      videoId = "validVideoId123";
      recipeId = UUID.randomUUID();
      captionId = UUID.randomUUID();

      InputStream is = getClass()
          .getClassLoader()
          .getResourceAsStream("captions/caption_client_response.json");

      ObjectMapper mapper = new ObjectMapper();
      clientCaptionResponse = mapper.readValue(is, ClientCaptionResponse.class);

      recipeCaption = mock(RecipeCaption.class);
      when(recipeCaption.getId()).thenReturn(captionId);
    }

    @Nested
    @DisplayName("When : 자막을 생성한다면")
    class WhenCreateCaption {

      @BeforeEach
      void setUp() {
        when(captionClient.fetchCaption(videoId)).thenReturn(clientCaptionResponse);
        when(recipeCaptionRepository.save(any(RecipeCaption.class))).thenReturn(recipeCaption);
      }

      @Test
      @DisplayName("Then : 성공적으로 자막이 생성되고 captionId를 반환한다.")
      void then() {
        UUID result = recipeCaptionService.create(videoId, recipeId);

        assertNotNull(result);
        assertEquals(captionId, result);

        verify(captionClient, times(1)).fetchCaption(videoId);
        verify(recipeCaptionRepository, times(1)).save(any(RecipeCaption.class));
      }
    }
  }


  @Nested
  @DisplayName("Given : 유효하지 않은 videoId(요리 비디오가 아닌 id)가 주어질 때")
  class GivenInValidVideoId {

    private String invalidVideoId;
    private UUID recipeId;

    @BeforeEach
    void setUp() throws IOException {
      invalidVideoId = "videoId123";
      recipeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("When : 자막을 생성한다면")
    class WhenCreateCaption {

      @BeforeEach
      void setUp() {
        when(captionClient.fetchCaption(invalidVideoId)).thenThrow(new CaptionClientException(
            CaptionClientErrorCode.NOT_COOK_VIDEO));
      }

      @Test
      @DisplayName("Then : 자막이 저장되지 않고, CaptionException(NOT_COOK_ID)가 발생한다.")
      void then() {
        CaptionClientException exception = assertThrows(CaptionClientException.class,
            () -> recipeCaptionService.create(invalidVideoId, recipeId));

        assertEquals(CaptionErrorCode.NOT_COOK_ID, exception.getErrorMessage());
      }
    }
  }


  @Nested
  @DisplayName("Given : 존재하는 captionId가 주어질 때")
  class GivenExistingCaptionId {

    private UUID captionId;
    private RecipeCaption recipeCaption;
    private CaptionInfo expectedCaptionInfo;

    @BeforeEach
    void setUp() {
      captionId = UUID.randomUUID();
      recipeCaption = mock(RecipeCaption.class);
      expectedCaptionInfo = mock(CaptionInfo.class);
    }

    @Nested
    @DisplayName("When : 자막 정보를 조회한다면")
    class WhenFindCaptionInfo {

      @BeforeEach
      void setUp() {
        when(recipeCaptionRepository.findById(captionId)).thenReturn(Optional.of(recipeCaption));
        mockStatic(CaptionInfo.class);
        when(CaptionInfo.from(recipeCaption)).thenReturn(expectedCaptionInfo);
      }

      @Test
      @DisplayName("Then : 성공적으로 자막 정보를 반환한다.")
      void then() {
        CaptionInfo result = recipeCaptionService.findCaptionInfo(captionId);

        assertNotNull(result);
        assertEquals(expectedCaptionInfo, result);

        verify(recipeCaptionRepository, times(1)).findById(captionId);
      }
    }
  }

  @Nested
  @DisplayName("Given : 존재하지 않는 captionId가 주어질 때")
  class GivenNonExistingCaptionId {

    private UUID nonExistingCaptionId;

    @BeforeEach
    void setUp() {
      nonExistingCaptionId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("When : 자막 정보를 조회한다면")
    class WhenFindCaptionInfo {

      @BeforeEach
      void setUp() {
        when(recipeCaptionRepository.findById(nonExistingCaptionId)).thenReturn(Optional.empty());
      }

      @Test
      @DisplayName("Then : RecipeCaptionException(CAPTION_NOT_FOUND)이 발생한다.")
      void then() {
        RecipeCaptionException exception = assertThrows(
            RecipeCaptionException.class,
            () -> recipeCaptionService.findCaptionInfo(nonExistingCaptionId)
        );

        assertEquals(CaptionErrorCode.CAPTION_NOT_FOUND, exception.getErrorMessage());

        verify(recipeCaptionRepository, times(1)).findById(nonExistingCaptionId);
      }
    }
  }
}