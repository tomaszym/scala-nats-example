package com.wearerealitygames.blog.nats

import java.util.concurrent.TimeoutException

import io.nats.client._
import monix.execution.FutureUtils.extensions._
import monix.execution.Scheduler

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}

class NatsTransport(natsUrl: String)(implicit scheduler: Scheduler) {

  protected val conn: Connection = Nats.connect(natsUrl: String)

  def subscribe(subject: String)(handler: Message => Unit): AsyncSubscription = conn.subscribe(subject, new MessageHandler {
    override def onMessage(msg: Message): Unit = handler(msg)
  })

  def tell(subject: String, payload: Array[Byte]): Unit = {
    val inbox: String = subject + "." + conn.newInbox()
    val msg = new Message(
      subject,
      inbox,
      payload
    )
    conn.publish(msg)
  }

  def ask(subject: String, payload: Array[Byte]): Future[Array[Byte]] = {
    val inbox: String = subject + "." + conn.newInbox()
    val msg = new Message(
      subject,
      inbox,
      payload
    )

    val promise = Promise[Array[Byte]]()

    val subscription = conn.subscribe(msg.getReplyTo, (msg: Message) => promise.success(msg.getData))

    conn.publish(msg)


    val success = promise.future.timeoutTo(10.seconds, {
      subscription.unsubscribe()
      Future.failed(new TimeoutException())
    })
    success.onComplete { _ =>
      subscription.unsubscribe()
    }
    success
  }

  def flush() = conn.flush()
  def close() = conn.close()
}

