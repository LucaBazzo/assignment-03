package pcd.assignment03.management

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.ControllerMessage
import pcd.assignment03.management.MasterActor.{MasterMessage, Start, StopComputation}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.{ViewMessage}

import java.io.File

object MasterActor {

  sealed trait MasterMessage
  case class Start(pdfDirectory: File, forbidden: File, nWords: Int) extends MasterMessage
  case class Error() extends MasterMessage

  case class StopComputation() extends MasterMessage

  case class WorkEnded() extends MasterMessage

  def apply(controller: ActorRef[ControllerMessage],
            view: ActorRef[ViewMessage]): Behavior[MasterMessage] =
    Behaviors.setup { context =>
      new MasterActor(context, controller, view).standby
    }
}

/** Actor that controls the execution flow of the application
 *
 * @param context the actor context
 * @param controller reference to the controller in order to notify the completed result at the end
 * @param view the view to interact with
 *
 */
class MasterActor(val context: ActorContext[MasterMessage],
                  val controller: ActorRef[ControllerMessage],
                  val view: ActorRef[ViewMessage]) {

  private val actorType: String = ApplicationConstants.MasterActorType

  private val standby: Behavior[MasterMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case Start(pdfDirectory, forbidden, nWords) =>

        gettingPDF

      case StopComputation() =>
        log("Already in standby")
        Behaviors.same

      case _ =>
        log("ERROR")
        Behaviors.stopped
    }
  }

  private val gettingPDF: Behavior[MasterMessage] = Behaviors.receive { (_, message) =>
    message match {

      case StopComputation() =>
        log("Interrupted")
        //view ! ChangeState("Interrupted")

        standby
    }
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)

}
