package pcd.assignment03.utils


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

}
