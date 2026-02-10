package com.cheftory.api.credit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 크레딧 잔액 응답 DTO.
 *
 * @param balance 사용자의 현재 크레딧 잔액
 */
public record CreditBalanceResponse(@JsonProperty("balance") long balance) {
    /**
     * 크레딧 잔액으로부터 응답 DTO를 생성합니다.
     *
     * @param balance 크레딧 잔액
     * @return 크레딧 잔액 응답 DTO
     */
    public static CreditBalanceResponse from(long balance) {
        return new CreditBalanceResponse(balance);
    }
}
