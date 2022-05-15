package pcd.assignment03.management

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.CborSerializable
import pcd.assignment03.management.ReceptionistManager._
import pcd.assignment03.management.SelectionManager.{ReceivePuzzleUpdate, SelectionManagerMessage}
import pcd.assignment03.utils.ApplicationConstants

import scala.util.Random

/** Companion Object for [[ReceptionistManager]], contains the messages accepted by Receptionist actors
 *
 */
object ReceptionistManager {

  trait ReceptionistManagerMessage
  case class Add(workerSet: Set[ActorRef[ReceptionistManagerMessage]]) extends ReceptionistManagerMessage

  case class RequestTileset(replyTo: ActorRef[ReceptionistManagerMessage]) extends ReceptionistManagerMessage with CborSerializable
  case class ReceiveTileset(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage with CborSerializable

  case class InitializeTileList(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage
  case class TilesHasChanged(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage
  case class ExpandChange(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage with CborSerializable
  case class SendClockInfo() extends ReceptionistManagerMessage
  case class CheckClockIntegrity(clock: Int, tileList: List[(Int, Int)], sourcePort: Int) extends ReceptionistManagerMessage with CborSerializable

  val ReceptionistServiceKey: ServiceKey[ReceptionistManagerMessage] = ServiceKey[ReceptionistManagerMessage]("ReceptionNode")

  def apply(selectionRef: ActorRef[SelectionManagerMessage],
            port: Int): Behavior[ReceptionistManagerMessage] =
    Behaviors.setup { ctx =>
      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case ReceptionistServiceKey.Listing(workers) =>
          Add(workers)
      }
      ctx.system.receptionist ! Receptionist.Subscribe(ReceptionistServiceKey, subscriptionAdapter)
      ctx.system.receptionist ! Receptionist.Register(ReceptionistServiceKey, ctx.self)

      new ReceptionistManager(port, selectionRef).waitingEvents
    }
}

/** Manages the comunications between this and the other nodes of the cluster
 *
 * @param port the port of this node
 * @param selectionRef reference to the selection manager actor of this node
 */
class ReceptionistManager(val port: Int, val selectionRef: ActorRef[SelectionManagerMessage]){

  private val actorType: String = ApplicationConstants.ReceptionistManagerActorType
  private var tileList: List[(Int, Int)] = _

  var actorSet: Set[ActorRef[ReceptionistManagerMessage]] = Set.empty

  var internalClock: Int = 0
  var polling: Boolean = true

  var hasStarted: Boolean = false

  private val waitingEvents: Behavior[ReceptionistManagerMessage] = Behaviors.receive { (ctx, message) =>
    if (polling) {
      //start polling for checking clocks different between cluster nodes
      ctx.scheduleOnce(ApplicationConstants.PollingDelay, ctx.self, SendClockInfo())
      polling = false
    }
    message match {

      case Add(workerSet) =>
        log("Worker Set Updated")
        if(workerSet.nonEmpty)
          actorSet = workerSet
        log(workerSet.toString())

        if(!hasStarted){
          if (isFirst) {
            this.tileList = Random.shuffle(this.tileList)
            selectionRef ! ReceivePuzzleUpdate(this.tileList)
            this.hasStarted = true
          }
          else if (actorSet.exists(a => checkIP(a))) {
              actorSet.filter(a => checkIP(a)).head ! RequestTileset(ctx.self)
            this.hasStarted = true
          }
        }

      case InitializeTileList(tileList) => this.tileList = tileList

      case TilesHasChanged(tileList) =>
        this.internalClock += 1
        this.tileList = tileList
        actorSet.foreach(w => if (checkIP(w)) {
          w ! ExpandChange(tileList) })

      case ExpandChange(tileList) =>
        this.internalClock += 1
        this.tileList = tileList
        selectionRef ! ReceivePuzzleUpdate(tileList)

      case RequestTileset(from) => from ! ReceiveTileset(this.tileList)

      case ReceiveTileset(tileList) =>
        this.tileList = tileList
        selectionRef ! ReceivePuzzleUpdate(tileList)

      case SendClockInfo() =>
        if(ApplicationConstants.PollingDebug) log("DEBUG - Polling - internal clock " + internalClock)
        actorSet.foreach(p => if (checkIP(p)) {
          p ! CheckClockIntegrity(this.internalClock, this.tileList, this.port) })
        ctx.scheduleOnce(ApplicationConstants.PollingDelay, ctx.self, SendClockInfo())

      /** Check if the internal clock is different (but not greater) from the one received from other nodes,
       * in that case it means that this node's tileset isn't the latest in the cluster and needs to be updated.
       * If clocks are equals, tileset are checked and, if different, it means that some updates went lost between those nodes
       * and so it is prioritized the one coming from the node with lower port number. In this way, only the nodes with greater port number will be updated
       *
       */
      case CheckClockIntegrity(clock, tileList, sourcePort) =>
        if(ApplicationConstants.PollingDebug) log("DEBUG - check clock integrity: clock received - " + clock + " port received - " + sourcePort )
        if ((this.internalClock < clock) || (this.internalClock == clock && this.tileList != tileList && this.port > sourcePort)){
          if(ApplicationConstants.PollingDebug) log("DEBUG - Update performed")
          this.tileList = tileList
          this.internalClock = clock
          selectionRef ! ReceivePuzzleUpdate(this.tileList)
        }

      case _ => log("Error")
    }
    Behaviors.same
  }

  private def isFirst: Boolean = actorSet.size == 1 && !checkIP(actorSet.head) && port == ApplicationConstants.DefaultPort

  private def checkIP[T](ref: ActorRef[T]): Boolean = ref.path.toString.contains("127.0.0.1")

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
