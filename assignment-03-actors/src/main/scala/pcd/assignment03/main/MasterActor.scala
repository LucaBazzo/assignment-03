package pcd.assignment03.main

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.util.Timeout
import pcd.assignment03.main.MasterActor._
import pcd.assignment03.pdf.ExtractorManager
import pcd.assignment03.pdf.ExtractorManager.{ExtractorManagerMessage, StartProcessing}
import pcd.assignment03.utils.ApplicationConstants
import pcd.assignment03.view.View.{ChangeState, UpdateResult, ViewMessage}
import pcd.assignment03.words.WordsBag.{Clear, Command, Pick}
import pcd.assignment03.words.WordsManager.{ManageList, StopActor, WordsManagerMessage}
import pcd.assignment03.words.{PickActor, WordsBag, WordsManager}

import java.io.File
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.Success

object MasterActor {

  sealed trait MasterMessage
  case class Start(pdfDirectory: File, forbidden: File, nWords: Int) extends MasterMessage
  case class Error() extends MasterMessage
  case class ProcessingReady() extends MasterMessage
  case class ProcessPDFCompleted() extends MasterMessage
  case class WordsLists(result: Option[List[String]]) extends MasterMessage
  case class MaxWordsResult(result: Option[(Integer, List[(String, Integer)])]) extends MasterMessage

  case class StopComputation() extends MasterMessage

  case class WorkEnded() extends MasterMessage

  def apply(controller: ActorRef[ControllerMessage],
            view: ActorRef[ViewMessage],
            wordsBag: ActorRef[Command],
            numActors: Int = ApplicationConstants.NumProcessors): Behavior[MasterMessage] =
    Behaviors.setup { context =>
      new MasterActor(context, controller, view, wordsBag, numActors).standby
    }
}

