package com.ariskk.pravegasource

import scala.annotation.tailrec
import scala.concurrent.duration._

object AwaitCond {

  def awaitCond(p: => Boolean, max: FiniteDuration = 5.seconds, interval: FiniteDuration = 100.millis) {
    def now = System.nanoTime.nanos.toNanos
    val stop = now + max.toNanos

    @tailrec
    def poll() {
      if (!p) {
        assert(now < stop, s"timeout")
        Thread.sleep(interval.toMillis)
        poll()
      }
    }

    poll()
  }

}
