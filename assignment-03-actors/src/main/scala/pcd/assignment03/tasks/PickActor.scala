package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.WordsBagFilling._
import pcd.assignment03.tasks.MasterActor.MaxWordsResult

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

object PickActor {

  def apply(nWords: Int, wordsBag: ActorRef[Command]): Behavior[Command] =
    Behaviors.setup { ctx =>
      new PickActor(ctx, nWords, wordsBag).pick
    }
}

class PickActor(val ctx: ActorContext[Command], var nWords: Int, var wordsBag: ActorRef[Command]){

  private val actorType: String = "Pick Actor"
  private var interrupted: Boolean = false

  private val pick: Behavior[Command] = Behaviors.receive[Command] { (context, message) =>
    message match {
      case Pick(from) =>
        log("Acquiring the bag...")
        implicit val timeout: Timeout = 2.seconds
        implicit val scheduler: Scheduler = ctx.system.scheduler
        val f: Future[Command] = wordsBag ? (replyTo => GetBag(replyTo))
        implicit val ec: ExecutionContextExecutor = ctx.executionContext
        //remember you can't call context on future callback
        f.onComplete({
          case Success(value) if value.isInstanceOf[Return] =>
            if(!interrupted) {
              log("Bag copy acquired")
              val maxWords = this.countingMaxWords(value.asInstanceOf[Return].map)
              from ! MaxWordsResult(maxWords)
            }

          case _ => log("ERROR")
        })
        Behaviors.same

      case StopActor() =>
        log("Interrupted")
        this.interrupted = true
        Behaviors.stopped
    }
  }


  private def log(messages: String*): Unit = {
    for (msg <- messages) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }

  def countingMaxWords(map: mutable.HashMap[String, Int]): Option[(Integer, List[(String, Integer)])] = {
    var maxList: List[(String, Integer)] = List.empty

    if(map.nonEmpty) {
      val wordsProc: Int = map.values.sum
      maxList = pickWordsMax(map)
      log(wordsProc.toString, maxList.toString())
      return Option.apply((wordsProc, maxList))
    }

    log("Bag empty")
    Option.empty
  }

  private def pickWordsMax(map: mutable.HashMap[String, Int]): List[(String, Integer)] = {
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
}
