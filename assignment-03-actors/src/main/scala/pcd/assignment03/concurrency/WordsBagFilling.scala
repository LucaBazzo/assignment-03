package pcd.assignment03.concurrency

import scala.collection.mutable

class WordsBagFilling {

  private val map: mutable.HashMap[String, Int] = new mutable.HashMap[String, Int]()

  def clearBag(): Unit = synchronized {
    this.map.clear()
  }

  def getBag: mutable.HashMap[String, Int] = synchronized {
    this.map.clone()
  }

  def addElement(word: String): Unit = synchronized {
    var count: Int = 1
    if(this.map.contains(word)) {
      count = this.map(word) + 1
    }
    this.map.put(word, count)
    notify()
  }

}
