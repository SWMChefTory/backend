package com.cheftory.api.affiliate.coupang;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.coupang.exception.CoupangErrorCode;
import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;
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

/**
 * 쿠팡 파트너스 API와 통신하는 클라이언트.
 *
 * <p>HTTP 요청 생성, HMAC 서명 생성, API 호출을 담당합니다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangClient {

    @Qualifier("coupangClient")
    private final WebClient webClient;

    private final CoupangPartnersProperties properties;

    /**
     * 쿠팡 파트너스 API를 통해 제품을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 쿠팡 제품 목록
     * @throws CoupangException API 요청 실패 시
     */
    public CoupangProducts searchProducts(String keyword) throws CoupangException {
        try {
            URI uri = UriComponentsBuilder.fromPath("/v2/providers/affiliate_open_api/apis/openapi/products/search")
                    .queryParam("keyword", keyword)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            if (uri.getRawQuery() == null || uri.getRawPath() == null) {
                throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
            }

            String pathAndQueryForSign = uri.getRawPath() + "?" + uri.getRawQuery();

            String authorization = HmacGenerator.generate(
                    "GET", pathAndQueryForSign, properties.getSecretKey(), properties.getAccessKey());

            String pathAndQuery = uri.getRawPath() + "?" + uri.getRawQuery();

            CoupangSearchResponse response = webClient
                    .get()
                    .uri(pathAndQuery)
                    .header("Authorization", authorization)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(CoupangSearchResponse.class)
                    .block();

            if (response == null) {
                throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
            }

            return CoupangProducts.of(response.data().productData().stream()
                    .map(CoupangProduct::from)
                    .toList());

        } catch (CoupangException e) {
            throw e;
        } catch (WebClientException e) {
            log.error("쿠팡 파트너스 API 요청 중 WebClient 오류 발생", e);
            throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL, e);
        } catch (Exception e) {
            log.error("쿠팡 파트너스 API 요청 중 알 수 없는 오류 발생", e);
            throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL, e);
        }
    }
}
