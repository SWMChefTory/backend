package com.cheftory.api.user.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.user.share.entity.UserShare;
import com.cheftory.api.user.share.exception.UserShareErrorCode;
import com.cheftory.api.user.share.exception.UserShareException;
import com.cheftory.api.user.share.repository.UserShareRepository;
import com.cheftory.api.user.share.repository.UserShareRepositoryImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({UserShareRepositoryImpl.class})
@DisplayName("UserShareRepository 테스트")
class UserShareRepositoryTest extends DbContextTest {

    private static final int DAILY_SHARE_LIMIT = 3;

    @Autowired
    private UserShareRepository userShareRepository;

    @Mock
    private Clock clock;

    private final LocalDate today = LocalDate.of(2024, 1, 1);
    private final LocalDateTime now = today.atStartOfDay();

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("shareTx 메서드는")
    class Describe_shareTx {

        @Test
        @DisplayName("일일 제한 횟수를 초과하면 예외를 던진다")
        void it_throws_exception_when_limit_exceeded() throws UserShareException {
            UUID userId = UUID.randomUUID();
            UserShare created = userShareRepository.create(userId, clock);

            userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
            userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
            userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);

            assertThatThrownBy(() -> userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT))
                    .isInstanceOf(UserShareException.class)
                    .extracting("error")
                    .isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("데이터가 존재하지 않으면 예외를 던진다")
        void it_throws_exception_when_not_exists() throws Exception {
            assertThatThrownBy(() -> userShareRepository.shareTx(UUID.randomUUID(), DAILY_SHARE_LIMIT))
                    .isInstanceOf(UserShareException.class)
                    .extracting("error")
                    .isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
        }

        @Test
        @DisplayName("정상적으로 공유 횟수를 증가시킨다")
        void it_increases_share_count_successfully() throws UserShareException {
            UUID userId = UUID.randomUUID();
            UserShare created = userShareRepository.create(userId, clock);

            UserShare result = userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);

            assertThat(result.getId()).isEqualTo(created.getId());
            assertThat(result.getCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("compensateTx 메서드는")
    class Describe_compensateTx {
        @Test
        @DisplayName("데이터가 존재하지 않으면 예외를 던진다")
        void it_throws_exception_when_not_exists() throws UserShareException {
            assertThatThrownBy(() -> userShareRepository.compensateTx(
                            UUID.randomUUID(), clock.now().toLocalDate()))
                    .isInstanceOf(UserShareException.class)
                    .extracting("error")
                    .isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
        }

        @Test
        @DisplayName("정상적으로 공유 횟수를 감소시킨다")
        void it_decreases_share_count_successfully() throws UserShareException {
            UUID userId = UUID.randomUUID();
            UserShare created = userShareRepository.create(userId, clock);
            userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);

            userShareRepository.compensateTx(created.getId(), created.getSharedAt());
            UserShare result = userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);

            assertThat(result.getId()).isEqualTo(created.getId());
            assertThat(result.getCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class Describe_create {

        @Test
        @DisplayName("새로운 UserShare를 생성한다")
        void it_creates_new_user_share() throws UserShareException {
            UUID userId = UUID.randomUUID();

            UserShare result = userShareRepository.create(userId, clock);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getCount()).isZero();
        }

        @Test
        @DisplayName("같은 날짜에 이미 존재하면 기존 데이터를 조회하여 반환한다")
        void it_returns_existing_user_share_when_already_exists() throws UserShareException {
            UUID userId = UUID.randomUUID();
            UserShare first = userShareRepository.create(userId, clock);
            userShareRepository.shareTx(first.getId(), DAILY_SHARE_LIMIT);

            UserShare second = userShareRepository.create(userId, clock);

            assertThat(second.getId()).isEqualTo(first.getId());
            assertThat(second.getCount()).isEqualTo(1);
        }
    }
}
