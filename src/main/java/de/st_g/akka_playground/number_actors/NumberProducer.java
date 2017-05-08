package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.actor.UntypedActor;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.concurrent.TimeUnit;

public class NumberProducer extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  Cluster cluster = Cluster.get(getContext().system());

  boolean sending = false;

  @Override public void preStart() throws Exception {
    log.info("preStart(), registering for MemberEvents");
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(),
        MemberEvent.class, UnreachableMember.class);
  }

  @Override public void postStop() throws Exception {
    cluster.unsubscribe(getSelf());
  }

  @Override
  public void onReceive(Object msg) throws Throwable {

    log.info("Received {}", msg);
    if (msg instanceof MemberUp) {
      MemberUp upEvent = (MemberUp) msg;
      log.debug("Received MemberUp event: {} (roles: {}", upEvent, upEvent.member().roles());
      if (upEvent.member().roles().contains("consumer")) {
        Member member = upEvent.member();
        log.info("New consumer at {}", member.address());

        if (!isSending()) {
          log.info("Starting to send");
          // sendNumbers();
        }
      }

    } else {
      log.info("Ignoring message: {}", msg);
    }
  }

  private boolean isSending() {
    return sending;
  }

  private void sendNumbers() throws InterruptedException {

    sending = true;

    NumberReceiver.Number num;

    for (int i = 0; i <= 300; i++) {
      num = new NumberReceiver.Number(i);
      for (ActorRef child : getContext().getChildren()) {
        log.info("Sending {} to {}", num, child.path());
        child.tell(num, getSelf());
      }
      TimeUnit.SECONDS.sleep(1);
    }


    getContext().system().terminate();
    System.out.println("Finished");
  }
}
