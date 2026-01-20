package com.cheftory.api._common.region;

import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestRegionService {

    private static final Logger log = LoggerFactory.getLogger(TestRegionService.class);
    private final TestRegionEntityRepository repo;
    private final AsyncTaskExecutor taskExecutor;

    public TestRegionService(
            TestRegionEntityRepository repo, @Qualifier("recipeCreateExecutor") AsyncTaskExecutor taskExecutor) {
        this.repo = repo;
        this.taskExecutor = taskExecutor;
    }

    public void save(String name) {
        repo.save(TestRegionEntity.of(name));
    }

    @Async("recipeCreateExecutor")
    public CompletableFuture<Void> createAsync(String prefix) {

        repo.saveAndFlush(TestRegionEntity.of(prefix + "-start"));

        CompletableFuture<Void> f1 =
                CompletableFuture.runAsync(() -> repo.saveAndFlush(TestRegionEntity.of(prefix + "-a")), taskExecutor);

        CompletableFuture<Void> f2 =
                CompletableFuture.runAsync(() -> repo.saveAndFlush(TestRegionEntity.of(prefix + "-b")), taskExecutor);

        CompletableFuture<Void> f3 =
                CompletableFuture.runAsync(() -> repo.findAll().size(), taskExecutor);

        CompletableFuture.allOf(f1, f2, f3).join();
        return CompletableFuture.completedFuture(null);
    }

    public List<String> findAllNames() {
        return repo.findAll().stream().map(TestRegionEntity::getName).toList();
    }
}
