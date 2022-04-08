package pcd.assignment03

import pcd.assignment03.view.PuzzleBoard

object Main {

  private val n = 3
  private val m = 5

  def main(args:Array[String]): Unit = {

    val imagePath: String = "src/main/scala/pcd/assignment03/bletchley-park-mansion.jpg"
    val puzzle: PuzzleBoard = new PuzzleBoard(n, m, imagePath)
    puzzle.setVisible(true)
  }
}