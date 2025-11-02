package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/affiliate")
public class AffiliateController {

  private final AffiliateService affiliateService;

  @GetMapping("/coupang/search")
  public AffiliateSearchResponse searchProducts(@RequestParam String keyword) {
    return affiliateService.searchCoupangProducts(keyword);
  }
}
