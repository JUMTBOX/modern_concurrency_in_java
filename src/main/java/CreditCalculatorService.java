import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Mono;
import records.Asset;
import records.Credit;
import records.Liability;
import records.Person;
import static java.util.concurrent.CompletableFuture.*;


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

  /**
   * 1. 독립적인 작업을 비동기로 시작
   * 2. `importantWork()` 수행이 완료된 후에 person 데이터를 비동기로 가져온다.
   * 3. person 데이터를 사용해서 `getAssets()`와 `getLiabilities()`를 비동기로 병렬 실행한다.
   * 4. assets 데이터와 liabilities 데이터를 사용해서 신용 점수를 계산한다.
   * 5. 스레드 실행을 막고 신용 점수 계산 결과를 기다린 후 완료되면 반환한다.
   * */
  public Credit calculateCreditWithCompletableFuture(Long personId) throws ExecutionException, InterruptedException {
    return runAsync(this::importantWork)
        .thenCompose(aVoid -> supplyAsync(() -> getPerson(personId)))
        .thenCompose(person -> supplyAsync(() -> getAssets(person))
            .thenCombineAsync(
                supplyAsync(() -> getLiabilities(person)),
                this::calculateCredits
            )
        )
        .get();
  }

  /**
   * 1. `importantWork()`를 비동기로 실행하는 Mono 객체를 생성한다.
   * 2. 리액티브 스트림 안에서 person 데이터 조회 로직을 감싼다.
   * 3. 비동기 assets 데이터 조회 로직을 적용해서 person 스트림을 assets 스트림으로 변환한다.
   * 4. 비동기 liabilities 데이터 조회 로직을 적용해서 person 스트림을 liabilities 스트림으로 변환한다.
   * 5. `importantWork()`가 완료된 후에 신용 점수 계산 작업을 시작한다.
   * 6. assets 스트림과 liabilities 스트림을 하나의 스트림으로 결합한다.
   * 7. 결합된 스트림에서 assets 데이터와 liabilities 데이터를 획득해서 최종 신용 점수를 계산한다.
   * */
  public Mono<Credit> calculateCreditReactive(Long personId) {
    Mono<Void> importantWorkMono = Mono.fromRunnable(this::importantWork);
    Mono<Person> personMono = Mono.fromSupplier(() -> getPerson(personId));

    Mono<List<Asset>> assetsMono = personMono.map(this::getAssets);
    Mono<List<Liability>> liabilitiesMono = personMono.map(this::getLiabilities);

    return importantWorkMono.then(
        Mono.zip(assetsMono, liabilitiesMono)
            .map(tuple -> {
              var assets = tuple.getT1();
              var liabilities = tuple.getT2();
              return calculateCredits(assets, liabilities);
            })
    );
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