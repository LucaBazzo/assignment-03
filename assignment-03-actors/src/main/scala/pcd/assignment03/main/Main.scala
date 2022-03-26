package pcd.assignment03.main

import pcd.assignment03.concurrency.WordsBagFilling
import pcd.assignment03.view.View

object Main {

  private var DEFAULT_PDF_PATH = ".\\pdf\\"
  private var DEFAULT_IGNORED_PATH = ".\\ignored\\ignored.txt"
  private var DEFAULT_N_WORDS = 5

  def main(args:Array[String]): Unit = {
    if(args.length == 3) {
      DEFAULT_PDF_PATH = args(0)
      DEFAULT_IGNORED_PATH = args(1)
      DEFAULT_N_WORDS = args(2).toInt
    }

    val weight: Int = 700
    val height: Int = 400

    val view: View = new View(weight, height, DEFAULT_PDF_PATH,
      DEFAULT_IGNORED_PATH, DEFAULT_N_WORDS);
    val wordsBag: WordsBagFilling = new WordsBagFilling();

    val controller: Controller = new Controller(view, wordsBag);
    view.addListener(controller);
    view.display();
  }

}
