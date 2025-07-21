package com.cheftory.api.voicecommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandHistoryService {

    private final VoiceCommandHistoryRepository voiceCommandHistoryRepository;

    public void create(String transcribe, String result, UUID userId, String sttModel, String intentModel) {
        VoiceCommandHistory voiceCommandHistory = VoiceCommandHistory.create(sttModel,transcribe, intentModel, result, userId);
        voiceCommandHistoryRepository.save(voiceCommandHistory);
    }
}
