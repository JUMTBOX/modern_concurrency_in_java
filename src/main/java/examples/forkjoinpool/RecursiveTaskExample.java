package examples.forkjoinpool;

import java.util.concurrent.RecursiveTask;

public class RecursiveTaskExample extends RecursiveTask<Long> {

  private static final int THRESHOLD = 10_000;

  private final long[] numbers;
  private final int start;
  private final int end;

  RecursiveTaskExample(
      long[] numbers,
      int start,
      int end
  ) {
    this.numbers = numbers;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Long compute() {
    int length = end - start;

    if(length <= THRESHOLD) {
      long sum = 0;
      for(var i = start; i < end; i++) {
        sum += numbers[i];
      }
      return sum;
    }

    int mid = start + length / 2;

    var leftTask = new RecursiveTaskExample(numbers, start, mid);
    var rightTask = new RecursiveTaskExample(numbers, mid, end);

    leftTask.fork();

    long rightResult = rightTask.compute();
    long leftResult = leftTask.join();

    return leftResult + rightResult;
  }
}
