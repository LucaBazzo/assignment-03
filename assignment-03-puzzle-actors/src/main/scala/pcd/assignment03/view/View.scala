package pcd.assignment03.view

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.{ControllerMessage, Initialize, TileSelected}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.ViewMessage
import pcd.assignment03.utils.ImplicitConversions._

object View {

  sealed trait ViewMessage
  case class Display() extends ViewMessage
  case class TileSelected(tile: Tile) extends ViewMessage
  case class UpdateView(tileList: List[Tile], isPuzzleCompleted: Boolean) extends ViewMessage


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
    case View.Display() =>
      controller ! Initialize(this.puzzleBoard.getTileList, context.self)
      javax.swing.SwingUtilities.invokeLater(() => puzzleBoard.setVisible(true))
      Behaviors.same

    case View.TileSelected(tile) =>
      controller ! TileSelected(tile)
      Behaviors.same

    case View.UpdateView(tileList, isPuzzleCompleted) =>
      this.puzzleBoard.UpdatePuzzle(tileList)
      if(isPuzzleCompleted)
        this.puzzleBoard.PuzzleCompleted()
      Behaviors.same
  }
}
