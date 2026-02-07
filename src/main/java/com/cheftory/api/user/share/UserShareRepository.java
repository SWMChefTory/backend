package com.cheftory.api.user.share;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserShareRepository extends JpaRepository<UserShare, UUID> {
    Optional<UserShare> findByUserIdAndSharedAt(UUID userId, LocalDate sharedAt);
}
