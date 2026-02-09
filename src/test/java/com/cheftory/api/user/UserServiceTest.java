package com.cheftory.api.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.entity.*;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("UserService")
class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;
    private Clock clock;
    private UserCreditPort userCreditPort;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        clock = mock(Clock.class);
        userCreditPort = mock(UserCreditPort.class);
        doReturn(LocalDateTime.now()).when(clock).now();
        userService = new UserService(userRepository, userCreditPort, clock);
    }

    @Nested
    @DisplayName("provider+sub로 유저 조회 (getByProviderAndProviderSub)")
    class GetUserByProviderAndSub {

        @Nested
        @DisplayName("Given - 유저가 존재할 때")
        class GivenUserExists {

            @Test
            @DisplayName("Then - 유저 정보를 반환해야 한다")
            void thenShouldReturnUser() throws UserException {
                User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                when(userRepository.find(Provider.KAKAO, "sub")).thenReturn(user);

                User result = userService.get(Provider.KAKAO, "sub");

                Assertions.assertEquals(user, result);
            }
        }
    }

    @Nested
    @DisplayName("유저 생성 (create)")
    class CreateUser {

        @Test
        @DisplayName("이용약관에 동의하지 않으면 예외를 던진다")
        void throws_when_terms_not_agreed() {
            UserException ex = assertThrows(
                    UserException.class,
                    () -> userService.create(
                            "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", false, true, true));

            assertThat(ex.getError()).isEqualTo(UserErrorCode.TERMS_OF_USE_NOT_AGREED);
        }

        @Test
        @DisplayName("개인정보 처리방침에 동의하지 않으면 예외를 던진다")
        void throws_when_privacy_not_agreed() {
            UserException ex = assertThrows(
                    UserException.class,
                    () -> userService.create(
                            "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, false, true));

            assertThat(ex.getError()).isEqualTo(UserErrorCode.PRIVACY_POLICY_NOT_AGREED);
        }

        @Test
        @DisplayName("이미 존재하는 유저면 예외를 던진다")
        void throws_when_user_already_exists() throws UserException {
            when(userRepository.exist(Provider.KAKAO, "sub")).thenReturn(true);

            UserException ex = assertThrows(
                    UserException.class,
                    () -> userService.create(
                            "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, true, true));

            assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_ALREADY_EXIST);
        }

        @Test
        @DisplayName("정상적으로 유저를 생성한다")
        void creates_user_successfully() throws UserException {
            when(userRepository.exist(Provider.KAKAO, "sub")).thenReturn(false);

            User user =
                    userService.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, true, true);

            verify(userRepository).create(any(User.class));
        }
    }

    @Nested
    @DisplayName("유저 존재 여부 확인 (exists)")
    class UserExists {

        @Test
        @DisplayName("유저가 존재하면 true를 반환한다")
        void returns_true_when_exists() {
            UUID userId = UUID.randomUUID();
            when(userRepository.exist(userId)).thenReturn(true);

            boolean result = userService.exists(userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 false를 반환한다")
        void returns_false_when_not_exists() {
            UUID userId = UUID.randomUUID();
            when(userRepository.exist(userId)).thenReturn(false);

            boolean result = userService.exists(userId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("유저 조회 (get)")
    class GetUser {

        @Nested
        @DisplayName("Given - 유저가 존재할 때")
        class GivenUserExists {

            @Test
            @DisplayName("Then - 유저 정보를 반환해야 한다")
            void thenShouldReturnUser() throws UserException {
                User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                UUID userId = UUID.randomUUID();
                when(userRepository.find(userId)).thenReturn(user);

                User result = userService.get(userId);

                Assertions.assertEquals(user, result);
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
            void updateNicknameOnly() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(newNickname), eq(oldGender), eq(oldBirth), any(Clock.class));

                userService.update(id, newNickname, oldGender, oldBirth);

                verify(userRepository).update(id, newNickname, oldGender, oldBirth, clock);
            }

            @Test
            @DisplayName("성별 수정")
            void updateGenderOnly() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), eq(newGender), eq(oldBirth), any(Clock.class));

                userService.update(id, oldNickname, newGender, oldBirth);

                verify(userRepository).update(id, oldNickname, newGender, oldBirth, clock);
            }

            @Test
            @DisplayName("성별 수정 (NULL)")
            void clearGenderToNull() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), isNull(), eq(oldBirth), any(Clock.class));

                userService.update(id, oldNickname, null, oldBirth);

                verify(userRepository).update(id, oldNickname, null, oldBirth, clock);
            }

            @Test
            @DisplayName("생년월일 수정")
            void updateBirthOnly() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), eq(oldGender), eq(newBirth), any(Clock.class));

                userService.update(id, oldNickname, oldGender, newBirth);

                verify(userRepository).update(id, oldNickname, oldGender, newBirth, clock);
            }

            @Test
            @DisplayName("생년월일 수정 (NULL)")
            void clearBirthToNull() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), eq(oldGender), isNull(), any(Clock.class));

                userService.update(id, oldNickname, oldGender, null);

                verify(userRepository).update(id, oldNickname, oldGender, null, clock);
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
            void thenShouldMarkUserAsDeleted() throws UserException {
                User user = User.create("nick", Gender.FEMALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                when(userRepository.find(userId)).thenReturn(user);

                userService.delete(userId);

                verify(userRepository).delete(userId, clock);
            }
        }
    }

    @Nested
    @DisplayName("튜토리얼 완료 처리 (tutorial)")
    class Tutorial {

        UUID userId = UUID.randomUUID();

        @Test
        @DisplayName("유저가 없으면 USER_NOT_FOUND 예외를 던진다")
        void it_throws_when_user_not_found() throws CreditException, UserException {
            when(userRepository.exist(userId)).thenReturn(false);

            UserException ex = assertThrows(UserException.class, () -> userService.tutorial(userId));

            assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
            verify(userRepository, never()).completeTutorial(any(), any());
            verify(userCreditPort, never()).grantUserTutorial(any());
        }

        @Test
        @DisplayName("이미 완료된 경우 TUTORIAL_ALREADY_FINISHED 예외를 던진다")
        void it_throws_when_already_completed() throws UserException, CreditException {
            when(userRepository.exist(userId)).thenReturn(true);
            UserException exception = new UserException(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
            doThrow(exception).when(userRepository).completeTutorial(eq(userId), any(Clock.class));

            UserException thrown = assertThrows(UserException.class, () -> userService.tutorial(userId));

            assertThat(thrown).isEqualTo(exception);
            verify(userCreditPort, never()).grantUserTutorial(any());
        }

        @Test
        @DisplayName("완료 처리 후 크레딧을 지급한다")
        void it_grants_credit_after_completion() throws CreditException, UserException {
            when(userRepository.exist(userId)).thenReturn(true);

            userService.tutorial(userId);

            verify(userRepository).completeTutorial(userId, clock);
            verify(userCreditPort).grantUserTutorial(userId);
        }

        @Test
        @DisplayName("크레딧 지급 실패 시 보상 처리 후 예외를 던진다")
        void it_reverts_when_credit_grant_fails() throws CreditException, UserException {
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);
            when(clock.now()).thenReturn(now);
            when(userRepository.exist(userId)).thenReturn(true);
            CreditException exception = new CreditException(null);
            doThrow(exception).when(userCreditPort).grantUserTutorial(userId);

            CreditException thrown = assertThrows(CreditException.class, () -> userService.tutorial(userId));

            assertThat(thrown).isEqualTo(exception);
            verify(userRepository).completeTutorial(eq(userId), any(Clock.class));
            verify(userRepository).decompleteTutorial(eq(userId), any(Clock.class));
        }
    }
}
