package ch.becompany.akka.demo

import akka.actor.ActorRef
import ch.becompany.akka.demo.actor.ProviderActor

import scala.util.Random

/**
  * Created by jpuerto on 11/11/16.
  */
class SupplierService(val providerActor: ActorRef) extends Runnable {
  val random = new Random(12345)

  override def run(): Unit = {
    providerActor ! randomIngredient
  }

  private def randomIngredient = Ingredients.values(random.nextInt(Ingredients.values.size))
}