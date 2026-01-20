package com.cheftory.api._config;

import com.cheftory.api._common.region.MarketContext;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(destroyMethod = "close")
    public ExecutorService asyncVirtualThreadExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public TaskDecorator marketContextTaskDecorator() {
        return MarketContext::wrap;
    }

    @Bean("recipeCreateExecutor")
    public AsyncTaskExecutor recipeCreateExecutor(
            ExecutorService asyncVirtualThreadExecutorService, TaskDecorator marketContextTaskDecorator) {
        return new AsyncTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                asyncVirtualThreadExecutorService.execute(marketContextTaskDecorator.decorate(task));
            }

            @Override
            public void execute(Runnable task, long startTimeout) {
                asyncVirtualThreadExecutorService.execute(marketContextTaskDecorator.decorate(task));
            }

            @Override
            public Future<?> submit(Runnable task) {
                return asyncVirtualThreadExecutorService.submit(marketContextTaskDecorator.decorate(task));
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return asyncVirtualThreadExecutorService.submit(MarketContext.wrap(task));
            }
        };
    }
}
