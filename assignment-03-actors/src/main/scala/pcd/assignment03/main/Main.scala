package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View
import pcd.assignment03.view.View.Display

/** The object in which the program starts
 *
 */
object Main {

  private var pdfPath = ApplicationConstants.DefaultPDFPath
  private var ignoredPath = ApplicationConstants.DefaultIgnoredPath
  private var nWords = ApplicationConstants.DefaultNWords

  private val weight: Int = ApplicationConstants.DisplayWeight
  private val height: Int = ApplicationConstants.DisplayHeight

  def apply(initialConfig: Array[String]): Behavior[NotUsed] = Behaviors.setup { context =>

    if(initialConfig.length == 3) {
      this.pdfPath = initialConfig(0)
      this.ignoredPath = initialConfig(1)
      this.nWords = initialConfig(2).toInt
    }

    val controller = context.spawn(Controller(), "Controller")
    val view = context.spawn(View(this.weight, this.height, this.pdfPath,
      this.ignoredPath, this.nWords, controller), "View")

    controller ! Initialize(view)
    view ! Display()

    Behaviors.empty
  }



  def main(args:Array[String]): Unit = {
    ActorSystem(Main(args), "Concurrency")
  }

}
