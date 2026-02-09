package com.cheftory.api.user;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 유저 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserCreditPort userCreditPort;
    private final Clock clock;

    /**
     * Provider와 ProviderSub로 유저 조회
     *
     * @param provider 외부 인증 제공자 (KAKAO, APPLE 등)
     * @param providerSub 제공자별 유저 고유 식별자
     * @return 조회된 유저 정보
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     */
    public User get(Provider provider, String providerSub) throws UserException {
        return userRepository.find(provider, providerSub);
    }

    /**
     * 신규 유저 생성
     *
     * @param nickname 유저 닉네임 (최대 20자)
     * @param gender 성별
     * @param dateOfBirth 생년월일
     * @param provider 외부 인증 제공자
     * @param providerSub 제공자별 유저 고유 식별자 (최대 127자)
     * @param isTermsOfUseAgreed 이용약관 동의 여부
     * @param isPrivacyPolicyAgreed 개인정보 처리방침 동의 여부
     * @param isMarketingAgreed 마케팅 정보 수신 동의 여부
     * @return 생성된 유저 정보
     * @throws UserException 이용약관 미동의 시 TERMS_OF_USE_NOT_AGREED
     * @throws UserException 개인정보 처리방침 미동의 시 PRIVACY_POLICY_NOT_AGREED
     * @throws UserException 이미 존재하는 유저일 때 USER_ALREADY_EXIST
     */
    public User create(
            String nickname,
            Gender gender,
            LocalDate dateOfBirth,
            Provider provider,
            String providerSub,
            boolean isTermsOfUseAgreed,
            boolean isPrivacyPolicyAgreed,
            boolean isMarketingAgreed)
            throws UserException {

        if (!isTermsOfUseAgreed) {
            throw new UserException(UserErrorCode.TERMS_OF_USE_NOT_AGREED);
        }

        if (!isPrivacyPolicyAgreed) {
            throw new UserException(UserErrorCode.PRIVACY_POLICY_NOT_AGREED);
        }

        boolean exist = userRepository.exist(provider, providerSub);

        if (exist) {
            throw new UserException(UserErrorCode.USER_ALREADY_EXIST);
        }

        User user = User.create(nickname, gender, dateOfBirth, provider, providerSub, isMarketingAgreed, clock);
        return userRepository.create(user);
    }

    /**
     * 유저 ID로 유저 조회
     *
     * @param userId 유저 ID
     * @return 조회된 유저 정보
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     */
    public User get(UUID userId) throws UserException {
        return userRepository.find(userId);
    }

    /**
     * 유저 정보 수정
     *
     * @param userId 유저 ID
     * @param nickname 수정할 닉네임
     * @param gender 수정할 성별 (null 가능)
     * @param dateOfBirth 수정할 생년월일 (null 가능)
     * @return 수정된 유저 정보
     */
    public User update(UUID userId, String nickname, Gender gender, LocalDate dateOfBirth) throws UserException {
        return userRepository.update(userId, nickname, gender, dateOfBirth, clock);
    }

    /**
     * 튜토리얼 완료 처리 및 크레딧 지급
     *
     * @param userId 유저 ID
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     * @throws UserException 이미 튜토리얼을 완료했을 때 TUTORIAL_ALREADY_FINISHED
     * @throws CheftoryException 크레딧 지급 실패 시 튜토리얼 상태 복구 후 예외 전파
     */
    public void tutorial(UUID userId) throws UserException, CreditException {

        boolean exist = userRepository.exist(userId);

        if (!exist) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        userRepository.completeTutorial(userId, clock);

        try {
            userCreditPort.grantUserTutorial(userId);
        } catch (CreditException e) {
            log.error("튜토리얼 크레딧 지급 실패. 보상 실행: userId={}", userId, e);
            userRepository.decompleteTutorial(userId, clock);
            throw e;
        }
    }

    /**
     * 유저 삭제 (DELETED 상태로 변경)
     *
     * @param userId 유저 ID
     * @throws UserException 유저를 찾을 수 없을 때 USER_NOT_FOUND
     */
    public void delete(UUID userId) throws UserException {
        userRepository.delete(userId, clock);
    }

    /**
     * 유저 존재 여부 확인
     *
     * @param userId 유저 ID
     * @return 유저 존재 여부
     */
    public boolean exists(UUID userId) {
        return userRepository.exist(userId);
    }
}
