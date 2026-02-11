package com.cheftory.api.auth.verifier.client;

import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Google Token Info 외부 클라이언트 구현체
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenExternalClient implements GoogleTokenClient {

    private static final String DEFAULT_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    @Qualifier("googleClient")
    private final WebClient webClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${google.token-info-url:" + DEFAULT_TOKEN_INFO_URL + "}")
    private String tokenInfoUrl = DEFAULT_TOKEN_INFO_URL;

    @Override
    public JsonNode fetchTokenInfo(String idToken) throws VerificationException {
        try {
            String uri = UriComponentsBuilder.fromUriString(tokenInfoUrl)
                    .queryParam("id_token", idToken)
                    .build()
                    .toUriString();

            String response = webClient
                    .get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                log.error("[GoogleTokenExternalClient] Token Info 응답이 비어있습니다");
                throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
            }

            return mapper.readTree(response);

        } catch (WebClientException e) {
            log.error("[GoogleTokenExternalClient] Token Info 조회 중 WebClient 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
        } catch (Exception e) {
            log.error("[GoogleTokenExternalClient] Token Info 조회 중 알 수 없는 오류 발생", e);
            throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
        }
    }
}
