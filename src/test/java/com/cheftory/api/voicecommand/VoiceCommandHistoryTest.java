package com.cheftory.api.voicecommand;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VoiceCommand Entity")
public class VoiceCommandHistoryTest {

  @Nested
  @DisplayName("음성 명령 기록 생성")
  class CreateVoiceCommandHistory {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String sttModel;
      private String intentGPT4Model;
      private String intentRegexModel;
      private String transcribe;
      private String result;
      private UUID userId;

      @BeforeEach
      void setUp() {
        sttModel = "VITO";
        intentGPT4Model = "GPT4.1";
        intentRegexModel = "REGEX";
        transcribe = "hello world";
        result = "greeting";
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - GPT4.1 모델로 생성한다면")
      class WhenCreatingWithGPT4 {

        private VoiceCommandHistory voiceCommandHistory;

        @BeforeEach
        void beforeEach() {
          voiceCommandHistory = VoiceCommandHistory.create(sttModel, transcribe, intentGPT4Model, result, userId);
        }

        @Test
        @DisplayName("Then - 올바른 속성으로 VoiceCommand가 생성되어야 한다")
        public void thenShouldCreateVoiceCommandWithCorrectProperties() {
          assertThat(voiceCommandHistory.getId()).isNotNull();
          assertThat(voiceCommandHistory.getSttModel()).isEqualTo(STTModel.VITO);
          assertThat(voiceCommandHistory.getTranscribe()).isEqualTo(transcribe);
          assertThat(voiceCommandHistory.getIntentModel()).isEqualTo(IntentModel.GPT4_1);
          assertThat(voiceCommandHistory.getResult()).isEqualTo(result);
          assertThat(voiceCommandHistory.getUserId()).isEqualTo(userId);
          assertThat(voiceCommandHistory.getCreatedAt()).isNotNull();
          assertThat(voiceCommandHistory.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }
      }

      @Nested
      @DisplayName("When - REGEX 모델로 생성한다면")
      class WhenCreatingWithRegex {

        private VoiceCommandHistory voiceCommandHistory;

        @BeforeEach
        void beforeEach() {
          voiceCommandHistory = VoiceCommandHistory.create(sttModel, transcribe, intentRegexModel, result, userId);
        }

        @Test
        @DisplayName("Then - REGEX 모델로 올바르게 생성되어야 한다")
        public void thenShouldCreateWithRegexModel() {
          assertThat(voiceCommandHistory.getSttModel()).isEqualTo(STTModel.VITO);
          assertThat(voiceCommandHistory.getIntentModel()).isEqualTo(IntentModel.REGEX);
          assertThat(voiceCommandHistory.getTranscribe()).isEqualTo(transcribe);
          assertThat(voiceCommandHistory.getResult()).isEqualTo(result);
          assertThat(voiceCommandHistory.getUserId()).isEqualTo(userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 STT 모델이 주어졌을 때")
    class GivenInvalidSttModel {

      private String invalidSttModel;
      private String validIntentModel;
      private String transcribe;
      private String result;
      private UUID userId;

      @BeforeEach
      void setUp() {
        invalidSttModel = "INVALID_MODEL";
        validIntentModel = "GPT4.1";
        transcribe = "test";
        result = "result";
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 음성 명령 기록을 생성한다면")
      class WhenCreatingVoiceCommandHistory {

        @Test
        @DisplayName("Then - STT 모델 예외가 발생해야 한다")
        public void thenShouldThrowSttModelException() {
          assertThatThrownBy(() ->
              VoiceCommandHistory.create(invalidSttModel, transcribe, validIntentModel, result, userId)
          ).isInstanceOf(VoiceCommandHistoryException.class)
              .satisfies(ex -> {
                VoiceCommandHistoryException exception = (VoiceCommandHistoryException) ex;
                assertThat(exception.getErrorMessage()).isEqualTo(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL);
              });
        }
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 Intent 모델이 주어졌을 때")
    class GivenInvalidIntentModel {

      private String validSttModel;
      private String invalidIntentModel;
      private String transcribe;
      private String result;
      private UUID userId;

      @BeforeEach
      void setUp() {
        validSttModel = "VITO";
        invalidIntentModel = "INVALID_MODEL";
        transcribe = "test";
        result = "result";
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 음성 명령 기록을 생성한다면")
      class WhenCreatingVoiceCommandHistory {

        @Test
        @DisplayName("Then - Intent 모델 예외가 발생해야 한다")
        public void thenShouldThrowIntentModelException() {
          assertThatThrownBy(() ->
              VoiceCommandHistory.create(validSttModel, transcribe, invalidIntentModel, result, userId)
          ).isInstanceOf(VoiceCommandHistoryException.class)
              .satisfies(ex -> {
                VoiceCommandHistoryException exception = (VoiceCommandHistoryException) ex;
                assertThat(exception.getErrorMessage()).isEqualTo(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL);
              });
        }
      }
    }
  }
}