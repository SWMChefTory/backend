package com.cheftory.api._common.region;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.GlobalErrorCode;
import java.util.concurrent.Callable;

public final class MarketContext {
  private static final ThreadLocal<Info> THREAD_LOCAL = new ThreadLocal<>();
  private MarketContext() {}

  public record Info(Market market, String countryCode) {}

  public static Info currentOrNull() { return THREAD_LOCAL.get(); }

  public static Info required() {
    Info info = THREAD_LOCAL.get();
    if (info == null) throw new CheftoryException(GlobalErrorCode.UNKNOWN_REGION);
    return info;
  }

  public static Scope with(Info info) {
    Info prev = THREAD_LOCAL.get();
    THREAD_LOCAL.set(info);
    return new Scope(prev);
  }

  public static final class Scope implements AutoCloseable {
    private final Info prev;
    private Scope(Info prev) { this.prev = prev; }
    @Override public void close() {
      if (prev == null) THREAD_LOCAL.remove();
      else THREAD_LOCAL.set(prev);
    }
  }
  public static Runnable wrap(Runnable task) {
    var captured = currentOrNull();
    return () -> {
      if (captured == null) { task.run(); return; }
      try (var ignored = with(captured)) { task.run(); }
    };
  }

  public static <T> Callable<T> wrap(Callable<T> task) {
    var captured = currentOrNull();
    return () -> {
      if (captured == null) return task.call();
      try (var ignored = with(captured)) { return task.call(); }
    };
  }
}