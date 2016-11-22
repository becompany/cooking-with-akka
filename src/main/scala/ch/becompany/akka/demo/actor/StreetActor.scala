package ch.becompany.akka.demo.actor

import scala.collection.mutable.Set
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing._

import scala.concurrent.duration._
import ch.becompany.akka.demo.actor.StreetActor.{Initialize, LeaveQueue}
import ch.becompany.akka.demo.routing.SequentialPool

class StreetActor(val maximum: Int = 15, val barsNumber: Int) extends Actor with ActorLogging {
  val bars = context.actorOf(SequentialPool(barsNumber).props(BarActor.props), "bars")
  var queue = Set[ActorRef]()
  var counter = 0

  override def receive = {
    case Initialize => {
      log.info("Street is being created.")
      log.info("Created {} bars.", barsNumber)
      bars ! Broadcast(Initialize)
    }
    case LeaveQueue => {
      queue.remove(sender())
    }
    case customerName: String =>  {
      if (queue.size < maximum) {
        counter += 1
        val customerActor = context.actorOf(CustomerActor.props(customerName), "customer" + counter)
        queue += customerActor
        log.info(s"The customer '{}' is in the queue. Queue size: {}", customerName, queue.size)

        customerActor ! Initialize
      } else {
        log.warning(s"Customer '{}' has to leave because queue is full.", customerName)
      }
    }
    case unknown => log.error(s"Actor name '{}' not recognized.", unknown)
  }
}

object StreetActor {
  def props(maximum: Int, barsNumber: Int = 1) = Props(new StreetActor(maximum, barsNumber))
  case object Initialize
  case object LeaveQueue
}