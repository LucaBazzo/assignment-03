package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.concurrency.WordsBagFilling
import pcd.assignment03.concurrency.WordsBagFilling.{Command, CountWords}
import pcd.assignment03.tasks.MasterActor.{MasterMessage, WorkEnded}

object ProcessWords {

  sealed trait ProcessWordsMessage
  case class ProcessList(list: List[String], numActors: Int) extends ProcessWordsMessage
  case class ChildEnded() extends ProcessWordsMessage
  case class StopActor() extends ProcessWordsMessage

  private val actorType: String = "Process Words"

  private var nActors: Int = 0
  private var childrenList: List[ActorRef[Command]] = List.empty

  def apply(bag: ActorRef[Command], fatherRef: ActorRef[MasterMessage]): Behavior[ProcessWordsMessage] = Behaviors.receive { (context, message) =>
    message match {
      case ProcessList(list, n) =>
        this.nActors = n
        round(list, n).foreach(sublist => {
          val actor = context.spawnAnonymous(WordsActor(bag, context.self))
          this.childrenList = actor :: childrenList
          actor ! CountWords(sublist)
        })
      case ChildEnded() =>
        nActors -= 1
        log("Child terminated. " + nActors + " left")
        if(nActors == 0) {
          fatherRef ! WorkEnded()
          log("Work completed")
          Behaviors.stopped
        } else if(nActors < 0) {
          log("Error")
          Behaviors.stopped
        }
      case StopActor() =>
        log("Interrupted")
        this.childrenList.foreach(child => child ! WordsBagFilling.StopActor())
        Behaviors.stopped
    }

    Behaviors.same
  }

  private def round(l: List[String], n: Int): Seq[List[String]] = (0 until n).map{ i => l.drop(i).sliding(1, n).flatten.toList }.toList

  private def log(messages: String*): Unit = {
    for (msg <- messages) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }

}
