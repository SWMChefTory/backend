package com.cheftory.api.user;

import com.cheftory.api._common.Clock;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.entity.UserStatus;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserCreditPort userCreditPort;
    private final Clock clock;

    public User getByProviderAndProviderSub(Provider provider, String providerSub) {
        return userRepository
                .findByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public User create(
            String nickname,
            Gender gender,
            LocalDate dateOfBirth,
            Provider provider,
            String providerSub,
            boolean isTermsOfUseAgreed,
            boolean isPrivacyPolicyAgreed,
            boolean isMarketingAgreed) {
        Optional<User> existUser =
                userRepository.findByProviderAndProviderSubAndUserStatus(provider, providerSub, UserStatus.ACTIVE);
        if (existUser.isPresent()) {
            throw new UserException(UserErrorCode.USER_ALREADY_EXIST);
        }

        if (!isTermsOfUseAgreed) {
            throw new UserException(UserErrorCode.TERMS_OF_USE_NOT_AGREED);
        }

        if (!isPrivacyPolicyAgreed) {
            throw new UserException(UserErrorCode.PRIVACY_POLICY_NOT_AGREED);
        }

        User user = User.create(nickname, gender, dateOfBirth, provider, providerSub, isMarketingAgreed, clock);
        return userRepository.save(user);
    }

    public User get(UUID id) {
        return userRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public User update(UUID id, String nickname, Gender gender, LocalDate dateOfBirth) {
        User user = userRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.change(nickname, gender, dateOfBirth, clock);
        userRepository.save(user);

        return userRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public void tutorial(UUID userId){

        if (!userRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        int updated = userRepository.completeTutorialIfNotCompleted(userId, clock.now());
        if (updated == 0) {
            throw new UserException(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
        }

        try {
            userCreditPort.grantUserTutorial(userId);
        } catch (CheftoryException e) {
            log.error("튜토리얼 크레딧 지급 실패. 보상 실행: userId={}", userId, e);
            userRepository.revertTutorial(userId, clock.now());
            throw e;
        }
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository
                .findByIdAndUserStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.changeStatus(UserStatus.DELETED, clock);
        userRepository.save(user);
    }

    public boolean exists(UUID id) {
        return userRepository.existsById(id);
    }
}
