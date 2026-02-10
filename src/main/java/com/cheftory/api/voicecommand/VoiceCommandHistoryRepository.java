package com.cheftory.api.voicecommand;

import com.cheftory.api.voicecommand.model.VoiceCommandHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 보이스 커맨드 히스토리 JPA 리포지토리.
 */
public interface VoiceCommandHistoryRepository extends JpaRepository<VoiceCommandHistory, UUID> {}
