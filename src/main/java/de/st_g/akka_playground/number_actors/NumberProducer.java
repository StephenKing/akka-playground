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
      sendNumbers();
    } else {
      log.info("oh... not starting?");
    }
  }

  protected void sendNumbers() throws InterruptedException {
    ActorRef printNumbersConsumer =
        getContext().system().actorOf(Props.create(NumberReceiver.class), "number-consumer");

    for (int i = 1; i <= 10; i++) {
      log.info("Sending:  {}", i);
      printNumbersConsumer.tell(i, ActorRef.noSender());
      TimeUnit.SECONDS.sleep(1);
    }


    getContext().system().terminate();
    System.out.println("Finished");
  }
}
