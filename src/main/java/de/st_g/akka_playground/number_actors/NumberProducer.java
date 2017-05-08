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

  ActorRef consumerRouter = getContext()
      .actorOf(FromConfig.getInstance().props(Props.create(NumberConsumer.class)),
          "consumer-router");

  public NumberProducer() {
    sendNumbers();
  }


  private void sendNumbers() {


    NumberConsumer.Number num;
    int i = 0;
    while (true) {
      
      num = new NumberConsumer.Number(i);
      log.info("Sending {}", num);
      consumerRouter.tell(num, getSelf());

      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      i++;
    }
  }

  @Override public void onReceive(Object message) throws Throwable {
    log.info("Ignoring message: {}", message);
  }
}
