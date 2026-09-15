package examples;

public class AdaptWithVThread {
  /**
   * 가상 스레드는 본질적으로 Thread 클래스의 인스턴스...
   * 따라서 취소 역시 Thread#interrupt 메서드를 호출해서 처리할 수 있다.
   * */
  public static void main(String[] args) {
    /* 두 메서드의 콘솔 출력 결과는 동일 */
    platformThreadInterruption();
    virtualThreadInterruption();
  }

  private static void platformThreadInterruption() {
    var platformThread = Thread.ofPlatform().start(() -> {
      try {
        System.out.println("Platform thread started...");
        for(int i = 0; i < 5; i++) {
          System.out.println("Platform thread working: " + i);
          Thread.sleep(1000);
        }
        System.out.println("Platform thread finished.");
      } catch (InterruptedException e) {
        System.out.println("Platform thread interrupted!");
      }
    });

    try {
      Thread.sleep(2500);
    } catch (InterruptedException ignored) {}

    platformThread.interrupt();
  }

  /**
   * 모든 가상 스레드는 단 하나의 스레드 그룹에 속하며, 다른 스레드 그룹을 가진 가상 스레드를 생성하는 API는 없다.
   * 가상 스레드를 생성하여 `getThreadGroup()`메서드를 호출하면 항상 동일한 ThreadGroup 인스턴스가 반환된다.
   * */
  private static void virtualThreadInterruption() {
    var virtualThread = Thread.ofVirtual().start(() -> {
      try {
        System.out.println("Virtual thread started...");
        for(int i = 0; i < 5; i++) {
          System.out.println("Virtual thread working: " + i);
          Thread.sleep(1000); // 자동으로 제어권 양보
        }

        System.out.println("Virtual thread finished.");
      } catch (InterruptedException e) {
        System.out.println("Virtual thread interrupted!");
      }
    });

    try {
      Thread.sleep(2500);
    } catch (InterruptedException ignored) {}

    virtualThread.interrupt();
  }
}
