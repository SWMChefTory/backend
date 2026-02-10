package com.cheftory.api.voicecommand.model;

import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보이스 커맨드 히스토리 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VoiceCommandHistory {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private STTModel sttModel;

    @Column(nullable = false)
    private String transcribe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntentModel intentModel;

    @Column(nullable = false)
    private String result;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "start_time")
    private Integer start;

    @Column(nullable = false, name = "end_time")
    private Integer end;

    /**
     * 보이스 커맨드 히스토리를 생성합니다.
     *
     * @param sttModel STT 모델 문자열 값
     * @param transcribe 음성 변환 텍스트
     * @param intentModel 의도 파악 모델 문자열 값
     * @param result 의도 파싱 결과
     * @param userId 사용자 ID
     * @param start 음성 인식 시작 시간
     * @param end 음성 인식 종료 시간
     * @return 생성된 보이스 커맨드 히스토리 엔티티
     * @throws VoiceCommandHistoryException 지원하지 않는 모델인 경우
     */
    public static VoiceCommandHistory create(
            String sttModel,
            String transcribe,
            String intentModel,
            String result,
            UUID userId,
            Integer start,
            Integer end)
            throws VoiceCommandHistoryException {
        return new VoiceCommandHistory(
                UUID.randomUUID(),
                STTModel.fromValue(sttModel),
                transcribe,
                IntentModel.fromValue(intentModel),
                result,
                userId,
                LocalDateTime.now(),
                start,
                end);
    }
}
