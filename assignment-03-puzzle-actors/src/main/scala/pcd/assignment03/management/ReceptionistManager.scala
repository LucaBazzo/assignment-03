package pcd.assignment03.management

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import pcd.assignment03.CborSerializable
import pcd.assignment03.main.Controller.{ControllerMessage, DisplayView, DisplayViewWithTileset, ReceiveUpdate}
import pcd.assignment03.management.ReceptionistManager._
import pcd.assignment03.utils.ApplicationConstants

import scala.concurrent.duration.DurationInt

object ReceptionistManager {

  trait ReceptionistManagerMessage
  case class Add(newNode: Set[ActorRef[ReceptionistManagerMessage]]) extends ReceptionistManagerMessage
  case class RequestSeed(replyTo: ActorRef[ReceptionistManagerMessage]) extends ReceptionistManagerMessage with CborSerializable
  case class SendSeed(seed: Int, tileList: List[(Int, Int)], isPuzzleCompleted: Boolean) extends ReceptionistManagerMessage with CborSerializable

  case class InitializeTileList(tileList: List[(Int, Int)]) extends ReceptionistManagerMessage
  case class Swap(tileList: List[(Int, Int)], puzzleCompleted: Boolean) extends ReceptionistManagerMessage
  case class ExpandChange(tileList: List[(Int, Int)], puzzleCompleted: Boolean) extends ReceptionistManagerMessage with CborSerializable

  val ReceptionistServiceKey: ServiceKey[ReceptionistManagerMessage] = ServiceKey[ReceptionistManagerMessage]("ReceptionNode")

  def apply(controllerRef: ActorRef[ControllerMessage], port: Int): Behavior[ReceptionistManagerMessage] =
    Behaviors.setup { ctx =>
      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case ReceptionistServiceKey.Listing(workers) =>
          Add(workers)
      }
      ctx.system.receptionist ! Receptionist.Subscribe(ReceptionistServiceKey, subscriptionAdapter)
      ctx.system.receptionist ! Receptionist.Register(ReceptionistServiceKey, ctx.self)

      new ReceptionistManager(port, controllerRef).waitingEvents
    }
}

class ReceptionistManager(val port: Int, val controllerRef: ActorRef[ControllerMessage]){

  private val actorType: String = ApplicationConstants.ReceptionistManagerActorType
  private var seed: Int = 0
  private var tileList: List[(Int, Int)] = _
  private var isPuzzleCompleted: Boolean = false

  var actorSet: Set[ActorRef[ReceptionistManagerMessage]] = Set.empty

  private val waitingEvents: Behavior[ReceptionistManagerMessage] = Behaviors.receive { (ctx, message) =>
    message match {

      case Add(worker) =>
        log("UN ANOTHER")
        if(worker.nonEmpty)
          actorSet = worker
        log(worker.toString())

        if(seed == 0) {
          if (actorSet.size == 1 && !checkIP(actorSet.head) && port == ApplicationConstants.DefaultPort) {
            seed = scala.util.Random.nextInt() + 1
            controllerRef ! DisplayView(this.seed)
          } else {
            if (actorSet.exists(a => checkIP(a)))
              actorSet.filter(a => checkIP(a)).head ! RequestSeed(ctx.self)
          }
        }

      case InitializeTileList(tileList) => this.tileList = tileList

      case Swap(tileList, isPuzzleCompleted) =>
        this.tileList = tileList
        this.isPuzzleCompleted = isPuzzleCompleted
        implicit val timeout: Timeout = 5.seconds
        actorSet.foreach(w => if (checkIP(w)) {
          w ! ExpandChange(tileList, isPuzzleCompleted) })

      case ExpandChange(tileList, isPuzzleCompleted) => controllerRef ! ReceiveUpdate(tileList, isPuzzleCompleted)

      case RequestSeed(from) => from ! SendSeed(this.seed, this.tileList, this.isPuzzleCompleted)

      case SendSeed(seed, tileList, isPuzzleCompleted) =>
        this.seed = seed
        this.tileList = tileList
        this.isPuzzleCompleted = isPuzzleCompleted
      controllerRef ! DisplayViewWithTileset(seed, tileList, isPuzzleCompleted)

      case _ => log("Bubba")
    }
    Behaviors.same
  }

  private def checkIP[T](ref: ActorRef[T]): Boolean = ref.path.toString.contains("127.0.0.1")

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
