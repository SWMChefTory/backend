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
import org.springframework.context.annotation.Import;

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
    @DisplayName("공유 횟수 증가 (shareTx)")
    class ShareTx {

        @Nested
        @DisplayName("Given - 제한 횟수 미만일 때")
        class GivenUnderLimit {
            UUID userId;
            UserShare created;

            @BeforeEach
            void setUp() throws UserShareException {
                userId = UUID.randomUUID();
                created = userShareRepository.create(userId, clock);
            }

            @Nested
            @DisplayName("When - 증가를 요청하면")
            class WhenIncreasing {
                UserShare result;

                @BeforeEach
                void setUp() throws UserShareException {
                    result = userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
                }

                @Test
                @DisplayName("Then - 횟수가 증가한다")
                void thenIncreasesCount() {
                    assertThat(result.getId()).isEqualTo(created.getId());
                    assertThat(result.getCount()).isEqualTo(1);
                }
            }
        }

        @Nested
        @DisplayName("Given - 제한 횟수에 도달했을 때")
        class GivenLimitReached {
            UUID userId;
            UserShare created;

            @BeforeEach
            void setUp() throws UserShareException {
                userId = UUID.randomUUID();
                created = userShareRepository.create(userId, clock);
                userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
                userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
                userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
            }

            @Nested
            @DisplayName("When - 증가를 요청하면")
            class WhenIncreasing {

                @Test
                @DisplayName("Then - LIMIT_EXCEEDED 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT))
                            .isInstanceOf(UserShareException.class)
                            .extracting("error")
                            .isEqualTo(UserShareErrorCode.USER_SHARE_LIMIT_EXCEEDED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 데이터일 때")
        class GivenNonExisting {
            UUID id;

            @BeforeEach
            void setUp() {
                id = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 증가를 요청하면")
            class WhenIncreasing {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> userShareRepository.shareTx(id, DAILY_SHARE_LIMIT))
                            .isInstanceOf(UserShareException.class)
                            .extracting("error")
                            .isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("공유 횟수 보상 (compensateTx)")
    class CompensateTx {

        @Nested
        @DisplayName("Given - 공유 횟수가 증가된 상태일 때")
        class GivenIncreased {
            UUID userId;
            UserShare created;

            @BeforeEach
            void setUp() throws UserShareException {
                userId = UUID.randomUUID();
                created = userShareRepository.create(userId, clock);
                userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
            }

            @Nested
            @DisplayName("When - 보상을 요청하면")
            class WhenCompensating {

                @BeforeEach
                void setUp() throws UserShareException {
                    userShareRepository.compensateTx(created.getId(), created.getSharedAt());
                }

                @Test
                @DisplayName("Then - 횟수가 감소한다")
                void thenDecreasesCount() throws UserShareException {
                    UserShare result = userShareRepository.shareTx(created.getId(), DAILY_SHARE_LIMIT);
                    assertThat(result.getId()).isEqualTo(created.getId());
                    assertThat(result.getCount()).isEqualTo(1);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 데이터일 때")
        class GivenNonExisting {
            UUID id;

            @BeforeEach
            void setUp() {
                id = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 보상을 요청하면")
            class WhenCompensating {

                @Test
                @DisplayName("Then - NOT_FOUND 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> userShareRepository.compensateTx(
                                    id, clock.now().toLocalDate()))
                            .isInstanceOf(UserShareException.class)
                            .extracting("error")
                            .isEqualTo(UserShareErrorCode.USER_SHARE_NOT_FOUND);
                }
            }
        }
    }

    @Nested
    @DisplayName("생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 새로운 데이터일 때")
        class GivenNew {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                UserShare result;

                @BeforeEach
                void setUp() throws UserShareException {
                    result = userShareRepository.create(userId, clock);
                }

                @Test
                @DisplayName("Then - 초기 상태로 생성된다")
                void thenCreated() {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getUserId()).isEqualTo(userId);
                    assertThat(result.getCount()).isZero();
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 존재하는 데이터일 때")
        class GivenExisting {
            UUID userId;
            UserShare first;

            @BeforeEach
            void setUp() throws UserShareException {
                userId = UUID.randomUUID();
                first = userShareRepository.create(userId, clock);
                userShareRepository.shareTx(first.getId(), DAILY_SHARE_LIMIT);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                UserShare second;

                @BeforeEach
                void setUp() throws UserShareException {
                    second = userShareRepository.create(userId, clock);
                }

                @Test
                @DisplayName("Then - 기존 데이터를 반환한다")
                void thenReturnsExisting() {
                    assertThat(second.getId()).isEqualTo(first.getId());
                    assertThat(second.getCount()).isEqualTo(1);
                }
            }
        }
    }
}
