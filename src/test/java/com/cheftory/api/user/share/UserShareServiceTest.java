package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.port.UserShareCreditPort;
import com.cheftory.api.user.share.repository.UserShareRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserShareService 테스트")
class UserShareServiceTest {

    @Mock
    private UserShareRepository userShareRepository;

    @Mock
    private UserShareCreditPort userShareCreditPort;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserShareService userShareService;

    private final UUID userId = UUID.randomUUID();
    private final LocalDate today = LocalDate.of(2024, 1, 1);
    private final LocalDateTime now = today.atStartOfDay();

    @Nested
    @DisplayName("share 메서드는")
    class Describe_share {

        @Test
        @DisplayName("성공적으로 공유하면 크레딧을 지급하고 횟수를 반환한다")
        void it_grants_credit_and_returns_count() {
            // given
            when(clock.now()).thenReturn(now);

            UserShare createdShare = UserShare.create(userId, today, clock);
            when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

            UserShare increasedShare = UserShare.create(userId, today, clock);
            increasedShare.increase(3);
            when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

            // when
            int result = userShareService.share(userId);

            // then
            assertThat(result).isEqualTo(1);
            verify(userShareCreditPort).grantUserShare(userId, 1);
            verify(userShareRepository, never()).compensateTx(any(), any());
        }

        @Test
        @DisplayName("크레딧 지급에 실패하면 보상 트랜잭션을 실행하고 예외를 던진다")
        void it_compensates_when_credit_grant_fails() {
            // given
            when(clock.now()).thenReturn(now);

            UserShare createdShare = UserShare.create(userId, today, clock);
            when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

            UserShare increasedShare = UserShare.create(userId, today, clock);
            increasedShare.increase(3);
            when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

            CreditException creditException = mock(CreditException.class);
            doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);

            // when & then
            assertThrows(CreditException.class, () -> userShareService.share(userId));

            verify(userShareRepository).compensateTx(createdShare.getId(), today);
        }

        @Test
        @DisplayName("보상 트랜잭션까지 실패하더라도 원래의 CreditException을 던진다")
        void it_throws_original_exception_even_if_compensation_fails() {
            // given
            when(clock.now()).thenReturn(now);

            UserShare createdShare = UserShare.create(userId, today, clock);
            when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

            UserShare increasedShare = UserShare.create(userId, today, clock);
            increasedShare.increase(3);
            when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

            CreditException creditException = mock(CreditException.class);
            doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);
            doThrow(new CreditException(null)).when(userShareRepository).compensateTx(createdShare.getId(), today);

            // when & then
            assertThrows(CreditException.class, () -> userShareService.share(userId));

            verify(userShareRepository).compensateTx(createdShare.getId(), today);
        }
    }
}
