package com.cheftory.api.voicecommand;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("VoiceCommand Service")
public class VoiceCommandHistoryServiceTest {

  private VoiceCommandHistoryRepository voiceCommandHistoryRepository;
  private VoiceCommandHistoryService voiceCommandHistoryService;

  @BeforeEach
  void setUp() {
    voiceCommandHistoryRepository = mock(VoiceCommandHistoryRepository.class);
    voiceCommandHistoryService = new VoiceCommandHistoryService(voiceCommandHistoryRepository);
  }

  @Nested
  @DisplayName("음성 명령 기록 생성")
  class CreateVoiceCommandHistory {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String baseIntent;
      private String intent;
      private UUID userId;
      private String sttModel;
      private String intentModel;

      @BeforeEach
      void setUp() {
        baseIntent = "testBaseIntent";
        intent = "testIntent";
        userId = UUID.randomUUID();
        sttModel = "VITO";
        intentModel = "GPT4.1";
      }

      @Nested
      @DisplayName("When - 음성 명령 기록을 생성한다면")
      class WhenCreatingVoiceCommandHistory {

        @Test
        @DisplayName("Then - 올바른 파라미터로 Repository의 save가 호출되어야 한다")
        public void thenShouldCallRepositorySaveWithCorrectParameters() {
          voiceCommandHistoryService.create(baseIntent, intent, userId, sttModel, intentModel);

          verify(voiceCommandHistoryRepository).save(argThat(voiceCommand ->
              voiceCommand.getTranscribe().equals(baseIntent) &&
                  voiceCommand.getResult().equals(intent) &&
                  voiceCommand.getUserId().equals(userId) &&
                  voiceCommand.getSttModel().equals(STTModel.fromValue(sttModel)) &&
                  voiceCommand.getIntentModel().equals(IntentModel.fromValue(intentModel))
          ));
        }
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 STT 모델이 주어졌을 때")
    class GivenInvalidSttModel {

      private String baseIntent;
      private String intent;
      private UUID userId;
      private String invalidSttModel;
      private String intentModel;

      @BeforeEach
      void setUp() {
        baseIntent = "testBaseIntent";
        intent = "testIntent";
        userId = UUID.randomUUID();
        invalidSttModel = "INVALID_STT";
        intentModel = "GPT4.1";
      }

      @Nested
      @DisplayName("When - 음성 명령 기록을 생성한다면")
      class WhenCreatingVoiceCommandHistory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        public void thenShouldThrowException() {
          assertThatThrownBy(() ->
              voiceCommandHistoryService.create(baseIntent, intent, userId, invalidSttModel, intentModel))
              .isInstanceOf(VoiceCommandHistoryException.class);
        }
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 Intent 모델이 주어졌을 때")
    class GivenInvalidIntentModel {

      private String baseIntent;
      private String intent;
      private UUID userId;
      private String sttModel;
      private String invalidIntentModel;

      @BeforeEach
      void setUp() {
        baseIntent = "testBaseIntent";
        intent = "testIntent";
        userId = UUID.randomUUID();
        sttModel = "VITO";
        invalidIntentModel = "INVALID_INTENT";
      }

      @Nested
      @DisplayName("When - 음성 명령 기록을 생성한다면")
      class WhenCreatingVoiceCommandHistory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        public void thenShouldThrowException() {
          assertThatThrownBy(() ->
              voiceCommandHistoryService.create(baseIntent, intent, userId, sttModel, invalidIntentModel))
              .isInstanceOf(VoiceCommandHistoryException.class);
        }
      }
    }
  }
}