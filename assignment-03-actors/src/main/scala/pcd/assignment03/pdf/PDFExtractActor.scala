package pcd.assignment03.pdf

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.text.PDFTextStripper
import pcd.assignment03.pdf.ExtractorManager.{ExtractorManagerMessage, RetrieveWords}
import pcd.assignment03.utils.ApplicationConstants

import java.io.File

/** Actor that extracts words from pdf document
 *
 */
object PDFExtractActor {

  sealed trait PDFExtractMessage
  case class StartExtraction(from: ActorRef[ExtractorManagerMessage]) extends PDFExtractMessage
  case class ResultList(result: Option[List[String]]) extends PDFExtractMessage

  private val actorType: String = ApplicationConstants.PDFExtractActorType

  /**
   *
   * @param forbiddenList list that contains words to be ignored
   * @param pdfDoc pdf document handled by this actor
   */
  def apply(forbiddenList: List[String], pdfDoc: File): Behavior[PDFExtractMessage] =
    Behaviors.receive { (ctx, message) =>
    message match {

      case StartExtraction(from) =>
        this.log("Document " + pdfDoc.getName + " loaded")
        val document: PDDocument = PDDocument.load(pdfDoc)
        val ap: AccessPermission = document.getCurrentAccessPermission
        if (!ap.canExtractContent) {
          log("You do not have permission to extract text")
        } else {
          val stripper: PDFTextStripper = new PDFTextStripper()
          stripper.setSortByPosition(true)
          val text: String = stripper.getText(document)

          from ! RetrieveWords(Option.apply(computeResult(text, forbiddenList)), ctx.self)
        }
        document.close()

        Behaviors.stopped
      }
    }

  private def computeResult(text: String, forbiddenList: List[String]): List[String] = {
    var resultString: String = text.toLowerCase()
      .trim()
      .replaceAll("\n", " ")
      .replaceAll("\r", " ")
      .replaceAll(" +", " ")

    for (x: String <- forbiddenList)
      resultString = resultString.replaceAll(" " + x + " ", "")

    resultString.split(" ").toList
  }


  private def log(messages: String*): Unit = for (msg <- messages) println("[" + actorType + "] " + msg)

}
