package de.st_g.akka_playground.number_actors;

import static java.util.concurrent.TimeUnit.SECONDS;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.GCounterKey;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.LWWMapKey;
import akka.cluster.ddata.Replicator;
import akka.cluster.ddata.Replicator.Get;
import akka.cluster.ddata.Replicator.GetSuccess;
import akka.cluster.ddata.Replicator.ReadAll;
import akka.cluster.ddata.Replicator.ReadConsistency;
import akka.cluster.ddata.Replicator.ReadMajority;
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.UpdateSuccess;
import akka.cluster.ddata.Replicator.WriteAll;
import akka.cluster.ddata.Replicator.WriteConsistency;
import akka.cluster.ddata.Replicator.WriteMajority;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;

public class DDTestActor extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  Cluster cluster = Cluster.get(getContext().system());
  final ActorRef replicator = DistributedData.get(getContext().system()).replicator();

  static Key<LWWMap<String, String>> ImsiDataKey = LWWMapKey.create("imsi-to-endpoint");
  static LWWMap<String, String> map = LWWMap.empty();
  private final Key<GCounter> cDataKey = GCounterKey.create("c");
  final WriteConsistency writeMajority = new WriteMajority(Duration.create(50, SECONDS));
  final WriteConsistency writeAll = new WriteAll(Duration.create(50, SECONDS));
  final ReadConsistency readMajority = new ReadMajority(Duration.create(50, SECONDS));
  final ReadConsistency readAll = new ReadAll(Duration.create(50, SECONDS));
  private int updateFrom = 70000;
  private int updateTo = 75000;
  private int count = 0;
  private long started = 0;

  Map<Integer, Long> results = new HashMap<>();

  @Override
  public void preStart() throws Exception {
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
        ClusterEvent.ClusterDomainEvent.class);
  }

  @Override
  public void postStop() throws Exception {
    cluster.unsubscribe(getSelf());
  }

  @Override
  public void onReceive(Object message) throws Throwable {
    log.info("Received message: {}", message);
    if (message instanceof String) {
      if (NumberActors.WRITE.equals(message)) {
        write();
      } else if (NumberActors.READ.equals(message)) {
        read();
      }
    } else if (message instanceof GetSuccess) {
      LWWMap<String, String> maap = (LWWMap<String, String>) ((GetSuccess) message).dataValue();
      log.error(maap.get("666") + " ::: " + maap.size());
    } else if (message instanceof UpdateSuccess) {
      if (count == 0) {
        started = System.currentTimeMillis();
      }
      count++;
      if (count % 50 == 0) {
        results.put(count, System.currentTimeMillis() - started);

      }
      if (count == (updateTo - updateFrom)) {
        results.forEach((k, v) -> {
          log.warning(k + " ::: " + v);
        });
      }
    } else {
      unhandled(message);
    }
  }

  private void write() {
    for (int j = updateFrom; j < updateTo; j++) {
      String a = j + "";
      Update<LWWMap<String, String>> u = new Update<>(ImsiDataKey, LWWMap.create(),
          writeMajority,
          curr -> curr.put(cluster, a, "TEST" + a)
      );
      replicator.tell(u, getSelf());
//     if (j % 500 == 0) {
//        try {
//          Thread.sleep(500);
//        } catch (InterruptedException e) {
//          e.printStackTrace();
//        }
//      }
    }
  }

  private void read() {
    replicator.tell(new Get<>(ImsiDataKey, Replicator.readLocal()), getSelf());
  }
}
