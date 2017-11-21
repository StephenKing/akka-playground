package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NumberProducer extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  Cluster cluster = Cluster.get(getContext().system());
  Random rand = new Random();

  ActorRef consumerRouter = getContext()
      .actorOf(FromConfig.getInstance().props(Props.create(NumberConsumer.class)),
          "consumer-router");

  private boolean consumersAvailable = false;

  @Override public void preStart() throws Exception {
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
        ClusterEvent.ClusterDomainEvent.class);
  }

  @Override public void postStop() throws Exception {
    cluster.unsubscribe(getSelf());
  }

  private void sendNumbers() {


    NumberConsumer.Number num;
    int i = 0;
    while (true) {

      waitConsumerAvailable();

      num = new NumberConsumer.Number(i);
      log.info("Sending {}", num);
      consumerRouter.tell(num, getSelf());

      try {
        int sleep = rand.nextInt(50);
        log.info("Sleeping for {}ms", sleep);
        TimeUnit.MILLISECONDS.sleep(sleep);

      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      i++;
    }
  }

  private void waitConsumerAvailable() {
    while (!consumersAvailable) {
      log.debug("Waiting for consumers to come up...");
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void setConsumersAvailable(boolean newStatus) {
    boolean oldStatus = consumersAvailable;
    consumersAvailable = newStatus;
    // we're changing from no consumers to consumers available
    if (newStatus && !oldStatus) {
      sendNumbers();
    }
  }

  @Override public void onReceive(Object message) throws Throwable {
    log.info("Received message: {}", message);
    if (message instanceof CurrentClusterState) {
      CurrentClusterState state = (CurrentClusterState) message;
      if (state.getAllRoles().contains("consumer")) {
        log.info("There's a consumer among all our roles: {}", state.getAllRoles());
        setConsumersAvailable(true);
      } else {
        setConsumersAvailable(false);
      }
    } else if (message instanceof MemberUp) {
      MemberUp upEvent = (MemberUp) message;
      if (upEvent.member().roles().contains("consumer")) {
        log.info("Got a consumer at {}", upEvent.member().address());
        setConsumersAvailable(true);
      }
    } else if (message instanceof ClusterEvent.MemberLeft) {
      log.info("Member left: {}", ((ClusterEvent.MemberLeft) message).member().address());
    } else if (message instanceof ClusterEvent.MemberRemoved) {
      log.info("Member removed: {}", ((ClusterEvent.MemberRemoved) message).member().address());
    }
  }
}
