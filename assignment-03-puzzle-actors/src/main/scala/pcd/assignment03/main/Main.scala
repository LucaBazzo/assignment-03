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

    this.startCluster()

  }

  /** Try many ports (starting from the default 25251) until a valid one is found and start a Cluster on that port
   *
   */
  def startCluster(): Unit = {
    var tempPort = this.defaultPort
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
