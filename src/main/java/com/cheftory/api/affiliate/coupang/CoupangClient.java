package com.cheftory.api.affiliate.coupang;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangClient {

  @Qualifier("coupangClient")
  private final WebClient webClient;

  private final CoupangPartnersProperties properties;

  public CoupangSearchResponse searchProducts(String keyword) {
    try {
      // 1) path + query 생성 후 UTF-8 인코딩 → URI 획득
      URI uri = UriComponentsBuilder
          .fromPath("/v2/providers/affiliate_open_api/apis/openapi/products/search")
          .queryParam("keyword", keyword)
          .build()
          .encode(StandardCharsets.UTF_8)
          .toUri();

      // 2) 서명용 문자열: path + '?' + rawQuery
      String pathAndQueryForSign =
          uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");

      // 3) HMAC 서명 생성
      String authorization = HmacGenerator.generate(
          "GET",
          pathAndQueryForSign,
          properties.getSecretKey(),
          properties.getAccessKey()
      );

      log.debug("REQ URL: {}", uri.toASCIIString());
      log.debug("SIGN path+query={}", pathAndQueryForSign);

      // 4) WebClient 요청
      // ※ 중요: .uri(uri) → .uri(pathAndQuery) 로 변경 (baseUrl과 결합 보장)
      String pathAndQuery =
          uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");

      return webClient
          .get()
          .uri(pathAndQuery) // baseUrl과 안전하게 결합됨
          .header("Authorization", authorization)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(
              s -> s.is4xxClientError() || s.is5xxServerError(),
              resp -> resp.bodyToMono(String.class).flatMap(body -> {
                log.error(
                    "Coupang API error: status={}, headers={}, body={}",
                    resp.statusCode(),
                    resp.headers().asHttpHeaders(),
                    body);
                return Mono.error(new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL));
              })
          )
          .bodyToMono(CoupangSearchResponse.class)
          .block();

    } catch (CoupangException e) {
      throw e;
    } catch (WebClientException e) {
      log.error("쿠팡 파트너스 API 요청 중 WebClient 오류 발생", e);
      throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    } catch (Exception e) {
      log.error("쿠팡 파트너스 API 요청 중 알 수 없는 오류 발생", e);
      throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }
  }
}
