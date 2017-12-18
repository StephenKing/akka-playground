package de.st_g.akka_playground.number_actors;

import static java.util.concurrent.TimeUnit.SECONDS;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.GCounter;
import akka.cluster.ddata.GCounterKey;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.LWWMap;
import akka.cluster.ddata.Replicator;
import akka.cluster.ddata.Replicator.Changed;
import akka.cluster.ddata.Replicator.GetSuccess;
import akka.cluster.ddata.Replicator.NotFound;
import akka.cluster.ddata.Replicator.ReadConsistency;
import akka.cluster.ddata.Replicator.ReadMajority;
import akka.cluster.ddata.Replicator.Subscribe;
import akka.cluster.ddata.Replicator.Update;
import akka.cluster.ddata.Replicator.WriteMajority;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import sun.misc.GC;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class NumberConsumer extends AbstractActor {

  public static final String READ = "read!";
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  final ActorRef replicator = DistributedData.get(getContext().system()).replicator();
  Cluster cluster = Cluster.get(getContext().system());
  final ReadConsistency readMajority = new ReadMajority(Duration.create(5, TimeUnit.SECONDS));
  private final Key<GCounter> cDataKey = GCounterKey.create("c");

  @Override
  public void preStart() {
    // this is really optional, as we not necessarily need to know about changes (we read, when we need the info)
    log.info("Subscribing to {}", NumberProducer.ImsiDataKey);
    Subscribe<LWWMap<Integer, Integer>> subscribe = new Subscribe<>(NumberProducer.ImsiDataKey, getSelf());
    replicator.tell(subscribe, ActorRef.noSender());

    Subscribe<GCounter> subscribe2 = new Subscribe<>(cDataKey, getSelf());
    replicator.tell(subscribe2, ActorRef.noSender());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(String.class, a -> a.equals(NumberActors.START), a -> onStart())
        .match(String.class, a -> a.equals(READ), a -> onDoRead())
        .match(GetSuccess.class, a -> a.key().equals(NumberProducer.ImsiDataKey), this::onGetSuccess)
        .match(GetSuccess.class, a -> a.key().equals(cDataKey), this::onGetCounterSuccess)
        .match(Changed.class, c -> onChanged((Changed<GCounter>) c))
        .match(NotFound.class, a -> a.key().equals(cDataKey), this::onCounterNotFound)
        .match(NotFound.class, a -> a.key().equals(NumberProducer.ImsiDataKey), this::onNotFound)
        .build();
  }

  private void onStart() {
    context().system().scheduler().schedule(
        Duration.Zero(),
        Duration.create(5, TimeUnit.SECONDS),
        self(),
        READ,
        context().system().dispatcher(),
        getSelf()
    );
  }

  private void onChanged(Changed<GCounter> c) {
    log.info("Changed: {}", c.dataValue());
  }

  private void onCounterNotFound(NotFound<GCounter> data) {
    log.warning("Not found: {}", data);
    // We could initialize the counter here once, then we don't get the NotFound but still no increments from the producer
    // Update<GCounter> u = new Update<>(cDataKey, GCounter.create(), new WriteMajority(Duration.create(5, SECONDS)), curr -> curr.increment(cluster, 1));
    // replicator.tell(u, getSelf());
  }

  private void onNotFound(NotFound<LWWMap<Integer, Integer>> data) {
    log.warning("Not found: {}", data);
  }

  private void onDoRead() {
    log.info("Getting the map...");
    Optional<Object> ctx = Optional.of(sender());
    replicator.tell(new Replicator.Get<>(NumberProducer.ImsiDataKey, readMajority, ctx), self());
    replicator.tell(new Replicator.Get<>(cDataKey, readMajority, ctx), self());
  }

  private void onGetSuccess(GetSuccess<LWWMap<Integer, Integer>> map) {
    log.info("Read {}", map.dataValue().get(3).get());
  }

  private void onGetCounterSuccess(GetSuccess<GCounter> c) {
    log.info("Read {}", c.dataValue());
  }

}
