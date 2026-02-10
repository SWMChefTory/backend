package com.cheftory.api.credit;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.credit.dto.CreditBalanceResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 크레딧 관련 HTTP 요청을 처리하는 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/credit")
public class CreditController {

    private final CreditService service;

    /**
     * 사용자의 크레딧 잔액을 조회합니다.
     *
     * @param userId 인증된 사용자 ID
     * @return 크레딧 잔액 응답
     */
    @GetMapping("/balance")
    public CreditBalanceResponse getBalance(@UserPrincipal UUID userId) {
        long balance = service.getBalance(userId);
        return CreditBalanceResponse.from(balance);
    }
}
