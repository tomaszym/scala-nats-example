package com.wearerealitygames.blog.nats

import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration._

object ScalaAliceAndBob extends LazyLogging {
  val subject = "translate"

  def translate(en: String): Option[String] = en match {
    case "cat" => Some("kato")
    case "dog" => Some("hundo")
    case _: String => None
  }

  def main(args: Array[String]): Unit = {
    import monix.execution.Scheduler.Implicits.global

    val conn = new NatsTransport("nats://0.0.0.0:4222")

    val subscription = conn.subscribe(subject) { msg =>
      val questionPayload = new String(msg.getData, StandardCharsets.UTF_8)
      println(s"A: Got question: $questionPayload")
      val answerPayload = translate(questionPayload).getOrElse("unknown")
      println(s"A: $questionPayload means $answerPayload")
      conn.tell(msg.getReplyTo, answerPayload.getBytes(StandardCharsets.UTF_8))
    }

    println("B: asking question cat")
    val done = conn.ask(subject,"cat".getBytes(StandardCharsets.UTF_8)) map { response =>
      val responsePayload = new String(response, StandardCharsets.UTF_8)
      println(s"B: Got response: $responsePayload")
    }
    conn.flush()
    subscription.unsubscribe()

    Await.ready(done map { _ =>
      conn.close()
    }, 2.seconds)
  }
}
