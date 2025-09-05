package com.cheftory.api.voicecommand.model;

import com.cheftory.api.voicecommand.enums.IntentModel;
import com.cheftory.api.voicecommand.enums.STTModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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

  @Column(nullable = false)
  private String awsAudioFileName;

  public static VoiceCommandHistory create(
      String sttModel,
      String transcribe,
      String intentModel,
      String result,
      UUID userId,
      String awsAudioFileName
  ) {
    return new VoiceCommandHistory(
        UUID.randomUUID(),
        STTModel.fromValue(sttModel),
        transcribe,
        IntentModel.fromValue(intentModel),
        result,
        userId,
        LocalDateTime.now(),
        awsAudioFileName
    );
  }
}