package ch.becompany.akka

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext.Implicits.global
import ch.becompany.akka.demo.CustomerService
import ch.becompany.akka.demo.actor.StreetActor

import scala.concurrent.duration.Duration

object ApplicationMain extends App {
  val system = ActorSystem("BreakfastDemo")
  val streetActor = system.actorOf(StreetActor.props(15), "streetActor")
  val customerInterval = Duration.create(1000, TimeUnit.MILLISECONDS)

  streetActor ! StreetActor.Initialize
  //system.scheduler.scheduleOnce(Duration.Zero, new CustomerService(streetActor))
  val customerGenerator = system.scheduler.schedule(Duration.Zero, customerInterval, new CustomerService(streetActor))
  try {
    system.awaitTermination(Duration.create(30, TimeUnit.SECONDS))
  } catch {
    case e:Throwable => {
      customerGenerator.cancel()
      try {
        system.awaitTermination(Duration.create(10, TimeUnit.SECONDS))
      } catch {
        case e: Throwable => {
          system.shutdown()
        }
      }
    }
  }
}
