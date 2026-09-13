package utils;

import java.util.concurrent.Callable;

public class ExecutionTimer {

  public static <T> T measure(Callable<T> task) throws Exception {
    long startTime = System.nanoTime();
    try {
      return task.call();
    } finally {
      long endTime = System.nanoTime();
      /* 밀리초로 전환 */
      long duration = (endTime - startTime) / 1_000_000;
      System.out.println("Execution Time: " + duration + " milliseconds");
    }
  }
}
