package com.cheftory.api.recipe.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.recipe.category.repository.RecipeCategoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCategoryService 테스트")
class RecipeCategoryServiceTest {

    private RecipeCategoryRepository repository;
    private Clock clock;
    private RecipeCategoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeCategoryRepository.class);
        clock = mock(Clock.class);
        service = new RecipeCategoryService(repository, clock);
    }

    @Nested
    @DisplayName("카테고리 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            String name;
            UUID userId;
            UUID categoryId;
            LocalDateTime fixedTime;

            @BeforeEach
            void setUp() {
                name = "한식";
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                doReturn(fixedTime).when(clock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                UUID result;

                @BeforeEach
                void setUp() throws RecipeCategoryException {
                    doReturn(categoryId).when(repository).create(any(RecipeCategory.class));
                    result = service.create(name, userId);
                }

                @Test
                @DisplayName("Then - 생성된 카테고리 ID를 반환한다")
                void thenReturnsCategoryId() {
                    assertThat(result).isEqualTo(categoryId);
                    verify(repository).create(any(RecipeCategory.class));
                }
            }
        }

        @Nested
        @DisplayName("Given - 이름이 비어있을 때")
        class GivenEmptyName {
            String name;
            UUID userId;

            @BeforeEach
            void setUp() {
                name = "";
                userId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    RecipeCategoryException thrown =
                            assertThrows(RecipeCategoryException.class, () -> service.create(name, userId));
                    assertThat(thrown.getError()).isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 유효한 ID들이 주어졌을 때")
        class GivenValidIds {
            UUID userId;
            UUID categoryId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws RecipeCategoryException {
                    service.delete(userId, categoryId);
                }

                @Test
                @DisplayName("Then - 카테고리를 삭제한다")
                void thenDeletes() throws RecipeCategoryException {
                    verify(repository).delete(userId, categoryId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 카테고리일 때")
        class GivenNonExistentCategory {
            UUID userId;
            UUID categoryId;
            RecipeCategoryException exception;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                exception = new RecipeCategoryException(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
                doThrow(exception).when(repository).delete(userId, categoryId);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - 예외를 던진다")
                void thenThrowsException() {
                    RecipeCategoryException thrown =
                            assertThrows(RecipeCategoryException.class, () -> service.delete(userId, categoryId));
                    assertSame(exception, thrown);
                }
            }
        }
    }

    @Nested
    @DisplayName("유저 카테고리 목록 조회 (getUsers)")
    class GetUsers {

        @Nested
        @DisplayName("Given - 유저 ID가 주어졌을 때")
        class GivenUserId {
            UUID userId;
            List<RecipeCategory> categories;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                categories = List.of(mock(RecipeCategory.class), mock(RecipeCategory.class));
                doReturn(categories).when(repository).gets(userId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeCategory> result;

                @BeforeEach
                void setUp() {
                    result = service.getUsers(userId);
                }

                @Test
                @DisplayName("Then - 카테고리 목록을 반환한다")
                void thenReturnsCategories() {
                    assertThat(result).hasSize(2);
                    assertThat(result).containsExactlyElementsOf(categories);
                    verify(repository).gets(userId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 카테고리가 없을 때")
        class GivenNoCategories {
            UUID userId;

            @BeforeEach
            void setUp() {
                userId = UUID.randomUUID();
                doReturn(List.of()).when(repository).gets(userId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {
                List<RecipeCategory> result;

                @BeforeEach
                void setUp() {
                    result = service.getUsers(userId);
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmpty() {
                    assertThat(result).isEmpty();
                    verify(repository).gets(userId);
                }
            }
        }
    }

    @Nested
    @DisplayName("카테고리 존재 여부 확인 (exists)")
    class Exists {

        @Nested
        @DisplayName("Given - 카테고리 ID가 주어졌을 때")
        class GivenCategoryId {
            UUID categoryId;

            @BeforeEach
            void setUp() {
                categoryId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 카테고리가 존재하면")
            class WhenExists {
                boolean result;

                @BeforeEach
                void setUp() {
                    doReturn(true).when(repository).exists(categoryId);
                    result = service.exists(categoryId);
                }

                @Test
                @DisplayName("Then - true를 반환한다")
                void thenReturnsTrue() {
                    assertThat(result).isTrue();
                    verify(repository).exists(categoryId);
                }
            }

            @Nested
            @DisplayName("When - 카테고리가 존재하지 않으면")
            class WhenNotExists {
                boolean result;

                @BeforeEach
                void setUp() {
                    doReturn(false).when(repository).exists(categoryId);
                    result = service.exists(categoryId);
                }

                @Test
                @DisplayName("Then - false를 반환한다")
                void thenReturnsFalse() {
                    assertThat(result).isFalse();
                    verify(repository).exists(categoryId);
                }
            }
        }
    }
}
