package com.cheftory.api.account.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api.account.user.entity.*;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.account.user.exception.UserException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.openapitools.jackson.nullable.JsonNullable;

@DisplayName("UserService")
class UserServiceTest {

  private UserRepository userRepository;
  private UserService userService;

  @BeforeEach
  void setUp() {
    userRepository = mock(UserRepository.class);
    userService = new UserService(userRepository);
  }

  @Nested
  @DisplayName("유저 생성 (create)")
  class CreateUser {

    private final String nickname = "cheftory";
    private final Gender gender = Gender.MALE;
    private final LocalDate birth = LocalDate.of(1999, 9, 9);
    private final Provider provider = Provider.KAKAO;
    private final String providerSub = "123456";

    @Nested
    @DisplayName("Given - 기존 유저가 없을 때")
    class GivenNoExistingUser {

      @Test
      @DisplayName("Then - 새 유저를 저장하고 ID를 반환해야 한다")
      void thenShouldSaveUserAndReturnId() {
        when(userRepository.findByProviderAndProviderSubAndUserStatus(provider, providerSub,
            UserStatus.ACTIVE))
            .thenReturn(Optional.empty());

        UUID generatedId = UUID.randomUUID();
        User user = User.create(nickname, gender, birth, provider, providerSub, true);
        user = user.toBuilder().id(generatedId).build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        User user1 = userService.create(nickname, gender, birth, provider, providerSub, true, true, false);

        verify(userRepository).save(any(User.class));
        Assertions.assertEquals(generatedId, user1.getId());
      }
    }

    @Nested
    @DisplayName("Given - 이미 존재하는 유저일 때")
    class GivenUserAlreadyExists {

      @Test
      @DisplayName("Then - 예외를 던져야 한다")
      void thenShouldThrowUserAlreadyExist() {
        // Given
        User existing = User.create(nickname, gender, birth, provider, providerSub, true);
        when(userRepository.findByProviderAndProviderSubAndUserStatus(provider, providerSub,
            UserStatus.ACTIVE))
            .thenReturn(Optional.of(existing));

        // When
        UserException ex = assertThrows(UserException.class, () ->
            userService.create(nickname, gender, birth, provider, providerSub, true, true, false)
        );

        // Then
        assertThat(ex.getErrorMessage()).isEqualTo(UserErrorCode.USER_ALREADY_EXIST);
      }
    }
  }

  @Nested
  @DisplayName("유저 조회 (get)")
  class GetUser {

    UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("Given - 유저가 존재할 때")
    class GivenUserExists {

      @Test
      @DisplayName("Then - 유저 정보를 반환해야 한다")
      void thenShouldReturnUser() {
        User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true);
        when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
            .thenReturn(Optional.of(user));

        User result = userService.get(userId);

        Assertions.assertEquals(user, result);
      }
    }

    @Nested
    @DisplayName("Given - 유저가 존재하지 않을 때")
    class GivenUserNotFound {

      @Test
      @DisplayName("Then - 예외를 던져야 한다")
      void thenShouldThrowNotFound() {
        // Given
        when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
            .thenReturn(Optional.empty());

        // When
        UserException ex = assertThrows(UserException.class, () ->
            userService.get(userId)
        );

        // Then
        assertThat(ex.getErrorMessage()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
      }
    }
  }

  @Nested
  @DisplayName("유저 정보 수정 (update)")
  class UpdateUser {

    String oldNickname = "oldNick";
    Gender oldGender = Gender.FEMALE;
    LocalDate oldBirth = LocalDate.of(1999, 1, 1);

    String newNickname = "newNick";
    Gender newGender = Gender.MALE;
    LocalDate newBirth = LocalDate.of(2000, 1, 1);

    @Nested
    @DisplayName("Given - 유저가 존재할 때")
    class GivenUserExists {

      @Test
      @DisplayName("닉네임 수정")
      void updateNicknameOnly() {
        // given
        User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true);
        UUID id = user.getId();
        doReturn(Optional.of(user))
            .when(userRepository)
            .findByIdAndUserStatus(id, UserStatus.ACTIVE);

