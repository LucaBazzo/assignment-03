package pcd.assignment03.view

import java.awt.Image

class TileProperties(val originalPosition: Int, var currentPosition: Int) extends Comparable[TileProperties]{

  val startPosition: Int = currentPosition

  def isInRightPlace: Boolean = currentPosition == originalPosition

  def getCurrentPosition: Int = currentPosition

  def getStartPosition: Int = startPosition

  def setCurrentPosition(newPosition: Int): Unit = currentPosition = newPosition

  override def compareTo(other: TileProperties): Int = this.currentPosition.compareTo(other.currentPosition)

  override def toString = s"Tile($originalPosition, $currentPosition, $isInRightPlace)"
}

class Tile(override val originalPosition: Int, currentPosition: Int, val image: Image)
  extends TileProperties(originalPosition, currentPosition) {

  def getImage: Image = image
}
