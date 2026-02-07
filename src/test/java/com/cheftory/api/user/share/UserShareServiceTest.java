package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api.credit.exception.CreditException;
import java.time.LocalDate;
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
    private UserShareTxService userShareTxService;

    @Mock
    private UserShareCreditPort userShareCreditPort;

    @InjectMocks
    private UserShareService userShareService;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("share 메서드는")
    class Describe_share {

        @Test
        @DisplayName("성공적으로 공유하면 크레딧을 지급하고 횟수를 반환한다")
        void it_grants_credit_and_returns_count() {
            // given
            UserShare userShare = mock(UserShare.class);
            when(userShare.getCount()).thenReturn(1);
            when(userShareTxService.shareTx(userId)).thenReturn(userShare);

            // when
            int result = userShareService.share(userId);

            // then
            assertThat(result).isEqualTo(1);
            verify(userShareCreditPort).grantUserShare(userId, 1);
            verify(userShareTxService, never()).compensateTx(any(), any());
        }

        @Test
        @DisplayName("크레딧 지급에 실패하면 보상 트랜잭션을 실행하고 예외를 던진다")
        void it_compensates_when_credit_grant_fails() {
            // given
            UserShare userShare = mock(UserShare.class);
            LocalDate sharedAt = LocalDate.now();
            when(userShare.getCount()).thenReturn(1);
            when(userShare.getSharedAt()).thenReturn(sharedAt);
            when(userShareTxService.shareTx(userId)).thenReturn(userShare);
            
            CreditException creditException = mock(CreditException.class);
            doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);

            // when & then
            assertThrows(CreditException.class, () -> userShareService.share(userId));

            verify(userShareTxService).compensateTx(userId, sharedAt);
        }

        @Test
        @DisplayName("보상 트랜잭션까지 실패하더라도 원래의 CreditException을 던진다")
        void it_throws_original_exception_even_if_compensation_fails() {
            // given
            UserShare userShare = mock(UserShare.class);
            LocalDate sharedAt = LocalDate.now();
            when(userShare.getCount()).thenReturn(1);
            when(userShare.getSharedAt()).thenReturn(sharedAt);
            when(userShareTxService.shareTx(userId)).thenReturn(userShare);

            CreditException creditException = mock(CreditException.class);
            doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);
            doThrow(new CreditException(null)).when(userShareTxService).compensateTx(userId, sharedAt);

            // when & then
            assertThrows(CreditException.class, () -> userShareService.share(userId));

            verify(userShareTxService).compensateTx(userId, sharedAt);
        }
    }
}
