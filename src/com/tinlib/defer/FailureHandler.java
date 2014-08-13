package com.tinlib.defer;

public interface FailureHandler {
  public void onError(RuntimeException exception);
}
