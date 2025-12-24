package com.cheftory.api.auth.verifier;

import com.cheftory.api.auth.verifier.exception.VerificationErrorCode;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class GoogleTokenVerifier {

  private final HttpClient client = HttpClient.newHttpClient();
  private final ObjectMapper mapper = new ObjectMapper();

  public String getSubFromToken(String idToken) {
    JsonNode payload = getPayload(idToken);
    JsonNode subNode = payload.get("sub");

    if (subNode == null || subNode.isNull()) {
      throw new VerificationException(VerificationErrorCode.GOOGLE_MISSING_SUB);
    }

    return subNode.asText();
  }

  private JsonNode getPayload(String idToken) {
    try {
      URI uri =
          UriComponentsBuilder.fromUriString("https://oauth2.googleapis.com/tokeninfo")
              .queryParam("id_token", idToken)
              .build()
              .toUri();

      HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        log.error(
            "[GoogleTokenVerifier] 응답 실패 - statusCode: {}, body: {}", status, response.body());
        throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
      }

      return mapper.readTree(response.body());

    } catch (IOException | InterruptedException e) {
      log.error("[GoogleTokenVerifier] HTTP 요청 실패", e);
      throw new VerificationException(VerificationErrorCode.GOOGLE_RESPONSE_NOT_OK);
    } catch (Exception e) {
      log.error("[GoogleTokenVerifier] 토큰 검증 중 알 수 없는 예외", e);
      throw new VerificationException(VerificationErrorCode.UNKNOWN_ERROR);
    }
  }
}
