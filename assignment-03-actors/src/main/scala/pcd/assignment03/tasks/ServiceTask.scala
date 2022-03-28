package pcd.assignment03.tasks


import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.concurrency.WordsBagFilling.{Clear, Command, Pick}
import pcd.assignment03.view.View

import java.io.{File, FileNotFoundException}
import java.util
import java.util.Scanner
import java.util.concurrent.{Executors, Future}
import scala.util.control.Breaks.{break, breakable}


class ServiceTask(val taskType: String, val view: View, val pdfDirectory: File, val forbidden: File,
                  var wordsBag: ActorRef[Command], val stopMonitor: StopMonitor, var numTasks: Int,
                  val nWords: Int, var picker: ActorRef[Command], var context: ActorContext[NotUsed])
  extends Runnable {

  private val WAITING_TIME = 10

  private var forbiddenList: List[String] = List.empty
  private var taskList: util.Collection[PDFExtractTask] = new util.ArrayList[PDFExtractTask]() //TODO ricontrolla

  private val executor = Executors.newCachedThreadPool()
  private var extractResults: util.List[Future[List[String]]] = new util.LinkedList[Future[List[String]]]()

  private var stringList: List[String] = List.empty

  private var workCompleted = false
  private var startTime = 0L

  override def run(): Unit = {
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
      reader.close();
    } catch {
      case e: FileNotFoundException => {
        log("An error occurred.")
        e.printStackTrace()
      }
    }

    view.changeState("Getting PDF...")

    log("Wait completion")

    pdfDirectory.listFiles().foreach(pdfFile => {
      val task: PDFExtractTask = new PDFExtractTask(forbiddenList, pdfFile, stopMonitor)
      taskList.add(task)
    })

    view.changeState("PDF Processing...");

    try {
      extractResults = executor.invokeAll(taskList)
    } catch {
      case e: Exception => {
        stopMonitor.stop()
        log("Interrupted")
        view.changeState("Interrupted")
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
        view.changeState("Interrupted")
      }
    }
  }

  private def mostFrequentWords(): Unit = {
    log("Computing most frequent words...")
    view.changeState("Computing most frequent words...")

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
          this.pickWordsFrequency()
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

        this.pickWordsFrequency()
        log("Done")
      }
      else {
        log("Interrupted");
        view.changeState("Interrupted");
      }
    }
    else {
      log("Interrupted");
      view.changeState("Interrupted");
    }
  }

  private def pickWordsFrequency(): Unit = {
    log("Starting Pick Task")
    /*val pickResult: Future[Option[(Integer, List[(String, Integer)])]] =
      executor.submit(new PickTask("Pick Task", nWords, wordsBag))*/

    picker ! Pick()

    /*var result: Option[(Integer, List[(String, Integer)])] = Option.empty
    try {
      result = pickResult.get()
      log("Pick result ready")
    } catch {
      case e: Exception =>
        e.printStackTrace()
        stopMonitor.stop()
        log("Interrupted")
        view.changeState("Interrupted")
    }

    val x: Pair[String, String] = ("ciao", "Ciao")

    if(!result.isEmpty) {
      val wordsProcessed: Int = result.get._1
      val wordsFreq: List[(String, Integer)] = result.get._2

      //conversion of Scala List to Java list for the View
      val w: util.List[Pair[String, Integer]] = new util.ArrayList[Pair[String, Integer]]()
      wordsFreq.foreach(tuple => w.add(tuple))

      log("Words processed: " + wordsProcessed, wordsFreq.toString());

      view.updateResult(wordsProcessed, w, this.workCompleted)
      if(this.workCompleted) {
        log("Most Frequent Words completed");

        val timeElapsed: Long = System.currentTimeMillis() - startTime
        log("Completed - time elapsed: " + timeElapsed)
        view.changeState("Completed - time elapsed: "+ timeElapsed)
      }
    }
    else {
      log("Empty bag");
    }*/
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
