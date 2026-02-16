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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoupangClient {

    private static final String SEARCH_PATH = "/v2/providers/affiliate_open_api/apis/openapi/products/search";

    private final CoupangHttpApi coupangHttpApi;
    private final CoupangPartnersProperties properties;

    public CoupangProducts searchProducts(String keyword) throws CoupangException {
        try {
            URI uri = UriComponentsBuilder.fromPath(SEARCH_PATH)
                    .queryParam("keyword", keyword)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            String rawPath = uri.getRawPath();
            String rawQuery = uri.getRawQuery();
            if (rawPath == null || rawQuery == null) {
                throw new CoupangException(CoupangErrorCode.COUPANG_API_REQUEST_FAIL);
            }

            String pathAndQueryForSign = rawPath + "?" + rawQuery;

            String authorization = HmacGenerator.generate(
                    "GET", pathAndQueryForSign, properties.getSecretKey(), properties.getAccessKey());

            CoupangSearchResponse response =
                    coupangHttpApi.searchProducts(keyword, authorization, MediaType.APPLICATION_JSON_VALUE);

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
