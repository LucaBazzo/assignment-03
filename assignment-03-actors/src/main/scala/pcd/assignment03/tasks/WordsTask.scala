package pcd.assignment03.tasks

import akka.actor.typed.ActorRef
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Command, Update}

import java.util.concurrent.Callable

class WordsTask(val taskType: String, var stringList: List[String], var startingIndex: Int, var arrivingIndex: Int,
                var bag: ActorRef[Command], var stopMonitor: StopMonitor) extends Callable[Boolean] {

  override def call(): Boolean = {
    log("Started");

    var partialList: List[String] = stringList.slice(startingIndex, arrivingIndex)

    log("N° Words: " + partialList.length)

    while(partialList.nonEmpty && !stopMonitor.isStopped) {
      bag ! Update(partialList.head)
      partialList = partialList.tail
    }

    if(!stopMonitor.isStopped) {
      log("Completed");
      true
    }
    else {
      log("Interrupted");
      false
    }
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }
}
