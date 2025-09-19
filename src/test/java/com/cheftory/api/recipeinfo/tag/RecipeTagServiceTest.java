package com.cheftory.api.recipeinfo.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("RecipeTagService 테스트")
public class RecipeTagServiceTest {

  private RecipeTagRepository recipeTagRepository;
  private Clock clock;
  private RecipeTagService recipeTagService;
  private LocalDateTime fixedTime;

  @BeforeEach
  public void setUp() {
    recipeTagRepository = mock(RecipeTagRepository.class);
    clock = mock(Clock.class);
    fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);

    when(clock.now()).thenReturn(fixedTime);

    recipeTagService = new RecipeTagService(recipeTagRepository, clock);
  }

  @DisplayName("레시피 태그 생성")
  @Nested
  class CreateRecipeTags {

    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    @Nested
    class GivenValidParameters {

      @Test
      @DisplayName("When - 레시피 태그를 생성하면 모든 태그가 저장된다")
      void shouldCreateAllRecipeTags() {
        UUID recipeId = UUID.randomUUID();
        List<String> tags = List.of("한식", "매운맛", "간단요리");

        recipeTagService.create(recipeId, tags);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RecipeTag>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipeTagRepository).saveAll(captor.capture());

        List<RecipeTag> capturedTags = captor.getValue();
        assertThat(capturedTags).hasSize(3);

        for (RecipeTag tag : capturedTags) {
          assertThat(tag.getRecipeId()).isEqualTo(recipeId);
          assertThat(tag.getTag()).isIn("한식", "매운맛", "간단요리");
          assertThat(tag.getCreatedAt()).isEqualTo(fixedTime);
        }
      }

      @Test
      @DisplayName("When - 빈 태그 목록으로 생성하면 빈 목록이 저장된다")
      void shouldSaveEmptyListWhenEmptyTagsProvided() {
        UUID recipeId = UUID.randomUUID();
        List<String> emptyTags = Collections.emptyList();

        recipeTagService.create(recipeId, emptyTags);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RecipeTag>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipeTagRepository).saveAll(captor.capture());

        List<RecipeTag> capturedTags = captor.getValue();
        assertThat(capturedTags).isEmpty();
      }
    }
  }

  @DisplayName("레시피 태그 조회")
  @Nested
  class FindRecipeTags {

    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    @Nested
    class GivenValidRecipeId {

      @Test
      @DisplayName("When - 레시피 태그를 조회하면 해당 레시피의 태그 목록을 반환한다")
      void thenReturnTagsForRecipe() {
        UUID recipeId = UUID.randomUUID();
        List<RecipeTag> mockTags =
            List.of(createMockRecipeTag("한식", recipeId), createMockRecipeTag("매운맛", recipeId));

        doReturn(mockTags).when(recipeTagRepository).findAllByRecipeId(recipeId);

        List<RecipeTag> result = recipeTagService.finds(recipeId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛");
        assertThat(result).allMatch(tag -> tag.getRecipeId().equals(recipeId));

        verify(recipeTagRepository).findAllByRecipeId(recipeId);
      }
    }

    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    @Nested
    class GivenNonExistentRecipeId {

      @Test
      @DisplayName("When - 레시피 태그를 조회하면 빈 목록을 반환한다")
      void thenReturnEmptyList() {
        UUID nonExistentRecipeId = UUID.randomUUID();
        doReturn(Collections.emptyList())
            .when(recipeTagRepository)
            .findAllByRecipeId(nonExistentRecipeId);

        List<RecipeTag> result = recipeTagService.finds(nonExistentRecipeId);

        assertThat(result).isEmpty();
        verify(recipeTagRepository).findAllByRecipeId(nonExistentRecipeId);
      }
    }
  }

  @DisplayName("여러 레시피의 태그 조회")
  @Nested
  class FindTagsForMultipleRecipes {

    @Test
    @DisplayName("Given - 여러 레시피 ID가 주어졌을 때 모든 레시피의 태그를 반환한다")
    void shouldReturnTagsForMultipleRecipes() {
      UUID recipeId1 = UUID.randomUUID();
      UUID recipeId2 = UUID.randomUUID();
      List<UUID> recipeIds = List.of(recipeId1, recipeId2);

      List<RecipeTag> mockTags =
          List.of(
              createMockRecipeTag("한식", recipeId1),
              createMockRecipeTag("매운맛", recipeId1),
              createMockRecipeTag("양식", recipeId2));

      doReturn(mockTags).when(recipeTagRepository).findAllByRecipeIdIn(recipeIds);

      List<RecipeTag> result = recipeTagService.findIn(recipeIds);

      assertThat(result).hasSize(3);
      assertThat(result).extracting(RecipeTag::getRecipeId).containsOnly(recipeId1, recipeId2);
      assertThat(result).extracting(RecipeTag::getTag).containsExactlyInAnyOrder("한식", "매운맛", "양식");

      verify(recipeTagRepository).findAllByRecipeIdIn(recipeIds);
    }
  }

  private RecipeTag createMockRecipeTag(String tagName, UUID recipeId) {
    RecipeTag tag = mock(RecipeTag.class);
    UUID tagId = UUID.randomUUID();

    doReturn(tagId).when(tag).getId();
    doReturn(tagName).when(tag).getTag();
    doReturn(recipeId).when(tag).getRecipeId();
    doReturn(fixedTime).when(tag).getCreatedAt();

    return tag;
  }
}
