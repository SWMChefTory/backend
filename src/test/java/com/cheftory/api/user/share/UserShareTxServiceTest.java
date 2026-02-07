package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
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
@DisplayName("UserShareTxService 테스트")
class UserShareTxServiceTest {

    @Mock
    private UserShareRepository userShareRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserShareTxService userShareTxService;

    private final UUID userId = UUID.randomUUID();
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
        @DisplayName("기존 데이터가 없으면 새로 생성하고 횟수를 증가시킨다")
        void it_creates_new_share_and_increases_count() {
            // given
            when(userShareRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.empty());
            UserShare newUserShare = UserShare.create(userId, today, clock);
            when(userShareRepository.save(any(UserShare.class))).thenReturn(newUserShare);

            // when
            UserShare result = userShareTxService.shareTx(userId);

            // then
            assertThat(result.getCount()).isEqualTo(1);
            verify(userShareRepository).save(any(UserShare.class));
            verify(userShareRepository).flush();
        }

        @Test
        @DisplayName("기존 데이터가 있으면 횟수만 증가시킨다")
        void it_increases_count_of_existing_share() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            when(userShareRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.of(existingShare));

            // when
            UserShare result = userShareTxService.shareTx(userId);

            // then
            assertThat(result.getCount()).isEqualTo(1);
            verify(userShareRepository, never()).save(any());
            verify(userShareRepository).flush();
        }

        @Test
        @DisplayName("일일 제한 횟수를 초과하면 예외를 던진다")
        void it_throws_exception_when_limit_exceeded() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            for (int i = 0; i < 3; i++) {
                existingShare.increase(3);
            }
            when(userShareRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.of(existingShare));

            // when & then
            UserShareException exception = assertThrows(UserShareException.class, () -> userShareTxService.shareTx(userId));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("동시 생성 시 DataIntegrityViolationException이 발생하면 기존 데이터를 조회하여 처리한다")
        void it_handles_race_condition_on_create() {
            // given
            UserShare existingShare = UserShare.create(userId, today, clock);
            when(userShareRepository.findByUserIdAndSharedAt(userId, today))
                    .thenReturn(Optional.empty(), Optional.of(existingShare));
            when(userShareRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

            // when
            UserShare result = userShareTxService.shareTx(userId);

            // then
            assertThat(result.getCount()).isEqualTo(1);
            verify(userShareRepository, times(2)).findByUserIdAndSharedAt(userId, today);
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
            existingShare.increase(3); // count = 1
            when(userShareRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.of(existingShare));

            // when
            userShareTxService.compensateTx(userId, today);

            // then
            assertThat(existingShare.getCount()).isZero();
            verify(userShareRepository).flush();
        }

        @Test
        @DisplayName("데이터가 없으면 아무 일도 하지 않는다")
        void it_does_nothing_when_not_exists() {
            // given
            when(userShareRepository.findByUserIdAndSharedAt(userId, today)).thenReturn(Optional.empty());

            // when
            userShareTxService.compensateTx(userId, today);

            // then
            verify(userShareRepository, never()).flush();
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
                    new ObjectOptimisticLockingFailureException(UserShare.class, userId);

            // when & then
            UserShareException userShareException =
                    assertThrows(UserShareException.class, () -> userShareTxService.recover(exception, userId));

            assertThat(userShareException.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_CREATE_FAIL);
        }
    }
}
