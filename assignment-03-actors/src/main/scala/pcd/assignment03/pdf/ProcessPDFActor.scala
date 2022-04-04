package pcd.assignment03.pdf

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.main.MasterActor.{MasterMessage, WordsLists}
import pcd.assignment03.pdf.PDFExtractActor.{PDFExtractMessage, StartExtraction}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.{ChangeState, ViewMessage}

import java.io.{File, FileNotFoundException}
import java.util.Scanner

object ProcessPDFActor {

  sealed trait ProcessPDFMessage
  case class StartProcessing(pdfDirectory: Array[File], forbidden: File,
                             from: ActorRef[MasterMessage]) extends ProcessPDFMessage
  case class RetrieveWords(result: Option[List[String]]) extends ProcessPDFMessage

  private var forbiddenList: List[String] = List.empty
  private var stringList: List[String] = List.empty
  private val actorType: String = ApplicationConstants.PDFExtractActorType
  private var master: ActorRef[MasterMessage] = _

  private var nActiveActors: Int = 0
  private var childrenList: List[ActorRef[PDFExtractMessage]] = List.empty

  def apply(view: ActorRef[ViewMessage]): Behavior[ProcessPDFMessage] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case StartProcessing(pdfDirectory, forbidden, from) =>
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

          //TODO codice di com'era prima, try catch per me useless
          /*try {
            taskList.foreach(x => {
              x ! StartExtraction(ctx.self)
            })
          } catch {
            case _: Exception =>
              stopMonitor.stop()
              log("Interrupted")
              view ! ChangeState("Interrupted")
          }*/

        case RetrieveWords(result) =>
          if (result.isDefined) {
            stringList = stringList.appendedAll(result.get)
            nActiveActors -= 1
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
            Behaviors.stopped
          }
      }
    Behaviors.same
  }

  private def log(messages: String*): Unit = {
    for (msg <- messages) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }

}
