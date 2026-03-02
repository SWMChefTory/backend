package com.cheftory.api.search.indexing.scheduler;

import com.cheftory.api._common.Clock;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchIndexingScheduler {

    private final JobOperator jobOperator;
    private final Clock clock;
    private final Job autocompleteIndexJob;
    private final Job searchQueryUpsertIndexJob;
    private final Job searchQueryDeleteIndexJob;

    public SearchIndexingScheduler(
            JobOperator jobOperator,
            Clock clock,
            @Qualifier("autocompleteIndexJob") Job autocompleteIndexJob,
            @Qualifier("searchQueryUpsertIndexJob") Job searchQueryUpsertIndexJob,
            @Qualifier("searchQueryDeleteIndexJob") Job searchQueryDeleteIndexJob) {
        this.jobOperator = jobOperator;
        this.clock = clock;
        this.autocompleteIndexJob = autocompleteIndexJob;
        this.searchQueryUpsertIndexJob = searchQueryUpsertIndexJob;
        this.searchQueryDeleteIndexJob = searchQueryDeleteIndexJob;
    }

    @Scheduled(cron = "${search.indexing.cron.autocomplete}", zone = "${search.indexing.timezone:Asia/Seoul}")
    @SchedulerLock(name = "search-indexing-autocomplete", lockAtMostFor = "PT2M", lockAtLeastFor = "PT5S")
    public void runAutocompleteIndexJob() {
        start("autocompleteIndexJob", autocompleteIndexJob);
    }

    @Scheduled(cron = "${search.indexing.cron.search-query-upsert}", zone = "${search.indexing.timezone:Asia/Seoul}")
    @SchedulerLock(name = "search-indexing-query-upsert", lockAtMostFor = "PT1M", lockAtLeastFor = "PT3S")
    public void runSearchQueryUpsertIndexJob() {
        start("searchQueryUpsertIndexJob", searchQueryUpsertIndexJob);
    }

    @Scheduled(cron = "${search.indexing.cron.search-query-delete}", zone = "${search.indexing.timezone:Asia/Seoul}")
    @SchedulerLock(name = "search-indexing-query-delete", lockAtMostFor = "PT1M", lockAtLeastFor = "PT3S")
    public void runSearchQueryDeleteIndexJob() {
        start("searchQueryDeleteIndexJob", searchQueryDeleteIndexJob);
    }

    private void start(String jobName, Job job) {
        try {
            long nowMillis = clock.nowMillis();
            String upperBoundUpdatedAt = clock.now().toString();
            JobParametersBuilder builder = new JobParametersBuilder()
                    .addLong("timestamp", nowMillis)
                    .addString("runId", jobName + "-" + nowMillis)
                    .addString("upperBoundUpdatedAt", upperBoundUpdatedAt);

            JobParameters jobParameters = builder.toJobParameters();

            JobExecution execution = jobOperator.start(job, jobParameters);
            log.info("{} started. executionId={}", jobName, execution.getId());
        } catch (Exception e) {
            log.error("{} failed to start", jobName, e);
        }
    }
}
