package com.cheftory.api.account.model;

import com.cheftory.api.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 인증 계정 정보를 담는 도메인 객체.
 *
 * <p>액세스 토큰, 리프레시 토큰, 사용자 정보를 포함하며 로그인/회원가입 후 반환됩니다.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    private String accessToken;
    private String refreshToken;
    private User user;

    public static Account of(String accessToken, String refreshToken, User user) {
        return new Account(accessToken, refreshToken, user);
    }
}
