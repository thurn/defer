package com.tinlib.defer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Promises {

  private Promises() {}

  @SafeVarargs
  public static Promise<Void> awaitVoid(Promise<Void>... promises) {
    final Deferred<Void> result = Deferreds.newDeferred();
    final AtomicInteger numOutstanding = new AtomicInteger(promises.length);
    final List<RuntimeException> exceptions = Lists.newArrayList();
    for (Promise<Void> promise : promises) {
      promise.addFailureHandler(new FailureHandler() {
        @Override
        public void onError(RuntimeException exception) {
          exceptions.add(exception);
        }
      });
      promise.addCompletionHandler(new Runnable() {
        @Override
        public void run() {
          if (numOutstanding.decrementAndGet() == 0) {
            if (exceptions.size() == 0) {
              result.resolve();
            } else {
              result.fail(new RuntimeException("Errors ocurred in inputs to awaitVoid: "
                  + exceptions));
            }
          }
        }
      });
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <A,B> Promise<Pair<A,B>> awaitPair(Promise<A> promise1,
      Promise<B> promise2) {
    return awaitAll(promise1, promise2).then(new Function<List<Object>, Promise<Pair<A, B>>>() {
      @Override
      public Promise<Pair<A, B>> apply(List<Object> objects) {
        return Deferreds.newResolvedDeferred(new Pair<>((A)objects.get(0), (B)objects.get(1)));
      }
    });
  }

  @SuppressWarnings("unchecked")
  public static Promise<List<Object>> awaitAll(Promise<?>... promises) {
    final Deferred<List<Object>> awaitAll = Deferreds.newDeferred();
    final List<Object> values = Lists.newArrayList(Collections.nCopies(promises.length, null));
    final AtomicInteger numOutstanding = new AtomicInteger(promises.length);
    final AtomicBoolean error = new AtomicBoolean(false);
    for (int i = 0; i < promises.length; ++i) {
      Promise<Object> promise = (Promise<Object>)promises[i];
      final int index = i;
      promise.addSuccessHandler(new SuccessHandler<Object>() {
        @Override
        public void onSuccess(Object value) {
          values.set(index, value);
        }
      });
      promise.addFailureHandler(new FailureHandler() {
        @Override
        public void onError(RuntimeException exception) {
          error.set(true);
          values.set(index, exception);
        }
      });
      promise.addCompletionHandler(new Runnable() {
        @Override
        public void run() {
          if (numOutstanding.decrementAndGet() == 0) {
            if (error.get()) {
              awaitAll.fail(new RuntimeException(
                  "Error in a promise passed to awaitAll. Values: " + values));
            } else {
              awaitAll.resolve(Lists.newArrayList(values));
            }
          }
        }
      });
    }
    return awaitAll;
  }
}
