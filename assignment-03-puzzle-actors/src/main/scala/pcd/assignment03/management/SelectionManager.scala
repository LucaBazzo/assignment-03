package pcd.assignment03.view

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.{ControllerMessage, SendUpdate, SynchronizeView}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.SelectionManager.SelectionManagerMessage

object SelectionManager {

  trait SelectionManagerMessage
  case class UpdateTileList(newTileList: List[Tile]) extends SelectionManagerMessage
  case class SelectTile(tile: Tile) extends SelectionManagerMessage
  case class ReceivePuzzleUpdate(tupleList: List[(Int, Int)], isPuzzleCompleted: Boolean) extends  SelectionManagerMessage

  def apply(tiles: List[Tile], controllerRef: ActorRef[ControllerMessage]): Behavior[SelectionManagerMessage] =
    Behaviors.setup { _ =>
      new SelectionManager(tiles, controllerRef).waitingEvents
    }
}

class SelectionManager(var tiles: List[Tile],
                       val controllerRef: ActorRef[ControllerMessage]){

  private val actorType: String = ApplicationConstants.SelectionManagerActorType
  private var selectedTile: Option[Tile] = Option.empty

  private val waitingEvents: Behavior[SelectionManagerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case SelectionManager.UpdateTileList(newTileList) =>
        this.tiles = newTileList

        Behaviors.same

      case SelectionManager.ReceivePuzzleUpdate(tupleList, isPuzzleCompleted) =>
        this.tiles.foreach(tile => tile.setCurrentPosition(tupleList(tile.getStartPosition)._2))
        controllerRef ! SynchronizeView(this.tiles, isPuzzleCompleted)

        Behaviors.same

      case SelectionManager.SelectTile(tile) =>
        if(selectedTile.nonEmpty) {
          swap(selectedTile.get, tile)
          selectedTile = Option.empty
          tiles.foreach(t => log(t.toString))
          controllerRef ! SendUpdate(tiles, tiles.forall(tile => tile.isInRightPlace))
        }
        else
          selectedTile = Option.apply(tile)

        Behaviors.same
    }
  }

  private def swap(t1:Tile, t2:Tile): Unit = {
    val pos = t1.getCurrentPosition
    t1.setCurrentPosition(t2.getCurrentPosition)
    t2.setCurrentPosition(pos)

    log(t2.currentPosition.toString + " " + t1.currentPosition.toString)
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}

