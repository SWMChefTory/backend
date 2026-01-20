package com.cheftory.api.voicecommand;

import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandHistoryService {

    private final VoiceCommandHistoryRepository voiceCommandHistoryRepository;

    public void create(
            String transcribe,
            String result,
            UUID userId,
            String sttModel,
            String intentModel,
            Integer start,
            Integer end) {
        VoiceCommandHistory voiceCommandHistory =
                VoiceCommandHistory.create(sttModel, transcribe, intentModel, result, userId, start, end);
        voiceCommandHistoryRepository.save(voiceCommandHistory);
    }
}
