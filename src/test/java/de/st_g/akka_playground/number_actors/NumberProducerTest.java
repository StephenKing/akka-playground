package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NumberProducerTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
  }

  @Test
  public void expectStartedMessage() {

    new JavaTestKit(system) {
      {
        final Props props = Props.create(NumberProducer.class);
        final ActorRef subject = system.actorOf(props);

        subject.tell("start", getRef());

        expectMsgEquals("started");
      }
    };
  }
  
  @Test
  public void integrationTestConsumerLog() {

    new JavaTestKit(system) {
      {
        final Props props = Props.create(NumberConsumer.class);
        final ActorRef subject = system.actorOf(props);

        // create the message that we send
        final NumberConsumer.Number testNum = new NumberConsumer.Number(1);

        // we expect a INFO log message that such number was received
        new EventFilter<Boolean>(akka.event.Logging.Info.class) {
          protected Boolean run() {
            // send the message
            subject.tell(testNum, ActorRef.noSender());
            return true;
          }
        }.message("And the number is: " + testNum.getNumber()).occurrences(1).exec();
      }
    };
  }
}
