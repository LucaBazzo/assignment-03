package pcd.assignment03.concurrency

class StopMonitor(var stopped: Boolean = false) {

  def stop(): Unit = synchronized {
    stopped = true
    notifyAll()
  }

  def isStopped: Boolean = synchronized {
    stopped
  }

}
