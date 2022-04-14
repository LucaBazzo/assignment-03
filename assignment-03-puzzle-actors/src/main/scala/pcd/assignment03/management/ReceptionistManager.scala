package pcd.assignment03.management

import akka.actor.Status.{Failure, Success}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import pcd.assignment03.CborSerializable
import pcd.assignment03.main.{ControllerMessage, ReceiveUpdate}
import pcd.assignment03.management.ReceptionistManager.{Add, ExpandChange, MessageExchange, ReceptionistManagerMessage, Swap, actorSet}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.Tile

import scala.concurrent.duration.DurationInt

object ReceptionistManager {

  trait ReceptionistManagerMessage
  case class Add(newNode: Set[ActorRef[ReceptionistManagerMessage]]) extends ReceptionistManagerMessage
  case class Swap(tileList: List[(Int, Int)], puzzleCompleted: Boolean) extends ReceptionistManagerMessage
  case class ExpandChange(tileList: List[(Int, Int)], puzzleCompleted: Boolean, replyTo: ActorRef[ReceptionistManagerMessage]) extends ReceptionistManagerMessage with CborSerializable
  case class MessageExchange(message: String) extends ReceptionistManagerMessage

  val ReceptionistServiceKey: ServiceKey[ReceptionistManagerMessage] = ServiceKey[ReceptionistManagerMessage]("ReceptionNode")
  var actorSet: Set[ActorRef[ReceptionistManagerMessage]] = Set.empty

  def apply(tiles: List[Tile], controllerRef: ActorRef[ControllerMessage]): Behavior[ReceptionistManagerMessage] =
    Behaviors.setup { ctx =>

      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case ReceptionistServiceKey.Listing(workers) =>
          Add(workers)
      }
      ctx.system.receptionist ! Receptionist.Subscribe(ReceptionistServiceKey, subscriptionAdapter)
      ctx.system.receptionist ! Receptionist.Register(ReceptionistServiceKey, ctx.self)

      new ReceptionistManager(tiles, controllerRef).waitingEvents
    }
}

class ReceptionistManager(val tiles: List[Tile], val controllerRef: ActorRef[ControllerMessage]){

  private val actorType: String = ApplicationConstants.ReceptionistManagerActorType

  private val waitingEvents: Behavior[ReceptionistManagerMessage] = Behaviors.receive { (ctx, message) =>
    message match {

      case Add(worker) => log("UN ANOTHER")
        if(worker.nonEmpty)
          actorSet = worker
        log(worker.toString())

      case Swap(tileList, isPuzzleCompleted) => tileList.foreach(t => log(t.toString()))
        implicit val timeout: Timeout = 5.seconds
        actorSet.foreach(w => if (w.path.toString.contains("127.0.0.1")) {
          ctx.ask(w, ExpandChange(tileList, isPuzzleCompleted, _)) {
          //case Success(resp) => UpdatePuzzle(resp)
          //case Failure(ex) =>
          case _ => MessageExchange("sent")
        }})

      case ExpandChange(tileList, isPuzzleCompleted, _) => controllerRef ! ReceiveUpdate(tileList, isPuzzleCompleted)

      case MessageExchange(str) => log(str)

      case _ => log("Bubba")
    }
    Behaviors.same
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
