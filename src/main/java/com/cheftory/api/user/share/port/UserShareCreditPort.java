package com.cheftory.api.user.share.port;

import com.cheftory.api.user.share.exception.UserShareCreditException;
import java.util.UUID;

/**
 * 유저 공유 관련 크레딧 지급 포트
 */
public interface UserShareCreditPort {

    /**
     * 유저 공유 시 크레딧 지급
     *
     * @param userId 크레딧을 지급할 유저 ID
     * @param count 지급할 크레딧 수량
     */
    void grantUserShare(UUID userId, int count) throws UserShareCreditException;
}
