package com.cheftory.api.affiliate.coupang.dto;

import java.util.List;

/**
 * 쿠팡 파트너스 검색 API 응답 DTO.
 *
 * @param rCode 응답 코드
 * @param rMessage 응답 메시지
 * @param data 검색 결과 데이터
 */
public record CoupangSearchResponse(String rCode, String rMessage, Data data) {

    /**
     * 쿠팡 검색 결과 데이터.
     *
     * @param landingUrl 랜딩 URL
     * @param productData 제품 목록
     */
    public record Data(String landingUrl, List<Product> productData) {}

    /**
     * 쿠팡 제품 정보.
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
            String productUrl) {}
}
