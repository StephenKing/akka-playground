package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import kamon.Kamon;
import kamon.metric.Counter;
import kamon.prometheus.PrometheusReporter;

import java.io.IOException;

public class NumberActors {

  public static void main(String[] args) throws IOException {

    PrometheusReporter rep = new PrometheusReporter();
    Kamon.addReporter(rep);
    Counter c =  Kamon.counter("akka.test.counter");
    c.increment(42L);


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
        break;
      case "consumer":
        system.actorOf(Props.create(NumberConsumer.class), "number-consumer");
        System.out.println("Created consumer");
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
