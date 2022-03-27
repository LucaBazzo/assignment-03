package pcd.assignment03.tasks

import akka.actor.typed.ActorRef
import pcd.assignment03.concurrency.WordsBagFilling.Command

import java.util.concurrent.Callable
import scala.collection.mutable

class PickTask(val taskType: String, var nWords: Int, var wordsBag: ActorRef[Command]) extends Callable[Option[(Integer, List[(String, Integer)])]]{

  override def call(): Option[(Integer, List[(String, Integer)])] = {
    log("Started");

    var maxList: List[(String, Integer)] = List.empty

    log("Acquiring the bag...")

    /*val f: Future[mutable.HashMap[String, Int]] = wordsBag ? (replyTo => Greet("Bob", replyTo))
    implicit val ec = ctx.executionContext
    f.onComplete {
      case Success(Greeted(who,from)) => println(s"${who} has been greeted by ${from.path}!")
      case _ => println("No greet")
    }*/

    val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()//wordsBag.getBag

    log("Bag copy acquired");

    if(map.nonEmpty) {
      val wordsProc: Int = wordsProcessed(map)
      maxList = pickWordsMax(map);
      return Option.apply((wordsProc, maxList))
    }

    log("Bag empty");
    Option.empty
  }

  private def wordsProcessed(map: mutable.HashMap[String, Int]): Int = map.values.sum

  private def pickWordsMax(map: mutable.HashMap[String, Int]): List[(String, Integer)] = {
    log("Picking...");

    var maxList: List[(String, Integer)] = List.empty

    while(maxList.length < nWords && map.nonEmpty) {
      val max: Int = map.values.max

      //Map -> Stream -> Filter -> Limit -> ForEach
      map.filter(entry => entry._2 == max)
        .take(nWords - maxList.length)
        .foreach(entry => {
          maxList = maxList.appended((entry._1, entry._2))  //TODO ricontrolla appended
        })

      maxList.foreach(pair => map.remove(pair._1))
    }

    map.filter(entry => entry._1 == "text").foreach(entry => println(entry))

    log("Most frequent words computed")

    maxList
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }
}
