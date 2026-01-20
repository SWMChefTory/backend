package com.cheftory.api.affiliate.dto;

import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;

public record AffiliateSearchResponse(CoupangProducts coupangProducts) {
    public record Product(
            String keyword,
            Integer rank,
            Boolean isRocket,
            Boolean isFreeShipping,
            Long productId,
            String productImage,
            String productName,
            Integer productPrice,
            String productUrl) {
        public static Product from(CoupangProduct coupangProduct) {
            return new Product(
                    coupangProduct.getKeyword(),
                    coupangProduct.getRank(),
                    coupangProduct.getIsRocket(),
                    coupangProduct.getIsFreeShipping(),
                    coupangProduct.getProductId(),
                    coupangProduct.getProductImage(),
                    coupangProduct.getProductName(),
                    coupangProduct.getProductPrice(),
                    coupangProduct.getProductUrl());
        }
    }

    public static AffiliateSearchResponse from(CoupangProducts coupangProducts) {
        return new AffiliateSearchResponse(coupangProducts);
    }
}
