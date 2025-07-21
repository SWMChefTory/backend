package com.cheftory.api.voicecommand;

import com.cheftory.api.DbContextTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VoiceCommand Repository")
public class VoiceCommandHistoryRepositoryTest extends DbContextTest {

  @Autowired
  private VoiceCommandHistoryRepository repository;

  @Nested
  @DisplayName("Given - 유효한 음성명령이 주어졌을 때")
  class GivenValidVoiceCommandHistory {

    private UUID userId;
    private String baseIntent;
    private String intent;
    private String sttModel;
    private String intentModel;
    private VoiceCommandHistory voiceCommandHistory;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      baseIntent = "testBaseIntent";
      intent = "testIntent";
      sttModel = "VITO";
      intentModel = "GPT4.1";
      voiceCommandHistory = VoiceCommandHistory.create(sttModel, baseIntent, intentModel, intent, userId);
    }

    @Nested
    @DisplayName("When - 음성명령을 저장할 때")
    class WhenSaving {

      private VoiceCommandHistory savedVoiceCommandHistory;

      @BeforeEach
      void beforeEach() {
        savedVoiceCommandHistory = repository.save(voiceCommandHistory);
      }

      @Test
      @DisplayName("Then - 생성된 ID와 함께 저장되어야 한다")
      public void thenShouldPersistWithGeneratedId() {

        assertThat(savedVoiceCommandHistory.getId()).isNotNull();
        assertThat(savedVoiceCommandHistory.getId()).isEqualTo(voiceCommandHistory.getId());

      }

      @Test
      @DisplayName("Then - ID로 조회가 가능해야 한다")
      public void thenShouldBeRetrievableById() {

        VoiceCommandHistory found = repository.findById(savedVoiceCommandHistory.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(savedVoiceCommandHistory.getId());
        assertThat(found.getTranscribe()).isEqualTo(baseIntent);
        assertThat(found.getResult()).isEqualTo(intent);
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getSttModel()).isEqualTo(STTModel.VITO);
        assertThat(found.getIntentModel()).isEqualTo(IntentModel.GPT4_1);

      }

      @Test
      @DisplayName("Then - 모든 필드가 보존되어야 한다")
      public void thenAllFieldsShouldBePreserved() {

        VoiceCommandHistory found = repository.findById(savedVoiceCommandHistory.getId()).orElseThrow();

        assertThat(found.getTranscribe()).isEqualTo(baseIntent);
        assertThat(found.getResult()).isEqualTo(intent);
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getSttModel()).isEqualTo(STTModel.VITO);
        assertThat(found.getIntentModel()).isEqualTo(IntentModel.GPT4_1);
        assertThat(found.getCreatedAt()).isNotNull();
      }
    }
  }
}