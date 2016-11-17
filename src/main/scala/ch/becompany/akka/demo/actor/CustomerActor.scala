package ch.becompany.akka.demo.actor

import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.pattern.ask
import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, Props, Terminated}
import akka.util.Timeout
import ch.becompany.akka.demo.actor.BarActor.{HappyLeaving, TableRequest, UnhappyLeaving}
import ch.becompany.akka.demo.actor.ChefActor.Breakfast
import ch.becompany.akka.demo.actor.StreetActor.LeaveQueue

import scala.concurrent.Await
import scala.util.{Failure, Success}

class CustomerActor(val name: String) extends Actor with ActorLogging {
  import CustomerActor._
  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case Queue =>
        context.actorSelection("/user/streetActor/bar*").resolveOne().onComplete
      {
        case Success(ref) =>
          context.watch(ref)
          context.become(active(ref))
            log.info("Customer '{}' requests place in the Bar.", name)
            val future = ref ? TableRequest(self)
            future onComplete {
              case Success(waiterActor: ActorRef) => {
                context.parent ! LeaveQueue
                requestBreakfast(waiterActor, ref)
              }
              case Failure(msg) => {
                context.parent ! LeaveQueue
                log.error("The customer '{}' has to leave because no table is available. Error={}", name, msg)
              }
            }
        case Failure(_) => {
          log.error("Customer '{}' leaving because no bars are available.", name)
          context.parent ! LeaveQueue
        }
      }
    case _ => Unit
  }

  def requestBreakfast(waiterActor: ActorRef, barActor: ActorRef): Unit = {
    log.info("Request a breakfast.")
    val future = waiterActor ? WaiterActor.BreakfastRequest
    future.onComplete({
      case Success(_) => {
        log.info("The customer '{}' got a breakfast. Eating...", name)
        barActor ! HappyLeaving(name)
        context.stop(self)
      }
      case Failure(msg) => {
        log.error("The breakfast can not be served. Error = {}", msg)
        sender() ! akka.actor.Status.Failure(msg)
        barActor ! UnhappyLeaving(name)
        context.stop(self)
      }
    })
  }

  def active(another: ActorRef): Actor.Receive = {
    case Terminated(another) => context.stop(self)
  }
}

object CustomerActor {
  def props(name: String) = Props(new CustomerActor(name))
  case class Queue()
  case class Bar(waiter: ActorRef)
  case class Request()
  case class Eating()
}
