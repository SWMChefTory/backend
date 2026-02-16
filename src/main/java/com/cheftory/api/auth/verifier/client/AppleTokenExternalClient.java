package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Apple JWKS 외부 클라이언트 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppleTokenExternalClient implements AppleTokenClient {

    private final AppleTokenHttpApi appleTokenHttpApi;

    @Override
    @Cacheable("apple-jwks")
    public String fetchJwks() throws VerificationException {
        try {
            String response = appleTokenHttpApi.fetchJwks();

            if (response == null || response.isBlank()) {
                log.error("[AppleTokenExternalClient] JWKS 응답이 비어있습니다");
                throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
            }

            return response;

        } catch (WebClientException e) {
            log.error("[AppleTokenExternalClient] JWKS 조회 중 WebClient 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND, e);
        } catch (Exception e) {
            log.error("[AppleTokenExternalClient] JWKS 조회 중 알 수 없는 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND, e);
        }
    }
}
