package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NumberActors {


  public static final String START = "start";

  public static void main(String[] args) throws IOException {

    System.out.println("Starting");
    final ActorSystem system = ActorSystem.create("number-actors");
    System.out.println("Got my actor system");


    ConfigList roles = system.settings().config().getList("akka.cluster.roles");
    if (roles.size() != 1) {
      throw new IllegalArgumentException(
          "Expected akka.cluster.roles to contain exactly one role. Got: " + roles.unwrapped());
    }
    System.out.println(roles);

    ConfigValue roleConfig = roles.get(0);
    String role = roleConfig.unwrapped().toString();

    switch (role) {
      case "producer":
        final ActorRef producer =
            system.actorOf(Props.create(NumberProducer.class), "number-producer");
        System.out.println("Created producer");
        producer.tell(START, null);
        break;
      case "consumer":
        final ActorRef consumer = system.actorOf(Props.create(NumberConsumer.class), "number-consumer");
        System.out.println("Created consumer");
        system.scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS), consumer, START, system.dispatcher(), null);
        break;
      case "seed":
        System.out.println("I'm the seed.. boring.");
        break;
      default:
        throw new IllegalArgumentException("Value " + role + " is invalid in akka.cluster.roles");
    }

    //    System.out.println("Press any key to stop");
    //    System.in.read();
    //    System.out.println("Shutting down actor system...");
    //    system.terminate();
  }

}
