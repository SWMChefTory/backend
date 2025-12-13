package com.cheftory.api.recipeinfo.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.identify.entity.RecipeIdentify;
import com.cheftory.api.recipeinfo.identify.exception.RecipeIdentifyErrorCode;
import com.cheftory.api.recipeinfo.identify.exception.RecipeIdentifyException;
import java.net.URI;
import java.time.LocalDateTime;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("RecipeIdentifyService")
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
  @DisplayName("create(url)")
  class Create {

    private URI url;
    private LocalDateTime now;

    @BeforeEach
    void init() {
      url = URI.create("https://www.youtube.com/watch?v=LOCK_TEST");
      now = LocalDateTime.now();
      when(clock.now()).thenReturn(now);
    }

    @Nested
    @DisplayName("Given - 유효한 URI가 주어졌을 때")
    class GivenValidUri {

      @Nested
      @DisplayName("When - 저장 요청을 하면")
      class WhenSaving {

        @Test
        @DisplayName("Then - 엔티티가 저장되고 반환된다")
        void thenSavedAndReturned() {
          RecipeIdentify entity = RecipeIdentify.create(url, clock);
          when(repository.save(any(RecipeIdentify.class))).thenReturn(entity);

          RecipeIdentify result = service.create(url);

          assertThat(result.getUrl()).isEqualTo(url);
          assertThat(result.getCreatedAt()).isEqualTo(now);
          verify(repository).save(any(RecipeIdentify.class));
        }
      }

      @Nested
      @DisplayName("When - 같은 URI로 두 번 저장 요청하면")
      class WhenSavingDuplicate {

        @Test
        @DisplayName("Then - RECIPE_IDENTIFY_PROGRESSING 예외가 발생한다")
        void thenThrowsRecipeIdentifyException() {
          when(repository.save(any(RecipeIdentify.class)))
              .thenThrow(new DataIntegrityViolationException("duplicate key"));

          RecipeIdentifyException ex =
              assertThrows(RecipeIdentifyException.class, () -> service.create(url));

          assertThat(ex.getErrorMessage().getErrorCode())
              .isEqualTo(RecipeIdentifyErrorCode.RECIPE_IDENTIFY_PROGRESSING.getErrorCode());
          verify(repository).save(any(RecipeIdentify.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("delete(url)")
  class Delete {

    @Nested
    @DisplayName("Given - 식별 URL이 저장되어 있을 때")
    class GivenExistingUrl {

      private URI url;

      @BeforeEach
      void setUp() {
        url = URI.create("https://example.com/lock");
      }

      @Nested
      @DisplayName("When - 삭제 요청을 하면")
      class WhenDeleting {

        @Test
        @DisplayName("Then - repository.deleteByUrl()이 호출된다")
        void thenRepositoryCalled() {
          service.delete(url);
          verify(repository).deleteByUrl(url);
        }
      }
    }
  }
}
