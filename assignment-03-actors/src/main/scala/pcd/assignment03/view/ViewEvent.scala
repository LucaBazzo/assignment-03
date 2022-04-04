package pcd.assignment03.view

import akka.actor.typed.ActorRef
import pcd.assignment03.view.View.ViewMessage

class ViewEvent(view: ActorRef[ViewMessage]) {

  def notifyStart(pdfPath: String, ignoredFile: String, nWords: Int): Unit = {
    view ! View.StartProcess(pdfPath, ignoredFile, nWords)
  }

  def notifyStop(): Unit = {
    view ! View.StopProcess()
  }

}
