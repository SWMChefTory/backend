package com.cheftory.api.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.entity.*;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("UserService")
class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        clock = mock(Clock.class);
        doReturn(LocalDateTime.now()).when(clock).now();
        userService = new UserService(userRepository, clock);
    }

    @Nested
    @DisplayName("유저 조회 (get)")
    class GetUser {

        @Nested
        @DisplayName("Given - 유저가 존재할 때")
        class GivenUserExists {

            @Test
            @DisplayName("Then - 유저 정보를 반환해야 한다")
            void thenShouldReturnUser() {
                User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                when(userRepository.findByIdAndUserStatus(user.getId(), UserStatus.ACTIVE))
                        .thenReturn(Optional.of(user));

                User result = userService.get(user.getId());

                Assertions.assertEquals(user, result);
            }
        }

        @Nested
        @DisplayName("Given - 유저가 존재하지 않을 때")
        class GivenUserNotFound {

            @Test
            @DisplayName("Then - 예외를 던져야 한다")
            void thenShouldThrowNotFound() {

                UUID userId = UUID.randomUUID();
                when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
                        .thenReturn(Optional.empty());

                UserException ex = assertThrows(UserException.class, () -> userService.get(userId));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
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
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = user.getId();
                doReturn(Optional.of(user)).when(userRepository).findByIdAndUserStatus(id, UserStatus.ACTIVE);

                userService.update(id, newNickname, oldGender, oldBirth);

                Assertions.assertEquals(newNickname, user.getNickname());
                Assertions.assertEquals(oldGender, user.getGender());
                Assertions.assertEquals(oldBirth, user.getDateOfBirth());
            }

            @Test
            @DisplayName("성별 수정")
            void updateGenderOnly() {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = user.getId();
                doReturn(Optional.of(user)).when(userRepository).findByIdAndUserStatus(id, UserStatus.ACTIVE);

                userService.update(id, oldNickname, newGender, oldBirth);

                Assertions.assertEquals(oldNickname, user.getNickname());
                Assertions.assertEquals(newGender, user.getGender());
                Assertions.assertEquals(oldBirth, user.getDateOfBirth());
            }

            @Test
            @DisplayName("성별 수정 (NULL)")
            void clearGenderToNull() {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = user.getId();
                doReturn(Optional.of(user)).when(userRepository).findByIdAndUserStatus(id, UserStatus.ACTIVE);

                userService.update(id, oldNickname, null, oldBirth);

                Assertions.assertEquals(oldNickname, user.getNickname());
                Assertions.assertNull(user.getGender());
                Assertions.assertEquals(oldBirth, user.getDateOfBirth());
            }

            @Test
            @DisplayName("생년월일 수정")
            void updateBirthOnly() {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = user.getId();
                doReturn(Optional.of(user)).when(userRepository).findByIdAndUserStatus(id, UserStatus.ACTIVE);

                userService.update(id, oldNickname, oldGender, newBirth);

                Assertions.assertEquals(oldNickname, user.getNickname());
                Assertions.assertEquals(oldGender, user.getGender());
                Assertions.assertEquals(newBirth, user.getDateOfBirth());
            }

            @Test
            @DisplayName("생년월일 수정 (NULL)")
            void clearBirthToNull() {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = user.getId();
                doReturn(Optional.of(user)).when(userRepository).findByIdAndUserStatus(id, UserStatus.ACTIVE);

                userService.update(id, oldNickname, oldGender, null);

                Assertions.assertEquals(oldNickname, user.getNickname());
                Assertions.assertNull(user.getDateOfBirth());
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
                User user = User.create("nick", Gender.FEMALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
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
                when(userRepository.findByIdAndUserStatus(userId, UserStatus.ACTIVE))
                        .thenReturn(Optional.empty());

                UserException ex = assertThrows(UserException.class, () -> userService.deleteUser(userId));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
            }
        }
    }
}
