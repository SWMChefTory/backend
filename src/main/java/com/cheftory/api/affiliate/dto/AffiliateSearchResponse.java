package com.cheftory.api.affiliate.dto;

import com.cheftory.api.affiliate.model.CoupangProduct;
import com.cheftory.api.affiliate.model.CoupangProducts;

/**
 * 제휴사 검색 응답 DTO.
 *
 * @param coupangProducts 쿠팡 제품 목록
 */
public record AffiliateSearchResponse(CoupangProducts coupangProducts) {
    /**
     * 제휴사 제품 정보.
     *
     * @param keyword 검색 키워드
     * @param rank 검색 결과 순위
     * @param isRocket 로켓배송 여부
     * @param isFreeShipping 무료배송 여부
     * @param productId 제품 ID
     * @param productImage 제품 이미지 URL
     * @param productName 제품명
     * @param productPrice 제품 가격
     * @param productUrl 제품 상세 URL
     */
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

        /**
         * CoupangProduct 엔티티로부터 Product DTO 생성.
         *
         * @param coupangProduct 쿠팡 제품 엔티티
         * @return 제품 정보 DTO
         */
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

    /**
     * CoupangProducts 엔티티로부터 AffiliateSearchResponse DTO 생성.
     *
     * @param coupangProducts 쿠팡 제품 목록 엔티티
     * @return 제휴사 검색 응답 DTO
     */
    public static AffiliateSearchResponse from(CoupangProducts coupangProducts) {
        return new AffiliateSearchResponse(coupangProducts);
    }
}
