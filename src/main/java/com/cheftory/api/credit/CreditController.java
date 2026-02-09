package com.cheftory.api.credit;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.credit.dto.CreditBalanceResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/credit")
public class CreditController {

    private final CreditService service;

    @GetMapping("/balance")
    public CreditBalanceResponse getBalance(@UserPrincipal UUID userId) {
        long balance = service.getBalance(userId);
        return CreditBalanceResponse.from(balance);
    }
}
