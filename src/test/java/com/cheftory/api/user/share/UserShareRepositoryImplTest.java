package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import com.cheftory.api.user.share.repository.UserShareJpaRepository;
import com.cheftory.api.user.share.repository.UserShareRepositoryImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserShareRepositoryImpl 테스트")
class UserShareRepositoryImplTest {

    @Mock
    private UserShareJpaRepository userShareJpaRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserShareRepositoryImpl userShareRepositoryImpl;

    private final UUID userId = UUID.randomUUID();
    private final UUID userShareId = UUID.randomUUID();
    private final LocalDate today = LocalDate.of(2024, 1, 1);
    private final LocalDateTime now = today.atStartOfDay();

    @BeforeEach
    void setUp() {
        lenient().when(clock.now()).thenReturn(now);
    }

    @Nested
    @DisplayName("shareTx 메서드는")
    class Describe_shareTx {

        @Test
        @DisplayName("데이터가 존재하면 횟수를 증가시킨다")
        void it_increases_count_of_existing_share() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            when(userShareJpaRepository.findById(userShareId)).thenReturn(Optional.of(existingShare));

            // when
            UserShare result = userShareRepositoryImpl.shareTx(userShareId, 3);

            // then
            assertThat(result.getCount()).isEqualTo(1);
            verify(userShareJpaRepository).save(existingShare);
        }

        @Test
        @DisplayName("일일 제한 횟수를 초과하면 예외를 던진다")
        void it_throws_exception_when_limit_exceeded() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            for (int i = 0; i < 3; i++) {
                existingShare.increase(3);
            }
            when(userShareJpaRepository.findById(userShareId)).thenReturn(Optional.of(existingShare));

            // when & then
            UserShareException exception =
                    assertThrows(UserShareException.class, () -> userShareRepositoryImpl.shareTx(userShareId, 3));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("데이터가 존재하지 않으면 예외를 던진다")
        void it_throws_exception_when_not_exists() {
            // given
            when(userShareJpaRepository.findById(userShareId)).thenReturn(Optional.empty());

            // when & then
            UserShareException exception =
                    assertThrows(UserShareException.class, () -> userShareRepositoryImpl.shareTx(userShareId, 3));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("compensateTx 메서드는")
    class Describe_compensateTx {

        @Test
        @DisplayName("데이터가 존재하면 횟수를 감소시킨다")
        void it_decreases_count_when_exists() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            existingShare.increase(3);
            when(userShareJpaRepository.findById(userShareId)).thenReturn(Optional.of(existingShare));

            // when
            userShareRepositoryImpl.compensateTx(userShareId, today);

            // then
            assertThat(existingShare.getCount()).isZero();
            verify(userShareJpaRepository).save(existingShare);
        }

        @Test
        @DisplayName("데이터가 존재하지 않으면 예외를 던진다")
        void it_throws_exception_when_not_exists() {
            // given
            when(userShareJpaRepository.findById(userShareId)).thenReturn(Optional.empty());

            // when & then
            UserShareException exception = assertThrows(
                    UserShareException.class, () -> userShareRepositoryImpl.compensateTx(userShareId, today));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("새로운 UserShare를 생성한다")
        void it_creates_new_user_share() {
            // given
            UserShare newUserShare = UserShare.create(userId, today, clock);
            when(userShareJpaRepository.save(any(UserShare.class))).thenReturn(newUserShare);

            // when
            UserShare result = userShareRepositoryImpl.create(userId, clock);

            // then
            assertThat(result).isNotNull();
            verify(userShareJpaRepository).save(any(UserShare.class));
        }

        @Test
        @DisplayName("동시 생성 시 DataIntegrityViolationException이 발생하면 기존 데이터를 조회하여 반환한다")
        void it_handles_race_condition_on_create() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            existingShare.increase(3);
            when(userShareJpaRepository.save(any(UserShare.class))).thenThrow(DataIntegrityViolationException.class);
            when(userShareJpaRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.of(existingShare));

            // when
            UserShare result = userShareRepositoryImpl.create(userId, clock);

            // then
            assertThat(result).isEqualTo(existingShare);
            verify(userShareJpaRepository).findByUserIdAndSharedAt(userId, today);
        }

        @Test
        @DisplayName("동시 생성 후 기존 데이터도 없으면 예외를 던진다")
        void it_throws_exception_when_race_condition_and_not_exists() {
            // given
            when(userShareJpaRepository.save(any(UserShare.class))).thenThrow(DataIntegrityViolationException.class);
            when(userShareJpaRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.empty());

            // when & then
            UserShareException exception =
                    assertThrows(UserShareException.class, () -> userShareRepositoryImpl.create(userId, clock));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("recover 메서드는")
    class Describe_recover {

        @Test
        @DisplayName("shareTx 재시도 실패 시 USER_SHARE_CREATE_FAIL 예외를 던진다")
        void it_throws_user_share_create_fail() {
            // given
            ObjectOptimisticLockingFailureException exception =
                    new ObjectOptimisticLockingFailureException(UserShare.class, userShareId);

            // when & then
            UserShareException userShareException = assertThrows(
                    UserShareException.class, () -> userShareRepositoryImpl.recover(exception, userShareId, 3));

            assertThat(userShareException.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
        }

        @Test
        @DisplayName("compensateTx 재시도 실패 시 아무 작업도 하지 않는다")
        void it_does_nothing_on_compensate_failure() {
            // given
            ObjectOptimisticLockingFailureException exception =
                    new ObjectOptimisticLockingFailureException(UserShare.class, userShareId);

            // when & then
            UserShareException userShareException = assertThrows(
                    UserShareException.class, () -> userShareRepositoryImpl.recover(exception, userShareId, 3));

            assertThat(userShareException.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
        }
    }
}
