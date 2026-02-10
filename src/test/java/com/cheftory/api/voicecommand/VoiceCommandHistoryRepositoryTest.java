package com.cheftory.api.voicecommand;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("VoiceCommandHistoryRepository 테스트")
public class VoiceCommandHistoryRepositoryTest extends DbContextTest {

    @Autowired
    private VoiceCommandHistoryRepository repository;

    @Nested
    @DisplayName("음성 명령 기록 저장 (save)")
    class Save {

        @Nested
        @DisplayName("Given - 유효한 음성 명령이 주어졌을 때")
        class GivenValidHistory {
            UUID userId;
            String baseIntent;
            String intent;
            String sttModel;
            String intentModel;
            Integer start;
            Integer end;
            VoiceCommandHistory voiceCommandHistory;

            @BeforeEach
            void setUp() throws VoiceCommandHistoryException {
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
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {
                VoiceCommandHistory saved;

                @BeforeEach
                void setUp() {
                    saved = repository.save(voiceCommandHistory);
                }

                @Test
                @DisplayName("Then - ID가 생성되고 모든 필드가 저장된다")
                void thenSavesCorrectly() {
                    assertThat(saved.getId()).isNotNull();
                    assertThat(saved.getId()).isEqualTo(voiceCommandHistory.getId());

                    VoiceCommandHistory found =
                            repository.findById(saved.getId()).orElseThrow();
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
    @DisplayName("음성 명령 기록 조회 (findById)")
    class FindById {

        @Nested
        @DisplayName("Given - 저장된 음성 명령이 있을 때")
        class GivenSavedHistory {
            VoiceCommandHistory saved;

            @BeforeEach
            void setUp() throws VoiceCommandHistoryException {
                VoiceCommandHistory history =
                        VoiceCommandHistory.create("VITO", "base", "GPT4.1", "intent", UUID.randomUUID(), 1, 2);
                saved = repository.save(history);
            }

            @Nested
            @DisplayName("When - ID로 조회하면")
            class WhenFinding {
                VoiceCommandHistory found;

                @BeforeEach
                void setUp() {
                    found = repository.findById(saved.getId()).orElseThrow();
                }

                @Test
                @DisplayName("Then - 저장된 데이터를 반환한다")
                void thenReturnsData() {
                    assertThat(found.getId()).isEqualTo(saved.getId());
                    assertThat(found.getTranscribe()).isEqualTo(saved.getTranscribe());
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 ID일 때")
        class GivenNonExistentId {
            UUID id;

            @BeforeEach
            void setUp() {
                id = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - ID로 조회하면")
            class WhenFinding {

                @Test
                @DisplayName("Then - 빈 Optional을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(repository.findById(id)).isEmpty();
                }
            }
        }
    }
}
