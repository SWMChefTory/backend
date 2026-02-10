package com.cheftory.api._common.reponse;

import lombok.Getter;

/**
 * 성공 응답 DTO.
 *
 * @param message 성공 메시지
 */
public record SuccessOnlyResponse(@Getter String message) {
    /**
     * 기본 성공 응답을 생성합니다.
     *
     * @return 성공 응답
     */
    public static SuccessOnlyResponse create() {
        return new SuccessOnlyResponse("success");
    }
}
