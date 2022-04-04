package pcd.assignment03.view

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.utils.ImplicitConversions.tupleToPair
import pcd.assignment03.main.{ControllerMessage, StartProcess, StopProcess}
import pcd.assignment03.view.View.ViewMessage

import java.util

object View {

  sealed trait ViewMessage
  case class Display() extends ViewMessage
  case class StartProcess(pdfPath: String, ignoredPath: String, nWords: Int) extends ViewMessage
  case class StopProcess() extends ViewMessage
  case class UpdateResult(count: Int, result: List[(String, Integer)] , workEnded: Boolean) extends ViewMessage
  case class ChangeState(state: String) extends ViewMessage


  def apply(width: Int, height: Int, pdfPath: String, ignoredFile: String,
            nWords: Int, controller: ActorRef[ControllerMessage]): Behavior[ViewMessage] =
    Behaviors.setup { ctx =>
      new View(ctx, width, height, pdfPath, ignoredFile, nWords, controller).standby
    }
}


class View(val context: ActorContext[ViewMessage], val width: Int, val height: Int, val pdfPath: String,
           val ignoredFile: String, val nWords: Int, controller: ActorRef[ControllerMessage]) {

  private val event: ViewEvent = new ViewEvent(context.self)
  private val gui: ViewGUI = new ViewGUI(width, height, pdfPath, ignoredFile, nWords, event)

  private val standby: Behavior[ViewMessage] = Behaviors.receiveMessagePartial {
    case View.Display() =>
      javax.swing.SwingUtilities.invokeLater(() => gui.setVisible(true))
      Behaviors.same

    case View.UpdateResult(count, result, workEnded) =>
      //conversion of Scala List to Java list for the ViewGUI
      val w: util.List[Pair[String, Integer]] = new util.ArrayList[Pair[String, Integer]]()
      result.foreach(tuple => w.add(tuple))
      gui.updateResult(count, w, workEnded)
      Behaviors.same

    case View.ChangeState(state) =>
      gui.updateState(state)
      Behaviors.same

    case View.StartProcess(pdfPath, ignoredPath, nWords) =>
      controller ! StartProcess(pdfPath, ignoredPath, nWords)
      Behaviors.same

    case View.StopProcess() =>
      controller ! StopProcess()
      Behaviors.same
  }
}
