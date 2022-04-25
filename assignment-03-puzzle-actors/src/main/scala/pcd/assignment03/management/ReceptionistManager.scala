package pcd.assignment03.management

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import pcd.assignment03.CborSerializable
import pcd.assignment03.main.Controller.{ControllerMessage, DisplayView}
import pcd.assignment03.management.ReceptionistManager._
import pcd.assignment03.management.SelectionManager.{ReceivePuzzleUpdate, SelectionManagerMessage}
import pcd.assignment03.utils.ApplicationConstants

import scala.concurrent.duration.DurationInt

object ReceptionistManager {

  trait ReceptionistManagerMessage
  case class Add(workerSet: Set[ActorRef[ReceptionistManagerMessage]]) extends ReceptionistManagerMessage

  case class RequestTileset(replyTo: ActorRef[ReceptionistManagerMessage]) extends ReceptionistManagerMessage with CborSerializable
  case class ReceiveTileset(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage with CborSerializable

  case class InitializeTileList(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage
  case class TilesHasChanged(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage
  case class ExpandChange(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage with CborSerializable

  val ReceptionistServiceKey: ServiceKey[ReceptionistManagerMessage] = ServiceKey[ReceptionistManagerMessage]("ReceptionNode")

  def apply(controllerRef: ActorRef[ControllerMessage], selectionRef: ActorRef[SelectionManagerMessage],
            port: Int): Behavior[ReceptionistManagerMessage] =
    Behaviors.setup { ctx =>
      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case ReceptionistServiceKey.Listing(workers) =>
          Add(workers)
      }
      ctx.system.receptionist ! Receptionist.Subscribe(ReceptionistServiceKey, subscriptionAdapter)
      ctx.system.receptionist ! Receptionist.Register(ReceptionistServiceKey, ctx.self)

      new ReceptionistManager(port, controllerRef, selectionRef).waitingEvents
    }
}

class ReceptionistManager(val port: Int, controllerRef: ActorRef[ControllerMessage],
                          val selectionRef: ActorRef[SelectionManagerMessage]){

  private val actorType: String = ApplicationConstants.ReceptionistManagerActorType
  private var tileList: List[(Int, Int)] = _

  var actorSet: Set[ActorRef[ReceptionistManagerMessage]] = Set.empty

  private val waitingEvents: Behavior[ReceptionistManagerMessage] = Behaviors.receive { (ctx, message) =>
    message match {

      case Add(workerSet) =>
        log("Worker Set Updated")
        if(workerSet.nonEmpty)
          actorSet = workerSet
        log(workerSet.toString())

        if (actorSet.size == 1 && !checkIP(actorSet.head) && port == ApplicationConstants.DefaultPort) {
          controllerRef ! DisplayView()
        } else {
          if (actorSet.exists(a => checkIP(a)))
            actorSet.filter(a => checkIP(a)).head ! RequestTileset(ctx.self)
        }

      case InitializeTileList(tileList) => this.tileList = tileList

      case TilesHasChanged(tileList) =>
        this.tileList = tileList
        implicit val timeout: Timeout = 5.seconds
        actorSet.foreach(w => if (checkIP(w)) {
          w ! ExpandChange(tileList) })

      case ExpandChange(tileList) =>
        selectionRef ! ReceivePuzzleUpdate(tileList)

      case RequestTileset(from) => from ! ReceiveTileset(this.tileList)

      case ReceiveTileset(tileList) =>
        this.tileList = tileList
        selectionRef ! ReceivePuzzleUpdate(tileList)

      case _ => log("Error")
    }
    Behaviors.same
  }

  private def checkIP[T](ref: ActorRef[T]): Boolean = ref.path.toString.contains("127.0.0.1")

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
