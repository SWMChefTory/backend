package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationException;

/**
 * Apple JWKS (JSON Web Key Set) 조회용 클라이언트
 */
public interface AppleTokenClient {

    /**
     * Apple의 공개키 목록(JWKS)을 조회합니다.
     *
     * @return JWKS 응답
     * @throws VerificationException 조회 실패 시
     */
    AppleJwksResponse fetchJwks() throws VerificationException;
}
