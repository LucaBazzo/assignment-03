package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.management.MasterActor
import pcd.assignment03.management.MasterActor.{MasterMessage, Start, StopComputation}
import pcd.assignment03.view.View.ViewMessage

import java.io.File

sealed trait ControllerMessage
case class Initialize(viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class SwapEvent(firstTilePosition: Int, secondTilePosition: Int) extends ControllerMessage
case class StopProcess() extends ControllerMessage
case class ProcessCompleted() extends ControllerMessage

object Controller {

  def apply(): Behavior[ControllerMessage] =
    Behaviors.setup { ctx => new Controller(ctx).initializing }
}

/** The Controller of the processes, receives messages from the view and the master
 *
 *  @constructor create a controller actor
 *  @param context the actor context
 */
class Controller(context: ActorContext[ControllerMessage]) {
  private var masterActor: ActorRef[MasterMessage] = _

  private val initializing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case Initialize(viewRef) =>
        this.masterActor = context.spawn(MasterActor(context.self, viewRef), "Master")

        standby
    }
  }

  private val standby: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case SwapEvent(firstTilePosition, secondTilePosition) =>
        //this.masterActor ! Start()
        println(secondTilePosition, firstTilePosition)

        Behaviors.same
        //computing
      case StopProcess() =>
        println("PUZZLE COMPLETED")
        this.masterActor ! StopComputation()
        Behaviors.same
    }
  }

  private val computing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case StopProcess() =>
        this.masterActor ! StopComputation()

        standby

      case ProcessCompleted() => standby
    }
  }
}
