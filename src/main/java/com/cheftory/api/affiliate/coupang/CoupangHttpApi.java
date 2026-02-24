package com.cheftory.api.affiliate.coupang;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface CoupangHttpApi {

    @GetExchange("/v2/providers/affiliate_open_api/apis/openapi/products/search")
    CoupangSearchResponse searchProducts(
            @RequestParam("keyword") String keyword,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(HttpHeaders.ACCEPT) String accept);
}
