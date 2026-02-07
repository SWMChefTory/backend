package com.cheftory.api.user.share.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.cheftory.api.user.share.entity.UserShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserShareJpaRepository extends JpaRepository<UserShare, UUID> {
    Optional<UserShare> findByUserIdAndSharedAt(UUID userId, LocalDate sharedAt);
}
