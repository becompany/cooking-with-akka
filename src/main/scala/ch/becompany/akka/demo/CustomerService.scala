package ch.becompany.akka.demo

import akka.actor.ActorRef
import dispatch._
import Defaults._
import argonaut._
import Argonaut._

import scala.util.{Failure, Random, Success}

class CustomerService (val streetActor: ActorRef) extends Runnable {

  val namesUrl = url("http://uinames.com/api/?amount=25")
  var names: List[String] = Nil
  var running: Boolean = false

  override def run(): Unit = {
    if (!running && names.isEmpty) {
      running = true
      Http(namesUrl OK as.String).andThen {
        case Success(jsonString) =>
          names = Parse.parseWith[List[String]](jsonString, json => json.arrayOrEmpty.map {
            jsonObject =>
              jsonObject.field("name").flatMap(_.string).get
          }, failure => Nil)
          streetActor ! names(Random.nextInt(names.size))
      }
    } else if (!names.isEmpty) {
      streetActor ! names(Random.nextInt(names.size))
    }
  }

}
