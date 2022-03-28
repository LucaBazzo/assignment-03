package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.WordsBagFilling.{Command, GetBag, Pick, Return}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Success

object PickActor {

  def apply(taskType: String, nWords: Int, wordsBag: ActorRef[Command]): Behavior[Command] =
    Behaviors.setup { ctx =>
      new PickActor(ctx, taskType, nWords, wordsBag).pick
    }
}

class PickActor(val ctx: ActorContext[Command], val taskType: String, var nWords: Int, var wordsBag: ActorRef[Command]){

  private val pick: Behavior[Command] = Behaviors.receiveMessagePartial {
    case Pick() =>
      log("Acquiring the bag...")
      implicit val timeout: Timeout = 2.seconds
      implicit val scheduler: Scheduler = ctx.system.scheduler
      val f: Future[Command] = wordsBag ? (replyTo => GetBag(replyTo))
      implicit val ec: ExecutionContextExecutor = ctx.executionContext
      //remember you can't call context on future callback
      f.onComplete({
        case Success(value) if value.isInstanceOf[Return] => {
          log("Bag copy acquired")
          this.call(value.asInstanceOf[Return].map)
        }
        case _ => log("ERROR")
      })

      Behaviors.same
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }

  //TODO cambia il nome
  def call(map: mutable.HashMap[String, Int]): Option[(Integer, List[(String, Integer)])] = {
    var maxList: List[(String, Integer)] = List.empty

    if(map.nonEmpty) {
      val wordsProc: Int = map.values.sum
      maxList = pickWordsMax(map)
      log(wordsProc.toString(), maxList.toString())
      return Option.apply((wordsProc, maxList))
    }

    log("Bag empty")
    Option.empty
  }

  private def pickWordsMax(map: mutable.HashMap[String, Int]): List[(String, Integer)] = {
    log("Picking...");

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

    map.filter(entry => entry._1 == "text").foreach(entry => println(entry))

    log("Most frequent words computed")

    maxList
  }
}
