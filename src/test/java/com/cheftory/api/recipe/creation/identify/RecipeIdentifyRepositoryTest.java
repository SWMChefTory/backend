package com.cheftory.api.recipe.creation.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import com.cheftory.api.recipe.creation.identify.repository.RecipeIdentifyRepository;
import com.cheftory.api.recipe.creation.identify.repository.RecipeIdentifyRepositoryImpl;
import java.net.URI;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(RecipeIdentifyRepositoryImpl.class)
@DisplayName("RecipeIdentifyRepository 테스트")
class RecipeIdentifyRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeIdentifyRepository repository;

    private Clock clock;
    private final LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(now).when(clock).now();
    }

    @Nested
    @DisplayName("식별 정보 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 식별 정보가 주어졌을 때")
        class GivenValidIdentify {
            URI url;
            RecipeIdentify identify;

            @BeforeEach
            void setUp() {
                url = URI.create("https://www.youtube.com/watch?v=TEST_1");
                identify = RecipeIdentify.create(url, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenCreating {
                RecipeIdentify result;

                @BeforeEach
                void setUp() throws RecipeIdentifyException {
                    result = repository.create(identify, clock);
                }

                @Test
                @DisplayName("Then - 식별 정보를 저장하고 반환한다")
                void thenSavesAndReturns() {
                    assertThat(result).isNotNull();
                    assertThat(result.getUrl()).isEqualTo(url);
                    assertThat(result.getCreatedAt()).isEqualTo(now);
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 존재하는 URL이 주어졌을 때")
        class GivenDuplicateUrl {
            URI url;
            RecipeIdentify identify2;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                url = URI.create("https://www.youtube.com/watch?v=DUPLICATE");
                RecipeIdentify identify1 = RecipeIdentify.create(url, clock);
                repository.create(identify1, clock);

                identify2 = RecipeIdentify.create(url, clock);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenCreating {
                Throwable thrown;

                @BeforeEach
                void setUp() {
                    thrown = catchThrowable(() -> repository.create(identify2, clock));
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
        @DisplayName("Given - 존재하는 URL이 주어졌을 때")
        class GivenExistingUrl {
            URI url;

            @BeforeEach
            void setUp() throws RecipeIdentifyException {
                url = URI.create("https://www.youtube.com/watch?v=DELETE_TEST");
                RecipeIdentify identify = RecipeIdentify.create(url, clock);
                repository.create(identify, clock);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() {
                    repository.delete(url);
                }

                @Test
                @DisplayName("Then - 해당 식별 정보를 삭제한다")
                void thenDeletesIdentify() throws RecipeIdentifyException {
                    RecipeIdentify newIdentify = RecipeIdentify.create(url, clock);
                    RecipeIdentify result = repository.create(newIdentify, clock);
                    assertThat(result).isNotNull();
                }
            }
        }
    }
}
