package pcd.assignment03.pdf

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, WordsLists}
import pcd.assignment03.pdf.PDFExtractActor.{PDFExtractMessage, StartExtraction}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.{ChangeState, ViewMessage}

import java.io.{File, FileNotFoundException}
import java.util.Scanner

object ExtractorManager {

  sealed trait ExtractorManagerMessage
  case class StartProcessing(pdfDirectory: Array[File], forbidden: File,
                             from: ActorRef[MasterMessage]) extends ExtractorManagerMessage
  case class RetrieveWords(result: Option[List[String]],
                           from: ActorRef[PDFExtractMessage]) extends ExtractorManagerMessage
  case class StopActor() extends ExtractorManagerMessage

  private var forbiddenList: List[String] = List.empty
  private var stringList: List[String] = List.empty
  private val actorType: String = ApplicationConstants.ExtractorManagerActorType
  private var master: ActorRef[MasterMessage] = _

  private var nActiveActors: Int = 0
  private var childrenList: List[ActorRef[PDFExtractMessage]] = List.empty
  private var interrupted: Boolean = false

  def apply(view: ActorRef[ViewMessage]): Behavior[ExtractorManagerMessage] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case StartProcessing(pdfDirectory, forbidden, from) =>
          this.interrupted = false
          try {
            val reader: Scanner = new Scanner(forbidden)
            master = from
            while (reader.hasNextLine) {
              val data: String = reader.nextLine()
              forbiddenList = data :: forbiddenList
            }
            reader.close()
          } catch {
            case e: FileNotFoundException =>
              log("An error occurred.")
              e.printStackTrace()

          }

          view ! ChangeState("Getting PDF...")
          log("Wait completion")
          pdfDirectory.foreach(pdfFile => {
            val child = ctx.spawnAnonymous(PDFExtractActor(forbiddenList, pdfFile))
            childrenList = child :: childrenList
          })

          nActiveActors = childrenList.length

          view ! ChangeState("PDF Processing...")

          childrenList.foreach(x => x ! StartExtraction(ctx.self))
          Behaviors.same

        case RetrieveWords(result, from) =>
          if(!interrupted) {
            if (result.isDefined) {
              stringList = stringList.appendedAll(result.get)
              nActiveActors -= 1
              childrenList = childrenList.filterNot(child => child == from)
              log("Child terminated. " + nActiveActors + " left")
            }
            if (nActiveActors == 0) {
              log("Process PDF completed")
              log("Completion arrived")
              master ! WordsLists(Option.apply(stringList))
              childrenList = List.empty
              stringList = List.empty
            } else if(nActiveActors < 0) {
              log("Error")
              return Behaviors.stopped
            }
          }
          Behaviors.same

        case StopActor() =>
          log("Interrupted")
          this.interrupted = true
          this.childrenList.foreach(child => ctx.stop(child))
          childrenList = List.empty
          stringList = List.empty
          Behaviors.same
      }
  }

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)
}
