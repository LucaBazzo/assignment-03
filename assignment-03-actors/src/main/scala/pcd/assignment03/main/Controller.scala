package pcd.assignment03.main

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, Start, StopComputation}
import pcd.assignment03.words.WordsBag.Command
import pcd.assignment03.view.View.ViewMessage

import java.io.File

sealed trait ControllerMessage
case class StartProcess(pdfPath: String, ignoredPath: String, nWords: Int, viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class StopProcess() extends ControllerMessage

object Controller {
  private var masterActor: ActorRef[MasterMessage] = _

  def apply(wordsBag: ActorRef[Command]): Behavior[ControllerMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case StartProcess(pdfPath, ignoredPath, nWords, viewRef) =>
        val directory: File = new File(pdfPath)
        val forbidden: File = new File(ignoredPath)

        val numTasks: Int = Runtime.getRuntime.availableProcessors() + 1

        this.masterActor = ctx.spawn(MasterActor(viewRef, directory, forbidden, wordsBag,
          numTasks, nWords), "Master")
        this.masterActor ! Start()

      case StopProcess() => if(this.masterActor != null) this.masterActor ! StopComputation()
    }

    Behaviors.same
  }
}
