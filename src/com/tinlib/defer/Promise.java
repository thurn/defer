package com.tinlib.defer;

import com.google.common.base.Function;

import java.util.concurrent.Callable;

public interface Promise<V> {
  public enum State {
    PENDING,
    RESOLVED,
    FAILED
  }

  public void addSuccessHandler(SuccessHandler<V> successHandler);

  public void addSuccessHandler(Runnable runnable);

  public void addFailureHandler(FailureHandler failureHandler);

  public void addFailureHandler(Runnable runnable);

  public void addCompletionHandler(Runnable onComplete);

  public State getState();

  public <K> Promise<K> then(Function<V, Promise<K>> function);

  public <K> Promise<K> then(Callable<Promise<K>> function);

  public Promise<Void> then(Runnable runnable);
}
