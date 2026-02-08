package com.cheftory.api.auth.repository;

import com.cheftory.api.auth.entity.Login;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 로그인 엔티티용 JPA 리포지토리
 */
public interface LoginJpaRepository extends JpaRepository<Login, UUID> {

    /**
     * 유저 ID와 리프레시 토큰으로 로그인 조회
     *
     * @param userId 유저 ID
     * @param refreshToken 리프레시 토큰
     * @return 조회된 로그인 엔티티 (Optional)
     */
    Optional<Login> findByUserIdAndRefreshToken(UUID userId, String refreshToken);
}
