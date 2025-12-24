package com.cheftory.api.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.batch.RecipeValidationScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeValidationScheduler 테스트")
class RecipeInfoValidationSchedulerTest {

  @Mock private JobLauncher jobLauncher;

  @Mock private Job youtubeValidationJob;

  @Mock private JobExecution jobExecution;

  @InjectMocks private RecipeValidationScheduler scheduler;

  @Test
  @DisplayName("스케줄러가 YouTube 검증 Job을 성공적으로 실행")
  void shouldRunYouTubeValidationJobSuccessfully() throws Exception {
    when(jobExecution.getId()).thenReturn(1L);
    when(jobLauncher.run(eq(youtubeValidationJob), any(JobParameters.class)))
        .thenReturn(jobExecution);

    scheduler.runYouTubeValidation();

    verify(jobLauncher, times(2)).run(eq(youtubeValidationJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("Job 실행 중 예외 발생 시 로깅하고 정상 종료")
  void shouldHandleExceptionDuringJobExecution() throws Exception {
    when(jobLauncher.run(eq(youtubeValidationJob), any(JobParameters.class)))
        .thenThrow(new RuntimeException("Job execution failed"));

    scheduler.runYouTubeValidation();

    verify(jobLauncher, times(2)).run(eq(youtubeValidationJob), any(JobParameters.class));
  }

  @Test
  @DisplayName("매번 새로운 JobParameters로 Job 실행")
  void shouldRunJobWithNewParametersEachTime() throws Exception {
    when(jobExecution.getId()).thenReturn(1L);
    when(jobLauncher.run(eq(youtubeValidationJob), any(JobParameters.class)))
        .thenReturn(jobExecution);

    scheduler.runYouTubeValidation();
    scheduler.runYouTubeValidation();

    verify(jobLauncher, times(4)).run(eq(youtubeValidationJob), any(JobParameters.class));
  }
}
