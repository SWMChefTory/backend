package com.cheftory.api.voicecommand;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api.voicecommand.dto.VoiceCommandHistoryCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/papi/v1/voice-command")
@RequiredArgsConstructor
@Slf4j
public class VoiceCommandHistoryController {

    private final VoiceCommandHistoryService voiceCommandHistoryService;

    @PostMapping("")
    public ResponseEntity<SuccessOnlyResponse> createVoiceCommandHistory(
            @Valid @RequestBody VoiceCommandHistoryCreateRequest request
    ){
        voiceCommandHistoryService.create(request.transcribe(),  request.result(),
                                   request.userId(), request.sttModel(), request.intentModel());
        return ResponseEntity.ok(new SuccessOnlyResponse());
    }
}
