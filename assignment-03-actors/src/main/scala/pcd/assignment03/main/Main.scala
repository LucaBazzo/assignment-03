package pcd.assignment03.main

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import pcd.assignment03.concurrency.WordsBagFilling
import pcd.assignment03.tasks.PickActor
import pcd.assignment03.view.View

object Main {

  private var DEFAULT_PDF_PATH = ".\\pdf\\"
  private var DEFAULT_IGNORED_PATH = ".\\ignored\\ignored.txt"
  private var DEFAULT_N_WORDS = 5

  def apply(initialConfig: Array[String]): Behavior[NotUsed] = Behaviors.setup { context =>

    if(initialConfig.length == 3) {
      DEFAULT_PDF_PATH = initialConfig(0)
      DEFAULT_IGNORED_PATH = initialConfig(1)
      DEFAULT_N_WORDS = initialConfig(2).toInt
    }

    val weight: Int = 700
    val height: Int = 400

    val view: View = new View(weight, height, DEFAULT_PDF_PATH,
      DEFAULT_IGNORED_PATH, DEFAULT_N_WORDS)
    //val wordsBag: WordsBagFilling = new WordsBagFilling()
    val wordsBag = context.spawn(WordsBagFilling(), "WordsBag")
    val picker = context.spawn(PickActor("Pick Actor", 5, wordsBag), "Picker") //TODO spostare nel service quando sarà un actor

    /*wordsBag ! Update("ciao")
    wordsBag ! Update("piacere")
    wordsBag ! Update("ciao")

    picker ! Pick()*/



    val controller: Controller = new Controller(view, wordsBag, picker, context)
    view.addListener(controller)
    view.display()

    Behaviors.empty
  }




  def main(args:Array[String]): Unit = {
    ActorSystem(Main(args), "Concurrency")
  }

}
