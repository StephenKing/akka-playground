package de.st_g.akka_playground.number_actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class NumberReceiver extends UntypedActor {

  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  public static class Number {
    final Integer number;

    public Number(Integer number) {
      this.number = number;
    }

    public Integer getNumber() {
      return number;
    }

    public String toString() {
      return "Number[" + number + "]";
    }
  }


  public void onReceive(Object message) throws Exception {
    log.info("Received: {}", message);
    if (message instanceof Number) {
      processNumber((Number) message);
    } else {
      log.warning("Message not of type Number");
      unhandled(message);
    }
  }

  /**
   * Logs the number, returns true if number >0
   * 
   * @param num
   * @return
   */
  public boolean processNumber(Number num) {
    log.info("And the number is: {}", num.getNumber());
    return num.getNumber() > 0;
  }
}
