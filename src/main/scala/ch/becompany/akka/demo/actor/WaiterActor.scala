package ch.becompany.akka.demo.actor

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import ch.becompany.akka.demo.actor.WaiterActor.BreakfastRequest

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import ch.becompany.akka.demo.actor.BarActor.ChefRequest

import scala.util.{Failure, Success}


/**
  * Created by jpuerto on 11/11/16.
  */
class WaiterActor extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case BreakfastRequest => {
      log.info("Waiter got a breakfast request from '{}'.", sender().path.name)
      val future = context.parent ? ChefRequest
      val customer = sender()
      future.onComplete({
        case Success(chefActor: ActorRef) => chefActor ? BreakfastRequest onComplete {
          case Success(_) => customer ! ChefActor.Breakfast
          case Failure(ex) => {
            log.error("Error dispatching the breakfast.", ex)
            customer ! akka.actor.Status.Failure(ex)
          }
        }
        case Failure(ex) => {
          log.error("Error requesting a free chef.", ex)
          customer ! akka.actor.Status.Failure(ex)
        }
      })
    }
    case _ => Unit
  }
}

object WaiterActor {
  val props = Props[WaiterActor]
  case object BreakfastRequest
}
