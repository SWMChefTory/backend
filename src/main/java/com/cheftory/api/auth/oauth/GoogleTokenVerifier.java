package com.cheftory.api.auth.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private final HttpClient client = HttpClient.newHttpClient(); // 생성 시점 1회만

    public String getEmailFromToken(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                    .header("Authorization", "Bearer " + token)
                    .GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(response.body());
                return json.get("email").asText();
            } else {
                log.error("[GoogleTokenVerifier] 응답 실패 - statusCode: {}, body: {}", response.statusCode(), response.body());
            }
        } catch (JsonProcessingException e) {
            log.error("[GoogleTokenVerifier] JSON 파싱 실패", e);
        } catch (IOException | InterruptedException e) {
            log.error("[GoogleTokenVerifier] HTTP 요청 실패", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[GoogleTokenVerifier] 알 수 없는 예외 발생", e);
        }

        return null;
    }
}
