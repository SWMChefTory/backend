package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Google Token Info 외부 클라이언트 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenExternalClient implements GoogleTokenClient {

    private final GoogleTokenHttpApi googleTokenHttpApi;

    private final ObjectMapper mapper;

    @Override
    public JsonNode fetchTokenInfo(String idToken) throws VerificationException {
        try {
            String response = googleTokenHttpApi.fetchTokenInfo(idToken);

            if (response == null || response.isBlank()) {
                log.error("[GoogleTokenExternalClient] Token Info 응답이 비어있습니다");
                throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
            }

            return mapper.readTree(response);

        } catch (WebClientException e) {
            log.error("[GoogleTokenExternalClient] Token Info 조회 중 WebClient 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK, e);
        } catch (Exception e) {
            log.error("[GoogleTokenExternalClient] Token Info 조회 중 알 수 없는 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK, e);
        }
    }
}
