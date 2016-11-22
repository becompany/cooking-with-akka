package ch.becompany.akka.demo.actor

import java.util.concurrent.{TimeUnit, TimeoutException}

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import ch.becompany.akka.demo.actor.WaiterActor.{ChefRequest, Elaborate}

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

import scala.util.{Failure, Success}


class WaiterActor extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case CustomerActor.Request(name, request) => {
      log.info("Waiter got a breakfast request from '{}'.", name)
      val customer = sender()
      try {
        context.system.awaitTermination(Duration.create(1, TimeUnit.SECONDS))
      } catch {
        case ex: TimeoutException =>
          context.actorSelection("../..") ? ChefRequest onComplete {
            case Success(chefActor: ActorRef) =>
              log.info("The waiter got the assigned chef.")
              chefActor ? Elaborate(BarActor.Menu.Breakfast) onComplete {
                case Success(breakfast) =>
                  try {
                    context.system.awaitTermination(Duration.create(1, TimeUnit.SECONDS))
                  } catch {
                    case ex: TimeoutException => customer ! breakfast
                  }
                case Failure(ex) => {
                  log.error("Error dispatching the breakfast.", ex)
                  customer ! akka.actor.Status.Failure(ex)
                }
              }
            case Failure(ex) => {
              log.error("Error requesting a free chef.", ex)
              customer ! akka.actor.Status.Failure(ex)
            }
          }
      }
    }
    case _ => Unit
  }
}

object WaiterActor {
  val props = Props[WaiterActor]
  case object ChefRequest
  case class Elaborate(val plate: Menu)
}
