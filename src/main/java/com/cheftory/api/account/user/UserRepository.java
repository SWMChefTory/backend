package com.cheftory.api.account.user;

import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.UserStatus;
import com.cheftory.api.account.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByProviderAndProviderSubAndUserStatus(Provider provider, String sub, UserStatus userStatus);
    Optional<User> findByIdAndUserStatus(UUID userId, UserStatus userStatus);
}