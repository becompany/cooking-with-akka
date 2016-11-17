package ch.becompany.akka.demo.actor

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import ch.becompany.akka.demo.Ingredients
import ch.becompany.akka.demo.actor.ChefActor.IngredientsRequest

import scala.util.{Failure, Success}

class ChefActor extends Actor with ActorLogging {

  var providerActor: ActorRef = ActorRef.noSender

  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case WaiterActor.Elaborate(plate) => {
      val waiterActor = sender()
      log.info("The chef got a request.")
      context.parent ? IngredientsRequest(plate.ingredients) onComplete {
        case Success(_) =>
          log.info("Breakfast elaborated.")
          waiterActor ! ChefActor.Breakfast
        case Failure(ex) =>
          log.error("Timeout requesting the breakfast ingredients.", ex)
          waiterActor ! akka.actor.Status.Failure(ex)
      }
    }
    case _ => log.error("The chef does not know how to handle the request.")
  }
}

object ChefActor {
  val props = Props[ChefActor]
  case object Breakfast
  case class IngredientsRequest(val ingredients: Seq[Ingredients])
}
