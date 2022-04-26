package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.typed.Cluster
import akka.remote.RemoteTransportException
import com.typesafe.config.ConfigFactory
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View
import pcd.assignment03.view.View.Initialize

/** The object in which the program starts
 *
 */
object Main {

  private val nRows = ApplicationConstants.NRows
  private val nColumns = ApplicationConstants.NColumns

  private val defaultPort = ApplicationConstants.DefaultPort

  def apply(port: Int): Behavior[NotUsed] = Behaviors.setup { context =>

    Cluster(context.system)

    val controller = context.spawn(Controller(port), "Controller")
    val view = context.spawn(View(this.nRows, this.nColumns, controller), "View")

    view ! Initialize()

    Behaviors.empty
  }

  def main(args: Array[String]): Unit = {
    val port: Int =
      if (args.isEmpty)
        this.defaultPort
      else
        args.toSeq.map(_.toInt).head

    this.startCluster(port)

  }

  def startCluster(port: Int): Unit = {
    var tempPort = port
    var isPortValid = false

    while(!isPortValid) {
      try {
        val config = ConfigFactory.parseString(ApplicationConstants.PortConfiguration + tempPort).withFallback(ConfigFactory.load())
        ActorSystem(Main(tempPort), "PuzzleActorSystem", config)
        isPortValid = true
      }
      catch {
        case _: RemoteTransportException =>
          tempPort += 1
      }
    }
  }

}
