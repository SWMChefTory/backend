package com.cheftory.api.recipe.content.briefing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeBriefing 엔티티")
public class RecipeBriefingTest {

    @Nested
    @DisplayName("레시피 브리핑 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID recipeId;
            String content;
            LocalDateTime fixedTime;
            Clock mockClock;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                content = "이 요리는 매우 맛있습니다";
                fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
                mockClock = mock(Clock.class);
                doReturn(fixedTime).when(mockClock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeBriefing result;

                @BeforeEach
                void setUp() {
                    result = RecipeBriefing.create(recipeId, content, mockClock);
                }

                @Test
                @DisplayName("Then - 레시피 브리핑이 올바르게 생성된다")
                void thenCreatedCorrectly() {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getContent()).isEqualTo(content);
                    assertThat(result.getCreatedAt()).isEqualTo(fixedTime);
                }
            }
        }

        @Nested
        @DisplayName("Given - 긴 컨텐츠가 주어졌을 때")
        class GivenLongContent {
            UUID recipeId;
            String longContent;
            LocalDateTime fixedTime;
            Clock mockClock;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                longContent = "이 요리는 정말 맛있고 건강한 재료들로 만들어진 요리입니다. "
                        + "조리 시간은 약 45분 정도 소요되며, 초보자도 쉽게 따라할 수 있는 레시피입니다. "
                        + "특히 이 요리의 특징은 영양가가 높고 칼로리가 낮다는 점입니다.";
                fixedTime = LocalDateTime.of(2024, 5, 1, 19, 45, 30);
                mockClock = mock(Clock.class);
                doReturn(fixedTime).when(mockClock).now();
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeBriefing result;

                @BeforeEach
                void setUp() {
                    result = RecipeBriefing.create(recipeId, longContent, mockClock);
                }

                @Test
                @DisplayName("Then - 긴 컨텐츠도 올바르게 저장된다")
                void thenCreatedCorrectly() {
                    assertThat(result).isNotNull();
                    assertThat(result.getContent()).isEqualTo(longContent);
                    assertThat(result.getContent().length()).isGreaterThan(100);
                    assertThat(result.getRecipeId()).isEqualTo(recipeId);
                    assertThat(result.getCreatedAt()).isEqualTo(fixedTime);
                }
            }
        }
    }
}
