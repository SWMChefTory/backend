package com.cheftory.api.user.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.entity.UserStatus;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 유저 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public User find(UUID userId) throws UserException {
        return userJpaRepository
                .findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User find(Provider provider, String providerSub) throws UserException {
        return userJpaRepository
                .findByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public boolean exist(Provider provider, String providerSub) {
        return userJpaRepository.existsByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE);
    }

    @Override
    public User create(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User update(UUID id, String nickname, Gender gender, LocalDate dateOfBirth, Clock clock)
            throws UserException {
        User user = userJpaRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.change(nickname, gender, dateOfBirth, clock);
        return userJpaRepository.save(user);
    }

    @Override
    public void delete(UUID userId, Clock clock) throws UserException {
        User user = userJpaRepository
                .findByIdAndUserStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.changeStatus(UserStatus.DELETED, clock);
        userJpaRepository.save(user);
    }

    @Override
    public boolean exist(UUID userId) {
        return userJpaRepository.existsById(userId);
    }

    @Override
    public void completeTutorial(UUID userId, Clock clock) throws UserException {
        int updated = userJpaRepository.completeTutorial(userId, clock.now());
        if (updated == 0) {
            throw new UserException(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
        }
    }

    @Override
    public void decompleteTutorial(UUID userId, Clock clock) {
        userJpaRepository.revertTutorial(userId, clock.now());
    }
}
