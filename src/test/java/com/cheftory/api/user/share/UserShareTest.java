package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserShare 엔티티")
class UserShareTest {

    private final UUID userId = UUID.randomUUID();
    private final LocalDate sharedAt = LocalDate.now();
    private final Clock clock = mock(Clock.class);

    @Nested
    @DisplayName("공유 횟수 증가 (increase)")
    class Increase {

        @Nested
        @DisplayName("Given - 제한 횟수 미만일 때")
        class GivenUnderLimit {

            @Test
            @DisplayName("Then - 횟수를 1 증가시킨다")
            void thenIncreasesCount() throws UserShareException {
                when(clock.now()).thenReturn(LocalDateTime.now());
                UserShare userShare = UserShare.create(userId, sharedAt, clock);

                userShare.increase(3);

                assertThat(userShare.getCount()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("Given - 제한 횟수에 도달했을 때")
        class GivenLimitReached {

            @Test
            @DisplayName("Then - LIMIT_EXCEEDED 예외를 던진다")
            void thenThrowsException() throws UserShareException {
                when(clock.now()).thenReturn(LocalDateTime.now());
                UserShare userShare = UserShare.create(userId, sharedAt, clock);
                userShare.increase(3);
                userShare.increase(3);
                userShare.increase(3);

                UserShareException exception = assertThrows(UserShareException.class, () -> userShare.increase(3));
                assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
            }
        }
    }

    @Nested
    @DisplayName("공유 횟수 감소 (decrease)")
    class Decrease {

        @Nested
        @DisplayName("Given - 횟수가 0보다 클 때")
        class GivenPositiveCount {

            @Test
            @DisplayName("Then - 횟수를 1 감소시킨다")
            void thenDecreasesCount() throws UserShareException {
                when(clock.now()).thenReturn(LocalDateTime.now());
                UserShare userShare = UserShare.create(userId, sharedAt, clock);
                userShare.increase(3);

                userShare.decrease();

                assertThat(userShare.getCount()).isZero();
            }
        }

        @Nested
        @DisplayName("Given - 횟수가 0일 때")
        class GivenZeroCount {

            @Test
            @DisplayName("Then - 감소시키지 않는다")
            void thenDoesNotDecrease() {
                when(clock.now()).thenReturn(LocalDateTime.now());
                UserShare userShare = UserShare.create(userId, sharedAt, clock);

                userShare.decrease();

                assertThat(userShare.getCount()).isZero();
            }
        }
    }
}
