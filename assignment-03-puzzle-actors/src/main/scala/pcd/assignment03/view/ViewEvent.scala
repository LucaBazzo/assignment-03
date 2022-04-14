package pcd.assignment03.view

import akka.actor.typed.ActorRef
import pcd.assignment03.view.View.ViewMessage

/** Intermediary between the GUI written in java and the View actor written in scala
 *
 */
class ViewEvent(view: ActorRef[ViewMessage]) {

  /** Notifies the actor view of the user-side swap event
   *
   */
  def notifySwap(firstTilePosition: Int, secondTilePosition: Int): Unit = {
    view ! View.SwapEvent(firstTilePosition, secondTilePosition)
  }

  def puzzleCompleted(): Unit = {
    view ! View.PuzzleCompleted()
  }

}
