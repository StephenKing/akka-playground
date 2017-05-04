package de.st_g.akka_playground.number_actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class NumberReceiver extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  public void onReceive(Object message) throws Exception {
    log.info("Received: {}", message);
    if (message instanceof String) {
      log.info("Received String message: {}", message);
      // getSender().tell("Hello " + , getSelf());
    } else {
      unhandled(message);
    }
  }
}
