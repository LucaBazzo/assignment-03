package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.Command
import pcd.assignment03.main.View.ViewMessage
import pcd.assignment03.tasks.ServiceTask

import java.io.File
import java.util.concurrent.{ExecutorService, Executors}

sealed trait ControllerMessage
case class StartProcess(pdfPath: String, ignoredPath: String, nWords: Int, viewRef: ActorRef[ViewMessage]) extends ControllerMessage
case class StopProcess() extends ControllerMessage

object Controller {

  private val stopMonitor = new StopMonitor()

  def apply(wordsBag: ActorRef[Command], picker: ActorRef[Command],
            context: ActorContext[NotUsed]): Behavior[ControllerMessage] = Behaviors.receive { (_, message) =>
    message match {
      case StartProcess(pdfPath, ignoredPath, nWords, viewRef) =>
        val directory: File = new File(pdfPath)
        val forbidden: File = new File(ignoredPath)

        val numTasks: Int = Runtime.getRuntime.availableProcessors() + 1

        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.execute(new ServiceTask("Master", viewRef, directory, forbidden, wordsBag,
          stopMonitor, numTasks, nWords, picker, context))
      case StopProcess() => stopMonitor.stop()
    }

    Behaviors.same
  }
}
