package com.cheftory.api.account.auth.repository;

import com.cheftory.api.account.auth.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginRepository extends JpaRepository<Login, UUID> {

    Optional<Login> findByUserIdAndRefreshToken(UUID userId, String refreshToken);

    void deleteByUserIdAndRefreshToken(UUID userId, String refreshToken);
}