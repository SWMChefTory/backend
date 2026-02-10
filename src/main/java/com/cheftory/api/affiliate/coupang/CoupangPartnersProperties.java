package com.cheftory.api.affiliate.coupang;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 쿠팡 파트너스 API 연동을 위한 설정 프로퍼티.
 *
 * <p>application.yml 또는 환경 변수에서 쿠팡 파트너스 API 인증 정보를 로드합니다.</p>
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "coupang-partners")
public class CoupangPartnersProperties {

    private String accessKey;
    private String secretKey;
}
