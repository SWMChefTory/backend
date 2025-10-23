package com.cheftory.api.recipeinfo.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecipeValidationScheduler {

  private final JobLauncher jobLauncher;
  private final Job youtubeValidationJob;

  @Scheduled(cron = "0 0 2 * * *")
  public void runYouTubeValidation() {
    try {
      JobParameters jobParameters =
          new JobParametersBuilder()
              .addLong("timestamp", System.currentTimeMillis())
              .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(youtubeValidationJob, jobParameters);
      log.info("YouTube validation job started with execution id: {}", jobExecution.getId());
    } catch (Exception e) {
      log.error("Failed to start YouTube validation job", e);
    }
  }
}
