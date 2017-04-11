package com.wearerealitygames.blog.nats

import java.nio.charset.StandardCharsets
import io.nats.client._

object JavaClient  {
  val subject = "translate"

  def translate(en: String): Option[String] = en match {
    case "cat" => Some("kato")
    case "dog" => Some("hundo")
    case s: String => None
  }

  def main(args: Array[String]): Unit = {
    val conn: Connection = Nats.connect("nats://0.0.0.0:4222")

    val aliceSub = alice(conn)
    val bobSub = bob(conn)

    Thread.sleep(5)

    conn.flush()
    aliceSub.unsubscribe()
    bobSub.unsubscribe()
    conn.close()

  }

  def alice(conn:Connection): AsyncSubscription = conn.subscribe(subject, new MessageHandler {
    override def onMessage(question: Message): Unit = {
      val questionPayload = new String(question.getData, StandardCharsets.UTF_8)
      println(s"A: Question ${questionPayload} received")
      val answerPayload = translate(questionPayload).getOrElse("unknown")
      val answer = new Message(
        question.getReplyTo,
        conn.newInbox(),
        answerPayload.getBytes(StandardCharsets.UTF_8)
      )
//      println("A: Publishing answer")
      conn.publish(answer)
      conn.flush()
      println(s"A: Answer ${answerPayload} published")
    }
  })


  def bob(conn:Connection): AsyncSubscription = {
    val questionPayload = "dog"
    val inbox = conn.newInbox()
    val question = new Message(
      subject,
      inbox,
      questionPayload.getBytes(StandardCharsets.UTF_8)
    )
    conn.publish(question)
    conn.flush()
    println(s"B: Question $questionPayload published")
    conn.subscribe(inbox, new MessageHandler {
      override def onMessage(answer: Message): Unit = {
        println("B: Answer " + new String(answer.getData, StandardCharsets.UTF_8) + " received")
      }
    })
  }


}
