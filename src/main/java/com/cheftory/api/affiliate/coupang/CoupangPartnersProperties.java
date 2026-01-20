package com.cheftory.api.affiliate.coupang;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "coupang-partners")
public class CoupangPartnersProperties {

    private String accessKey;
    private String secretKey;
}
