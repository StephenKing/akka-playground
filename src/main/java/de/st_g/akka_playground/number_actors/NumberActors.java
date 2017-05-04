package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class NumberActors {

  public static void main(String[] args) {
    System.out.println("Starting");
    final ActorSystem system = ActorSystem.create("number-actors");
    System.out.println("Got my actor system");
    final ActorRef producer = system.actorOf(Props.create(NumberProducer.class), "number-producer");
    System.out.println("Created producer");

    producer.tell("start", ActorRef.noSender());
  }

}
