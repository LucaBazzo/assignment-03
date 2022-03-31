package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Clear, Command, Pick}
import pcd.assignment03.main.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.tasks.MasterActor.{Error, MasterMessage, ProcessPDFCompleted, ProcessingReady, WordsLists, executor, extractResults, forbiddenList, log, pdfProcessor, stringList, taskList}
import pcd.assignment03.tasks.PDFExtractActor.{PDFExtractMessage, StartExtraction, ResultList}

import java.io.{File, FileNotFoundException}
import java.util
import java.util.Scanner
import java.util.concurrent.{Executors, Future}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.control.Breaks.{break, breakable}

object ProcessPDFActor {

  sealed trait ProcessPDFMessage
  case class StartProcessing(from: ActorRef[MasterMessage]) extends ProcessPDFMessage
  case class RetrieveWords(result: Option[List[String]]) extends ProcessPDFMessage
  //case class WordsList(result: Option[List[String]]) extends ProcessPDFMessage

  private var forbiddenList: List[String] = List.empty
  private var taskList: List[ActorRef[PDFExtractMessage]] = List.empty
  private val executor = Executors.newCachedThreadPool()
  private val extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()
  private var stringList: List[String] = List.empty
  private var taskType: String = ""
  private var master: ActorRef[MasterMessage] = null
  private var slaveActorCount: Int = 0

  def apply(taskType: String, forbidden: File, view: ActorRef[ViewMessage], pdfDirectory: Array[File],
            stopMonitor: StopMonitor): Behavior[ProcessPDFMessage] = Behaviors.receive { (ctx, message) =>
    this.taskType = taskType
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

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }

}
