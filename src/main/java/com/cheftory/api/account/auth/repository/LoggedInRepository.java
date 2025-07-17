package com.cheftory.api.auth.repository;

import com.cheftory.api.auth.entity.LoggedIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoggedInRepository extends JpaRepository<LoggedIn, UUID> {

    Optional<LoggedIn> findByUserIdAndRefreshToken(UUID userId, String refreshToken);

    void deleteByUserIdAndRefreshToken(UUID userId, String refreshToken);
}