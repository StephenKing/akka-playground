package de.st_g.akka_playground.number_actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

import java.util.ArrayList;
import java.util.List;


public class NumberRouter extends UntypedActor {

  @Override
  public void onReceive(Object msg) throws Throwable {

    Router router;
    {
      List<Routee> routees = new ArrayList<Routee>();
      for (int i = 0; i < 5; i++) {
        ActorRef r = getContext().actorOf(Props.create(NumberReceiver.Number.class));
        getContext().watch(r);
        routees.add(new ActorRefRoutee(r));
      }
      router = new Router(new RoundRobinRoutingLogic(), routees);  
    }
    
    if (msg instanceof NumberReceiver.Number) {
      router.route(msg, getSender());
    } else if (msg instanceof Terminated) {
      router = router.removeRoutee(((Terminated) msg).actor());
      ActorRef r = getContext().actorOf(Props.create(NumberReceiver.Number.class));
      getContext().watch(r);
      router = router.addRoutee(new ActorRefRoutee(r));
    }
  }

}
