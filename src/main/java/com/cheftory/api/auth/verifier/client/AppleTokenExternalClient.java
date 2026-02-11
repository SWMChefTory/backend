package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Apple JWKS 외부 클라이언트 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppleTokenExternalClient implements AppleTokenClient {

    private static final String DEFAULT_JWKS_URL = "https://appleid.apple.com/auth/keys";

    @Qualifier("appleClient")
    private final WebClient webClient;

    @Value("${apple.public-keys-url:" + DEFAULT_JWKS_URL + "}")
    private String jwksUrl = DEFAULT_JWKS_URL;

    @Override
    @Cacheable("apple-jwks")
    public AppleJwksResponse fetchJwks() throws VerificationException {
        try {
            String response = webClient
                    .get()
                    .uri(jwksUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                log.error("[AppleTokenExternalClient] JWKS 응답이 비어있습니다");
                throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
            }

            return AppleJwksResponse.fromJson(response);

        } catch (WebClientException e) {
            log.error("[AppleTokenExternalClient] JWKS 조회 중 WebClient 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
        } catch (Exception e) {
            log.error("[AppleTokenExternalClient] JWKS 조회 중 알 수 없는 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND);
        }
    }
}
