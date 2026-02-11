package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.nimbusds.jose.jwk.JWKSet;

/**
 * Apple JWKS (JSON Web Key Set) 조회용 클라이언트
 */
public interface AppleTokenClient {

    /**
     * Apple의 공개키 목록(JWKS)을 조회합니다.
     *
     * @return JWKS JSON 문자열
     * @throws VerificationException 조회 실패 시
     */
    String fetchJwks() throws VerificationException;
}
