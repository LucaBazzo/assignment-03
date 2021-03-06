package pcd.assignment03.utils

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/** Utility to manage application constants more easily
 *
 */
object ApplicationConstants {

  val DefaultPort: Int = 25251

  val DisplayWeight: Int = 700
  val DisplayHeight: Int = 400

  val NRows: Int = 3
  val NColumns: Int = 5

  val ImagePath: String = "src/main/resources/bletchley-park-mansion.jpg"

  val DefaultSeed: Int = 1

  val SelectionManagerActorType: String = "Selection Manager Actor"
  val ReceptionistManagerActorType: String = "Receptionist Manager Actor"

  val PortConfiguration: String = "akka.remote.artery.canonical.port="
  val PollingDelay: FiniteDuration = 5.seconds
  val PollingDebug: Boolean = false
}
