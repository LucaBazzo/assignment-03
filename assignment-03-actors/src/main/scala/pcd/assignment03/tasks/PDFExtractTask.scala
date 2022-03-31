package pcd.assignment03.tasks

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.text.PDFTextStripper
import pcd.assignment03.concurrency.StopMonitor
import pcd.assignment03.tasks.ProcessPDFActor.{ProcessPDFMessage, RetrieveWords}

import java.io.{File, IOException}
import java.util.concurrent.Callable

object  PDFExtractActor {

  sealed trait PDFExtractMessage
  case class StartExtraction(from: ActorRef[ProcessPDFMessage]) extends PDFExtractMessage
  //case class RequestResult(from: ActorRef[PDFExtractMessage]) extends PDFExtractMessage
  case class ResultList(result: Option[List[String]]) extends PDFExtractMessage
  //var result: List[String] = List.empty

  def apply(forbiddenList: List[String], pdfDoc: File,
            stopMonitor: StopMonitor): Behavior[PDFExtractMessage] = Behaviors.receive { (ctx, message) =>
    message match {

      case StartExtraction(from) =>
        var resultString: String = ""

        if (!stopMonitor.isStopped) {

          try {
            this.log("Document " + pdfDoc.getName + " loaded")
            val document: PDDocument = PDDocument.load(pdfDoc)
            val ap: AccessPermission = document.getCurrentAccessPermission
            if (!ap.canExtractContent) {
              throw new IOException("You do not have permission to extract text")
            }

            val stripper: PDFTextStripper = new PDFTextStripper()
            stripper.setSortByPosition(true)
            val text: String = stripper.getText(document)

            resultString = text.toLowerCase().trim()
            resultString = resultString.replaceAll("\n", " ")
            resultString = resultString.replaceAll("\r", " ")
            resultString = resultString.replaceAll(" +", " ")
            for (x: String <- forbiddenList)
              resultString = resultString.replaceAll(" " + x + " ", "")

            document.close()

          } catch {
            case ex: IOException => ex.printStackTrace()
          }

        }
        else
          throw new InterruptedException()

        from ! RetrieveWords(Option.apply(resultString.split(" ").toList))

    }

    Behaviors.same
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[PDFExtractTask] " + msg)
    }
  }

}

class PDFExtractTask(val forbiddenList: List[String], val pdfDoc: File, var stopMonitor: StopMonitor) extends Callable[List[String]]{

  override def call(): List[String] = {
    var resultString: String = ""

    if(!this.stopMonitor.isStopped) {

      try {
        this.log("Document " + pdfDoc.getName + " loaded")
        val document: PDDocument = PDDocument.load(pdfDoc)
        val ap: AccessPermission = document.getCurrentAccessPermission
        if (!ap.canExtractContent) {
          throw new IOException("You do not have permission to extract text")
        }

        val stripper: PDFTextStripper = new PDFTextStripper()
        stripper.setSortByPosition(true)
        var text: String = stripper.getText(document)

        resultString = text.toLowerCase().trim()
        resultString = resultString.replaceAll("\n", " ")
        resultString = resultString.replaceAll("\r", " ")
        resultString = resultString.replaceAll(" +", " ")
        for (x: String <- forbiddenList)
        resultString = resultString.replaceAll(" " + x + " ", "")

        document.close()

      } catch {
        case ex: IOException => ex.printStackTrace()
      }

    }
    else
      throw new InterruptedException()

    resultString.split(" ").toList
  }

  private def log(msgs: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[PDFExtractTask] " + msg)
    }
  }
}
