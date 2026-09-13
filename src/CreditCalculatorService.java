import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import records.Asset;
import records.Credit;
import records.Liability;
import records.Person;


public class CreditCalculatorService {

  public Credit calculateCredit(Long personId) throws InterruptedException {
    var person = getPerson(personId);
    var assets = getAssets(person);
    var liabilities = getLiabilities(person);
    importantWork();
    return calculateCredits(assets, liabilities);
  }

  public Credit calculateCreditWithUnboundedThreads (Long personId) throws InterruptedException {

    var person = getPerson(personId);

    var assetRef = new AtomicReference<List<Asset>>();

    var thread1 = new Thread(() -> {
      var assets = getAssets(person);
      assetRef.set(assets);
    });

    var liabilitiesRef = new AtomicReference<List<Liability>>();

    var thread2 = new Thread(() -> {
      var liabilities = getLiabilities(person);
      liabilitiesRef.set(liabilities);
    });

    var thread3 = new Thread(this::importantWork);

    thread1.start();
    thread2.start();
    thread3.start();

    thread1.join();
    thread2.join();

    var credit = calculateCredits(assetRef.get(), liabilitiesRef.get());

    thread3.join();

    return credit;
  }

  /**
   * [ExecutorService]
   * 위의 unbounded 메서드 방식과 성능상으로 유의미한 차이는 없지만 <br/>
   * 스레드 생성 및 관리, 적절한 작업 부하에 대한 실행 속도 상승을 편리하게 해준다.
   * */
  public Credit calculateCreditWithExecutor(Long personId) throws ExecutionException, InterruptedException {
    try(var executor = Executors.newFixedThreadPool(5)) {
     var person = getPerson(personId);

     var assetsFuture = executor.submit(() -> getAssets(person));
     var liabilities = executor.submit(() -> getLiabilities(person));
     executor.submit(this::importantWork);

     return calculateCredits(assetsFuture.get(), liabilities.get());
    }
  }

  private Person getPerson (Long personId) {
    simulateDelay(200);
    return new Person(personId, "John Doe");
  }

  private List<Asset> getAssets(Person person) {
    simulateDelay(200);
    return List.of(
        new Asset("House", 300000),
        new Asset("Car",25000)
    );
  }

  private List<Liability> getLiabilities(Person person) {
    simulateDelay(200);
    return List.of(
        new Liability("Mortgage", 200000),
        new Liability("Credit Card", 5000)
    );
  }

  private void importantWork(){
    simulateDelay(200);
    System.out.println("Important work completed");
  }

  private Credit calculateCredits(List<Asset> assets, List<Liability> liabilities) {
    simulateDelay(200);
    double totalAssets = assets.stream().mapToDouble(Asset::value).sum();
    double totalLiabilities = liabilities.stream().mapToDouble(Liability::amount).sum();

    double creditScore = (totalAssets - totalLiabilities) / 1000;
    return new Credit(creditScore);
  }

  /* 각종 I/O 작업 simulate를 위한 지연 */
  private void simulateDelay(int mills) {
    try {
      Thread.sleep(mills);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}