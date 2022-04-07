package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, WorkEnded}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.WordsActor.{CountWords, WordsMessage}
import pcd.assignment03.words.WordsBag.Command

/** Manages the words actor on the basis of the list of words sent by the master.
 *  Based on the actors to spawn, he divides the list into equal parts and gives them
 *  to the respective actors
 */
object WordsManager {

  sealed trait WordsManagerMessage
  case class ManageList(list: List[String], numActors: Int) extends WordsManagerMessage
  case class ChildEnded(childRef: ActorRef[WordsMessage]) extends WordsManagerMessage
  case class StopActor() extends WordsManagerMessage

  private val actorType: String = ApplicationConstants.WordsManagerActorType

  private var nActiveActors: Int = 0
  private var childrenList: List[ActorRef[WordsMessage]] = List.empty
  private var interrupted: Boolean = false

  /**
   *  @param bag reference to the bag that contains all the words count
   *  @param fatherRef the reference to the actor that has spawned this
   */
  def apply(bag: ActorRef[Command], fatherRef: ActorRef[MasterMessage]): Behavior[WordsManagerMessage] = Behaviors.receive { (context, message) =>
    message match {
      case ManageList(list, n) =>
        log("Work with " + n + " actors started")
        this.interrupted = false
        this.nActiveActors = n
        round(list, n).foreach(sublist => {
          val actor = context.spawnAnonymous(WordsActor(bag, context.self))
          this.childrenList = actor :: childrenList
          actor ! CountWords(sublist)
        })

      case ChildEnded(childRef) =>
        if(!interrupted) {
          nActiveActors -= 1
          childrenList = childrenList.filterNot(child => child == childRef)
          log("Child terminated. " + nActiveActors + " left")
          if(nActiveActors == 0) {
            childrenList = List.empty
            fatherRef ! WorkEnded()
            log("Work completed")
          } else if(nActiveActors < 0) {
            log("Error")
            return Behaviors.stopped
          }
        }

      case StopActor() =>
        this.interrupted = true
        this.childrenList.foreach(child => context.stop(child))
        this.childrenList = List.empty
        log("Interrupted")
    }

    Behaviors.same
  }

  private def round(l: List[String], n: Int): Seq[List[String]] = (0 until n)
    .map{ i => l.drop(i).sliding(1, n).flatten.toList }.toList

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)

}