        // when: 닉네임만 전달 (다른 필드는 미제공)
        userService.update(
            id,
            Optional.of(newNickname),
            JsonNullable.undefined(),
            JsonNullable.undefined()
        );

        // then
        Assertions.assertEquals(newNickname, user.getNickname());
        Assertions.assertEquals(oldGender, user.getGender());
        Assertions.assertEquals(oldBirth, user.getDateOfBirth());
      }

      @Test
      @DisplayName("성별 수정")
      void updateGenderOnly() {
        // given
        User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true);
        UUID id = user.getId();
        doReturn(Optional.of(user))
            .when(userRepository)
            .findByIdAndUserStatus(id, UserStatus.ACTIVE);

        // when: 성별만 전달
        userService.update(
            id,
            Optional.empty(),
            JsonNullable.of(newGender),
            JsonNullable.undefined()
        );

        // then
        Assertions.assertEquals(oldNickname, user.getNickname());
        Assertions.assertEquals(newGender, user.getGender());
        Assertions.assertEquals(oldBirth, user.getDateOfBirth());
      }

      // 성별을 null 로 비우는 케이스도 검증하고 싶다면 추가
      @Test
      @DisplayName("성별 수정 (NULL)")
      void clearGenderToNull() {
        User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true);
        UUID id = user.getId();
        doReturn(Optional.of(user))
            .when(userRepository)
            .findByIdAndUserStatus(id, UserStatus.ACTIVE);

        userService.update(
            id,
            Optional.empty(),
            JsonNullable.of(null),
            JsonNullable.undefined()
        );

        Assertions.assertNull(user.getGender());
        Assertions.assertEquals(oldNickname, user.getNickname());
        Assertions.assertEquals(oldBirth, user.getDateOfBirth());
      }

      @Test
      @DisplayName("생년월일 수정")
      void updateBirthOnly() {
        // given
        User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true);
        UUID id = user.getId();
        doReturn(Optional.of(user))
            .when(userRepository)
            .findByIdAndUserStatus(id, UserStatus.ACTIVE);

        // when: 생년월일만 전달
        userService.update(
            id,
            Optional.empty(),
            JsonNullable.undefined(),
            JsonNullable.of(newBirth)
        );

        // then
        Assertions.assertEquals(oldNickname, user.getNickname());
        Assertions.assertEquals(oldGender, user.getGender());
        Assertions.assertEquals(newBirth, user.getDateOfBirth());
      }

      @Test
      @DisplayName("생년월일 수정 (NULL)")
      void clearBirthToNull() {
        User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true);
        UUID id = user.getId();
        doReturn(Optional.of(user))
            .when(userRepository)
            .findByIdAndUserStatus(id, UserStatus.ACTIVE);

        userService.update(
            id,
            Optional.empty(),
            JsonNullable.undefined(),
            JsonNullable.of(null)
        );

        Assertions.assertNull(user.getDateOfBirth());
        Assertions.assertEquals(oldNickname, user.getNickname());
        Assertions.assertEquals(oldGender, user.getGender());
      }
    }
  }

  @Nested
  @DisplayName("유저 삭제 (deleteUser)")
  class DeleteUser {

    UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("Given - 유저가 존재할 때")
    class GivenUserExists {

      @Test
      @DisplayName("Then - 유저 상태를 DELETED로 변경해야 한다")
      void thenShouldMarkUserAsDeleted() {
        User user = User.create("nick", Gender.FEMALE, LocalDate.now(), Provider.KAKAO, "sub", true);
        when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
            .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        Assertions.assertEquals(UserStatus.DELETED, user.getUserStatus());
        verify(userRepository).save(user);
      }
    }

    @Nested
    @DisplayName("Given - 유저가 존재하지 않을 때")
    class GivenUserNotFound {

      @Test
      @DisplayName("Then - 예외를 던져야 한다")
      void thenShouldThrowNotFound() {
        // Given
        when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
            .thenReturn(Optional.empty());

        // When
        UserException ex = assertThrows(UserException.class, () ->
            userService.deleteUser(userId)
        );

        // Then
        assertThat(ex.getErrorMessage()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
      }
    }
  }
}