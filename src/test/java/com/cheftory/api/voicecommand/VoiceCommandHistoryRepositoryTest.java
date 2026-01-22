package com.cheftory.api.voicecommand;

import static org.assertj.core.api.Assertions.*;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("VoiceCommand Repository")
public class VoiceCommandHistoryRepositoryTest extends DbContextTest {

    @Autowired
    private VoiceCommandHistoryRepository repository;

    @Nested
    @DisplayName("음성 명령 기록 저장")
    class SaveVoiceCommandHistory {

        @Nested
        @DisplayName("Given - 유효한 음성 명령이 주어졌을 때")
        class GivenValidVoiceCommandHistory {

            private UUID userId;
            private String baseIntent;
            private String intent;
            private String sttModel;
            private String intentModel;
            private Integer start;
            private Integer end;
            private VoiceCommandHistory voiceCommandHistory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                baseIntent = "testBaseIntent";
                intent = "testIntent";
                sttModel = "VITO";
                intentModel = "GPT4.1";
                start = 1;
                end = 2;
                voiceCommandHistory =
                        VoiceCommandHistory.create(sttModel, baseIntent, intentModel, intent, userId, start, end);
            }

            @Nested
            @DisplayName("When - 음성 명령을 저장한다면")
            class WhenSavingVoiceCommand {

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
                @DisplayName("Then - 모든 필드가 보존되어야 한다")
                public void thenAllFieldsShouldBePreserved() {
                    VoiceCommandHistory found = repository
                            .findById(savedVoiceCommandHistory.getId())
                            .orElseThrow();

                    assertThat(found.getTranscribe()).isEqualTo(baseIntent);
                    assertThat(found.getResult()).isEqualTo(intent);
                    assertThat(found.getUserId()).isEqualTo(userId);
                    assertThat(found.getSttModel()).isEqualTo(STTModel.VITO);
                    assertThat(found.getIntentModel()).isEqualTo(IntentModel.GPT4_1);
                    assertThat(found.getStart()).isEqualTo(start);
                    assertThat(found.getEnd()).isEqualTo(end);
                    assertThat(found.getCreatedAt()).isNotNull();
                }
            }
        }
    }

    @Nested
    @DisplayName("음성 명령 기록 조회")
    class FindVoiceCommandHistory {

        @Nested
        @DisplayName("Given - 저장된 음성 명령이 존재할 때")
        class GivenSavedVoiceCommandExists {

            private UUID userId;
            private String baseIntent;
            private String intent;
            private String sttModel;
            private String intentModel;
            private Integer start;
            private Integer end;
            private VoiceCommandHistory savedVoiceCommandHistory;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                baseIntent = "testBaseIntent";
                intent = "testIntent";
                sttModel = "VITO";
                intentModel = "GPT4.1";
                start = 1;
                end = 2;

                VoiceCommandHistory voiceCommandHistory =
                        VoiceCommandHistory.create(sttModel, baseIntent, intentModel, intent, userId, start, end);
                savedVoiceCommandHistory = repository.save(voiceCommandHistory);
            }

            @Nested
            @DisplayName("When - ID로 조회한다면")
            class WhenFindingById {

                @Test
                @DisplayName("Then - 올바른 데이터를 반환해야 한다")
                public void thenShouldReturnCorrectData() {
                    VoiceCommandHistory found = repository
                            .findById(savedVoiceCommandHistory.getId())
                            .orElseThrow();

                    assertThat(found.getId()).isEqualTo(savedVoiceCommandHistory.getId());
                    assertThat(found.getTranscribe()).isEqualTo(baseIntent);
                    assertThat(found.getResult()).isEqualTo(intent);
                    assertThat(found.getUserId()).isEqualTo(userId);
                    assertThat(found.getSttModel()).isEqualTo(STTModel.VITO);
                    assertThat(found.getIntentModel()).isEqualTo(IntentModel.GPT4_1);
                    assertThat(found.getStart()).isEqualTo(start);
                    assertThat(found.getEnd()).isEqualTo(end);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 ID가 주어졌을 때")
        class GivenNonExistentId {

            private UUID nonExistentId;

            @BeforeEach
            void setUp() {
                nonExistentId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - ID로 조회한다면")
            class WhenFindingById {

                @Test
                @DisplayName("Then - 빈 결과를 반환해야 한다")
                public void thenShouldReturnEmpty() {
                    var result = repository.findById(nonExistentId);
                    assertThat(result).isEmpty();
                }
            }
        }
    }
}
