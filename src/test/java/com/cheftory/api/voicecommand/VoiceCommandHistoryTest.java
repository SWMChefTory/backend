package com.cheftory.api.voicecommand;

import static org.assertj.core.api.Assertions.*;

import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.exception.VoiceCommandErrorCode;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("VoiceCommandHistory 엔티티")
public class VoiceCommandHistoryTest {

    @Nested
    @DisplayName("생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String sttModel;
            String intentGPT4Model;
            String intentNLUModel;
            String transcribe;
            String result;
            UUID userId;
            Integer start;
            Integer end;

            @BeforeEach
            void setUp() {
                sttModel = "VITO";
                intentGPT4Model = "GPT4.1";
                intentNLUModel = "NLU";
                transcribe = "hello world";
                result = "greeting";
                userId = UUID.randomUUID();
                start = 1;
                end = 2;
            }

            @Nested
            @DisplayName("When - GPT4.1 모델로 생성하면")
            class WhenGPT4 {
                VoiceCommandHistory history;

                @BeforeEach
                void setUp() throws VoiceCommandHistoryException {
                    history = VoiceCommandHistory.create(
                            sttModel, transcribe, intentGPT4Model, result, userId, start, end);
                }

                @Test
                @DisplayName("Then - 올바르게 생성된다")
                void thenCreatedCorrectly() {
                    assertThat(history.getId()).isNotNull();
                    assertThat(history.getSttModel()).isEqualTo(STTModel.VITO);
                    assertThat(history.getTranscribe()).isEqualTo(transcribe);
                    assertThat(history.getIntentModel()).isEqualTo(IntentModel.GPT4_1);
                    assertThat(history.getResult()).isEqualTo(result);
                    assertThat(history.getUserId()).isEqualTo(userId);
                    assertThat(history.getCreatedAt()).isNotNull();
                    assertThat(history.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
                    assertThat(history.getStart()).isEqualTo(start);
                    assertThat(history.getEnd()).isEqualTo(end);
                }
            }

            @Nested
            @DisplayName("When - NLU 모델로 생성하면")
            class WhenNLU {
                VoiceCommandHistory history;

                @BeforeEach
                void setUp() throws VoiceCommandHistoryException {
                    history = VoiceCommandHistory.create(
                            sttModel, transcribe, intentNLUModel, result, userId, start, end);
                }

                @Test
                @DisplayName("Then - 올바르게 생성된다")
                void thenCreatedCorrectly() {
                    assertThat(history.getSttModel()).isEqualTo(STTModel.VITO);
                    assertThat(history.getIntentModel()).isEqualTo(IntentModel.NLU);
                    assertThat(history.getTranscribe()).isEqualTo(transcribe);
                    assertThat(history.getResult()).isEqualTo(result);
                    assertThat(history.getUserId()).isEqualTo(userId);
                    assertThat(history.getStart()).isEqualTo(start);
                    assertThat(history.getEnd()).isEqualTo(end);
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 STT 모델일 때")
        class GivenInvalidSttModel {

            @Test
            @DisplayName("Then - UNKNOWN_STT_MODEL 예외를 던진다")
            void thenThrowsException() {
                assertThatThrownBy(() ->
                                VoiceCommandHistory.create("INVALID", "text", "GPT4.1", "res", UUID.randomUUID(), 1, 2))
                        .isInstanceOf(VoiceCommandHistoryException.class)
                        .satisfies(ex -> {
                            VoiceCommandHistoryException exception = (VoiceCommandHistoryException) ex;
                            assertThat(exception.getError())
                                    .isEqualTo(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_STT_MODEL);
                        });
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 Intent 모델일 때")
        class GivenInvalidIntentModel {

            @Test
            @DisplayName("Then - UNKNOWN_INTENT_MODEL 예외를 던진다")
            void thenThrowsException() {
                assertThatThrownBy(() ->
                                VoiceCommandHistory.create("VITO", "text", "INVALID", "res", UUID.randomUUID(), 1, 2))
                        .isInstanceOf(VoiceCommandHistoryException.class)
                        .satisfies(ex -> {
                            VoiceCommandHistoryException exception = (VoiceCommandHistoryException) ex;
                            assertThat(exception.getError())
                                    .isEqualTo(VoiceCommandErrorCode.VOICE_COMMAND_UNKNOWN_INTENT_MODEL);
                        });
            }
        }
    }
}
