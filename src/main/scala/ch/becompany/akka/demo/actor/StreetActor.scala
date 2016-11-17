package ch.becompany.akka.demo.actor

import scala.collection.mutable.Set
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import ch.becompany.akka.demo.actor.StreetActor.{Initialize, LeaveQueue}

class StreetActor(val maximum: Int = 15, val barsNumber: Int = 1) extends Actor with ActorLogging {
  var bars = scala.collection.mutable.ArrayBuffer.empty[ActorRef]
  var queue = Set[ActorRef]()
  var counter = 0

  override def receive = {
    case Initialize => {
      log.info("Street is being created.")
      for (i <- 0 until barsNumber ) {
        bars += context.actorOf(BarActor.props, "bar" + i)
        log.info(s"Created 'bar$i'.")
      }
      bars map (actor => actor ! Initialize)
    }
    case LeaveQueue => {
      queue.remove(sender())
    }
    case customerName: String =>  {
      if (queue.size < maximum) {
        counter += 1
        val customerActor = context.actorOf(CustomerActor.props(customerName), "customer" + counter)
        queue += customerActor
        log.info(s"The customer '$customerName' is in the queue. Queue size: ${queue.size}")

        customerActor ! Initialize
      } else {
        log.warning(s"Customer '$customerName' has to leave because queue is full.")
      }
    }
    case unknown => log.error(s"Actor name '$unknown' not recognized.")
  }
}

object StreetActor {
  def props(maximum: Int) = Props(new StreetActor(maximum))
  case object Initialize
  case object LeaveQueue
}