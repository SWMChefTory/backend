package com.cheftory.api.account.auth.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private final HttpClient client = HttpClient.newHttpClient();

    public String getEmailFromToken(String idToken) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("https://oauth2.googleapis.com/tokeninfo").queryParam("id_token", idToken).build().toUri();
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // TODO 찾아보기
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());

                return json.get("email").asText();
            }
            log.error("[GoogleTokenVerifier] 응답 실패 - statusCode: {}, body: {}", response.statusCode(), response.body());

            // Serverside, Clientside Exception
        } catch (IOException | InterruptedException e) {
            log.error("[GoogleTokenVerifier] HTTP 요청 실패", e);
            // Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[GoogleTokenVerifier] 알 수 없는 예외 발생", e);
        }
        // TODO exception으로 처리
        return null;
    }
}
