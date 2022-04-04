package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.main.View.{ChangeState, ViewMessage}
import pcd.assignment03.tasks.MasterActor.{MasterMessage, WordsLists}
import pcd.assignment03.tasks.PDFExtractActor.{PDFExtractMessage, StartExtraction}

import java.io.{File, FileNotFoundException}
import java.util.Scanner

object ProcessPDFActor {

  sealed trait ProcessPDFMessage
  case class StartProcessing(from: ActorRef[MasterMessage]) extends ProcessPDFMessage
  case class RetrieveWords(result: Option[List[String]]) extends ProcessPDFMessage
  //case class WordsList(result: Option[List[String]]) extends ProcessPDFMessage

  private var forbiddenList: List[String] = List.empty
  private var taskList: List[ActorRef[PDFExtractMessage]] = List.empty
  private var stringList: List[String] = List.empty
  private val actorType: String = "Process PDF Actor"
  private var master: ActorRef[MasterMessage] = _
  private var slaveActorCount: Int = 0

  def apply(forbidden: File, view: ActorRef[ViewMessage], pdfDirectory: Array[File],
            stopMonitor: StopMonitor): Behavior[ProcessPDFMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case StartProcessing(from) =>
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
          val task = ctx.spawn(PDFExtractActor(forbiddenList, pdfFile, stopMonitor), "PDFExtractor" + slaveActorCount)
          taskList = task :: taskList
          slaveActorCount += 1
        })

        view ! ChangeState("PDF Processing...")

        try {
          taskList.foreach(x => {
            x ! StartExtraction(ctx.self)})
        } catch {
          case _: Exception =>
            stopMonitor.stop()
            log("Interrupted")
            view ! ChangeState("Interrupted")
        }

      case RetrieveWords(result) =>
        if (result.isDefined) {
          stringList = stringList.appendedAll(result.get)
          slaveActorCount -= 1
        }
        if (slaveActorCount == 0) {
          log("Process PDF completed")
          log("Completion arrived")
          master ! WordsLists(Option.apply(stringList))
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
