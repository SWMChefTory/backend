package com.cheftory.api.user.repository;

import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.entity.UserStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 유저 JPA Repository 인터페이스
 *
 * <p>Spring Data JPA를 사용하여 유저 엔티티에 대한 데이터베이스 작업을 수행합니다.</p>
 */
public interface UserJpaRepository extends JpaRepository<User, UUID> {
    /**
     * Provider와 ProviderSub, UserStatus로 유저 존재 여부 확인
     *
     * @param provider 소셜 로그인 제공자
     * @param sub 제공자별 유저 고유 식별자
     * @param userStatus 유저 상태
     * @return 유저 존재 여부
     */
    boolean existsByProviderAndProviderSubAndUserStatus(Provider provider, String sub, UserStatus userStatus);

    /**
     * Provider와 ProviderSub, UserStatus로 유저 조회
     *
     * @param provider 소셜 로그인 제공자
     * @param sub 제공자별 유저 고유 식별자
     * @param userStatus 유저 상태
     * @return 조회된 유저 (Optional)
     */
    Optional<User> findByProviderAndProviderSubAndUserStatus(Provider provider, String sub, UserStatus userStatus);

    /**
     * 유저 ID와 UserStatus로 유저 조회
     *
     * @param userId 유저 ID
     * @param userStatus 유저 상태
     * @return 조회된 유저 (Optional)
     */
    Optional<User> findByIdAndUserStatus(UUID userId, UserStatus userStatus);

    /**
     * 튜토리얼 완료 처리 (update 쿼리)
     *
     * @param userId 유저 ID
     * @param now 현재 시간
     * @return 업데이트된 행 수 (0이면 이미 완료된 상태)
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
update User u
   set u.tutorialAt = :now,
       u.updatedAt = :now
 where u.id = :userId
   and u.tutorialAt is null
""")
    int completeTutorial(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * 튜토리얼 완료 취소 처리 (update 쿼리)
     *
     * @param userId 유저 ID
     * @param now 현재 시간
     * @return 업데이트된 행 수
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
update User u
   set u.tutorialAt = null,
       u.updatedAt = :now
 where u.id = :userId
   and u.tutorialAt is not null
""")
    int revertTutorial(UUID userId, LocalDateTime now);
}
