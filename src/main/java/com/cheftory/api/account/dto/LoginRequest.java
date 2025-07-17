package com.cheftory.api.account.dto;

import com.cheftory.api.user.entity.Provider;
import lombok.Getter;

@Getter
public class LoginRequest {
    private String token;
    private Provider provider;
}
