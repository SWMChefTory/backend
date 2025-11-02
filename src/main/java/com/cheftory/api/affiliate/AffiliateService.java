package com.cheftory.api.affiliate;

import com.cheftory.api.affiliate.coupang.CoupangClient;
import com.cheftory.api.affiliate.coupang.dto.CoupangSearchResponse;
import com.cheftory.api.affiliate.dto.AffiliateSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AffiliateService {

  private final CoupangClient client;

  public AffiliateSearchResponse searchCoupangProducts(String keyword) {
    CoupangSearchResponse response = client.searchProducts(keyword);

    return toSearchResponse(response);
  }

  private AffiliateSearchResponse toSearchResponse(CoupangSearchResponse res) {
    var data = res != null ? res.data() : null;

    List<AffiliateSearchResponse.Product> items =
        (data != null && data.productData() != null)
            ? data.productData().stream()
                .map(
                    p ->
                        new AffiliateSearchResponse.Product(
                            p.keyword(),
                            p.rank(),
                            Boolean.TRUE.equals(p.isRocket()),
                            Boolean.TRUE.equals(p.isFreeShipping()),
                            p.productId(),
                            p.productImage(),
                            p.productName(),
                            p.productPrice(),
                            p.productUrl()))
                .toList()
            : List.of();

    return new AffiliateSearchResponse(items);
  }
}
