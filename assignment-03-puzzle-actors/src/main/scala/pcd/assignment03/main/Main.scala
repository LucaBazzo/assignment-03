package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View
import pcd.assignment03.view.View.Display

/** The object in which the program starts
 *
 */
object Main {

  private val nRows = ApplicationConstants.NRows
  private val nColumns = ApplicationConstants.NColumns

  private val defaultPort = ApplicationConstants.DefaultPort

  def apply(initialConfig: Array[String]): Behavior[NotUsed] = Behaviors.setup { context =>

    Cluster(context.system)

    val controller = context.spawn(Controller(initialConfig.toSeq.map(_.toInt).head), "Controller")
    val view = context.spawn(View(this.nRows, this.nColumns, controller), "View")

    controller ! RegisterView(view)

    Behaviors.empty
  }

  def main(args: Array[String]): Unit = {
    val port =
      if (args.isEmpty)
        Seq(this.defaultPort)
      else
        args.toSeq.map(_.toInt).head

    val config = ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port=$port
      """).withFallback(ConfigFactory.load())

    ActorSystem(Main(args), "PuzzleActorSystem", config)
  }

}
