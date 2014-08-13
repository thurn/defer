package com.tinlib.defer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PromisesTest {
  @Test
  public void testAwaitVoid() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> one = Deferreds.newDeferred();
    Deferred<Void> two = Deferreds.newDeferred();
    Promise<Void> result = Promises.awaitVoid(one, two);
    result.addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    one.resolve();
    assertFalse(ran.get());
    two.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testAwaitVoidFailure() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> one = Deferreds.newDeferred();
    Deferred<Void> two = Deferreds.newDeferred();
    Promise<Void> result = Promises.awaitVoid(one, two);
    result.addFailureHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    one.resolve();
    assertFalse(ran.get());
    two.fail(new RuntimeException());
    assertTrue(ran.get());
  }

  @Test
  public void testAwaitPair() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> one = Deferreds.newDeferred();
    Deferred<String> two = Deferreds.newDeferred();
    Promise<Pair<String, String>> result = Promises.awaitPair(one, two);
    result.addSuccessHandler(new SuccessHandler<Pair<String, String>>() {
      @Override
      public void onSuccess(Pair<String, String> pair) {
        assertEquals("one", pair.getFirst());
        assertEquals("two", pair.getSecond());
        ran.set(true);
      }
    });
    assertFalse(ran.get());
    one.resolve("one");
    two.resolve("two");
    assertTrue(ran.get());
  }

  @Test
  public void testAwaitAll() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> one = Deferreds.newDeferred();
    Deferred<String> two = Deferreds.newDeferred();
    Promise<List<Object>> result = Promises.awaitAll(one, two);
    result.addSuccessHandler(new SuccessHandler<List<Object>>() {
      @Override
      public void onSuccess(List<Object> list) {
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        ran.set(true);
      }
    });
    one.resolve("one");
    assertFalse(ran.get());
    two.resolve("two");
    assertTrue(ran.get());
  }

  @Test
  public void testAwaitAllFailure() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> one = Deferreds.newDeferred();
    Deferred<String> two = Deferreds.newDeferred();
    Promise<List<Object>> result = Promises.awaitAll(one, two);
    result.addFailureHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    one.fail(new RuntimeException());
    assertFalse(ran.get());
    two.resolve("two");
    assertTrue(ran.get());
  }
}
