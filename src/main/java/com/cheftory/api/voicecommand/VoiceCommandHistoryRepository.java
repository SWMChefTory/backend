package com.cheftory.api.voicecommand;

import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoiceCommandHistoryRepository extends JpaRepository<VoiceCommandHistory, UUID> {}
