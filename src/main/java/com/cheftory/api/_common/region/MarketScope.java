package com.cheftory.api._common.region;

import com.cheftory.api.exception.CheftoryException;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MarketScope {

    @TenantId
    @Column(nullable = false, updatable = false, length = 20)
    private String market;

    @Column(nullable = false, updatable = false, length = 2)
    private String countryCode;

    @PrePersist
    protected void onCreate() throws CheftoryException {
        this.countryCode = MarketContext.required().countryCode();
    }
}
