package com.cheftory.api.user;

import java.util.UUID;

/**
 * 유저 크레딧 포트 인터페이스
 *
 * <p>크레딧 도메인과의 통신을 담당하는 포트입니다.</p>
 */
public interface UserCreditPort {
    /**
     * 튜토리얼 완료 크레딧 지급
     *
     * @param userId 유저 ID
     */
    void grantUserTutorial(UUID userId);
}
