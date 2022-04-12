package pcd.assignment03

import pcd.assignment03.view.{Tile, ViewEvent}

import java.util

object SelectionManager{
  trait Listener{
    def onSwapPerformed():Unit
  }
}

class SelectionManager(val viewEvent: ViewEvent, val tiles: util.List[Tile]){

  private var selectedTile:Option[Tile]=Option.empty

  def selectTile(tile:Tile,listener:SelectionManager.Listener):Unit={
    if(selectedTile.nonEmpty){
      swap(selectedTile.get,tile)
      listener.onSwapPerformed()
      selectedTile = Option.empty
    }
    else{
      selectedTile=Option.apply(tile)
    }
  }

  private def swap(t1:Tile,t2:Tile):Unit={
    val pos=t1.getCurrentPosition
    t1.setCurrentPosition(t2.getCurrentPosition)
    t2.setCurrentPosition(pos)
    viewEvent.notifySwap(t1.getCurrentPosition, t2.getCurrentPosition());
  }

  def isPuzzleCompleted: Boolean = {
    val result: Boolean = tiles.stream().allMatch(tile => tile.isInRightPlace)//tiles.forall(tile => tile.isInRightPlace)
    if (result)
      viewEvent.puzzleCompleted()

    result
  }

}

