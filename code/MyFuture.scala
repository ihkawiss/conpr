import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

object MyFuture {

  val ex: ExecutorService = Executors.newFixedThreadPool(8)

  def async[A](body: => A): MyFutureClazz[A] = {
    val result: MyFutureClazz[A] = new MyFutureClazz[A]()

    ex.execute(() => {
      val funcResult = body
      result.setResult(funcResult)
    })

    result // immediate return
  }

  class MyFutureClazz[A] {
    val callbacks = Array[A => Unit]()
    var result: A = null.asInstanceOf[A]
    val lock = new Object()

    def setResult(result: A): Unit = {
      lock.synchronized({
        this.result = result

        for (i <- 0 to callbacks.length) {
          callbacks(i).apply(result)
        }
      })
    }

    def onSuccess(callback: A => Unit): Unit = {
      lock.synchronized({
        if (result != null) {
          callback.apply(result)
        } else {
          callbacks :+ callback
        }
      })
    }

    def map[B](f: A => B): MyFutureClazz[B] = {
      ???
    }
  }

}