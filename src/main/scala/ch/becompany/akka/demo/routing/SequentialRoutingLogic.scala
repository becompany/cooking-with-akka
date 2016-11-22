package ch.becompany.akka.demo.routing

import java.util.concurrent.atomic.AtomicInteger

import akka.routing.{NoRoutee, Routee, RoutingLogic}

import scala.collection.immutable.IndexedSeq

final class SequentialRoutingLogic extends RoutingLogic {

  var current: AtomicInteger = new AtomicInteger(0)

  override def select(message: Any, routees: IndexedSeq[Routee]): Routee =
    if (routees.isEmpty) NoRoutee
    else routees(getNext(routees))

  private def getNext(routees: IndexedSeq[Any]): Int = {
    val actual = current.addAndGet(1)
    actual % routees.size
  }
}
