package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import WordsBag.{Command, CountWords, StopActor, Update}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.WordsManager.{ChildEnded, WordsManagerMessage}

import scala.concurrent.{ExecutionContext, Future}

object WordsActor {

  private val actorType: String = ApplicationConstants.WordsActorType
  private var interrupted: Boolean = false

  def apply(bag: ActorRef[Command], fatherRef: ActorRef[WordsManagerMessage]): Behavior[Command] = Behaviors.receive { (ctx, message) =>
    message match {
        case CountWords(subList) =>
          log("Started")
          log("N° Words: " + subList.length)

          implicit val executionContext: ExecutionContext =
            ctx.system.dispatchers.lookup(DispatcherSelector.fromConfig("blocking-dispatcher"))
          Future {
            subList.foreach(word => if(!interrupted) bag ! Update(word))
            if(!interrupted) {
              fatherRef ! ChildEnded()
              log("Completed")
              Behaviors.stopped
            }
          }

        case StopActor() =>
          log("Interrupted")
          this.interrupted = true
          Behaviors.stopped
      }

      Behaviors.same
  }

  private def log(messages: String*): Unit = {
    for (msg <- messages) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }
}
