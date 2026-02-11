package com.cheftory.api.recipe.report;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.report.entity.RecipeReport;
import com.cheftory.api.recipe.report.entity.RecipeReportReason;
import com.cheftory.api.recipe.report.exception.RecipeReportErrorCode;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import com.cheftory.api.recipe.report.repository.RecipeReportRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeReportService 테스트")
public class RecipeReportServiceTest {

    private RecipeReportRepository repository;
    private RecipeReportService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(RecipeReportRepository.class);
        clock = mock(Clock.class);
        service = new RecipeReportService(repository, clock);
    }

    @Nested
    @DisplayName("레시피 신고 생성 (report)")
    class Report {

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
                description = "부적절한 콘텐츠입니다.";
            }

            @Nested
            @DisplayName("When - 신고를 요청하면")
            class WhenReporting {

                @BeforeEach
                void setUp() throws RecipeReportException {
                    doNothing().when(repository).create(any(RecipeReport.class));
                }

                @Test
                @DisplayName("Then - 신고가 생성된다")
                void thenCreatesReport() throws RecipeReportException {
                    assertThatNoException().isThrownBy(() -> service.report(reporterId, recipeId, reason, description));
                    verify(repository).create(any(RecipeReport.class));
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

            @BeforeEach
            void setUp() throws RecipeReportException {
                reporterId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                reason = RecipeReportReason.OTHER;
                description = "기타 사유입니다.";

                doThrow(new RecipeReportException(RecipeReportErrorCode.DUPLICATE_REPORT))
                        .when(repository)
                        .create(any(RecipeReport.class));
            }

            @Nested
            @DisplayName("When - 신고를 요청하면")
            class WhenReporting {

                @Test
                @DisplayName("Then - DUPLICATE_REPORT 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> service.report(reporterId, recipeId, reason, description))
                            .isInstanceOf(RecipeReportException.class)
                            .extracting("error")
                            .isEqualTo(RecipeReportErrorCode.DUPLICATE_REPORT);
                }
            }
        }
    }
}
