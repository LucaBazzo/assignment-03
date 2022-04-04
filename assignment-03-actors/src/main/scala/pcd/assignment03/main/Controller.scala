package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, Start, StopComputation}
import pcd.assignment03.words.WordsBag.Command
import pcd.assignment03.view.View.ViewMessage

import java.io.File

sealed trait ControllerMessage
case class Initialize(viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class StartProcess(pdfPath: String, ignoredPath: String, nWords: Int) extends ControllerMessage
case class StopProcess() extends ControllerMessage
case class ProcessCompleted() extends ControllerMessage

object Controller {

  def apply(wordsBag: ActorRef[Command]): Behavior[ControllerMessage] =
    Behaviors.setup { ctx => new Controller(ctx, wordsBag).initializing }
}

class Controller(context: ActorContext[ControllerMessage], wordsBag: ActorRef[Command]) {
  private var masterActor: ActorRef[MasterMessage] = _

  private val initializing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case Initialize(viewRef) =>
        this.masterActor = context.spawn(MasterActor(context.self, viewRef, wordsBag), "Master")

        standby
    }
  }

  private val standby: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case StartProcess(pdfPath, ignoredPath, nWords) =>
        val directory: File = new File(pdfPath)
        val forbidden: File = new File(ignoredPath)

        this.masterActor ! Start(directory, forbidden, nWords)

        computing
    }
  }

  private val computing: Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case StopProcess() =>
        this.masterActor ! StopComputation()

        standby

      case ProcessCompleted() => standby
    }
  }
}
