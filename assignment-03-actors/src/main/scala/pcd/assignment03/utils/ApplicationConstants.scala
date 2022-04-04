package pcd.assignment03.utils

object ApplicationConstants {

  val DisplayWeight: Int = 700
  val DisplayHeight: Int = 400

  val DefaultPDFPath = ".\\pdf\\"
  val DefaultIgnoredPath = ".\\ignored\\ignored.txt"
  val DefaultNWords = 5

  val NumProcessors: Int = Runtime.getRuntime.availableProcessors() + 1

  val MasterActorType: String = "Master Actor"
  val PDFExtractActorType: String = "PDF Extract Actor"
  val PickerActorType: String = "Picker Actor"
  val WordsActorType: String = "Words Actor"
  val WordsManagerActorType: String = "Words Manager Actor"

}
