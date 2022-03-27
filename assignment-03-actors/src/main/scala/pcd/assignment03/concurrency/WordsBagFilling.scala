package pcd.assignment03.concurrency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object WordsBagFilling {

  sealed trait Command
  final case class Clear() extends Command
  final case class Update(word: String) extends Command
  final case class Get(from: ActorRef[Update]) extends Command

  private val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Update(word) =>
        var count: Int = 1
        if(this.map.contains(word)) {
          count = this.map(word) + 1
        }
        this.map.put(word, count)
        context.log.info("Update {}!", (word, count))
      case Get(from) => ???
      case Clear() => this.map.clear()
    }

    Behaviors.same
  }

  def getBag: mutable.HashMap[String, Int] = synchronized {
    this.map.clone()
  }
}

/*class WordsBagFilling {

  private val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def clearBag(): Unit = synchronized {
    this.map.clear()
  }

  def getBag: mutable.HashMap[String, Int] = synchronized {
    this.map.clone()
  }

  def addElement(word: String): Unit = synchronized {
    var count: Int = 1
    if(this.map.contains(word)) {
      count = this.map(word) + 1
    }
    this.map.put(word, count)
    notify()
  }

}*/
