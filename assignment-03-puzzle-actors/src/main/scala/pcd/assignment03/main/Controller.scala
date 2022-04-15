package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.management.{ReceptionistManager, SelectionManager}
import pcd.assignment03.management.ReceptionistManager.{ReceptionistManagerMessage, Swap}
import pcd.assignment03.management.SelectionManager.{ReceivePuzzleUpdate, SelectTile, SelectionManagerMessage, UpdateTileList}
import pcd.assignment03.view.View.{Display, UpdateView, ViewMessage}
import pcd.assignment03.view.Tile
import pcd.assignment03.utils.ImplicitConversions

import scala.language.implicitConversions

sealed trait ControllerMessage
case class Initialize(tileList: List[Tile]) extends ControllerMessage
case class UpdatePuzzle(tileList: List[Tile]) extends ControllerMessage
case class SendUpdate(tileList: List[Tile], isPuzzleCompleted: Boolean) extends ControllerMessage
case class ReceiveUpdate(tileList: List[(Int, Int)], isPuzzleCompleted: Boolean) extends ControllerMessage
case class SynchronizeView(tileList: List[Tile], isPuzzleCompleted: Boolean) extends ControllerMessage
case class TileSelected(tile: Tile) extends ControllerMessage
case class StopProcess() extends ControllerMessage
case class ProcessCompleted() extends ControllerMessage
case class RegisterView(viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class DisplayView(seed: Int) extends ControllerMessage

object Controller {

  def apply(port: Int): Behavior[ControllerMessage] =
    Behaviors.setup { ctx => new Controller(ctx, port).initializing }
}

/** The Controller of the processes, receives messages from the view and the master
 *
 *  @constructor create a controller actor
 *  @param context the actor context
 */
class Controller(context: ActorContext[ControllerMessage], port: Int) {

  private var viewRef: ActorRef[ViewMessage] = _
  private var selectionManager: ActorRef[SelectionManagerMessage] = _
  private var receptionistManager: ActorRef[ReceptionistManagerMessage] = _

  private val initializing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    if(this.receptionistManager == null)
      this.receptionistManager = context.spawn(ReceptionistManager(context.self, port), "ReceptionistManager")

    message match {
      case Initialize(tileList) =>
        this.selectionManager = context.spawn(SelectionManager(tileList, context.self), "SelectionManager")
        waitingEvents

      case RegisterView(viewRef) =>
        this.viewRef = viewRef
        Behaviors.same

      case DisplayView(seed) =>
        println(seed)
        this.viewRef ! Display(seed)
        Behaviors.same
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
