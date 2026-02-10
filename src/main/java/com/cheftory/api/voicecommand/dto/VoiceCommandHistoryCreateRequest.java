package com.cheftory.api.voicecommand.dto;

import com.cheftory.api.user.validator.ExistsUserId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 보이스 커맨드 히스토리 생성 요청 DTO.
 *
 * @param transcribe 음성 변환 텍스트
 * @param result 의도 파싱 결과
 * @param userId 사용자 ID
 * @param sttModel STT 모델 종류
 * @param intentModel 의도 파악 모델 종류
 * @param start 음성 인식 시작 시간
 * @param end 음성 인식 종료 시간
 */
public record VoiceCommandHistoryCreateRequest(
        @NotNull @JsonProperty("transcribe") String transcribe,
        @NotNull @JsonProperty("intent") String result,
        @NotNull @ExistsUserId @JsonProperty("user_id") UUID userId,
        @NotNull @JsonProperty("stt_model") String sttModel,
        @NotNull @JsonProperty("intent_model") String intentModel,
        @NotNull @JsonProperty("start") Integer start,
        @NotNull @JsonProperty("end") Integer end) {}
