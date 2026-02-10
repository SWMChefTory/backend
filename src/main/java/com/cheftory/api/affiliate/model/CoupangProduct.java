package com.cheftory.api.affiliate.model;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠팡 파트너스 제품 정보를 담는 도메인 객체.
 *
 * <p>쿠팡 검색 API 응답을 변환하여 레시피와 연관된 제품 정보를 제공합니다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoupangProduct {
    String keyword;
    Integer rank;
    Boolean isRocket;
    Boolean isFreeShipping;
    Long productId;
    String productImage;
    String productName;
    Integer productPrice;
    String productUrl;

    public static CoupangProduct from(CoupangSearchResponse.Product dto) {
        return new CoupangProduct(
                dto.keyword(),
                dto.rank(),
                dto.isRocket(),
                dto.isFreeShipping(),
                dto.productId(),
                dto.productImage(),
                dto.productName(),
                dto.productPrice(),
                dto.productUrl());
    }
}
