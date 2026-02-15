package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareCreditException;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
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
    @DisplayName("공유하기 (share)")
    class Share {

        @Nested
        @DisplayName("Given - 정상적인 공유 요청일 때")
        class GivenValidRequest {

            @Test
            @DisplayName("Then - 크레딧을 지급하고 공유 횟수를 반환한다")
            void thenGrantsCreditAndReturnsCount() throws UserShareException, CreditException {
                when(clock.now()).thenReturn(now);

                UserShare createdShare = UserShare.create(userId, today, clock);
                when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

                UserShare increasedShare = UserShare.create(userId, today, clock);
                increasedShare.increase(3);
                when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

                int result = userShareService.share(userId);

                assertThat(result).isEqualTo(1);
                verify(userShareCreditPort).grantUserShare(userId, 1);
                verify(userShareRepository, never()).compensateTx(any(), any());
            }
        }

        @Nested
        @DisplayName("Given - 크레딧 지급 실패 시")
        class GivenCreditGrantFail {

            @Test
            @DisplayName("Then - 보상 트랜잭션을 실행하고 예외를 던진다")
            void thenCompensatesAndThrowsException() throws UserShareException, CreditException {
                when(clock.now()).thenReturn(now);

                UserShare createdShare = UserShare.create(userId, today, clock);
                when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

                UserShare increasedShare = UserShare.create(userId, today, clock);
                increasedShare.increase(3);
                when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

                CreditException creditException =
                        new UserShareCreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
                doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);

                assertThrows(CreditException.class, () -> userShareService.share(userId));

                verify(userShareRepository).compensateTx(createdShare.getId(), today);
            }
        }

        @Nested
        @DisplayName("Given - 보상 트랜잭션 실패 시")
        class GivenCompensationFail {

            @Test
            @DisplayName("Then - 원래의 CreditException을 던진다")
            void thenThrowsOriginalException() throws UserShareException, CreditException {
                when(clock.now()).thenReturn(now);

                UserShare createdShare = UserShare.create(userId, today, clock);
                when(userShareRepository.create(userId, clock)).thenReturn(createdShare);

                UserShare increasedShare = UserShare.create(userId, today, clock);
                increasedShare.increase(3);
                when(userShareRepository.shareTx(createdShare.getId(), 3)).thenReturn(increasedShare);

                CreditException creditException =
                        new UserShareCreditException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
                doThrow(creditException).when(userShareCreditPort).grantUserShare(userId, 1);
                doThrow(new UserShareException(UserShareErrorCode.USER_SHARE_CREATE_FAIL))
                        .when(userShareRepository)
                        .compensateTx(createdShare.getId(), today);

                assertThrows(CreditException.class, () -> userShareService.share(userId));

                verify(userShareRepository).compensateTx(createdShare.getId(), today);
            }
        }
    }
}
