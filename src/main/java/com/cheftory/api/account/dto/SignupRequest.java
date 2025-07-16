package com.cheftory.api.account.dto;

import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import lombok.Getter;

@Getter
public class SignupRequest {
    private String token;
    private Provider provider;
    private String nickname;
    private Gender gender;
}