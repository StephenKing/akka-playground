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
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.WriteConsistency;
import akka.cluster.ddata.Replicator.WriteMajority;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.math.BigInt;
import sun.misc.GC;

public class NumberProducer extends UntypedActor {

  public static final String WRITE = "write!";
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  Cluster cluster = Cluster.get(getContext().system());
  final ActorRef replicator = DistributedData.get(getContext().system()).replicator();
  static Key<LWWMap<Integer, Integer>> ImsiDataKey = LWWMapKey.create("imsi-to-endpoint");
  private final Key<GCounter> cDataKey = GCounterKey.create("c");
  final WriteConsistency writeMajority = new WriteMajority(Duration.create(5, SECONDS));

  @Override public void preStart() throws Exception {
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
        ClusterEvent.ClusterDomainEvent.class);
  }

  @Override public void postStop() throws Exception {
    cluster.unsubscribe(getSelf());
  }

  private void onDoWrite() {

    int i = 3;

    log.info("Saving {}=42 in {}", i, ImsiDataKey);

    Update<LWWMap<Integer, Integer>> u = new Update<>(ImsiDataKey, LWWMap.create(),
        writeMajority, curr -> curr.put(cluster, i, 42));
    replicator.tell(u, getSelf());

    log.info("Incrementing counter c by 3");
    Update<GCounter> u2 = new Update<>(cDataKey, GCounter.create(), writeMajority, curr -> curr.increment(cluster, 3L));
    replicator.tell(u2, getSelf());

    try {
      SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  @Override public void onReceive(Object message) throws Throwable {
    log.info("Received message: {}", message);
    if (NumberActors.START.equals(message)) {
      onStart();
    } else if (WRITE.equals(message)) {
      onDoWrite();
    }
  }

  private void onStart() {
    context().system().scheduler().schedule(
        Duration.Zero(),
        Duration.create(15, SECONDS),
        self(),
        WRITE,
        context().system().dispatcher(),
        getSelf()
    );
  }
}
