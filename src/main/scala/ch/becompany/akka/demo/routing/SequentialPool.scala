package ch.becompany.akka.demo.routing

import akka.actor.{ActorSystem, SupervisorStrategy}
import akka.dispatch.Dispatchers
import akka.routing._

@SerialVersionUID(1L)
final case class SequentialPool(override val nrOfInstances: Int, override val resizer: Option[Resizer] = None,
override val supervisorStrategy: SupervisorStrategy = Pool.defaultSupervisorStrategy,
override val routerDispatcher: String = Dispatchers.DefaultDispatcherId,
override val usePoolDispatcher: Boolean = false)
  extends Pool
{

  override def createRouter(system: ActorSystem): Router = Router(new SequentialRoutingLogic)
}
