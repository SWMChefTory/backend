package com.cheftory.api.recipeinfo.caption;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.entity.LangCodeType;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption.Segment;
import com.cheftory.api.recipeinfo.caption.repository.RecipeCaptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeCaptionRepositoryTest")
public class RecipeCaptionRepositoryTest extends DbContextTest {

  @Autowired private RecipeCaptionRepository recipeCaptionRepository;

  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 자막 저장")
  class SaveRecipeCaption {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {
      private Segment segment;
      private LangCodeType langCodeType;
      private UUID recipeId;
      private Clock clock;
      private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

      @BeforeEach
      void setUp() {
        clock = mock(Clock.class);
        segment = Segment.builder().text("Hello World").start(0.0).end(2.0).build();
        langCodeType = LangCodeType.ko;
        recipeId = UUID.randomUUID();
        doReturn(FIXED_TIME).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 자막을 저장하면")
      class WhenSaveRecipeCaption {

        private RecipeCaption recipeCaption;

        @BeforeEach
        void setUp() {
          recipeCaption =
              RecipeCaption.from(List.of(segment), langCodeType, recipeId, clock);
          recipeCaptionRepository.save(recipeCaption);
        }

        @DisplayName("Then - 레시피 자막이 저장된다")
        @Test
        void thenRecipeCaptionIsSaved() {
          RecipeCaption savedRecipeCaption =
              recipeCaptionRepository.findById(recipeCaption.getId()).orElse(null);
          assertNotNull(savedRecipeCaption);
          assertThat(savedRecipeCaption.getId()).isEqualTo(recipeCaption.getId());
          assertThat(savedRecipeCaption.getRecipeId()).isEqualTo(recipeId);
          assertThat(savedRecipeCaption.getLangCode()).isEqualTo(langCodeType);
          assertThat(savedRecipeCaption.getSegments()).isNotNull();
          assertThat(savedRecipeCaption.getSegments().size()).isEqualTo(1);
          assertThat(savedRecipeCaption.getSegments().getFirst().getText()).isEqualTo(segment.getText());
          assertThat(savedRecipeCaption.getSegments().getFirst().getStart())
              .isEqualTo(segment.getStart());
          assertThat(savedRecipeCaption.getSegments().getFirst().getEnd()).isEqualTo(segment.getEnd());
          assertThat(savedRecipeCaption.getCreatedAt()).isEqualTo(FIXED_TIME);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 자막 조회")
  class FindRecipeCaptionById {

    private RecipeCaption recipeCaption;
    private UUID recipeId;
    private Clock clock;
    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    @BeforeEach
    void setUp() {
      clock = mock(Clock.class);
      recipeId = UUID.randomUUID();
      doReturn(FIXED_TIME).when(clock).now();
    }

    @Nested
    @DisplayName("When - 레시피 자막을 조회하면")
    class WhenFindRecipeCaptionById {

      private RecipeCaption foundRecipeCaption;

      @BeforeEach
      void setUp() {
        recipeCaption =
            RecipeCaption.from(
                List.of(Segment.builder().text("Hello World").start(0.0).end(2.0).build()),
                LangCodeType.ko,
                recipeId,
                clock);
        recipeCaptionRepository.save(recipeCaption);

        foundRecipeCaption = recipeCaptionRepository.findById(recipeCaption.getId()).orElse(null);
      }

      @DisplayName("Then - 레시피 자막이 조회된다")
      @Test
      void thenRecipeCaptionIsFound() {
        assertNotNull(foundRecipeCaption);
        assertThat(foundRecipeCaption.getId()).isEqualTo(recipeCaption.getId());
        assertThat(foundRecipeCaption.getRecipeId()).isEqualTo(recipeId);
        assertThat(foundRecipeCaption.getLangCode()).isEqualTo(LangCodeType.ko);
        assertThat(foundRecipeCaption.getSegments()).isNotNull();
        assertThat(foundRecipeCaption.getSegments().size()).isEqualTo(1);
        assertThat(foundRecipeCaption.getSegments().getFirst().getText()).isEqualTo("Hello World");
        assertThat(foundRecipeCaption.getSegments().getFirst().getStart()).isEqualTo(0.0);
        assertThat(foundRecipeCaption.getSegments().getFirst().getEnd()).isEqualTo(2.0);
        assertThat(foundRecipeCaption.getCreatedAt()).isEqualTo(FIXED_TIME);
      }
    }

    @Nested
    @DisplayName("When - 레시피 아이디로 레시피 자막을 조회하면")
    class WhenFindRecipeCaptionByRecipeId {

      private RecipeCaption foundRecipeCaption;

      @BeforeEach
      void setUp() {
        recipeCaption =
            RecipeCaption.from(
                List.of(Segment.builder().text("Hello World").start(0.0).end(2.0).build()),
                LangCodeType.ko,
                recipeId,
                clock);
        recipeCaptionRepository.save(recipeCaption);

        foundRecipeCaption = recipeCaptionRepository.findByRecipeId(recipeId).orElse(null);
      }

      @DisplayName("Then - 레시피 자막이 조회된다")
      @Test
      void thenRecipeCaptionIsFound() {
        assertNotNull(foundRecipeCaption);
        assertThat(foundRecipeCaption.getId()).isEqualTo(recipeCaption.getId());
        assertThat(foundRecipeCaption.getRecipeId()).isEqualTo(recipeId);
        assertThat(foundRecipeCaption.getLangCode()).isEqualTo(LangCodeType.ko);
        assertThat(foundRecipeCaption.getSegments()).isNotNull();
        assertThat(foundRecipeCaption.getSegments().size()).isEqualTo(1);
        assertThat(foundRecipeCaption.getSegments().getFirst().getText()).isEqualTo("Hello World");
        assertThat(foundRecipeCaption.getSegments().getFirst().getStart()).isEqualTo(0.0);
        assertThat(foundRecipeCaption.getSegments().getFirst().getEnd()).isEqualTo(2.0);
        assertThat(foundRecipeCaption.getCreatedAt()).isEqualTo(FIXED_TIME);
      }
    }
  }
}
