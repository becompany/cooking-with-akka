package ch.becompany.akka.demo.actor

import akka.actor.Actor.Receive
import scala.collection.mutable.Map
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import ch.becompany.akka.demo.Ingredients
import ch.becompany.akka.demo.Ingredients._

class ProviderActor extends Actor with ActorLogging {
  import ProviderActor._

  val stock = Map[Ingredients, Int](Coffee -> 10, Egg -> 10, Water -> 10)

  lazy val demoBar: ActorRef = sender()

  override def receive = {
    case Initialize => {
      log.info("ProviderActor has been initialized")
    }
    case Coffee => log.info(s"Storing Coffee in the stock. Total: ${stock.put(Coffee, stock.get(Coffee).get + 1) get}")
    case Egg => log.info(s"Storing Egg in the stock. Total: ${stock.put(Egg, stock.get(Egg).get + 1) get}")
    case Water => log.info(s"Storing Water in the stock. Total: ${stock.put(Water, stock.get(Water).get + 1) get}")
    case Request(ingredients) =>
      log.info(s"The Bar request the ingredients.")
      if (ingredients.map(ingredient => stock.get(ingredient).get > 0).reduce((x, y) => x && y)) {
        ingredients.map(ingredient => stock.put(ingredient, stock.get(ingredient).get -1))
        sender() ! "ok"
      } else {
        sender() ! akka.actor.Status.Failure(new IllegalStateException("No ingredients available."))
      }
    case _ => log.info("We do not work with this ingredient.")
  }
}

object ProviderActor {
  val props = Props[ProviderActor]
  case object Initialize
  case class Request(val ingredients: Seq[Ingredients])
}
