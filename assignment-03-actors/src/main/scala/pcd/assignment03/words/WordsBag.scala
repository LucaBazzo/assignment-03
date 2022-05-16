package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.words.PickActor.{PickerMessage, ReturnBag}

import scala.collection.mutable

/** Managing the collection of words occurrences
 *
 */
object WordsBag {

  sealed trait Command
  case class Clear() extends Command
  case class Update(word: String) extends Command
  case class GetBag(from: ActorRef[PickerMessage]) extends Command

  private val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def apply(): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
      case Update(word) =>
        var count: Int = 1
        if (this.map.contains(word)) {
          count = this.map(word) + 1
        }
        this.map.put(word, count)

      case GetBag(from) => from ! ReturnBag(map.clone())

      case Clear() => this.map.clear()
    }

    Behaviors.same
  }
}
