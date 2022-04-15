package pcd.assignment03.management

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import pcd.assignment03.CborSerializable
import pcd.assignment03.main.{ControllerMessage, DisplayView, ReceiveUpdate}
import pcd.assignment03.management.ReceptionistManager._
import pcd.assignment03.utils.ApplicationConstants
import scala.concurrent.duration.DurationInt

object ReceptionistManager {

  trait ReceptionistManagerMessage
  case class Add(newNode: Set[ActorRef[ReceptionistManagerMessage]]) extends ReceptionistManagerMessage
  case class Swap(tileList: List[(Int, Int)], puzzleCompleted: Boolean) extends ReceptionistManagerMessage
  case class ExpandChange(tileList: List[(Int, Int)], puzzleCompleted: Boolean) extends ReceptionistManagerMessage with CborSerializable
  case class RequestSeed(replyTo: ActorRef[ReceptionistManagerMessage]) extends ReceptionistManagerMessage with CborSerializable
  case class SendSeed(seed: Int) extends ReceptionistManagerMessage with CborSerializable

  val ReceptionistServiceKey: ServiceKey[ReceptionistManagerMessage] = ServiceKey[ReceptionistManagerMessage]("ReceptionNode")
  var actorSet: Set[ActorRef[ReceptionistManagerMessage]] = Set.empty
  var port: Int = _

  def apply(controllerRef: ActorRef[ControllerMessage], port: Int): Behavior[ReceptionistManagerMessage] =
    Behaviors.setup { ctx =>
      this.port = port
      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case ReceptionistServiceKey.Listing(workers) =>
          Add(workers)
      }
      ctx.system.receptionist ! Receptionist.Subscribe(ReceptionistServiceKey, subscriptionAdapter)
      ctx.system.receptionist ! Receptionist.Register(ReceptionistServiceKey, ctx.self)

      new ReceptionistManager(controllerRef).waitingEvents
    }
}

class ReceptionistManager(val controllerRef: ActorRef[ControllerMessage]){

  private val actorType: String = ApplicationConstants.ReceptionistManagerActorType
  private var seed: Int = 0

  private val waitingEvents: Behavior[ReceptionistManagerMessage] = Behaviors.receive { (ctx, message) =>
    message match {

      case Add(worker) => log("UN ANOTHER")
        if(worker.nonEmpty)
          actorSet = worker
        log(worker.toString())
        if(seed == 0) {
          if (actorSet.size == 1 && !actorSet.head.path.toString.contains("127.0.0.1") && port == 25251) {
            seed = scala.util.Random.nextInt() + 1
            controllerRef ! DisplayView(this.seed)
          } else {
            if (actorSet.exists(a => a.path.toString.contains("127.0.0.1")))
              actorSet.filter(a => a.path.toString.contains("127.0.0.1")).head ! RequestSeed(ctx.self)
          }
        }

      case Swap(tileList, isPuzzleCompleted) => tileList.foreach(t => log(t.toString()))
        implicit val timeout: Timeout = 5.seconds
        actorSet.foreach(w => if (w.path.toString.contains("127.0.0.1")) {
          w ! ExpandChange(tileList, isPuzzleCompleted) })

      case ExpandChange(tileList, isPuzzleCompleted) => controllerRef ! ReceiveUpdate(tileList, isPuzzleCompleted)

      case RequestSeed(from) => from ! SendSeed(this.seed)

      case SendSeed(seed) => this.seed = seed
      controllerRef ! DisplayView(this.seed)

      case _ => log("Bubba")
    }
    Behaviors.same
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
