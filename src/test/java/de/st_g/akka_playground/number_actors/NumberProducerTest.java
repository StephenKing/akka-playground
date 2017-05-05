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
  public void integrationExectMessage() {

    new JavaTestKit(system) {
      {
        final Props props = Props.create(NumberProducer.class);
        final ActorRef subject = system.actorOf(props);

        final JavaTestKit probe = new JavaTestKit(system);

        subject.tell(probe.getRef(), getRef());


      }
    };
  }
}
