package com.cheftory.api.recipe.report.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeReport 엔티티 테스트")
public class RecipeReportTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        doReturn(LocalDateTime.now()).when(clock).now();
    }

    @Nested
    @DisplayName("레시피 신고 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            UUID reporterId;
            UUID recipeId;
            RecipeReportReason reason;
            String description;

            @BeforeEach
            void setUp() {
                reporterId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                reason = RecipeReportReason.INAPPROPRIATE_CONTENT;
                description = "부적절한 내용이 포함되어 있습니다.";
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeReport report;

                @BeforeEach
                void setUp() {
                    report = RecipeReport.create(clock, reporterId, recipeId, reason, description);
                }

                @Test
                @DisplayName("Then - 신고 엔티티가 생성된다")
                void thenCreated() {
                    assertThat(report).isNotNull();
                    assertThat(report.getId()).isNotNull();
                    assertThat(report.getRecipeId()).isEqualTo(recipeId);
                    assertThat(report.getReporterId()).isEqualTo(reporterId);
                    assertThat(report.getReason()).isEqualTo(reason);
                    assertThat(report.getDescription()).isEqualTo(description);
                    assertThat(report.getCreatedAt()).isNotNull();
                }
            }
        }

        @Nested
        @DisplayName("Given - description이 null일 때")
        class GivenNullDescription {
            UUID reporterId;
            UUID recipeId;
            RecipeReportReason reason;

            @BeforeEach
            void setUp() {
                reporterId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                reason = RecipeReportReason.OTHER;
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeReport report;

                @BeforeEach
                void setUp() {
                    report = RecipeReport.create(clock, reporterId, recipeId, reason, null);
                }

                @Test
                @DisplayName("Then - 빈 문자열로 변환되어 생성된다")
                void thenCreatedWithEmptyDescription() {
                    assertThat(report.getDescription()).isEmpty();
                }
            }
        }
    }
}
