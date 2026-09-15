package examples;

import java.util.HashSet;
import java.util.Set;

public class VirtualThreadGroupExample {

  /**
   * Thread::getAllStackTraces는 플랫폼 스레드의 스택 트레이스만 반환하고 가상 스레드의 스택 트레이스는 포함하지 않는다.
   * 또한 가상 스레드를 어떤 플랫폼 스레드가 실행하고 있는지 확인할 방법이 현재로서는 없다.
   * */
  public static void main(String[] args) throws InterruptedException {
    Set<ThreadGroup> threadGroups = new HashSet<>();

    for(var i=0; i<100; i++) {
      var vThread = Thread.ofVirtual().start(() -> {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
      threadGroups.add(vThread.getThreadGroup());
    }

    Thread.sleep(1000); // 가상 스레드 실행 종료되도록 대기
    System.out.println("Unique thread groups: " + threadGroups.size());
  }
}
