package ch.becompany.akka

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext.Implicits.global
import ch.becompany.akka.demo.{CustomerService, SupplierService}
import ch.becompany.akka.demo.actor.{ProviderActor, StreetActor}

import scala.concurrent.duration.Duration
import scala.util.{Failure, Random, Success, Try}


object ApplicationMain extends App {
  val system = ActorSystem("BreakfastDemo")
  val streetActor = system.actorOf(StreetActor.props(15), "streetActor")
  val supplierInterval = Duration.create(1000, TimeUnit.MILLISECONDS)
  val customerInterval = Duration.create(2, TimeUnit.SECONDS)

  streetActor ! StreetActor.Initialize
  system.scheduler.schedule(Duration.Zero, customerInterval, new CustomerService(streetActor))
  system.actorSelection("/user/streetActor/providerActor").resolveOne(Duration.create(1, TimeUnit.SECONDS)).onComplete {
    case Success(providerActor) => system.scheduler.schedule(Duration.Zero, supplierInterval, new SupplierService(providerActor))
    case Failure(ex) => println(ex)
  }
  try {
    system.awaitTermination(Duration.create(30, TimeUnit.SECONDS))
  } catch {
    case e => system.shutdown()
  }
}
