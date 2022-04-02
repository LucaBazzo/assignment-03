package pcd.assignment03.tasks

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import pcd.assignment03.concurrency.WordsBagFilling.{Command, CountWords}

object ProcessWords {

  sealed trait ProcessWordsMessage
  case class ProcessList(list: List[String], numActors: Int) extends ProcessWordsMessage

  def apply(bag: ActorRef[Command]): Behavior[ProcessWordsMessage] = Behaviors.receive { (context, message) =>
    message match {
      case ProcessList(list, n) =>
        round(list, n).foreach(sublist => {
          val actor = context.spawnAnonymous(WordsActor(bag))
          actor ! CountWords(sublist)
        })
    }

    Behaviors.same
  }

  private def round(l: List[String], n: Int): Seq[List[String]] = (0 until n).map{ i => l.drop(i).sliding(1, n).flatten.toList }.toList


}
