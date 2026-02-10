package com.cheftory.api.user.share.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.region.MarketScope;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 유저 공유 엔티티
 *
 * <p>유저의 일별 공유 횟수를 저장하는 엔티티입니다. 낙관락을 지원하여 동시 공유 요청을 처리합니다.</p>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_user_share_date",
                    columnNames = {"user_id", "shared_at"})
        })
public class UserShare extends MarketScope {
    /**
     * 공유 기록 고유 ID
     */
    @Id
    private UUID id;

    /**
     * 유저 ID
     */
    @Column(nullable = false)
    private UUID userId;

    /**
     * 공유 일자
     */
    @Column(nullable = false)
    private LocalDate sharedAt;

    /**
     * 공유 횟수
     */
    @Column(nullable = false)
    private int count;

    /**
     * 낙관락 버전
     */
    @Version
    private long version;

    /**
     * 생성일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 신규 공유 기록 생성
     *
     * @param userId 유저 ID
     * @param sharedAt 공유 일자
     * @param clock 현재 시간 제공 객체
     * @return 생성된 공유 기록 객체
     */
    public static UserShare create(UUID userId, LocalDate sharedAt, Clock clock) {
        LocalDateTime now = clock.now();
        return new UserShare(UUID.randomUUID(), userId, sharedAt, 0, 0, now);
    }

    /**
     * 공유 횟수 증가
     *
     * @param max 일일 최대 공유 횟수
     * @throws UserShareException 일일 공유 횟수 초과 시
     */
    public void increase(int max) throws UserShareException {
        if (this.count >= max) {
            throw new UserShareException(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }
        this.count++;
    }

    /**
     * 공유 횟수 감소
     *
     * <p>크레딧 지급 실패 시 보상용으로 사용됩니다.</p>
     */
    public void decrease() {
        if (this.count > 0) {
            this.count--;
        }
    }
}
