package com.tinlib.defer;

public interface SuccessHandler<V> {
  public void onSuccess(V value);
}
