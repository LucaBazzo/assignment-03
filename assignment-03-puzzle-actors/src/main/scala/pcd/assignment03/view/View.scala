package pcd.assignment03.view

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.{ControllerMessage, SwapEvent, StopProcess}
import pcd.assignment03.view.View.ViewMessage

import java.util

object View {

  sealed trait ViewMessage
  case class Display() extends ViewMessage
  case class SwapEvent(firstTilePosition: Int, secondTilePosition: Int) extends ViewMessage
  case class PuzzleCompleted() extends ViewMessage
  case class UpdateResult(count: Int, result: List[(String, Integer)] , workEnded: Boolean) extends ViewMessage
  case class ChangeState(state: String) extends ViewMessage


  def apply(n: Int, m: Int, controller: ActorRef[ControllerMessage]): Behavior[ViewMessage] =
    Behaviors.setup { ctx =>
      new View(ctx, n, m, controller).standby
    }
}

/** Manages the view part
 *
 * @param context the actor context
 * @param n the view's width
 * @param m the view's height
 * @param controller reference to the controller, in order to send him the user events
 */
class View(val context: ActorContext[ViewMessage], val n: Int, val m: Int,
           controller: ActorRef[ControllerMessage]) {

  private val viewEvent: ViewEvent = new ViewEvent(context.self)
  val imagePath: String = "src/main/resources/bletchley-park-mansion.jpg"
  val gui: PuzzleBoard = new PuzzleBoard(n, m, imagePath, viewEvent)

  private val standby: Behavior[ViewMessage] = Behaviors.receiveMessagePartial {
    case View.Display() =>
      javax.swing.SwingUtilities.invokeLater(() => gui.setVisible(true))
      Behaviors.same

    case View.UpdateResult(count, result, workEnded) =>

      Behaviors.same

    case View.ChangeState(state) =>
      Behaviors.same

    case View.SwapEvent(firstTilePosition, secondTilePosition) =>
      controller ! SwapEvent(firstTilePosition, secondTilePosition)
      Behaviors.same

    case View.PuzzleCompleted() =>
      controller ! StopProcess()
      Behaviors.same
  }
}
