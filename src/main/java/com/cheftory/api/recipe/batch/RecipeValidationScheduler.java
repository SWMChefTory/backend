package com.cheftory.api.recipe.batch;

import com.cheftory.api._common.region.Market;
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
    for (Market market : Market.values()) {
      runForMarket(market);
    }
  }

  private void runForMarket(Market market) {
    try {
      JobParameters jobParameters =
          new JobParametersBuilder()
              .addLong("timestamp", System.currentTimeMillis())
              .addString("market", market.name())
              .addString("runId", market + "-" + System.currentTimeMillis())
              .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(youtubeValidationJob, jobParameters);
      log.info(
          "YouTube validation job started. market={}, executionId={}",
          market,
          jobExecution.getId());
    } catch (Exception e) {
      log.error("Failed to start YouTube validation job. market={}", market, e);
    }
  }
}
