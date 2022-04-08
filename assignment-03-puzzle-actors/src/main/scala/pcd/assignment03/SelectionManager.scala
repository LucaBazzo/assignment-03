package pcd.assignment03

import pcd.assignment03.view.Tile

object SelectionManager{
  trait Listener{
    def onSwapPerformed():Unit
  }
}

class SelectionManager{

  private var selectionActive=false
  private var selectedTile:Tile=null

  def selectTile(tile:Tile,listener:SelectionManager.Listener):Unit={
    if(selectionActive){
      selectionActive=false
      swap(selectedTile,tile)
      listener.onSwapPerformed()
    }
    else{
      selectionActive=true
      selectedTile=tile
    }
  }

  private def swap(t1:Tile,t2:Tile):Unit={
    val pos=t1.getCurrentPosition
    t1.setCurrentPosition(t2.getCurrentPosition)
    t2.setCurrentPosition(pos)
  }

}

