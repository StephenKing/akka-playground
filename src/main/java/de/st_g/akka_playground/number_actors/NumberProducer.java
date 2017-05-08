package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.concurrent.TimeUnit;

public class NumberProducer extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  @Override
  public void onReceive(Object msg) throws Throwable {

    log.info("Received {}", msg);
    String m = (String) msg;
    if (m.equals("start")) {
      createChildActors();

      getSender().tell("started", getSelf());
      sendNumbers();
    } else {
      log.info("oh... not starting?");
    }
  }

  private void sendNumbers() throws InterruptedException {

    NumberReceiver.Number num;

    for (int i = 0; i <= 3; i++) {
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

  private void createChildActors() {
    int numActors = 2;
    log.info("Creating {} actors", numActors);
    for (int i = 0; i < 2; i++) {
      getContext().actorOf(Props.create(NumberReceiver.class), "number-consumer-" + i);
    }
  }
}
