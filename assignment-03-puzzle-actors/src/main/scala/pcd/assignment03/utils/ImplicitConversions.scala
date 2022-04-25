package pcd.assignment03.utils


import pcd.assignment03.view.TileProperties

import java.util

/** Implicit conversions object
 *
 */
object ImplicitConversions {

  implicit def javaListToScalaList[T](javaList: util.List[T]): List[T] = {
    var scalaList: List[T] = List.empty
    javaList.forEach(value => scalaList = value :: scalaList)
    scalaList
  }

  implicit def scalaListToJavaList[T](scalaList: List[T]): util.List[T] = {
    val javaList: util.List[T] = new util.ArrayList[T]()
    scalaList.foreach(tile => javaList.add(tile))
    javaList
  }

  implicit def TileListToIntPairList(tileList: List[TileProperties]): List[(Int, Int)] = {
    var intList: List[(Int, Int)] = List.empty
    tileList.foreach(tile => intList = Tuple2(tile.getStartPosition, tile.getCurrentPosition) :: intList)
    intList.sortBy(_._1)
  }

}
