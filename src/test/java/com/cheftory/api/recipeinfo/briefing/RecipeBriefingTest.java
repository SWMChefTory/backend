package com.cheftory.api.recipeinfo.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBriefing 엔티티 테스트")
public class RecipeBriefingTest {

  @DisplayName("RecipeBriefing.create() - 정상 생성")
  @Test
  void shouldCreateRecipeBriefingCorrectly() {
    UUID recipeId = UUID.randomUUID();
    String content = "이 요리는 매우 맛있습니다";
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

    Clock mockClock = mock(Clock.class);
    doReturn(fixedTime).when(mockClock).now();

    RecipeBriefing result = RecipeBriefing.create(recipeId, content, mockClock);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getRecipeId()).isEqualTo(recipeId);
    assertThat(result.getContent()).isEqualTo(content);
    assertThat(result.getCreatedAt()).isEqualTo(fixedTime);
  }

  @DisplayName("RecipeBriefing.create() - 다른 recipeId로 생성")
  @Test
  void shouldCreateWithDifferentRecipeId() {
    UUID firstRecipeId = UUID.randomUUID();
    UUID secondRecipeId = UUID.randomUUID();
    String content = "조리 시간이 30분 정도 걸립니다";
    LocalDateTime fixedTime = LocalDateTime.of(2024, 2, 1, 15, 30, 0);

    Clock mockClock = mock(Clock.class);
    doReturn(fixedTime).when(mockClock).now();

    RecipeBriefing firstBriefing = RecipeBriefing.create(firstRecipeId, content, mockClock);
    RecipeBriefing secondBriefing = RecipeBriefing.create(secondRecipeId, content, mockClock);

    assertThat(firstBriefing.getRecipeId()).isEqualTo(firstRecipeId);
    assertThat(secondBriefing.getRecipeId()).isEqualTo(secondRecipeId);
    assertThat(firstBriefing.getContent()).isEqualTo(content);
    assertThat(secondBriefing.getContent()).isEqualTo(content);
    assertThat(firstBriefing.getId()).isNotEqualTo(secondBriefing.getId());
  }

  @DisplayName("RecipeBriefing.create() - 긴 컨텐츠로 생성")
  @Test
  void shouldCreateWithLongContent() {
    // given
    UUID recipeId = UUID.randomUUID();
    String longContent =
        "이 요리는 정말 맛있고 건강한 재료들로 만들어진 요리입니다. "
            + "조리 시간은 약 45분 정도 소요되며, 초보자도 쉽게 따라할 수 있는 레시피입니다. "
            + "특히 이 요리의 특징은 영양가가 높고 칼로리가 낮다는 점입니다.";
    LocalDateTime fixedTime = LocalDateTime.of(2024, 5, 1, 19, 45, 30);

    Clock mockClock = mock(Clock.class);
    doReturn(fixedTime).when(mockClock).now();

    // when
    RecipeBriefing result = RecipeBriefing.create(recipeId, longContent, mockClock);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(longContent);
    assertThat(result.getContent().length()).isGreaterThan(100);
    assertThat(result.getRecipeId()).isEqualTo(recipeId);
    assertThat(result.getCreatedAt()).isEqualTo(fixedTime);
  }
}
