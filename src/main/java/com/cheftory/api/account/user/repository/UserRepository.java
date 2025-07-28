package com.cheftory.api.account.user.repository;

import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByProviderAndProviderSub(Provider provider, String sub);

    boolean existsUserByProviderAndProviderSub(Provider provider, String providerSub);
}