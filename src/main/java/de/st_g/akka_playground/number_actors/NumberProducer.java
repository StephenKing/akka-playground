package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

import java.util.concurrent.TimeUnit;

public class NumberProducer extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  boolean sending = false;

  ActorRef consumerRouter = getContext()
      .actorOf(FromConfig.getInstance().props(Props.create(NumberReceiver.class)),
          "receiver-router");

  public NumberProducer() {
    sendNumbers();
  }


  private void sendNumbers() {

    NumberReceiver.Number num;

    for (int i = 0; i <= 300; i++) {
      num = new NumberReceiver.Number(i);
      log.info("Sending {}", num);
      consumerRouter.tell(num, getSelf());

      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override public void onReceive(Object message) throws Throwable {
    log.info("Ignoring message: {}", message);
  }
}
