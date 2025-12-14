package com.cheftory.api._common.region;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskDecorator;

@DisplayName("MarketContext TaskDecorator Tests")
class MarketContextTaskDecoratorTest {

  private final TaskDecorator decorator =
      runnable -> {
        var captured = MarketContext.currentOrNull();
        return () -> {
          if (captured == null) {
            runnable.run();
            return;
          }
          try (var ignored = MarketContext.with(captured)) {
            runnable.run();
          }
        };
      };

  @Nested
  @DisplayName("decorate")
  class Decorate {

    @Test
    @DisplayName("컨텍스트가 있으면 다른 스레드로 전파되어야 한다")
    void shouldPropagate() throws Exception {
      var latch = new CountDownLatch(1);
      var capturedInOtherThread = new AtomicReference<MarketContext.Info>();

      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        Runnable original =
            () -> {
              try {
                capturedInOtherThread.set(MarketContext.required());
              } finally {
                latch.countDown();
              }
            };

        Thread t = new Thread(decorator.decorate(original));
        t.start();
        t.join();
        latch.await();
      }

      assertThat(capturedInOtherThread.get()).isNotNull();
      assertThat(capturedInOtherThread.get().market()).isEqualTo(Market.KOREA);
      assertThat(capturedInOtherThread.get().countryCode()).isEqualTo("KR");
    }

    @Test
    @DisplayName("컨텍스트가 없으면 다른 스레드에서도 null이어야 한다")
    void shouldNotPropagateWhenAbsent() throws Exception {
      var latch = new CountDownLatch(1);
      var capturedInOtherThread = new AtomicReference<MarketContext.Info>();

      Runnable original =
          () -> {
            capturedInOtherThread.set(MarketContext.currentOrNull());
            latch.countDown();
          };

      Thread t = new Thread(decorator.decorate(original));
      t.start();
      t.join();
      latch.await();

      assertThat(capturedInOtherThread.get()).isNull();
    }

    @Test
    @DisplayName("예외가 발생해도 대상 스레드에서 컨텍스트가 정리되어야 한다")
    void shouldClearInDecoratedThreadEvenOnException() throws Exception {
      var latch = new CountDownLatch(1);
      var after = new AtomicReference<MarketContext.Info>();

      try (var ignored = MarketContext.with(new MarketContext.Info(Market.KOREA, "KR"))) {
        Runnable original =
            () -> {
              try {
                throw new RuntimeException("boom");
              } finally {
                latch.countDown();
              }
            };

        Runnable decorated =
            () -> {
              try {
                decorator.decorate(original).run();
              } finally {
                after.set(MarketContext.currentOrNull());
              }
            };

        Thread t = new Thread(decorated);
        t.start();
        t.join();
        latch.await();
      }

      assertThat(after.get()).isNull();
    }
  }
}
