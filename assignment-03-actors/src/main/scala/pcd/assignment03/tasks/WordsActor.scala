package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.concurrency.WordsBagFilling.{Command, CountWords, Stop, Update}
import pcd.assignment03.tasks.ProcessWords.{ChildEnded, ProcessWordsMessage}

object WordsActor {

  private val actorType: String = "Words Actor"

  def apply(bag: ActorRef[Command], fatherRef: ActorRef[ProcessWordsMessage]): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
        case CountWords(subList) =>
          log("Started")

          log("N° Words: " + subList.length)

          subList.foreach(word => bag ! Update(word))

          log("Completed")

          fatherRef ! ChildEnded()

        case Stop() =>
          log("Interrupted")
          Behaviors.stopped
      }

      Behaviors.same
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }
}
