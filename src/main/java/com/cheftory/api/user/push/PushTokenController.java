package com.cheftory.api.user.push;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.user.push.dto.PushTokenDeleteRequest;
import com.cheftory.api.user.push.dto.PushTokenUpsertRequest;
import com.cheftory.api.user.push.exception.PushException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/pushToken")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @PostMapping
    public SuccessOnlyResponse upsert(@UserPrincipal UUID userId, @Valid @RequestBody PushTokenUpsertRequest request) {
        pushTokenService.upsert(userId, request.provider(), request.token(), request.platform());
        return SuccessOnlyResponse.create();
    }

    @DeleteMapping
    public SuccessOnlyResponse delete(@UserPrincipal UUID userId, @Valid @RequestBody PushTokenDeleteRequest request)
            throws PushException {
        pushTokenService.delete(userId, request.provider(), request.token());
        return SuccessOnlyResponse.create();
    }
}
