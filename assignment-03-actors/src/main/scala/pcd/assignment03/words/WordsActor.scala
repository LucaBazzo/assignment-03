package pcd.assignment03.words

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.words.WordsBag.{Command, CountWords, Update}
import pcd.assignment03.words.WordsManager.{ChildEnded, WordsManagerMessage}

object WordsActor {

  private val actorType: String = ApplicationConstants.WordsActorType

  def apply(bag: ActorRef[Command], fatherRef: ActorRef[WordsManagerMessage]): Behavior[Command] = Behaviors.receive { (ctx, message) =>
    message match {
        case CountWords(subList) =>
          log("Started")
          //waitFor(2000)
          log("N° Words: " + subList.length)

          subList.foreach(word => bag ! Update(word))
          fatherRef ! ChildEnded(ctx.self)
          log("Completed")
          Behaviors.stopped

        /*case CountWords(subList) =>
          log("Started")
          log("N° Words: " + subList.length)

          implicit val executionContext: ExecutionContext =
            ctx.system.dispatchers.lookup(DispatcherSelector.fromConfig("blocking-dispatcher"))
          Future {
            subList.foreach(word => if(!interrupted) bag ! Update(word))
            if(!interrupted) {
              fatherRef ! ChildEnded()
              log("Completed")
              return Behaviors.stopped
            }
          }
          Behaviors.same*/
      }
  }

  @throws[InterruptedException]
  private def waitFor(ms: Long): Unit = {
    Thread.sleep(ms)
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}

