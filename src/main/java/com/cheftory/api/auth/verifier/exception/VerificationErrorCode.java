package com.cheftory.api.auth.verifier.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

/**
 * OAuth 토큰 검증 관련 에러 코드
 */
@Getter
public enum VerificationErrorCode implements Error {

    // Apple 관련
    /**
     * Apple 토큰 형식이 유효하지 않음
     */
    APPLE_INVALID_FORMAT("APPLE_001", "Apple 토큰 형식이 유효하지 않습니다.", ErrorType.VALIDATION),
    /**
     * Apple 공개키를 찾을 수 없음
     */
    APPLE_PUBLIC_KEY_NOT_FOUND("APPLE_002", "Apple 공개키를 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    /**
     * Apple 토큰의 서명 검증 실패
     */
    APPLE_SIGNATURE_VERIFICATION_FAILED("APPLE_003", "Apple 토큰의 서명 검증에 실패했습니다.", ErrorType.INTERNAL),
    /**
     * Apple 토큰 발급자가 유효하지 않음
     */
    APPLE_INVALID_ISSUER("APPLE_004", "Apple 토큰 발급자가 유효하지 않습니다.", ErrorType.VALIDATION),
    /**
     * Apple 토큰 대상 클라이언트 정보가 유효하지 않음
     */
    APPLE_INVALID_AUDIENCE("APPLE_005", "Apple 토큰 대상 클라이언트 정보가 유효하지 않습니다.", ErrorType.VALIDATION),
    /**
     * Apple 토큰의 서명 알고리즘이 올바르지 않음
     */
    APPLE_INVALID_ALGORITHM("APPLE_006", "Apple 토큰의 서명 알고리즘이 올바르지 않습니다.", ErrorType.VALIDATION),
    /**
     * Apple 토큰이 만료됨
     */
    APPLE_TOKEN_EXPIRED("APPLE_007", "Apple 토큰이 만료되었습니다.", ErrorType.VALIDATION),

    // Google 관련
    /**
     * Google 토큰 검증 응답 실패
     */
    GOOGLE_RESPONSE_NOT_OK("GOOGLE_001", "Google 토큰 검증 응답이 실패했습니다.", ErrorType.VALIDATION),
    /**
     * Google 토큰에 이메일 정보가 없음
     */
    GOOGLE_MISSING_EMAIL("GOOGLE_002", "Google 토큰에 이메일 정보가 없습니다.", ErrorType.VALIDATION),
    /**
     * Google 토큰에 SUB 정보가 없음
     */
    GOOGLE_MISSING_SUB("GOOGLE_003", "Google 토큰에 SUB 정보가 없습니다.", ErrorType.VALIDATION),

    // 공통 오류 코드
    /**
     * 알 수 없는 오류 발생
     */
    UNKNOWN_ERROR("COMMON_001", "알 수 없는 오류가 발생했습니다.", ErrorType.INTERNAL);

    private final String code;
    private final String message;
    private final ErrorType type;

    VerificationErrorCode(String code, String message, ErrorType type) {
        this.code = code;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
