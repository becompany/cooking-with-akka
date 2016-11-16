package ch.becompany.akka.demo

import akka.actor.ActorRef
import dispatch._
import Defaults._
import argonaut._
import Argonaut._

import scala.util.{Failure, Success}

class CustomerService (val streetActor: ActorRef) extends Runnable {

  val namesUrl = url("http://uinames.com/api/")

  override def run(): Unit = {
    Http(namesUrl OK as.String).andThen {
      case Success(jsonString) => streetActor ! Parse.parseWith[String](jsonString, _.field("name").flatMap(_.string).get, msg => msg)
    }
  }

}
