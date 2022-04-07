package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.WordsBag.{Command, Update}
import pcd.assignment03.words.WordsManager.{ChildEnded, WordsManagerMessage}

/** Actor that adds words and their frequency into the bag
 *
 */
object WordsActor {

  sealed trait WordsMessage
  case class CountWords(subList: List[String]) extends WordsMessage

  private val actorType: String = ApplicationConstants.WordsActorType

  /**
   *  @param bag reference to the bag that contains all the words count
   *  @param fatherRef the reference to the actor that has spawned this
   */
  def apply(bag: ActorRef[Command], fatherRef: ActorRef[WordsManagerMessage]): Behavior[WordsMessage] = Behaviors.receive { (ctx, message) =>
    message match {
        case CountWords(subList) =>
          log("Started with " + subList.length + " words")

          subList.foreach(word => bag ! Update(word))
          fatherRef ! ChildEnded(ctx.self)
          log("Completed")
          Behaviors.stopped
      }
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}

