//package com.wearerealitygames.blog.nats
//
//import io.nats.client.{Connection, Message, Nats}
//import monix.execution.Scheduler
//
//import scala.concurrent.duration.FiniteDuration
//import scala.concurrent.{Future, Promise}
//
//
//
//
//
//
//object bareNats {
//
//
//}
//
//
//
//case class NatsConfig(
//  url: String,
//  ackTimeout: FiniteDuration
//)
//class NatsTransport(
//  config: NatsConfig
//)(implicit
//  scheduler: Scheduler,
//  timeout: FiniteDuration
//) extends ControlProtocolTransport {
//
//  //  protected val options = new Options.Builder()
//  protected val conn: Connection = Nats.connect(config.url)
//
//  override def ask(subject: String, payload: Array[Byte]): TransportResponse = {
//    val inbox: String = "ctrlreply." + conn.newInbox()
//    val msg = new Message(
//      subject,
//      inbox,
//      payload
//    )
//
//    conn.publish(msg)
//    val promise = Promise[Either[ProtocolError, Array[Byte]]]()
//    val subscription = conn.subscribe(msg.getReplyTo, (msg: Message) => promise.success(Right(msg.getData)))
//
//    def futureFailedTimeout: TransportResponse = Future.successful(Left(AckTimeout(s"Ack lost on subject $subject")))
//
//    val success = promise.future
//    success.onComplete { _ =>
//      subscription.unsubscribe()
//    }
//    Future.firstCompletedOf(Seq(
//      success,
//      after(timeout.duration, scheduler){
//        subscription.unsubscribe()
//        futureFailedTimeout
//      }
//    ))
//
//  }
//}
//
