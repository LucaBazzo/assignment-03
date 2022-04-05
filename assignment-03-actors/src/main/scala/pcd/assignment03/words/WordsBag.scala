package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object WordsBag {

  sealed trait Command
  final case class Clear() extends Command
  final case class Update(word: String) extends Command
  final case class GetBag(from: ActorRef[Command]) extends Command
  final case class Return(map: mutable.HashMap[String, Int]) extends Command

  final case class StopActor() extends Command
  final case class CountWords(subList: List[String]) extends Command

  private val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def apply(): Behavior[Command] = Behaviors.receive { (_, message) =>
    message match {
      case Update(word) =>
        var count: Int = 1
        if (this.map.contains(word)) {
          count = this.map(word) + 1
        }
        this.map.put(word, count)
      case GetBag(from) => from ! Return(map.clone())
      case Clear() => this.map.clear()
    }

    Behaviors.same
  }
}
