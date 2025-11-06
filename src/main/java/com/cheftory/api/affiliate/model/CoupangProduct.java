package com.cheftory.api.affiliate.model;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
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
    return CoupangProduct.builder()
        .keyword(dto.keyword())
        .rank(dto.rank())
        .isRocket(dto.isRocket())
        .isFreeShipping(dto.isFreeShipping())
        .productId(dto.productId())
        .productImage(dto.productImage())
        .productName(dto.productName())
        .productPrice(dto.productPrice())
        .productUrl(dto.productUrl())
        .build();
  }
}
