package pcd.assignment03.tasks


import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Clear, Command, Pick}
import pcd.assignment03.main.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.tasks.MasterActor.{MasterMessage, MaxWordsResult, Start}

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

  def apply(taskType: String, view: ActorRef[ViewMessage], pdfDirectory: File, forbidden: File,
            wordsBag: ActorRef[Command], stopMonitor: StopMonitor, numTasks: Int,
            nWords: Int): Behavior[MasterMessage] =
    Behaviors.setup { context =>
      new MasterActor(taskType, view, pdfDirectory, forbidden, wordsBag, stopMonitor, numTasks,
        nWords, context).start
    }
}


class MasterActor(val taskType: String, val view: ActorRef[ViewMessage], val pdfDirectory: File, val forbidden: File,
                  var wordsBag: ActorRef[Command], val stopMonitor: StopMonitor, var numTasks: Int,
                  val nWords: Int, var context: ActorContext[MasterMessage]) {

  private val WAITING_TIME = 10

  private var forbiddenList: List[String] = List.empty
  private var taskList: util.Collection[PDFExtractTask] = new util.ArrayList[PDFExtractTask]() //TODO ricontrolla

  private val executor = Executors.newCachedThreadPool()
  private var extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()

  private var stringList: List[String] = List.empty

  private var workCompleted = false
  private var startTime = 0L

  private val start: Behavior[MasterMessage] = Behaviors.receiveMessagePartial {
    case Start() =>
      run()
      Behaviors.same
  }

  def run(): Unit = {
    this.processPDF()
    if (!stopMonitor.isStopped)
      this.mostFrequentWords()
  }

  private def processPDF(): Unit = {
    this.startTime = System.currentTimeMillis()

    try {
      var reader: Scanner = new Scanner(forbidden)
      while (reader.hasNextLine()) {
        var data: String = reader.nextLine()
        forbiddenList = data :: forbiddenList
      }
      reader.close()
    } catch {
      case e: FileNotFoundException => {
        log("An error occurred.")
        e.printStackTrace()
      }
    }

    view ! ChangeState("Getting PDF...")

    log("Wait completion")

    pdfDirectory.listFiles().foreach(pdfFile => {
      val task: PDFExtractTask = new PDFExtractTask(forbiddenList, pdfFile, stopMonitor)
      taskList.add(task)
    })

    view ! ChangeState("PDF Processing...");

    try {
      extractResults = executor.invokeAll(taskList)
    } catch {
      case e: Exception => {
        stopMonitor.stop()
        log("Interrupted")
        view ! ChangeState("Interrupted")
      }
    }

    try {
      extractResults.forEach(future => breakable {
        while(!this.stopMonitor.isStopped || future.isDone)
          if(this.stopMonitor.isStopped)
            throw new Exception()
          else {
            future.get().foreach(string => stringList = string :: stringList)
            break
          }
      })

      if(!this.stopMonitor.isStopped) {
        log("Process PDF completed")
        log("Completion arrived")
      }

    } catch {
      case e: Exception => {
        this.stopMonitor.stop()
        log("Interrupted")
        view ! ChangeState("Interrupted")
      }
    }
  }

  private def mostFrequentWords(): Unit = {
    val picker = context.spawn(PickActor("Pick Actor", nWords, wordsBag), "Picker")

    log("Computing most frequent words...")
    view ! ChangeState("Computing most frequent words...")

    this.wordsBag ! Clear()

    // - 1 present for the pick task the map of strings
    numTasks = numTasks - 1

    val numOfWords: Int = stringList.length
    var startingIndex: Int = 0
    val dx: Int = numOfWords / numTasks

    log("List of words size: " + numOfWords, "Num of tasks: " + numTasks)

    var wordsResults: List[Future[Boolean]] = List.empty
    for(i <- 0 until numTasks) {
      try {
        val res: Future[Boolean] = executor.submit(new WordsTask("Words Task", stringList, startingIndex,
          startingIndex + dx, wordsBag, stopMonitor));
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
      log("Wait words tasks completion");
      try {
        while(!wordsResults.forall(res => res.isDone)) {
          this.pickWordsFrequency(picker)
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
            result = res.get();
          } catch {
            case e: Exception => e.printStackTrace()
          }
          result
        })

        this.pickWordsFrequency(picker)
        log("Done")
      }
      else {
        log("Interrupted");
        view ! ChangeState("Interrupted");
      }
    }
    else {
      log("Interrupted");
      view ! ChangeState("Interrupted");
    }
  }

  private def pickWordsFrequency(picker: ActorRef[Command]): Unit = {
    log("Picking start")

    implicit val timeout: Timeout = 2.seconds
    implicit val scheduler: Scheduler = context.system.scheduler
    //remember if ? doesn't work, it's because of an import that has been forgotten
    val f: scala.concurrent.Future[MasterMessage] = picker ? (replyTo => Pick(replyTo))
    implicit val ec: ExecutionContextExecutor = context.executionContext
    //remember you can't call context on future callback
    f.onComplete({
      case Success(value) if value.isInstanceOf[MaxWordsResult] => {
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

        if(!result.isEmpty) {
          val wordsProcessed: Int = result.get._1
          val wordsFreq: List[(String, Integer)] = result.get._2

          log("Words processed: " + wordsProcessed, wordsFreq.toString());

          view ! UpdateResult(wordsProcessed, wordsFreq, this.workCompleted)
          if(this.workCompleted) {
            log("Most Frequent Words completed");

            val timeElapsed: Long = System.currentTimeMillis() - startTime
            log("Completed - time elapsed: " + timeElapsed)
            view ! ChangeState("Completed - time elapsed: "+ timeElapsed)
          }
        }
        else {
          log("Empty bag");
        }
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
