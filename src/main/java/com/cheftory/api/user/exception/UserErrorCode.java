package com.cheftory.api.user.exception;

import lombok.Getter;

@Getter
public enum UserErrorCode {

    USER_NOT_FOUND("USER_1", "회원을 찾을 수 없습니다."),
    USER_ALREADY_EXIST("USER_2", "이미 존재하는 회원입니다.");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}