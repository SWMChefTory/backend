package com.cheftory.api.affiliate.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 쿠팡 파트너스 제품 목록을 담는 도메인 객체.
 *
 * <p>여러 쿠팡 제품을 포함하는 컨테이너 역할을 합니다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoupangProducts {
    private List<CoupangProduct> coupangProducts;

    /**
     * 쿠팡 제품 목록을 생성합니다.
     *
     * @param coupangProducts 쿠팡 제품 목록
     * @return 쿠팡 제품 목록 객체
     */
    public static CoupangProducts of(List<CoupangProduct> coupangProducts) {
        return new CoupangProducts(coupangProducts);
    }
}
