package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationException;
import tools.jackson.databind.JsonNode;

/**
 * Google Token Info 조회용 클라이언트
 */
public interface GoogleTokenClient {

    /**
     * Google Token Info API를 통해 토큰을 검증하고 payload를 조회합니다.
     *
     * @param idToken Google ID 토큰
     * @return 토큰 payload (JsonNode)
     * @throws VerificationException 조회 실패 시
     */
    JsonNode fetchTokenInfo(String idToken) throws VerificationException;
}
