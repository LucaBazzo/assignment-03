package pcd.assignment03.view

import akka.actor.typed.ActorRef
import pcd.assignment03.view.View.ViewMessage

/** Intermediary between the GUI written in java and the View actor written in scala
 *
 */
class ViewEvent(view: ActorRef[ViewMessage]) {

  /** Notifies the actor view of the user-side start event
   *
   * @param pdfPath the path where the pdf are contained
   * @param ignoredFile the path where the ignored.txt is contained
   * @param nWords the number of most frequent words to obtain
   */
  def notifySwap(firstTilePosition: Int, secondTilePosition: Int): Unit = {
    view ! View.SwapEvent(firstTilePosition, secondTilePosition)
  }

  def puzzleCompleted(): Unit = {
    view ! View.PuzzleCompleted()
  }

}
