package de.st_g.akka_playground.number_actors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NumberReceiverTest {

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
  public void unitProcessNumbers() {
    final Props props = Props.create(NumberReceiver.class);
    final TestActorRef<NumberReceiver> ref =
        TestActorRef.create(system, props, "testsystem");
    final NumberReceiver actor = ref.underlyingActor();

    NumberReceiver.Number zeroNumber = new NumberReceiver.Number(0);
    assertFalse(actor.processNumber(zeroNumber));

    // should be a separate test?
    NumberReceiver.Number bigNumber = new NumberReceiver.Number(99);
    assertTrue(actor.processNumber(bigNumber));

  }

  @Test
  public void integrationSendNumber() {

    new JavaTestKit(system) {
      {
        final Props props = Props.create(NumberReceiver.class);
        final ActorRef subject = system.actorOf(props);

        // we don't expect that the number receiver sends any messages
        expectNoMsg();

        // create the message that we send
        final NumberReceiver.Number testNum = new NumberReceiver.Number(1);

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
