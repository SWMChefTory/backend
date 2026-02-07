package com.cheftory.api.credit;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.credit.dto.CreditBalanceResponse;
import com.cheftory.api.credit.entity.Credit;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/credit")
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/balance")
    public CreditBalanceResponse getBalance(@UserPrincipal UUID userId) {
        long balance = creditService.getBalance(userId);
        return CreditBalanceResponse.from(balance);
    }
}
