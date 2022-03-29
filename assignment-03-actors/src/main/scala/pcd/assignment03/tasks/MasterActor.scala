package pcd.assignment03.tasks


import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Clear, Command, Pick}
import pcd.assignment03.main.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.tasks.MasterActor.{MasterMessage, MaxWordsResult, Start}
import pcd.assignment03.tasks.ProcessPDFActor.{ProcessPDFMessage, RetrieveWords, StartProcessing, WordsList}

import java.io.{File, FileNotFoundException}
import java.util
import java.util.Scanner
import java.util.concurrent.{Executors, Future}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.Success
import scala.util.control.Breaks.{break, breakable}

object MasterActor {

  sealed trait MasterMessage
  case class Start() extends MasterMessage
  case class MaxWordsResult(result: Option[(Integer, List[(String, Integer)])]) extends MasterMessage

  private val WAITING_TIME = 10

  private var forbiddenList: List[String] = List.empty
  private var taskList: util.Collection[PDFExtractTask] = new util.ArrayList[PDFExtractTask]() //TODO ricontrolla

  private val executor = Executors.newCachedThreadPool()
  private var extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()

  private var stringList: List[String] = List.empty

  private var workCompleted = false
  private var startTime = 0L
  private var workd: Boolean = false

  private var taskType: String = ""

  def apply(taskType: String, view: ActorRef[ViewMessage], pdfDirectory: File, forbidden: File,
            wordsBag: ActorRef[Command], stopMonitor: StopMonitor, numTasks: Int,
            nWords: Int): Behavior[MasterMessage] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case Start() => this.taskType = taskType
          this.startTime = System.currentTimeMillis()
          val pdfProcessor = ctx.spawn(ProcessPDFActor("Process PDF Actor", forbidden, view,
                                       pdfDirectory.listFiles(), stopMonitor), "ProcessPDF")
          pdfProcessor ! StartProcessing()
          waitFor(5000)
          implicit val timeout: Timeout = 2.seconds
          implicit val scheduler: Scheduler = ctx.system.scheduler
          //remember if ? doesn't work, it's because of an import that has been forgotten
          val f: scala.concurrent.Future[ProcessPDFMessage] = pdfProcessor ? (replyTo => RetrieveWords(replyTo))
          implicit val ec: ExecutionContextExecutor = ctx.executionContext
          //remember you can't call context on future callback
          f.onComplete({
            case Success(value) if value.isInstanceOf[WordsList] =>
              var result: Option[List[String]] = Option.empty
              try {
                result = value.asInstanceOf[WordsList].result
                if (result.isDefined) {
                  stringList = result.get
                  workd = true
                }
                log("Process PDF completed")
                log("Completion arrived")
              } catch {
                case e: Exception =>
                  e.printStackTrace()
                  stopMonitor.stop()
                  log("Interrupted")
                  view ! ChangeState("Interrupted")
              }

            case _ => log("ERROR")
          })
          waitFor(5000)
          if (!stopMonitor.isStopped)
            this.mostFrequentWords(ctx, nWords, wordsBag, view, numTasks, stopMonitor)

