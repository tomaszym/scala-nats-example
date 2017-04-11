package com.wearerealitygames.blog.nats

import java.util.concurrent.TimeoutException

import io.nats.client.{Connection, Message, Nats}
import monix.execution.Scheduler
import monix.execution.schedulers.TestScheduler.Task

import scala.concurrent.{Future, Promise}

class NatsTransport(natsUrl: String, scheduler: Scheduler) {

//  protected val options = new Options.Builder()
  protected val conn: Connection = Nats.connect(natsUrl: String)

  override def ask(subject: String, payload: Array[Byte]) = {
    val inbox: String = "ctrlreply." + conn.newInbox()
    val msg = new Message(
      subject,
      inbox,
      payload
    )

    conn.publish(msg)
    val promise = Promise[Either[Throwable, Array[Byte]]]()
    val subscription = conn.subscribe(msg.getReplyTo, (msg: Message) => promise.success(Right(msg.getData)))

    def futureFailedTimeout = Future.failed(new TimeoutException())

    val success = Task..future
    success.onComplete { _ =>
      subscription.unsubscribe()
    }
    Future.firstCompletedOf(Seq(
      success,
      after(timeout.duration, scheduler){
        subscription.unsubscribe()
        futureFailedTimeout
      }
    ))

  }
}

