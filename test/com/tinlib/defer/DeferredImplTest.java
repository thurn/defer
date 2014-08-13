package com.tinlib.defer;

import com.google.common.base.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DeferredImplTest {
  @Test
  public void testResolve() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> deferred = Deferreds.newDeferred();
    deferred.addSuccessHandler(new SuccessHandler<String>() {
      @Override
      public void onSuccess(String value) {
        assertEquals("value", value);
        ran.set(true);
      }
    });
    deferred.resolve("value");
    assertTrue(ran.get());
  }

  @Test
  public void testFail() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> deferred = Deferreds.newDeferred();
    final RuntimeException toThrow = new RuntimeException();
    deferred.addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        assertEquals(toThrow, exception);
        ran.set(true);
      }
    });
    deferred.fail(toThrow);
    assertTrue(ran.get());
  }

  @Test
  public void testResolveVoid() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    deferred.addSuccessHandler(new SuccessHandler<Void>() {
      @Override
      public void onSuccess(Void value) {
        assertNull(value);
        ran.set(true);
      }
    });
    deferred.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testCompletionHandler() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    deferred.addCompletionHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    deferred.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testAddAfterResolve() {
    final AtomicBoolean successRan = new AtomicBoolean(false);
    final AtomicBoolean completeRan = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    deferred.resolve();
    deferred.addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        successRan.set(true);
      }
    });
    deferred.addCompletionHandler(new Runnable() {
      @Override
      public void run() {
        completeRan.set(true);
      }
    });
    assertTrue(successRan.get());
    assertTrue(completeRan.get());
  }

  @Test
  public void testAddAfterFail() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    deferred.fail(new RuntimeException());
    deferred.addFailureHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    assertTrue(ran.get());
  }

  @Test
  public void testGetState() {
    Deferred<Void> deferred = Deferreds.newDeferred();
    assertEquals(Promise.State.PENDING, deferred.getState());
    deferred.resolve();
    assertEquals(Promise.State.RESOLVED, deferred.getState());
    Deferred<Void> deferred2 = Deferreds.newDeferred();
    deferred2.fail(new RuntimeException());
    assertEquals(Promise.State.FAILED, deferred2.getState());
  }

  @Test
  public void testThenFunction() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<String> deferred = Deferreds.newDeferred();
    final Deferred<String> two = Deferreds.newDeferred();
    Promise<String> result = deferred.then(new Function<String, Promise<String>>() {
      @Override
      public Promise<String> apply(String s) {
        assertEquals("one", s);
        return two;
      }
    });
    result.addSuccessHandler(new SuccessHandler<String>() {
      @Override
      public void onSuccess(String value) {
        assertEquals("two", value);
        ran.set(true);
      }
    });
    deferred.resolve("one");
    two.resolve("two");
    assertTrue(ran.get());
  }

  @Test
  public void testThenCallableFail() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    final Deferred<String> two = Deferreds.newDeferred();
    Promise<String> result = deferred.then(new Callable<Promise<String>>() {
      @Override
      public Promise<String> call() throws Exception {
        return two;
      }
    });
    result.addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        ran.set(true);
      }
    });
    deferred.fail(new RuntimeException());
    assertTrue(ran.get());
  }

  @Test
  public void testThenRunnable() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    Promise<Void> result = deferred.then(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    result.addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    deferred.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testChainFrom() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> parent = Deferreds.newDeferred();
    Deferred<Void> child = Deferreds.newDeferred();
    child.chainFrom(parent);
    child.addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    parent.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testFailedCompletionHandler() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> deferred = Deferreds.newDeferred();
    deferred.addCompletionHandler(new Runnable() {
      @Override
      public void run() {
        ran.set(true);
      }
    });
    deferred.fail(new RuntimeException());
    assertTrue(ran.get());
  }

  @Test
  public void testThenCallableSuccess() {
    final AtomicBoolean ran = new AtomicBoolean(false);
    Deferred<Void> one = Deferreds.newDeferred();
    final Deferred<String> two = Deferreds.newDeferred();
    Promise<String> result = one.then(new Callable<Promise<String>>() {
      @Override
      public Promise<String> call() throws Exception {
        return two;
      }
    });
    result.addSuccessHandler(new SuccessHandler<String>() {
      @Override
      public void onSuccess(String value) {
        assertEquals("foo", value);
        ran.set(true);
      }
    });
    two.resolve("foo");
    one.resolve();
    assertTrue(ran.get());
  }

  @Test
  public void testNoExceptionsAddHandlers() {
    Deferred<Void> one = Deferreds.newDeferred();
    one.resolve();
    one.addFailureHandler(new FailureHandler() {
      @Override
      public void onError(RuntimeException exception) {
        fail("Failure handler should not be invoked.");
      }
    });

    Deferred<Void> two = Deferreds.newDeferred();
    two.fail(new RuntimeException());
    two.addSuccessHandler(new Runnable() {
      @Override
      public void run() {
        fail("Success handler should not be invoked");
      }
    });
  }
}