        case _ =>
      }

      Behaviors.empty

    }

  private def processPDF(forbidden: File, view: ActorRef[ViewMessage], pdfDirectory: Array[File],
                        stopMonitor: StopMonitor): Unit = {
    this.startTime = System.currentTimeMillis()

    try {
      var reader: Scanner = new Scanner(forbidden)
      while (reader.hasNextLine) {
        var data: String = reader.nextLine()
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

    try {
      extractResults.forEach(future => breakable {
        while(!stopMonitor.isStopped || future.isDone)
          if(stopMonitor.isStopped)
            throw new Exception()
          else {
            future.get().foreach(string => stringList = string :: stringList)
            break
          }
      })

      if(!stopMonitor.isStopped) {
        log("Process PDF completed")
        log("Completion arrived")
      }

    } catch {
      case e: Exception =>
        stopMonitor.stop()
        log("Interrupted")
        view ! ChangeState("Interrupted")

    }
  }

  private def mostFrequentWords(context: ActorContext[MasterMessage], nWords: Int,
                                wordsBag: ActorRef[Command], view: ActorRef[ViewMessage],
                                numTasks: Int, stopMonitor: StopMonitor): Unit = {
    val picker = context.spawn(PickActor("Pick Actor", nWords, wordsBag), "Picker")

    log("Computing most frequent words...")
    view ! ChangeState("Computing most frequent words...")

    wordsBag ! Clear()

    // - 1 present for the pick task the map of strings
    val numTask: Int = numTasks - 1

    val numOfWords: Int = stringList.length
    var startingIndex: Int = 0
    val dx: Int = numOfWords / numTask

    log("List of words size: " + numOfWords, "Num of tasks: " + numTask)

    var wordsResults: List[Future[Boolean]] = List.empty
    for(i <- 0 until numTask) {
      try {
        val res: Future[Boolean] = executor.submit(new WordsTask("Words Task", stringList, startingIndex,
          startingIndex + dx, wordsBag, stopMonitor))
        wordsResults = res :: wordsResults
        startingIndex = startingIndex + dx
      } catch {
        case e: Exception =>
          e.printStackTrace()
          stopMonitor.stop()
      }
    }

    try {
      val res: Future[Boolean] = executor.submit(new WordsTask("Words Task", stringList, startingIndex,
        numOfWords, wordsBag, stopMonitor))
      wordsResults = res :: wordsResults
    } catch {
      case e: Exception =>
        e.printStackTrace()
        stopMonitor.stop()
    }

    if(!stopMonitor.isStopped) {
      log("Wait words tasks completion")
      try {
        while(!wordsResults.forall(res => res.isDone)) {
          this.pickWordsFrequency(context, picker, stopMonitor, view)
          waitFor(WAITING_TIME)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          stopMonitor.stop()
      }

      if(!stopMonitor.isStopped) {
        this.workCompleted = wordsResults.forall(res => {
          var result: Boolean = false
          try {
            result = res.get()
          } catch {
            case e: Exception => e.printStackTrace()
          }
          result
        })

        this.pickWordsFrequency(context, picker, stopMonitor, view)
        log("Done")
      }
      else {
        log("Interrupted")
        view ! ChangeState("Interrupted")
      }
    }
    else {
      log("Interrupted")
      view ! ChangeState("Interrupted")
    }
  }

  private def pickWordsFrequency(context: ActorContext[MasterMessage], picker: ActorRef[Command],
                                 stopMonitor: StopMonitor, view: ActorRef[ViewMessage]): Unit = {
    log("Picking start")

    implicit val timeout: Timeout = 2.seconds
    implicit val scheduler: Scheduler = context.system.scheduler
    //remember if ? doesn't work, it's because of an import that has been forgotten
    val f: scala.concurrent.Future[MasterMessage] = picker ? (replyTo => Pick(replyTo))
    implicit val ec: ExecutionContextExecutor = context.executionContext
    //remember you can't call context on future callback
    f.onComplete({
      case Success(value) if value.isInstanceOf[MaxWordsResult] =>
        var result: Option[(Integer, List[(String, Integer)])] = Option.empty
        try {
          result = value.asInstanceOf[MaxWordsResult].result
          log("Pick result ready")
        } catch {
          case e: Exception =>
            e.printStackTrace()
            stopMonitor.stop()
            log("Interrupted")
            view ! ChangeState("Interrupted")
        }

        if(result.isDefined) {
          val wordsProcessed: Int = result.get._1
          val wordsFreq: List[(String, Integer)] = result.get._2

          log("Words processed: " + wordsProcessed, wordsFreq.toString())

          view ! UpdateResult(wordsProcessed, wordsFreq, this.workCompleted)
          if(this.workCompleted) {
            log("Most Frequent Words completed")

            val timeElapsed: Long = System.currentTimeMillis() - startTime
            log("Completed - time elapsed: " + timeElapsed)
            view ! ChangeState("Completed - time elapsed: "+ timeElapsed)
          }
        }
        else {
          log("Empty bag")
        }

      case _ => log("ERROR")
    })
  }


  @throws[InterruptedException]
  private def waitFor(ms: Long): Unit = {
    Thread.sleep(ms)
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[" + taskType + "] " + msg)
    }
  }

}
