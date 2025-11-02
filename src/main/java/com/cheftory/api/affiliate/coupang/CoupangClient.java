package com.cheftory.api.affiliate.coupang;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangClient {

  @Qualifier("coupangClient")
  private final WebClient webClient;

  private final CoupangPartnersProperties properties;

  private static final String URL = "/v2/providers/affiliate_open_api/apis/openapi/products/search";

  public CoupangSearchResponse searchProducts(String keyword) {
    // 1) 값만 인코딩
    String encodedKeyword = UriUtils.encodeQueryParam(keyword, StandardCharsets.UTF_8);
    String uriForSign = URL + "?keyword=" + encodedKeyword; // 도메인 제외 (공식 문서와 동일)

    String authorization =
        HmacGenerator.generate(
            "GET", uriForSign, properties.getSecretKey(), properties.getAccessKey());

    try {
      // 2) WebClient는 queryParam에 생(raw) 값을 넘김
      //    → 위에서 설정한 VALUES_ONLY에 의해 "값만" 동일하게 인코딩됨
      return webClient
          .get()
          .uri(
              builder ->
                  builder
                      .path(URL)
                      .queryParam("keyword", keyword) // encode() 호출하지 않음
                      .build())
          .header("Authorization", authorization)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .bodyToMono(CoupangSearchResponse.class)
          .block();

    } catch (Exception e) {
      log.error("쿠팡 파트너스 API 요청 중 오류 발생: {}", e.getMessage());
      throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
    }
  }
}
