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

@DisplayName("VoiceCommandHistoryService 테스트")
public class VoiceCommandHistoryServiceTest {

    private VoiceCommandHistoryRepository voiceCommandHistoryRepository;
    private VoiceCommandHistoryService voiceCommandHistoryService;

    @BeforeEach
    void setUp() {
        voiceCommandHistoryRepository = mock(VoiceCommandHistoryRepository.class);
        voiceCommandHistoryService = new VoiceCommandHistoryService(voiceCommandHistoryRepository);
    }

    @Nested
    @DisplayName("음성 명령 기록 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String baseIntent;
            String intent;
            UUID userId;
            String sttModel;
            String intentModel;
            Integer start;
            Integer end;

            @BeforeEach
            void setUp() {
                baseIntent = "testBaseIntent";
                intent = "testIntent";
                userId = UUID.randomUUID();
                sttModel = "VITO";
                intentModel = "GPT4.1";
                start = 1;
                end = 2;
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - 올바른 파라미터로 저장한다")
                void thenSavesCorrectly() throws VoiceCommandHistoryException {
                    voiceCommandHistoryService.create(baseIntent, intent, userId, sttModel, intentModel, start, end);

                    verify(voiceCommandHistoryRepository).save(argThat(voiceCommand -> {
                        try {
                            return voiceCommand.getTranscribe().equals(baseIntent)
                                    && voiceCommand.getResult().equals(intent)
                                    && voiceCommand.getUserId().equals(userId)
                                    && voiceCommand.getSttModel().equals(STTModel.fromValue(sttModel))
                                    && voiceCommand.getIntentModel().equals(IntentModel.fromValue(intentModel))
                                    && voiceCommand.getStart().equals(start)
                                    && voiceCommand.getEnd().equals(end);
                        } catch (VoiceCommandHistoryException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 STT 모델일 때")
        class GivenInvalidSttModel {
            String invalidSttModel;

            @BeforeEach
            void setUp() {
                invalidSttModel = "INVALID_STT";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> voiceCommandHistoryService.create(
                                    "base", "intent", UUID.randomUUID(), invalidSttModel, "GPT4.1", 1, 2))
                            .isInstanceOf(VoiceCommandHistoryException.class);
                }
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 Intent 모델일 때")
        class GivenInvalidIntentModel {
            String invalidIntentModel;

            @BeforeEach
            void setUp() {
                invalidIntentModel = "INVALID_INTENT";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> voiceCommandHistoryService.create(
                                    "base", "intent", UUID.randomUUID(), "VITO", invalidIntentModel, 1, 2))
                            .isInstanceOf(VoiceCommandHistoryException.class);
                }
            }
        }
    }
}
