package pcd.assignment03.main

import pcd.assignment03.concurrency.{StopMonitor, WordsBagFilling}
import pcd.assignment03.tasks.ServiceTask
import pcd.assignment03.view.View

import java.io.File
import java.util.concurrent.{ExecutorService, Executors}

trait Process {
  def startProcess(pdfPath: String, ignoredFile: String, nWords: Int): Unit

  def stopProcess(): Unit
}

class Controller(var view: View, var wordsBag: WordsBagFilling) extends Process {

  private val stopMonitor = new StopMonitor()

  override def startProcess(pdfPath: String, ignoredFile: String, nWords: Int): Unit = {
    val directory: File = new File(pdfPath)
    val forbidden: File = new File(ignoredFile)

    val numTasks: Int = Runtime.getRuntime().availableProcessors() + 1

    val executor: ExecutorService = Executors.newSingleThreadExecutor();
    executor.execute(new ServiceTask("Master", view, directory, forbidden, wordsBag,
      stopMonitor, numTasks, nWords));
  }

  override def stopProcess(): Unit = stopMonitor.stop()
}
