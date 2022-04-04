package pcd.assignment03.utils

import pcd.assignment03.view.Pair

/** Implicit conversions object
 *
 */
object ImplicitConversions {

  implicit def tupleToPair[X, Y](tuple: (X, Y)): Pair[X, Y] =
    new Pair[X, Y](tuple._1, tuple._2)

}
