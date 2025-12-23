package com.cheftory.api.affiliate.model;

import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
