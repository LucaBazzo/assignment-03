package pcd.assignment03.view

import java.awt.Image

/** Represent a puzzle piece without the image associated
 *
 * @param originalPosition position occupied by the puzzle piece prior the shuffling
 * @param currentPosition actual position of puzzle piece
 */
class TileProperties(val originalPosition: Int, var currentPosition: Int) extends Comparable[TileProperties]{

  val startPosition: Int = currentPosition

  /**
   *
   * @return if the piece is in its original position
   */
  def isInRightPlace: Boolean = currentPosition == originalPosition

  def getCurrentPosition: Int = currentPosition

  /**
   *
   * @return the first position occupied by the puzzle piece post initial shuffling
   */
  def getStartPosition: Int = startPosition

  def setCurrentPosition(newPosition: Int): Unit = currentPosition = newPosition


  override def compareTo(other: TileProperties): Int = this.currentPosition.compareTo(other.currentPosition)

  override def toString = s"Tile($originalPosition, $currentPosition, $isInRightPlace)"
}

/** extension of [[TileProperties]] with the image associated
 *
 * @param originalPosition position occupied by the puzzle piece prior the shuffling
 * @param currentPosition actual position of puzzle piece
 * @param image image associated to the puzzle piece
 */
class Tile(override val originalPosition: Int, currentPosition: Int, val image: Image)
  extends TileProperties(originalPosition, currentPosition) {

  def getImage: Image = image
}
