package pcd.assignment03.pdf

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.text.PDFTextStripper
import pcd.assignment03.pdf.ProcessPDFActor.{ProcessPDFMessage, RetrieveWords}
import pcd.assignment03.utils.ApplicationConstants

import java.io.{File, IOException}

object PDFExtractActor {

  sealed trait PDFExtractMessage
  case class StartExtraction(from: ActorRef[ProcessPDFMessage]) extends PDFExtractMessage
  case class ResultList(result: Option[List[String]]) extends PDFExtractMessage

  private val actorType: String = ApplicationConstants.PDFExtractActorType

  def apply(forbiddenList: List[String], pdfDoc: File): Behavior[PDFExtractMessage] = Behaviors.receive { (_, message) =>
    message match {

      case StartExtraction(from) =>
        var resultString: String = ""

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

        } catch { //TODO attenzione, probabilmente si deve avvisare il master
          case ex: IOException => ex.printStackTrace()
        }

        from ! RetrieveWords(Option.apply(resultString.split(" ").toList))

        Behaviors.stopped

    }

    //Behaviors.same
  }

  private def log(messages: String*): Unit = {
    for (msg <- messages) {
      System.out.println("[" + actorType + "] " + msg)
    }
  }

}

/*class PDFExtractTask(val forbiddenList: List[String], val pdfDoc: File) extends Callable[List[String]]{

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

  private def log(messages: String*): Unit = {
    for (msg <- msgs) {
      System.out.println("[PDFExtractTask] " + msg)
    }
  }
}*/
