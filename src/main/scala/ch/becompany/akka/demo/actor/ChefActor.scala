package ch.becompany.akka.demo.actor

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import ch.becompany.akka.demo.Ingredients.{Coffee, Egg, Water}
import ch.becompany.akka.demo.actor.BarActor.Provider

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import ch.becompany.akka.demo.actor.ProviderActor.Request

import scala.util.{Failure, Success}

class ChefActor extends Actor with ActorLogging {

  var providerActor: ActorRef = ActorRef.noSender

  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case Provider(providerActor) => this.providerActor = providerActor
    case WaiterActor.BreakfastRequest => {
      val waiterActor = sender()
      log.info("The chef got a request.")
      providerActor ? Request(Seq(Coffee, Water, Egg)) onComplete {
        case Success(_) =>
          log.info("Breakfast elaborated.")
          waiterActor ! ChefActor.Breakfast
        case Failure(ex) =>
          log.error("Timeout requesting the breakfast ingredients.", ex)
          waiterActor ! akka.actor.Status.Failure(ex)
      }
    }
    case _ => log.error("The chef does not know how to handle the message.")
  }
}

object ChefActor {
  val props = Props[ChefActor]
  case object Breakfast
}
