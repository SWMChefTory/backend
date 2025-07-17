package com.cheftory.api.user.repository;

import com.cheftory.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);

    boolean existsUserByEmail(String email);
}