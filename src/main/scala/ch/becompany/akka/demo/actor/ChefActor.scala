package ch.becompany.akka.demo.actor

import java.util.concurrent.{TimeUnit, TimeoutException}

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
      context.actorSelection("../..") ? IngredientsRequest(plate.ingredients) onComplete {
        case Success(_) =>
          log.info("Breakfast elaborated.")
          try {
            context.system.awaitTermination(Duration.create(2, TimeUnit.SECONDS))
          } catch {
            case ex: TimeoutException => waiterActor ! plate
            case ex => log.error("Unable to elaborate the plate.", ex)
          }

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
  case class IngredientsRequest(val ingredients: Seq[Ingredients])
}
