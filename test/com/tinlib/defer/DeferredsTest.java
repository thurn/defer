package com.tinlib.defer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DeferredsTest {
  @Test
  public void testNewResolvedDeferred() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferreds.newResolvedDeferred("string").addSuccessHandler(new SuccessHandler<String>() {
      @Override
      public void onSuccess(String value) {
        assertEquals("string", value);
        ran.set(true);
      }
    });
    assertTrue(ran.get());
  }

  @Test
  public void testNewFailedDeferred() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    final RuntimeException exception = new RuntimeException();
    Deferreds.newFailedDeferred(exception).addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException value) {
        assertEquals(exception, value);
        ran.set(true);
      }
    });
    assertTrue(ran.get());
  }

  @Test
  public void testFailAll() {
    final AtomicBoolean oneRan = new AtomicBoolean(false);
    final AtomicBoolean twoRan = new AtomicBoolean(false);
    Deferred<Void> one = Deferreds.newDeferred();
    Deferred<Void> two = Deferreds.newDeferred();
    one.addFailureHandler(new Runnable() {
      @Override
      public void run() {
        oneRan.set(true);
      }
    });
    Deferreds.failAll(new RuntimeException(), one, two);
    two.addFailureHandler(new Runnable() {
      @Override
      public void run() {
        twoRan.set(true);
      }
    });
    assertTrue(oneRan.get());
    assertTrue(twoRan.get());
  }
}
