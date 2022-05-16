package pcd.assignment03.utils

import pcd.assignment03.view.Pair

import java.util

/** Implicit conversions for compatibility between Scala and Java
 *
 */
object ImplicitConversions {

  implicit def tupleToPair[X, Y](tuple: (X, Y)): Pair[X, Y] =
    new Pair[X, Y](tuple._1, tuple._2)

  implicit def scalaListToJavaList[T](scalaList: List[T]): util.List[T] = {
    val javaList: util.List[T] = new util.ArrayList[T]()
    scalaList.foreach(tile => javaList.add(tile))
    javaList
  }

}
