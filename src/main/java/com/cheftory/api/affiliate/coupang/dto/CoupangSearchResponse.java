package com.cheftory.api.affiliate.coupang.dto;

import java.util.List;

public record CoupangSearchResponse(String rCode, String rMessage, Data data) {

  public record Data(String landingUrl, List<Product> productData) {}

  public record Product(
      String keyword,
      Integer rank,
      Boolean isRocket,
      Boolean isFreeShipping,
      Long productId,
      String productImage,
      String productName,
      Integer productPrice,
      String productUrl) {}
}
