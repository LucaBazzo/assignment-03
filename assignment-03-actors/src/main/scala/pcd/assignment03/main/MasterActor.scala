package pcd.assignment03.main

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor._
import pcd.assignment03.pdf.ExtractorManager
import pcd.assignment03.pdf.ExtractorManager.{ExtractorManagerMessage, StartProcessing}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.words.PickActor.{Pick, PickerMessage, StartPicking, StopPicking}
import pcd.assignment03.words.WordsBag.Clear
import pcd.assignment03.words.WordsManager.{ManageList, StopActor, WordsManagerMessage}
import pcd.assignment03.words.{PickActor, WordsBag, WordsManager}

import java.io.File

object MasterActor {

  sealed trait MasterMessage
  case class Start(pdfDirectory: File, forbidden: File, nWords: Int) extends MasterMessage
  case class Error() extends MasterMessage
  case class ProcessingReady() extends MasterMessage
  case class ProcessPDFCompleted() extends MasterMessage
  case class WordsLists(result: Option[List[String]]) extends MasterMessage
  case class PickerResult(result: Option[(Integer, List[(String, Integer)])],
                          isLast: Boolean) extends MasterMessage

  case class StopComputation() extends MasterMessage

  case class WorkEnded() extends MasterMessage

  def apply(controller: ActorRef[ControllerMessage],
            view: ActorRef[ViewMessage],
            numActors: Int = ApplicationConstants.NumProcessors): Behavior[MasterMessage] =
    Behaviors.setup { context =>
      new MasterActor(context, controller, view, numActors).standby
    }
}

/** Actor that controls the execution flow of the application
 *
 * @param context the actor context
 * @param controller reference to the controller in order to notify the completed result at the end
 * @param view the view to interact with
 * @param numActors number of words actors
 *
 */
class MasterActor(val context: ActorContext[MasterMessage],
                  val controller: ActorRef[ControllerMessage],
                  val view: ActorRef[ViewMessage],
                  val numActors: Int) {

  private val actorType: String = ApplicationConstants.MasterActorType

  private val wordsBag = context.spawn(WordsBag(), "WordsBag")
  private val extractorManager: ActorRef[ExtractorManagerMessage] =
    context.spawn(ExtractorManager(view), "ProcessPDF")
  private val picker: ActorRef[PickerMessage] = context.spawn(PickActor(context.self, wordsBag),
    "Picker")
  private val wordsManager: ActorRef[WordsManagerMessage] =
    context.spawn(WordsManager(wordsBag, context.self), "WordsManager")

  private var startTime = 0L
  private var nWords = 0
  private var workCompleted: Boolean = false

  private val standby: Behavior[MasterMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case Start(pdfDirectory, forbidden, nWords) =>
        this.workCompleted = false
        this.startTime = System.currentTimeMillis()
        this.nWords = nWords
        wordsBag ! Clear()
        extractorManager ! StartProcessing(pdfDirectory.listFiles(), forbidden, ctx.self)

        gettingPDF

      case StopComputation() =>
        log("Already in standby")
        Behaviors.same

      case _ => Behaviors.same
    }
  }

  //state in which it requires the opening of the pdf to obtain the text
  private val gettingPDF: Behavior[MasterMessage] = Behaviors.receive { (_, message) =>
    message match {
      case WordsLists(stringList) =>
        val result = stringList.get
        log("Computing most frequent words...")
        view ! ChangeState("Computing most frequent words...")

        log("List of words size: " + result.length, "Num of actors: " + numActors)

        this.wordsManager ! ManageList(result, numActors)

        log("Picking start")
        this.picker ! StartPicking(nWords)

        computingMostFrequentWords

      case StopComputation() =>
        this.extractorManager ! ExtractorManager.StopActor()
        log("Interrupted")
        view ! ChangeState("Interrupted")

        standby
    }
  }

  //behavior while other actors are counting words, wait for notifications from the Picker and WordsManager
  private val computingMostFrequentWords: Behavior[MasterMessage] = Behaviors.receive { (_, message) =>
    message match {
      case WorkEnded() =>
        this.picker ! Pick(nWords, last=true)
        controller ! ProcessCompleted()

        Behaviors.same

      /**
       * Message that arrives every time the picker counts the words frequency
       */
      case PickerResult(result, isLast) =>
        if(result.isDefined) {
          val wordsProcessed: Int = result.get._1
          val wordsFreq: List[(String, Integer)] = result.get._2

          log("Words processed: " + wordsProcessed, wordsFreq.toString())

          view ! UpdateResult(wordsProcessed, wordsFreq, isLast)
          if(isLast) {
            log("Most Frequent Words completed")

            val timeElapsed: Long = System.currentTimeMillis() - startTime
            log("Completed - time elapsed: " + timeElapsed)
            view ! ChangeState("Completed - time elapsed: "+ timeElapsed)
          }
        }
        if(isLast) standby
        else Behaviors.same

      case StopComputation() =>
        this.wordsManager ! StopActor()
        this.picker ! StopPicking()
        log("Interrupted")
        view ! ChangeState("Interrupted")

        standby
    }
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)

}