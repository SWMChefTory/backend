package com.cheftory.api.recipe.creation.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.identify.entity.RecipeIdentify;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeIdentifyRepository")
class RecipeIdentifyRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeIdentifyRepository repository;

    @MockitoBean
    private Clock clock;

    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
        doReturn(FIXED_TIME).when(clock).now();
    }

    @Nested
    @DisplayName("Identify URL 저장")
    class SaveIdentifyUrl {

        @Nested
        @DisplayName("Given - 유효한 URI 주어졌을 때")
        class GivenValidUriAndRecipeId {

            private URI url;

            @BeforeEach
            void init() {
                url = URI.create("https://www.youtube.com/watch?v=LOCK_" + UUID.randomUUID());
            }

            @Nested
            @DisplayName("When - 엔티티를 저장하면")
            class WhenSaving {

                private RecipeIdentify identifyUrl;

                @BeforeEach
                void beforeEach() {
                    identifyUrl = RecipeIdentify.create(url, clock);
                    repository.save(identifyUrl);
                }

                @Test
                @DisplayName("Then - 정상적으로 저장되고 조회된다")
                void thenPersistAndFind() {
                    RecipeIdentify found =
                            repository.findById(identifyUrl.getId()).orElseThrow();
                    assertThat(found.getUrl()).isEqualTo(url);
                    assertThat(found.getCreatedAt()).isEqualTo(FIXED_TIME);
                }
            }

            @Nested
            @DisplayName("When - 같은 URI와 recipeId를 중복 저장하면")
            class WhenSavingDuplicate {

                private RecipeIdentify identifyUrl;

                @BeforeEach
                void beforeEach() {
                    identifyUrl = RecipeIdentify.create(url, clock);
                    repository.save(identifyUrl);
                    repository.flush();
                }

                @Test
                @DisplayName("Then - 유니크 제약 위반(DataIntegrityViolationException)")
                void thenUniqueViolation() throws Exception {
                    assertThrows(DataIntegrityViolationException.class, () -> {
                        repository.save(RecipeIdentify.create(url, clock));
                        repository.flush();
                    });
                }
            }
        }
    }

    @Nested
    @DisplayName("Identify URL 조회")
    class FindIdentifyUrl {

        @Nested
        @DisplayName("Given - 저장된 URL이 있을 때")
        class GivenSavedUrl {

            private URI url;
            private RecipeIdentify identifyUrl;

            @BeforeEach
            void init() {
                url = URI.create("https://example.com/video/LOCK_" + UUID.randomUUID());
                identifyUrl = RecipeIdentify.create(url, clock);
                repository.save(identifyUrl);
            }

            @Test
            @DisplayName("findByUrl 호출 시 해당 레코드가 반환된다")
            void findByUrlReturnsRecord() {
                Optional<RecipeIdentify> found = repository.findByUrl(url);

                assertThat(found).isPresent();
                assertThat(found.get().getUrl()).isEqualTo(url);
            }
        }

        @Nested
        @DisplayName("Given - 저장되지 않은 URL일 때")
        class GivenNonSavedUrl {

            private URI url;

            @BeforeEach
            void init() {
                url = URI.create("https://example.com/video/NOT_SAVED");
            }

            @Test
            @DisplayName("findByUrl 호출 시 빈 Optional이 반환된다")
            void findByUrlReturnsEmpty() {
                Optional<RecipeIdentify> found = repository.findByUrl(url);

                assertThat(found).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Identify URL 삭제")
    @Transactional
    class DeleteIdentifyUrl {

        @Nested
        @DisplayName("Given - 저장된 URL과 recipeId가 있을 때")
        class GivenSavedUrl {

            private URI url;

            @BeforeEach
            void init() {
                url = URI.create("https://example.com/video/LOCK_" + UUID.randomUUID());
                repository.save(RecipeIdentify.create(url, clock));
            }

            @Test
            @DisplayName("deleteByUrl 호출 시 해당 레코드가 삭제된다")
            void deleteByUrlRemovesRow() {
                repository.deleteByUrl(url);

                Optional<RecipeIdentify> found = repository.findByUrl(url);
                assertThat(found).isEmpty();
            }
        }
    }
}
