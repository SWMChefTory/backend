package com.cheftory.api.account.dto;

import lombok.Getter;

@Getter
public class LogoutRequest {
    private String refreshToken;
}
