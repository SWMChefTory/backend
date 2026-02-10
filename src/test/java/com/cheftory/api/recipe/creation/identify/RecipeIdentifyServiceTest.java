package com.cheftory.api.recipe.creation.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import com.cheftory.api.recipe.creation.identify.repository.RecipeIdentifyRepository;
import java.net.URI;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeIdentifyService 테스트")
class RecipeIdentifyServiceTest {

    private RecipeIdentifyService service;
    private RecipeIdentifyRepository repository;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeIdentifyRepository.class);
        clock = mock(Clock.class);
        service = new RecipeIdentifyService(repository, clock);
    }

    @Nested
    @DisplayName("식별 정보 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 URL이 주어졌을 때")
        class GivenValidUrl {
            URI url;
            LocalDateTime now;
            RecipeIdentify expected;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                url = URI.create("https://www.youtube.com/watch?v=LOCK_TEST");
                now = LocalDateTime.now();
                expected = mock(RecipeIdentify.class);

                doReturn(now).when(clock).now();
                doReturn(expected).when(repository).create(any(RecipeIdentify.class), eq(clock));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeIdentify result;

                @BeforeEach
                void setUp() throws RecipeIdentifyException {
                    result = service.create(url);
                }

                @Test
                @DisplayName("Then - 식별 정보를 생성하여 저장하고 반환한다")
                void thenCreatesAndReturns() throws RecipeIdentifyException {
                    assertThat(result).isEqualTo(expected);
                    verify(repository).create(any(RecipeIdentify.class), eq(clock));
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 처리 중인 URL이 주어졌을 때")
        class GivenDuplicateUrl {
            URI url;
            LocalDateTime now;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                url = URI.create("https://www.youtube.com/watch?v=DUPLICATE");
                now = LocalDateTime.now();

                doReturn(now).when(clock).now();
                doThrow(new RecipeIdentifyException(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING))
                        .when(repository)
                        .create(any(RecipeIdentify.class), eq(clock));
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                Throwable thrown;

                @BeforeEach
                void setUp() {
                    thrown = catchThrowable(() -> service.create(url));
                }

                @Test
                @DisplayName("Then - RECIPE_IDENTIFY_PROGRESSING 예외를 던진다")
                void thenThrowsException() {
                    assertThat(thrown)
                            .isInstanceOf(RecipeIdentifyException.class)
                            .hasFieldOrPropertyWithValue(
                                    "error.errorCode",
                                    RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING.getErrorCode());
                }
            }
        }
    }

    @Nested
    @DisplayName("식별 정보 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - URL이 주어졌을 때")
        class GivenUrl {
            URI url;

            @BeforeEach
            void setUp() {
                url = URI.create("https://example.com/lock");
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    service.delete(url);
                }

                @Test
                @DisplayName("Then - 해당 식별 정보를 삭제한다")
                void thenDeletes() {
                    verify(repository).delete(url);
                }
            }
        }
    }
}
