package com.cheftory.api.user.entity;

/**
 * 유저 상태 열거형
 */
public enum UserStatus {
    /**
     * 활성 상태
     */
    ACTIVE,
    /**
     * 삭제 상태 (논리적 삭제)
     */
    DELETED,
    /**
     * 차단 상태
     */
    BANNED
}
