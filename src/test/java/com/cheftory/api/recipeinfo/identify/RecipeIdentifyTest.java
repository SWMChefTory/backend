package com.cheftory.api.recipeinfo.identify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.identify.entity.RecipeIdentify;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.*;

@DisplayName("RecipeIdentifyUrl 엔티티")
class RecipeIdentifyTest {

  @Nested
  @DisplayName("create(url, clock)")
  class Create {

    @Nested
    @DisplayName("Given - 유효한 URL이 주어졌을 때")
    class GivenValidUrl {

      private Clock clock;
      private LocalDateTime now;
      private URI url;

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        now = LocalDateTime.now();
        doReturn(now).when(clock).now();
        url = URI.create("https://www.youtube.com/watch?v=test_" + UUID.randomUUID());
      }

      @Nested
      @DisplayName("When - create() 메서드를 호출하면")
      class WhenCreatingEntity {

        private RecipeIdentify recipeIdentify;

        @BeforeEach
        void createEntity() {
          recipeIdentify = RecipeIdentify.create(url, clock);
        }

        @Test
        @DisplayName("Then - UUID가 자동 생성되고 URL/createdAt이 설정된다")
        void thenEntityFieldsArePopulated() {
          assertThat(recipeIdentify).isNotNull();
          assertThat(recipeIdentify.getId()).isNotNull();
          assertThat(recipeIdentify.getUrl()).isEqualTo(url);
          assertThat(recipeIdentify.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("Then - 동일한 URL로 다른 create()를 호출하면 ID는 달라진다")
        void thenDifferentIdsForDifferentInstances() {
          RecipeIdentify another = RecipeIdentify.create(url, clock);

          assertThat(another.getId()).isNotEqualTo(recipeIdentify.getId());
          assertThat(another.getUrl()).isEqualTo(url);
          assertThat(another.getCreatedAt()).isEqualTo(now);
        }
      }
    }
  }
}
