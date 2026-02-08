package com.cheftory.api.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(UserRepositoryImpl.class)
class UserRepositoryTest extends DbContextTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 ID로 유저 조회")
    void find_byUserId() {
        Clock clock = new Clock();
        User user = User.create("테스터", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "sub-1234", true, clock);

        userRepository.create(user);

        User result = userRepository.find(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 조회하면 예외 발생")
    void find_byNotFoundUserId_throwException() {
        assertThatThrownBy(() -> userRepository.find(java.util.UUID.randomUUID()))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("Provider와 ProviderSub로 유저 조회")
    void find_byProviderAndProviderSub() {
        Clock clock = new Clock();
        User user = User.create(
                "테스터", Gender.FEMALE, LocalDate.of(1995, 3, 15), Provider.APPLE, "apple-sub-999", false, clock);

        userRepository.create(user);

        User result = userRepository.find(Provider.APPLE, "apple-sub-999");

        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("Provider와 ProviderSub로 존재 여부 확인 - 존재하는 경우")
    void exist_byProviderAndProviderSub_returnsTrue() {
        Clock clock = new Clock();
        User user =
                User.create("테스터", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.KAKAO, "kakao-sub-111", true, clock);

        userRepository.create(user);

        boolean result = userRepository.exist(Provider.KAKAO, "kakao-sub-111");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Provider와 ProviderSub로 존재 여부 확인 - 존재하지 않는 경우")
    void exist_byProviderAndProviderSub_returnsFalse() {
        boolean result = userRepository.exist(Provider.GOOGLE, "non-existent-sub");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유저 생성")
    void create_user() {
        Clock clock = new Clock();
        User user =
                User.create("새유저", Gender.FEMALE, LocalDate.of(2000, 5, 20), Provider.GOOGLE, "new-sub", true, clock);

        User result = userRepository.create(user);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getNickname()).isEqualTo("새유저");
    }

    @Test
    @DisplayName("유저 정보 수정")
    void update_user() {
        Clock clock = new Clock();
        User user =
                User.create("수정전", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "update-sub", true, clock);
        userRepository.create(user);

        User updated = userRepository.update(user.getId(), "수정후", Gender.FEMALE, LocalDate.of(1995, 5, 5), clock);

        assertThat(updated.getNickname()).isEqualTo("수정후");
        assertThat(updated.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(updated.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    @DisplayName("존재하지 않는 유저 정보 수정 시도 시 예외 발생")
    void update_byNotFoundUserId_throwException() {
        Clock clock = new Clock();

        assertThatThrownBy(() ->
                        userRepository.update(UUID.randomUUID(), "닉네임", Gender.MALE, LocalDate.of(1990, 1, 1), clock))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 삭제 (DELETED 상태로 변경)")
    void delete_user() {
        Clock clock = new Clock();
        User user =
                User.create("삭제할유저", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "delete-sub", true, clock);
        userRepository.create(user);

        userRepository.delete(user.getId(), clock);

        // DELETED 상태로 변경되었으므로 ACTIVE 상태로 조회되지 않아야 함
        assertThatThrownBy(() -> userRepository.find(user.getId()))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 유저 삭제 시도 시 예외 발생")
    void delete_byNotFoundUserId_throwException() {
        Clock clock = new Clock();

        assertThatThrownBy(() -> userRepository.delete(UUID.randomUUID(), clock))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 ID로 존재 여부 확인 - 존재하는 경우")
    void exist_byUserId_returnsTrue() {
        Clock clock = new Clock();
        User user =
                User.create("테스터", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "exist-sub", true, clock);
        userRepository.create(user);

        boolean result = userRepository.exist(user.getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유저 ID로 존재 여부 확인 - 존재하지 않는 경우")
    void exist_byUserId_returnsFalse() {
        boolean result = userRepository.exist(java.util.UUID.randomUUID());

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("튜토리얼 완료 처리")
    void completeTutorial_user() {
        Clock clock = new Clock();
        User user = User.create(
                "튜토리얼", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "tutorial-sub", true, clock);
        userRepository.create(user);

        userRepository.completeTutorial(user.getId(), clock);

        // 조회하여 tutorialAt이 설정되었는지 확인 (UserRepository.find는 DELETED/ACTIVE 상태만 체크하므로 ACTIVE 상태 유지)
        User result = userRepository.find(user.getId());
        assertThat(result.getTutorialAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 튜토리얼을 완료한 유저의 완료 처리 시도 시 예외 발생")
    void completeTutorial_alreadyFinished_throwException() {
        Clock clock = new Clock();
        User user = User.create(
                "튜토리얼완료", Gender.FEMALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "tutorial-done-sub", true, clock);
        user.changeTutorial(clock);
        userRepository.create(user);

        assertThatThrownBy(() -> userRepository.completeTutorial(user.getId(), clock))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
    }

    @Test
    @DisplayName("존재하지 않는 유저 튜토리얼 완료 처리 시도 시 예외 발생")
    void completeTutorial_byNotFoundUserId_throwException() {
        Clock clock = new Clock();

        assertThatThrownBy(() -> userRepository.completeTutorial(UUID.randomUUID(), clock))
                .isInstanceOf(UserException.class)
                .extracting("error")
                .isEqualTo(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
    }

    @Test
    @DisplayName("튜토리얼 완료 취소 처리")
    void decompleteTutorial_user() {
        Clock clock = new Clock();
        User user = User.create(
                "튜토리얼취소", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "revert-sub", true, clock);
        user.changeTutorial(clock);
        userRepository.create(user);

        userRepository.decompleteTutorial(user.getId(), clock);

        User result = userRepository.find(user.getId());
        assertThat(result.getTutorialAt()).isNull();
    }
}
