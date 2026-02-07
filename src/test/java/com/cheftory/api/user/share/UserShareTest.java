package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserShare 도메인 테스트")
class UserShareTest {

    private final UUID userId = UUID.randomUUID();
    private final LocalDate sharedAt = LocalDate.now();
    private final Clock clock = mock(Clock.class);

    @Nested
    @DisplayName("increase 메서드는")
    class Describe_increase {

        @Test
        @DisplayName("제한 횟수 미만이면 횟수를 1 증가시킨다")
        void it_increases_count() {
            // given
            when(clock.now()).thenReturn(LocalDateTime.now());
            UserShare userShare = UserShare.create(userId, sharedAt, clock);

            // when
            userShare.increase(3);

            // then
            assertThat(userShare.getCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("제한 횟수에 도달하면 예외를 던진다")
        void it_throws_exception_when_at_limit() {
            // given
            when(clock.now()).thenReturn(LocalDateTime.now());
            UserShare userShare = UserShare.create(userId, sharedAt, clock);
            userShare.increase(3);
            userShare.increase(3);
            userShare.increase(3);

            // when & then
            UserShareException exception = assertThrows(UserShareException.class, () -> userShare.increase(3));
            assertThat(exception.getError()).isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("decrease 메서드는")
    class Describe_decrease {

        @Test
        @DisplayName("횟수가 0보다 크면 1 감소시킨다")
        void it_decreases_count() {
            // given
            when(clock.now()).thenReturn(LocalDateTime.now());
            UserShare userShare = UserShare.create(userId, sharedAt, clock);
            userShare.increase(3);

            // when
            userShare.decrease();

            // then
            assertThat(userShare.getCount()).isZero();
        }

        @Test
        @DisplayName("횟수가 0이면 감소시키지 않는다")
        void it_does_not_decrease_below_zero() {
            // given
            when(clock.now()).thenReturn(LocalDateTime.now());
            UserShare userShare = UserShare.create(userId, sharedAt, clock);

            // when
            userShare.decrease();

            // then
            assertThat(userShare.getCount()).isZero();
        }
    }
}
