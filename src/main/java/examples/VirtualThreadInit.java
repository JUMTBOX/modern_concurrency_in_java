package examples;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadInit {

  public static void main(String[] args) throws Exception {
    /*
     * `new Thread()`로 만든 스레드는 기본적으로 부모 스레드의 daemon 여부를 물려받아
     * 여기서는 부모 스레드인 main 스레드를 따라 non-daemon으로 생성된다.
     * `Thread#setDaemon`메서드를 통해 daemon 여부를 설정할 수 있다.
     * */
    var thread = new Thread(() -> {
      System.out.println("this thread has 'non-daemon' attribute inherited from parent thread");
      System.out.println("Thread Name >>> " + Thread.currentThread().getName()); // Thread Name >>> Thread-0
    });
    thread.start();

    /* 가상 스레드는 부모와 무관하게 항상 daemon이며 변경 불가하다. */
    var vThread = Thread.startVirtualThread(() -> {
      System.out.println("Unleash massive parallelism with virtual thread! Here's a taste.");
      System.out.println("Thread Name >>> " + Thread.currentThread().getName());  // Thread Name >>> ${empty}
    });
    vThread.join();

    threadBuilderAPI();

    usingVThreadBiaExecutorService();
  }

  private static void threadBuilderAPI() throws Exception {
    var startedThread = Thread.ofVirtual()
        .start(() -> System.out.println("Hello World! from `threadBuilderAPI`"));
    startedThread.join();
  }

  private static void usingVThreadBiaExecutorService() throws Exception {
    try(var virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> future = virtualExecutor.submit(
          () -> {
            System.out.println("Hello World! from `usingVThreadBiaExecutorService`");
            return "returnValue";
          }
      );
      var returnValue = future.get();
      System.out.println("result >>> " + returnValue);
    }
  }
}
