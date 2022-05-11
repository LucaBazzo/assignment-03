package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.Controller._
import pcd.assignment03.management.ReceptionistManager.{InitializeTileList, ReceptionistManagerMessage, TilesHasChanged}
import pcd.assignment03.management.SelectionManager.{SelectTile, SelectionManagerMessage}
import pcd.assignment03.management.{ReceptionistManager, SelectionManager}
import pcd.assignment03.utils.ImplicitConversions._
import pcd.assignment03.view.TileProperties
import pcd.assignment03.view.View.{DisplayWithTileset, UpdateView, ViewMessage}

/** Companion object for [[Controller]], contains the messages accepted by Controller actor
 *
 */
object Controller {

  sealed trait ControllerMessage

  case class Initialize(viewRef: ActorRef[ViewMessage], tileList: List[TileProperties]) extends ControllerMessage

  case class TileSelected(tile: TileProperties) extends ControllerMessage
  case class SendUpdate(tileList: List[TileProperties], isPuzzleCompleted: Boolean) extends ControllerMessage
  case class SynchronizeView(tileList: List[TileProperties], isPuzzleCompleted: Boolean) extends ControllerMessage

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
    message match {
      case Initialize(viewRef, tileList) =>
        this.viewRef = viewRef
        this.selectionManager =
          context.spawn(SelectionManager(tileList, context.self), "SelectionManager")
        this.receptionistManager =
          context.spawn(ReceptionistManager(context.self, selectionManager, port), "ReceptionistManager")

        this.receptionistManager ! InitializeTileList(tileList)

        Behaviors.same

      case SynchronizeView(tileList, isPuzzleCompleted) =>
        this.viewRef ! DisplayWithTileset(tileList, isPuzzleCompleted)

        waitingEvents
    }
  }

  private val waitingEvents: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {

      case SendUpdate(tileList, isPuzzleCompleted) =>
        viewRef ! UpdateView(tileList, isPuzzleCompleted)
        this.receptionistManager ! TilesHasChanged(tileList)
        Behaviors.same

      case TileSelected(tile) =>
        this.selectionManager ! SelectTile(tile)

        Behaviors.same

      case SynchronizeView(tileList, isPuzzleCompleted) =>
        viewRef ! UpdateView(tileList, isPuzzleCompleted)

        Behaviors.same
    }
  }
}
