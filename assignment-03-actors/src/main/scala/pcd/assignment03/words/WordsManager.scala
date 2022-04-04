package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, WorkEnded}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.WordsBag.{Command, CountWords}

object WordsManager {

  sealed trait WordsManagerMessage
  case class ManageList(list: List[String], numActors: Int) extends WordsManagerMessage
  case class ChildEnded() extends WordsManagerMessage
  case class StopActor() extends WordsManagerMessage

  private val actorType: String = ApplicationConstants.WordsManagerActorType

  private var nActiveActors: Int = 0
  private var childrenList: List[ActorRef[Command]] = List.empty

  def apply(bag: ActorRef[Command], fatherRef: ActorRef[MasterMessage]): Behavior[WordsManagerMessage] = Behaviors.receive { (context, message) =>
    message match {
      case ManageList(list, n) =>
        this.nActiveActors = n
        round(list, n).foreach(sublist => {
          val actor = context.spawnAnonymous(WordsActor(bag, context.self))
          this.childrenList = actor :: childrenList
          actor ! CountWords(sublist)
        })
      case ChildEnded() =>
        nActiveActors -= 1
        log("Child terminated. " + nActiveActors + " left")
        if(nActiveActors == 0) {
          childrenList = List.empty
          fatherRef ! WorkEnded()
          log("Work completed")
        } else if(nActiveActors < 0) {
          log("Error")
          Behaviors.stopped
        }
      case StopActor() =>
        log("Interrupted")
        this.childrenList.foreach(child => child ! WordsBag.StopActor())
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
