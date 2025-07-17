package com.cheftory.api.account.auth.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class AuthToken {
    private final String accessToken;
    private final String refreshToken;
}