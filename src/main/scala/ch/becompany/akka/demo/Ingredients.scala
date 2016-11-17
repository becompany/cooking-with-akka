package ch.becompany.akka.demo

abstract class Ingredients(val name: String)

object Ingredients {
  case object Egg extends Ingredients("egg")
  case object Water extends Ingredients("water")
  case object Coffee extends Ingredients("coffee")
  val values = List(Egg, Water, Coffee)
}
