package pcd.assignment03.view

import akka.actor.typed.ActorRef
import pcd.assignment03.view.View.ViewMessage

/** Intermediary between the GUI written in java and the View actor written in scala
 *
 */
class ViewEvent(view: ActorRef[ViewMessage]) {

  /** Notifies the actor view of the user-side tile selected event
   *
   */
  def notifyTileSelected(tile: Tile): Unit = view ! View.TileSelected(tile)

}
