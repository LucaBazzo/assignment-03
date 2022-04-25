package pcd.assignment03.management

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.Controller.{ControllerMessage, SendUpdate, SynchronizeView}
import pcd.assignment03.management.SelectionManager.SelectionManagerMessage
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.TileProperties

object SelectionManager {

  trait SelectionManagerMessage

  case class SelectTile(tile: TileProperties) extends SelectionManagerMessage
  case class ReceivePuzzleUpdate(tupleList: List[(Int, Int)]) extends  SelectionManagerMessage

  def apply(tiles: List[TileProperties], controllerRef: ActorRef[ControllerMessage]): Behavior[SelectionManagerMessage] =
    Behaviors.setup { _ =>
      new SelectionManager(tiles, controllerRef).waitingEvents
    }
}

class SelectionManager(var tiles: List[TileProperties],
                       val controllerRef: ActorRef[ControllerMessage]){

  private val actorType: String = ApplicationConstants.SelectionManagerActorType
  private var selectedTile: Option[TileProperties] = Option.empty

  private val waitingEvents: Behavior[SelectionManagerMessage] = Behaviors.receive { (_, message) =>
    message match {

      case SelectionManager.ReceivePuzzleUpdate(tupleList) =>
        this.tiles.foreach(tile => tile.setCurrentPosition(tupleList(tile.getStartPosition)._2))
        controllerRef ! SynchronizeView(this.tiles, isPuzzleCompleted(this.tiles))

        Behaviors.same

      case SelectionManager.SelectTile(tile) =>
        if(selectedTile.nonEmpty) {
          this.swap(selectedTile.get, tile)
          selectedTile = Option.empty
          controllerRef ! SendUpdate(tiles, isPuzzleCompleted(this.tiles))
        }
        else
          selectedTile = Option.apply(tile)

        Behaviors.same
    }
  }

  private def swap(firstTile: TileProperties, secondTile: TileProperties): Unit = {
    val pos = firstTile.getCurrentPosition
    firstTile.setCurrentPosition(secondTile.getCurrentPosition)
    secondTile.setCurrentPosition(pos)

    log(secondTile.currentPosition.toString + " " + firstTile.currentPosition.toString)
  }

  private def isPuzzleCompleted(tiles: List[TileProperties]): Boolean =
    tiles.forall(tile => tile.isInRightPlace)

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}

