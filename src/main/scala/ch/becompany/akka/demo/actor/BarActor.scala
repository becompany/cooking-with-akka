package ch.becompany.akka.demo.actor

import scala.collection.mutable.Set
import scala.collection.mutable.Map
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import ch.becompany.akka.demo.Ingredients

import scala.concurrent.duration._
import akka.util.Timeout


class BarActor extends Actor with ActorLogging {
  import Ingredients._

  implicit val timeout = Timeout(5 seconds)

  val stock = Map[Ingredients, Int](Coffee -> 100, Egg -> 100, Water -> 100)
  val places = Set[ActorRef]()
  val maximum = 15
  var happyCustomers = 0
  var unhappyCustomers = 0

  val barReferences: BarReferences = BarReferences(
    context.actorOf(RoundRobinPool(2).props(ChefActor.props)),
    context.actorOf(RoundRobinPool(2).props(WaiterActor.props)))

  override def receive = {
    case StreetActor.Initialize =>
      log.info("Opening a new bar.")
    case CustomerActor.TableRequest(customer) =>
      log.info("We got a table request.")
      if (places.size < maximum) {
        places += customer
        log.info("Places available: {}", maximum - places.size )
        sender() ! (barReferences.waiterActor, self)
      }
    case WaiterActor.ChefRequest =>
      log.info("Bar is dispatching a Chef request.")
      sender() ! barReferences.chefActor

    case CustomerActor.Leaving(name, happy) =>
      places.remove(sender())
      if (happy) {
        log.info("The customer '{}' is happy.", name)
        happyCustomers += 1
      } else {
        log.info("The customer '{}' is unhappy.", name)
        unhappyCustomers += 1
      }
    case ChefActor.IngredientsRequest(ingredients) =>
      log.info(s"The Bar request the ingredients.")
      if (ingredients.map(ingredient => stock.get(ingredient).get > 0).reduce((x, y) => x && y)) {
        ingredients.map(ingredient => stock.put(ingredient, stock.get(ingredient).get -1))
        sender() ! "ok"
      } else {
        sender() ! akka.actor.Status.Failure(new IllegalStateException("No ingredients available."))
      }
    case _ => log.warning("We do not work with these ingredients.")
  }

  override def postStop(): Unit = {
    super.postStop()
    log.info("We close the bar with {} happy customers and {} unhappy.", happyCustomers, unhappyCustomers)
  }
}

abstract sealed class Menu(val ingredients: Seq[Ingredients])

object BarActor {
  val props = Props[BarActor]
  case class Provider(val name: ActorRef)

  object Menu  {
    case object Breakfast extends Menu(Seq(Ingredients.Coffee, Ingredients.Egg, Ingredients.Water))
  }
}

case class BarReferences(val chefActor: ActorRef, val waiterActor: ActorRef)