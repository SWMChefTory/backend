package com.cheftory.api.affiliate.dto;

import java.util.List;

public record AffiliateSearchResponse(List<Product> productData) {
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
