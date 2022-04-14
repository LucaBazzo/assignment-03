package pcd.assignment03.view

import java.awt.Image

class Tile(val image: Image, val originalPosition: Int, var currentPosition: Int) extends Comparable[Tile]{

  def getImage: Image = image

  def isInRightPlace: Boolean = currentPosition == originalPosition

  def getCurrentPosition: Int = currentPosition

  def setCurrentPosition(newPosition: Int): Unit = currentPosition = newPosition

  override def compareTo(other: Tile): Int = this.currentPosition.compareTo(other.currentPosition)

  override def toString = s"Tile($originalPosition, $currentPosition, $isInRightPlace)"
}
