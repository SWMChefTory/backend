package com.cheftory.api.recipe.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.report.entity.RecipeReport;
import com.cheftory.api.recipe.report.entity.RecipeReportReason;
import com.cheftory.api.recipe.report.exception.RecipeReportErrorCode;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import com.cheftory.api.recipe.report.repository.RecipeReportJpaRepository;
import com.cheftory.api.recipe.report.repository.RecipeReportRepository;
import com.cheftory.api.recipe.report.repository.RecipeReportRepositoryImpl;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeReportRepository 테스트")
@Import(RecipeReportRepositoryImpl.class)
public class RecipeReportRepositoryTest extends DbContextTest {

    @Autowired
    private RecipeReportRepository recipeReportRepository;

    @Autowired
    private RecipeReportJpaRepository recipeReportJpaRepository;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(LocalDateTime.now());
    }

    @Nested
    @DisplayName("신고 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 신고가 주어졌을 때")
        class GivenValidReport {
            UUID reporterId;
            UUID recipeId;
            RecipeReportReason reason;
            String description;
            RecipeReport report;

            @BeforeEach
            void setUp() {
                reporterId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                reason = RecipeReportReason.INAPPROPRIATE_CONTENT;
                description = "부적절한 콘텐츠가 포함되어 있습니다.";
                report = RecipeReport.create(clock, reporterId, recipeId, reason, description);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeReportException {
                    recipeReportRepository.create(report);
                }

                @Test
                @DisplayName("Then - 신고가 저장된다")
                void thenSaved() {
                    RecipeReport saved =
                            recipeReportJpaRepository.findById(report.getId()).orElseThrow();
                    assertThat(saved.getRecipeId()).isEqualTo(recipeId);
                    assertThat(saved.getReporterId()).isEqualTo(reporterId);
                    assertThat(saved.getReason()).isEqualTo(reason);
                    assertThat(saved.getDescription()).isEqualTo(description);
                }
            }
        }

        @Nested
        @DisplayName("Given - 중복 신고일 때")
        class GivenDuplicateReport {
            UUID reporterId;
            UUID recipeId;
            RecipeReportReason reason;
            String description;
            RecipeReport firstReport;
            RecipeReport secondReport;

            @BeforeEach
            void setUp() {
                reporterId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                reason = RecipeReportReason.OTHER;
                description = "기타 사유입니다.";

                when(clock.now()).thenReturn(LocalDateTime.of(2024, 1, 1, 10, 0), LocalDateTime.of(2024, 1, 1, 11, 0));

                firstReport = RecipeReport.create(clock, reporterId, recipeId, reason, description);
                secondReport = RecipeReport.create(clock, reporterId, recipeId, reason, "다른 기타 사유입니다.");
            }

            @Nested
            @DisplayName("When - 중복 생성을 요청하면")
            class WhenCreatingDuplicate {

                @BeforeEach
                void setUp() throws RecipeReportException {
                    recipeReportRepository.create(firstReport);
                }

                @Test
                @DisplayName("Then - DUPLICATE_REPORT 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> recipeReportRepository.create(secondReport))
                            .isInstanceOf(RecipeReportException.class)
                            .extracting("error")
                            .isEqualTo(RecipeReportErrorCode.DUPLICATE_REPORT);
                }
            }
        }
    }
}
