package com.cheftory.api.search.indexing.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchIndexingScheduler 테스트")
class SearchIndexingSchedulerTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Clock clock;

    @Mock
    private Job autocompleteIndexJob;

    @Mock
    private Job searchQueryUpsertIndexJob;

    @Mock
    private Job searchQueryDeleteIndexJob;

    @Mock
    private JobExecution jobExecution;

    private SearchIndexingScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SearchIndexingScheduler(
                jobOperator, clock, autocompleteIndexJob, searchQueryUpsertIndexJob, searchQueryDeleteIndexJob);
    }

    @Test
    @DisplayName("자동완성 잡은 upperBoundUpdatedAt/timestamp/runId 파라미터로 실행한다")
    void shouldStartAutocompleteJobWithUpperBound() throws Exception {
        long nowMillis = 1_700_000_000_000L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 11, 0, 0);
        when(clock.nowMillis()).thenReturn(nowMillis);
        when(clock.now()).thenReturn(now);
        when(jobExecution.getId()).thenReturn(101L);
        when(jobOperator.start(eq(autocompleteIndexJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        scheduler.runAutocompleteIndexJob();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(autocompleteIndexJob), captor.capture());

        JobParameters parameters = captor.getValue();
        assertThat(parameters.getLong("timestamp")).isEqualTo(nowMillis);
        assertThat(parameters.getString("runId")).isEqualTo("autocompleteIndexJob-" + nowMillis);
        assertThat(parameters.getString("upperBoundUpdatedAt")).isEqualTo(now.toString());
    }

    @Test
    @DisplayName("검색어 upsert 잡은 upperBoundUpdatedAt 파라미터를 포함한다")
    void shouldStartSearchQueryUpsertJobWithUpperBound() throws Exception {
        long nowMillis = 1_700_000_000_123L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 12, 34, 56);
        when(clock.nowMillis()).thenReturn(nowMillis);
        when(clock.now()).thenReturn(now);
        when(jobExecution.getId()).thenReturn(201L);
        when(jobOperator.start(eq(searchQueryUpsertIndexJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        scheduler.runSearchQueryUpsertIndexJob();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(searchQueryUpsertIndexJob), captor.capture());

        JobParameters parameters = captor.getValue();
        assertThat(parameters.getLong("timestamp")).isEqualTo(nowMillis);
        assertThat(parameters.getString("runId")).isEqualTo("searchQueryUpsertIndexJob-" + nowMillis);
        assertThat(parameters.getString("upperBoundUpdatedAt")).isEqualTo(now.toString());
    }

    @Test
    @DisplayName("검색어 delete 잡은 upperBoundUpdatedAt 파라미터를 포함한다")
    void shouldStartSearchQueryDeleteJobWithUpperBound() throws Exception {
        long nowMillis = 1_700_000_000_456L;
        LocalDateTime now = LocalDateTime.of(2026, 3, 1, 13, 0, 1);
        when(clock.nowMillis()).thenReturn(nowMillis);
        when(clock.now()).thenReturn(now);
        when(jobExecution.getId()).thenReturn(301L);
        when(jobOperator.start(eq(searchQueryDeleteIndexJob), any(JobParameters.class)))
                .thenReturn(jobExecution);

        scheduler.runSearchQueryDeleteIndexJob();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(searchQueryDeleteIndexJob), captor.capture());

        JobParameters parameters = captor.getValue();
        assertThat(parameters.getLong("timestamp")).isEqualTo(nowMillis);
        assertThat(parameters.getString("runId")).isEqualTo("searchQueryDeleteIndexJob-" + nowMillis);
        assertThat(parameters.getString("upperBoundUpdatedAt")).isEqualTo(now.toString());
    }

    @Test
    @DisplayName("잡 시작 예외가 발생해도 스케줄러 호출은 예외를 던지지 않는다")
    void shouldSwallowExceptionWhenStartFails() throws Exception {
        when(clock.nowMillis()).thenReturn(1_700_000_000_999L);
        when(clock.now()).thenReturn(LocalDateTime.of(2026, 3, 1, 14, 0));
        when(jobOperator.start(eq(searchQueryUpsertIndexJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("failed"));

        assertThatCode(() -> scheduler.runSearchQueryUpsertIndexJob()).doesNotThrowAnyException();
        verify(jobOperator).start(eq(searchQueryUpsertIndexJob), any(JobParameters.class));
    }
}
