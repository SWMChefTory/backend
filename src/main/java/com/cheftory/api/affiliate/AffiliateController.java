package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.coupang.exception.CoupangException;
import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/affiliate")
public class AffiliateController {

    private final AffiliateService affiliateService;

    @GetMapping("/search/coupang")
    public AffiliateSearchResponse searchProducts(@NotBlank @RequestParam String keyword) throws CoupangException {
        return AffiliateSearchResponse.from(affiliateService.searchCoupangProducts(keyword));
    }
}
