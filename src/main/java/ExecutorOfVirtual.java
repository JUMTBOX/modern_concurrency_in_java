import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ExecutorOfVirtual {

  /**
   * `Executors.newCachedThreadPool()`, `Executors.newFixedThreadPool()`로
   * 다수의 플랫폼 스레드를 만들어 사용하면 동시성 처리 능력이 급격히 감소한다.
   * 200개의 플랫폼 스레드를 사용하면 많은 태스크는 실질적으로는 순차적으로 처리 될 수 있으며
   * 모든 태스크를 완료하는 데 훨씬 많은 시간이 걸린다.
   * */
  public static void main(String[] args) {
    try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      IntStream.range(0, 10_000).forEach(i -> {
        executor.submit(() -> {
          System.out.println("Virtual Thread No." + Thread.currentThread().threadId());
          Thread.sleep(Duration.ofSeconds(1));
          return i;
        });
      });
    }
  }
}
