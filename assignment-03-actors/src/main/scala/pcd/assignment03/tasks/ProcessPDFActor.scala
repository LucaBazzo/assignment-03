package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Clear, Command, Pick}
import pcd.assignment03.main.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.tasks.MasterActor.{MasterMessage, ProcessingReady, executor, extractResults, forbiddenList, log, stringList, taskList}

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
  case class RetrieveWords(from: ActorRef[ProcessPDFMessage]) extends ProcessPDFMessage
  case class WordsList(result: Option[List[String]]) extends ProcessPDFMessage

  private var forbiddenList: List[String] = List.empty
  private val taskList: util.Collection[PDFExtractTask] = new util.ArrayList[PDFExtractTask]() //TODO ricontrolla
  private val executor = Executors.newCachedThreadPool()
  private var extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()
  private var stringList: List[String] = List.empty
  private var taskType: String = ""

  def apply(taskType: String, forbidden: File, view: ActorRef[ViewMessage], pdfDirectory: Array[File],
            stopMonitor: StopMonitor): Behavior[ProcessPDFMessage] = Behaviors.receive { (ctx, message) =>
    this.taskType = taskType
    message match {
      case StartProcessing(from) =>
        try {
          val reader: Scanner = new Scanner(forbidden)
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
          val task: PDFExtractTask = new PDFExtractTask(forbiddenList, pdfFile, stopMonitor)
          taskList.add(task)
        })

        view ! ChangeState("PDF Processing...")

        try {
          extractResults = executor.invokeAll(taskList)
        } catch {
          case e: Exception =>
            stopMonitor.stop()
            log("Interrupted")
            view ! ChangeState("Interrupted")
        }
        from ! ProcessingReady()

      case RetrieveWords(from) => try {
        extractResults.forEach(future => breakable {
          while(!stopMonitor.isStopped || future.isDone)
            if(stopMonitor.isStopped)
              throw new Exception()
            else {
              future.get().foreach(string => stringList = string :: stringList)
              break
            }
        })

      } catch {
        case e: Exception =>
          stopMonitor.stop()
          log("Interrupted")
          view ! ChangeState("Interrupted")
      }

        if(!stopMonitor.isStopped) {
          log("Process PDF completed")
          log("Completion arrived")
        }

        from ! WordsList(Option.apply(stringList))
    }
    Behaviors.same
    }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }

}
