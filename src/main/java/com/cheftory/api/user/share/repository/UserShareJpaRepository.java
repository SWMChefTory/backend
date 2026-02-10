package com.cheftory.api.user.share.repository;

import com.cheftory.api.user.share.entity.UserShare;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 유저 공유 JPA Repository 인터페이스
 *
 * <p>Spring Data JPA를 사용하여 유저 공유 엔티티에 대한 데이터베이스 작업을 수행합니다.</p>
 */
public interface UserShareJpaRepository extends JpaRepository<UserShare, UUID> {
    /**
     * 유저 ID와 공유 일자로 공유 기록 조회
     *
     * @param userId 유저 ID
     * @param sharedAt 공유 일자
     * @return 조회된 공유 기록 (Optional)
     */
    Optional<UserShare> findByUserIdAndSharedAt(UUID userId, LocalDate sharedAt);
}
