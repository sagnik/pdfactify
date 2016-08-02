package edu.psu.sagnik.research.table.impl

/**
  * Created by schoudhury on 7/25/16.
  */
import scala.concurrent._


sealed class TimeoutException extends RuntimeException

object FutureTimeout {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit class FutureTimeoutLike[T](f: Future[T]) {
    def withTimeout(ms: Long): Future[T] = Future.firstCompletedOf(List(f, Future {
      Thread.sleep(ms)
      throw new TimeoutException
    }))

    lazy val withTimeout: Future[T] = withTimeout(60000) // 1 minute time out
  }

}