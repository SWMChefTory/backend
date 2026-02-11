package com.cheftory.api.auth.verifier;

import com.cheftory.api.auth.verifier.client.GoogleTokenClient;
import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Google OAuth 토큰 검증기
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifier {

    private final GoogleTokenClient googleTokenClient;

    /**
     * Google ID 토큰에서 sub 클레임 추출
     *
     * @param idToken Google ID 토큰
     * @return 유저 고유 식별자 (sub)
     * @throws VerificationException 토큰 검증 실패 시
     */
    public String getSubFromToken(String idToken) throws VerificationException {
        JsonNode payload = googleTokenClient.fetchTokenInfo(idToken);
        JsonNode subNode = payload.get("sub");

        if (subNode == null || subNode.isNull()) {
            throw new VerificationException(VerificationErrorCode.GOOGLE_MISSING_SUB);
        }

        return subNode.asText();
    }
}
