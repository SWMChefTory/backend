package com.cheftory.api.voicecommand;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
    @DisplayName("When - create를 호출할 때")
    class WhenCreate {

      @Test
      @DisplayName("Then - Repository의 save가 호출되어야 한다")
      public void thenShouldCallRepositorySave() {

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
}