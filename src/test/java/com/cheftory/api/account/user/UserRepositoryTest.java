package com.cheftory.api.account.user;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.account.user.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends DbContextTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("User 저장 및 조회 테스트")
  void saveAndFindByProviderAndProviderSubAndUserStatus() {
    // given
    User user = User.create(
        "테스터",
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        Provider.GOOGLE,
        "sub-1234"
    );

    userRepository.save(user);

    // when
    Optional<User> result = userRepository.findByProviderAndProviderSubAndUserStatus(
        Provider.GOOGLE, "sub-1234", UserStatus.ACTIVE
    );

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getNickname()).isEqualTo("테스터");
  }

  @Test
  @DisplayName("UserStatus가 다르면 조회되지 않아야 함")
  void shouldNotFindUserWithDifferentStatus() {
    // given
    User user = User.create(
        "탈퇴유저",
        Gender.FEMALE,
        LocalDate.of(1995, 3, 15),
        Provider.GOOGLE,
        "sub-9999"
    );
    user.changeStatus(UserStatus.DELETED);
    userRepository.save(user);

    // when
    Optional<User> result = userRepository.findByProviderAndProviderSubAndUserStatus(
        Provider.GOOGLE, "sub-9999", UserStatus.ACTIVE
    );

    // then
    assertThat(result).isEmpty();
  }
}