class MasterActor(val context: ActorContext[MasterMessage],
                  val controller: ActorRef[ControllerMessage],
                  val view: ActorRef[ViewMessage],
                  val wordsBag: ActorRef[Command],
                  val numActors: Int) {

  private val actorType: String = ApplicationConstants.MasterActorType

  private val pdfProcessor: ActorRef[ExtractorManagerMessage] =
    context.spawn(ExtractorManager(view), "ProcessPDF")
  private val picker: ActorRef[Command] = context.spawn(PickActor(wordsBag), "Picker")
  private val wordsManager: ActorRef[WordsManagerMessage] =
    context.spawn(WordsManager(wordsBag, context.self), "WordsManager")

  private var startTime = 0L
  private var nWords = 0

  private val standby: Behavior[MasterMessage] = Behaviors.receive { (ctx, message) =>
    message match {
      case Start(pdfDirectory, forbidden, nWords) =>
        this.startTime = System.currentTimeMillis()
        this.nWords = nWords
        wordsBag ! Clear()
        pdfProcessor ! StartProcessing(pdfDirectory.listFiles(), forbidden, ctx.self)

        gettingPDF

      case StopComputation() =>
        log("Already in standby")
        Behaviors.same

      case _ =>
        log("ERROR")
        Behaviors.stopped
    }
  }

  private val gettingPDF: Behavior[MasterMessage] = Behaviors.receive { (_, message) =>
    message match {
      case WordsLists(stringList) =>
        this.mostFrequentWords(stringList.get)

        computingMostFrequentWords

      case StopComputation() =>
        this.pdfProcessor ! ExtractorManager.StopActor()
        log("Interrupted")
        view ! ChangeState("Interrupted")

        standby
    }
  }

  private val computingMostFrequentWords: Behavior[MasterMessage] = Behaviors.receive { (_, message) =>
    message match {
      case WorkEnded() =>
        log("Work Ended")
        this.pickWordsFrequency(workCompleted = true)
        log("Completed")

        controller ! ProcessCompleted()

        standby

      case StopComputation() =>
        this.wordsManager ! StopActor()
        this.picker ! WordsBag.StopActor()
        log("Interrupted")
        view ! ChangeState("Interrupted")

        standby
    }
  }

  private def mostFrequentWords(stringList: List[String]): Unit = {

    log("Computing most frequent words...")
    view ! ChangeState("Computing most frequent words...")

    log("List of words size: " + stringList.length, "Num of tasks: " + numActors)

    this.wordsManager ! ManageList(stringList, numActors)

    this.pickWordsFrequency()

  }

  private def pickWordsFrequency(workCompleted: Boolean = false): Unit = {
    log("Picking start")

    implicit val timeout: Timeout = 2.seconds
    implicit val scheduler: Scheduler = context.system.scheduler
    //remember if ? doesn't work, it's because of an import that has been forgotten
    val future: scala.concurrent.Future[MasterMessage] = picker ? (replyTo => Pick(nWords, replyTo))
    implicit val ec: ExecutionContextExecutor = context.executionContext
    //remember you can't call context on future callback
    future.onComplete({
      case Success(value) if value.isInstanceOf[MaxWordsResult] =>
        val result: Option[(Integer, List[(String, Integer)])] = value.asInstanceOf[MaxWordsResult].result

        if(result.isDefined) {
          val wordsProcessed: Int = result.get._1
          val wordsFreq: List[(String, Integer)] = result.get._2

          log("Words processed: " + wordsProcessed, wordsFreq.toString())

          view ! UpdateResult(wordsProcessed, wordsFreq, workCompleted)
          if(workCompleted) {
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

  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)

}










/*object MasterActor {

  sealed trait MasterMessage
  case class Start() extends MasterMessage
  case class Error() extends MasterMessage
  case class ProcessingReady() extends MasterMessage
  case class ProcessPDFCompleted() extends MasterMessage
  case class WordsLists(result: Option[List[String]]) extends MasterMessage
  case class MaxWordsResult(result: Option[(Integer, List[(String, Integer)])]) extends MasterMessage

  case class StopComputation() extends MasterMessage

  case class WorkEnded() extends MasterMessage

  private val WAITING_TIME = 1000

  private var forbiddenList: List[String] = List.empty
  private var taskList: util.Collection[PDFExtractTask] = new util.ArrayList[PDFExtractTask]() //TODO ricontrolla

  private val executor = Executors.newCachedThreadPool()
  private var extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()

  private var stringList: List[String] = List.empty

  private var workCompleted = false
  private var startTime = 0L

  private var pdfProcessor: ActorRef[ProcessPDFMessage] = _

  private var taskType: String = ""

  private var picker: ActorRef[Command] = _
  private var processWords: ActorRef[ProcessWordsMessage] = _

  def apply(taskType: String, view: ActorRef[ViewMessage], pdfDirectory: File, forbidden: File,
            wordsBag: ActorRef[Command], stopMonitor: StopMonitor, numTasks: Int,
            nWords: Int): Behavior[MasterMessage] =
    Behaviors.receive { (ctx, message) =>
      message match {
        case Start() => this.taskType = taskType
          this.startTime = System.currentTimeMillis()
          pdfProcessor = ctx.spawn(ProcessPDFActor("Process PDF Actor", forbidden, view,
            pdfDirectory.listFiles(), stopMonitor), "ProcessPDF")
          pdfProcessor ! StartProcessing(ctx.self)

        case WordsLists(strings) =>
          stringList = strings.get
          if (!stopMonitor.isStopped)
            this.mostFrequentWords(ctx, nWords, wordsBag, view, numTasks, stopMonitor)

        case WorkEnded() =>
          log("Completed")
          this.pickWordsFrequency(ctx, picker, stopMonitor, view)

        case StopComputation() =>
          if(this.processWords != null) this.processWords ! StopActor()
          if(this.picker != null) this.picker ! WordsBagFilling.StopActor()
          log("Interrupted")
          view ! ChangeState("Interrupted")
          Behaviors.stopped

        case _ => log("ERROR")
      }

      Behaviors.same

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
    this.picker = context.spawn(PickActor(nWords, wordsBag), "Picker")

    log("Computing most frequent words...")
    view ! ChangeState("Computing most frequent words...")

    wordsBag ! Clear()

    log("List of words size: " + stringList.length, "Num of tasks: " + numTasks)

    this.processWords = context.spawn(ProcessWords(wordsBag, context.self), "WordsProcessor")
    processWords ! ProcessList(stringList, numTasks)

    this.pickWordsFrequency(context, picker, stopMonitor, view)


    /*//TODO temporaneo, da mettere a posto
    if(!stopMonitor.isStopped) {
      log("Wait words tasks completion")
      try {
        while (true) {
          this.pickWordsFrequency(context, picker, stopMonitor, view)
          waitFor(WAITING_TIME)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          stopMonitor.stop()
      }
    }*/

    //this.pickWordsFrequency(context, picker, stopMonitor, view)

    /*// - 1 present for the pick task the map of strings
    val numTask: Int = numTasks - 1

    val numOfWords: Int = stringList.length
    var startingIndex: Int = 0
    val dx: Int = numOfWords / numTask

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
    }*/

    /*if(!stopMonitor.isStopped) {
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
    }*/
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

}*/
