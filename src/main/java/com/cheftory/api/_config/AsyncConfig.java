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

/**
 * 비동기 처리를 위한 설정 클래스.
 *
 * <p>가상 스레드를 사용한 Executor Service와 Task Decorator를 설정합니다.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 가상 스레드 기반 Executor Service 빈을 생성합니다.
     *
     * @return ExecutorService 인스턴스
     */
    @Bean(destroyMethod = "close")
    public ExecutorService asyncVirtualThreadExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 마켓 컨텍스트 래핑 Task Decorator 빈을 생성합니다.
     *
     * @return TaskDecorator 인스턴스
     */
    @Bean
    public TaskDecorator marketContextTaskDecorator() {
        return MarketContext::wrap;
    }

    /**
     * 레시피 생성을 위한 비동기 Task Executor 빈을 생성합니다.
     *
     * @param asyncVirtualThreadExecutorService 가상 스레드 Executor Service
     * @param marketContextTaskDecorator 마켓 컨텍스트 Task Decorator
     * @return AsyncTaskExecutor 인스턴스
     */
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
