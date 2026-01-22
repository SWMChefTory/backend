package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.model.CoupangProducts;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AffiliateService {

    private final CoupangClient client;

    @Cacheable(value = "coupangSearchCache", key = "#keyword")
    public CoupangProducts searchCoupangProducts(String keyword) {
        return client.searchProducts(keyword);
    }
}
