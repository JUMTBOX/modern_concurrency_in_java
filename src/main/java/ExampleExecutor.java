import utils.ExecutionTimer;

public class ExampleExecutor {

  public static void main(String[] args) throws Exception {
    var ex1 = new CreditCalculatorService();

    System.out.println("=== Sequential Execution ===");
    ExecutionTimer.measure(()-> {
      try {
        return ex1.calculateCredit(1L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    });

    System.out.println("=== Parallel Execution ===");
    ExecutionTimer.measure(()-> {
      try {
        return ex1.calculateCreditWithUnboundedThreads(1L);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    });
  }
}
