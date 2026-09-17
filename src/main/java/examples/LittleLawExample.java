package examples;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class LittleLawExample {

  /**
   * 실행 결과
   * <pre>{@code
   * === Little's Law Throughput Comparison ===
   * Testing 10000 tasks with 500ms latency each
   * Virtual Threads           - Time:  1215ms, Throughput:  8230.45 tasks/s
   * Fixed ThreadPool (100)    - Time: 53103ms, Throughput:   188.31 tasks/s
   * Fixed ThreadPool (500)    - Time: 10252ms, Throughput:   975.42 tasks/s
   * Fixed ThreadPool (1000)   - Time:  5178ms, Throughput:  1931.25 tasks/s
   * }
   * </pre>
   * */
  public static void main(String[] args) {
    int numTasks = 10000;
    int avgResponseTimeMills = 500; // 평균 응답 시간

    /* 응답 시간을 조절할 수 있는 I/O 집중적인 작업 시뮬레이션 */
    Runnable ioBoundTask = () -> {
      try {
        Thread.sleep(Duration.ofMillis(avgResponseTimeMills));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    };

    System.out.println("=== Little's Law Throughput Comparison ===");
    System.out.println("Testing " + numTasks + " tasks with " + avgResponseTimeMills + "ms latency each");

    benchmark(
        "Virtual Threads",
        Executors.newVirtualThreadPerTaskExecutor(),
        ioBoundTask,
        numTasks
    );
    benchmark(
        "Fixed ThreadPool (100)",
        Executors.newFixedThreadPool(100),
        ioBoundTask,
        numTasks
    );
    benchmark(
        "Fixed ThreadPool (500)",
        Executors.newFixedThreadPool(500),
        ioBoundTask,
        numTasks
    );
    benchmark(
        "Fixed ThreadPool (1000)",
        Executors.newFixedThreadPool(1000),
        ioBoundTask,
        numTasks
    );

  }

  public static void benchmark(String type, ExecutorService executor, Runnable task, int numTasks) {
    var start = Instant.now();
    AtomicLong completedTasks = new AtomicLong();

    try(executor) {
      IntStream.range(0, numTasks)
          .forEach(i -> executor.submit(() -> {
            task.run();
            completedTasks.incrementAndGet();
          }));
    }

    var end = Instant.now();
    var duration = Duration.between(start, end).toMillis();
    /* 초당 처리량 */
    double throughput = (double) completedTasks.get() / duration * 1000;
    System.out.printf(
        "%-25s - Time: %5dms, Throughput: %8.2f tasks/s%n",
        type,
        duration,
        throughput
    );
  }
}
