package com.tinlib.defer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.Callable;

class DeferredImpl<V> implements Deferred<V> {
  private Promise.State state = State.PENDING;
  private Optional<V> value;
  private RuntimeException exception;
  private final List<SuccessHandler<V>> successHandlers = Lists.newArrayList();
  private final List<FailureHandler> failureHandlers = Lists.newArrayList();
  private final List<Runnable> completionHandlers = Lists.newArrayList();

  @Override
  public synchronized void resolve() {
    resolveOptional(Optional.<V>absent());
  }

  public synchronized void resolve(V value) {
    resolveOptional(Optional.of(value));
  }

  private void resolveOptional(Optional<V> value) {
    Preconditions.checkArgument(this.state == State.PENDING);

    this.value = value;
    for (SuccessHandler<V> successHandler : successHandlers) {
      successHandler.onSuccess(value.orNull());
    }
    for (Runnable completionHandler : completionHandlers) {
      completionHandler.run();
    }
    this.state = State.RESOLVED;
  }

  @Override
  public synchronized void fail(RuntimeException exception) {
    Preconditions.checkArgument(this.state == State.PENDING);

    this.exception = exception;
    for (FailureHandler failureHandler : failureHandlers) {
      failureHandler.onError(exception);
    }
    for (Runnable completionHandler : completionHandlers) {
      completionHandler.run();
    }
    this.state = State.FAILED;
  }

  @Override
  public synchronized void addSuccessHandler(SuccessHandler<V> handler) {
    switch (state) {
      case PENDING:
        successHandlers.add(handler);
        break;
      case RESOLVED:
        handler.onSuccess(value.orNull());
        break;
      case FAILED:
        // Do nothing
        break;
    }
  }

  @Override
  public void addSuccessHandler(final Runnable successHandler) {
    addSuccessHandler(new SuccessHandler<V>() {
      @Override
      public void onSuccess(V value) {
        successHandler.run();
      }
    });
  }

  @Override
  public synchronized void addFailureHandler(FailureHandler failureHandler) {
    switch (state) {
      case PENDING:
        failureHandlers.add(failureHandler);
        break;
      case RESOLVED:
        // Do nothing
        break;
      case FAILED:
        failureHandler.onError(exception);
        break;
    }
  }

  @Override
  public void addFailureHandler(final Runnable runnable) {
    addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        runnable.run();
      }
    });
  }

  @Override
  public synchronized void addCompletionHandler(Runnable onComplete) {
    if (state == State.PENDING) {
      completionHandlers.add(onComplete);
    } else {
      onComplete.run();
    }
  }

  @Override
  public synchronized State getState() {
    return state;
  }

  @Override
  public <K> Promise<K> then(final Function<V, Promise<K>> function) {
    final DeferredImpl<K> result = new DeferredImpl<>();
    addSuccessHandler(new SuccessHandler<V>() {
      @Override
      public void onSuccess(V value) {
        Preconditions.checkNotNull(value);
        result.chainFrom(function.apply(value));
      }
    });
    addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        result.fail(exception);
      }
    });
    return result;
  }

  @Override
  public <K> Promise<K> then(final Callable<Promise<K>> function) {
    final DeferredImpl<K> result = new DeferredImpl<>();
    addSuccessHandler(new SuccessHandler<V>() {
      @Override
      public void onSuccess(V value) {
        try {
          result.chainFrom(function.call());
        } catch (Exception exception) {
          throw new RuntimeException(exception);
        }
      }
    });
    addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        result.fail(exception);
      }
    });
    return result;
  }

  @Override
  public Promise<Void> then(final Runnable runnable) {
    final DeferredImpl<Void> result = new DeferredImpl<>();
    addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        runnable.run();
        result.resolve();
      }
    });
    addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        result.fail(exception);
      }
    });
    return result;
  }

  @Override
  public void chainFrom(Promise<V> promise) {
    promise.addSuccessHandler(new SuccessHandler<V>() {
      @Override
      public void onSuccess(V value) {
        resolveOptional(Optional.fromNullable(value));
      }
    });
    promise.addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        fail(exception);
      }
    });
  }

}
