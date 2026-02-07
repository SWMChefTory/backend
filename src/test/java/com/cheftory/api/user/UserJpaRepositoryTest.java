package com.cheftory.api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.user.entity.*;
import com.cheftory.api.user.repository.UserJpaRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class UserJpaRepositoryTest extends DbContextTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("User 저장 및 조회 테스트")
    void saveAndFindByProviderAndProviderSubAndUserStatus() {
        Clock clock = new Clock();
        User user = User.create("테스터", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "sub-1234", true, clock);

        userJpaRepository.save(user);

        Optional<User> result = userJpaRepository.findByProviderAndProviderSubAndUserStatus(
                Provider.GOOGLE, "sub-1234", UserStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("UserStatus가 다르면 조회되지 않아야 함")
    void shouldNotFindUserWithDifferentStatus() {
        Clock clock = new Clock();
        User user = User.create(
                "탈퇴유저", Gender.FEMALE, LocalDate.of(1995, 3, 15), Provider.GOOGLE, "sub-9999", false, clock);
        user.changeStatus(UserStatus.DELETED, clock);
        userJpaRepository.save(user);

        Optional<User> result = userJpaRepository.findByProviderAndProviderSubAndUserStatus(
                Provider.GOOGLE, "sub-9999", UserStatus.ACTIVE);

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("tutorialAt이 null이면 완료 처리로 업데이트된다")
    void completeTutorialIfNotCompleted_updatesTutorialAt() {
        Clock clock = new Clock();
        User user = User.create("튜토리얼", Gender.MALE, LocalDate.of(1990, 1, 1), Provider.GOOGLE, "sub-1", true, clock);
        userJpaRepository.save(user);

        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);
        int updated = userJpaRepository.completeTutorial(user.getId(), now);

        entityManager.clear();
        assertThat(updated).isEqualTo(1);
        Optional<User> result = userJpaRepository.findById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTutorialAt()).isEqualTo(now);
    }

    @Test
    @Transactional
    @DisplayName("tutorialAt이 이미 있으면 완료 처리되지 않는다")
    void completeTutorial() {
        Clock clock = new Clock();
        User user = User.create("튜토리얼", Gender.FEMALE, LocalDate.of(1992, 2, 2), Provider.GOOGLE, "sub-2", true, clock);
        user.changeTutorial(clock);
        userJpaRepository.save(user);

        LocalDateTime now = LocalDateTime.of(2024, 1, 2, 0, 0);
        int updated = userJpaRepository.completeTutorial(user.getId(), now);

        entityManager.clear();
        assertThat(updated).isZero();
        Optional<User> result = userJpaRepository.findById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTutorialAt()).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("tutorialAt이 있으면 revert 시 null로 변경된다")
    void revertTutorial_clears_tutorialAt() {
        Clock clock = new Clock();
        User user = User.create("튜토리얼", Gender.MALE, LocalDate.of(1991, 1, 1), Provider.GOOGLE, "sub-3", true, clock);
        user.changeTutorial(clock);
        userJpaRepository.save(user);

        LocalDateTime now = LocalDateTime.of(2024, 1, 3, 0, 0);
        int updated = userJpaRepository.revertTutorial(user.getId(), now);

        entityManager.clear();
        assertThat(updated).isEqualTo(1);
        Optional<User> result = userJpaRepository.findById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTutorialAt()).isNull();
    }

    @Test
    @Transactional
    @DisplayName("tutorialAt이 null이면 revert가 적용되지 않는다")
    void revertTutorial_doesNothing_whenNotCompleted() {
        Clock clock = new Clock();
        User user = User.create("튜토리얼", Gender.FEMALE, LocalDate.of(1993, 3, 3), Provider.GOOGLE, "sub-4", true, clock);
        userJpaRepository.save(user);

        LocalDateTime now = LocalDateTime.of(2024, 1, 4, 0, 0);
        int updated = userJpaRepository.revertTutorial(user.getId(), now);

        entityManager.clear();
        assertThat(updated).isZero();
        Optional<User> result = userJpaRepository.findById(user.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getTutorialAt()).isNull();
    }
}
