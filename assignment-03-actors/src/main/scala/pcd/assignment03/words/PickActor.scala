package pcd.assignment03.words

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.main.MasterActor.{MasterMessage, PickerResult}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.PickActor.{Pick, PickerMessage, ReturnBag, StartPicking, StopPicking}
import pcd.assignment03.words.WordsBag.{Command, GetBag}

import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

object PickActor {

  sealed trait PickerMessage
  case class StartPicking(nWords: Int) extends PickerMessage
  case class Pick(nWords: Int, last: Boolean = false) extends PickerMessage
  case class ReturnBag(map: mutable.HashMap[String, Int]) extends PickerMessage
  case class StopPicking() extends PickerMessage

  def apply(masterRef: ActorRef[MasterMessage], wordsBag: ActorRef[Command]): Behavior[PickerMessage] =
    Behaviors.setup { ctx =>
      new PickActor(ctx, masterRef, wordsBag).standby
    }
}

/** Actor that, when started, obtain the most frequent words periodically
 *
 *  @constructor create a new picker
 *  @param ctx the actor context
 *  @param masterRef the reference to the master, in order to send him the answers
 *  @param wordsBag a bag that contain the collection of words occurrences
 */
class PickActor(val ctx: ActorContext[PickerMessage],
                val masterRef: ActorRef[MasterMessage],
                val wordsBag: ActorRef[Command]){

  private val actorType: String = ApplicationConstants.PickerActorType
  private var interrupted: Boolean = false

  private val pickDelay: FiniteDuration = ApplicationConstants.PickDelay

  //Expect to be notified to periodically count the most frequent words
  private val standby: Behavior[PickerMessage] = Behaviors.receive[PickerMessage] { (ctx, message) =>
    message match {
      case StartPicking(nWords) =>
        this.interrupted = false

        ctx.scheduleOnce(this.pickDelay, ctx.self, Pick(nWords))
        picking

      case Pick(_, _) => Behaviors.same
    }
  }

  //picks to count the required number of max words
  private val picking: Behavior[PickerMessage] = Behaviors.receive[PickerMessage] { (_, message) =>
    message match {
      case Pick(nWords, last) =>
        log("Acquiring the bag...")

        implicit val timeout: Timeout = 2.seconds
        implicit val scheduler: Scheduler = ctx.system.scheduler
        val f: Future[PickerMessage] = wordsBag ? (replyTo => GetBag(replyTo))
        implicit val ec: ExecutionContextExecutor = ctx.executionContext

        //remember you can't call context on future callback
        f.onComplete({
          case Success(value) if value.isInstanceOf[ReturnBag] =>
            if(!interrupted) {
              log("Bag copy acquired")
              val maxWords = this.countingMaxWords(value.asInstanceOf[ReturnBag].map, nWords)
              masterRef ! PickerResult(maxWords, last)
              if(last) log("Pick completed")
            }

          case _ => log("ERROR")
        })

        if(last) standby
        else {
          ctx.scheduleOnce(this.pickDelay, ctx.self, Pick(nWords))
          Behaviors.same
        }

      case StopPicking() =>
        log("Interrupted")
        this.interrupted = true
        standby
    }
  }

  private def countingMaxWords(map: mutable.HashMap[String, Int], nWords: Int): Option[(Integer, List[(String, Integer)])] = {
    var maxList: List[(String, Integer)] = List.empty

    if(map.nonEmpty) {
      val wordsProc: Int = map.values.sum
      maxList = pickWordsMax(map, nWords)
      log(wordsProc.toString, maxList.toString())
      return Option.apply((wordsProc, maxList))
    }

    log("Bag empty")
    Option.empty
  }

  private def pickWordsMax(map: mutable.HashMap[String, Int], nWords: Int): List[(String, Integer)] = {
    log("Picking...")

    var maxList: List[(String, Integer)] = List.empty

    while(maxList.length < nWords && map.nonEmpty) {
      val max: Int = map.values.max

      //Map -> Stream -> Filter -> Limit -> ForEach
      map.filter(entry => entry._2 == max)
        .take(nWords - maxList.length)
        .foreach(entry => {
          maxList = maxList.appended((entry._1, entry._2))
        })

      maxList.foreach(pair => map.remove(pair._1))
    }

    log("Most frequent words computed")

    maxList
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
