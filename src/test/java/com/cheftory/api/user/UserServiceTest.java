package com.cheftory.api.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.entity.*;
import com.cheftory.api.user.exception.UserCreditException;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("UserService 테스트")
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
    @DisplayName("유저 조회 (get)")
    class Get {

        @Nested
        @DisplayName("Given - Provider와 Sub로 조회할 때")
        class GivenProviderAndSub {

            @Test
            @DisplayName("Then - 유저 정보를 반환한다")
            void thenReturnsUser() throws UserException {
                User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                when(userRepository.find(Provider.KAKAO, "sub")).thenReturn(user);

                User result = userService.get(Provider.KAKAO, "sub");

                Assertions.assertEquals(user, result);
            }
        }

        @Nested
        @DisplayName("Given - ID로 조회할 때")
        class GivenId {

            @Test
            @DisplayName("Then - 유저 정보를 반환한다")
            void thenReturnsUser() throws UserException {
                User user = User.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                UUID userId = UUID.randomUUID();
                when(userRepository.find(userId)).thenReturn(user);

                User result = userService.get(userId);

                Assertions.assertEquals(user, result);
            }
        }
    }

    @Nested
    @DisplayName("유저 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 이용약관 미동의 시")
        class GivenTermsNotAgreed {

            @Test
            @DisplayName("Then - TERMS_OF_USE_NOT_AGREED 예외를 던진다")
            void thenThrowsException() {
                UserException ex = assertThrows(
                        UserException.class,
                        () -> userService.create(
                                "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", false, true, true));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.TERMS_OF_USE_NOT_AGREED);
            }
        }

        @Nested
        @DisplayName("Given - 개인정보 처리방침 미동의 시")
        class GivenPrivacyNotAgreed {

            @Test
            @DisplayName("Then - PRIVACY_POLICY_NOT_AGREED 예외를 던진다")
            void thenThrowsException() {
                UserException ex = assertThrows(
                        UserException.class,
                        () -> userService.create(
                                "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, false, true));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.PRIVACY_POLICY_NOT_AGREED);
            }
        }

        @Nested
        @DisplayName("Given - 이미 존재하는 유저일 때")
        class GivenUserAlreadyExists {

            @Test
            @DisplayName("Then - USER_ALREADY_EXIST 예외를 던진다")
            void thenThrowsException() throws UserException {
                when(userRepository.exist(Provider.KAKAO, "sub")).thenReturn(true);

                UserException ex = assertThrows(
                        UserException.class,
                        () -> userService.create(
                                "nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, true, true));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_ALREADY_EXIST);
            }
        }

        @Nested
        @DisplayName("Given - 유효한 정보일 때")
        class GivenValidInfo {

            @Test
            @DisplayName("Then - 유저를 생성한다")
            void thenCreatesUser() throws UserException {
                when(userRepository.exist(Provider.KAKAO, "sub")).thenReturn(false);

                userService.create("nick", Gender.MALE, LocalDate.now(), Provider.KAKAO, "sub", true, true, true);

                verify(userRepository).create(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("유저 존재 여부 확인 (exists)")
    class Exists {

        @Nested
        @DisplayName("Given - 유저가 존재할 때")
        class GivenUserExists {

            @Test
            @DisplayName("Then - true를 반환한다")
            void thenReturnsTrue() {
                UUID userId = UUID.randomUUID();
                when(userRepository.exist(userId)).thenReturn(true);

                boolean result = userService.exists(userId);

                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("Given - 유저가 존재하지 않을 때")
        class GivenUserNotExists {

            @Test
            @DisplayName("Then - false를 반환한다")
            void thenReturnsFalse() {
                UUID userId = UUID.randomUUID();
                when(userRepository.exist(userId)).thenReturn(false);

                boolean result = userService.exists(userId);

                assertThat(result).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("유저 정보 수정 (update)")
    class Update {

        String oldNickname = "oldNick";
        Gender oldGender = Gender.FEMALE;
        LocalDate oldBirth = LocalDate.of(1999, 1, 1);

        String newNickname = "newNick";
        Gender newGender = Gender.MALE;
        LocalDate newBirth = LocalDate.of(2000, 1, 1);

        @Nested
        @DisplayName("Given - 닉네임 수정 시")
        class GivenNicknameUpdate {

            @Test
            @DisplayName("Then - 닉네임을 업데이트한다")
            void thenUpdatesNickname() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(newNickname), eq(oldGender), eq(oldBirth), any(Clock.class));

                userService.update(id, newNickname, oldGender, oldBirth);

                verify(userRepository).update(id, newNickname, oldGender, oldBirth, clock);
            }
        }

        @Nested
        @DisplayName("Given - 성별 수정 시")
        class GivenGenderUpdate {

            @Test
            @DisplayName("Then - 성별을 업데이트한다")
            void thenUpdatesGender() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), eq(newGender), eq(oldBirth), any(Clock.class));

                userService.update(id, oldNickname, newGender, oldBirth);

                verify(userRepository).update(id, oldNickname, newGender, oldBirth, clock);
            }
        }

        @Nested
        @DisplayName("Given - 성별 NULL 수정 시")
        class GivenGenderNullUpdate {

            @Test
            @DisplayName("Then - 성별을 NULL로 업데이트한다")
            void thenUpdatesGenderToNull() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), isNull(), eq(oldBirth), any(Clock.class));

                userService.update(id, oldNickname, null, oldBirth);

                verify(userRepository).update(id, oldNickname, null, oldBirth, clock);
            }
        }

        @Nested
        @DisplayName("Given - 생년월일 수정 시")
        class GivenBirthUpdate {

            @Test
            @DisplayName("Then - 생년월일을 업데이트한다")
            void thenUpdatesBirth() throws UserException {
                User user = User.create(oldNickname, oldGender, oldBirth, Provider.KAKAO, "sub", true, clock);
                UUID id = UUID.randomUUID();
                doReturn(user).when(userRepository).find(id);
                doReturn(user)
                        .when(userRepository)
                        .update(eq(id), eq(oldNickname), eq(oldGender), eq(newBirth), any(Clock.class));

                userService.update(id, oldNickname, oldGender, newBirth);

                verify(userRepository).update(id, oldNickname, oldGender, newBirth, clock);
            }
        }

        @Nested
        @DisplayName("Given - 생년월일 NULL 수정 시")
        class GivenBirthNullUpdate {

            @Test
            @DisplayName("Then - 생년월일을 NULL로 업데이트한다")
            void thenUpdatesBirthToNull() throws UserException {
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
    @DisplayName("유저 삭제 (delete)")
    class Delete {

        UUID userId = UUID.randomUUID();

        @Nested
        @DisplayName("Given - 유저가 존재할 때")
        class GivenUserExists {

            @Test
            @DisplayName("Then - 유저를 삭제 처리한다")
            void thenDeletesUser() throws UserException {
                User user = User.create("nick", Gender.FEMALE, LocalDate.now(), Provider.KAKAO, "sub", true, clock);
                when(userRepository.find(userId)).thenReturn(user);

                userService.delete(userId);

                verify(userRepository).delete(userId, clock);
            }
        }
    }

    @Nested
    @DisplayName("튜토리얼 완료 (tutorial)")
    class Tutorial {

        UUID userId = UUID.randomUUID();

        @Nested
        @DisplayName("Given - 유저가 존재하지 않을 때")
        class GivenUserNotExists {

            @Test
            @DisplayName("Then - USER_NOT_FOUND 예외를 던진다")
            void thenThrowsException() throws CreditException, UserException {
                when(userRepository.exist(userId)).thenReturn(false);

                UserException ex = assertThrows(UserException.class, () -> userService.tutorial(userId));

                assertThat(ex.getError()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
                verify(userRepository, never()).completeTutorial(any(), any());
                verify(userCreditPort, never()).grantUserTutorial(any());
            }
        }

        @Nested
        @DisplayName("Given - 이미 완료된 경우")
        class GivenAlreadyCompleted {

            @Test
            @DisplayName("Then - TUTORIAL_ALREADY_FINISHED 예외를 던진다")
            void thenThrowsException() throws UserException, CreditException {
                when(userRepository.exist(userId)).thenReturn(true);
                UserException exception = new UserException(UserErrorCode.TUTORIAL_ALREADY_FINISHED);
                doThrow(exception).when(userRepository).completeTutorial(eq(userId), any(Clock.class));

                UserException thrown = assertThrows(UserException.class, () -> userService.tutorial(userId));

                assertThat(thrown).isEqualTo(exception);
                verify(userCreditPort, never()).grantUserTutorial(any());
            }
        }

        @Nested
        @DisplayName("Given - 정상적인 경우")
        class GivenValidCase {

            @Test
            @DisplayName("Then - 완료 처리하고 크레딧을 지급한다")
            void thenCompletesAndGrantsCredit() throws CreditException, UserException {
                when(userRepository.exist(userId)).thenReturn(true);

                userService.tutorial(userId);

                verify(userRepository).completeTutorial(userId, clock);
                verify(userCreditPort).grantUserTutorial(userId);
            }
        }

        @Nested
        @DisplayName("Given - 크레딧 지급 실패 시")
        class GivenCreditGrantFail {

            @Test
            @DisplayName("Then - 보상 처리 후 예외를 던진다")
            void thenCompensatesAndThrowsException() throws CreditException, UserException {
                LocalDateTime now = LocalDateTime.of(2024, 1, 1, 0, 0);
                when(clock.now()).thenReturn(now);
                when(userRepository.exist(userId)).thenReturn(true);
                CreditException exception = new UserCreditException(null);
                doThrow(exception).when(userCreditPort).grantUserTutorial(userId);

                CreditException thrown = assertThrows(CreditException.class, () -> userService.tutorial(userId));

                assertThat(thrown).isEqualTo(exception);
                verify(userRepository).completeTutorial(eq(userId), any(Clock.class));
                verify(userRepository).decompleteTutorial(eq(userId), any(Clock.class));
            }
        }
    }
}
