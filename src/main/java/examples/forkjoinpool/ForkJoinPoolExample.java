package examples.forkjoinpool;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;

public class ForkJoinPoolExample {

  public static void main(String[] args) {
    try(var pool = new ForkJoinPool()) {
      var numbers = LongStream.rangeClosed(1, 1_000_000).toArray();

      var result = pool.invoke(
          new RecursiveTaskExample(numbers, 0, numbers.length)
      );

      System.out.println("RESULT >>> " + result);
    }
  }
}
