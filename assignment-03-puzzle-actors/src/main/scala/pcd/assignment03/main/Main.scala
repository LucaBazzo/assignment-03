package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.ConfigFactory
import pcd.assignment03.view.View.Display
import pcd.assignment03.view.{PuzzleBoard, View}

/** The object in which the program starts
 *
 */
object Main {

  private val n = 3
  private val m = 5

  def apply(initialConfig: Array[String]): Behavior[NotUsed] = Behaviors.setup { context =>

    val controller = context.spawn(Controller(), "Controller")
    val view = context.spawn(View(this.n, this.m, controller), "View")

    controller ! Initialize(view)
    view ! Display()

    Behaviors.empty
  }

  def main(args: Array[String]): Unit = {
    val port =
      if (args.isEmpty)
        Seq(25251)
      else
        args.toSeq.map(_.toInt).head

    val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      """).withFallback(ConfigFactory.load())

    ActorSystem(Main(args), "PuzzleActorSystem", config)
  }

}
