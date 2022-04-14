package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.management.ReceptionistManager
import pcd.assignment03.management.ReceptionistManager.{ReceptionistManagerMessage, Swap}
import pcd.assignment03.view.SelectionManager.{ReceivePuzzleUpdate, SelectTile, SelectionManagerMessage, UpdateTileList}
import pcd.assignment03.view.View.{UpdateView, ViewMessage}
import pcd.assignment03.view.{SelectionManager, Tile}
import pcd.assignment03.utils.ImplicitConversions

import scala.language.implicitConversions

sealed trait ControllerMessage
case class Initialize(tileList: List[Tile], viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class UpdatePuzzle(tileList: List[Tile]) extends ControllerMessage
case class SendUpdate(tileList: List[Tile], isPuzzleCompleted: Boolean) extends ControllerMessage
case class ReceiveUpdate(tileList: List[(Int, Int)], isPuzzleCompleted: Boolean) extends ControllerMessage
case class SynchronizeView(tileList: List[Tile], isPuzzleCompleted: Boolean) extends ControllerMessage
case class TileSelected(tile: Tile) extends ControllerMessage
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

  private var viewRef: ActorRef[ViewMessage] = _
  private var selectionManager: ActorRef[SelectionManagerMessage] = _
  private var receptionistManager: ActorRef[ReceptionistManagerMessage] = _

  private val initializing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case Initialize(tileList, viewRef) =>
        this.viewRef = viewRef
        this.selectionManager = context.spawn(SelectionManager(tileList, context.self), "SelectionManager")
        this.receptionistManager = context.spawn(ReceptionistManager(tileList, context.self), "ReceptionistManager")

        //request puzzle
        waitingEvents
    }
  }

  private val waitingEvents: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case UpdatePuzzle(tileList) =>
        this.selectionManager ! UpdateTileList(tileList)
        Behaviors.same

      case SendUpdate(tileList, isPuzzleCompleted) =>
        viewRef ! UpdateView(tileList, isPuzzleCompleted)
        this.receptionistManager ! Swap(tileList, isPuzzleCompleted)
        Behaviors.same

      case TileSelected(tile) =>
        this.selectionManager ! SelectTile(tile)

        Behaviors.same

      case ReceiveUpdate(tileList, isPuzzleCompleted) =>
        selectionManager ! ReceivePuzzleUpdate(tileList, isPuzzleCompleted)

        Behaviors.same

      case SynchronizeView(tileList, isPuzzleCompleted) =>
        viewRef ! UpdateView(tileList, isPuzzleCompleted)

        Behaviors.same

      case ProcessCompleted() =>
        println("PUZZLE COMPLETED")
        //this.masterActor ! StopComputation()
        Behaviors.same
    }
  }

  implicit def TileListToIntPairList(tileList: List[Tile]): List[(Int, Int)] = {
    var intList: List[(Int, Int)] = List.empty
    tileList.foreach(tile => intList = Tuple2(tile.getStartPosition, tile.getCurrentPosition) :: intList)
    intList.sortBy(_._1)
  }

}
