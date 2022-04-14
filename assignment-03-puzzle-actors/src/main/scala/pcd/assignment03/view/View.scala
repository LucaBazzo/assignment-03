package pcd.assignment03.view

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.{ControllerMessage, StopProcess, SwapEvent}
import pcd.assignment03.view.View.ViewMessage

object View {

  sealed trait ViewMessage
  case class Display() extends ViewMessage
  case class SwapEvent(firstTilePosition: Int, secondTilePosition: Int) extends ViewMessage
  case class PuzzleCompleted() extends ViewMessage


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
  val imagePath: String = "src/main/resources/bletchley-park-mansion.jpg"
  val gui: PuzzleBoard = new PuzzleBoard(nRows, nColumns, imagePath, viewEvent)

  private val standby: Behavior[ViewMessage] = Behaviors.receiveMessagePartial {
    case View.Display() =>
      javax.swing.SwingUtilities.invokeLater(() => gui.setVisible(true))
      Behaviors.same

    case View.SwapEvent(firstTilePosition, secondTilePosition) =>
      controller ! SwapEvent(firstTilePosition, secondTilePosition)
      Behaviors.same

    case View.PuzzleCompleted() =>
      controller ! StopProcess()
      Behaviors.same
  }
}
