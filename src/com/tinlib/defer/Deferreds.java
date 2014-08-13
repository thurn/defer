package com.tinlib.defer;

public class Deferreds {
  private Deferreds() {}

  public static <V> Deferred<V> newDeferred() {
    return new DeferredImpl<>();
  }

  public static <V> Deferred<V> newResolvedDeferred(V value) {
    DeferredImpl<V> result = new DeferredImpl<>();
    result.resolve(value);
    return result;
  }

  public static <V> Deferred<V> newFailedDeferred(RuntimeException exception) {
    DeferredImpl<V> result = new DeferredImpl<>();
    result.fail(exception);
    return result;
  }

  public static void failAll(RuntimeException exception, Deferred<?>... deferreds) {
    for (Deferred<?> deferred : deferreds) {
      deferred.fail(exception);
    }
  }

}
