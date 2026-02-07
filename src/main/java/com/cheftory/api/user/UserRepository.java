package com.cheftory.api.user;

import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByProviderAndProviderSubAndUserStatus(Provider provider, String sub, UserStatus userStatus);

    Optional<User> findByIdAndUserStatus(UUID userId, UserStatus userStatus);

    @Transactional
    @Modifying
    @Query(
            """
        update User u
           set u.tutorialAt = :now,
               u.updatedAt = :now
         where u.id = :userId
           and u.tutorialAt is null
    """)
    int completeTutorialIfNotCompleted(UUID userId, LocalDateTime now);

    @Transactional
    @Modifying
    @Query(
            """
        update User u
           set u.tutorialAt = null,
               u.updatedAt = :now
         where u.id = :userId
           and u.tutorialAt is not null
    """)
    int revertTutorial(UUID userId, LocalDateTime now);
}
