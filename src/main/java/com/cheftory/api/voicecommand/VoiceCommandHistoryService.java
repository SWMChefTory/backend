package com.cheftory.api.voicecommand;

import com.cheftory.api.voicecommand.exception.VoiceCommandHistoryException;
import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 보이스 커맨드 히스토리 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandHistoryService {

    private final VoiceCommandHistoryRepository voiceCommandHistoryRepository;

    /**
     * 보이스 커맨드 히스토리를 생성합니다.
     *
     * @param transcribe 음성 변환 텍스트
     * @param result 의도 파싱 결과
     * @param userId 사용자 ID
     * @param sttModel STT 모델 종류
     * @param intentModel 의도 파악 모델 종류
     * @param start 음성 인식 시작 시간
     * @param end 음성 인식 종료 시간
     * @throws VoiceCommandHistoryException 보이스 커맨드 히스토리 생성 중 오류 발생 시
     */
    public void create(
            String transcribe,
            String result,
            UUID userId,
            String sttModel,
            String intentModel,
            Integer start,
            Integer end)
            throws VoiceCommandHistoryException {
        VoiceCommandHistory voiceCommandHistory =
                VoiceCommandHistory.create(sttModel, transcribe, intentModel, result, userId, start, end);
        voiceCommandHistoryRepository.save(voiceCommandHistory);
    }
}
