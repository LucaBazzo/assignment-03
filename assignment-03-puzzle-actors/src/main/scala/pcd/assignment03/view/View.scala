package pcd.assignment03.view

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.Controller.{ControllerMessage, Initialize, TileSelected}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.utils.ImplicitConversions._
import pcd.assignment03.view.View.ViewMessage

object View {

  sealed trait ViewMessage

  case class Initialize() extends ViewMessage
  case class Display() extends ViewMessage
  case class DisplayWithTileset(tileList: List[TileProperties], isPuzzleCompleted: Boolean) extends ViewMessage

  case class TileSelected(tile: Tile) extends ViewMessage
  case class UpdateView(tileList: List[TileProperties], isPuzzleCompleted: Boolean) extends ViewMessage

  def apply(nRows: Int, nColumns: Int, controller: ActorRef[ControllerMessage]): Behavior[ViewMessage] =
    Behaviors.setup { ctx =>
      new View(ctx, nRows, nColumns, controller).standby
    }
}

/** Manages the view part
 *
 * @param context the actor context
 * @param nRows the puzzle's rows
 * @param nColumns the puzzle's columns
 * @param controller reference to the controller, in order to send him the user events
 */
class View(val context: ActorContext[ViewMessage], val nRows: Int, val nColumns: Int,
           controller: ActorRef[ControllerMessage]) {

  private val viewEvent: ViewEvent = new ViewEvent(context.self)
  private val imagePath: String = ApplicationConstants.ImagePath
  private val puzzleBoard: PuzzleBoard = new PuzzleBoard(nRows, nColumns, imagePath, viewEvent)

  private val standby: Behavior[ViewMessage] = Behaviors.receiveMessagePartial {

    case View.Initialize() =>
      controller ! Initialize(this.context.self, this.puzzleBoard.getTileList)
      Behaviors.same

    case View.Display() =>
      this.display()
      idle

    case View.DisplayWithTileset(tileList, isPuzzleCompleted) =>
      this.update(tileList, isPuzzleCompleted)
      this.display()
      idle
  }

  private val idle: Behavior[ViewMessage] = Behaviors.receiveMessagePartial {

    case View.TileSelected(tile) =>
      controller ! TileSelected(tile)
      Behaviors.same

    case View.UpdateView(tileList, isPuzzleCompleted) =>
      this.update(tileList, isPuzzleCompleted)
      Behaviors.same
  }

  private def display(): Unit = javax.swing.SwingUtilities.invokeLater(() => puzzleBoard.setVisible(true))

  private def update(tileList: List[TileProperties], isPuzzleCompleted: Boolean): Unit = {
    this.puzzleBoard.UpdatePuzzle(tileList)
    if(isPuzzleCompleted)
      this.puzzleBoard.PuzzleCompleted()
  }
}
