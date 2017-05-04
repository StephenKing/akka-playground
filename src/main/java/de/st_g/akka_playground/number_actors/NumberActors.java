package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class NumberActors {

  public static void main(String[] args) throws IOException {
    System.out.println("Starting");
    final ActorSystem system = ActorSystem.create("number-actors");
    System.out.println("Got my actor system");
    final ActorRef producer = system.actorOf(Props.create(NumberProducer.class), "number-producer");
    System.out.println("Created producer");

    producer.tell("start", ActorRef.noSender());
    System.out.println("Press any key to stop");
    System.in.read();
    System.out.println("Shutting down actor system...");
    system.terminate();
  }

}
