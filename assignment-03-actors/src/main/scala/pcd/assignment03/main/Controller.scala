package pcd.assignment03.main

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.Command
import pcd.assignment03.main.View.ViewMessage
import pcd.assignment03.tasks.MasterActor
import pcd.assignment03.tasks.MasterActor.{MasterMessage, Start, StopComputation}

import java.io.File

sealed trait ControllerMessage
case class StartProcess(pdfPath: String, ignoredPath: String, nWords: Int, viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class StopProcess() extends ControllerMessage

object Controller {

  private val stopMonitor = new StopMonitor()
  private var masterActor: ActorRef[MasterMessage] = _

  def apply(wordsBag: ActorRef[Command]): Behavior[ControllerMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case StartProcess(pdfPath, ignoredPath, nWords, viewRef) =>
        val directory: File = new File(pdfPath)
        val forbidden: File = new File(ignoredPath)

        val numTasks: Int = Runtime.getRuntime.availableProcessors() + 1

        this.masterActor = ctx.spawn(MasterActor(viewRef, directory, forbidden, wordsBag,
          stopMonitor, numTasks, nWords), "Master")
        this.masterActor ! Start()

      case StopProcess() => if(this.masterActor != null) this.masterActor ! StopComputation()
    }

    Behaviors.same
  }
}
