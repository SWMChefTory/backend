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

/**
 * 레시피 검증 스케줄러
 *
 * <p>주기적으로 YouTube 레시피 유효성을 검증하는 배치 작업을 실행합니다.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecipeValidationScheduler {

    private final JobLauncher jobLauncher;
    private final Job youtubeValidationJob;

    /**
     * YouTube 검증 배치 작업 실행
     *
     * <p>매일 새벽 2시에 모든 마켓에 대해 YouTube 검증 작업을 실행합니다.</p>
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runYouTubeValidation() {
        for (Market market : Market.values()) {
            runForMarket(market);
        }
    }

    /**
     * 특정 마켓에 대해 검증 작업 실행
     *
     * @param market 마켓
     */
    private void runForMarket(Market market) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("market", market.name())
                    .addString("runId", market + "-" + System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(youtubeValidationJob, jobParameters);
            log.info("YouTube validation job started. market={}, executionId={}", market, jobExecution.getId());
        } catch (Exception e) {
            log.error("Failed to start YouTube validation job. market={}", market, e);
        }
    }
}
