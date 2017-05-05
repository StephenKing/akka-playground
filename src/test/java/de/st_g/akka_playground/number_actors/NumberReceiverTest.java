package de.st_g.akka_playground.number_actors;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.junit.AfterClass;
import org.junit.Assert;
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
  public void exectLogOutput() {

    new JavaTestKit(system) {
      {
        Assert.assertEquals("default", system.name());
      }
    };
  };
}
