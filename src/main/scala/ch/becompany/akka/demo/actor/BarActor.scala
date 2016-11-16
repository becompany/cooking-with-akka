package ch.becompany.akka.demo.actor

import java.util.concurrent.TimeUnit

import scala.collection.mutable.Set
import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Props}
import ch.becompany.akka.demo.Ingredients
import ch.becompany.akka.demo.actor.BarActor._
import ch.becompany.akka.demo.actor.WaiterActor.BreakfastRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import ch.becompany.akka.demo.actor.ChefActor.Breakfast


/**
  * Created by jpuerto on 11/11/16.
  */
class BarActor extends Actor with ActorLogging {
  import Ingredients._

  implicit val timeout = Timeout(5 seconds)
  val places = Set[ActorRef]()
  val maximum = 15
  var happyCustomers = 0
  var unhappyCustomers = 0

  val barReferences: BarReferences = new BarReferences(
    context.actorOf(ChefActor.props),
    context.actorOf(WaiterActor.props))(ActorRef.noSender)

  override def receive = {
    case Initialize =>
      log.info("Opening a new bar. Looking for providers.")
      context.actorSelection("../providerActor").resolveOne(Duration.create(1, TimeUnit.SECONDS)).onComplete {
        case Success(providerRef) => {
          barReferences.providerActor = providerRef
          barReferences.chefActor ! Provider(providerRef)
        }
        case Failure(ex) => log.error("Error locating a provider action. Error = {}.", ex)
      }
    case TableRequest(customer) =>
      log.info("We got a table request.")
      if (places.size < maximum) {
        places += customer
        log.info("Places available: {}", maximum - places.size )
        sender() ! barReferences.waiterActor
      }
    case ChefRequest =>
      log.info("Bar is dispatching a Chef request.")
      sender() ! barReferences.chefActor

    case HappyLeaving(name) =>
      log.info("The customer '{}' is happy.", name)
      places.remove(sender())
      happyCustomers += 1
    case UnhappyLeaving(name) =>
      log.info("The customer '{}' is unhappy.", name)
      places.remove(sender())
      unhappyCustomers += 1
    case _ => log.warning("We do not work with these ingredients.")
  }
}

object BarActor {
  val props = Props[BarActor]
  case object Initialize
  case class TableRequest(val customer: ActorRef)
  case object ChefRequest
  case class Provider(val name: ActorRef)
  case class HappyLeaving(val name: String)
  case class UnhappyLeaving(val name: String)
}

class BarReferences(val chefActor: ActorRef, val waiterActor: ActorRef)(var providerActor: ActorRef) {

}